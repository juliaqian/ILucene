package com.ilucene;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import com.ilucene.util.CommonUtil;
import com.ilucene.util.FileUtils;
import junit.framework.Assert;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchService {

	private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
	private static String indexDir = null;

    /*------Defines a few variables for indexing --------------*/
    protected String[] ids = {"1", "2","3"};
    protected String[] unindexed = {"Netherlands", "Italy", "Netherlands"};
    protected String[] unstored = {"Amsterdam has lots of bridges",
                                       "Venice has lots of canals",
                                       "Great city"};
    protected String[] text = { "Venice","Amsterdam", "Venice"};
    private Directory directory;
    private IndexSearcher searcher;
    /**
     * Initializes the index data directory path
     */
	public SearchService(){
		if(indexDir == null){
			indexDir = FileUtils.getIndexDir();
		}
	}

    public boolean initRAMDir(){
        directory = new RAMDirectory();
        IndexWriter writer = null;
        try {
            writer = getWriter();
            if(writer == null){
                return false ;
            }
            for (int i = 0; i < ids.length; i++) {
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
        } catch (IOException e) {
           logger.error("getWriter fails.", e);
        } finally{
            if(writer != null){
                try {
                    writer.close();
                } catch (IOException e) {
                    logger.error("writer closes :", e);
                }
            }
        }
        return true;
    }

    protected int getHitCount(String fieldName, String searchString)
            throws IOException {
        IndexSearcher searcher = new IndexSearcher(directory);
        Term t = new Term(fieldName, searchString);
        Query query = new TermQuery(t);
        int hitCount = CommonUtil.hitCount(searcher, query);  // equels "search(query, 1).totalHits"
        logger.debug("search " + query + " hitCount: "+ hitCount);
        searcher.close();
        return hitCount;
    }
    /**
     *   Gets the IndexWriter instance.
     * @return   IndexWriter
     * @throws IOException
     */
    private IndexWriter getWriter() throws IOException {
        return new IndexWriter(directory, new WhitespaceAnalyzer(),
                IndexWriter.MaxFieldLength.UNLIMITED);
    }
    /**
     * Searches the content with specified TermQuery .
     * @param filed
     *         document filed in lucene
     * @param content
     *         content value with String type
     */
    public void searchContent(String filed, String content){
    	IndexSearcher searcher;
        try {
            Directory dir = FSDirectory.open(new File(indexDir));
            searcher = new IndexSearcher(dir);
            TermQuery termQuery = new TermQuery(new Term(filed, content));
            TopDocs docs = searcher.search(termQuery, 10);
            logger.info("masScore" + docs.getMaxScore());
            logger.info("Total hits: " + docs.totalHits);
        } catch (IOException e) {
            logger.error("Failed to open directory ", e );
        }
    }

    /**
     * Searches the content with specified QueryParser .
     * @param filed
     *         document filed in lucene
     * @param content
     *         content value with String type
     */
    public TopDocs queryParser(String filed, String content){
        TopDocs docs = null;
        try {
            Directory dir = FSDirectory.open(new File(indexDir));
            searcher = new IndexSearcher(dir);
            QueryParser parser = new QueryParser(Version.LUCENE_30,      //A
                    filed,                  //A
                    new SimpleAnalyzer());       //A

            try {
                Query query = parser.parse(content);                  //B
                docs = searcher.search(query, 10);
                logger.info(content + "masScore" + docs.getMaxScore());
                logger.info(filed + "Total hits: " + docs.totalHits);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            logger.error("Failed to open directory ", e );
        }
        return docs;
    }
    /**
     *  A module indexes the author array
     * @throws Exception
     */
    public void indexAuthors() throws Exception {
        String[] authors = new String[] {"lisa", "tom"};
        Document doc = new Document();
        for (String author: authors) {                  //Adds the content to doc
            doc.add(new Field("author", author,
                    Field.Store.YES,
                    Field.Index.ANALYZED));
        }
    }

    /**
     * A module sets the boost factor for a field.
     * @throws IOException
     */
    public void fieldBoostMethod() throws IOException {
        String subject = "That's a good idea!";
        Field subjectField = new Field("subject", subject,
                Field.Store.YES,
                Field.Index.ANALYZED);
        subjectField.setBoost(1.2F);
    }

    /**
     * Shows the IndexWriter that it has the lock mechanism when one IndexWriter exists.
     * @throws IOException
     */
    protected void lockMechanism() throws IOException {
        File tempDir = new File(
                System.getProperty("java.io.tmpdir", "tmp") +
                        System.getProperty("file.separator") + "index");
        FSDirectory dir = FSDirectory.open(tempDir);

        IndexWriter writer1 = new IndexWriter(dir, new SimpleAnalyzer(),
                                       IndexWriter.MaxFieldLength.UNLIMITED);
        IndexWriter writer2 = null;
        /*--The IndexWriter 2 stance will never be done because there is an IndexWriter that locks the operation.*/
        try {
            writer2 = new IndexWriter(dir, new SimpleAnalyzer(),
                    IndexWriter.MaxFieldLength.UNLIMITED);
            logger.warn("We should never reach this point");
        }
        catch (LockObtainFailedException e) {
            // e.printStackTrace();  // #A
        }
        finally {
            writer1.close();
            logger.warn("write2 can't be instantiated because it is "+ writer2 );
            CommonUtil.rmDir(tempDir);
        }
    }

    /**
     * Optimizes the verbose index.
     * @throws IOException
     */
    protected void showVerboseIndex() throws IOException {
        Directory dir = new RAMDirectory();

        IndexWriter writer = new IndexWriter(dir, new WhitespaceAnalyzer(),
                IndexWriter.MaxFieldLength.UNLIMITED);

        writer.setInfoStream(System.out);
        logger.debug("Optimise the verbose index filed");
        for (int i = 0; i < 100; i++) {
            Document doc = new Document();
            doc.add(new Field("keyword", "goober", Field.Store.YES, Field.Index.NOT_ANALYZED));
            writer.addDocument(doc);
        }
        /**--It does many things on the hood.---**/
        writer.optimize();
        writer.close();
    }


    protected void validateQueryParser(){
        TopDocs topDocs = queryParser("contents", "+ArgoUML +UML -Eclipse");
        Assert.assertTrue(topDocs.totalHits == 1)  ;
        try {
            Document doc = searcher.doc(topDocs.scoreDocs[0].doc);
            Assert.assertEquals("contents:+ArgoUML +UML -Eclipse", "ArgoUML-1.txt", doc.get("fileName"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        TopDocs topDocs1 = queryParser("contents", "ArgoUML AND UML OR Eclipse");
        Assert.assertTrue(topDocs1.totalHits == 2)  ;
    }

    /**
     * Searches the content near real time.
     * /*
     #1 Create near-real-time reader
     #A Wrap reader in IndexSearcher
     #B Search returns 10 hits
     #2 Delete 1 document
     #3 Add 1 document
     #4 Reopen reader
     #5 Confirm reader is new
     #6 Close old reader
     #7 Verify 9 hits now
     #8 Confirm new document matched
     */
    protected void searchNRT() throws IOException {
        Directory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir,
                                   new StandardAnalyzer(Version.LUCENE_30),
                                   IndexWriter.MaxFieldLength.UNLIMITED);
        for(int i=0;i<10;i++) {
            Document doc = new Document();
            doc.add(new Field("id", ""+i,
                    Field.Store.NO,
                    Field.Index.NOT_ANALYZED_NO_NORMS));
            doc.add(new Field("text", "aaa",
                    Field.Store.NO,
                    Field.Index.ANALYZED));
            writer.addDocument(doc);
        }
        IndexReader reader = writer.getReader();                 // #1
        IndexSearcher searcher = new IndexSearcher(reader);      // #A

        Query query = new TermQuery(new Term("text", "aaa"));
        TopDocs docs = searcher.search(query, 1);
        Assert.assertEquals(10, docs.totalHits);                        // #B

        writer.deleteDocuments(new Term("id", "7"));             // #2

        Document doc = new Document();                           // #3
        doc.add(new Field("id",                                  // #3
                "11",                                  // #3
                Field.Store.NO,                        // #3
                Field.Index.NOT_ANALYZED_NO_NORMS));   // #3
        doc.add(new Field("text",                                // #3
                "bbb",                                 // #3
                Field.Store.NO,                        // #3
                Field.Index.ANALYZED));                // #3
        writer.addDocument(doc);                                 // #3

        IndexReader newReader = reader.reopen();                 // #4
        Assert.assertFalse(reader == newReader);                        // #5
        reader.close();                                          // #6
        searcher = new IndexSearcher(newReader);

        TopDocs hits = searcher.search(query, 10);               // #7
        Assert.assertEquals(9, hits.totalHits);                         // #7

        query = new TermQuery(new Term("text", "bbb"));          // #8
        hits = searcher.search(query, 1);                        // #8
        Assert.assertEquals(1, hits.totalHits);                         // #8

        newReader.close();
        writer.close();
    }

    protected void queryByPhrase() throws IOException {
        Directory dir;
        IndexSearcher searcher;
        dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir,
                new WhitespaceAnalyzer(),
                IndexWriter.MaxFieldLength.UNLIMITED);
        Document doc = new Document();
        doc.add(new Field("field",                                    // 1
                    "the quick brown fox jumped over the lazy dog",     // 1
                    Field.Store.YES,                                    // 1
                    Field.Index.ANALYZED));                             // 1
        writer.addDocument(doc);
        writer.close();

        searcher = new IndexSearcher(dir);

        PhraseQuery query = new PhraseQuery();              // 2
        String[] phrase = new String[] {"quick", "fox"};
        for (String word : phrase) {             // 3
            query.add(new Term("field", word));          // 3
        }                                                   // 3
        query.setSlop(1);                                // 2
        TopDocs matches = searcher.search(query, 10);
        Assert.assertTrue( matches.totalHits == 1);
    }

    protected void indexSingleFieldDocs(Field[] fields) throws IOException {
        directory = new RAMDirectory();
        IndexWriter writer = new IndexWriter(directory, new WhitespaceAnalyzer(),
                                             IndexWriter.MaxFieldLength.UNLIMITED);
        for (Field f : fields) {
            Document doc = new Document();
            doc.add(f);
            writer.addDocument(doc);
        }
        writer.optimize();
        writer.close();
    }

    protected void searchByWildcard() throws IOException {
        directory  = new RAMDirectory();
        indexSingleFieldDocs(new Field[]
                { new Field("contents", "wild", Field.Store.YES, Field.Index.ANALYZED),
                        new Field("contents", "child", Field.Store.YES, Field.Index.ANALYZED),
                        new Field("contents", "mild", Field.Store.YES, Field.Index.ANALYZED),
                        new Field("contents", "mildew", Field.Store.YES, Field.Index.ANALYZED) });

        IndexSearcher searcher = new IndexSearcher(directory);
        Query query = new WildcardQuery(new Term("contents", "?ild*"));  //#A
        TopDocs matches = searcher.search(query, 10);
        logger.debug( "--------------------------" + matches.totalHits);
        Assert.assertEquals("child no match", 3, matches.totalHits);

        Assert.assertEquals("score the same", matches.scoreDocs[0].score,
                matches.scoreDocs[1].score, 0.0);
        Assert.assertEquals("score the same", matches.scoreDocs[1].score,
                matches.scoreDocs[2].score, 0.0);
        searcher.close();
        directory.close();
    }

    protected void searchByFuzzy() throws IOException {
        directory  = new RAMDirectory();
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
        Assert.assertEquals("both close enough", 2, matches.totalHits);
        logger.debug("matches.scoreDocs[0].score: " + matches.scoreDocs[0].score+ "\n" +
                      "matches.scoreDocs[1].score:" + matches.scoreDocs[1].score);
        Assert.assertTrue("wuzzy closer than fuzzy",
                matches.scoreDocs[0].score != matches.scoreDocs[1].score);

        Document doc = searcher.doc(matches.scoreDocs[0].doc);
        Assert.assertEquals("wuzza bear", "wuzzy", doc.get("contents"));
        searcher.close();
    }

    public static class SimpleSimilarity extends Similarity {
        public float lengthNorm(String field, int numTerms) {
            return 1.0f;
        }

        public float queryNorm(float sumOfSquaredWeights) {
            return 1.0f;
        }

        public float tf(float freq) {
            return freq;
        }

        public float sloppyFreq(int distance) {
            return 2.0f;
        }

        public float idf(Vector terms, Searcher searcher) {
            return 1.0f;
        }

        public float idf(int docFreq, int numDocs) {
            return 1.0f;
        }

        public float coord(int overlap, int maxOverlap) {
            return 1.0f;
        }
    }

    /**
     * <p>This is intended to be used in developing Similarity implementations,
     * and, for good performance, should not be displayed with every hit.
     * Computing an explanation is as expensive as executing the query over the
     * entire index.
     * </p>
     * @throws IOException
     */
    protected void doSimpleSearch() throws IOException {
        directory  = new RAMDirectory();
        indexSingleFieldDocs(new Field[] {new Field("contents", "x z", Field.Store.YES, Field.Index.ANALYZED),
                                           new Field("contents", "w x y", Field.Store.YES, Field.Index.ANALYZED)});
        IndexSearcher searcher = new IndexSearcher(directory);
        searcher.setSimilarity(new SimpleSimilarity());

        Query query = new TermQuery(new Term("contents", "x"));
        Explanation explanation = searcher.explain(query, 0);
        logger.debug("[TermQuery(new Term(\"contents\", \"x\"));]:" + explanation);

        TopDocs matches = searcher.search(query, 10);
        logger.debug("doc count: " +  matches.totalHits);
        //Assert.assertEquals(1, matches.totalHits);
        logger.debug("matches.scoreDocs[0].score: " + matches.scoreDocs[0].score);
       // Assert.assertEquals(1F, scroe_0, 0.0);
        logger.debug("matches.scoreDocs[0].score: " +  matches.scoreDocs[1].score);

        searcher.close();
    }
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		SearchService service = new SearchService();
		service.searchContent("contents", "license");

        /*----Test the RAMDirectory index and query----*/
        if(service.initRAMDir()){
            int cityNum = service.getHitCount("city", "Venice");
            logger.info("-------hit city: " + cityNum+ "\n"
                        + System.getProperty("java.io.tmpdir", "tmp")
                       + "\nindex directory:" +System.getProperty("user.home") );
        }
        //Validates the QueryParser
        service.validateQueryParser();

        // search near real time
        service.searchNRT();

        //search by phrase
        service.queryByPhrase();

        //search by fuzzy
        service.searchByFuzzy();

        //Does s simple search
        service.doSimpleSearch();
        /* Display the lock mechanism for IndexWriter*/
      //  service.lockMechanism();
        /*-----Display verbose index----*/
      //  service.showVerboseIndex();
	}
}
