package com.github.tteofili.nlputils.lucene;

import java.util.Arrays;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.MockAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.RandomIndexWriter;
import org.apache.lucene.index.Terms;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.LuceneTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.tteofili.nlputils.lucene.LuceneDocToTermVector;

/**
 * Testcase for {@link com.github.tteofili.nlputils.lucene.LuceneDocToTermVector}
 */
public class LuceneDocToTermVectorTest extends LuceneTestCase {

  private IndexReader index;
  private Directory dir;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    dir = newDirectory();
    RandomIndexWriter indexWriter = new RandomIndexWriter(random(), dir);

    FieldType ft = new FieldType(TextField.TYPE_STORED);
    ft.setStoreTermVectors(true);
    ft.setStoreTermVectorOffsets(true);
    ft.setStoreTermVectorPositions(true);

    Analyzer analyzer = new MockAnalyzer(random());

    Document doc;
    for (int i = 0; i < 100; i++) {
      doc = new Document();
      doc.add(new Field("id", Integer.toString(i), ft));
      doc.add(new Field("text", String.valueOf(i % 31) + " " + String.valueOf(i % 32) + " " + String.valueOf(i % 33), ft));
      indexWriter.addDocument(doc, analyzer);
    }

    indexWriter.commit();

    index = indexWriter.getReader();

    indexWriter.close();
  }

  @Override
  @After
  public void tearDown() throws Exception {
    index.close();
    dir.close();
    super.tearDown();
  }

  @Test
  public void testDenseFreqDoubleArrayConversion() throws Exception {
    IndexSearcher indexSearcher = new IndexSearcher(index);
    for (ScoreDoc scoreDoc : indexSearcher.search(new MatchAllDocsQuery(), Integer.MAX_VALUE).scoreDocs) {
      Terms docTerms = index.getTermVector(scoreDoc.doc, "text");
      Double[] vector = LuceneDocToTermVector.toDenseFreqDoubleArray(docTerms);
      assertNotNull(vector);
      assertTrue(vector.length > 0);
    }
  }

  @Test
  public void testSparseFreqDoubleArrayConversion() throws Exception {
    Terms fieldTerms = MultiFields.getTerms(index, "text");
    IndexSearcher indexSearcher = new IndexSearcher(index);
    int i = 1;
    for (ScoreDoc scoreDoc : indexSearcher.search(new MatchAllDocsQuery(), Integer.MAX_VALUE).scoreDocs) {
      Terms docTerms = index.getTermVector(scoreDoc.doc, "text");
      Double[] vector = LuceneDocToTermVector.toSparseFreqDoubleArray(docTerms, fieldTerms);
      System.err.println(Arrays.toString(vector) +" > "+i);
      assertNotNull(vector);
      assertTrue(vector.length > 0);
      i++;
    }
  }
}
