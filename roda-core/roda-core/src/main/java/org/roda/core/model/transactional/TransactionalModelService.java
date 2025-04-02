package org.roda.core.model.transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.roda.core.common.ReturnWithExceptionsWrapper;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.notifications.NotificationProcessor;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.SecureString;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.EmailAlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.InvalidTokenException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.NotImplementedException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.UserAlreadyExistsException;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.accessKey.AccessKey;
import org.roda.core.data.v2.accessKey.AccessKeys;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmationAIPEntry;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.hold.DisposalHolds;
import org.roda.core.data.v2.disposal.metadata.DisposalAIPMetadata;
import org.roda.core.data.v2.disposal.metadata.DisposalHoldsAIPMetadata;
import org.roda.core.data.v2.disposal.metadata.DisposalTransitiveHoldsAIPMetadata;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.disposal.rule.DisposalRules;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedules;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.IndexedJob;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.central.DistributedInstances;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.events.EventsManager;
import org.roda.core.model.DefaultModelService;
import org.roda.core.model.ModelObserver;
import org.roda.core.model.ModelService;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryVersion;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.Directory;
import org.roda.core.storage.StorageService;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class TransactionalModelService implements ModelService {
  private final ModelService defaultModelService;

  public TransactionalModelService(StorageService storage, EventsManager eventsManager, RodaConstants.NodeType nodeType,
    String instanceId) {
    defaultModelService = new DefaultModelService(storage, eventsManager, nodeType, instanceId);
  }

  private ModelService getModelService() {
    return defaultModelService;
  }

  public void rollback() throws NotImplementedException {
    throw new NotImplementedException("rollback method not implemented yet");
  }

  public void commit() throws NotImplementedException {
    throw new NotImplementedException("rollback method not implemented yet");
  }

  @Override
  public StorageService getStorage() {
    return getModelService().getStorage();
  }

  @Override
  public CloseableIterable<OptionalWithCause<AIP>> listAIPs()
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().listAIPs();
  }

  @Override
  public AIP retrieveAIP(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return getModelService().retrieveAIP(aipId);
  }

  @Override
  public AIP createAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath, boolean notify,
    String createdBy) throws RequestNotValidException, GenericException, AuthorizationDeniedException,
    AlreadyExistsException, NotFoundException, ValidationException {
    return getModelService().createAIP(aipId, sourceStorage, sourcePath, notify, createdBy);
  }

  @Override
  public AIP createAIP(String parentId, String type, Permissions permissions, List<String> ingestSIPIds,
    String ingestJobId, boolean notify, String createdBy, boolean isGhost) throws RequestNotValidException,
    NotFoundException, GenericException, AlreadyExistsException, AuthorizationDeniedException {
    return getModelService().createAIP(parentId, type, permissions, ingestSIPIds, ingestJobId, notify, createdBy,
      isGhost);
  }

  @Override
  public AIP createAIP(String parentId, String type, Permissions permissions, String createdBy)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {
    return getModelService().createAIP(parentId, type, permissions, createdBy);
  }

  @Override
  public AIP createAIP(AIPState state, String parentId, String type, Permissions permissions, String createdBy)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {
    return getModelService().createAIP(state, parentId, type, permissions, createdBy);
  }

  @Override
  public AIP createAIP(AIPState state, String parentId, String type, Permissions permissions, boolean notify,
    String createdBy) throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {
    return getModelService().createAIP(state, parentId, type, permissions, notify, createdBy);
  }

  @Override
  public AIP createAIP(AIPState state, String parentId, String type, Permissions permissions, String ingestSIPUUID,
    List<String> ingestSIPIds, String ingestJobId, boolean notify, String createdBy) throws RequestNotValidException,
    NotFoundException, GenericException, AlreadyExistsException, AuthorizationDeniedException {
    return getModelService().createAIP(state, parentId, type, permissions, ingestSIPUUID, ingestSIPIds, ingestJobId,
      notify, createdBy);
  }

  @Override
  public AIP createAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath, String createdBy)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, AlreadyExistsException,
    NotFoundException, ValidationException {
    return getModelService().createAIP(aipId, sourceStorage, sourcePath, createdBy);
  }

  @Override
  public AIP notifyAipCreated(String aipId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().notifyAipCreated(aipId);
  }

  @Override
  public AIP notifyAipUpdated(String aipId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().notifyAipUpdated(aipId);
  }

  @Override
  public AIP updateAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath, String updatedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException,
    AlreadyExistsException, ValidationException {
    return getModelService().updateAIP(aipId, sourceStorage, sourcePath, updatedBy);
  }

  @Override
  public AIP destroyAIP(AIP aip, String updatedBy)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    return getModelService().destroyAIP(aip, updatedBy);
  }

  @Override
  public AIP updateAIP(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    return getModelService().updateAIP(aip, updatedBy);
  }

  @Override
  public AIP updateAIPState(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    return getModelService().updateAIPState(aip, updatedBy);
  }

  @Override
  public AIP updateAIPInstanceId(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    return getModelService().updateAIPInstanceId(aip, updatedBy);
  }

  @Override
  public AIP moveAIP(String aipId, String parentId, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    return getModelService().moveAIP(aipId, parentId, updatedBy);
  }

  @Override
  public void deleteAIP(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    getModelService().deleteAIP(aipId);
  }

  @Override
  public void changeAIPType(String aipId, String type, String updatedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    getModelService().changeAIPType(aipId, type, updatedBy);
  }

  @Override
  public Binary retrieveDescriptiveMetadataBinary(String aipId, String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrieveDescriptiveMetadataBinary(aipId, descriptiveMetadataId);
  }

  @Override
  public Binary retrieveDescriptiveMetadataBinary(String aipId, String representationId, String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrieveDescriptiveMetadataBinary(aipId, representationId, descriptiveMetadataId);
  }

  @Override
  public DescriptiveMetadata retrieveDescriptiveMetadata(String aipId, String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrieveDescriptiveMetadata(aipId, descriptiveMetadataId);
  }

  @Override
  public DescriptiveMetadata retrieveDescriptiveMetadata(String aipId, String representationId,
    String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrieveDescriptiveMetadata(aipId, representationId, descriptiveMetadataId);
  }

  @Override
  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String descriptiveMetadataId,
    ContentPayload payload, String descriptiveMetadataType, String descriptiveMetadataVersion, String createdBy,
    boolean notify) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {
    return getModelService().createDescriptiveMetadata(aipId, descriptiveMetadataId, payload, descriptiveMetadataType,
      descriptiveMetadataVersion, createdBy, notify);
  }

  @Override
  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String descriptiveMetadataId,
    ContentPayload payload, String descriptiveMetadataType, String descriptiveMetadataVersion, String createdBy)
    throws RequestNotValidException, GenericException, AlreadyExistsException, AuthorizationDeniedException,
    NotFoundException {
    return getModelService().createDescriptiveMetadata(aipId, descriptiveMetadataId, payload, descriptiveMetadataType,
      descriptiveMetadataVersion, createdBy);
  }

  @Override
  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String representationId,
    String descriptiveMetadataId, ContentPayload payload, String descriptiveMetadataType,
    String descriptiveMetadataVersion, String createdBy) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    return getModelService().createDescriptiveMetadata(aipId, representationId, descriptiveMetadataId, payload,
      descriptiveMetadataType, descriptiveMetadataVersion, createdBy);
  }

  @Override
  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String representationId,
    String descriptiveMetadataId, ContentPayload payload, String descriptiveMetadataType,
    String descriptiveMetadataVersion, String createdBy, boolean notify) throws RequestNotValidException,
    GenericException, AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    return getModelService().createDescriptiveMetadata(aipId, representationId, descriptiveMetadataId, payload,
      descriptiveMetadataType, descriptiveMetadataVersion, createdBy, notify);
  }

  @Override
  public DescriptiveMetadata updateDescriptiveMetadata(String aipId, String descriptiveMetadataId,
    ContentPayload descriptiveMetadataPayload, String descriptiveMetadataType, String descriptiveMetadataVersion,
    Map<String, String> properties, String updatedBy)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().updateDescriptiveMetadata(aipId, descriptiveMetadataId, descriptiveMetadataPayload,
      descriptiveMetadataType, descriptiveMetadataVersion, properties, updatedBy);
  }

  @Override
  public DescriptiveMetadata updateDescriptiveMetadata(String aipId, String representationId,
    String descriptiveMetadataId, ContentPayload descriptiveMetadataPayload, String descriptiveMetadataType,
    String descriptiveMetadataVersion, Map<String, String> properties, String updatedBy)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().updateDescriptiveMetadata(aipId, representationId, descriptiveMetadataId,
      descriptiveMetadataPayload, descriptiveMetadataType, descriptiveMetadataVersion, properties, updatedBy);
  }

  @Override
  public void deleteDescriptiveMetadata(String aipId, String descriptiveMetadataId, String deletedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    getModelService().deleteDescriptiveMetadata(aipId, descriptiveMetadataId, deletedBy);
  }

  @Override
  public void deleteDescriptiveMetadata(String aipId, String representationId, String descriptiveMetadataId,
    String deletedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    getModelService().deleteDescriptiveMetadata(aipId, representationId, descriptiveMetadataId, deletedBy);
  }

  @Override
  public CloseableIterable<BinaryVersion> listDescriptiveMetadataVersions(String aipId, String representationId,
    String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().listDescriptiveMetadataVersions(aipId, representationId, descriptiveMetadataId);
  }

  @Override
  public BinaryVersion revertDescriptiveMetadataVersion(String aipId, String descriptiveMetadataId, String versionId,
    Map<String, String> properties)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().revertDescriptiveMetadataVersion(aipId, descriptiveMetadataId, versionId, properties);
  }

  @Override
  public BinaryVersion revertDescriptiveMetadataVersion(String aipId, String representationId,
    String descriptiveMetadataId, String versionId, Map<String, String> properties)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().revertDescriptiveMetadataVersion(aipId, representationId, descriptiveMetadataId, versionId,
      properties);
  }

  @Override
  public CloseableIterable<OptionalWithCause<DescriptiveMetadata>> listDescriptiveMetadata()
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return getModelService().listDescriptiveMetadata();
  }

  @Override
  public CloseableIterable<OptionalWithCause<DescriptiveMetadata>> listDescriptiveMetadata(String aipId,
    boolean includeRepresentations)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return getModelService().listDescriptiveMetadata(aipId, includeRepresentations);
  }

  @Override
  public CloseableIterable<OptionalWithCause<DescriptiveMetadata>> listDescriptiveMetadata(String aipId,
    String representationId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return getModelService().listDescriptiveMetadata(aipId, representationId);
  }

  @Override
  public Representation retrieveRepresentation(String aipId, String representationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrieveRepresentation(aipId, representationId);
  }

  @Override
  public Representation createRepresentation(String aipId, String representationId, boolean original, String type,
    boolean notify, String createdBy, List<String> representationState) throws RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException, AlreadyExistsException {
    return getModelService().createRepresentation(aipId, representationId, original, type, notify, createdBy,
      representationState);
  }

  @Override
  public Representation createRepresentation(String aipId, String representationId, boolean original, String type,
    boolean notify, String createdBy) throws RequestNotValidException, GenericException, NotFoundException,
    AuthorizationDeniedException, AlreadyExistsException {
    return getModelService().createRepresentation(aipId, representationId, original, type, notify, createdBy);
  }

  @Override
  public Representation createRepresentation(String aipId, String representationId, boolean original, String type,
    StorageService sourceStorage, StoragePath sourcePath, boolean justData, String createdBy)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException,
    AlreadyExistsException {
    return getModelService().createRepresentation(aipId, representationId, original, type, sourceStorage, sourcePath,
      justData, createdBy);
  }

  @Override
  public Representation updateRepresentationInfo(Representation representation) throws GenericException {
    return getModelService().updateRepresentationInfo(representation);
  }

  @Override
  public void changeRepresentationType(String aipId, String representationId, String type, String updatedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    getModelService().changeRepresentationType(aipId, representationId, type, updatedBy);
  }

  @Override
  public void changeRepresentationShallowFileFlag(String aipId, String representationId, boolean hasShallowFiles,
    String updatedBy, boolean notify)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    getModelService().changeRepresentationShallowFileFlag(aipId, representationId, hasShallowFiles, updatedBy, notify);
  }

  @Override
  public void changeRepresentationStates(String aipId, String representationId, List<String> newStates,
    String updatedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    getModelService().changeRepresentationStates(aipId, representationId, newStates, updatedBy);
  }

  @Override
  public Representation updateRepresentation(String aipId, String representationId, boolean original, String type,
    StorageService sourceStorage, StoragePath sourcePath, String updatedBy) throws RequestNotValidException,
    NotFoundException, GenericException, AuthorizationDeniedException, ValidationException {
    return getModelService().updateRepresentation(aipId, representationId, original, type, sourceStorage, sourcePath,
      updatedBy);
  }

  @Override
  public void deleteRepresentation(String aipId, String representationId, String username)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    getModelService().deleteRepresentation(aipId, representationId, username);
  }

  @Override
  public CloseableIterable<OptionalWithCause<File>> listFilesUnder(String aipId, String representationId,
    boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    return getModelService().listFilesUnder(aipId, representationId, recursive);
  }

  @Override
  public CloseableIterable<OptionalWithCause<File>> listExternalFilesUnder(File file)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    return getModelService().listExternalFilesUnder(file);
  }

  @Override
  public CloseableIterable<OptionalWithCause<File>> listFilesUnder(File f, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    return getModelService().listFilesUnder(f, recursive);
  }

  @Override
  public CloseableIterable<OptionalWithCause<File>> listFilesUnder(String aipId, String representationId,
    List<String> directoryPath, String fileId, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    return getModelService().listFilesUnder(aipId, representationId, directoryPath, fileId, recursive);
  }

  @Override
  public File retrieveFile(String aipId, String representationId, List<String> directoryPath, String fileId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrieveFile(aipId, representationId, directoryPath, fileId);
  }

  @Override
  public File createFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload, String createdBy) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    return getModelService().createFile(aipId, representationId, directoryPath, fileId, contentPayload, createdBy);
  }

  @Override
  public File createFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload, String createdBy, boolean notify) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    return getModelService().createFile(aipId, representationId, directoryPath, fileId, contentPayload, createdBy,
      notify);
  }

  @Override
  public File createFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    String dirName, String createdBy, boolean notify) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    return getModelService().createFile(aipId, representationId, directoryPath, fileId, dirName, createdBy, notify);
  }

  @Override
  public File updateFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload, boolean createIfNotExists, String updatedBy, boolean notify)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().updateFile(aipId, representationId, directoryPath, fileId, contentPayload,
      createIfNotExists, updatedBy, notify);
  }

  @Override
  public File updateFile(File file, ContentPayload contentPayload, boolean createIfNotExists, String updatedBy,
    boolean notify) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().updateFile(file, contentPayload, createIfNotExists, updatedBy, notify);
  }

  @Override
  public void deleteFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    String deletedBy, boolean notify)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    getModelService().deleteFile(aipId, representationId, directoryPath, fileId, deletedBy, notify);
  }

  @Override
  public void deleteFile(File file, String deletedBy, boolean notify)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    getModelService().deleteFile(file, deletedBy, notify);
  }

  @Override
  public File renameFolder(File folder, String newName, boolean reindexResources) throws AlreadyExistsException,
    GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    return getModelService().renameFolder(folder, newName, reindexResources);
  }

  @Override
  public File moveFile(File file, String newAipId, String newRepresentationId, List<String> newDirectoryPath,
    String newId, boolean reindexResources) throws AlreadyExistsException, GenericException, NotFoundException,
    RequestNotValidException, AuthorizationDeniedException {
    return getModelService().moveFile(file, newAipId, newRepresentationId, newDirectoryPath, newId, reindexResources);
  }

  @Override
  public void createRepositoryEvent(RodaConstants.PreservationEventType eventType, String eventDescription,
    PluginState outcomeState, String outcomeText, String outcomeDetail, String agentName, boolean notify) {
    getModelService().createRepositoryEvent(eventType, eventDescription, outcomeState, outcomeText, outcomeDetail,
      agentName, notify);
  }

  @Override
  public void createRepositoryEvent(RodaConstants.PreservationEventType eventType, String eventDescription,
    List<LinkingIdentifier> sources, List<LinkingIdentifier> targets, PluginState outcomeState, String outcomeText,
    String outcomeDetail, String agentName, boolean notify) {
    getModelService().createRepositoryEvent(eventType, eventDescription, sources, targets, outcomeState, outcomeText,
      outcomeDetail, agentName, notify);
  }

  @Override
  public void createUpdateAIPEvent(String aipId, String representationId, List<String> filePath, String fileId,
    RodaConstants.PreservationEventType eventType, String eventDescription, PluginState outcomeState,
    String outcomeText, String outcomeDetail, String agentName, boolean notify) {
    getModelService().createUpdateAIPEvent(aipId, representationId, filePath, fileId, eventType, eventDescription,
      outcomeState, outcomeText, outcomeDetail, agentName, notify);
  }

  @Override
  public void createEvent(String aipId, String representationId, List<String> filePath, String fileId,
    RodaConstants.PreservationEventType eventType, String eventDescription, List<LinkingIdentifier> sources,
    List<LinkingIdentifier> targets, PluginState outcomeState, String outcomeText, String outcomeDetail,
    String agentName, boolean notify) {
    getModelService().createEvent(aipId, representationId, filePath, fileId, eventType, eventDescription, sources,
      targets, outcomeState, outcomeText, outcomeDetail, agentName, notify);
  }

  @Override
  public void createEvent(String aipId, String representationId, List<String> filePath, String fileId,
    RodaConstants.PreservationEventType eventType, String eventDescription, List<LinkingIdentifier> sources,
    List<LinkingIdentifier> targets, PluginState outcomeState, String outcomeText, String outcomeDetail,
    String agentName, String agentRole, boolean notify) {
    getModelService().createEvent(aipId, representationId, filePath, fileId, eventType, eventDescription, sources,
      targets, outcomeState, outcomeText, outcomeDetail, agentName, agentRole, notify);
  }

  @Override
  public void createEvent(String aipId, String representationId, List<String> filePath, String fileId,
    RodaConstants.PreservationEventType eventType, String eventDescription, List<LinkingIdentifier> sources,
    List<LinkingIdentifier> targets, PluginState outcomeState, String outcomeDetail, String outcomeExtension,
    List<LinkingIdentifier> agentIds, String username, boolean notify) throws GenericException, ValidationException,
    NotFoundException, RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException {
    getModelService().createEvent(aipId, representationId, filePath, fileId, eventType, eventDescription, sources,
      targets, outcomeState, outcomeDetail, outcomeExtension, agentIds, username, notify);
  }

  @Override
  public PreservationMetadata retrievePreservationMetadata(String id,
    PreservationMetadata.PreservationMetadataType type) {
    return getModelService().retrievePreservationMetadata(id, type);
  }

  @Override
  public PreservationMetadata retrievePreservationMetadata(String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId, PreservationMetadata.PreservationMetadataType type) {
    return getModelService().retrievePreservationMetadata(aipId, representationId, fileDirectoryPath, fileId, type);
  }

  @Override
  public Binary retrievePreservationRepresentation(String aipId, String representationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrievePreservationRepresentation(aipId, representationId);
  }

  @Override
  public boolean preservationRepresentationExists(String aipId, String representationId)
    throws RequestNotValidException {
    return getModelService().preservationRepresentationExists(aipId, representationId);
  }

  @Override
  public Binary retrievePreservationFile(File file)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrievePreservationFile(file);
  }

  @Override
  public Binary retrievePreservationFile(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrievePreservationFile(aipId, representationId, fileDirectoryPath, fileId);
  }

  @Override
  public boolean preservationFileExists(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().preservationFileExists(aipId, representationId, fileDirectoryPath, fileId);
  }

  @Override
  public Binary retrieveRepositoryPreservationEvent(String fileId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrieveRepositoryPreservationEvent(fileId);
  }

  @Override
  public Binary retrievePreservationEvent(String aipId, String representationId, List<String> filePath, String fileId,
    String preservationID)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrievePreservationEvent(aipId, representationId, filePath, fileId, preservationID);
  }

  @Override
  public Binary retrievePreservationAgent(String preservationID)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrievePreservationAgent(preservationID);
  }

  @Override
  public PreservationMetadata createPreservationMetadata(PreservationMetadata.PreservationMetadataType type,
    String aipId, String representationId, List<String> fileDirectoryPath, String fileId, ContentPayload payload,
    String username, boolean notify) throws GenericException, NotFoundException, RequestNotValidException,
    AuthorizationDeniedException, AlreadyExistsException {
    return getModelService().createPreservationMetadata(type, aipId, representationId, fileDirectoryPath, fileId,
      payload, username, notify);
  }

  @Override
  public void createTechnicalMetadata(String aipId, String representationId, String metadataType, String fileId,
    ContentPayload payload, String createdBy, boolean notify) throws AuthorizationDeniedException,
    RequestNotValidException, AlreadyExistsException, NotFoundException, GenericException {
    getModelService().createTechnicalMetadata(aipId, representationId, metadataType, fileId, payload, createdBy,
      notify);
  }

  @Override
  public PreservationMetadata createPreservationMetadata(PreservationMetadata.PreservationMetadataType type,
    String aipId, List<String> fileDirectoryPath, String fileId, ContentPayload payload, String username,
    boolean notify) throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException,
    AlreadyExistsException {
    return getModelService().createPreservationMetadata(type, aipId, fileDirectoryPath, fileId, payload, username,
      notify);
  }

  @Override
  public PreservationMetadata createPreservationMetadata(PreservationMetadata.PreservationMetadataType type,
    String aipId, String representationId, ContentPayload payload, String username, boolean notify)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException,
    AlreadyExistsException {
    return getModelService().createPreservationMetadata(type, aipId, representationId, payload, username, notify);
  }

  @Override
  public PreservationMetadata createPreservationMetadata(PreservationMetadata.PreservationMetadataType type, String id,
    ContentPayload payload, boolean notify) throws GenericException, NotFoundException, RequestNotValidException,
    AuthorizationDeniedException, AlreadyExistsException {
    return getModelService().createPreservationMetadata(type, id, payload, notify);
  }

  @Override
  public PreservationMetadata createPreservationMetadata(PreservationMetadata.PreservationMetadataType type, String id,
    String aipId, String representationId, List<String> fileDirectoryPath, String fileId, ContentPayload payload,
    String createdBy, boolean notify) throws GenericException, NotFoundException, RequestNotValidException,
    AuthorizationDeniedException, AlreadyExistsException {
    return getModelService().createPreservationMetadata(type, id, aipId, representationId, fileDirectoryPath, fileId,
      payload, createdBy, notify);
  }

  @Override
  public PreservationMetadata updatePreservationMetadata(PreservationMetadata.PreservationMetadataType type, String id,
    ContentPayload payload, boolean notify)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    return getModelService().updatePreservationMetadata(type, id, payload, notify);
  }

  @Override
  public PreservationMetadata updatePreservationMetadata(String id, PreservationMetadata.PreservationMetadataType type,
    String aipId, String representationId, List<String> fileDirectoryPath, String fileId, ContentPayload payload,
    String updatedBy, boolean notify)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    return getModelService().updatePreservationMetadata(id, type, aipId, representationId, fileDirectoryPath, fileId,
      payload, updatedBy, notify);
  }

  @Override
  public void deletePreservationMetadata(PreservationMetadata pm, boolean notify)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    getModelService().deletePreservationMetadata(pm, notify);
  }

  @Override
  public void deletePreservationMetadata(PreservationMetadata.PreservationMetadataType type, String aipId,
    String representationId, String id, List<String> filePath, boolean notify)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    getModelService().deletePreservationMetadata(type, aipId, representationId, id, filePath, notify);
  }

  @Override
  public CloseableIterable<OptionalWithCause<PreservationMetadata>> listPreservationMetadata()
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return getModelService().listPreservationMetadata();
  }

  @Override
  public CloseableIterable<OptionalWithCause<PreservationMetadata>> listPreservationMetadata(String aipId,
    boolean includeRepresentations)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return getModelService().listPreservationMetadata(aipId, includeRepresentations);
  }

  @Override
  public CloseableIterable<OptionalWithCause<PreservationMetadata>> listPreservationMetadata(String aipId,
    String representationId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return getModelService().listPreservationMetadata(aipId, representationId);
  }

  @Override
  public CloseableIterable<OptionalWithCause<PreservationMetadata>> listPreservationAgents()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException {
    return getModelService().listPreservationAgents();
  }

  @Override
  public CloseableIterable<OptionalWithCause<PreservationMetadata>> listPreservationRepositoryEvents()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException {
    return getModelService().listPreservationRepositoryEvents();
  }

  @Override
  public Binary retrieveOtherMetadataBinary(OtherMetadata om)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrieveOtherMetadataBinary(om);
  }

  @Override
  public Binary retrieveOtherMetadataBinary(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId, String fileSuffix, String type)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrieveOtherMetadataBinary(aipId, representationId, fileDirectoryPath, fileId, fileSuffix,
      type);
  }

  @Override
  public OtherMetadata retrieveOtherMetadata(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId, String fileSuffix, String type)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrieveOtherMetadata(aipId, representationId, fileDirectoryPath, fileId, fileSuffix,
      type);
  }

  @Override
  public OtherMetadata createOrUpdateOtherMetadata(String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId, String fileSuffix, String type, ContentPayload payload,
    String username, boolean notify)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().createOrUpdateOtherMetadata(aipId, representationId, fileDirectoryPath, fileId, fileSuffix,
      type, payload, username, notify);
  }

  @Override
  public void deleteOtherMetadata(String aipId, String representationId, List<String> fileDirectoryPath, String fileId,
    String fileSuffix, String type, String username)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    getModelService().deleteOtherMetadata(aipId, representationId, fileDirectoryPath, fileId, fileSuffix, type,
      username);
  }

  @Override
  public CloseableIterable<OptionalWithCause<OtherMetadata>> listOtherMetadata(String aipId, String type,
    boolean includeRepresentations)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return getModelService().listOtherMetadata(aipId, type, includeRepresentations);
  }

  @Override
  public CloseableIterable<OptionalWithCause<OtherMetadata>> listOtherMetadata(String aipId, String representationId)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    return getModelService().listOtherMetadata(aipId, representationId);
  }

  @Override
  public CloseableIterable<OptionalWithCause<OtherMetadata>> listOtherMetadata(String aipId, String representationId,
    List<String> filePath, String fileId, String type)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return getModelService().listOtherMetadata(aipId, representationId, filePath, fileId, type);
  }

  @Override
  public void importLogEntries(InputStream inputStream, String filename) throws AuthorizationDeniedException,
    GenericException, AlreadyExistsException, RequestNotValidException, NotFoundException {
    getModelService().importLogEntries(inputStream, filename);
  }

  @Override
  public void addLogEntry(LogEntry logEntry, Path logDirectory, boolean notify)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    getModelService().addLogEntry(logEntry, logDirectory, notify);
  }

  @Override
  public void addLogEntry(LogEntry logEntry, Path logDirectory)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    getModelService().addLogEntry(logEntry, logDirectory);
  }

  @Override
  public void findOldLogsAndSendThemToMaster(Path logDirectory, Path currentLogFile) {
    getModelService().findOldLogsAndSendThemToMaster(logDirectory, currentLogFile);
  }

  @Override
  public void findOldLogsAndMoveThemToStorage(Path logDirectory, Path currentLogFile)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    getModelService().findOldLogsAndMoveThemToStorage(logDirectory, currentLogFile);
  }

  @Override
  public User retrieveAuthenticatedUser(String name, String password)
    throws GenericException, AuthenticationDeniedException {
    return getModelService().retrieveAuthenticatedUser(name, password);
  }

  @Override
  public User retrieveUserByEmail(String email) throws GenericException {
    return getModelService().retrieveUserByEmail(email);
  }

  @Override
  public User registerUser(User user, SecureString password, boolean notify)
    throws GenericException, UserAlreadyExistsException, EmailAlreadyExistsException, AuthorizationDeniedException {
    return getModelService().registerUser(user, password, notify);
  }

  @Override
  public User createUser(User user, boolean notify) throws GenericException, EmailAlreadyExistsException,
    UserAlreadyExistsException, IllegalOperationException, NotFoundException, AuthorizationDeniedException {
    return getModelService().createUser(user, notify);
  }

  @Override
  public User createUser(User user, SecureString password, boolean notify)
    throws EmailAlreadyExistsException, UserAlreadyExistsException, IllegalOperationException, GenericException,
    NotFoundException, AuthorizationDeniedException {
    return getModelService().createUser(user, password, notify);
  }

  @Override
  public User createUser(User user, SecureString password, boolean notify, boolean isHandlingEvent)
    throws GenericException, EmailAlreadyExistsException, UserAlreadyExistsException, IllegalOperationException,
    NotFoundException, AuthorizationDeniedException {
    return getModelService().createUser(user, password, notify, isHandlingEvent);
  }

  @Override
  public User updateUser(User user, SecureString password, boolean notify)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    return getModelService().updateUser(user, password, notify);
  }

  @Override
  public User updateUser(User user, SecureString password, boolean notify, boolean isHandlingEvent)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    return getModelService().updateUser(user, password, notify, isHandlingEvent);
  }

  @Override
  public User deActivateUser(String id, boolean activate, boolean notify)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    return getModelService().deActivateUser(id, activate, notify);
  }

  @Override
  public User deActivateUser(String id, boolean activate, boolean notify, boolean isHandlingEvent)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    return getModelService().deActivateUser(id, activate, notify, isHandlingEvent);
  }

  @Override
  public User updateMyUser(User user, SecureString password, boolean notify)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    return getModelService().updateMyUser(user, password, notify);
  }

  @Override
  public User updateMyUser(User user, SecureString password, boolean notify, boolean isHandlingEvent)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    return getModelService().updateMyUser(user, password, notify, isHandlingEvent);
  }

  @Override
  public void deleteUser(String id, boolean notify) throws GenericException, AuthorizationDeniedException {
    getModelService().deleteUser(id, notify);
  }

  @Override
  public void deleteUser(String id, boolean notify, boolean isHandlingEvent)
    throws GenericException, AuthorizationDeniedException {
    getModelService().deleteUser(id, notify, isHandlingEvent);
  }

  @Override
  public List<User> listUsers() throws GenericException {
    return getModelService().listUsers();
  }

  @Override
  public User retrieveUser(String name) throws GenericException {
    return getModelService().retrieveUser(name);
  }

  @Override
  public String retrieveExtraLdap(String name) throws GenericException {
    return getModelService().retrieveExtraLdap(name);
  }

  @Override
  public Group retrieveGroup(String name) throws GenericException, NotFoundException {
    return getModelService().retrieveGroup(name);
  }

  @Override
  public Group createGroup(Group group, boolean notify)
    throws GenericException, AlreadyExistsException, AuthorizationDeniedException {
    return getModelService().createGroup(group, notify);
  }

  @Override
  public Group createGroup(Group group, boolean notify, boolean isHandlingEvent)
    throws GenericException, AlreadyExistsException, AuthorizationDeniedException {
    return getModelService().createGroup(group, notify, isHandlingEvent);
  }

  @Override
  public Group updateGroup(Group group, boolean notify)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().updateGroup(group, notify);
  }

  @Override
  public Group updateGroup(Group group, boolean notify, boolean isHandlingEvent)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().updateGroup(group, notify, isHandlingEvent);
  }

  @Override
  public void deleteGroup(String id, boolean notify) throws GenericException, AuthorizationDeniedException {
    getModelService().deleteGroup(id, notify);
  }

  @Override
  public void deleteGroup(String id, boolean notify, boolean isHandlingEvent)
    throws GenericException, AuthorizationDeniedException {
    getModelService().deleteGroup(id, notify, isHandlingEvent);
  }

  @Override
  public List<Group> listGroups() throws GenericException {
    return getModelService().listGroups();
  }

  @Override
  public User confirmUserEmail(String username, String email, String emailConfirmationToken, boolean useModel,
    boolean notify) throws NotFoundException, InvalidTokenException, GenericException {
    return getModelService().confirmUserEmail(username, email, emailConfirmationToken, useModel, notify);
  }

  @Override
  public User requestPasswordReset(String username, String email, boolean useModel, boolean notify)
    throws IllegalOperationException, NotFoundException, GenericException, AuthorizationDeniedException {
    return getModelService().requestPasswordReset(username, email, useModel, notify);
  }

  @Override
  public User resetUserPassword(String username, SecureString password, String resetPasswordToken, boolean useModel,
    boolean notify) throws NotFoundException, InvalidTokenException, IllegalOperationException, GenericException,
    AuthorizationDeniedException {
    return getModelService().resetUserPassword(username, password, resetPasswordToken, useModel, notify);
  }

  @Override
  public void createJob(Job job)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    getModelService().createJob(job);
  }

  @Override
  public void createOrUpdateJob(Job job)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    getModelService().createOrUpdateJob(job);
  }

  @Override
  public Job retrieveJob(String jobId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrieveJob(jobId);
  }

  @Override
  public void deleteJob(String jobId)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    getModelService().deleteJob(jobId);
  }

  @Override
  public Report retrieveJobReport(String jobId, String jobReportId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrieveJobReport(jobId, jobReportId);
  }

  @Override
  public Report retrieveJobReport(String jobId, String sourceObjectId, String outcomeObjectId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrieveJobReport(jobId, sourceObjectId, outcomeObjectId);
  }

  @Override
  public void createOrUpdateJobReport(Report jobReport, Job cachedJob)
    throws GenericException, AuthorizationDeniedException {
    getModelService().createOrUpdateJobReport(jobReport, cachedJob);
  }

  @Override
  public void createOrUpdateJobReport(Report jobReport, IndexedJob indexJob)
    throws GenericException, AuthorizationDeniedException {
    getModelService().createOrUpdateJobReport(jobReport, indexJob);
  }

  @Override
  public void deleteJobReport(String jobId, String jobReportId)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    getModelService().deleteJobReport(jobId, jobReportId);
  }

  @Override
  public void updateAIPPermissions(String aipId, Permissions permissions, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    getModelService().updateAIPPermissions(aipId, permissions, updatedBy);
  }

  @Override
  public void updateAIPPermissions(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    getModelService().updateAIPPermissions(aip, updatedBy);
  }

  @Override
  public void updateDIPPermissions(DIP dip)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    getModelService().updateDIPPermissions(dip);
  }

  @Override
  public void deleteTransferredResource(TransferredResource transferredResource)
    throws GenericException, AuthorizationDeniedException {
    getModelService().deleteTransferredResource(transferredResource);
  }

  @Override
  public Job updateJobInstanceId(Job job)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    return getModelService().updateJobInstanceId(job);
  }

  @Override
  public Risk createRisk(Risk risk, boolean commit) throws GenericException, AuthorizationDeniedException {
    return getModelService().createRisk(risk, commit);
  }

  @Override
  public Risk updateRiskInstanceId(Risk risk, boolean commit) throws GenericException, AuthorizationDeniedException {
    return getModelService().updateRiskInstanceId(risk, commit);
  }

  @Override
  public Risk updateRisk(Risk risk, Map<String, String> properties, boolean commit, int incidences)
    throws GenericException, AuthorizationDeniedException {
    return getModelService().updateRisk(risk, properties, commit, incidences);
  }

  @Override
  public void deleteRisk(String riskId, boolean commit)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    getModelService().deleteRisk(riskId, commit);
  }

  @Override
  public Risk retrieveRisk(String riskId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrieveRisk(riskId);
  }

  @Override
  public BinaryVersion retrieveVersion(String id, String versionId)
    throws RequestNotValidException, GenericException, NotFoundException {
    return getModelService().retrieveVersion(id, versionId);
  }

  @Override
  public BinaryVersion revertRiskVersion(String riskId, String versionId, Map<String, String> properties,
    boolean commit, int incidences)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().revertRiskVersion(riskId, versionId, properties, commit, incidences);
  }

  @Override
  public RiskIncidence createRiskIncidence(RiskIncidence riskIncidence, boolean commit)
    throws AlreadyExistsException, NotFoundException, AuthorizationDeniedException, GenericException {
    return getModelService().createRiskIncidence(riskIncidence, commit);
  }

  @Override
  public RiskIncidence updateRiskIncidenceInstanceId(RiskIncidence riskIncidence, boolean commit)
    throws GenericException, AuthorizationDeniedException {
    return getModelService().updateRiskIncidenceInstanceId(riskIncidence, commit);
  }

  @Override
  public RiskIncidence updateRiskIncidence(RiskIncidence riskIncidence, boolean commit)
    throws GenericException, AuthorizationDeniedException {
    return getModelService().updateRiskIncidence(riskIncidence, commit);
  }

  @Override
  public void deleteRiskIncidence(String riskIncidenceId, boolean commit)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    getModelService().deleteRiskIncidence(riskIncidenceId, commit);
  }

  @Override
  public RiskIncidence retrieveRiskIncidence(String incidenceId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrieveRiskIncidence(incidenceId);
  }

  @Override
  public Notification createNotification(Notification notification, NotificationProcessor processor)
    throws GenericException, AuthorizationDeniedException {
    return getModelService().createNotification(notification, processor);
  }

  @Override
  public Notification updateNotificationInstanceId(Notification notification)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().updateNotificationInstanceId(notification);
  }

  @Override
  public Notification updateNotification(Notification notification)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().updateNotification(notification);
  }

  @Override
  public void deleteNotification(String notificationId)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    getModelService().deleteNotification(notificationId);
  }

  @Override
  public Notification retrieveNotification(String notificationId)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrieveNotification(notificationId);
  }

  @Override
  public Notification acknowledgeNotification(String notificationId, String token)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().acknowledgeNotification(notificationId, token);
  }

  @Override
  public CloseableIterable<OptionalWithCause<DIPFile>> listDIPFilesUnder(DIPFile f, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    return getModelService().listDIPFilesUnder(f, recursive);
  }

  @Override
  public CloseableIterable<OptionalWithCause<DIPFile>> listDIPFilesUnder(String dipId, List<String> directoryPath,
    String fileId, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    return getModelService().listDIPFilesUnder(dipId, directoryPath, fileId, recursive);
  }

  @Override
  public CloseableIterable<OptionalWithCause<DIPFile>> listDIPFilesUnder(String dipId, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    return getModelService().listDIPFilesUnder(dipId, recursive);
  }

  @Override
  public void updateDIPInstanceId(DIP dip)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    getModelService().updateDIPInstanceId(dip);
  }

  @Override
  public DIP createDIP(DIP dip, boolean notify) throws GenericException, AuthorizationDeniedException {
    return getModelService().createDIP(dip, notify);
  }

  @Override
  public DIP updateDIP(DIP dip) throws GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().updateDIP(dip);
  }

  @Override
  public void deleteDIP(String dipId) throws GenericException, NotFoundException, AuthorizationDeniedException {
    getModelService().deleteDIP(dipId);
  }

  @Override
  public DIP retrieveDIP(String dipId) throws GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrieveDIP(dipId);
  }

  @Override
  public DIPFile createDIPFile(String dipId, List<String> directoryPath, String fileId, long size,
    ContentPayload contentPayload, boolean notify) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    return getModelService().createDIPFile(dipId, directoryPath, fileId, size, contentPayload, notify);
  }

  @Override
  public DIPFile createDIPFile(String dipId, List<String> directoryPath, String fileId, String dirName, boolean notify)
    throws RequestNotValidException, GenericException, AlreadyExistsException, AuthorizationDeniedException {
    return getModelService().createDIPFile(dipId, directoryPath, fileId, dirName, notify);
  }

  @Override
  public DIPFile updateDIPFile(String dipId, List<String> directoryPath, String oldFileId, String fileId, long size,
    ContentPayload contentPayload, boolean createIfNotExists, boolean notify) throws RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException, AlreadyExistsException {
    return getModelService().updateDIPFile(dipId, directoryPath, oldFileId, fileId, size, contentPayload,
      createIfNotExists, notify);
  }

  @Override
  public void deleteDIPFile(String dipId, List<String> directoryPath, String fileId, boolean notify)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    getModelService().deleteDIPFile(dipId, directoryPath, fileId, notify);
  }

  @Override
  public DIPFile retrieveDIPFile(String dipId, List<String> directoryPath, String fileId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrieveDIPFile(dipId, directoryPath, fileId);
  }

  @Override
  public Directory getSubmissionDirectory(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return getModelService().getSubmissionDirectory(aipId);
  }

  @Override
  public void createSubmission(StorageService submissionStorage, StoragePath submissionStoragePath, String aipId)
    throws AlreadyExistsException, GenericException, RequestNotValidException, NotFoundException,
    AuthorizationDeniedException {
    getModelService().createSubmission(submissionStorage, submissionStoragePath, aipId);
  }

  @Override
  public void createSubmission(Path submissionPath, String aipId) throws AlreadyExistsException, GenericException,
    RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    getModelService().createSubmission(submissionPath, aipId);
  }

  @Override
  public Directory getDocumentationDirectory(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return getModelService().getDocumentationDirectory(aipId);
  }

  @Override
  public Directory getDocumentationDirectory(String aipId, String representationId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return getModelService().getDocumentationDirectory(aipId, representationId);
  }

  @Override
  public File createDocumentation(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {
    return getModelService().createDocumentation(aipId, representationId, directoryPath, fileId, contentPayload);
  }

  @Override
  public Directory getSchemasDirectory(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return getModelService().getSchemasDirectory(aipId);
  }

  @Override
  public Directory getSchemasDirectory(String aipId, String representationId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return getModelService().getSchemasDirectory(aipId, representationId);
  }

  @Override
  public File createSchema(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {
    return getModelService().createSchema(aipId, representationId, directoryPath, fileId, contentPayload);
  }

  @Override
  public <T extends IsRODAObject> Optional<LiteRODAObject> retrieveLiteFromObject(T object) {
    return getModelService().retrieveLiteFromObject(object);
  }

  @Override
  public <T extends IsModelObject> OptionalWithCause<T> retrieveObjectFromLite(LiteRODAObject liteRODAObject) {
    return getModelService().retrieveObjectFromLite(liteRODAObject);
  }

  @Override
  public TransferredResource retrieveTransferredResource(String fullPath) {
    return getModelService().retrieveTransferredResource(fullPath);
  }

  @Override
  public <T extends IsRODAObject> CloseableIterable<OptionalWithCause<T>> list(Class<T> objectClass)
    throws RODAException {
    return getModelService().list(objectClass);
  }

  @Override
  public <T extends IsRODAObject> CloseableIterable<OptionalWithCause<LiteRODAObject>> listLite(Class<T> objectClass)
    throws RODAException {
    return getModelService().listLite(objectClass);
  }

  @Override
  public CloseableIterable<OptionalWithCause<LogEntry>> listLogEntries() {
    return getModelService().listLogEntries();
  }

  @Override
  public CloseableIterable<OptionalWithCause<LogEntry>> listLogEntries(int daysToIndex) {
    return getModelService().listLogEntries(daysToIndex);
  }

  @Override
  public boolean hasObjects(Class<? extends IsRODAObject> objectClass) {
    return getModelService().hasObjects(objectClass);
  }

  @Override
  public boolean checkObjectPermission(String username, String permissionType, String objectClass, String id)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    return getModelService().checkObjectPermission(username, permissionType, objectClass, id);
  }

  @Override
  public RepresentationInformation createRepresentationInformation(RepresentationInformation ri, String createdBy,
    boolean commit) throws GenericException, AuthorizationDeniedException {
    return getModelService().createRepresentationInformation(ri, createdBy, commit);
  }

  @Override
  public RepresentationInformation updateRepresentationInformation(RepresentationInformation ri, String updatedBy,
    boolean commit) throws GenericException, AuthorizationDeniedException {
    return getModelService().updateRepresentationInformation(ri, updatedBy, commit);
  }

  @Override
  public RepresentationInformation updateRepresentationInformationInstanceId(RepresentationInformation ri,
    String updatedBy, boolean notify)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    return getModelService().updateRepresentationInformationInstanceId(ri, updatedBy, notify);
  }

  @Override
  public void deleteRepresentationInformation(String representationInformationId, boolean commit)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    getModelService().deleteRepresentationInformation(representationInformationId, commit);
  }

  @Override
  public RepresentationInformation retrieveRepresentationInformation(String representationInformationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrieveRepresentationInformation(representationInformationId);
  }

  @Override
  public DisposalHold retrieveDisposalHold(String disposalHoldId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return getModelService().retrieveDisposalHold(disposalHoldId);
  }

  @Override
  public DisposalHold createDisposalHold(DisposalHold disposalHold, String createdBy) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException, AlreadyExistsException, NotFoundException {
    return getModelService().createDisposalHold(disposalHold, createdBy);
  }

  @Override
  public DisposalHold updateDisposalHoldFirstUseDate(DisposalHold disposalHold, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, IllegalOperationException,
    GenericException {
    return getModelService().updateDisposalHoldFirstUseDate(disposalHold, updatedBy);
  }

  @Override
  public DisposalHold updateDisposalHold(DisposalHold disposalHold, String updatedBy, String details)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, IllegalOperationException,
    GenericException {
    return getModelService().updateDisposalHold(disposalHold, updatedBy, details);
  }

  @Override
  public DisposalHold updateDisposalHold(DisposalHold disposalHold, String updatedBy, boolean updateFirstUseDate,
    String details) throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException,
    IllegalOperationException {
    return getModelService().updateDisposalHold(disposalHold, updatedBy, updateFirstUseDate, details);
  }

  @Override
  public void deleteDisposalHold(String disposalHoldId) throws RequestNotValidException, NotFoundException,
    GenericException, AuthorizationDeniedException, IllegalOperationException {
    getModelService().deleteDisposalHold(disposalHoldId);
  }

  @Override
  public DisposalHolds listDisposalHolds()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, IOException {
    return getModelService().listDisposalHolds();
  }

  @Override
  public DisposalAIPMetadata createDisposalHoldAssociation(String aipId, String disposalHoldId, Date associatedOn,
    String associatedBy)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    return getModelService().createDisposalHoldAssociation(aipId, disposalHoldId, associatedOn, associatedBy);
  }

  @Override
  public List<DisposalHold> retrieveDirectActiveDisposalHolds(String aipId)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    return getModelService().retrieveDirectActiveDisposalHolds(aipId);
  }

  @Override
  public boolean onDisposalHold(String aipId)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    return getModelService().onDisposalHold(aipId);
  }

  @Override
  public boolean isAIPOnDirectHold(String aipId, String holdId)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    return getModelService().isAIPOnDirectHold(aipId, holdId);
  }

  @Override
  public DisposalSchedule createDisposalSchedule(DisposalSchedule disposalSchedule, String createdBy)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {
    return getModelService().createDisposalSchedule(disposalSchedule, createdBy);
  }

  @Override
  public DisposalSchedule updateDisposalSchedule(DisposalSchedule disposalSchedule, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException,
    IllegalOperationException {
    return getModelService().updateDisposalSchedule(disposalSchedule, updatedBy);
  }

  @Override
  public DisposalSchedule retrieveDisposalSchedule(String disposalScheduleId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrieveDisposalSchedule(disposalScheduleId);
  }

  @Override
  public DisposalSchedules listDisposalSchedules()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, IOException {
    return getModelService().listDisposalSchedules();
  }

  @Override
  public void deleteDisposalSchedule(String disposalScheduleId) throws NotFoundException, GenericException,
    AuthorizationDeniedException, RequestNotValidException, IllegalOperationException {
    getModelService().deleteDisposalSchedule(disposalScheduleId);
  }

  @Override
  public DisposalConfirmation retrieveDisposalConfirmation(String disposalConfirmationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrieveDisposalConfirmation(disposalConfirmationId);
  }

  @Override
  public void addDisposalHoldEntry(String disposalConfirmationId, DisposalHold disposalHold)
    throws GenericException, RequestNotValidException {
    getModelService().addDisposalHoldEntry(disposalConfirmationId, disposalHold);
  }

  @Override
  public void addDisposalHoldTransitiveEntry(String disposalConfirmationId, DisposalHold transitiveDisposalHold)
    throws RequestNotValidException, GenericException {
    getModelService().addDisposalHoldTransitiveEntry(disposalConfirmationId, transitiveDisposalHold);
  }

  @Override
  public void addDisposalScheduleEntry(String disposalConfirmationId, DisposalSchedule disposalSchedule)
    throws RequestNotValidException, GenericException {
    getModelService().addDisposalScheduleEntry(disposalConfirmationId, disposalSchedule);
  }

  @Override
  public void addAIPEntry(String disposalConfirmationId, DisposalConfirmationAIPEntry entry)
    throws RequestNotValidException, GenericException {
    getModelService().addAIPEntry(disposalConfirmationId, entry);
  }

  @Override
  public DisposalConfirmation updateDisposalConfirmation(DisposalConfirmation disposalConfirmation)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    return getModelService().updateDisposalConfirmation(disposalConfirmation);
  }

  @Override
  public DisposalConfirmation createDisposalConfirmation(DisposalConfirmation disposalConfirmation, String createdBy)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {
    return getModelService().createDisposalConfirmation(disposalConfirmation, createdBy);
  }

  @Override
  public void deleteDisposalConfirmation(String disposalConfirmationId) throws AuthorizationDeniedException,
    RequestNotValidException, NotFoundException, GenericException, IllegalOperationException {
    getModelService().deleteDisposalConfirmation(disposalConfirmationId);
  }

  @Override
  public DisposalHoldsAIPMetadata listDisposalHoldsAssociation(String aipId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().listDisposalHoldsAssociation(aipId);
  }

  @Override
  public DisposalTransitiveHoldsAIPMetadata listTransitiveDisposalHolds(String aipId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().listTransitiveDisposalHolds(aipId);
  }

  @Override
  public DisposalRule createDisposalRule(DisposalRule disposalRule, String createdBy) throws RequestNotValidException,
    NotFoundException, GenericException, AlreadyExistsException, AuthorizationDeniedException {
    return getModelService().createDisposalRule(disposalRule, createdBy);
  }

  @Override
  public DisposalRule updateDisposalRule(DisposalRule disposalRule, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    return getModelService().updateDisposalRule(disposalRule, updatedBy);
  }

  @Override
  public void deleteDisposalRule(String disposalRuleId, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, IOException, GenericException, NotFoundException {
    getModelService().deleteDisposalRule(disposalRuleId, updatedBy);
  }

  @Override
  public DisposalRule retrieveDisposalRule(String disposalRuleId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrieveDisposalRule(disposalRuleId);
  }

  @Override
  public DisposalRules listDisposalRules()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, IOException {
    return getModelService().listDisposalRules();
  }

  @Override
  public DistributedInstance createDistributedInstance(DistributedInstance distributedInstance, String createdBy)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, AlreadyExistsException,
    NotFoundException, IllegalOperationException {
    return getModelService().createDistributedInstance(distributedInstance, createdBy);
  }

  @Override
  public DistributedInstances listDistributedInstances()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, IOException {
    return getModelService().listDistributedInstances();
  }

  @Override
  public DistributedInstance retrieveDistributedInstance(String distributedInstanceId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrieveDistributedInstance(distributedInstanceId);
  }

  @Override
  public void deleteDistributedInstance(String distributedInstanceId)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    getModelService().deleteDistributedInstance(distributedInstanceId);
  }

  @Override
  public DistributedInstance updateDistributedInstance(DistributedInstance distributedInstance, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    return getModelService().updateDistributedInstance(distributedInstance, updatedBy);
  }

  @Override
  public AccessKey createAccessKey(AccessKey accessKey, String createdBy) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException, AlreadyExistsException, NotFoundException {
    return getModelService().createAccessKey(accessKey, createdBy);
  }

  @Override
  public AccessKeys listAccessKeys() throws RequestNotValidException, AuthorizationDeniedException, GenericException {
    return getModelService().listAccessKeys();
  }

  @Override
  public AccessKey retrieveAccessKey(String accessKeyId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getModelService().retrieveAccessKey(accessKeyId);
  }

  @Override
  public AccessKey updateAccessKey(AccessKey accessKey, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    return getModelService().updateAccessKey(accessKey, updatedBy);
  }

  @Override
  public void deleteAccessKey(String accessKeyId)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    getModelService().deleteAccessKey(accessKeyId);
  }

  @Override
  public void updateAccessKeyLastUsageDate(AccessKey accessKey)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    getModelService().updateAccessKeyLastUsageDate(accessKey);
  }

  @Override
  public AccessKeys listAccessKeysByUser(String userId)
    throws RequestNotValidException, AuthorizationDeniedException, GenericException {
    return getModelService().listAccessKeysByUser(userId);
  }

  @Override
  public void deactivateUserAccessKeys(String userId, String updatedBy)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    getModelService().deactivateUserAccessKeys(userId, updatedBy);
  }

  @Override
  public void deleteUserAccessKeys(String userId, String updatedBy)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    getModelService().deleteUserAccessKeys(userId, updatedBy);
  }

  @Override
  public void addModelObserver(ModelObserver observer) {
    getModelService().addModelObserver(observer);
  }

  @Override
  public void removeModelObserver(ModelObserver observer) {
    getModelService().removeModelObserver(observer);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyAipCreated(AIP aip) {
    return getModelService().notifyAipCreated(aip);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyAipUpdated(AIP aip) {
    return getModelService().notifyAipUpdated(aip);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyAipUpdatedOnChanged(AIP aip) {
    return getModelService().notifyAipUpdatedOnChanged(aip);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyAipDestroyed(AIP aip) {
    return getModelService().notifyAipDestroyed(aip);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyAipMoved(AIP aip, String oldParentId, String newParentId) {
    return getModelService().notifyAipMoved(aip, oldParentId, newParentId);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyAipStateUpdated(AIP aip) {
    return getModelService().notifyAipStateUpdated(aip);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyAipInstanceIdUpdated(AIP aip) {
    return getModelService().notifyAipInstanceIdUpdated(aip);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyAipDeleted(String aipId) {
    return getModelService().notifyAipDeleted(aipId);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyDescriptiveMetadataCreated(DescriptiveMetadata descriptiveMetadata) {
    return getModelService().notifyDescriptiveMetadataCreated(descriptiveMetadata);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyDescriptiveMetadataUpdated(DescriptiveMetadata descriptiveMetadata) {
    return getModelService().notifyDescriptiveMetadataUpdated(descriptiveMetadata);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyDescriptiveMetadataDeleted(String aipId, String representationId,
    String descriptiveMetadataBinaryId) {
    return getModelService().notifyDescriptiveMetadataDeleted(aipId, representationId, descriptiveMetadataBinaryId);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyRepresentationCreated(Representation representation) {
    return getModelService().notifyRepresentationCreated(representation);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyRepresentationUpdated(Representation representation) {
    return getModelService().notifyRepresentationUpdated(representation);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyRepresentationDeleted(String aipId, String representationId) {
    return getModelService().notifyRepresentationDeleted(aipId, representationId);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyRepresentationUpdatedOnChanged(Representation representation) {
    return getModelService().notifyRepresentationUpdatedOnChanged(representation);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyFileCreated(File file) {
    return getModelService().notifyFileCreated(file);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyFileUpdated(File file) {
    return getModelService().notifyFileUpdated(file);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyFileDeleted(String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId) {
    return getModelService().notifyFileDeleted(aipId, representationId, fileDirectoryPath, fileId);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyLogEntryCreated(LogEntry entry) {
    return getModelService().notifyLogEntryCreated(entry);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyUserCreated(User user) {
    return getModelService().notifyUserCreated(user);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyUserUpdated(User user) {
    return getModelService().notifyUserUpdated(user);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyUserDeleted(String userID) {
    return getModelService().notifyUserDeleted(userID);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyGroupCreated(Group group) {
    return getModelService().notifyGroupCreated(group);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyGroupUpdated(Group group) {
    return getModelService().notifyGroupUpdated(group);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyGroupDeleted(String groupID) {
    return getModelService().notifyGroupDeleted(groupID);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyPreservationMetadataCreated(
    PreservationMetadata preservationMetadataBinary) {
    return getModelService().notifyPreservationMetadataCreated(preservationMetadataBinary);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyPreservationMetadataUpdated(
    PreservationMetadata preservationMetadataBinary) {
    return getModelService().notifyPreservationMetadataUpdated(preservationMetadataBinary);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyPreservationMetadataDeleted(PreservationMetadata pm) {
    return getModelService().notifyPreservationMetadataDeleted(pm);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyOtherMetadataCreated(OtherMetadata otherMetadataBinary) {
    return getModelService().notifyOtherMetadataCreated(otherMetadataBinary);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyJobCreatedOrUpdated(Job job, boolean reindexJobReports) {
    return getModelService().notifyJobCreatedOrUpdated(job, reindexJobReports);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyJobDeleted(String jobId) {
    return getModelService().notifyJobDeleted(jobId);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyJobReportCreatedOrUpdated(Report jobReport, Job cachedJob) {
    return getModelService().notifyJobReportCreatedOrUpdated(jobReport, cachedJob);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyJobReportCreatedOrUpdated(Report jobReport, IndexedJob indexedJob) {
    return getModelService().notifyJobReportCreatedOrUpdated(jobReport, indexedJob);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyJobReportDeleted(String jobReportId) {
    return getModelService().notifyJobReportDeleted(jobReportId);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyAipPermissionsUpdated(AIP aip) {
    return getModelService().notifyAipPermissionsUpdated(aip);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyDipPermissionsUpdated(DIP dip) {
    return getModelService().notifyDipPermissionsUpdated(dip);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyDipInstanceIdUpdated(DIP dip) {
    return getModelService().notifyDipInstanceIdUpdated(dip);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyTransferredResourceDeleted(String transferredResourceID) {
    return getModelService().notifyTransferredResourceDeleted(transferredResourceID);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyRiskCreatedOrUpdated(Risk risk, int incidences, boolean commit) {
    return getModelService().notifyRiskCreatedOrUpdated(risk, incidences, commit);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyRiskDeleted(String riskId, boolean commit) {
    return getModelService().notifyRiskDeleted(riskId, commit);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyRiskIncidenceCreatedOrUpdated(RiskIncidence riskIncidence, boolean commit) {
    return getModelService().notifyRiskIncidenceCreatedOrUpdated(riskIncidence, commit);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyRiskIncidenceDeleted(String riskIncidenceId, boolean commit) {
    return getModelService().notifyRiskIncidenceDeleted(riskIncidenceId, commit);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyRepresentationInformationCreatedOrUpdated(RepresentationInformation ri,
    boolean commit) {
    return getModelService().notifyRepresentationInformationCreatedOrUpdated(ri, commit);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyRepresentationInformationDeleted(String representationInformationId,
    boolean commit) {
    return getModelService().notifyRepresentationInformationDeleted(representationInformationId, commit);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyNotificationCreatedOrUpdated(Notification notification) {
    return getModelService().notifyNotificationCreatedOrUpdated(notification);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyNotificationDeleted(String notificationId) {
    return getModelService().notifyNotificationDeleted(notificationId);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyDIPCreated(DIP dip, boolean commit) {
    return getModelService().notifyDIPCreated(dip, commit);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyDIPUpdated(DIP dip, boolean commit) {
    return getModelService().notifyDIPUpdated(dip, commit);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyDIPDeleted(String dipId, boolean commit) {
    return getModelService().notifyDIPDeleted(dipId, commit);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyDIPFileCreated(DIPFile file) {
    return getModelService().notifyDIPFileCreated(file);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyDIPFileUpdated(DIPFile file) {
    return getModelService().notifyDIPFileUpdated(file);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyDIPFileDeleted(String dipId, List<String> path, String fileId) {
    return getModelService().notifyDIPFileDeleted(dipId, path, fileId);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyDisposalConfirmationCreatedOrUpdated(DisposalConfirmation confirmation) {
    return getModelService().notifyDisposalConfirmationCreatedOrUpdated(confirmation);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyDisposalConfirmationDeleted(String disposalConfirmationId, boolean commit) {
    return getModelService().notifyDisposalConfirmationDeleted(disposalConfirmationId, commit);
  }
}
