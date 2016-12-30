/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

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

public final class XMLUtility {

  /** Private empty constructor */
  private XMLUtility() {

  }

  public static String getStringFromFile(Path file, String xpath) {
    String ret = "";
    try {
      InputStream inputStream = Files.newInputStream(file);
      return getString(inputStream, xpath);
    } catch (IOException e) {
      // do nothing
    }
    return ret;
  }

  /**
   * 20160518 hsilva: the inputStream gets closed in the end
   */
  public static String getString(InputStream inputStream, String xpath) {
    String ret = "";
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      builder.setEntityResolver(new RodaEntityResolver());
      Document doc;
      doc = builder.parse(inputStream);
      XPathFactory xPathfactory = XPathFactory.newInstance();
      XPath xPath = xPathfactory.newXPath();
      XPathExpression expr = xPath.compile(xpath);
      ret = (String) expr.evaluate(doc, XPathConstants.STRING);
    } catch (SAXException | ParserConfigurationException | XPathExpressionException | IOException e) {
      // do nothing and return already defined OTHER
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
    return ret;
  }
}
