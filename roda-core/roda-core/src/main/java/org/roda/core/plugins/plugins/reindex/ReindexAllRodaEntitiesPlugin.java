/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.reindex;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.utils.IdUtils;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAProcessingLogic;
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
    return "Clears all indexes and recreates them from actual physical data that exists on the storage. This task aims to fix inconsistencies between what "
      + "is shown in the graphical user interface of the repository and what is actually kept at the storage layer. Such inconsistencies may occur for "
      + "various reasons, e.g. index corruption, ungraceful shutdown of the repository, etc.";
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
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> list) throws PluginException {
    final List<Class<? extends IsRODAObject>> classes = PluginHelper.getReindexObjectClasses();
    classes.remove(Job.class);
    return PluginHelper.processVoids(this, new RODAProcessingLogic<Void>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        SimpleJobPluginInfo jobPluginInfo, Plugin<Void> plugin) {
        reindexAll(index, model, report, jobPluginInfo, cachedJob, classes);
      }
    }, index, model, storage, classes.size());
  }

  private void reindexAll(IndexService index, ModelService model, Report pluginReport,
    SimpleJobPluginInfo jobPluginInfo, Job job, List<Class<? extends IsRODAObject>> classes) {

    for (Class<? extends IsRODAObject> reindexClass : classes) {
      Report reportItem = reindexRODAObject(model, reindexClass, jobPluginInfo);
      if (reportItem != null) {
        pluginReport.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
      }
    }

    pluginReport.setPluginState(PluginState.SUCCESS);
  }

  private Report reindexRODAObject(ModelService model, Class<? extends IsRODAObject> reindexClass,
    SimpleJobPluginInfo jobPluginInfo) {
    LOGGER.debug("Creating job to reindexing all {}", reindexClass.getSimpleName());
    Report report = null;

    if (model.hasObjects(reindexClass)) {
      String jobId = IdUtils.createUUID();
      String jobName = ReindexRodaEntityPlugin.class.getSimpleName() + " (" + reindexClass.getSimpleName() + ")";
      report = PluginHelper.initPluginReportItem(this, jobId, Job.class);

      try {
        String username = PluginHelper.getJobUsername(this, model);
        Job job = initReindexJob(reindexClass, jobId, jobName, username);
        PluginHelper.createAndExecuteJob(job);
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
        report.setPluginState(PluginState.SUCCESS).setPluginDetails(jobName + " ran successfully");
      } catch (RODAException e) {
        LOGGER.error("Error creating job to reindex all {}", reindexClass.getSimpleName(), e);
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        report.setPluginState(PluginState.FAILURE).setPluginDetails(jobName + " did not run successfully");
      }
    } else {
      jobPluginInfo.incrementObjectsProcessedWithSuccess();
    }

    return report;
  }

  private <T extends IsRODAObject> Job initReindexJob(Class<T> reindexClass, String jobId, String jobName,
    String username) throws NotFoundException {
    Job job = new Job();
    job.setId(jobId);
    job.setName(jobName);

    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES, "true");
    job.setPluginParameters(pluginParameters);
    job.setPluginType(PluginType.MISC);
    job.setUsername(username);

    job.setPlugin(PluginHelper.getReindexPluginName(reindexClass));

    if (TransferredResource.class.equals(reindexClass) || LogEntry.class.equals(reindexClass)
      || RODAMember.class.equals(reindexClass) || IndexedPreservationAgent.class.equals(reindexClass)) {
      job.setSourceObjects(SelectedItemsNone.create());
    } else {
      job.setSourceObjects(SelectedItemsAll.create(reindexClass));
    }

    return job;
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

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.NONE;
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
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_REINDEX);
  }

  @Override
  public List<Class<Void>> getObjectClasses() {
    return Arrays.asList(Void.class);
  }

}
