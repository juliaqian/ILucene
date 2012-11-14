package com.ilucene;

import java.io.File;
import java.io.IOException;

import com.ilucene.util.CommonUtil;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ilucene.util.FileUtil;

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
    /**
     * Initializes the index data directory path
     */
	public SearchService(){
		if(indexDir == null){
			indexDir = FileUtil.getIndexDir();
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
        int hitCount = CommonUtil.hitCount(searcher, query);
        logger.debug("hitCount: "+ hitCount);
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
     * Searches the content with specified filed.
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

        for (int i = 0; i < 100; i++) {
            Document doc = new Document();
            doc.add(new Field("keyword", "goober", Field.Store.YES, Field.Index.NOT_ANALYZED));
            writer.addDocument(doc);
        }
        writer.optimize();
        writer.close();
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
        /* Display the lock mechanism for IndexWriter*/
        service.lockMechanism();
        /*-----Display verbose index----*/
        service.showVerboseIndex();
	}
}
