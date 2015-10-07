package org.roda.action.ingest.bagit;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.roda.action.ingest.bagit.utils.BagitNotValidException;
import org.roda.action.ingest.bagit.utils.BagitUtils;
import org.roda.action.orchestrate.Plugin;
import org.roda.action.orchestrate.PluginException;
import org.roda.core.common.InvalidParameterException;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.index.IndexService;
import org.roda.model.AIP;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.storage.StorageService;
import org.roda.storage.StorageServiceException;

public class BagitToAIPAction implements Plugin<String> {
  private final Logger logger = Logger.getLogger(getClass());

  @Override
  public void init() throws PluginException {
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Bagit to AIP";
  }

  @Override
  public String getDescription() {
    return "Converts a Bagit zip file to an AIP";
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
  public Report execute(IndexService index, ModelService model, StorageService storage, List<String> list)
    throws PluginException {
    for (String bagitStringPath : list) {
      Path bagitPath = Paths.get(bagitStringPath);
      logger.debug("Converting " + bagitPath + " to AIP");
      try {
        AIP aip = BagitUtils.bagitToAip(bagitPath, model);
      } catch (ModelServiceException | StorageServiceException | IOException e) {
        logger.error("Error converting " + bagitPath + " to AIP: " + e.getMessage(), e);
      } catch (BagitNotValidException e) {
        logger.error("Bagit file " + bagitPath + " is not valid...");
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
