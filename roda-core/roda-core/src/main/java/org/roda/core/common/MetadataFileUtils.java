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

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.tika.metadata.Metadata;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StringContentPayload;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import gov.loc.repository.bagit.BagInfoTxt;

public class MetadataFileUtils {

  public static String generateMetadataFile(Metadata metadata) throws IOException {
    StringBuilder b = new StringBuilder();
    b.append("<metadata>");
    String[] names = metadata.names();
    for (String name : names) {
      String[] values = metadata.getValues(name);
      if (values != null && values.length > 0) {
        for (String value : values) {
          b.append("<field name='").append(name).append("'>").append(StringEscapeUtils.escapeXml11(value))
            .append("</field>");
        }
      }

    }
    b.append("</metadata>");
    return b.toString();
  }

  public static String generateMetadataFile(BagInfoTxt bagInfoTxt) throws IOException {
    StringBuilder b = new StringBuilder();
    b.append("<metadata>");
    for (Map.Entry<String, String> entry : bagInfoTxt.entrySet()) {
      if (!entry.getKey().equalsIgnoreCase("parent")) {
        b.append("<field name='").append(entry.getKey()).append("'>")
          .append(StringEscapeUtils.escapeXml11(entry.getValue())).append("</field>");
      }
    }
    b.append("</metadata>");
    return b.toString();
  }

  public static ContentPayload getMetadataPayload(TransferredResource transferredResource) {
    StringBuilder b = new StringBuilder();
    b.append("<metadata>").append("<field name='title'>")
      .append(StringEscapeUtils.escapeXml11(transferredResource.getName())).append("</field>").append("</metadata>");

    return new StringContentPayload(b.toString());
  }

  public static Map<String, List<String>> parseBinary(Binary binary)
    throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
    Map<String, List<String>> otherProperties = new HashMap<String, List<String>>();
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
      List<String> values = new ArrayList<String>();
      if (otherProperties.containsKey(name)) {
        values = otherProperties.get(name);
      }
      values.add(value);
      otherProperties.put(name, values);
    }
    return otherProperties;
  }

}
