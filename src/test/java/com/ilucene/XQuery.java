/**
 * 
 */
package com.ilucene;

import static org.junit.Assert.*;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author JohnLiu
 *
 */
public class XQuery {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private Directory directory;

	public void setUp() throws Exception {
	    directory = new RAMDirectory();
	}
	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}


	private void indexSingleFieldDocs(Field[] fields) throws Exception {
		IndexWriter writer = new IndexWriter(directory,
		new WhitespaceAnalyzer(), IndexWriter.MaxFieldLength.UNLIMITED);
		for (Field f : fields) {
			Document doc = new Document();
			doc.add(f);
			writer.addDocument(doc);
		}
		writer.optimize();
		writer.close();
	}
	
	@Test
	public void testWildcard() throws Exception {
		indexSingleFieldDocs(new Field[] { new Field("contents", "wild", Field.Store.YES,
										Field.Index.ANALYZED),
										new Field("contents", "child", Field.Store.YES,
										Field.Index.ANALYZED),
										new Field("contents", "mild", Field.Store.YES,
										Field.Index.ANALYZED),
										new Field("contents", "mildew", Field.Store.YES,
										Field.Index.ANALYZED)
										});
		IndexSearcher searcher = new IndexSearcher(directory);
		Query query = new WildcardQuery(new Term("contents", "?ild*"));
		TopDocs matches = searcher.search(query, 10);
		assertEquals("child no match", 3, matches.totalHits);
		assertEquals("score the same", matches.scoreDocs[0].score,
		matches.scoreDocs[1].score, 0.0);
		assertEquals("score the same", matches.scoreDocs[1].score,
		matches.scoreDocs[2].score, 0.0);
		searcher.close();
	}

	
	public void testFuzzy() throws Exception {
		indexSingleFieldDocs(new Field[] { new Field("contents",
											"fuzzy",
											Field.Store.YES,
											Field.Index.ANALYZED),
											new Field("contents",
											"wuzzy",
											Field.Store.YES,
											Field.Index.ANALYZED)
											});
		IndexSearcher searcher = new IndexSearcher(directory);
		Query query = new FuzzyQuery(new Term("contents", "wuzza"));
		TopDocs matches = searcher.search(query, 10);
		assertEquals("both close enough", 2, matches.totalHits);
		assertTrue("wuzzy closer than fuzzy",
		matches.scoreDocs[0].score != matches.scoreDocs[1].score);
		Document doc = searcher.doc(matches.scoreDocs[0].doc);
		
	}
}
