package com.ilucene;

import com.ilucene.util.AnalyzerUtils;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.*;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;

/**
 * Delve into the details of analyzer
 * @author JohnLiu
 */
public class AnalyzerService {

    private static final Logger logger = LoggerFactory.getLogger(Analyzer.class);

    /*----Builds the analyzer list instance with the instantiated Analyzer object.-----*/
    private static final Analyzer[] analyzers = new Analyzer[]{
        new WhitespaceAnalyzer(),
        new SimpleAnalyzer(),                      //Divides text at nonletter characters and lowercases.
        new KeywordAnalyzer(),                     //Treats entire text as a single token.
        new StandardAnalyzer(Version.LUCENE_30),
        new StopAnalyzer(Version.LUCENE_30)
     };

    /**
     * Analyzes the text with different analyzer type .
     * @param text
     */
    protected void analyze(String text){
        for(Analyzer analyzer: analyzers){
            String name = analyzer.getClass().getName();
            logger.info("analyzer name: " + name);
            AnalyzerUtils.displayToken(analyzer, text);
        }
    }

    protected void displayTokenWithFullDetails(Analyzer analyzer, String text){
        logger.debug("analyze the text :"+ text);
        TokenStream stream = analyzer.tokenStream("contents", new StringReader(text));
        TermAttribute term = stream.addAttribute(TermAttribute.class);
        PositionIncrementAttribute positionAttr = stream.addAttribute(
                                                   PositionIncrementAttribute.class) ;
        OffsetAttribute offsetAttr = stream.addAttribute(OffsetAttribute.class);
        TypeAttribute typeAttr = stream.addAttribute(TypeAttribute.class);
        FlagsAttribute flagsAttr = stream.addAttribute(FlagsAttribute.class);
        PayloadAttribute payloadAttr = stream.addAttribute(PayloadAttribute.class);
        int position = 0;
        try {
            while(stream.incrementToken()){
               int increment = positionAttr.getPositionIncrement();
               position += increment;
               logger.info("--"+ position + "\t"+
                            term.term() + "\t,offset: "+ offsetAttr.startOffset()+
                            " to "+ offsetAttr.endOffset()+
                            ",type:"+ typeAttr.type()+
                            "flagsAttr:" + flagsAttr.toString() +
                             ", payload :" + payloadAttr.toString()
               );
            }
        } catch (IOException e) {
            logger.error("stream.incrementToken() error.", e);
        }
    }
    /**
     *  Main entrance
     * @param args
     */
    public static void main(String[] args){
         String[] email = {
                "The quick brown fox jumped over the lazy dog",
                "XY&Z Corporation professorLiu@sina.com.cn"
         } ;
         AnalyzerService service = new AnalyzerService();
         for(String text: email){
             service.analyze(text);
         }
        /*Displays the full token attributes*/
        service.displayTokenWithFullDetails(new SimpleAnalyzer(),email[0]);
    }
}
