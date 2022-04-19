package org.roda.core.plugins.plugins.internal.synchronization.proccess;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.SyncUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.utils.URLUtils;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.synchronization.bundle.BundleState;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.plugins.plugins.internal.synchronization.ImportUtils;
import org.roda.core.storage.Container;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ImportSyncBundlePlugin extends AbstractPlugin<Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ImportSyncBundlePlugin.class);

  private String bundlePath = null;
  private String instanceIdentifier = null;
  private int syncErrors = 0;
  private List<String> aipsToRemove = new ArrayList<>();

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_BUNDLE_PATH,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_BUNDLE_PATH, "Destination path",
        PluginParameter.PluginParameterType.STRING, "", true, false, "Destination path where bundles will be created"));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER, "Instance identifier",
        PluginParameter.PluginParameterType.STRING, "", true, false,
        "Identifier of the instance that will be synchronized "));
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_BUNDLE_PATH));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_BUNDLE_PATH)) {
      bundlePath = parameters.get(RodaConstants.PLUGIN_PARAMS_BUNDLE_PATH);
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER)) {
      instanceIdentifier = parameters.get(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER);
    }
    getParameterValues().put(RodaConstants.PLUGIN_PARAMS_REPORTING_CLASS, getClass().getName());
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
    return new ImportSyncBundlePlugin();
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
        importSyncBundle(model, index, storage, report, cachedJob, jobPluginInfo);
      }
    }, index, model, storage);
  }

  private void importSyncBundle(final ModelService model, final IndexService index, final StorageService storage,
    final Report report, final Job cachedJob, final JobPluginInfo jobPluginInfo) throws PluginException {
    if (Files.exists(Paths.get(bundlePath))) {
      report.setSourceAndOutcomeObjectClass(AIP.class.getName(), AIP.class.getName());
      Path bundleWorkingDir = null;
      try {
        DistributedInstance distributedInstance = model.retrieveDistributedInstance(instanceIdentifier);
        distributedInstance.cleanEntitiesSummaries();
        bundleWorkingDir = SyncUtils.extractBundle(instanceIdentifier, Paths.get(bundlePath));
        BundleState bundleState = SyncUtils.getIncomingBundleState(instanceIdentifier);
        importStorage(model, index, storage, cachedJob, bundleWorkingDir, bundleState, jobPluginInfo, report);
        SyncUtils.copyAttachments(instanceIdentifier);

        // Delete entities

        ImportUtils.deleteBundleEntities(model, index, cachedJob, this, jobPluginInfo, distributedInstance,
          bundleWorkingDir, bundleState.getValidationEntityList(), report);

        // Validate entities
        ImportUtils.validateEntitiesBundle(index, bundleWorkingDir, bundleState.getValidationEntityList(),
          distributedInstance, syncErrors);

        distributedInstance.setLastSyncDate(bundleState.getToDate());

        ImportUtils.updateEntityCounter(bundleState, distributedInstance);

        ImportUtils.createLastSyncFile(bundleWorkingDir, distributedInstance, cachedJob.getId(), bundleState.getId());

        model.updateDistributedInstance(distributedInstance, cachedJob.getUsername());
        report.setPluginState(PluginState.SUCCESS);
      } catch (IOException e) {
        LOGGER.error("Error extracting bundle to {}", bundleWorkingDir.toString(), e);
        report.setPluginState(PluginState.FAILURE)
          .setPluginDetails("Error extracting bundle to " + bundleWorkingDir.toString());
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      } catch (RODAException e) {
        LOGGER.error("Error creating temporary StorageService on {}", bundleWorkingDir.toString(), e);
        report.setPluginState(PluginState.FAILURE)
          .setPluginDetails("Error creating temporary StorageService on " + bundleWorkingDir.toString());
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      }
    } else {
      report.setPluginState(PluginState.FAILURE).setPluginDetails("Cannot find bundle on path " + bundlePath);
      jobPluginInfo.incrementObjectsProcessedWithFailure();
    }
  }

  private void importStorage(final ModelService model, final IndexService index, final StorageService storage,
    final Job job, final Path tempDirectory, final BundleState bundleState, final JobPluginInfo jobPluginInfo,
    final Report report) throws GenericException, NotFoundException, AuthorizationDeniedException,
    RequestNotValidException, AlreadyExistsException, JobAlreadyStartedException {
    FileStorageService temporaryStorage = new FileStorageService(
      tempDirectory.resolve(RodaConstants.CORE_STORAGE_FOLDER), false, null, false);
    CloseableIterable<Container> containers = temporaryStorage.listContainers();
    Iterator<Container> containerIterator = containers.iterator();
    while (containerIterator.hasNext()) {
      Container container = containerIterator.next();
      StoragePath containerStoragePath = container.getStoragePath();

      CloseableIterable<Resource> resources = temporaryStorage.listResourcesUnderContainer(containerStoragePath, false);
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
            try (
              InputStream inputStream = storage.getBinary(resource.getStoragePath()).getContent().createInputStream()) {
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
    ImportUtils.reindexBundle(model, index, bundleState, jobPluginInfo, report);
    PluginHelper.updatePartialJobReport(this, model, report, true, job);
  }

  private void importJobAttachments(Path tempDirectory) {
    Path jobAttachments = tempDirectory.resolve(RodaConstants.CORE_JOB_ATTACHMENTS_FOLDER);
    if (Files.exists(jobAttachments)) {
      try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(jobAttachments)) {
        for (Path path : dirStream) {
          Path jobAttachmentPath = RodaCoreFactory.getJobAttachmentsDirectoryPath().resolve(path.getFileName());
          Files.createDirectory(jobAttachmentPath);
          Files.list(path).forEach(file -> {
            try {
              String fileName = URLUtils.decode(file.getFileName().toString());
              Files.copy(file, jobAttachmentPath.resolve(fileName));
            } catch (IOException e) {
              LOGGER.error("Error creating file: " + file, e);
            }
          });
        }
      } catch (IOException e) {
        LOGGER.error("Error listing directory: " + jobAttachments, e);
      }
    }
  }

  private void validateChecksum(Path tempDirectory, BundleState bundleState) {
    // TODO validate checksum
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
