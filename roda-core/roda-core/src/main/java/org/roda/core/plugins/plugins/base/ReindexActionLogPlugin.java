/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.log.LogEntry;
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

public class ReindexActionLogPlugin extends AbstractPlugin<LogEntry> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReindexActionLogPlugin.class);
  private boolean clearIndexes = true;
  private int dontReindexOlderThanXDays = RodaCoreFactory.getRodaConfigurationAsInt(0, "core", "actionlogs",
    "delete_older_than_x_days");

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
    return "Rebuild log index";
  }

  @Override
  public String getDescription() {
    return "Clears the activity log index and rebuilds it from actual physical data existing on the storage. This task aims to fix inconsistencies between what is shown in the graphical user interface of the repository and what is actually kept at the storage layer. Such inconsistencies may occur for various reasons, e.g. index corruption, ungraceful shutdown of the repository, etc.";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters != null && parameters.get(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES) != null) {
      clearIndexes = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES));
    }

    if (parameters != null && parameters.get(RodaConstants.PLUGIN_PARAMS_INT_VALUE) != null) {
      try {
        int dontReindexOlderThanXDays = Integer.parseInt(parameters.get(RodaConstants.PLUGIN_PARAMS_INT_VALUE));
        if (dontReindexOlderThanXDays > 0) {
          this.dontReindexOlderThanXDays = dontReindexOlderThanXDays;
        }
      } catch (NumberFormatException e) {
        // do nothing
      }
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<LogEntry> list)
    throws PluginException {

    Report pluginReport = PluginHelper.initPluginReport(this);

    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, list.size());
      jobPluginInfo.setSourceObjectsCount(0);
      PluginHelper.updateJobInformation(this, jobPluginInfo);
      pluginReport.setPluginState(PluginState.SUCCESS);

      Date firstDayToIndex = PluginHelper.calculateFirstDayToIndex(dontReindexOlderThanXDays);
      jobPluginInfo = PluginHelper.reindexActionLogsStillNotInStorage(index, firstDayToIndex, pluginReport,
        jobPluginInfo, dontReindexOlderThanXDays);
      jobPluginInfo = PluginHelper.reindexActionLogsInStorage(index, model, firstDayToIndex, pluginReport,
        jobPluginInfo, dontReindexOlderThanXDays);

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (JobException e) {
      LOGGER.error("Error reindexing RODA entity", e);
    }

    return pluginReport;
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    if (clearIndexes) {
      LOGGER.debug("Clearing indexes");
      try {
        index.clearIndex(RodaConstants.INDEX_ACTION_LOG);
      } catch (GenericException e) {
        throw new PluginException("Error clearing index", e);
      }
    } else {
      LOGGER.debug("Skipping clear indexes");
    }

    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    LOGGER.debug("Optimizing indexes");
    try {
      index.optimizeIndex(RodaConstants.INDEX_ACTION_LOG);
    } catch (GenericException e) {
      throw new PluginException("Error optimizing index", e);
    }

    return null;
  }

  @Override
  public Plugin<LogEntry> cloneMe() {
    return new ReindexActionLogPlugin();
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
    return "Reindexed action logs";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Reindexed action logs successfully";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Reindex of action logs failed";
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public List<Class<LogEntry>> getObjectClasses() {
    return Arrays.asList(LogEntry.class);
  }

}
