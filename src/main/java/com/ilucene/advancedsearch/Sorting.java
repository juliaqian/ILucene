package com.ilucene.advancedsearch;

import com.ilucene.util.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;

/**
 * @author John Liu
 */
public class Sorting {
    private Directory directory;

    public Sorting(Directory directory){
        this.directory = directory;
    }

    public void displayResults(Query query, Sort sort)            // #1
            throws IOException {
        IndexSearcher searcher = new IndexSearcher(directory);

        searcher.setDefaultFieldSortScoring(true, false);            // #2

        TopDocs results = searcher.search(query, null,         // #3
                20, sort);           // #3

        System.out.println("\nResults for: " +                      // #4
                query.toString() + " sorted by " + sort);

        System.out.println(StringUtils.rightPad("Title", 30) +
                StringUtils.rightPad("pubmonth", 10) +
                StringUtils.center("id", 4) +
                StringUtils.center("score", 15));

        PrintStream out = new PrintStream(System.out, true, "UTF-8");    // #5

        DecimalFormat scoreFormatter = new DecimalFormat("0.######");
        for (ScoreDoc sd : results.scoreDocs) {
            int docID = sd.doc;
            float score = sd.score;
            Document doc = searcher.doc(docID);
            out.println(
                    StringUtils.rightPad(                                                  // #6
                            StringUtils.abbreviate(doc.get("title"), 29), 30) +                // #6
                            StringUtils.rightPad(doc.get("pubmonth"), 10) +                        // #6
                            StringUtils.center("" + docID, 4) +                                    // #6
                            StringUtils.leftPad(                                                   // #6
                                    scoreFormatter.format(score), 12));                                 // #6
            out.println("   " + doc.get("category"));
            //out.println(searcher.explain(query, docID));   // #7
        }

        searcher.close();
    }
    public static Directory getBookIndexDirectory(String indexDir) throws IOException {
        // The build.xml ant script sets this property for us:
        return FSDirectory.open(new File(indexDir));
    }

/*
  The Sort object (#1) encapsulates an ordered collection of
  field sorting information. We ask IndexSearcher (#2) to
  compute scores per hit. Then we call the overloaded search
  method that accepts the custom Sort (#3). We use the
  useful toString method (#4) of the Sort class to describe
  itself, and then create PrintStream that accepts UTF-8
  encoded output (#5), and finally use StringUtils (#6) from
  Apache Commons Lang for nice columnar output
  formatting. Later youùll see a reason to look at the
  explanation of score . For now, itùs commented out (#7).
*/

    public static void main(String[] args) throws Exception {
        Query allBooks = new MatchAllDocsQuery();

        QueryParser parser = new QueryParser(Version.LUCENE_30,                 // #1
                "contents",                             // #1
                new StandardAnalyzer(                   // #1
                        Version.LUCENE_30));             // #1
        BooleanQuery query = new BooleanQuery();                                     // #1
        query.add(allBooks, BooleanClause.Occur.SHOULD);                             // #1
        query.add(parser.parse("java OR action"), BooleanClause.Occur.SHOULD);       // #1

        Directory directory = getBookIndexDirectory(FileUtils.getIndexDir());    //change it according to the index document structure.#2
        Sorting example = new Sorting(directory);                     // #2

        example.displayResults(query, Sort.RELEVANCE);

        example.displayResults(query, Sort.INDEXORDER);

        example.displayResults(query, new Sort(new SortField("category", SortField.STRING)));

        example.displayResults(query, new Sort(new SortField("pubmonth", SortField.INT, true)));

        example.displayResults(query,
                new Sort(new SortField("category", SortField.STRING),
                        SortField.FIELD_SCORE,
                        new SortField("pubmonth", SortField.INT, true)
                ));

        example.displayResults(query, new Sort(new SortField[] {SortField.FIELD_SCORE, new SortField("category", SortField.STRING)}));
        directory.close();
    }
}
