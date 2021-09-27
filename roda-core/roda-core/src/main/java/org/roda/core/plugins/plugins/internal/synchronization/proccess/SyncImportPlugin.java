package org.roda.core.plugins.plugins.internal.synchronization.proccess;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.synchronization.bundle.BundleState;
import org.roda.core.data.v2.synchronization.bundle.PackageState;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.Container;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.util.IdUtils;
import org.roda.core.util.ZipUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SyncImportPlugin extends AbstractPlugin<Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SyncImportPlugin.class);

  private String bundlePath = null;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_BUNDLE_PATH,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_BUNDLE_PATH, "Destination path",
        PluginParameter.PluginParameterType.STRING, "", true, false, "Destination path where bundles will be created"));
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_BUNDLE_PATH));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_BUNDLE_PATH)) {
      bundlePath = parameters.get(RodaConstants.PLUGIN_PARAMS_BUNDLE_PATH);
    }
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public String getName() {
    return "Import sync bundle";
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
    return new SyncImportPlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public void init() throws PluginException {

  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public List<Class<Void>> getObjectClasses() {
    return Arrays.asList(Void.class);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    return PluginHelper.processVoids(this, new RODAProcessingLogic<Void>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<Void> plugin) throws PluginException {
        importSyncBundle(storage, report, jobPluginInfo);
      }
    }, index, model, storage);
  }

  private void importSyncBundle(StorageService storage, Report report, JobPluginInfo jobPluginInfo) {
    if (Files.exists(Paths.get(bundlePath))) {
      Path tempDirectory = null;
      FileStorageService temporaryStorage;
      try {
        tempDirectory = Files.createTempDirectory(Paths.get(bundlePath).getFileName().toString());
        ZipUtility.extractFilesFromZIP(new File(bundlePath), tempDirectory.toFile(), true);
        BundleState bundleState = JsonUtils.readObjectFromFile(tempDirectory.resolve("state.json"), BundleState.class);
        validateChecksum(tempDirectory, bundleState);
        temporaryStorage = new FileStorageService(tempDirectory.resolve(RodaConstants.CORE_STORAGE_FOLDER), false, null,
          false);

        CloseableIterable<Container> containers = temporaryStorage.listContainers();
        Iterator<Container> containerIterator = containers.iterator();
        while (containerIterator.hasNext()) {
          Container container = containerIterator.next();
          StoragePath containerStoragePath = container.getStoragePath();

          CloseableIterable<Resource> resources = temporaryStorage.listResourcesUnderContainer(containerStoragePath,
            false);
          Iterator<Resource> resourceIterator = resources.iterator();
          while (resourceIterator.hasNext()) {
            Resource resource = resourceIterator.next();
            StoragePath storagePath = resource.getStoragePath();

            // if the resource already exists, remove it before moving the updated resource
            if (storage.exists(storagePath)) {
              storage.deleteResource(storagePath);
            }
            storage.move(temporaryStorage, storagePath, storagePath);

            // Job and job reports
            if (resource.getStoragePath().getContainerName().equals(RodaConstants.STORAGE_CONTAINER_JOB)) {
              if (!resource.isDirectory()) {
                try (InputStream inputStream = storage.getBinary(resource.getStoragePath()).getContent()
                  .createInputStream()) {
                  Job jobToImport = JsonUtils.getObjectFromJson(inputStream, Job.class);
                  StoragePath jobReportsContainerPath = ModelUtils.getJobReportsStoragePath(jobToImport.getId());
                  if (storage.exists(jobReportsContainerPath)) {
                    storage.deleteResource(jobReportsContainerPath);
                  }
                  storage.createDirectory(jobReportsContainerPath);
                } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
                  | IOException e) {
                  LOGGER.error("Error getting Job json from binary", e);
                }
              }
            }

          }
        }
        reindexBundle(bundleState, jobPluginInfo);
        report.setPluginState(PluginState.SUCCESS);
      } catch (IOException e) {
        LOGGER.error("Error extracting bundle to {}", tempDirectory.toString(), e);
        report.setPluginState(PluginState.FAILURE)
          .setPluginDetails("Error extracting bundle to " + tempDirectory.toString());
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      } catch (RODAException e) {
        LOGGER.error("Error creating temporary StorageService on {}", tempDirectory.toString(), e);
        report.setPluginState(PluginState.FAILURE)
          .setPluginDetails("Error creating temporary StorageService on " + tempDirectory.toString());
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      }
    } else {
      report.setPluginState(PluginState.FAILURE).setPluginDetails("Cannot find bundle on path " + bundlePath);
      jobPluginInfo.incrementObjectsProcessedWithFailure();
    }
  }

  private void validateChecksum(Path tempDirectory, BundleState bundleState){
    // TODO validate checksum
  }

  private void reindexBundle(BundleState bundleState, JobPluginInfo jobPluginInfo) throws NotFoundException,
    AuthorizationDeniedException, JobAlreadyStartedException, GenericException, RequestNotValidException {
    List<PackageState> packageStateList = bundleState.getPackageStateList();
    jobPluginInfo.setSourceObjectsCount(packageStateList.size());
    for (PackageState packageState : packageStateList) {
      if (packageState.getCount() > 0) {
        Job job = new Job();
        job.setId(IdUtils.createUUID());
        job.setName("Reindex RODA entity (" + packageState.getClassName() + ")");
        job.setPluginType(PluginType.INTERNAL);
        job.setUsername(RodaConstants.ADMIN);

        job.setPlugin(PluginHelper.getReindexPluginName(packageState.getClassName()));
        job.setSourceObjects(SelectedItemsList.create(packageState.getClassName(), packageState.getIdList()));

        PluginHelper.createAndExecuteJob(job);
      }
      jobPluginInfo.incrementObjectsProcessedWithSuccess();
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> list) throws PluginException {
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return new Report();
  }

}
