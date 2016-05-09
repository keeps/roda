package org.roda.core.common;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XMLUtility {

  public static String getStringFromFile(InputStream inputStream, String xPath) {
    String ret = "";
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc;
      doc = builder.parse(inputStream);
      XPathFactory xPathfactory = XPathFactory.newInstance();
      XPath xpath = xPathfactory.newXPath();
      XPathExpression expr = xpath.compile(xPath);
      ret = (String) expr.evaluate(doc, XPathConstants.STRING);
    } catch (SAXException | IOException | ParserConfigurationException | XPathExpressionException e) {
      // do nothing and return already defined OTHER
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
    return ret;
  }
}
