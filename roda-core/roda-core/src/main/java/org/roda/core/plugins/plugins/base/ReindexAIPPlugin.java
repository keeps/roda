/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexAIPPlugin extends AbstractPlugin<AIP> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReindexAIPPlugin.class);
  private boolean clearIndexes = false;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES, "Clear indexes", PluginParameterType.BOOLEAN,
        "true", false, false, "Clear all the AIP indexes before reindexing them."));
  }

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
    return "Reindex AIP";
  }

  @Override
  public String getDescription() {
    return "Cleanup index and recreate it from data in storage";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<PluginParameter>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES)) {
      clearIndexes = Boolean.parseBoolean(getParameterValues().get(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES));
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {
    LOGGER.debug("Reindexing a total of {} AIPs", list.size());

    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, list.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      try {
        for (AIP aip : list) {
          LOGGER.debug("Reindexing AIP {}", aip.getId());
          index.reindexAIP(aip);
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
        }
      } catch (ClassCastException e) {
        LOGGER.error("Trying to execute an AIP-only plugin with other objects");
        jobPluginInfo.incrementObjectsProcessedWithFailure(list.size());
      }

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (JobException e) {
      LOGGER.error("Could not update Job information");
    }

    return PluginHelper.initPluginReport(this);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {

    if (clearIndexes) {
      LOGGER.debug("Clearing indexes");
      try {
        index.clearIndex(RodaConstants.INDEX_AIP);
        index.clearIndex(RodaConstants.INDEX_REPRESENTATION);
        index.clearIndex(RodaConstants.INDEX_PRESERVATION_EVENTS);
        index.clearIndex(RodaConstants.INDEX_FILE);
        index.clearIndex(RodaConstants.INDEX_PRESERVATION_AGENTS);
      } catch (GenericException e) {
        throw new PluginException("Error clearing index", e);
      }
    } else {
      LOGGER.debug("Skipping clear indexes");
    }

    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    LOGGER.debug("Optimizing indexes");

    index.reindexPreservationAgents();

    try {
      index.optimizeIndex(RodaConstants.INDEX_AIP);
      index.optimizeIndex(RodaConstants.INDEX_REPRESENTATION);
      index.optimizeIndex(RodaConstants.INDEX_PRESERVATION_EVENTS);
      index.optimizeIndex(RodaConstants.INDEX_FILE);
      index.optimizeIndex(RodaConstants.INDEX_PRESERVATION_AGENTS);
    } catch (GenericException e) {
      throw new PluginException("Error optimizing index", e);
    }

    return new Report();
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new ReindexAIPPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.MISC;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  // TODO FIX
  @Override
  public PreservationEventType getPreservationEventType() {
    return null;
  }

  @Override
  public String getPreservationEventDescription() {
    return "XXXXXXXXXX";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXX";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXXXX";
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_MANAGEMENT);
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Arrays.asList(AIP.class);
  }
}
