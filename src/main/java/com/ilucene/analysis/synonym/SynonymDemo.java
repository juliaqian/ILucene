package com.ilucene.analysis.synonym;

import com.ilucene.util.CommonUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;

/**
 * @author John Liu
 */
public class SynonymDemo {

    private IndexSearcher searcher;
    private static SynonymAnalyzer synonymAnalyzer =
                           new SynonymAnalyzer(new SynonymEngineImpl());
    public void init(){
        RAMDirectory directory = new RAMDirectory();

        IndexWriter writer = null;
        try {
            writer = new IndexWriter(directory,
                    synonymAnalyzer,  //#1
                    IndexWriter.MaxFieldLength.UNLIMITED);
            Document doc = new Document();
            doc.add(new Field("content",
                    "The quick brown fox jumps over the lazy dog",
                    Field.Store.YES,
                    Field.Index.ANALYZED));  //#2
            writer.addDocument(doc);
            writer.close();
            searcher = new IndexSearcher(directory, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeSearch(){
        try {
            if(searcher != null)searcher.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void searchParser() throws Exception{
        Query query = new QueryParser(Version.LUCENE_30,                   // 1
                "content",                                // 1
                synonymAnalyzer).parse("\"fox jumps\"");  // 1
        assertEquals(1, CommonUtils.hitCount(searcher, query));                   // 1
        System.out.println("With SynonymAnalyzer, \"fox jumps\" parses to " +
                query.toString("content"));

        query = new QueryParser(Version.LUCENE_30,                         // 2
                "content",                                      // 2
                new StandardAnalyzer(Version.LUCENE_30)).parse("\"fox jumps\""); // B
        assertEquals(1, CommonUtils.hitCount(searcher, query));                   // 2
        System.out.println("With StandardAnalyzer, \"fox jumps\" parses to " +
                                                          query.toString("content"));
    }

    public void searchByAPI() throws IOException {
        TermQuery tq = new TermQuery(new Term("content", "hops"));  //#1
        assertEquals(1, CommonUtils.hitCount(searcher, tq));

        PhraseQuery pq = new PhraseQuery();    //#2
        pq.add(new Term("content", "fox"));    //#2
        pq.add(new Term("content", "hops"));   //#2
        assertEquals(1, CommonUtils.hitCount(searcher, pq));
    }

    public static void main(String []args) throws Exception {
        SynonymDemo demo = new SynonymDemo();
        demo.init();
        demo.searchParser();
        demo.searchByAPI();
        demo.closeSearch();
    }

}
