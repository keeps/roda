/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.synchronization;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.SyncUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.IndexedJob;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.synchronization.EntitySummary;
import org.roda.core.data.v2.synchronization.RODAInstance;
import org.roda.core.data.v2.synchronization.bundle.AttachmentState;
import org.roda.core.data.v2.synchronization.bundle.Issue;
import org.roda.core.data.v2.synchronization.bundle.RemovedEntity;
import org.roda.core.data.v2.synchronization.bundle.v2.BundleManifest;
import org.roda.core.data.v2.synchronization.bundle.v2.PackageState;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.model.utils.ResourceParseUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.base.maintenance.DeleteRodaObjectPluginUtils;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.orchestrate.JobsHelper;
import org.roda.core.storage.Container;
import org.roda.core.storage.Resource;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class ImportUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImportUtils.class);

  private ImportUtils() {
    // do nothing
  }

  public static int importStorage(final ModelService model, final IndexService index,
    final Path workingDir, final boolean importJobs) throws GenericException, NotFoundException,
    AuthorizationDeniedException, AlreadyExistsException, RequestNotValidException {
    FileStorageService tmpStorage = new FileStorageService(workingDir.resolve(RodaConstants.CORE_STORAGE_FOLDER), false,
      null, false);

    int total = 0;

    for (Container container : tmpStorage.listContainers()) {
      // Let local instance handle job creation
      if (!RodaConstants.STORAGE_CONTAINER_JOB.equals(container.getStoragePath().getName()) || importJobs) {
        for (Resource resource : tmpStorage.listResourcesUnderContainer(container.getStoragePath(), false)) {
          StoragePath storagePath = resource.getStoragePath();
          if (RodaConstants.STORAGE_CONTAINER_PRESERVATION.equals(container.getStoragePath().getName())
            || RodaConstants.STORAGE_CONTAINER_JOB_REPORT.equals(container.getStoragePath().getName())) {
            CloseableIterable<Resource> resources = tmpStorage.listResourcesUnderDirectory(resource.getStoragePath(),
              true);
            for (Resource pmResource : resources) {
              StoragePath pmStoragePath = pmResource.getStoragePath();
              importResource(model, index, tmpStorage, pmResource, pmStoragePath);
            }
          } else {
            importResource(model, index, tmpStorage, resource, storagePath);
          }
          total++;
        }
      }
    }
    return total;
  }

  private static void importResource(ModelService model, IndexService index,
    FileStorageService tmpStorage, Resource resource, StoragePath storagePath) throws NotFoundException,
    GenericException, AuthorizationDeniedException, AlreadyExistsException, RequestNotValidException {

    // if the resource already exists, remove it before moving the updated resource
    if (model.exists(storagePath)) {
      model.deleteResource(storagePath);
    }
    model.copy(tmpStorage, storagePath, storagePath);
    reindexResource(model, index, resource);
  }

  private static void reindexResource(ModelService model, IndexService index, Resource resource)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    String containerName = resource.getStoragePath().getContainerName();
    Class<IsRODAObject> objectClass = ModelUtils.giveRespectiveModelClassFromContainerName(containerName);

    if (Report.class.equals(objectClass) || PreservationMetadata.class.equals(objectClass)) {
      if (resource.isDirectory()) {
        return;
      }
    }

    OptionalWithCause<LiteRODAObject> liteRODAObject = ResourceParseUtils.convertResourceToLite(resource, objectClass);
    if (liteRODAObject.isPresent()) {
      OptionalWithCause<IsModelObject> rodaObject = model.retrieveObjectFromLite(liteRODAObject.get());
      if (rodaObject.isPresent()) {
        if (PreservationMetadata.class.equals(objectClass)) {
          model.notifyPreservationMetadataCreated((PreservationMetadata) rodaObject.get()).failOnError();
        } else if (Report.class.equals(objectClass)) {
          String jobId = ModelUtils.getJobAndReportIds(resource.getStoragePath()).get(0);
          IndexedJob job = index.retrieve(IndexedJob.class, jobId, Arrays.asList());
          model.notifyJobReportCreatedOrUpdated((Report) rodaObject.get(), job);
        } else {
          clearSpecificIndexes(index, objectClass, rodaObject.get());
          index.reindex(rodaObject.get());
        }
      }
    }
  }

  private static void clearSpecificIndexes(IndexService index, Class<IsRODAObject> objectClass,
    IsModelObject rodaObject) throws AuthorizationDeniedException {
    if (AIP.class.equals(objectClass)) {
      List<String> ids = Arrays.asList(rodaObject.getId());
      index.delete(IndexedRepresentation.class,
        new Filter(new OneOfManyFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, ids)));
      index.delete(IndexedFile.class, new Filter(new OneOfManyFilterParameter(RodaConstants.FILE_AIP_ID, ids)));
      index.delete(IndexedPreservationEvent.class,
        new Filter(new OneOfManyFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, ids)));
    }
  }

  public static void importAttachments(Path workingDir, BundleManifest manifest) throws GenericException {
    for (AttachmentState attachmentState : manifest.getAttachmentStateList()) {
      for (String attachmentId : attachmentState.getAttachmentIdList()) {
        try {
          String fileName = Paths.get(attachmentId).toAbsolutePath().getParent().toUri()
            .relativize(Paths.get(attachmentId).toAbsolutePath().toUri()).toString();
          Path sourcePath = workingDir.resolve(RodaConstants.CORE_JOB_ATTACHMENTS_FOLDER)
            .resolve(attachmentState.getJobId()).resolve(fileName);
          Path targetPath = RodaCoreFactory.getJobAttachmentsDirectoryPath().resolve(attachmentState.getJobId())
            .resolve(attachmentId);
          FSUtils.copy(sourcePath, targetPath, true);
        } catch (AlreadyExistsException e) {
          // do nothing
        }
      }
    }
  }

  public static int deleteBundleEntities(final ModelService model, final IndexService index, final Job cachedJob,
    Plugin<? extends IsRODAObject> plugin, final JobPluginInfo jobPluginInfo, final RODAInstance rodaInstance,
    final Path bundleWorkingDir, final List<PackageState> validationEntityList, final Report reportItem) {
    Path temporaryReportPath = null;
    int removed = 0;
    final List<Path> reportPaths = new ArrayList<>();
    for (PackageState packageState : validationEntityList) {
      Class<? extends IsIndexed> indexedClass = (Class<? extends IsIndexed>) ModelUtils
        .giveRespectiveIndexedClass(packageState.getClassName());
      final StringBuilder fileNameBuilder = new StringBuilder();
      final String[] className = packageState.getClassName().getName().split("\\.");
      fileNameBuilder.append(RodaConstants.SYNCHRONIZATION_REMOVED_FILE).append("_").append(rodaInstance.getId())
        .append("_");
      fileNameBuilder.append(className[className.length - 1]).append(".jsonl");
      temporaryReportPath = bundleWorkingDir.resolve(fileNameBuilder.toString());
      if (!Files.exists(temporaryReportPath)) {
        createReport(temporaryReportPath);
        reportPaths.add(temporaryReportPath);
      }
      final Path listPath = bundleWorkingDir.resolve(packageState.getFilePath());
      removed += deleteRodaObject(model, index, cachedJob, jobPluginInfo, plugin, listPath, indexedClass, rodaInstance,
        temporaryReportPath, reportItem);
    }
    if (!reportPaths.isEmpty()) {
      for (final Path reportPath : reportPaths) {
        moveReportToSynchronizationFolder(reportPath);
      }
    }
    return removed;
  }

  private static int deleteRodaObject(final ModelService model, final IndexService index, final Job cachedJob,
    final JobPluginInfo jobPluginInfo, Plugin<? extends IsRODAObject> plugin, final Path readPath,
    final Class<? extends IsIndexed> indexedClass, final RODAInstance rodaInstance, final Path temporaryReportPath,
    final Report report) {
    final List<String> listToRemove = getListToRemove(index, readPath, indexedClass,
      rodaInstance instanceof DistributedInstance ? Optional.of(rodaInstance.getId()) : Optional.empty());
    int removed = 0;
    if (!listToRemove.isEmpty()) {
      try {
        final List<? extends IsRODAObject> objectsToRemove = JobsHelper.getObjectsFromUUID(model, index,
          ModelUtils.giveRespectiveModelClass(indexedClass), listToRemove);
        int sourceObjects = jobPluginInfo.getSourceObjectsCount() + listToRemove.size();
        jobPluginInfo.setSourceObjectsCount(sourceObjects);
        for (IsRODAObject object : objectsToRemove) {
          final String id = object.getId();
          DeleteRodaObjectPluginUtils.process(index, model, report, cachedJob, jobPluginInfo, plugin, object, "", true,
            false);
          final RemovedEntity removedEntity = new RemovedEntity(id, indexedClass.getName());
          writeJsonLinesReport(temporaryReportPath, removedEntity);
          rodaInstance.incrementEntityCounters(RodaConstants.SYNCHRONIZATION_ENTITY_SUMMARY_TYPE_REMOVED,
            ModelUtils.giveRespectiveModelClass(indexedClass).getName());
        }
        removed = listToRemove.size();
      } catch (RequestNotValidException | NotFoundException | GenericException e) {
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        report.addPluginDetails(e.getMessage());
        report.setPluginState(PluginState.FAILURE);
      }
    }
    return removed;
  }

  private static List<String> getListToRemove(final IndexService index, final Path readPath,
    final Class<? extends IsIndexed> indexedClass, final Optional<String> instanceIdentifier) {
    final List<String> listToRemove = new ArrayList<>();

    // Create Filter to SOLR query
    final Filter filter = new Filter();
    instanceIdentifier.ifPresent(s -> filter.add(new SimpleFilterParameter(RodaConstants.INDEX_INSTANCE_ID, s)));
    if (indexedClass == IndexedPreservationEvent.class) {
      filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_OBJECT_CLASS,
        IndexedPreservationEvent.PreservationMetadataEventClass.REPOSITORY.toString()));
    }

    // Iterate over SOLR query result and the JSON file in bundle
    try (IterableIndexResult<? extends IsIndexed> result = index.findAll(indexedClass, filter, true,
      Collections.singletonList(RodaConstants.INDEX_UUID))) {
      result.forEach(indexed -> {
        boolean exist = false;
        try {
          final JsonParser jsonParser = SyncUtils.createJsonParser(readPath);
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
        } catch (final IOException e) {
          LOGGER.error("Can't read the json file {}", e.getMessage());
        }
      });

    } catch (IOException | GenericException | RequestNotValidException e) {
      LOGGER.error("Error getting AIP iterator when creating aip list", e);
    }
    return listToRemove;
  }

  // Validate Roda Central Entities

  /**
   * Creates and Iterates through the {@link Map} with the class and the path to
   * the entities.
   *
   * @param index
   *          {@link IndexService}
   * @param bundleWorkingDir
   *          {@link Path}
   * @param validationEntityList
   *          {@link List}
   * @param distributedInstance
   *          {@link DistributedInstance}
   * @param syncErrors
   *          number of errors in synchronization
   */
  public static void validateEntitiesBundle(final IndexService index, final Path bundleWorkingDir,
    final List<PackageState> validationEntityList, final DistributedInstance distributedInstance, int syncErrors)
    throws GenericException {

    Path temporaryReportPath = null;
    final List<Path> reportPaths = new ArrayList<>();
    for (PackageState packageState : validationEntityList) {
      Class<? extends IsIndexed> indexedClass = (Class<? extends IsIndexed>) ModelUtils
        .giveRespectiveIndexedClass(packageState.getClassName());
      final StringBuilder fileNameBuilder = new StringBuilder();
      fileNameBuilder.append(RodaConstants.SYNCHRONIZATION_ISSUES_FILE).append("_").append(distributedInstance.getId())
        .append("_");
      final String[] className = ModelUtils.giveRespectiveModelClass(indexedClass).getName().split("\\.");
      fileNameBuilder.append(className[className.length - 1]).append(".jsonl");
      temporaryReportPath = bundleWorkingDir.resolve(fileNameBuilder.toString());
      if (!Files.exists(temporaryReportPath)) {
        createReport(temporaryReportPath);
        reportPaths.add(temporaryReportPath);
      }
      syncErrors += validateCentralEntities(index, temporaryReportPath,
        bundleWorkingDir.resolve(packageState.getFilePath()), indexedClass, distributedInstance);
    }
    if (temporaryReportPath != null) {
      for (final Path reportPath : reportPaths) {
        moveReportToSynchronizationFolder(reportPath);
      }
    }
  }

  /**
   * Read the Local Instance list of entities and checks if Central instance have
   * this entity.
   *
   * @param temporaryReportPath
   *          {@link Path}
   * @param readPath
   *          {@link Path}.
   * @param indexedClass
   *          {@link Class<? extends IsIndexed>}.
   */
  private static int validateCentralEntities(final IndexService index, final Path temporaryReportPath,
    final Path readPath, final Class<? extends IsIndexed> indexedClass, final DistributedInstance distributedInstance)
    throws GenericException {
    String id = null;
    int errors = 0;
    try {
      final JsonParser jsonParser = SyncUtils.createJsonParser(readPath);
      while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
        id = jsonParser.getText();
        final JsonToken token = jsonParser.currentToken();
        if ((id != null) && !id.isEmpty() && (token != JsonToken.START_ARRAY) && (token != JsonToken.END_ARRAY)) {
          index.retrieve(indexedClass, id, Collections.singletonList(RodaConstants.INDEX_UUID));
        }
      }
    } catch (NotFoundException | GenericException e) {
      final Issue issue = new Issue(id, RodaConstants.SYNCHRONIZATION_ISSUE_TYPE_MISSING,
        ModelUtils.giveRespectiveModelClass(indexedClass).getName());
      writeJsonLinesReport(temporaryReportPath, issue);
      errors++;
      distributedInstance.incrementEntityCounters(RodaConstants.SYNCHRONIZATION_ENTITY_SUMMARY_TYPE_ISSUE,
        ModelUtils.giveRespectiveModelClass(indexedClass).getName());
    } catch (final IOException e) {
      LOGGER.error("Can't read the json file {}", e.getMessage());
    }

    return errors;
  }

  private static void createReport(final Path temporaryReportPath) {
    try {
      Files.deleteIfExists(temporaryReportPath);
      Files.createFile(temporaryReportPath);
    } catch (final IOException e) {
      LOGGER.error("Can't create Report file {} because {}", temporaryReportPath, e.getMessage());
    }
  }

  private static void moveReportToSynchronizationFolder(final Path temporaryReportPath) {
    final Path reportPath = RodaCoreFactory.getSynchronizationDirectoryPath()
      .resolve(temporaryReportPath.getFileName().toString());
    try {
      Files.move(temporaryReportPath, reportPath, StandardCopyOption.REPLACE_EXISTING);
    } catch (final IOException e) {
      LOGGER.error("Can't move Report file to {} because {}", reportPath, e.getMessage());
    }
  }

  private static void writeJsonLinesReport(final Path path, final Object object) throws GenericException {
    try {
      JsonUtils.appendObjectToFile(object, path);
    } catch (final GenericException e) {
      throw new GenericException("Can't write the object in file" + path, e);
    }
  }

  public static void createLastSyncFile(final Path bundleWorkingDir, final RODAInstance rodaInstance,
    final String jobID, final String bundleId) throws IOException {
    final StringBuilder fileNameBuilder = new StringBuilder();
    fileNameBuilder.append(RodaConstants.SYNCHRONIZATION_REPORT_FILE).append("_").append(rodaInstance.getId())
      .append(".json");
    final Path temporaryReportPath = bundleWorkingDir.resolve(fileNameBuilder.toString());
    createReport(temporaryReportPath);
    OutputStream outputStream = null;
    JsonGenerator jsonGenerator = null;
    try {
      if (temporaryReportPath != null) {
        outputStream = new BufferedOutputStream(new FileOutputStream(temporaryReportPath.toFile()));
        final JsonFactory jsonFactory = new JsonFactory();
        jsonGenerator = jsonFactory.createGenerator(outputStream, JsonEncoding.UTF8).useDefaultPrettyPrinter();
      }

      writeLastSyncFile(jsonGenerator, rodaInstance, jobID, bundleId);
    } catch (final IOException e) {
      LOGGER.error("Can't create report with the summary of synchronization {}", e.getMessage());
    } finally {
      if (jsonGenerator != null) {
        jsonGenerator.close();
      }
      if (outputStream != null) {
        outputStream.close();
      }
    }
    moveReportToSynchronizationFolder(temporaryReportPath);
  }

  private static void writeLastSyncFile(final JsonGenerator jsonGenerator, final RODAInstance rodaInstance,
    final String jobID, final String bundleId) {

    try {
      jsonGenerator.writeStartObject();
      jsonGenerator.writeStringField(RodaConstants.SYNCHRONIZATION_REPORT_KEY_UUID, bundleId);
      jsonGenerator.writeStringField(RodaConstants.SYNCHRONIZATION_REPORT_KEY_INSTANCE_ID, rodaInstance.getId());
      jsonGenerator.writeStringField(RodaConstants.SYNCHRONIZATION_REPORT_KEY_FROM_DATE,
        rodaInstance.getLastSynchronizationDate().toString());
      if (rodaInstance.getSyncErrors() > 0) {
        jsonGenerator.writeStringField(RodaConstants.SYNCHRONIZATION_REPORT_KEY_STATUS,
          RodaConstants.SYNCHRONIZATION_REPORT_VALUE_STATUS_ERROR);
      } else {
        jsonGenerator.writeStringField(RodaConstants.SYNCHRONIZATION_REPORT_KEY_STATUS,
          RodaConstants.SYNCHRONIZATION_REPORT_VALUE_STATUS_SUCCESS);
      }
      jsonGenerator.writeStringField(RodaConstants.SYNCHRONIZATION_REPORT_KEY_JOB, jobID);

      jsonGenerator.writeFieldName(RodaConstants.SYNCHRONIZATION_REPORT_KEY_SUMMARY);
      writeSummaryLists(jsonGenerator, rodaInstance.getEntitySummaryList());

      jsonGenerator.writeEndObject();
    } catch (final IOException e) {
      LOGGER.error("Can't write report with the summary of synchronization {}", e.getMessage());
    }

  }

  private static void writeSummaryLists(final JsonGenerator jsonGenerator, final List<EntitySummary> entitySummaries)
    throws IOException {
    jsonGenerator.writeStartArray();
    for (EntitySummary entitySummary : entitySummaries) {
      jsonGenerator.writeStartObject();
      jsonGenerator.writeStringField(RodaConstants.SYNCHRONIZATION_REPORT_KEY_ENTITY_CLASS,
        entitySummary.getEntityClass());
      jsonGenerator.writeNumberField(RodaConstants.SYNCHRONIZATION_REPORT_KEY_UPDATED_AND_ADDED,
        entitySummary.getCountAddedUpdated());
      jsonGenerator.writeNumberField(RodaConstants.SYNCHRONIZATION_REPORT_KEY_REMOVED, entitySummary.getCountRemoved());
      jsonGenerator.writeNumberField(RodaConstants.SYNCHRONIZATION_REPORT_KEY_ISSUES, entitySummary.getCountIssues());
      jsonGenerator.writeEndObject();
    }
    jsonGenerator.writeEndArray();
  }

  public static void updateEntityCounter(final BundleManifest bundleState, RODAInstance rodaInstance) {
    final List<PackageState> packageStateList = bundleState.getPackageStateList();

    packageStateList.stream().forEach(packageState -> {
      rodaInstance.sumValueToEntitiesCounter(RodaConstants.SYNCHRONIZATION_ENTITY_SUMMARY_TYPE_UPDATED,
        ModelUtils.giveRespectiveModelClass(packageState.getClassName()).getName(), packageState.getCount());
    });
  }
}
