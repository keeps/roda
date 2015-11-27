/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.core.data.common.InvalidParameterException;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexServiceException;
import org.roda.core.model.AIP;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexPlugin implements Plugin<AIP> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReindexPlugin.class);
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
      LOGGER.debug("Reindexing AIP " + aip.getId());
      index.reindexAIP(aip);
    }

    return null;
  }

  @Override
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    if (clearIndexes) {
      LOGGER.debug("Clearing indexes");
      try {
        index.clearIndex(RodaConstants.INDEX_AIP);
        index.clearIndex(RodaConstants.INDEX_SDO);
        index.clearIndex(RodaConstants.INDEX_REPRESENTATIONS);
        index.clearIndex(RodaConstants.INDEX_PRESERVATION_OBJECTS);
        index.clearIndex(RodaConstants.INDEX_PRESERVATION_EVENTS);
        index.clearIndex(RodaConstants.INDEX_FILE);
      } catch (IndexServiceException e) {
        throw new PluginException("Error clearing index", e);
      }
    } else {
      LOGGER.debug("Skipping clear indexes");
    }

    return null;
  }

  @Override
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    LOGGER.debug("Optimizing indexes");
    try {
      index.optimizeIndex(RodaConstants.INDEX_AIP);
      index.optimizeIndex(RodaConstants.INDEX_SDO);
    } catch (IndexServiceException e) {
      throw new PluginException("Error optimizing index", e);
    }

    return null;
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new ReindexPlugin();
  }

}
