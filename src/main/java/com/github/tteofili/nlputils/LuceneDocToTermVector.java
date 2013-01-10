package com.github.tteofili.nlputils;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

/**
 * utility class for converting Lucene {@link org.apache.lucene.document.Document}s to vectors.
 */
public class LuceneDocToTermVector {

  public static Double[] toDenseFreqDoubleArray(Terms docTerms, Terms fieldTerms) throws IOException {

    TermsEnum fieldTermsEnum = fieldTerms.iterator(null);
    Double[] freqVector = null;
    if (docTerms != null) {
      freqVector = new Double[(int) docTerms.size()];
      int i = 0;
      TermsEnum docTermsEnum = docTerms.iterator(null);

      BytesRef term;
      while ((term = docTermsEnum.next()) != null) {
        fieldTermsEnum.seekExact(term, true);
        String termString = fieldTermsEnum.term().utf8ToString();
        long termFreqLocal = docTermsEnum.totalTermFreq(); // the total number of occurrences of this term in the given document
        int docFreqOverall = fieldTermsEnum.docFreq(); // the number of documents containing the current term
        long termFreqOverall = fieldTermsEnum.totalTermFreq(); // the total number of occurrences of this term across all documents
        System.err.println(termString + " : " + docFreqOverall + " - " + termFreqOverall + " - " + termFreqLocal);
        freqVector[i] = Long.valueOf(termFreqLocal).doubleValue();
        i++;
      }
    }
    System.err.println(Arrays.toString(freqVector));
    return freqVector;
  }
}
