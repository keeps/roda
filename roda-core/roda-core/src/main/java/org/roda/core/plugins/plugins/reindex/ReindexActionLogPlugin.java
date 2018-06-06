/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.reindex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.LogEntryJsonParseException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexActionLogPlugin extends AbstractPlugin<Void> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReindexActionLogPlugin.class);
  private boolean clearIndexes = false;
  private boolean optimizeIndexes = true;
  private int dontReindexOlderThanXDays = 90;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DELETE_OLDER_THAN_X_DAYS,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DELETE_OLDER_THAN_X_DAYS, "Delete older logs",
        PluginParameterType.INTEGER, "90", false, false, "Delete logs older than x days."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES, "Clear indexes", PluginParameterType.BOOLEAN,
        "false", false, false, "Clear all indexes before reindexing them."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_OPTIMIZE_INDEXES,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_OPTIMIZE_INDEXES, "Optimize indexes", PluginParameterType.BOOLEAN,
        "true", false, false, "Optimize indexes after reindexing them."));
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
    return "Rebuild log index";
  }

  @Override
  public String getDescription() {
    return "Clears the index and recreates it from actual physical data that exists on the storage. This task aims to fix inconsistencies between"
      + " what is shown in the graphical user interface of the repository and what is actually kept at the storage layer. Such inconsistencies may"
      + " occur for various reasons, e.g. index corruption, ungraceful shutdown of the repository, etc.";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DELETE_OLDER_THAN_X_DAYS));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_OPTIMIZE_INDEXES));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters != null && parameters.containsKey(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES)) {
      clearIndexes = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES));
    }

    if (parameters != null && parameters.containsKey(RodaConstants.PLUGIN_PARAMS_OPTIMIZE_INDEXES)) {
      optimizeIndexes = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_OPTIMIZE_INDEXES));
    }

    if (parameters != null && parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DELETE_OLDER_THAN_X_DAYS)) {
      try {
        int dontReindexOlderThan = Integer
          .parseInt(parameters.get(RodaConstants.PLUGIN_PARAMS_DELETE_OLDER_THAN_X_DAYS));
        if (dontReindexOlderThan >= 0) {
          dontReindexOlderThanXDays = dontReindexOlderThan;
        } else {
          dontReindexOlderThanXDays = 0;
        }
      } catch (NumberFormatException e) {
        dontReindexOlderThanXDays = 0;
      }
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> list) throws PluginException {
    return PluginHelper.processVoids(this, new RODAProcessingLogic<Void>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<Void> plugin) {
        reindexActionLogs(index, model, report, cachedJob, jobPluginInfo);
      }
    }, index, model, storage);
  }

  private void reindexActionLogs(IndexService index, ModelService model, Report report, Job job,
    JobPluginInfo jobPluginInfo) {
    report.setPluginState(PluginState.SUCCESS);

    for (OptionalWithCause<LogEntry> logEntry : model.listLogEntries(dontReindexOlderThanXDays)) {
      jobPluginInfo.incrementObjectsCount();

      if (logEntry.isPresent()) {
        index.reindexActionLog(logEntry.get());
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
      } else {
        jobPluginInfo.incrementObjectsProcessedWithFailure();

        // INFO when log entry is not present, a unique id is needed to create
        // multiple reports
        String id;

        StringBuilder message = new StringBuilder("Could not parse log entry: ");
        if (logEntry.getCause() instanceof LogEntryJsonParseException) {
          LogEntryJsonParseException cause = (LogEntryJsonParseException) logEntry.getCause();
          id = IdUtils.createUUID(cause.getFilename() + cause.getLine());
          message.append("Error parsing JSON on file " + cause.getFilename() + " on line " + cause.getLine());
          if (cause.getCause() != null) {
            message.append("\n cause: [" + cause.getCause().getCause().getClass().getSimpleName() + "] "
              + cause.getCause().getCause().getMessage());
          }
        } else {
          id = IdUtils.createUUID();
          RODAException cause = logEntry.getCause();
          message.append("[" + cause.getClass().getSimpleName() + "] " + cause.getMessage());
          if (cause.getCause() != null) {
            message.append(
              "\n cause: [" + cause.getCause().getClass().getSimpleName() + "] " + cause.getCause().getMessage());
          }
        }

        Report reportItem = PluginHelper.initPluginReportItem(this, id, LogEntry.class);
        reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(message.toString());
        report.addReport(reportItem);

        PluginHelper.updatePartialJobReport(this, model, reportItem, false, job);
      }
    }

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

    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    if (optimizeIndexes) {
      LOGGER.debug("Optimizing indexes");
      try {
        index.optimizeIndex(RodaConstants.INDEX_ACTION_LOG);
      } catch (GenericException e) {
        throw new PluginException("Error optimizing index", e);
      }
    }

    return new Report();
  }

  @Override
  public Plugin<Void> cloneMe() {
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

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.NONE;
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
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_REINDEX);
  }

  @Override
  public List<Class<Void>> getObjectClasses() {
    return Arrays.asList(Void.class);
  }

}
