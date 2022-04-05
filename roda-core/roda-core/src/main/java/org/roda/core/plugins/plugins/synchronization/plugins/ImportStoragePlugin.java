package org.roda.core.plugins.plugins.synchronization.plugins;

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
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.synchronization.bundle.BundleState;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class ImportStoragePlugin extends AbstractPlugin<Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ImportStoragePlugin.class);
  private String bundlePath = null;
  private String instanceIdentifier = null;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();

  @Override
  public List<PluginParameter> getParameters() {
    final ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_BUNDLE_PATH));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER));
    return parameters;
  }

  @Override
  public void setParameterValues(final Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public String getName() {
    return "Import storage ";
  }

  @Override
  public String getDescription() {
    return "Import bundle to storage";
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
    return new ImportStoragePlugin();
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
    return PluginHelper.processVoids(this, new RODAProcessingLogic<Void>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<Void> plugin) throws PluginException {
        importStorage(model, storage, report, cachedJob, jobPluginInfo);
      }
    }, index, model, storage);
  }

  private void importStorage(ModelService model, StorageService storage, Report report, Job cachedJob,
    JobPluginInfo jobPluginInfo) {

    if (Files.exists(Paths.get(bundlePath))) {
      Path bundleWorkingDir = null;
      try {
        bundleWorkingDir = SyncUtils.extractBundle(instanceIdentifier, Paths.get(bundlePath));
        // Vai ser usado nos outros plugins para já fica mas depois vai ser retirado
        BundleState bundleState = SyncUtils.getIncomingBundleState(instanceIdentifier);
        importStorage(storage, bundleWorkingDir);
        SyncUtils.copyAttachments(instanceIdentifier);
      } catch (IOException e) {
        LOGGER.error("Error extracting bundle to {}", bundleWorkingDir.toString(), e);
        report.setPluginState(PluginState.FAILURE)
          .setPluginDetails("Error extracting bundle to " + bundleWorkingDir.toString());
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      } catch (GenericException e) {
        e.printStackTrace();
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

  private void importStorage(StorageService storage, Path tempDirectory) throws GenericException, NotFoundException,
    AuthorizationDeniedException, RequestNotValidException, AlreadyExistsException, JobAlreadyStartedException {
    final FileStorageService temporaryStorage = new FileStorageService(
      tempDirectory.resolve(RodaConstants.CORE_STORAGE_FOLDER), false, null, false);
    final CloseableIterable<Container> containers = temporaryStorage.listContainers();
    final Iterator<Container> containerIterator = containers.iterator();
    while (containerIterator.hasNext()) {
      final Container container = containerIterator.next();
      final StoragePath containerStoragePath = container.getStoragePath();

      final CloseableIterable<Resource> resources = temporaryStorage.listResourcesUnderContainer(containerStoragePath,
        false);
      final Iterator<Resource> resourceIterator = resources.iterator();
      while (resourceIterator.hasNext()) {
        final Resource resource = resourceIterator.next();
        final StoragePath storagePath = resource.getStoragePath();

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
              final Job jobToImport = JsonUtils.getObjectFromJson(inputStream, Job.class);
              final StoragePath jobReportsContainerPath = ModelUtils.getJobReportsStoragePath(jobToImport.getId());
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
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> list) throws PluginException {
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return null;
  }

  @Override
  public void shutdown() {

  }
}
