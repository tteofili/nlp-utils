package com.github.tteofili.nlputils.lucene;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.classification.ClassificationResult;
import org.apache.lucene.classification.Classifier;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.StorableField;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.BytesRef;

/**
 * A perceptron (see <code>http://en.wikipedia.org/wiki/Perceptron</code>) based
 * <code>Boolean</code> {@link org.apache.lucene.classification.Classifier}.
 * The weights are calculated using {@link org.apache.lucene.index.TermsEnum#totalTermFreq}.
 */
public class BooleanPerceptronClassifier implements Classifier<Boolean> {

  private Map<String, Double> weights = new HashMap<String, Double>();
  private Terms textTerms;
  private Analyzer analyzer;
  private String textFieldName;
  private Double threshohld;

  public BooleanPerceptronClassifier(Double threshohld) {
    this.threshohld = threshohld;
  }

  public BooleanPerceptronClassifier() {
    this(10d);
  }

  @Override
  public ClassificationResult<Boolean> assignClass(String text) throws IOException {
    if (textTerms == null) {
      throw new IOException("You must first call Classifier#train()");
    }
    Double output = 0d;
    TokenStream tokenStream = analyzer.tokenStream(textFieldName, new StringReader(text));
    CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);
    while (tokenStream.incrementToken()) {
      String s = charTermAttribute.toString();
      Double d = weights.get(s);
      if (d != null && d > 0) {
        output += d;
      }
    }
    return new ClassificationResult<Boolean>(getClassFromOutput(output), output);
  }

  private Boolean getClassFromOutput(Double output) {
    return output >= threshohld;
  }

  @Override
  public void train(AtomicReader atomicReader, String textFieldName, String classFieldName, Analyzer analyzer) throws IOException {
    textTerms = MultiFields.getTerms(atomicReader, textFieldName);
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
        ClassificationResult<Boolean> classificationResult = assignClass(indexSearcher.doc(scoreDoc.doc).getField(textFieldName).stringValue());
        Boolean assignedClass = classificationResult.getAssignedClass();
        if (assignedClass != null) {
            StorableField field = indexSearcher.doc(scoreDoc.doc).getField(classFieldName);
            double modifier = calculateModifier(assignedClass, Boolean.valueOf(field.stringValue()));
          if (modifier != 0) {
            String termString = cte.term().utf8ToString();
            long termFreqLocal = termsEnum.totalTermFreq();
            weights.put(termString, weights.get(termString) + modifier * termFreqLocal);
          }
        }
      }
      reuse = cte;
    }
  }

    private double calculateModifier(Boolean assignedClass, Boolean correctClass) {
        return correctClass.compareTo(assignedClass);
    }

}
