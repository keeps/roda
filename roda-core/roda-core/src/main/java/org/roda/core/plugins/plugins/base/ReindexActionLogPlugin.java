/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
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
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.Binary;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Resource;
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
    return "Reindex actions log";
  }

  @Override
  public String getDescription() {
    return "Reset action log index and recreate it from data existing in the storage.";
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
    Date firstDayToIndex = calculateFirstDayToIndex();

    reindexActionLogsStillNotInStorage(index, firstDayToIndex);

    reindexActionLogsInStorage(index, model, firstDayToIndex);

    // FIXME 20160414 hsilva: this should return a better report
    return new Report();

  }

  private void reindexActionLogsInStorage(IndexService index, ModelService model, Date firstDayToIndex) {
    CloseableIterable<Resource> actionLogs = null;

    try {
      boolean recursive = false;
      actionLogs = model.getStorage()
        .listResourcesUnderContainer(DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_ACTIONLOG), recursive);

      for (Resource resource : actionLogs) {
        if (resource instanceof Binary && isToIndex(resource.getStoragePath().getName(), firstDayToIndex)) {
          LOGGER.debug("Going to reindex '{}'", resource.getStoragePath());
          Binary b = (Binary) resource;
          BufferedReader br = new BufferedReader(new InputStreamReader(b.getContent().createInputStream()));
          try {
            index.reindexActionLog(br);
          } catch (GenericException e) {
            LOGGER.error("Error while trying to reindex action log '" + resource.getStoragePath() + "' from storage",
              e);
          }
        }
      }
    } catch (IOException | NotFoundException | GenericException | AuthorizationDeniedException
      | RequestNotValidException e) {
      LOGGER.error("Error while trying to reindex action logs from storage", e);
    } finally {
      IOUtils.closeQuietly(actionLogs);
    }
  }

  private void reindexActionLogsStillNotInStorage(IndexService index, Date firstDayToIndex) {
    Path logFilesDirectory = RodaCoreFactory.getLogPath();
    DirectoryStream.Filter<Path> logFilesFilter = getLogFilesFilter(firstDayToIndex);

    try {
      BufferedReader br = null;
      InputStream logFileInputStream;
      for (Path logFile : Files.newDirectoryStream(logFilesDirectory, logFilesFilter)) {
        LOGGER.debug("Going to reindex '{}'", logFile);
        try {
          logFileInputStream = Files.newInputStream(logFile);
          br = new BufferedReader(new InputStreamReader(logFileInputStream));
          index.reindexActionLog(br);
        } catch (GenericException e) {
          LOGGER.error("Error reindexing action log", e);
        } finally {
          IOUtils.closeQuietly(br);
        }
      }
    } catch (IOException e) {
      LOGGER.error("Error while listing action logs for reindexing", e);
    }
  }

  private Date calculateFirstDayToIndex() {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_YEAR, -1 * dontReindexOlderThanXDays);
    Date firstDayToIndex = cal.getTime();
    return firstDayToIndex;
  }

  private DirectoryStream.Filter<Path> getLogFilesFilter(Date firstDayToIndex) {
    return new DirectoryStream.Filter<Path>() {
      public boolean accept(Path file) throws IOException {
        return isToIndex(file.getFileName().toString(), firstDayToIndex);
      }
    };
  }

  private boolean isToIndex(String fileName, Date firstDayToIndex) {
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
  public List<Class<LogEntry>> getObjectClasses() {
    return Arrays.asList(LogEntry.class);
  }

}
