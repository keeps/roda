package org.roda.action.ingest.fulltext.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.ToXMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class TikaUtils {
  public static Path extractMetadata(InputStream is) throws IOException, SAXException, TikaException{
    Parser parser = new AutoDetectParser();
    Metadata metadata = new Metadata();
    ContentHandler handler = new ToXMLContentHandler();
    parser.parse(is, handler, metadata, new ParseContext());
    String content = handler.toString();
    Path p = Files.createTempFile("tika", ".xml");
    Files.write(p, content.getBytes());
    return p;
  }
}
