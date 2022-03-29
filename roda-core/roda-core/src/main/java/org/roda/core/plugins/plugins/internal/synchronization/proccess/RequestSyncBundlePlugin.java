package org.roda.core.plugins.plugins.internal.synchronization.proccess;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.SyncUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
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
import org.roda.core.data.v2.synchronization.bundle.BundleState;
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
        requestRemoteActions(model, storage, report, jobPluginInfo, cachedJob);
      }
    }, index, model, storage);
  }

  private void requestRemoteActions(ModelService model, StorageService storage, Report report,
    JobPluginInfo jobPluginInfo, Job cachedJob) {
    Report reportItem = PluginHelper.initPluginReportItem(this, cachedJob.getId(), Job.class);
    PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
    PluginState pluginState = PluginState.SKIPPED;
    String outcomeDetailsText = "There are no updates from the central instance";

    try {
      final Path path = SyncUtils.requestRemoteActions(localInstance);
      if (path != null) {
        try {
          final Path bundleWorkingDir = SyncUtils.extractBundle(localInstance.getId(), path);
          final int jobs = createJobs(localInstance.getId());
          final BundleState bundleState = SyncUtils.getIncomingBundleState(localInstance.getId());
          final int imported = SyncUtils.importStorage(storage, bundleWorkingDir, bundleState, jobPluginInfo, false);
          outcomeDetailsText = "Received " + jobs + " jobs. Imported " + imported
            + " representations information and risks from Central";
        } catch (AlreadyExistsException | JobAlreadyStartedException e) {
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

  /**
   * Creates the Jobs from RODA Central in RODA local and executes.
   * 
   * @param instanceId
   *          the instance identifier.
   * @return number of jobs created and executed.
   * @throws GenericException
   *           if some error occurs.
   * @throws AuthorizationDeniedException
   *           if some error occurs.
   * @throws RequestNotValidException
   *           if some error occurs.
   * @throws JobAlreadyStartedException
   *           if some error occurs.
   * @throws NotFoundException
   *           if some error occurs.
   */
  public static int createJobs(final String instanceId) throws GenericException, AuthorizationDeniedException,
    RequestNotValidException, JobAlreadyStartedException, NotFoundException {

    PackageState packageState = null;
    int count = 0;
    try {
      packageState = SyncUtils.getIncomingEntityPackageState(instanceId, RodaConstants.CORE_JOB_FOLDER);
    } catch (final NotFoundException e) {
      // do nothing
    }

    if (packageState != null) {
      for (String jobId : packageState.getIdList()) {
        final Path jobPath = SyncUtils.getEntityStoragePath(instanceId, RodaConstants.CORE_JOB_FOLDER)
          .resolve(jobId + ".json");
        final Job job = JsonUtils.readObjectFromFile(jobPath, Job.class);
        PluginHelper.createAndExecuteJob(job);
      }

      count = packageState.getCount();
    }
    return count;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return null;
  }

  @Override
  public void shutdown() {

  }
}
