/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.SelectedItemsAll;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
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

public class ReindexAllRodaEntitiesPlugin extends AbstractPlugin<Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReindexAllRodaEntitiesPlugin.class);

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
    return "Rebuild all indexes";
  }

  @Override
  public String getDescription() {
    return "Clears all indexes and recreates them from actual physical data that exists on the storage. This task aims to fix inconsistencies between what is shown in the graphical user interface of the repository and what is actually kept at the storage layer. Such inconsistencies may occur for various reasons, e.g. index corruption, ungraceful shutdown of the repository, etc.";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<Void> list)
    throws PluginException {
    Report pluginReport = PluginHelper.initPluginReport(this);

    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, list.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      List<Class<? extends IsRODAObject>> classes = PluginHelper.getReindexObjectClasses();
      classes.remove(Job.class);
      jobPluginInfo.setSourceObjectsCount(classes.size());

      for (Class<? extends IsRODAObject> reindexClass : classes) {
        Report reportItem = reindexRODAObject(model, reindexClass, jobPluginInfo);
        if (reportItem != null) {
          pluginReport.addReport(reportItem);
          PluginHelper.updatePartialJobReport(this, model, index, reportItem, true);
        }
      }

      pluginReport.setPluginState(PluginState.SUCCESS);
      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (JobException e) {
      LOGGER.error("Error reindexing RODA entity", e);
    }

    return pluginReport;
  }

  private Report reindexRODAObject(ModelService model, Class<? extends IsRODAObject> reindexClass,
    SimpleJobPluginInfo jobPluginInfo) {
    LOGGER.debug("Creating job to reindexing all {}", reindexClass.getSimpleName());
    Report report;

    if (TransferredResource.class.equals(reindexClass)) {
      // TransferredResource does not need a job
      try {
        RodaCoreFactory.getTransferredResourcesScanner().updateAllTransferredResources(null, false);
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
      } catch (IsStillUpdatingException e) {
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      }
    } else {
      if (model.hasObjects(reindexClass)) {
        Job job = new Job();
        job.setId(UUID.randomUUID().toString());
        job.setName(ReindexRodaEntityPlugin.class.getSimpleName() + " (" + reindexClass.getSimpleName() + ")");

        Map<String, String> pluginParameters = new HashMap<>();
        pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES, "true");
        job.setPluginParameters(pluginParameters);

        if (LogEntry.class.equals(reindexClass)) {
          job.setPlugin(ReindexActionLogPlugin.class.getName());
        } else {
          job.setPlugin(ReindexRodaEntityPlugin.class.getName());
        }

        job.setSourceObjects(SelectedItemsAll.create(reindexClass));
        job.setPluginType(PluginType.MISC);

        report = PluginHelper.initPluginReportItem(this, job.getId(), Job.class);

        try {
          job.setUsername(PluginHelper.getJobUsername(this, model));
          PluginHelper.createAndExecuteJob(job);
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
          report.setPluginState(PluginState.SUCCESS).setPluginDetails(job.getName() + " ran successfully");
        } catch (RODAException e) {
          LOGGER.error("Error creating job to reindex all {}", reindexClass.getSimpleName(), e);
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          report.setPluginState(PluginState.FAILURE).setPluginDetails(job.getName() + " did not run successfully");
        }

        return report;
      } else {
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
      }
    }

    return null;
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // Do not need to clear indexes, single jobs already does it by default
    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // Do not need to optimize indexes, single jobs already does it by default
    return new Report();
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new ReindexAllRodaEntitiesPlugin();
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
  public List<Class<Void>> getObjectClasses() {
    return (List) Arrays.asList(Void.class);
  }

}
