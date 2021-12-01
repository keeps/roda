package org.roda.core.plugins.plugins.internal.synchronization.proccess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.TokenManager;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.accessToken.AccessToken;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.synchronization.bundle.BundleState;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.plugins.plugins.internal.synchronization.SyncBundleHelper;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.RESTClientUtility;
import org.roda.core.util.ZipUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SendSyncBundlePlugin extends AbstractPlugin<Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SendSyncBundlePlugin.class);

  private LocalInstance localInstance;

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public void init() throws PluginException {

  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Send synchronization bundle";
  }

  @Override
  public String getDescription() {
    return "Send the sync bundle to the central instance";
  }

  @Override
  public RodaConstants.PreservationEventType getPreservationEventType() {
    return RodaConstants.PreservationEventType.NONE;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Send the sync bundle to the central instance";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Sync bundle sent successfully";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Sync bundle not sent successfully";
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
    return new SendSyncBundlePlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
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
        sendSyncBundle(model, report, jobPluginInfo, cachedJob);
      }
    }, index, model, storage);
  }

  private void sendSyncBundle(ModelService model, Report report, JobPluginInfo jobPluginInfo, Job cachedJob)
    throws PluginException {
    Report reportItem = PluginHelper.initPluginReportItem(this, cachedJob.getId(), Job.class);
    PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
    PluginState pluginState = PluginState.SKIPPED;
    String pluginDetails = "";

    try {
      BundleState bundleState = SyncBundleHelper.buildBundleStateFile();
      if (!bundleState.getPackageStateList().isEmpty()) {
        LOGGER.debug("Sending sync bundle to: ", localInstance.getCentralInstanceURL());
        int responseCode = send(localInstance, bundleState);
        if (responseCode == 200) {
          localInstance.setLastSynchronizationDate(bundleState.getToDate());
          RodaCoreFactory.createOrUpdateLocalInstance(localInstance);
          bundleState.setSyncState(BundleState.Status.SENT);
          pluginState = PluginState.SUCCESS;
          jobPluginInfo.incrementObjectsProcessed(pluginState);
        } else {
          pluginDetails = "Server response is " + responseCode;
          pluginState = PluginState.FAILURE;
          bundleState.setSyncState(BundleState.Status.FAILED);
          jobPluginInfo.incrementObjectsProcessedWithFailure();
        }
        SyncBundleHelper.updateBundleStateFile(bundleState);
      }
    } catch (GenericException e) {
      jobPluginInfo.incrementObjectsProcessedWithFailure();
      pluginDetails = e.getMessage();
      pluginState = PluginState.FAILURE;
    }
    reportItem.setPluginState(pluginState).setPluginDetails(pluginDetails);
    report.addReport(reportItem);
    PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
  }

  private int send(LocalInstance localInstance, BundleState bundleStateFile) throws GenericException {
    try {
      Path zipPath = compressBundle(bundleStateFile);
      AccessToken accessToken = TokenManager.getInstance().getAccessToken(localInstance);

      String resource = RodaConstants.API_SEP + RodaConstants.API_REST_V1_DISTRIBUTED_INSTANCE
        + RodaConstants.API_PATH_PARAM_DISTRIBUTED_INSTANCE_SYNC + RodaConstants.API_SEP + localInstance.getId();
      return RESTClientUtility.sendPostRequestWithCompressedFile(localInstance.getCentralInstanceURL(), resource,
        zipPath, accessToken);
    } catch (RODAException | FileNotFoundException e) {
      LOGGER.error("Unable to send bundle to central instance", e);
      throw new GenericException("Unable to send bundle to central instance", e);
    }
  }

  private Path compressBundle(BundleState bundleStateFile) throws PluginException {
    try {
      String fileName = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'.zip'").format(bundleStateFile.getToDate());
      Path filePath = RodaCoreFactory.getSynchronizationDirectoryPath()
        .resolve(RodaConstants.CORE_SYNCHRONIZATION_OUTCOME_FOLDER).resolve(fileName);
      if (FSUtils.exists(filePath)) {
        FSUtils.deletePath(filePath);
      }
      Path file = Files.createFile(filePath);
      File zipFile = ZipUtility.createZIPFile(file.toFile(), new File(localInstance.getBundlePath()));
      bundleStateFile.setZipFile(zipFile.getPath());
      bundleStateFile.setSyncState(BundleState.Status.PREPARED);
      SyncBundleHelper.updateBundleStateFile(bundleStateFile);
      return filePath;
    } catch (GenericException | IOException | NotFoundException e) {
      LOGGER.error("Unable to read bundle state file", e);
      throw new PluginException("Unable to read bundle state file", e);
    }
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return new Report();
  }
}
