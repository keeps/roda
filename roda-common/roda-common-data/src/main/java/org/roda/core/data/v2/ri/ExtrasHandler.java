/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ri;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ExtrasHandler extends javax.xml.bind.annotation.adapters.XmlAdapter<Object, String> {
  private final DocumentBuilderFactory docBuilderFactory;
  private final TransformerFactory transformerFactory;

  public ExtrasHandler() {
    docBuilderFactory = DocumentBuilderFactory.newInstance();
    transformerFactory = TransformerFactory.newInstance();
  }

  @Override
  public String unmarshal(Object v) throws Exception {
    StringWriter sw = new StringWriter();
    Transformer t = transformerFactory.newTransformer();
    t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    t.setOutputProperty(OutputKeys.INDENT, "no");
    t.transform(new DOMSource((Node) v), new StreamResult(sw));
    return sw.toString();
  }

  @Override
  public Object marshal(String v) throws Exception {
    // DOM document builder
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    // Creating a new empty document
    Document doc = docBuilder.newDocument();
    // Creating a DOMResult as output for the transformer
    DOMResult result = new DOMResult(doc);
    // Default transformer: identity tranformer (doesn't alter input)
    Transformer transformer = transformerFactory.newTransformer();
    // String reader from the input and source
    StringReader stringReader = new StringReader(v);
    StreamSource source = new StreamSource(stringReader);
    // Transforming input string to the DOM
    transformer.transform(source, result);
    // Return DOM root element for JAXB marshalling to XML
    return doc.getDocumentElement();
  }

}
