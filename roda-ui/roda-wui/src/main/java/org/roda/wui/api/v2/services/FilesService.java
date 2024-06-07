package org.roda.wui.api.v2.services;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.DownloadUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ConsumesOutputStream;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.file.CreateFolderRequest;
import org.roda.core.data.v2.file.MoveFilesRequest;
import org.roda.core.data.v2.generics.DeleteRequest;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.base.characterization.SiegfriedPlugin;
import org.roda.core.plugins.base.maintenance.DeleteRODAObjectPlugin;
import org.roda.core.plugins.base.maintenance.MovePlugin;
import org.roda.core.protocols.Protocol;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryConsumesOutputStream;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.Directory;
import org.roda.core.storage.StorageService;
import org.roda.core.util.IdUtils;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FilesService {
  private static final Logger LOGGER = LoggerFactory.getLogger(FilesService.class);

  public IndexedFile renameFolder(User user, IndexedFile indexedFolder, String newName, String details)
    throws GenericException, RequestNotValidException, AlreadyExistsException, NotFoundException,
    AuthorizationDeniedException {
    String eventDescription = "The process of updating an object of the repository.";

    ModelService model = RodaCoreFactory.getModelService();
    IndexService index = RodaCoreFactory.getIndexService();
    String oldName = indexedFolder.getId();

    try {
      File folder = model.retrieveFile(indexedFolder.getAipId(), indexedFolder.getRepresentationId(),
        indexedFolder.getPath(), indexedFolder.getId());
      File newFolder = model.renameFolder(folder, newName, true);
      String outcomeText = "The folder '" + oldName + "' has been manually renamed to '" + newName + "'.";
      model.createUpdateAIPEvent(indexedFolder.getAipId(), indexedFolder.getRepresentationId(), null, null,
        RodaConstants.PreservationEventType.UPDATE, eventDescription, PluginState.SUCCESS, outcomeText, details,
        user.getName(), true);

      index.commitAIPs();
      return index.retrieve(IndexedFile.class, IdUtils.getFileId(newFolder), RodaConstants.FILE_FIELDS_TO_RETURN);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      String outcomeText = "The folder '" + oldName + "' has not been manually renamed to '" + newName + "'.";

      model.createUpdateAIPEvent(indexedFolder.getAipId(), indexedFolder.getRepresentationId(), null, null,
        RodaConstants.PreservationEventType.UPDATE, eventDescription, PluginState.FAILURE, outcomeText, details,
        user.getName(), true);

      throw e;
    }
  }

  public Job createFormatIdentificationJob(User user, SelectedItems<?> selected)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    return CommonServicesUtils.createAndExecuteJob("Format identification using Siegfried", selected,
      SiegfriedPlugin.class, PluginType.MISC, user, Collections.emptyMap(),
      "Could not execute format identification using Siegfrid action");
  }

  public Job deleteFiles(User user, DeleteRequest request)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, request.getDetails());
    return CommonServicesUtils.createAndExecuteInternalJob("Delete files",
      CommonServicesUtils.convertSelectedItems(request.getItemsToDelete(), IndexedFile.class),
      DeleteRODAObjectPlugin.class, user, pluginParameters, "Could not execute file delete action");
  }

  public Job moveFiles(User user, MoveFilesRequest request)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    IndexedFile fileToMove = null;
    if (request.getFileUUIDtoMove() != null) {
      fileToMove = RodaCoreFactory.getIndexService().retrieve(IndexedFile.class, request.getFileUUIDtoMove(),
        RodaConstants.FILE_FIELDS_TO_RETURN);
    }

    if (fileToMove != null && (!fileToMove.getAipId().equals(request.getAipId())
      || !fileToMove.getRepresentationId().equals(request.getRepresentationId()))) {
      throw new RequestNotValidException("Cannot move to a file outside defined representation");
    }

    Map<String, String> pluginParameters = new HashMap<>();
    if (fileToMove != null) {
      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_ID, fileToMove.getUUID());
    }
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, request.getDetails());

    return CommonServicesUtils.createAndExecuteInternalJob("Move files", request.getItemsToMove(), MovePlugin.class,
      user, pluginParameters, "Could not execute move job");
  }

  public File createFile(User user, String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload content, String details) throws GenericException, AuthorizationDeniedException,
    RequestNotValidException, NotFoundException, AlreadyExistsException {
    String eventDescription = "The process of creating an object of the repository.";

    ModelService model = RodaCoreFactory.getModelService();

    try {
      File file = model.createFile(aipId, representationId, directoryPath, fileId, content, user.getId());

      List<LinkingIdentifier> targets = new ArrayList<>();
      targets.add(PluginHelper.getLinkingIdentifier(aipId, file.getRepresentationId(), file.getPath(), file.getId(),
        RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME));

      String outcomeText = "The file '" + file.getId() + "' has been manually created.";
      model.createEvent(aipId, representationId, null, null, RodaConstants.PreservationEventType.CREATION,
        eventDescription, null, targets, PluginState.SUCCESS, outcomeText, details, user.getName(), true);

      RodaCoreFactory.getIndexService().commit(IndexedFile.class);

      return file;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | AlreadyExistsException e) {
      String outcomeText = "The file '" + fileId + "' has not been manually created.";
      model.createUpdateAIPEvent(aipId, representationId, null, null, RodaConstants.PreservationEventType.CREATION,
        eventDescription, PluginState.FAILURE, outcomeText, details, user.getName(), true);

      throw e;
    }
  }

  public IndexedFile createFolder(User user, IndexedRepresentation indexedRepresentation, CreateFolderRequest request)
    throws GenericException, RequestNotValidException, AlreadyExistsException, NotFoundException,
    AuthorizationDeniedException {
    String eventDescription = "The process of creating an object of the repository.";

    ModelService model = RodaCoreFactory.getModelService();
    IndexService index = RodaCoreFactory.getIndexService();
    File newFolder;

    String folderUUID = request.getFolderUUID();
    String folderName = request.getName();
    String details = request.getDetails();

    try {
      if (folderUUID != null) {
        IndexedFile indexedFile = index.retrieve(IndexedFile.class, folderUUID, RodaConstants.FILE_FIELDS_TO_RETURN);
        newFolder = model.createFile(indexedFile.getAipId(), indexedFile.getRepresentationId(), indexedFile.getPath(),
          indexedFile.getId(), folderName, user.getId(), true);
      } else {
        newFolder = model.createFile(indexedRepresentation.getAipId(), indexedRepresentation.getId(), null, null,
          folderName, user.getId(), true);
      }

      String outcomeText = "The folder '" + folderName + "' has been manually created.";
      model.createUpdateAIPEvent(indexedRepresentation.getAipId(), indexedRepresentation.getId(), null, null,
        RodaConstants.PreservationEventType.CREATION, eventDescription, PluginState.SUCCESS, outcomeText, details,
        user.getName(), true);

      index.commit(IndexedFile.class);
      return index.retrieve(IndexedFile.class, IdUtils.getFileId(newFolder), new ArrayList<>());
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      String outcomeText = "The folder '" + folderName + "' has not been manually created.";
      model.createUpdateAIPEvent(indexedRepresentation.getAipId(), indexedRepresentation.getId(), null, null,
        RodaConstants.PreservationEventType.CREATION, eventDescription, PluginState.FAILURE, outcomeText, details,
        user.getName(), true);

      throw e;
    }
  }

  public StreamResponse retrieveAIPRepresentationFile(IndexedFile indexedFile)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    StoragePath filePath = ModelUtils.getFileStoragePath(indexedFile.getAipId(), indexedFile.getRepresentationId(),
      indexedFile.getPath(), indexedFile.getId());

    if (!indexedFile.isDirectory()) {
      final ConsumesOutputStream stream;
      StorageService storage = RodaCoreFactory.getStorageService();
      Binary representationFileBinary = storage.getBinary(filePath);
      if (indexedFile.getFileFormat() != null && StringUtils.isNotBlank(indexedFile.getFileFormat().getMimeType())) {
        stream = new BinaryConsumesOutputStream(representationFileBinary, indexedFile.getFileFormat().getMimeType());
      } else {
        stream = new BinaryConsumesOutputStream(representationFileBinary);
      }
      return new StreamResponse(stream);
    } else {
      Directory directory = RodaCoreFactory.getStorageService().getDirectory(filePath);
      ConsumesOutputStream download = DownloadUtils.download(RodaCoreFactory.getStorageService(), directory, null);
      return new StreamResponse(download);
    }
  }

  public Optional<String> retrieveDistributedInstanceName(String instanceId, boolean isLocalInstance) {
    try {
      ModelService model = RodaCoreFactory.getModelService();
      RodaConstants.DistributedModeType distributedModeType = RodaCoreFactory.getDistributedModeType();

      if (RodaConstants.DistributedModeType.CENTRAL.equals(distributedModeType)) {
        if (isLocalInstance) {
          return Optional.of(RodaCoreFactory.getProperty(RodaConstants.CENTRAL_INSTANCE_NAME_PROPERTY,
            RodaConstants.DEFAULT_CENTRAL_INSTANCE_NAME));
        } else {
          DistributedInstance distributedInstance = model.retrieveDistributedInstance(instanceId);
          return Optional.of(distributedInstance.getName());
        }
      }
    } catch (GenericException | AuthorizationDeniedException | RequestNotValidException | NotFoundException e) {
      LOGGER.warn("Could not retrieve the distributed instance", e);
      return Optional.empty();
    }

    return Optional.empty();
  }

  public boolean isShallowFileAvailable(IndexedFile indexedFile) {
    try {
      if (indexedFile.isReference()) {
        String referenceURL = indexedFile.getReferenceURL();
        final Protocol protocol = RodaCoreFactory.getProtocol(new URI(referenceURL));
        return protocol.isAvailable();
      }
    } catch (URISyntaxException e) {
      LOGGER.warn("Cannot convert referenceURL to URI: {}", indexedFile.getUUID());
    } catch (GenericException e) {
      LOGGER.warn("File is not available: {}", indexedFile.getUUID());
    }
    return false;
  }

  public List<String> getConfigurationFileRules(User user) {
    if (UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_FIND_REPRESENTATION_INFORMATION)) {
      return RodaCoreFactory.getRodaConfigurationAsList("ui.ri.rule.File").stream()
        .map(r -> RodaCoreFactory.getRodaConfigurationAsString(r, RodaConstants.SEARCH_FIELD_FIELDS)).toList();
    } else {
      return Collections.emptyList();
    }
  }
}
