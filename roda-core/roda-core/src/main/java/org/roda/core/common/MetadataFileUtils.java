/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.jdom2.Element;
import org.jdom2.IllegalDataException;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StringContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.xml.XmlEscapers;

public class MetadataFileUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(MetadataFileUtils.class);

  /** Private empty constructor */
  private MetadataFileUtils() {
    // do nothing
  }

  public static String generateMetadataFile(Map<String, String> bagInfo) throws GenericException {
    try {
      Element root = new Element("metadata");
      org.jdom2.Document doc = new org.jdom2.Document();

      for (Map.Entry<String, String> entry : bagInfo.entrySet()) {
        if (!"parent".equalsIgnoreCase(entry.getKey())) {
          Element child = new Element("field");
          child.setAttribute("name", escapeAttribute(entry.getKey()));
          child.addContent(escapeContent(entry.getValue()));
          root.addContent(child);
        }
      }
      doc.setRootElement(root);
      XMLOutputter outter = new XMLOutputter();
      outter.setFormat(Format.getPrettyFormat());
      outter.outputString(doc);
      return outter.outputString(doc);
    } catch (IllegalDataException e) {
      throw new GenericException(e);
    }
  }

  public static ContentPayload getMetadataPayload(TransferredResource transferredResource) {
    try {
      Element root = new Element("metadata");
      org.jdom2.Document doc = new org.jdom2.Document();
      Element child = new Element("field");
      child.setAttribute("name", "title");
      child.addContent(escapeContent(transferredResource.getName()));
      root.addContent(child);
      doc.setRootElement(root);
      XMLOutputter outter = new XMLOutputter();
      outter.setFormat(Format.getPrettyFormat());
      outter.outputString(doc);
      return new StringContentPayload(outter.outputString(doc));
    } catch (IllegalDataException e) {
      LOGGER.debug("Error generating TransferredResource metadata file {}", e.getMessage());
      return new StringContentPayload("");
    }
  }

  public static Map<String, List<String>> parseBinary(Binary binary)
    throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
    Map<String, List<String>> otherProperties = new HashMap<>();
    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
    domFactory.setNamespaceAware(true);
    DocumentBuilder builder = domFactory.newDocumentBuilder();
    Document doc = builder.parse(binary.getContent().createInputStream());
    XPath xpath = XPathFactory.newInstance().newXPath();
    XPathExpression expr = xpath.compile("//field");
    Object result = expr.evaluate(doc, XPathConstants.NODESET);
    NodeList nodes = (NodeList) result;
    for (int i = 0; i < nodes.getLength(); i++) {
      String name = nodes.item(i).getAttributes().getNamedItem("name").getNodeValue() + "_txt";
      String value = nodes.item(i).getTextContent();
      List<String> values = new ArrayList<>();
      if (otherProperties.containsKey(name)) {
        values = otherProperties.get(name);
      }
      values.add(value);
      otherProperties.put(name, values);
    }
    return otherProperties;
  }

  public static String escapeAttribute(String value) {
    return XmlEscapers.xmlAttributeEscaper().escape(value);
  }

  public static String escapeContent(String value) {
    return XmlEscapers.xmlContentEscaper().escape(value);
  }

}
