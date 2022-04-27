package org.roda.core.plugins.plugins.internal.synchronization.proccess;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.SyncUtils;
import org.roda.core.common.TokenManager;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.accessToken.AccessToken;
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
import org.roda.core.plugins.plugins.internal.synchronization.ImportUtils;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class RequestSyncBundlePlugin extends AbstractPlugin<Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RequestSyncBundlePlugin.class);

  private LocalInstance localInstance;
  String bundleName;

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
        requestRemoteActions(model, index, storage, report, jobPluginInfo, cachedJob);
      }

    }, index, model, storage);
  }

  private void requestRemoteActions(ModelService model, IndexService index, StorageService storage, Report report,
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
          final int jobs = createJobs(localInstance.getId(), index);
          final BundleState bundleState = SyncUtils.getIncomingBundleState(localInstance.getId());
          final int imported = SyncUtils.importStorage(storage, bundleWorkingDir, bundleState, jobPluginInfo, false);

          ImportUtils.deleteBundleEntities(model, index, cachedJob, this, jobPluginInfo, localInstance,
            bundleWorkingDir, bundleState.getValidationEntityList(), report);

          ImportUtils.reindexFromFile(model, index, bundleState, jobPluginInfo, report, bundleWorkingDir);

          SyncUtils.removeSyncBundleLocal(bundleName,RodaConstants.CORE_SYNCHRONIZATION_OUTCOME_FOLDER);
          Files.deleteIfExists(path);
          String centralBundleName = path.toString().split("remote_actions/")[1];

          removeSyncBundlesFromCentral(bundleName, RodaConstants.CORE_SYNCHRONIZATION_INCOMING_FOLDER);
          removeSyncBundlesFromCentral(centralBundleName, RodaConstants.CORE_SYNCHRONIZATION_OUTCOME_FOLDER);

          ImportUtils.updateEntityCounter(bundleState, localInstance);
          ImportUtils.createLastSyncFile(bundleWorkingDir, localInstance, cachedJob.getId(), bundleState.getId());
          RodaCoreFactory.createOrUpdateLocalInstance(localInstance);
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

  private void removeSyncBundlesFromCentral(String bundleName, String bundleDirectory) {

    try {
      AccessToken accessToken = TokenManager.getInstance().getAccessToken(localInstance);
      String resource = RodaConstants.API_SEP + RodaConstants.API_REST_V1_DISTRIBUTED_INSTANCE + "remove"
        + RodaConstants.API_SEP + "bundle" + RodaConstants.API_QUERY_START + RodaConstants.SYNCHRONIZATION_BUNDLE_NAME
        + RodaConstants.API_QUERY_ASSIGN_SYMBOL + bundleName + RodaConstants.API_QUERY_SEP
        + RodaConstants.SYNCHRONIZATION_BUNDLE_DIRECTORY + RodaConstants.API_QUERY_ASSIGN_SYMBOL + bundleDirectory;

      CloseableHttpClient httpClient = HttpClientBuilder.create().build();
      HttpGet httpGet = new HttpGet(localInstance.getCentralInstanceURL() + resource);
      httpGet.addHeader("Authorization", "Bearer " + accessToken.getToken());
      httpGet.addHeader("content-type", "application/json");
      httpGet.addHeader("Accept", "application/json");

      HttpResponse response = httpClient.execute(httpGet);

      if (response.getStatusLine().getStatusCode() == RodaConstants.HTTP_RESPONSE_CODE_SUCCESS) {
        EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
      }
    } catch (AuthenticationDeniedException | GenericException | IOException e) {
      // do nothing
    }

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
  public static int createJobs(final String instanceId, final IndexService index) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException, JobAlreadyStartedException, NotFoundException {

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
        if (checkIfJobExist(index, job.getId())) {
          String SYNC_ACTION_TYPE = RodaCoreFactory
            .getRodaConfigurationAsString("core.synchronization.preservationActionExecution.type");
          if (!StringUtils.isNotBlank(SYNC_ACTION_TYPE)) {
            PluginHelper.createAndExecuteJob(job);
          } else if (RodaConstants.JOB_EXECUTION_TYPE_APPROVAL.equals(SYNC_ACTION_TYPE)) {
            job.setState(Job.JOB_STATE.PENDING_APPROVAL);
            PluginHelper.createJob(job);
          } else {
            job.setState(Job.JOB_STATE.SCHEDULED);
            PluginHelper.createJob(job);
          }
        }
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

  private static boolean checkIfJobExist(final IndexService index, final String jobId) {
    try {
      index.retrieve(Job.class, jobId, Collections.singletonList(RodaConstants.INDEX_UUID));
    } catch (NotFoundException e) {
      return true;
    } catch (GenericException e) {
      LOGGER.error("Can't retrieve the JOB {} ", e.getMessage());
    }
    return false;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_BUNDLE_NAME)) {
      bundleName = parameters.get(RodaConstants.PLUGIN_PARAMS_BUNDLE_NAME);
    }

  }
}
