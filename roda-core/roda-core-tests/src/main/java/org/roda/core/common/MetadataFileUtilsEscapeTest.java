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

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StringContentPayload;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_TRAVIS})
public class MetadataFileUtilsEscapeTest {

  @Test
  public void testGetMetadataPayload() throws GenericException, IOException {
    TransferredResource resource = new TransferredResource();
    resource.setName("a&b.csv");

    ContentPayload metadataPayload = MetadataFileUtils.getMetadataPayload(resource);
    Assert.assertTrue(metadataPayload instanceof StringContentPayload);
    Assert.assertTrue(
      IOUtils.toString(metadataPayload.createInputStream()).contains("<field name=\"title\">a&amp;b.csv</field>"));
  }

  @Test
  public void testEscape() throws GenericException {
    int total = (int) Math.pow(2, 20);
    StringBuilder temp = new StringBuilder();
    Map<String, String> values = new HashMap<>();
    for (int i = 0; i < total; i++) {
      char c = (char) i;
      temp.append(c);
      if (i % 100 == 0) {
        values.put(temp.toString(), temp.toString());
        temp = new StringBuilder();
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
    return new ByteArrayInputStream(outputStream.toByteArray());
  }
}
