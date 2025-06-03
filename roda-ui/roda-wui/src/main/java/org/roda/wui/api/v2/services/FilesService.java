package org.roda.wui.api.v2.services;

import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.Messages;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.TechnicalMetadataNotFoundException;
import org.roda.core.data.utils.URNUtils;
import org.roda.core.data.v2.ConsumesOutputStream;
import org.roda.core.data.v2.DefaultConsumesOutputStream;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.file.CreateFolderRequest;
import org.roda.core.data.v2.file.MoveFilesRequest;
import org.roda.core.data.v2.generics.DeleteRequest;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.ip.metadata.TechnicalMetadata;
import org.roda.core.data.v2.ip.metadata.TechnicalMetadataInfo;
import org.roda.core.data.v2.ip.metadata.TechnicalMetadataInfos;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.IndexService;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.base.characterization.SiegfriedPlugin;
import org.roda.core.plugins.base.maintenance.DeleteRODAObjectPlugin;
import org.roda.core.plugins.base.maintenance.MovePlugin;
import org.roda.core.protocols.Protocol;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryConsumesOutputStream;
import org.roda.core.storage.BinaryVersion;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.RangeConsumesOutputStream;
import org.roda.core.storage.utils.RODAInstanceUtils;
import org.roda.core.util.IdUtils;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.common.HTMLUtils;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.server.ServerTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FilesService {
  private static final Logger LOGGER = LoggerFactory.getLogger(FilesService.class);
  private static final String HTML_EXT = ".html";

  public IndexedFile renameFolder(RequestContext requestContext, IndexedFile indexedFolder, String newName,
    String details) throws GenericException, RequestNotValidException, AlreadyExistsException, NotFoundException,
    AuthorizationDeniedException {
    String eventDescription = "The process of updating an object of the repository.";

    User user = requestContext.getUser();
    ModelService model = requestContext.getModelService();
    IndexService index = requestContext.getIndexService();
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

  public Job moveFiles(RequestContext requestContext, MoveFilesRequest request)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    User user = requestContext.getUser();
    IndexService indexService = requestContext.getIndexService();
    IndexedFile fileToMove = null;
    if (request.getFileUUIDtoMove() != null) {
      fileToMove = indexService.retrieve(IndexedFile.class, request.getFileUUIDtoMove(),
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

  public File createFile(RequestContext requestContext, String aipId, String representationId,
    List<String> directoryPath, String fileId, ContentPayload content, String details) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException, NotFoundException, AlreadyExistsException {
    String eventDescription = "The process of creating an object of the repository.";

    User user = requestContext.getUser();
    ModelService model = requestContext.getModelService();

    try {
      File file = model.createFile(aipId, representationId, directoryPath, fileId, content, user.getId());

      List<LinkingIdentifier> targets = new ArrayList<>();
      targets.add(PluginHelper.getLinkingIdentifier(aipId, file.getRepresentationId(), file.getPath(), file.getId(),
        RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME));

      String outcomeText = "The file '" + file.getId() + "' has been manually created.";
      model.createEvent(aipId, representationId, null, null, RodaConstants.PreservationEventType.CREATION,
        eventDescription, null, targets, PluginState.SUCCESS, outcomeText, details, user.getName(), true);

      requestContext.getIndexService().commit(IndexedFile.class);
      return file;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | AlreadyExistsException e) {
      String outcomeText = "The file '" + fileId + "' has not been manually created.";
      model.createUpdateAIPEvent(aipId, representationId, null, null, RodaConstants.PreservationEventType.CREATION,
        eventDescription, PluginState.FAILURE, outcomeText, details, user.getName(), true);

      throw e;
    }
  }

  public IndexedFile createFolder(RequestContext requestContext, IndexedRepresentation indexedRepresentation,
    CreateFolderRequest request) throws GenericException, RequestNotValidException, AlreadyExistsException,
    NotFoundException, AuthorizationDeniedException {
    String eventDescription = "The process of creating an object of the repository.";

    User user = requestContext.getUser();
    ModelService model = requestContext.getModelService();
    IndexService index = requestContext.getIndexService();
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

  public RangeConsumesOutputStream retrieveAIPRepresentationRangeStream(RequestContext requestContext,
    IndexedFile indexedFile)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    ModelService model = requestContext.getModelService();
    if (!indexedFile.isDirectory()) {
      final RangeConsumesOutputStream stream;
      DirectResourceAccess directFileAccess = model.getDirectAccess(indexedFile);
      if (indexedFile.getFileFormat() != null && StringUtils.isNotBlank(indexedFile.getFileFormat().getMimeType())) {
        stream = new RangeConsumesOutputStream(directFileAccess.getPath(), indexedFile.getFileFormat().getMimeType());
      } else {
        stream = new RangeConsumesOutputStream(directFileAccess.getPath());
      }
      return stream;
    } else {
      throw new RequestNotValidException("Range stream for directory unsupported");
    }
  }

  public StreamResponse retrieveAIPRepresentationFile(RequestContext requestContext, IndexedFile indexedFile)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    ModelService model = requestContext.getModelService();
    Optional<LiteRODAObject> liteFile = LiteRODAObjectFactory.get(File.class, indexedFile.getId());
    if (liteFile.isEmpty()) {
      throw new RequestNotValidException("Couldn't retrieve file with id: " + indexedFile.getId());
    }
    if (!indexedFile.isDirectory()) {
      final ConsumesOutputStream stream;
      Binary representationFileBinary = model.getBinary(liteFile.get());
      if (indexedFile.getFileFormat() != null && StringUtils.isNotBlank(indexedFile.getFileFormat().getMimeType())) {
        stream = new BinaryConsumesOutputStream(representationFileBinary, indexedFile.getFileFormat().getMimeType());
      } else {
        stream = new BinaryConsumesOutputStream(representationFileBinary);
      }
      return new StreamResponse(stream);
    } else {
      ConsumesOutputStream download = model.exportObjectToStream(liteFile.get());
      return new StreamResponse(download);
    }
  }

  public Optional<String> retrieveDistributedInstanceName(RequestContext requestContext, String instanceId,
    boolean isLocalInstance) {
    try {
      ModelService model = requestContext.getModelService();
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

  public StreamResponse retrieveFilePreservationHTML(RequestContext requestContext, IndexedFile file, String language)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException,
    TechnicalMetadataNotFoundException {

    final String filename;
    final ConsumesOutputStream stream;
    StreamResponse ret;
    ModelService model = requestContext.getModelService();
    Binary preservationMetadataBinary = model.retrievePreservationFile(file.getAipId(), file.getRepresentationId(),
      file.getAncestorsPath(), file.getId());
    filename = preservationMetadataBinary.getStoragePath().getName() + HTML_EXT;
    List<String> parameters = PremisV3Utils.getApplicationTechnicalMetadataParameters(model, file.getAipId(),
      file.getRepresentationId(), file.getAncestorsPath(), file.getId());
    // PremisV3Utils
    StringBuilder htmlTechnical = new StringBuilder();
    for (int i = 0; i < parameters.size(); i += 2) {
      htmlTechnical.append(HTMLUtils.technicalMetadataToHtml(preservationMetadataBinary, parameters.get(i),
        parameters.get(i + 1), ServerTools.parseLocale(language)));
    }
    stream = new DefaultConsumesOutputStream(filename, RodaConstants.MEDIA_TYPE_TEXT_HTML, out -> {
      PrintStream printStream = new PrintStream(out);
      printStream.print(htmlTechnical);
      printStream.close();
    });

    ret = new StreamResponse(stream);

    return ret;
  }

  public StreamResponse retrieveFilePreservationFile(RequestContext requestContext, IndexedFile file)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException,
    TechnicalMetadataNotFoundException {

    final ConsumesOutputStream stream;
    StreamResponse ret;
    ModelService model = requestContext.getModelService();
    Binary preservationMetadataBinary = model.retrievePreservationFile(file.getAipId(), file.getRepresentationId(),
      file.getAncestorsPath(), file.getId());
    stream = new BinaryConsumesOutputStream(preservationMetadataBinary, RodaConstants.MEDIA_TYPE_TEXT_XML);

    ret = new StreamResponse(stream);

    return ret;
  }

  public TechnicalMetadataInfos retrieveFileTechnicalMetadataInfos(RequestContext requestContext, IndexedFile file,
    String localeString)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    TechnicalMetadataInfos technicalMetadataInfos = new TechnicalMetadataInfos();

    ModelService model = requestContext.getModelService();

    Locale locale = ServerTools.parseLocale(localeString);
    Messages messages = RodaCoreFactory.getI18NMessages(locale);

    Representation representation = model.retrieveRepresentation(file.getAipId(), file.getRepresentationId());

    for (TechnicalMetadata technicalMetadata : representation.getTechnicalMetadata()) {
      String type = technicalMetadata.getType();
      String label = messages.getTranslation(
        RodaConstants.I18N_UI_BROWSE_METADATA_TECHNICAL_TYPE_PREFIX + type.toLowerCase(), technicalMetadata.getId());
      technicalMetadataInfos.addObject(new TechnicalMetadataInfo(type, label));
    }

    return technicalMetadataInfos;
  }

  public StreamResponse retrieveFileTechnicalMetadataHTML(RequestContext requestContext, IndexedFile file, String type,
    String versionID, String localeString) throws RequestNotValidException, AuthorizationDeniedException,
    NotFoundException, GenericException, TechnicalMetadataNotFoundException {
    ModelService model = requestContext.getModelService();
    Representation representation = model.retrieveRepresentation(file.getAipId(), file.getRepresentationId());
    String techMDURN = URNUtils.createRodaTechnicalMetadataURN(file.getId(),
      RODAInstanceUtils.getLocalInstanceIdentifier(), type.toLowerCase());
    Binary metadataBinary;
    if (versionID != null) {
      BinaryVersion binaryVersion = model.getBinaryVersion(representation, versionID,
        List.of(RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_TECHNICAL, type,
          techMDURN + RodaConstants.REPRESENTATION_INFORMATION_FILE_EXTENSION));
      metadataBinary = binaryVersion.getBinary();
    } else {
      metadataBinary = model.getBinary(representation, RodaConstants.STORAGE_DIRECTORY_METADATA,
        RodaConstants.STORAGE_DIRECTORY_TECHNICAL, type,
        techMDURN + RodaConstants.REPRESENTATION_INFORMATION_FILE_EXTENSION);
    }
    String filename = metadataBinary.getStoragePath().getName() + HTML_EXT;
    String htmlDescriptive = HTMLUtils.technicalMetadataToHtml(metadataBinary, type, versionID,
      ServerTools.parseLocale(localeString));

    ConsumesOutputStream stream = new DefaultConsumesOutputStream(filename, RodaConstants.MEDIA_TYPE_APPLICATION_XML,
      out -> {
        PrintStream printStream = new PrintStream(out);
        printStream.print(htmlDescriptive);
        printStream.close();
      });

    return new StreamResponse(stream);
  }

  public StreamResponse retrieveFileTechnicalMetadata(RequestContext requestContext, IndexedFile file, String type,
    String versionID)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    final ConsumesOutputStream stream;
    StreamResponse ret;
    ModelService model = requestContext.getModelService();
    Representation representation = model.retrieveRepresentation(file.getAipId(), file.getRepresentationId());
    String techMDURN = URNUtils.createRodaTechnicalMetadataURN(file.getId(),
      RODAInstanceUtils.getLocalInstanceIdentifier(), type.toLowerCase());
    Binary metadataBinary;
    if (versionID != null) {
      BinaryVersion binaryVersion = model.getBinaryVersion(representation, versionID,
        List.of(RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_TECHNICAL, type,
          techMDURN + RodaConstants.REPRESENTATION_INFORMATION_FILE_EXTENSION));
      metadataBinary = binaryVersion.getBinary();
    } else {
      metadataBinary = model.getBinary(representation, RodaConstants.STORAGE_DIRECTORY_METADATA,
        RodaConstants.STORAGE_DIRECTORY_TECHNICAL, type,
        techMDURN + RodaConstants.REPRESENTATION_INFORMATION_FILE_EXTENSION);
    }
    stream = new BinaryConsumesOutputStream(metadataBinary, RodaConstants.MEDIA_TYPE_TEXT_XML);

    ret = new StreamResponse(stream);

    return ret;
  }

}
