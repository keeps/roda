/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.roda.core.data.exceptions.GenericException;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

@Test(groups = {"all", "travis"})
public class MetadataFileUtilsEscapeTest {

  @Test
  public void testEscape() throws GenericException {
    int total = (int) Math.pow(2, 20);
    String temp = "";
    Map<String, String> values = new HashMap<String, String>();
    for (int i = 0; i < total; i++) {
      char c = (char) i;
      temp += c;
      if (i % 100 == 0) {
        values.put(temp, temp);
        temp = "";
      }
    }

    // with escape, the SaxReader can't throw an exception...
    try {
      Document document = DocumentHelper.createDocument();
      Element root = document.addElement("root");
      for (Map.Entry<String, String> entry : values.entrySet()) {
        root.addElement("item").addAttribute("name", MetadataFileUtils.escapeAttribute(entry.getKey()))
          .addText(MetadataFileUtils.escapeContent(entry.getValue()));
      }
      SAXReader reader = new SAXReader();
      reader.setValidation(false);
      reader.read(documentToPrettyInputStream(document));
    } catch (Exception e) {
      AssertJUnit.fail();
    }

    // without escape, the SaxReader must throw an exception...
    try {
      Document document = DocumentHelper.createDocument();
      Element root = document.addElement("root");
      for (Map.Entry<String, String> entry : values.entrySet()) {
        root.addElement("item").addAttribute("name", entry.getKey()).addText(entry.getValue());
      }
      SAXReader reader = new SAXReader();
      reader.setValidation(false);
      reader.read(documentToPrettyInputStream(document));
      AssertJUnit.fail();
    } catch (Exception e) {
      // do nothing
    }
  }

  public static InputStream documentToPrettyInputStream(Document document) throws IOException {

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    XMLWriter xmlWriter = new XMLWriter(outputStream, OutputFormat.createPrettyPrint());
    xmlWriter.write(document);
    xmlWriter.close();
    InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
    return inputStream;
  }
}
