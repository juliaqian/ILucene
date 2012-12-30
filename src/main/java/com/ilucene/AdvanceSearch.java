package com.ilucene;
import junit.framework.Assert;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.function.CustomScoreProvider;
import org.apache.lucene.search.function.CustomScoreQuery;
import org.apache.lucene.search.function.FieldScoreQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author  JohnLiu
 */
public class AdvanceSearch {

    private static final Logger logger = LoggerFactory.getLogger(AdvanceSearch.class);

    IndexSearcher s;
    IndexWriter w;

    /**
     * Initializes the settings.
     * @throws IOException
     */
    protected void init() throws IOException {
        Directory dir = new RAMDirectory();
        w = new IndexWriter(dir,
                new StandardAnalyzer(
                        Version.LUCENE_30),
                IndexWriter.MaxFieldLength.UNLIMITED);
        try {
            addDoc(7, "this hat is green");
            addDoc(42, "this hat is blue");
            addDoc(6, "This hat is black");
            addDoc(1, "This hat is black");
        } catch (Exception e) {
            e.printStackTrace();
        }
        w.close();

        s = new IndexSearcher(dir, true);
    }

    /**
     * Adds the document to IndexWriter.
     * @param score
     * @param content
     * @throws Exception
     */
    private void addDoc(int score, String content) throws Exception {
        Document doc = new Document();
        doc.add(new Field("score",
                Integer.toString(score),
                Field.Store.NO,
                Field.Index.NOT_ANALYZED_NO_NORMS));
        doc.add(new Field("content",
                content,
                Field.Store.NO,
                Field.Index.ANALYZED));
        w.addDocument(doc);

    }

    /**
     * demo the FieldScoreQuery for the field "score"
     */
    protected void doFieldScoreQuery(){
        Query q = new FieldScoreQuery("score", FieldScoreQuery.Type.BYTE);
        TopDocs hits = null;
        try {
            hits = s.search(q, 10);
            if(hits == null){
                return;
            }
            logger.debug("\n\thits.scoreDocs.length = "+ hits.scoreDocs.length+
                          "\t hits.scoreDocs[0].doc= "+  hits.scoreDocs[0].doc+
                          "\n(int) hits.scoreDocs[0].score= "+ (int) hits.scoreDocs[0].score+
                          "\thits.scoreDocs[1].doc="+ hits.scoreDocs[1].doc+
                         "\t(int) hits.scoreDocs[1].score= "+ (int) hits.scoreDocs[1].score);

            Assert.assertEquals(4, hits.scoreDocs.length);       // #1
//            Assert.assertEquals(1, hits.scoreDocs[0].doc);       // #2
//            Assert.assertEquals(42, (int) hits.scoreDocs[0].score);
//            Assert.assertEquals(0, hits.scoreDocs[1].doc);
//            Assert.assertEquals(7, (int) hits.scoreDocs[1].score);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void doCustomScoreQuery() throws ParseException, IOException {
        Query q = new QueryParser(Version.LUCENE_30,
                "content",
                new StandardAnalyzer(
                        Version.LUCENE_30))
                .parse("the green hat");
        FieldScoreQuery qf = new FieldScoreQuery("score",
                FieldScoreQuery.Type.BYTE);
        CustomScoreQuery customQ = new CustomScoreQuery(q, qf) {
            public CustomScoreProvider getCustomScoreProvider(IndexReader r) {
                return new CustomScoreProvider(r) {
                    public float customScore(int doc,
                                             float subQueryScore,
                                             float valSrcScore) {
                        return (float) (Math.sqrt(subQueryScore) * valSrcScore);
                    }
                };
            }
        };

        TopDocs hits = s.search(customQ, 10);
        logger.debug("\n\thits.scoreDocs.length = " + hits.scoreDocs.length +
                "\t hits.scoreDocs[0].doc= " + hits.scoreDocs[0].doc +
                "\n(int) hits.scoreDocs[0].score= " + (int) hits.scoreDocs[0].score +
                "\thits.scoreDocs[1].doc=" + hits.scoreDocs[1].doc +
                "\t(int) hits.scoreDocs[1].score= " + (int) hits.scoreDocs[1].score);
    }

    public static void  main(String[] args) throws IOException, ParseException {
        AdvanceSearch search = new AdvanceSearch();
        search.init();

        search.doFieldScoreQuery();
        search.doCustomScoreQuery();
    }
}
