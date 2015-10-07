package org.roda.action.orchestrate.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.roda.action.orchestrate.Plugin;
import org.roda.action.orchestrate.PluginException;
import org.roda.core.common.InvalidParameterException;
import org.roda.core.common.RodaConstants;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.index.IndexService;
import org.roda.index.IndexServiceException;
import org.roda.model.AIP;
import org.roda.model.ModelService;
import org.roda.storage.StorageService;

public class ReindexAction implements Plugin<AIP> {

  private final Logger logger = Logger.getLogger(getClass());
  private boolean clearIndexes = true;

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
    return "Reindex";
  }

  @Override
  public String getDescription() {
    return "Clean-up index and re-create it from original data";
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

  public boolean isClearIndexes() {
    return clearIndexes;
  }

  public void setClearIndexes(boolean clearIndexes) {
    this.clearIndexes = clearIndexes;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {

    for (AIP aip : list) {
      logger.debug("Reindexing AIP " + aip.getId());
      index.reindexAIP(aip);
    }

    return null;
  }

  @Override
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    if (clearIndexes) {
      logger.debug("Clearing indexes");
      try {
        index.clearIndex(RodaConstants.INDEX_AIP);
        index.clearIndex(RodaConstants.INDEX_SDO);
      } catch (IndexServiceException e) {
        throw new PluginException("Error clearing index", e);
      }
    } else {
      logger.debug("Skipping clear indexes");
    }

    return null;
  }

  @Override
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    logger.debug("Optimizing indexes");
    try {
      index.optimizeIndex(RodaConstants.INDEX_AIP);
      index.optimizeIndex(RodaConstants.INDEX_SDO);
    } catch (IndexServiceException e) {
      throw new PluginException("Error optimizing index", e);
    }

    return null;
  }

}
