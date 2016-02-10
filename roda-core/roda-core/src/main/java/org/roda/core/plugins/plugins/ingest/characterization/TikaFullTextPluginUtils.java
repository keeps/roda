/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.characterization;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.ToXMLContentHandler;
import org.roda.core.common.PremisUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ClosableIterable;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StringContentPayload;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class TikaFullTextPluginUtils {
  public static String extractMetadata(InputStream is) throws IOException, SAXException, TikaException {
    Parser parser = new AutoDetectParser();
    Metadata metadata = new Metadata();
    ContentHandler handler = new ToXMLContentHandler();
    // FIXME does "is" gets closed???
    parser.parse(is, handler, metadata, new ParseContext());
    return handler.toString();
  }

  public static Map<String, String> extractPropertiesFromResult(String tikaResult)
    throws ParserConfigurationException, IOException, SAXException {
    return extractPropertiesFromResult(new ByteArrayInputStream(tikaResult.getBytes()));
  }

  public static Map<String, String> extractPropertiesFromResult(InputStream tikaResultStream)
    throws ParserConfigurationException, IOException, SAXException {
    Map<String, String> properties = new HashMap<String, String>();
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();

    Document doc = db.parse(tikaResultStream);
    NodeList nodes = doc.getElementsByTagName("body");
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      sb.append(node.getTextContent());
    }
    String fulltext = sb.toString();
    if (!StringUtils.isBlank(fulltext)) {
      properties.put(RodaConstants.FILE_FULLTEXT, fulltext);
    }

    NodeList metaNodes = doc.getElementsByTagName("meta");
    for (int i = 0; i < metaNodes.getLength(); i++) {
      Node node = metaNodes.item(i);
      Element e = (Element) node;
      if (e.getAttribute("name") != null && e.getAttribute("name").equalsIgnoreCase("Application-Name")) {
        properties.put(RodaConstants.FILE_CREATING_APPLICATION_NAME, e.getAttribute("content"));
      }
      if (e.getAttribute("name") != null && e.getAttribute("name").equalsIgnoreCase("Application-Version")) {
        properties.put(RodaConstants.FILE_CREATING_APPLICATION_VERSION, e.getAttribute("content"));
      }

      if (e.getAttribute("name") != null && e.getAttribute("name").equalsIgnoreCase("Creation-Date")) {
        properties.put(RodaConstants.FILE_DATE_CREATED_BY_APPLICATION, e.getAttribute("content"));
      }
    }

    return properties;
  }

  public static void runTikaFullTextOnRepresentation(IndexService index, ModelService model, StorageService storage,
    AIP aip, Representation representation) throws NotFoundException, GenericException, RequestNotValidException,
      AuthorizationDeniedException, IOException, SAXException, TikaException, ValidationException {

    ClosableIterable<File> allFiles = model.listAllFiles(aip.getId(), representation.getId());
    List<IndexedFile> updatedFiles = new ArrayList<IndexedFile>();
    for (File file : allFiles) {

      if (!file.isDirectory()) {
        StoragePath storagePath = ModelUtils.getFileStoragePath(file);
        Binary binary = storage.getBinary(storagePath);

        String tikaResult = TikaFullTextPluginUtils.extractMetadata(binary.getContent().createInputStream());
        ContentPayload payload = new StringContentPayload(tikaResult);
        model.createOtherMetadata(aip.getId(), representation.getId(), file.getPath(), file.getId(),
          TikaFullTextPlugin.FILE_SUFFIX, TikaFullTextPlugin.OTHER_METADATA_TYPE, payload);

        // update PREMIS
        try {
          Map<String, String> properties = TikaFullTextPluginUtils.extractPropertiesFromResult(tikaResult);
          String fulltext = properties.get(RodaConstants.FILE_FULLTEXT);
          String creatingApplicationName = properties.get(RodaConstants.FILE_CREATING_APPLICATION_NAME);
          String creatingApplicationVersion = properties.get(RodaConstants.FILE_CREATING_APPLICATION_VERSION);
          String dateCreatedByApplication = properties.get(RodaConstants.FILE_DATE_CREATED_BY_APPLICATION);

          Binary premis_bin = model.retrievePreservationFile(file);

          lc.xmlns.premisV2.File premis_file = PremisUtils.binaryToFile(premis_bin.getContent(), false);
          PremisUtils.updateCreatingApplication(premis_file, creatingApplicationName, creatingApplicationVersion,
            dateCreatedByApplication);

          PreservationMetadataType type = PreservationMetadataType.OBJECT_FILE;
          String id = ModelUtils.generatePreservationMetadataId(type, aip.getId(), representation.getId(),
            file.getPath(), file.getId());

          ContentPayload premis_file_payload = PremisUtils.fileToBinary(premis_file);
          model.updatePreservationMetadata(id, type, aip.getId(), representation.getId(), file.getPath(), file.getId(),
            premis_file_payload);

        } catch (ParserConfigurationException pce) {

        }
      }
    }
    IOUtils.closeQuietly(allFiles);
  }
}
