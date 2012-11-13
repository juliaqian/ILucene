package com.ilucene;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OneIndexTest {
	
	protected String[] ids = {"1", "2"};
	protected String[] unindexed = {"Netherlands", "Italy"};
	protected String[] unstored = {"Amsterdam has lots of bridges",
	                                 "Venice has lots of canals"};
	protected String[] text = {"Amsterdam", "Venice"};
	
	private Directory directory;
	
	private static Logger logger = LoggerFactory.getLogger(OneIndexTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	    directory = new RAMDirectory();

	    IndexWriter writer = getWriter();           //2

	    for (int i = 0; i < ids.length; i++) {      //3
	      Document doc = new Document();
	      doc.add(new Field("id", ids[i],
	                        Field.Store.YES,
	                        Field.Index.NOT_ANALYZED));
	      doc.add(new Field("country", unindexed[i],
	                        Field.Store.YES,
	                        Field.Index.NO));
	      doc.add(new Field("contents", unstored[i],
	                        Field.Store.NO,
	                        Field.Index.ANALYZED));
	      doc.add(new Field("city", text[i],
	                        Field.Store.YES,
	                        Field.Index.ANALYZED));
	      writer.addDocument(doc);
	    }

	    writer.close();
	}

	@After
	public void tearDown() throws Exception {
	}

	private IndexWriter getWriter() throws IOException {            // 2
		    return new IndexWriter(directory, new WhitespaceAnalyzer(),   // 2
		                           IndexWriter.MaxFieldLength.UNLIMITED); // 2
    }
	
	@Test
	protected int getHitCount(String fieldName, String searchString)
			    throws IOException {
			    IndexSearcher searcher = new IndexSearcher(directory); //4
			    Term t = new Term(fieldName, searchString);
			    Query query = new TermQuery(t);                        //5
			    int hitCount = TestUtil.hitCount(searcher, query);     //6
			    logger.info("hitCount = " + hitCount);
			    searcher.close();
			    return hitCount;
	}

   public void testIndexWriter() throws IOException {
		    IndexWriter writer = getWriter();
		    logger.info("" + writer.numDocs());
		    assertEquals(ids.length, writer.numDocs());            //7
		    writer.close();
   }
	

   public void testIndexReader() throws IOException {
	    IndexReader reader = IndexReader.open(directory);
	    assertEquals(ids.length, reader.maxDoc());             //8
	    assertEquals(ids.length, reader.numDocs());            //8
	    reader.close();
   }

}
