package org.roda.core.plugins.plugins.internal.synchronization.proccess;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.*;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.plugins.plugins.internal.synchronization.bundle.CreateSyncBundlePlugin;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SynchronizeInstancePlugin extends AbstractPlugin<Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizeInstancePlugin.class);

  private LocalInstance localInstance;

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public String getName() {
    return "Synchronize instances";
  }

  @Override
  public String getDescription() {
    return "Synchronize instances";
  }

  @Override
  public RodaConstants.PreservationEventType getPreservationEventType() {
    return RodaConstants.PreservationEventType.NONE;
  }

  @Override
  public String getPreservationEventDescription() {
    return "";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "";
  }

  @Override
  public PluginType getType() {
    return PluginType.INTERNAL;
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new SynchronizeInstancePlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return false;
  }

  @Override
  public void init() throws PluginException {

  }

  @Override
  public List<Class<Void>> getObjectClasses() {
    return Arrays.asList(Void.class);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    try {
      localInstance = RodaCoreFactory.getLocalInstance();
    } catch (GenericException e) {
      throw new PluginException("Unable to retrieve local instance configuration", e);
    }
    return new Report();
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> list) throws PluginException {
    return PluginHelper.processVoids(this, new RODAProcessingLogic<Void>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<Void> plugin) throws PluginException {
        synchronizeInstance(index, model, storage, report, jobPluginInfo, cachedJob);
      }
    }, index, model, storage);
  }

  private void synchronizeInstance(IndexService index, ModelService model, StorageService storage, Report report,
    JobPluginInfo jobPluginInfo, Job cachedJob) {
    Report reportItem = PluginHelper.initPluginReportItem(this, localInstance.getId(), LocalInstance.class);
    PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
    Report sendReport = null;
    Report remoteActionsReport = null;

    // TODO Create Bundle here

    reportItem.setPluginState(PluginState.SUCCESS);
    report.addReport(reportItem);
    PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);


    try {
      sendReport = executePlugin(index, model, storage, cachedJob, SendSyncBundlePlugin.class.getCanonicalName());
    } catch (InvalidParameterException | PluginException e) {
      LOGGER.debug("Failed to execute fixity check plugin on {}", e.getMessage(), e);
    }

    try {
      remoteActionsReport = executePlugin(index, model, storage, cachedJob,
        RequestRemoteActionsPlugin.class.getCanonicalName());
    } catch (InvalidParameterException | PluginException e) {
      LOGGER.debug("Failed to execute fixity check plugin on {}", e.getMessage(), e);
    }

    try {
      Report jobReport = model.retrieveJobReport(reportItem.getJobId(), reportItem.getSourceObjectId(),
        reportItem.getOutcomeObjectId());
      int totalSteps = jobReport.getTotalSteps();
      jobReport.setTotalSteps(totalSteps + 4);
      model.createOrUpdateJobReport(jobReport, cachedJob);
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      LOGGER.error("Failed to update job report");
      String outcomeDetailsText = "Failed to update job Report '" + localInstance.getId() + "due to: " + e.getMessage();
      reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(outcomeDetailsText);
    }

    if (sendReport != null) {
      report.addReport(sendReport);
    }

    if (remoteActionsReport != null) {
      report.addReport(remoteActionsReport);
    }
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return null;
  }

  @Override
  public void shutdown() {

  }

  private Report executePlugin(IndexService index, ModelService model, StorageService storage, Job job,
    final String pluginId) throws InvalidParameterException, PluginException {
    Plugin<Void> plugin = RodaCoreFactory.getPluginManager().getPlugin(pluginId, Void.class);
    Map<String, String> mergedParams = new HashMap<>(getParameterValues());
    mergedParams.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, job.getId());
    plugin.setParameterValues(mergedParams);
    plugin.setMandatory(false);
    return plugin.execute(index, model, storage, null);
  }

}
