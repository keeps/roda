package org.roda.action.ingest.fulltext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.ToXMLContentHandler;
import org.roda.action.orchestrate.Plugin;
import org.roda.action.orchestrate.PluginException;
import org.roda.core.common.InvalidParameterException;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.core.data.v2.Representation;
import org.roda.index.IndexService;
import org.roda.model.AIP;
import org.roda.model.File;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.storage.Binary;
import org.roda.storage.StorageService;
import org.roda.storage.StorageServiceException;
import org.roda.storage.fs.FSUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class FullTextAction implements Plugin<AIP> {
  private final Logger logger = Logger.getLogger(getClass());
  private Parser parser;

  @Override
  public void init() throws PluginException {
    parser = new AutoDetectParser();
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Full-text extraction action";
  }

  @Override
  public String getDescription() {
    return "Extracts the full-text from the representation files";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    return new ArrayList<>();
  }

  @Override
  public Map<String, String> getParameterValues() {
    return new HashMap<>();
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    // no params
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {
    try {
      for (AIP aip : list) {
        logger.debug("Deep characterization for AIP " + aip.getId());
        try {
          for (String representationID : aip.getRepresentationIds()) {
            Representation representation = model.retrieveRepresentation(aip.getId(), representationID);
            for (String fileID : representation.getFileIds()) {
              File file = model.retrieveFile(aip.getId(), representationID, fileID);
              Binary binary = storage.getBinary(file.getStoragePath());

              Metadata metadata = new Metadata();
              ContentHandler handler = new ToXMLContentHandler();
              parser.parse(binary.getContent().createInputStream(), handler, metadata, new ParseContext());
              String content = handler.toString();
              Path p = Files.createTempFile("tika", ".xml");
              Files.write(p, content.getBytes());
              Binary resource = (Binary) FSUtils.convertPathToResource(p.getParent(), p);
              model.createOtherMetadata(aip.getId(), file.getStoragePath().getName() + ".xml", "tika", resource);
            }
          }
        } catch (ModelServiceException mse) {
          logger.error("Error processing AIP " + aip.getId() + ": " + mse.getMessage(), mse);
        } catch (StorageServiceException sse) {
          logger.error("Error processing AIP " + aip.getId() + ": " + sse.getMessage(), sse);
        } catch (SAXException se) {
          logger.error("Error processing AIP " + aip.getId() + ": " + se.getMessage(), se);
        } catch (TikaException te) {
          logger.error("Error processing AIP " + aip.getId() + ": " + te.getMessage(), te);
        }
      }
    } catch (IOException ioe) {
      logger.error("Error executing FastCharacterizationAction: " + ioe.getMessage(), ioe);
    }
    return null;
  }

  @Override
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

    return null;
  }

  @Override
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

    return null;
  }

}
