package org.roda.core.plugins.plugins.internal.synchronization.proccess;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.SyncUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.synchronization.bundle.PackageState;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class RequestSyncBundlePlugin extends AbstractPlugin<Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SendSyncBundlePlugin.class);

  private LocalInstance localInstance;

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public String getName() {
    return "Request synchronization bundle";
  }

  @Override
  public String getDescription() {
    return "Request remote actions to central instance";
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
    return new RequestSyncBundlePlugin();
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
    return null;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> list) throws PluginException {
    return PluginHelper.processVoids(this, new RODAProcessingLogic<Void>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<Void> plugin) throws PluginException {
        try {
          localInstance = RodaCoreFactory.getLocalInstance();
        } catch (GenericException e) {
          throw new PluginException("Unable to retrieve local instance configuration", e);
        }
        requestRemoteActions(model, report, jobPluginInfo, cachedJob);
      }
    }, index, model, storage);
  }

  private void requestRemoteActions(ModelService model, Report report, JobPluginInfo jobPluginInfo, Job cachedJob) {
    Report reportItem = PluginHelper.initPluginReportItem(this, cachedJob.getId(), Job.class);
    PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
    PluginState pluginState = PluginState.SKIPPED;
    String outcomeDetailsText = "There are no updates from the central instance";

    try {
      Path path = SyncUtils.requestRemoteActions(localInstance);
      if (path != null) {
        SyncUtils.extractBundle(localInstance.getId(), path);
        try {
          int jobs = createJobs(localInstance.getId());
          outcomeDetailsText = "Received " + jobs + " jobs";
        } catch (JobAlreadyStartedException e) {
          // Do nothing
        }
      }
      pluginState = PluginState.SUCCESS;
      jobPluginInfo.incrementObjectsProcessed(pluginState);

    } catch (GenericException | IOException | AuthorizationDeniedException | RequestNotValidException
      | NotFoundException e) {
      jobPluginInfo.incrementObjectsProcessedWithFailure();
      pluginState = PluginState.FAILURE;
      outcomeDetailsText = e.getMessage();
    }

    reportItem.setPluginState(pluginState).setPluginDetails(outcomeDetailsText);
    report.addReport(reportItem);
    PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
  }

  public static int createJobs(String instanceId) throws GenericException, AuthorizationDeniedException,
    RequestNotValidException, JobAlreadyStartedException, NotFoundException {

    PackageState packageState = SyncUtils.getIncomingEntityPackageState(instanceId, "job");
    for (String jobId : packageState.getIdList()) {
      Path jobPath = SyncUtils.getEntityStoragePath(instanceId, RodaConstants.CORE_JOB_FOLDER).resolve(jobId + ".json");
      Job job = JsonUtils.readObjectFromFile(jobPath, Job.class);
      PluginHelper.createAndExecuteJob(job);
    }

    return packageState.getCount();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return null;
  }

  @Override
  public void shutdown() {

  }
}
