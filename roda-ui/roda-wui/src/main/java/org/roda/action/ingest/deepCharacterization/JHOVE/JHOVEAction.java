package org.roda.action.ingest.deepCharacterization.JHOVE;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.roda.action.ingest.deepCharacterization.JHOVE.utils.JHOVEUtils;
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

public class JHOVEAction implements Plugin<AIP> {
  private static final Logger LOGGER = Logger.getLogger(JHOVEAction.class);

  @Override
  public void init() throws PluginException {
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Deep characterization action";
  }

  @Override
  public String getDescription() {
    return "Update the premis files with the object characterization";
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
    for (AIP aip : list) {
      LOGGER.debug("Processing AIP " + aip.getId());
      try {
        for (String representationID : aip.getRepresentationIds()) {
          LOGGER.debug("Processing representation " + representationID + " from AIP " + aip.getId());
          Representation representation = model.retrieveRepresentation(aip.getId(), representationID);
          for (String fileID : representation.getFileIds()) {
            LOGGER.debug("Processing file " + fileID + " from " + representationID + " of AIP " + aip.getId());
            File file = model.retrieveFile(aip.getId(), representationID, fileID);
            Binary binary = storage.getBinary(file.getStoragePath());

            Path jhoveResults = JHOVEUtils.runJhove(file, binary, getParameterValues());
            Binary resource = (Binary) FSUtils.convertPathToResource(jhoveResults.getParent(), jhoveResults);
            model.createOtherMetadata(aip.getId(), representationID, file.getStoragePath().getName() + ".xml", "JHOVE",
              resource);
            jhoveResults.toFile().delete();
          }
        }
      } catch (ModelServiceException mse) {
        LOGGER.error("Error processing AIP " + aip.getId() + ": " + mse.getMessage());
      } catch (StorageServiceException sse) {
        LOGGER.error("Error processing AIP " + aip.getId() + ": " + sse.getMessage());
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
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
