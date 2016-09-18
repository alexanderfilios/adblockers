package com.adblockers.services.geocode;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alexandrosfilios on 18/09/16.
 */
@Component
public class LocationParserXml implements LocationParser {

    private XPathFactory xPathFactory;
    private XPath xPath;
    private DocumentBuilderFactory documentBuilderFactory;
    private DocumentBuilder documentBuilder;
    private static final String OUTPUT_FORMAT = "xml";

    public LocationParserXml() {
        xPathFactory = XPathFactory.newInstance();
        xPath = xPathFactory.newXPath();
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public String getOutputFormat() {
        return OUTPUT_FORMAT;
    }

    public Map<String, String> parseParams(InputSource inputSource, Map<String, String> params) {

        try {
            Map<String, String> result = new HashMap<>();
            Document document = documentBuilder.parse(inputSource);
            for (Map.Entry<String, String> param : params.entrySet()) {
                try {
                    result.put(param.getKey(), xPath.evaluate(param.getValue(), document));
                } catch (XPathExpressionException e) {
                    result.put(param.getKey(), null);
                }
            }
            return result;
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
