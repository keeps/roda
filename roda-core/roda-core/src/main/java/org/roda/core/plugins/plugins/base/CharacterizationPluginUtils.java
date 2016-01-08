/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerException;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.handler.loader.XMLLoader;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.RodaUtils;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.storage.Binary;

public class CharacterizationPluginUtils {

  // TODO improve XSL Result to Map (currently using solr utils class)
  public static Map<String, String> getObjectCharacteristicsFields(String aipID, String representationID, String fileID,
    Binary binary, Path configBasePath) throws GenericException {
    InputStream inputStream;
    SolrInputDocument doc = null;
    try {
      inputStream = binary.getContent().createInputStream();

      Reader premisReader = new InputStreamReader(inputStream);

      // FIXME delete the following comment
      /*
       * InputStream transformerStream =
       * RodaUtils.getResourceInputStream(configBasePath,
       * "crosswalks/ingest/other/characterization.xslt", "Ingesting");
       */
      InputStream transformerStream = RodaCoreFactory
        .getConfigurationFileAsStream("crosswalks/ingest/other/premis.xslt");

      // TODO support the use of scripts for non-xml transformers
      Reader xsltReader = new InputStreamReader(transformerStream);
      CharArrayWriter transformerResult = new CharArrayWriter();
      Map<String, Object> stylesheetOpt = new HashMap<String, Object>();
      RodaUtils.applyStylesheet(xsltReader, premisReader, stylesheetOpt, transformerResult);
      premisReader.close();

      XMLLoader loader = new XMLLoader();
      CharArrayReader transformationResult = new CharArrayReader(transformerResult.toCharArray());
      XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(transformationResult);

      boolean parsing = true;

      while (parsing) {
        int event = parser.next();

        if (event == XMLStreamConstants.END_DOCUMENT) {
          parser.close();
          parsing = false;
        } else if (event == XMLStreamConstants.START_ELEMENT) {
          String currTag = parser.getLocalName();
          if ("doc".equals(currTag)) {
            doc = loader.readDoc(parser);
          }
        }

      }
      transformationResult.close();

    } catch (IOException | TransformerException | XMLStreamException | FactoryConfigurationError e) {
      throw new GenericException("Could not process preservation metadata binary " + binary.getStoragePath()
        + " using xslt characterization.xslt", e);
    }
    Map<String, String> characteristics = new HashMap<String, String>();
    for (String s : doc.getFieldNames()) {
      characteristics.put(s, doc.getFieldValue(s).toString());
    }
    return characteristics;
  }
}
