package com.ilucene.analysis;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author John Liu
 */
public class MultiAnalyzer {
    private static Logger logger = LoggerFactory.getLogger(MultiAnalyzer.class);
    static Analyzer[] analyzers = new Analyzer[] {
            new WhitespaceAnalyzer(),
            new SimpleAnalyzer(),
            new KeywordAnalyzer(),
            new StopAnalyzer(Version.LUCENE_30),
            new StandardAnalyzer(Version.LUCENE_30)     /*Lucene's  most sophisticated core analyzer */
    };

    public static void analyze(String text) throws IOException {
        for (Analyzer analyzer : analyzers) {
            String name = analyzer.getClass().getSimpleName();
            logger.info("  " + name + ":");
            AnalyzerUtils.displayTokens(analyzer, text);     // under the same package
            logger.info("\n");
        }
    }

    public static void main(String []args) throws IOException {
        String[] examples = {
                "The quick brown fox jumped over the lazy dog",
                "XY&Z Corporation - xyz@example.com"
        };
        String info = "jEdit version 5.0.0 was recently released." +
                       " It has new editing modes for Scala and Dart, " +
                      "and improved Mac OS X support regarding the UI " +
                      "      and keymappings."   ;

        for(String text : examples){
            MultiAnalyzer.analyze(text);
        }

        //looking inside token
        AnalyzerUtils.displayTokensWithFullDetails(new SimpleAnalyzer(),
                                                   info);
    }

}
