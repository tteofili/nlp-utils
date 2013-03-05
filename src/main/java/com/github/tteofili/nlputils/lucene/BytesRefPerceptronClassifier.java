package com.github.tteofili.nlputils.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.classification.ClassificationResult;
import org.apache.lucene.classification.Classifier;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * A perceptron (see <code>http://en.wikipedia.org/wiki/Perceptron</code>) based {@link Classifier}.
 * The weights and output are calculated using {@link TermsEnum#totalTermFreq}.
 */
public class BytesRefPerceptronClassifier implements Classifier<BytesRef> {

  private Map<String, Double> weights = new HashMap<String, Double>();
  private Terms textTerms;
  private Analyzer analyzer;
  private String textFieldName;
  private Terms classTerms;

  @Override
  public ClassificationResult<BytesRef> assignClass(String text) throws IOException {
    if (textTerms == null) {
      throw new IOException("You must first call Classifier#train()");
    }
    Double output = 0d;
    TokenStream tokenStream = analyzer.tokenStream(textFieldName, new StringReader(text));
    while (tokenStream.incrementToken()) {
      CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);
      Double d = weights.get(String.valueOf(charTermAttribute.buffer()));
      if (d != null && d > 0) {
        // TODO : decide if there should be some other multiplier (e.g. local/global term/doc freq) or (better) make this configurable
        output += d;
      }
    }
    return new ClassificationResult<BytesRef>(getClassFromOutput(), output);
  }

    private BytesRef getClassFromOutput() {
      // TODO : implement this
      return null;
    }

    @Override
  public void train(AtomicReader atomicReader, String textFieldName, String classFieldName, Analyzer analyzer) throws IOException {
    classTerms = MultiFields.getTerms(atomicReader, classFieldName);
    textTerms = MultiFields.getTerms(atomicReader, textFieldName);
    classTerms = textTerms = MultiFields.getTerms(atomicReader, classFieldName);
    this.analyzer = analyzer;
    this.textFieldName = textFieldName;

    TermsEnum reuse = textTerms.iterator(null);
    BytesRef textTerm;
    while ((textTerm = reuse.next()) != null) {
        weights.put(textTerm.utf8ToString(), (double) reuse.totalTermFreq());
    }

    IndexSearcher indexSearcher = new IndexSearcher(atomicReader);
    // for each doc
    for (ScoreDoc scoreDoc : indexSearcher.search(new MatchAllDocsQuery(), Integer.MAX_VALUE).scoreDocs) {
      TermsEnum cte = textTerms.iterator(reuse);
      // get the term vectors
      Terms terms = atomicReader.getTermVector(scoreDoc.doc, textFieldName);

      TermsEnum termsEnum = terms.iterator(null);

      BytesRef term;
      while ((term = termsEnum.next()) != null) {
        cte.seekExact(term, true);
        ClassificationResult<BytesRef> classificationResult = assignClass(indexSearcher.doc(scoreDoc.doc).getField(textFieldName).stringValue());
        BytesRef assignedClass = classificationResult.getAssignedClass();
        if (assignedClass != null) {
          double modifier = calculateModifier(assignedClass, indexSearcher.doc(scoreDoc.doc).getField(classFieldName).binaryValue());
          if (modifier != 0) {
            String termString = cte.term().utf8ToString();
            long termFreqLocal = termsEnum.totalTermFreq();
//          int docFreqOverall = cte.docFreq();
//          long termFreqOverall = cte.totalTermFreq();
//          System.err.println(termString + " : " + docFreqOverall + " - " + termFreqOverall + " - " + termFreqLocal);
            weights.put(termString, weights.get(termString) + modifier * termFreqLocal);
          }
        }
      }
      reuse = cte;
    }
  }

  private double calculateModifier(BytesRef assignedClass, BytesRef correctClass) throws IOException {
    double modifier = 0;
    TermsEnum iterator = classTerms.iterator(null);
    if (!assignedClass.equals(correctClass)) {
      BytesRef next = iterator.next();
      long assignedOutput = 0l;
      long correctOutput = 0l;
      while (next != null) {
        if (assignedClass.utf8ToString().equals(next.utf8ToString())) {
          assignedOutput = iterator.totalTermFreq();
        }
        else if (correctClass.utf8ToString().equals(next.utf8ToString())) {
          correctOutput = iterator.totalTermFreq();
        }

        if (assignedOutput > 0 && correctOutput > 0) {
          break;
        }

      }
      modifier = correctOutput - assignedOutput;
    }
    return modifier;
  }
}
