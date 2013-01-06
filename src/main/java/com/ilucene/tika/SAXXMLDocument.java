package com.ilucene.tika;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SAXXMLDocument extends DefaultHandler {

    private static Logger logger = LoggerFactory.getLogger(SAXXMLDocument.class);
    private StringBuilder elementBuffer = new StringBuilder();
    private Map<String,String> attributeMap = new HashMap<String,String>();

    private Document doc;

    public Document getDocument(InputStream is)  // #1
            throws DocumentHandlerException {

        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser parser = spf.newSAXParser();
            parser.parse(is, this);
        } catch (Exception e) {
            throw new DocumentHandlerException(
                    "Cannot parse XML document", e);
        }

        return doc;
    }

    public void startDocument() {             // #create the new
        doc = new Document();
    }

    public void startElement(String uri, String localName,  // #3
                             String qName, Attributes atts)                        // #3
            throws SAXException {                                 // #3

        elementBuffer.setLength(0);
        attributeMap.clear();
        int numAtts = atts.getLength();
        if (numAtts > 0) {
            for (int i = 0; i < numAtts; i++) {
                attributeMap.put(atts.getQName(i), atts.getValue(i));
                logger.info("qName: "+ atts.getQName(i)+", name:" + atts.getValue(i));
            }
        }
    }

    /**
     * In it we append to our elementBuffer the element contents
     * passed into the method.
     * @param text
     * @param start
     * @param length
     */
    public void characters(char[] text, int start, int length) {  // #4
        elementBuffer.append(text, start, length);  //
    }

    public void endElement(String uri, String localName, String qName)  // #5
            throws SAXException {
        if (qName.equals("address-book")) {
            return;
        }else if (qName.equals("contact")) {
            for (Entry<String,String> attribute : attributeMap.entrySet()) {
                String attName = attribute.getKey();
                String attValue = attribute.getValue();
                logger.info("attName: "+ attName+ ", attValue:"+attValue);
                doc.add(new Field(attName, attValue, Field.Store.YES, Field.Index.NOT_ANALYZED));
            }
        }else {
            doc.add(new Field(qName, elementBuffer.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
            logger.info("attName: "+ qName + ", attValue:"+ elementBuffer.toString());
        }
    }

    public static void main(String args[]) throws Exception {
        SAXXMLDocument handler = new SAXXMLDocument();
        InputStream is = SAXXMLDocument.class.getResourceAsStream("addressbook.xml");
        if(is != null){
            Document doc = handler.getDocument(is);
            logger.info("\n" + doc);
        }else{
            logger.warn("No input stream ---------------------------------------------");
        }
    }
}

/*
#1 Start parser
#2 Called when parsing begins
#3 Beginning of new XML element
#4 Append element contents to elementBuffer
#5 Called when closing XML elements are processed
*/

