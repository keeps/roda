package org.roda.core.plugins.plugins.internal.synchronization.proccess;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.roda.core.data.utils.CentralEntitiesJsonUtils;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.utils.URLUtils;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.synchronization.bundle.BundleState;
import org.roda.core.data.v2.synchronization.bundle.CentralEntities;
import org.roda.core.data.v2.synchronization.bundle.EntitiesBundle;
import org.roda.core.data.v2.synchronization.bundle.PackageState;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.plugins.plugins.internal.DeleteRODAObjectPlugin;
import org.roda.core.storage.Container;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ImportSyncBundlePlugin extends AbstractPlugin<Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ImportSyncBundlePlugin.class);

  private String bundlePath = null;
  private String instanceIdentifier = null;
  private int syncErrors = 0;

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
        importSyncBundle(model, storage, report, cachedJob, jobPluginInfo);
      }
    }, index, model, storage);
  }

  private void importSyncBundle(ModelService model, StorageService storage, Report report, Job cachedJob,
    JobPluginInfo jobPluginInfo) {
    if (Files.exists(Paths.get(bundlePath))) {
      Path bundleWorkingDir = null;
      try {
        bundleWorkingDir = SyncUtils.extractBundle(instanceIdentifier, Paths.get(bundlePath));
        BundleState bundleState = SyncUtils.getIncomingBundleState(instanceIdentifier);
        validateChecksum(bundleWorkingDir, bundleState);
        SyncUtils.importStorage(storage, bundleWorkingDir, bundleState, jobPluginInfo, true);
        SyncUtils.copyAttachments(instanceIdentifier);
        deleteAndValidateEntities(instanceIdentifier, bundleWorkingDir, bundleState.getEntitiesBundle());

        DistributedInstance distributedInstance = model.retrieveDistributedInstance(instanceIdentifier);
        distributedInstance.setLastSyncDate(bundleState.getToDate());
        distributedInstance.setSyncErrors(syncErrors);
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

  /**
   * Iterates over the files in bundle (AIP, DIP, Risks) and deletes and validate
   * the Central Entities.
   * 
   * @param instanceIdentifier
   *          the instance identifier.
   * @param bundleWorkingDir
   *          {@link Path}.
   * @param entitiesBundle
   *          {@link EntitiesBundle}.
   * @throws GenericException
   *           if some error occurs.
   * @throws AuthorizationDeniedException
   *           if does not have permission to do this action.
   * @throws RequestNotValidException
   *           if the request is not valid
   * @throws NotFoundException
   *           if some error occurs
   * @throws JobAlreadyStartedException
   *           if the job is already in execution.
   * @throws IOException
   *           if some i/o error occurs.
   */
  public void deleteAndValidateEntities(final String instanceIdentifier, final Path bundleWorkingDir,
    final EntitiesBundle entitiesBundle) throws AuthorizationDeniedException, RequestNotValidException,
    GenericException, NotFoundException, JobAlreadyStartedException, IOException {
    final Map<Path, Class<? extends IsIndexed>> entitiesPathMap = SyncUtils.createEntitiesPaths(bundleWorkingDir,
      entitiesBundle);
    final CentralEntities centralEntities = new CentralEntities();
    for (Map.Entry entry : entitiesPathMap.entrySet()) {
      deleteBundleEntities((Path) entry.getKey(), (Class<? extends IsIndexed>) entry.getValue(), instanceIdentifier,
        centralEntities);
      validateCentralEntities(centralEntities, (Path) entry.getKey(), (Class<? extends IsIndexed>) entry.getValue());
    }

    SyncUtils.writeEntitiesFile(centralEntities, instanceIdentifier);
  }

  /**
   * Checks and delete entities from Central instance.
   * 
   * @param readPath
   *          {@link Path}.
   * @param indexedClass
   *          {@link Class<? extends IsIndexed>}.
   * @param instanceIdentifier
   *          Instance identifier.
   * @param centralEntities
   *          {@link CentralEntities}.
   * @throws GenericException
   *           if some error occurs.
   * @throws AuthorizationDeniedException
   *           if does not have permission to do this action.
   * @throws RequestNotValidException
   *           if the request is not valid
   * @throws NotFoundException
   *           if some error occurs
   * @throws JobAlreadyStartedException
   *           if the job is already in execution.
   */
  private void deleteBundleEntities(final Path readPath, final Class<? extends IsIndexed> indexedClass,
    final String instanceIdentifier, final CentralEntities centralEntities) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException, NotFoundException, JobAlreadyStartedException {
    final List<String> listToRemove = new ArrayList<>();
    final IndexService index = RodaCoreFactory.getIndexService();
    final Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.AIP_INSTANCE_ID, instanceIdentifier));
    if (indexedClass == IndexedPreservationEvent.class) {
      filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_OBJECT_CLASS,
        IndexedPreservationEvent.PreservationMetadataEventClass.REPOSITORY.toString()));
    }

    try (IterableIndexResult<? extends IsIndexed> result = index.findAll(indexedClass, filter, true,
      Collections.singletonList(RodaConstants.INDEX_UUID))) {
      result.forEach(indexed -> {
        boolean exist = false;
        try {
          final JsonParser jsonParser = CentralEntitiesJsonUtils.createJsonParser(readPath);
          while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
            JsonToken token = jsonParser.currentToken();
            if ((token != JsonToken.START_ARRAY) && (token != JsonToken.END_ARRAY)
              && jsonParser.getText().equals(indexed.getId())) {
              exist = true;
            }
          }
          if (!exist) {
            listToRemove.add(indexed.getId());
          }
        } catch (IOException e) {
          LOGGER.error("Can't read the json file {}", e.getMessage());
        }
      });

    } catch (IOException | GenericException | RequestNotValidException e) {
      LOGGER.error("Error getting AIP iterator when creating aip list", e);
    }

    if (!listToRemove.isEmpty()) {

      setRemovedEntities(centralEntities, listToRemove, indexedClass);

      final Job job = new Job();
      job.setId(IdUtils.createUUID());
      job.setName("Delete (" + indexedClass.getName() + ")");
      job.setPluginType(PluginType.INTERNAL);
      job.setUsername(RodaConstants.ADMIN);

      job.setPlugin(DeleteRODAObjectPlugin.class.getName());
      job.setSourceObjects(SelectedItemsList.create(indexedClass.getName(), listToRemove));

      PluginHelper.createAndExecuteJob(job);
    }
  }

  /**
   * Sets the lists of removed entities in {@link CentralEntities} by the given
   * {@link Class<? extends IsIndexed>}.
   * 
   * @param centralEntities
   *          {@link CentralEntities}.
   * @param listToRemove
   *          {@link List}.
   * @param indexedClass
   *          {@link Class<? extends IsIndexed>}.
   */
  private void setRemovedEntities(final CentralEntities centralEntities, final List<String> listToRemove,
    final Class<? extends IsIndexed> indexedClass) {

    if (indexedClass == IndexedAIP.class) {
      centralEntities.setAipsList(new ArrayList<>(listToRemove));
    } else if (indexedClass == IndexedDIP.class) {
      centralEntities.setDipsList(new ArrayList<>(listToRemove));
    } else {
      centralEntities.setRisksList(new ArrayList<>(listToRemove));
    }
  }

  /**
   * Read the Local Instance list of entities and checks if Central instance have
   * this entity.
   * 
   * @param centralEntities
   *          {@link CentralEntities}
   * @param readPath
   *          {@link Path}.
   * @param indexedClass
   *          {@link Class<? extends IsIndexed>}.
   */
  public void validateCentralEntities(final CentralEntities centralEntities, final Path readPath,
    final Class<? extends IsIndexed> indexedClass) {
    final IndexService index = RodaCoreFactory.getIndexService();
    final List<String> missingList = new ArrayList<>();
    String id = null;
    try {
      final JsonParser jsonParser = CentralEntitiesJsonUtils.createJsonParser(readPath);
      while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
        id = jsonParser.getText();
        final JsonToken token = jsonParser.currentToken();
        if ((id != null) && !id.isEmpty() && (token != JsonToken.START_ARRAY) && (token != JsonToken.END_ARRAY)) {
          index.retrieve(indexedClass, id, Collections.singletonList(RodaConstants.INDEX_UUID));
        }
      }
    } catch (NotFoundException | GenericException e) {
      missingList.add(id);
    } catch (final IOException e) {
      LOGGER.error("Can't read the json file {}", e.getMessage());
    }

    syncErrors += missingList.size();
    setMissingEntities(centralEntities, missingList, indexedClass);
  }

  /**
   * Sets the lists of missing entities in {@link CentralEntities} by the given
   * {@link Class<? extends IsIndexed>}.
   * 
   * @param centralEntities
   *          {@link CentralEntities}.
   * @param missingList
   *          {@link List}.
   * @param indexedClass
   *          {@link Class<? extends IsIndexed>}.
   */
  private void setMissingEntities(final CentralEntities centralEntities, final List<String> missingList,
    final Class<? extends IsIndexed> indexedClass) {

    if (indexedClass == IndexedAIP.class) {
      centralEntities.setMissingAips(new ArrayList<>(missingList));
    } else if (indexedClass == IndexedDIP.class) {
      centralEntities.setDipsList(new ArrayList<>(missingList));
    } else {
      centralEntities.setRisksList(new ArrayList<>(missingList));
    }
  }
}
