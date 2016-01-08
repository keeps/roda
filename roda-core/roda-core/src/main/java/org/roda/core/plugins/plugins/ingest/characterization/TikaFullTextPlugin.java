/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.characterization;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tika.exception.TikaException;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.core.data.common.InvalidParameterException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.PluginType;
import org.roda.core.data.v2.Representation;
import org.roda.core.index.IndexService;
import org.roda.core.model.AIP;
import org.roda.core.model.File;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class TikaFullTextPlugin implements Plugin<AIP> {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public void init() throws PluginException {
    // do nothing
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
        logger.debug("Processing AIP " + aip.getId());
        try {
          for (String representationID : aip.getRepresentationIds()) {
            logger.debug("Processing representation " + representationID + " of AIP " + aip.getId());
            Representation representation = model.retrieveRepresentation(aip.getId(), representationID);
            for (String fileID : representation.getFileIds()) {
              logger.debug(
                "Processing file " + fileID + " of representation " + representationID + " from AIP " + aip.getId());
              File file = model.retrieveFile(aip.getId(), representationID, fileID);
              Binary binary = storage.getBinary(file.getStoragePath());

              // FIXME file that doesn't get deleted afterwards
              Path tikaResult = TikaFullTextPluginUtils.extractMetadata(binary.getContent().createInputStream());

              Binary resource = (Binary) FSUtils.convertPathToResource(tikaResult.getParent(), tikaResult);
              model.createOtherMetadata(aip.getId(), representationID, file.getStoragePath().getName() + ".xml", "tika",
                resource);
            }
          }
        } catch (RODAException | SAXException | TikaException mse) {
          logger.error("Error processing AIP " + aip.getId() + ": " + mse.getMessage(), mse);
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

  @Override
  public Plugin<AIP> cloneMe() {
    return new TikaFullTextPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

}
