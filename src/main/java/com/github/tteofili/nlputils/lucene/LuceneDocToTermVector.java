package com.github.tteofili.nlputils.lucene;

import java.io.IOException;

import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

/**
 * utility class for converting Lucene {@link org.apache.lucene.document.Document}s to vectors.
 */
public class LuceneDocToTermVector {

  public static Double[] toSparseLocalFreqDoubleArray(Terms docTerms, Terms fieldTerms) throws IOException {

    TermsEnum fieldTermsEnum = fieldTerms.iterator(null);
    Double[] freqVector = null;
    if (docTerms != null && fieldTerms.size() > 0) {
      freqVector = new Double[(int) fieldTerms.size()];
      int i = 0;
      TermsEnum docTermsEnum = docTerms.iterator(null);
      BytesRef term;
      while ((term = fieldTermsEnum.next()) != null) {
        boolean found = false;
        BytesRef cdt;
        while ((cdt = docTermsEnum.next()) != null) {
          if (cdt.bytesEquals(term)) {
            long termFreqLocal = docTermsEnum.totalTermFreq(); // the total number of occurrences of this term in the given document
            freqVector[i] = Long.valueOf(termFreqLocal).doubleValue();
            found = true;
            break;
          }
        }
        if (!found) {
          freqVector[i] = 0d;
        }
        docTermsEnum = docTerms.iterator(docTermsEnum);

        i++;
      }
    }
    return freqVector;
  }

  public static Double[] toDenseLocalFreqDoubleArray(Terms docTerms) throws IOException {

    Double[] freqVector = null;
    if (docTerms != null) {
      freqVector = new Double[(int) docTerms.size()];
      int i = 0;
      TermsEnum docTermsEnum = docTerms.iterator(null);

      while (docTermsEnum.next() != null) {
        long termFreqLocal = docTermsEnum.totalTermFreq(); // the total number of occurrences of this term in the given document
        freqVector[i] = Long.valueOf(termFreqLocal).doubleValue();
        i++;
      }
    }
    return freqVector;
  }
}
