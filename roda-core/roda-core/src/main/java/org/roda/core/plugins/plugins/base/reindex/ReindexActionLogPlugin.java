/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base.reindex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexActionLogPlugin extends AbstractPlugin<Void> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReindexActionLogPlugin.class);
  private boolean clearIndexes = false;
  private boolean optimizeIndexes = true;
  private static int dontReindexOlderThanXDays = RodaCoreFactory.getRodaConfigurationAsInt(0, "core", "actionlogs",
    "delete_older_than_x_days");

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_INT_VALUE,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_INT_VALUE, "Delete older logs", PluginParameterType.INTEGER,
        Integer.toString(dontReindexOlderThanXDays), false, false, "Delete logs older than x days."));

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
    ArrayList<PluginParameter> parameters = new ArrayList<PluginParameter>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_INT_VALUE));
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

    if (parameters != null && parameters.containsKey(RodaConstants.PLUGIN_PARAMS_INT_VALUE)) {
      try {
        int dontReindexOlderThan = Integer.parseInt(parameters.get(RodaConstants.PLUGIN_PARAMS_INT_VALUE));
        if (dontReindexOlderThan > 0) {
          dontReindexOlderThanXDays = dontReindexOlderThan;
        }
      } catch (NumberFormatException e) {
        // do nothing
      }
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> list) throws PluginException {
    return PluginHelper.processVoids(this, new RODAProcessingLogic<Void>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        SimpleJobPluginInfo jobPluginInfo, Plugin<Void> plugin) {
        reindexActionLogs(index, model, report, jobPluginInfo);
      }
    }, index, model, storage);
  }

  private void reindexActionLogs(IndexService index, ModelService model, Report pluginReport,
    SimpleJobPluginInfo jobPluginInfo) {
    pluginReport.setPluginState(PluginState.SUCCESS);

    Date firstDayToIndex = calculateFirstDayToIndex(dontReindexOlderThanXDays);
    jobPluginInfo = reindexActionLogsStillNotInStorage(index, firstDayToIndex, pluginReport, jobPluginInfo,
      dontReindexOlderThanXDays);
    jobPluginInfo = reindexActionLogsInStorage(index, model, firstDayToIndex, pluginReport, jobPluginInfo,
      dontReindexOlderThanXDays);
  }

  private SimpleJobPluginInfo reindexActionLogsInStorage(IndexService index, ModelService model, Date firstDayToIndex,
    Report pluginReport, SimpleJobPluginInfo jobPluginInfo, int dontReindexOlderThanXDays) {
    CloseableIterable<Resource> actionLogs = null;

    try {
      boolean recursive = false;
      actionLogs = model.getStorage()
        .listResourcesUnderContainer(DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_ACTIONLOG), recursive);

      for (Resource resource : actionLogs) {
        if (resource instanceof Binary
          && isToIndex(resource.getStoragePath().getName(), firstDayToIndex, dontReindexOlderThanXDays)) {
          LOGGER.debug("Going to reindex '{}'", resource.getStoragePath());
          Binary b = (Binary) resource;
          try {
            BufferedReader br = new BufferedReader(new InputStreamReader(b.getContent().createInputStream()));
            jobPluginInfo = index.reindexActionLog(br, jobPluginInfo);
          } catch (IOException | GenericException e) {
            LOGGER.error("Error while trying to reindex action log '" + resource.getStoragePath() + "' from storage",
              e);
          }

          try {
            PluginHelper.updateJobInformation(this, jobPluginInfo);
          } catch (JobException e) {
            LOGGER.error("Could not update job information");
          }
        }
      }
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException e) {
      pluginReport.setPluginState(PluginState.FAILURE).setPluginDetails("Could not reindex action logs from storage");
      LOGGER.error("Error while trying to reindex action logs from storage", e);
    } finally {
      IOUtils.closeQuietly(actionLogs);
    }

    return jobPluginInfo;
  }

  private SimpleJobPluginInfo reindexActionLogsStillNotInStorage(IndexService index, Date firstDayToIndex,
    Report pluginReport, SimpleJobPluginInfo jobPluginInfo, int dontReindexOlderThanXDays) {
    Path logFilesDirectory = RodaCoreFactory.getLogPath();
    DirectoryStream.Filter<Path> logFilesFilter = getLogFilesFilter(firstDayToIndex, dontReindexOlderThanXDays);

    try {
      BufferedReader br = null;
      InputStream logFileInputStream;
      for (Path logFile : Files.newDirectoryStream(logFilesDirectory, logFilesFilter)) {
        LOGGER.debug("Going to reindex '{}'", logFile);
        try {
          logFileInputStream = Files.newInputStream(logFile);
          br = new BufferedReader(new InputStreamReader(logFileInputStream));
          jobPluginInfo = index.reindexActionLog(br, jobPluginInfo);
        } catch (IOException | GenericException e) {
          LOGGER.error("Error reindexing action log", e);
        } finally {
          IOUtils.closeQuietly(br);
        }

        try {
          PluginHelper.updateJobInformation(this, jobPluginInfo);
        } catch (JobException e) {
          LOGGER.error("Could not update job information");
        }
      }
    } catch (IOException e) {
      pluginReport.setPluginState(PluginState.FAILURE).setPluginDetails("Could not reindex action logs not in storage");
      LOGGER.error("Error while listing action logs for reindexing", e);
    }

    return jobPluginInfo;
  }

  private Date calculateFirstDayToIndex(int dontReindexOlderThanXDays) {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_YEAR, -1 * dontReindexOlderThanXDays);
    Date firstDayToIndex = cal.getTime();
    return firstDayToIndex;
  }

  private DirectoryStream.Filter<Path> getLogFilesFilter(Date firstDayToIndex, int dontReindexOlderThanXDays) {
    return new DirectoryStream.Filter<Path>() {
      public boolean accept(Path file) throws IOException {
        return isToIndex(file.getFileName().toString(), firstDayToIndex, dontReindexOlderThanXDays);
      }
    };
  }

  private boolean isToIndex(String fileName, Date firstDayToIndex, int dontReindexOlderThanXDays) {
    boolean isToIndex = false;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String fileNameWithoutExtension = fileName.replaceFirst(".log$", "");
    try {
      Date logDate = sdf.parse(fileNameWithoutExtension);
      if (dontReindexOlderThanXDays == 0 || logDate.after(firstDayToIndex)) {
        isToIndex = true;
      }

    } catch (ParseException e) {
      // do nothing and carry on
    }
    return isToIndex;
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
