/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.characterization;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.ToXMLContentHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class TikaFullTextPluginUtils {
  public static Path extractMetadata(InputStream is) throws IOException, SAXException, TikaException {
    Parser parser = new AutoDetectParser();
    Metadata metadata = new Metadata();
    ContentHandler handler = new ToXMLContentHandler();
    // FIXME does "is" gets closed???
    parser.parse(is, handler, metadata, new ParseContext());
    String content = handler.toString();
    Path p = Files.createTempFile("tika", ".xml");
    Files.write(p, content.getBytes());
    return p;
  }

  public static String extractFullTextFromResult(Path tikaResult)
    throws ParserConfigurationException, IOException, SAXException {
    return extractFullTextFromResult(Files.newInputStream(tikaResult));
  }

  public static String extractFullTextFromResult(InputStream tikaResultStream)
    throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();

    Document doc = db.parse(tikaResultStream);
    NodeList nodes = doc.getElementsByTagName("body");
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      sb.append(node.getTextContent());
    }
    return sb.toString();
  }
}
