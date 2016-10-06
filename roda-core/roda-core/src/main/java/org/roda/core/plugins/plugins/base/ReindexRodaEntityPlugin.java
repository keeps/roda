/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexRodaEntityPlugin<T extends IsRODAObject> extends AbstractPlugin<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReindexRodaEntityPlugin.class);
  private boolean clearIndexes = false;
  private int dontReindexOlderThanXDays = RodaCoreFactory.getRodaConfigurationAsInt(0, "core", "actionlogs",
    "delete_older_than_x_days");

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES, "Clear indexes", PluginParameterType.BOOLEAN,
        "false", false, false, "Clear all indexes before reindexing them."));
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
    return "Rebuild index(es)";
  }

  @Override
  public String getDescription() {
    return "Clears the selected index and recreates it from actual physical data that exists on the storage. This task aims to fix inconsistencies between what is shown in the graphical user interface of the repository and what is actually kept at the storage layer. Such inconsistencies may occur for various reasons, e.g. index corruption, ungraceful shutdown of the repository, etc.";
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
    if (parameters != null) {
      if (parameters.get(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES) != null) {
        clearIndexes = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES));
      }

      if (parameters.get(RodaConstants.PLUGIN_PARAMS_INT_VALUE) != null) {
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
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<T> list)
    throws PluginException {
    Report pluginReport = PluginHelper.initPluginReport(this);

    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, list.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);
      pluginReport.setPluginState(PluginState.SUCCESS);

      if (PluginHelper.getJob(this, model).getSourceObjects().getSelectedClass().equals(LogEntry.class.getName())) {
        jobPluginInfo.setSourceObjectsCount(0);
        Date firstDayToIndex = PluginHelper.calculateFirstDayToIndex(dontReindexOlderThanXDays);
        jobPluginInfo = PluginHelper.reindexActionLogsStillNotInStorage(index, firstDayToIndex, pluginReport,
          jobPluginInfo, dontReindexOlderThanXDays);
        jobPluginInfo = PluginHelper.reindexActionLogsInStorage(index, model, firstDayToIndex, pluginReport,
          jobPluginInfo, dontReindexOlderThanXDays);
      } else if (PluginHelper.getJob(this, model).getSourceObjects().getSelectedClass()
        .equals(RODAMember.class.getName())) {
        jobPluginInfo.setSourceObjectsCount(0);
        List<User> users = RodaCoreFactory.getModelService().listUsers();
        List<Group> groups = RodaCoreFactory.getModelService().listGroups();

        jobPluginInfo.setSourceObjectsCount(users.size() + groups.size());

        for (User ldapUser : users) {
          LOGGER.debug("User to be indexed: {}", ldapUser);
          RodaCoreFactory.getModelService().notifyUserUpdated(ldapUser);
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
        }
        for (Group ldapGroup : groups) {
          LOGGER.debug("Group to be indexed: {}", ldapGroup);
          RodaCoreFactory.getModelService().notifyGroupUpdated(ldapGroup);
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
        }
      } else {
        for (T object : list) {
          if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Reindexing {} {}", object.getClass().getSimpleName(), object.getId());
          }

          try {
            index.reindex(storage, object);
            jobPluginInfo.incrementObjectsProcessedWithSuccess();
          } catch (RODAException | IOException e) {
            jobPluginInfo.incrementObjectsProcessedWithFailure();
            LOGGER.error("Error reindexing RODA entity", e);
            pluginReport.setPluginState(PluginState.FAILURE).setPluginDetails("Reindex did not execute successfully");
          }
        }
      }

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);

    } catch (JobException | NotFoundException | GenericException | RequestNotValidException
      | AuthorizationDeniedException e) {
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
        Job job = PluginHelper.getJob(this, index);
        Class selectedClass = Class.forName(job.getSourceObjects().getSelectedClass());
        index.clearIndexes(SolrUtils.getIndexName(selectedClass));
      } catch (GenericException | NotFoundException | ClassNotFoundException e) {
        throw new PluginException("Error clearing index", e);
      }

    } else {
      LOGGER.debug("Skipping clear indexes");
    }

    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    if (clearIndexes) {
      LOGGER.debug("Optimizing indexes");

      try {
        Job job = PluginHelper.getJob(this, index);
        Class selectedClass = Class.forName(job.getSourceObjects().getSelectedClass());
        index.optimizeIndexes(SolrUtils.getIndexName(selectedClass));
      } catch (GenericException | NotFoundException | ClassNotFoundException e) {
        throw new PluginException("Error optimizing index", e);
      }
    }
    return new Report();
  }

  @Override
  public Plugin<T> cloneMe() {
    return new ReindexRodaEntityPlugin<T>();
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
    return "Reindex Roda entity";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "All entities were reindexed with success";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "An error occured while reindexing all entities";
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_MANAGEMENT);
  }

  @Override
  public List<Class<T>> getObjectClasses() {
    return (List) PluginHelper.getReindexObjectClasses();
  }

}
