package com.github.tteofili.nlputils.lucene;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.classification.ClassificationResult;
import org.apache.lucene.classification.Classifier;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.fst.Builder;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.PositiveIntOutputs;
import org.apache.lucene.util.fst.Util;

/**
 * A perceptron (see <code>http://en.wikipedia.org/wiki/Perceptron</code>) based
 * <code>Boolean</code> {@link org.apache.lucene.classification.Classifier}.
 * The weights are calculated using {@link org.apache.lucene.index.TermsEnum#totalTermFreq}
 * both on a per field and a per document basis and then a corresponding {@link org.apache.lucene.util.fst.FST} is used for class assignment.
 */
public class BooleanPerceptronClassifier implements Classifier<Boolean> {

  private final Double threshold;
  private Terms textTerms;
  private Analyzer analyzer;
  private String textFieldName;
  private FST<Long> fst;

  /**
   * Create a {@link BooleanPerceptronClassifier}
   *
   * @param threshold the binary threshold for perceptron output evaluation
   */
  public BooleanPerceptronClassifier(Double threshold) {
    this.threshold = threshold;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ClassificationResult<Boolean> assignClass(String text) throws IOException {
    if (textTerms == null) {
      throw new IOException("You must first call Classifier#train first");
    }
    Long output = 0l;
    // TODO : make this a FST traversal
    TokenStream tokenStream = analyzer.tokenStream(textFieldName, new StringReader(text));
    CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
    tokenStream.reset();
    while (tokenStream.incrementToken()) {
      String s = charTermAttribute.toString();
      Long d = Util.get(fst, new BytesRef(s));
      if (d != null && d > 0) {
        output += d;
      }
    }
    tokenStream.end();
    tokenStream.close();

    return new ClassificationResult<Boolean>(output >= threshold, output.doubleValue());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void train(AtomicReader atomicReader, String textFieldName, String classFieldName, Analyzer analyzer) throws IOException {
    this.textTerms = MultiFields.getTerms(atomicReader, textFieldName);
    this.analyzer = analyzer;
    this.textFieldName = textFieldName;

    // TODO : remove this map which makes things so much slow
    SortedMap<String, Double> weights = new TreeMap<String, Double>(); // this needs to be sorted to make FST update work

    TermsEnum reuse = textTerms.iterator(null);
    BytesRef textTerm;
    while ((textTerm = reuse.next()) != null) {
      weights.put(textTerm.utf8ToString(), (double) reuse.totalTermFreq());
    }
    updateFST(weights);

    IndexSearcher indexSearcher = new IndexSearcher(atomicReader);

    BinaryDocValues textDocValues = atomicReader.getBinaryDocValues(textFieldName);
    BinaryDocValues classDocValues = atomicReader.getBinaryDocValues(classFieldName);

    // use (binary) doc values if available
    if (textDocValues != null && !BinaryDocValues.EMPTY.equals(textDocValues) && classDocValues != null &&
            !BinaryDocValues.EMPTY.equals(classDocValues)) {
      int i = 1;
      BytesRef textBytes = new BytesRef();
      textDocValues.get(i, textBytes);
      BytesRef classBytes = new BytesRef();
      textDocValues.get(i, classBytes);

      while (atomicReader.document(i) != null && textBytes.bytes != null &&
              classBytes.bytes != null) {
        ClassificationResult<Boolean> classificationResult = assignClass(textBytes.utf8ToString());
        Boolean assignedClass = classificationResult.getAssignedClass();

        Boolean correctClass = Boolean.valueOf(classBytes.utf8ToString());
        long modifier = correctClass.compareTo(assignedClass);
        if (modifier != 0) {
          reuse = updateWeights(atomicReader, reuse, i, assignedClass, weights, modifier);
        }

        i++;
        textDocValues.get(i, textBytes);
        classDocValues.get(i, classBytes);
      }
    } else {
      // do a *:* search and use stored field values
      for (ScoreDoc scoreDoc : indexSearcher.search(new MatchAllDocsQuery(), Integer.MAX_VALUE).scoreDocs) {
        Document doc = indexSearcher.doc(scoreDoc.doc);

        // assign class to the doc
        ClassificationResult<Boolean> classificationResult = assignClass(doc.getField(textFieldName).stringValue());
        Boolean assignedClass = classificationResult.getAssignedClass();

        // get the expected result
        IndexableField field = doc.getField(classFieldName);

        Boolean correctClass = Boolean.valueOf(field.stringValue());
        long modifier = correctClass.compareTo(assignedClass);
        if (modifier != 0) {
          reuse = updateWeights(atomicReader, reuse, scoreDoc.doc, assignedClass, weights, modifier);
        }
      }
      weights.clear(); // free memory while waiting for GC
    }
  }

  private TermsEnum updateWeights(AtomicReader atomicReader, TermsEnum reuse,
                                  int docId, Boolean assignedClass, SortedMap<String, Double> weights, double modifier) throws IOException {
    TermsEnum cte = textTerms.iterator(reuse);

    // get the doc term vectors
    Terms terms = atomicReader.getTermVector(docId, textFieldName);

    if (terms == null) {
      throw new IOException("term vectors must be stored for field " + textFieldName);
    }

    TermsEnum termsEnum = terms.iterator(null);

    BytesRef term;
//    BytesRef scratchBytes = new BytesRef();
//    IntsRef scratchInts = new IntsRef();

    while ((term = termsEnum.next()) != null) {
      cte.seekExact(term);
      if (assignedClass != null) {
        long termFreqLocal = termsEnum.totalTermFreq();
        // update weights
        Long previousValue = Util.get(fst, term);
//        Double previousValue = weights.get(term.utf8ToString());
        String termString = term.utf8ToString();
//        Double previousValue = weights.get(termString);
        weights.put(termString, previousValue + modifier * termFreqLocal);
//        scratchBytes.copyChars(term.utf8ToString());
//        IntsRef intsRef = Util.toIntsRef(scratchBytes, scratchInts);
//        Util.update(fst, intsRef, previousValue + modifier * termFreqLocal);
//        fstBuilder.add(intsRef, previousValue + modifier * termFreqLocal);
      }
    }
    updateFST(weights);
    reuse = cte;
    return reuse;
  }

  private void updateFST(SortedMap<String, Double> weights) throws IOException {
    PositiveIntOutputs outputs = PositiveIntOutputs.getSingleton();
    Builder<Long> fstBuilder = new Builder<Long>(FST.INPUT_TYPE.BYTE1
            , outputs);
    BytesRef scratchBytes = new BytesRef();
    IntsRef scratchInts = new IntsRef();
    for (Map.Entry<String, Double> entry : weights.entrySet()) {
      scratchBytes.copyChars(entry.getKey());
      fstBuilder.add(Util.toIntsRef(scratchBytes, scratchInts), entry.getValue().longValue());
    }
    fst = fstBuilder.finish();
  }

}