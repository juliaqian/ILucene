package com.ilucene.util;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;

/**
 * @author JohnLiu
 */
public class AnalyzerUtils {

    private static final Logger logger = LoggerFactory.getLogger(AnalyzerUtils.class);
    /**
     * Displays the tokens attributes with log info.
     * @param analyzer
     * @param text
     */
    public static void displayToken(Analyzer analyzer, String text){
        displayTokens(analyzer.tokenStream("contents", new StringReader(text)));
    }

    /**
     * Displays the tokens attributes with log info.
     * @param stream
     *         TokenStream type stream is used to iterate the token attribute
     */
    public static void displayTokens(TokenStream stream){
        TermAttribute term = stream.addAttribute(TermAttribute.class);
        try {
            while(stream.incrementToken()){
                logger.info(" [" + term.term() + "] ");
            }
        } catch (IOException e) {
            logger.error("incrementToken error occurs", e);
        }
    }


}
