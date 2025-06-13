package org.roda.core.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.UserAlreadyExistsException;
import org.roda.core.data.v2.ConsumesOutputStream;
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
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
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
import org.roda.core.index.IndexService;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryVersion;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.Directory;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FileStorageService;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface ModelService extends ModelObservable {
  StorageService getStorage();

  CloseableIterable<OptionalWithCause<AIP>> listAIPs()
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  AIP retrieveAIP(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;

  AIP createAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath, boolean notify, String createdBy)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, AlreadyExistsException,
    NotFoundException, ValidationException;

  AIP createAIP(String parentId, String type, Permissions permissions, List<String> ingestSIPIds, String ingestJobId,
    boolean notify, String createdBy, boolean isGhost, String aipId) throws RequestNotValidException, NotFoundException,
    GenericException, AlreadyExistsException, AuthorizationDeniedException;

  AIP createAIP(String parentId, String type, Permissions permissions, String createdBy, String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException;

  AIP createAIP(AIPState state, String parentId, String type, Permissions permissions, String createdBy, String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException;

  AIP createAIP(AIPState state, String parentId, String type, Permissions permissions, boolean notify, String createdBy,
    String aipId) throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException;

  AIP createAIP(AIPState state, String parentId, String type, Permissions permissions, String ingestSIPUUID,
    List<String> ingestSIPIds, String ingestJobId, boolean notify, String createdBy, String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException;

  AIP createAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath, String createdBy)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, AlreadyExistsException,
    NotFoundException, ValidationException;

  AIP notifyAipCreated(String aipId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  AIP notifyAipUpdated(String aipId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  AIP updateAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath, String updatedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException,
    AlreadyExistsException, ValidationException;

  AIP destroyAIP(AIP aip, String updatedBy)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  AIP updateAIP(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException;

  AIP updateAIPState(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException;

  AIP updateAIPInstanceId(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException;

  AIP moveAIP(String aipId, String parentId, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException;

  void deleteAIP(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;

  void changeAIPType(String aipId, String type, String updatedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;

  Binary retrieveDescriptiveMetadataBinary(String aipId, String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  Binary retrieveDescriptiveMetadataBinary(String aipId, String representationId, String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  DescriptiveMetadata retrieveDescriptiveMetadata(String aipId, String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  DescriptiveMetadata retrieveDescriptiveMetadata(String aipId, String representationId, String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  DescriptiveMetadata createDescriptiveMetadata(String aipId, String descriptiveMetadataId, ContentPayload payload,
    String descriptiveMetadataType, String descriptiveMetadataVersion, String createdBy, boolean notify)
    throws RequestNotValidException, GenericException, AlreadyExistsException, AuthorizationDeniedException,
    NotFoundException;

  DescriptiveMetadata createDescriptiveMetadata(String aipId, String descriptiveMetadataId, ContentPayload payload,
    String descriptiveMetadataType, String descriptiveMetadataVersion, String createdBy)
    throws RequestNotValidException, GenericException, AlreadyExistsException, AuthorizationDeniedException,
    NotFoundException;

  DescriptiveMetadata createDescriptiveMetadata(String aipId, String representationId, String descriptiveMetadataId,
    ContentPayload payload, String descriptiveMetadataType, String descriptiveMetadataVersion, String createdBy)
    throws RequestNotValidException, GenericException, AlreadyExistsException, AuthorizationDeniedException,
    NotFoundException;

  DescriptiveMetadata createDescriptiveMetadata(String aipId, String representationId, String descriptiveMetadataId,
    ContentPayload payload, String descriptiveMetadataType, String descriptiveMetadataVersion, String createdBy,
    boolean notify) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException;

  DescriptiveMetadata updateDescriptiveMetadata(String aipId, String descriptiveMetadataId,
    ContentPayload descriptiveMetadataPayload, String descriptiveMetadataType, String descriptiveMetadataVersion,
    Map<String, String> properties, String updatedBy)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  DescriptiveMetadata updateDescriptiveMetadata(String aipId, String representationId, String descriptiveMetadataId,
    ContentPayload descriptiveMetadataPayload, String descriptiveMetadataType, String descriptiveMetadataVersion,
    Map<String, String> properties, String updatedBy)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  void deleteDescriptiveMetadata(String aipId, String descriptiveMetadataId, String deletedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;

  void deleteDescriptiveMetadata(String aipId, String representationId, String descriptiveMetadataId, String deletedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;

  CloseableIterable<BinaryVersion> listDescriptiveMetadataVersions(String aipId, String representationId,
    String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  BinaryVersion revertDescriptiveMetadataVersion(String aipId, String descriptiveMetadataId, String versionId,
    Map<String, String> properties)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  BinaryVersion revertDescriptiveMetadataVersion(String aipId, String representationId, String descriptiveMetadataId,
    String versionId, Map<String, String> properties)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  CloseableIterable<OptionalWithCause<DescriptiveMetadata>> listDescriptiveMetadata()
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;

  CloseableIterable<OptionalWithCause<DescriptiveMetadata>> listDescriptiveMetadata(String aipId,
    boolean includeRepresentations)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;

  CloseableIterable<OptionalWithCause<DescriptiveMetadata>> listDescriptiveMetadata(String aipId,
    String representationId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;

  Representation retrieveRepresentation(String aipId, String representationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  Representation createRepresentation(String aipId, String representationId, boolean original, String type,
    boolean notify, String createdBy, List<String> representationState) throws RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException, AlreadyExistsException;

  Representation createRepresentation(String aipId, String representationId, boolean original, String type,
    boolean notify, String createdBy) throws RequestNotValidException, GenericException, NotFoundException,
    AuthorizationDeniedException, AlreadyExistsException;

  Representation createRepresentation(String aipId, String representationId, boolean original, String type,
    StorageService sourceStorage, StoragePath sourcePath, boolean justData, String createdBy)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException,
    AlreadyExistsException;

  Representation updateRepresentationInfo(Representation representation) throws GenericException;

  void changeRepresentationType(String aipId, String representationId, String type, String updatedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;

  void changeRepresentationShallowFileFlag(String aipId, String representationId, boolean hasShallowFiles,
    String updatedBy, boolean notify)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  void changeRepresentationStates(String aipId, String representationId, List<String> newStates, String updatedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;

  Representation updateRepresentation(String aipId, String representationId, boolean original, String type,
    StorageService sourceStorage, StoragePath sourcePath, String updatedBy) throws RequestNotValidException,
    NotFoundException, GenericException, AuthorizationDeniedException, ValidationException;

  void deleteRepresentation(String aipId, String representationId, String username)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;

  CloseableIterable<OptionalWithCause<File>> listFilesUnder(String aipId, String representationId, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException;

  CloseableIterable<OptionalWithCause<File>> listExternalFilesUnder(File file)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException;

  CloseableIterable<OptionalWithCause<File>> listFilesUnder(File f, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException;

  CloseableIterable<OptionalWithCause<File>> listFilesUnder(String aipId, String representationId,
    List<String> directoryPath, String fileId, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException;

  Long getExternalFilesTotalSize(File file)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException, IOException;

  File retrieveFile(String aipId, String representationId, List<String> directoryPath, String fileId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  File createFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload, String createdBy) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException;

  File createFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload, String createdBy, boolean notify) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException;

  File createFile(String aipId, String representationId, List<String> directoryPath, String fileId, String dirName,
    String createdBy, boolean notify) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException;

  File updateFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload, boolean createIfNotExists, String updatedBy, boolean notify)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  File updateFile(File file, ContentPayload contentPayload, boolean createIfNotExists, String updatedBy, boolean notify)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  void deleteFile(String aipId, String representationId, List<String> directoryPath, String fileId, String deletedBy,
    boolean notify) throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;

  void deleteFile(File file, String deletedBy, boolean notify)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;

  File renameFolder(File folder, String newName, boolean reindexResources) throws AlreadyExistsException,
    GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException;

  File moveFile(File file, String newAipId, String newRepresentationId, List<String> newDirectoryPath, String newId,
    boolean reindexResources) throws AlreadyExistsException, GenericException, NotFoundException,
    RequestNotValidException, AuthorizationDeniedException;

  PreservationMetadata createRepositoryEvent(RodaConstants.PreservationEventType eventType, String eventDescription,
    PluginState outcomeState, String outcomeText, String outcomeDetail, String agentName, boolean notify);

  PreservationMetadata createRepositoryEvent(RodaConstants.PreservationEventType eventType, String eventDescription,
    List<LinkingIdentifier> sources, List<LinkingIdentifier> targets, PluginState outcomeState, String outcomeText,
    String outcomeDetail, String agentName, boolean notify);

  PreservationMetadata createUpdateAIPEvent(String aipId, String representationId, List<String> filePath, String fileId,
    RodaConstants.PreservationEventType eventType, String eventDescription, PluginState outcomeState,
    String outcomeText, String outcomeDetail, String agentName, boolean notify);

  PreservationMetadata createEvent(String aipId, String representationId, List<String> filePath, String fileId,
    RodaConstants.PreservationEventType eventType, String eventDescription, List<LinkingIdentifier> sources,
    List<LinkingIdentifier> targets, PluginState outcomeState, String outcomeText, String outcomeDetail,
    String agentName, boolean notify);

  PreservationMetadata createEvent(String aipId, String representationId, List<String> filePath, String fileId,
    RodaConstants.PreservationEventType eventType, String eventDescription, List<LinkingIdentifier> sources,
    List<LinkingIdentifier> targets, PluginState outcomeState, String outcomeText, String outcomeDetail,
    String agentName, String agentRole, boolean notify);

  PreservationMetadata createEvent(String aipId, String representationId, List<String> filePath, String fileId,
    RodaConstants.PreservationEventType eventType, String eventDescription, List<LinkingIdentifier> sources,
    List<LinkingIdentifier> targets, PluginState outcomeState, String outcomeDetail, String outcomeExtension,
    List<LinkingIdentifier> agentIds, String username, boolean notify) throws GenericException, ValidationException,
    NotFoundException, RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException;

  PreservationMetadata retrievePreservationMetadata(String id, PreservationMetadata.PreservationMetadataType type);

  PreservationMetadata retrievePreservationMetadata(String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId, PreservationMetadata.PreservationMetadataType type);

  Binary retrievePreservationRepresentation(String aipId, String representationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  boolean preservationRepresentationExists(String aipId, String representationId) throws RequestNotValidException;

  Binary retrievePreservationFile(File file)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  Binary retrievePreservationFile(String aipId, String representationId, List<String> fileDirectoryPath, String fileId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  boolean preservationFileExists(String aipId, String representationId, List<String> fileDirectoryPath, String fileId)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException;

  Binary retrieveRepositoryPreservationEvent(String fileId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  Binary retrievePreservationEvent(String aipId, String representationId, List<String> filePath, String fileId,
    String preservationID)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  Binary retrievePreservationAgent(String preservationID)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  PreservationMetadata createPreservationMetadata(PreservationMetadata.PreservationMetadataType type, String aipId,
    String representationId, List<String> fileDirectoryPath, String fileId, ContentPayload payload, String username,
    boolean notify) throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException,
    AlreadyExistsException;

  void createTechnicalMetadata(String aipId, String representationId, String metadataType, String fileId,
    ContentPayload payload, String createdBy, boolean notify) throws AuthorizationDeniedException,
    RequestNotValidException, AlreadyExistsException, NotFoundException, GenericException;

  public PreservationMetadata createPreservationMetadata(PreservationMetadataType type, String aipId,
    List<String> fileDirectoryPath, String fileId, ContentPayload payload, String username, boolean notify)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException,
    AlreadyExistsException;

  PreservationMetadata createPreservationMetadata(PreservationMetadata.PreservationMetadataType type, String aipId,
    String representationId, ContentPayload payload, String username, boolean notify) throws GenericException,
    NotFoundException, RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException;

  PreservationMetadata createPreservationMetadata(PreservationMetadata.PreservationMetadataType type, String id,
    ContentPayload payload, boolean notify) throws GenericException, NotFoundException, RequestNotValidException,
    AuthorizationDeniedException, AlreadyExistsException;

  PreservationMetadata createPreservationMetadata(PreservationMetadata.PreservationMetadataType type, String id,
    String aipId, String representationId, List<String> fileDirectoryPath, String fileId, ContentPayload payload,
    String createdBy, boolean notify) throws GenericException, NotFoundException, RequestNotValidException,
    AuthorizationDeniedException, AlreadyExistsException;

  PreservationMetadata updatePreservationMetadata(PreservationMetadata.PreservationMetadataType type, String id,
    ContentPayload payload, boolean notify)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException;

  PreservationMetadata updatePreservationMetadata(String id, PreservationMetadata.PreservationMetadataType type,
    String aipId, String representationId, List<String> fileDirectoryPath, String fileId, ContentPayload payload,
    String updatedBy, boolean notify)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException;

  void deletePreservationMetadata(PreservationMetadata pm, boolean notify)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException;

  void deletePreservationMetadata(PreservationMetadata.PreservationMetadataType type, String aipId,
    String representationId, String id, List<String> filePath, boolean notify)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException;

  CloseableIterable<OptionalWithCause<PreservationMetadata>> listPreservationMetadata()
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;

  CloseableIterable<OptionalWithCause<PreservationMetadata>> listPreservationMetadata(String aipId,
    boolean includeRepresentations)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;

  CloseableIterable<OptionalWithCause<PreservationMetadata>> listPreservationMetadata(String aipId,
    String representationId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;

  CloseableIterable<OptionalWithCause<PreservationMetadata>> listPreservationAgents()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException;

  CloseableIterable<OptionalWithCause<PreservationMetadata>> listPreservationRepositoryEvents()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException;

  Binary retrieveOtherMetadataBinary(OtherMetadata om)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  Binary retrieveOtherMetadataBinary(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId, String fileSuffix, String type)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  OtherMetadata retrieveOtherMetadata(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId, String fileSuffix, String type)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  OtherMetadata createOrUpdateOtherMetadata(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId, String fileSuffix, String type, ContentPayload payload, String username, boolean notify)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  void deleteOtherMetadata(String aipId, String representationId, List<String> fileDirectoryPath, String fileId,
    String fileSuffix, String type, String username)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException;

  CloseableIterable<OptionalWithCause<OtherMetadata>> listOtherMetadata(String aipId, String type,
    boolean includeRepresentations)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;

  CloseableIterable<OptionalWithCause<OtherMetadata>> listOtherMetadata(String aipId, String representationId)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException;

  CloseableIterable<OptionalWithCause<OtherMetadata>> listOtherMetadata(String aipId, String representationId,
    List<String> filePath, String fileId, String type)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;

  List<LogEntry> importLogEntries(InputStream inputStream, String filename) throws AuthorizationDeniedException,
    GenericException, AlreadyExistsException, RequestNotValidException, NotFoundException;

  void addLogEntry(LogEntry logEntry, Path logDirectory, boolean notify)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException;

  void addLogEntry(LogEntry logEntry, Path logDirectory)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException;

  void findOldLogsAndSendThemToMaster(Path logDirectory, Path currentLogFile) throws IOException;

  void findOldLogsAndMoveThemToStorage(Path logDirectory, Path currentLogFile)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, IOException;

  User retrieveAuthenticatedUser(String name, String password) throws GenericException, AuthenticationDeniedException;

  User retrieveUserByEmail(String email) throws GenericException;

  User registerUser(User user, SecureString password, boolean notify)
    throws GenericException, UserAlreadyExistsException, EmailAlreadyExistsException, AuthorizationDeniedException;

  User createUser(User user, boolean notify) throws GenericException, EmailAlreadyExistsException,
    UserAlreadyExistsException, IllegalOperationException, NotFoundException, AuthorizationDeniedException;

  User createUser(User user, SecureString password, boolean notify)
    throws EmailAlreadyExistsException, UserAlreadyExistsException, IllegalOperationException, GenericException,
    NotFoundException, AuthorizationDeniedException;

  User createUser(User user, SecureString password, boolean notify, boolean isHandlingEvent)
    throws GenericException, EmailAlreadyExistsException, UserAlreadyExistsException, IllegalOperationException,
    NotFoundException, AuthorizationDeniedException;

  User updateUser(User user, SecureString password, boolean notify)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException;

  User updateUser(User user, SecureString password, boolean notify, boolean isHandlingEvent)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException;

  User deActivateUser(String id, boolean activate, boolean notify)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException;

  User deActivateUser(String id, boolean activate, boolean notify, boolean isHandlingEvent)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException;

  User updateMyUser(User user, SecureString password, boolean notify)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException;

  User updateMyUser(User user, SecureString password, boolean notify, boolean isHandlingEvent)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException;

  void deleteUser(String id, boolean notify) throws GenericException, AuthorizationDeniedException;

  void deleteUser(String id, boolean notify, boolean isHandlingEvent)
    throws GenericException, AuthorizationDeniedException;

  List<User> listUsers() throws GenericException;

  User retrieveUser(String name) throws GenericException;

  String retrieveExtraLdap(String name) throws GenericException;

  Group retrieveGroup(String name) throws GenericException, NotFoundException;

  Group createGroup(Group group, boolean notify)
    throws GenericException, AlreadyExistsException, AuthorizationDeniedException;

  Group createGroup(Group group, boolean notify, boolean isHandlingEvent)
    throws GenericException, AlreadyExistsException, AuthorizationDeniedException;

  Group updateGroup(Group group, boolean notify)
    throws GenericException, NotFoundException, AuthorizationDeniedException;

  Group updateGroup(Group group, boolean notify, boolean isHandlingEvent)
    throws GenericException, NotFoundException, AuthorizationDeniedException;

  void deleteGroup(String id, boolean notify) throws GenericException, AuthorizationDeniedException;

  void deleteGroup(String id, boolean notify, boolean isHandlingEvent)
    throws GenericException, AuthorizationDeniedException;

  List<Group> listGroups() throws GenericException;

  User confirmUserEmail(String username, String email, String emailConfirmationToken, boolean useModel, boolean notify)
    throws NotFoundException, InvalidTokenException, GenericException;

  User requestPasswordReset(String username, String email, boolean useModel, boolean notify)
    throws IllegalOperationException, NotFoundException, GenericException, AuthorizationDeniedException;

  User resetUserPassword(String username, SecureString password, String resetPasswordToken, boolean useModel,
    boolean notify) throws NotFoundException, InvalidTokenException, IllegalOperationException, GenericException,
    AuthorizationDeniedException;

  void createJob(Job job)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException;

  void createOrUpdateJob(Job job)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  Job retrieveJob(String jobId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  CloseableIterable<OptionalWithCause<Report>> listJobReports(String jobId)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException;

  void deleteJob(String jobId)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException;

  Report retrieveJobReport(String jobId, String jobReportId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  Report retrieveJobReport(String jobId, String sourceObjectId, String outcomeObjectId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  void createOrUpdateJobReport(Report jobReport, Job cachedJob) throws GenericException, AuthorizationDeniedException;

  void createOrUpdateJobReport(Report jobReport, IndexedJob indexJob)
    throws GenericException, AuthorizationDeniedException;

  void deleteJobReport(String jobId, String jobReportId)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException;

  void updateAIPPermissions(String aipId, Permissions permissions, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException;

  void updateAIPPermissions(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException;

  void updateDIPPermissions(DIP dip)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException;

  void deleteTransferredResource(TransferredResource transferredResource)
    throws GenericException, AuthorizationDeniedException;

  Job updateJobInstanceId(Job job)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException;

  Risk createRisk(Risk risk, boolean commit) throws GenericException, AuthorizationDeniedException;

  Risk updateRiskInstanceId(Risk risk, boolean commit) throws GenericException, AuthorizationDeniedException;

  Risk updateRisk(Risk risk, Map<String, String> properties, boolean commit, int incidences)
    throws GenericException, AuthorizationDeniedException;

  void deleteRisk(String riskId, boolean commit)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException;

  Risk retrieveRisk(String riskId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  BinaryVersion retrieveVersion(String riskId, String versionId)
    throws RequestNotValidException, GenericException, NotFoundException;

  BinaryVersion revertRiskVersion(String riskId, String versionId, Map<String, String> properties, boolean commit,
    int incidences) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  RiskIncidence createRiskIncidence(RiskIncidence riskIncidence, boolean commit)
    throws AlreadyExistsException, NotFoundException, AuthorizationDeniedException, GenericException;

  RiskIncidence updateRiskIncidenceInstanceId(RiskIncidence riskIncidence, boolean commit)
    throws GenericException, AuthorizationDeniedException;

  RiskIncidence updateRiskIncidence(RiskIncidence riskIncidence, boolean commit)
    throws GenericException, AuthorizationDeniedException;

  void deleteRiskIncidence(String riskIncidenceId, boolean commit)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException;

  RiskIncidence retrieveRiskIncidence(String incidenceId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  Notification createNotification(Notification notification, NotificationProcessor processor)
    throws GenericException, AuthorizationDeniedException;

  Notification updateNotificationInstanceId(Notification notification)
    throws GenericException, NotFoundException, AuthorizationDeniedException;

  Notification updateNotification(Notification notification)
    throws GenericException, NotFoundException, AuthorizationDeniedException;

  void deleteNotification(String notificationId)
    throws GenericException, NotFoundException, AuthorizationDeniedException;

  Notification retrieveNotification(String notificationId)
    throws GenericException, NotFoundException, AuthorizationDeniedException;

  Notification acknowledgeNotification(String notificationId, String token)
    throws GenericException, NotFoundException, AuthorizationDeniedException;

  CloseableIterable<OptionalWithCause<DIPFile>> listDIPFilesUnder(DIPFile f, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException;

  CloseableIterable<OptionalWithCause<DIPFile>> listDIPFilesUnder(String dipId, List<String> directoryPath,
    String fileId, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException;

  CloseableIterable<OptionalWithCause<DIPFile>> listDIPFilesUnder(String dipId, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException;

  void updateDIPInstanceId(DIP dip)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException;

  DIP createDIP(DIP dip, boolean notify) throws GenericException, AuthorizationDeniedException;

  DIP updateDIP(DIP dip) throws GenericException, NotFoundException, AuthorizationDeniedException;

  void deleteDIP(String dipId) throws GenericException, NotFoundException, AuthorizationDeniedException;

  DIP retrieveDIP(String dipId) throws GenericException, NotFoundException, AuthorizationDeniedException;

  DIPFile createDIPFile(String dipId, List<String> directoryPath, String fileId, long size,
    ContentPayload contentPayload, boolean notify) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException;

  DIPFile createDIPFile(String dipId, List<String> directoryPath, String fileId, String dirName, boolean notify)
    throws RequestNotValidException, GenericException, AlreadyExistsException, AuthorizationDeniedException;

  DIPFile updateDIPFile(String dipId, List<String> directoryPath, String oldFileId, String fileId, long size,
    ContentPayload contentPayload, boolean createIfNotExists, boolean notify) throws RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException, AlreadyExistsException;

  void deleteDIPFile(String dipId, List<String> directoryPath, String fileId, boolean notify)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;

  DIPFile retrieveDIPFile(String dipId, List<String> directoryPath, String fileId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  Directory getSubmissionDirectory(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;

  void createSubmission(StorageService submissionStorage, StoragePath submissionStoragePath, String aipId)
    throws AlreadyExistsException, GenericException, RequestNotValidException, NotFoundException,
    AuthorizationDeniedException;

  void createSubmission(Path submissionPath, String aipId) throws AlreadyExistsException, GenericException,
    RequestNotValidException, NotFoundException, AuthorizationDeniedException;

  Directory getDocumentationDirectory(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;

  Directory getDocumentationDirectory(String aipId, String representationId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;

  File createDocumentation(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException;

  Long countDocumentationFiles(String aipId, String representationId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException;

  Long countSubmissionFiles(String aipId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException;

  Long countSchemaFiles(String aipId, String representationId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException;

  Directory getSchemasDirectory(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;

  Directory getSchemasDirectory(String aipId, String representationId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;

  File createSchema(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException;

  boolean checkIfSchemaExists(String aipId, String representationId, List<String> directoryPath, String fileId)
    throws RequestNotValidException;

  <T extends IsRODAObject> Optional<LiteRODAObject> retrieveLiteFromObject(T object);

  <T extends IsModelObject> OptionalWithCause<T> retrieveObjectFromLite(LiteRODAObject liteRODAObject);

  TransferredResource retrieveTransferredResource(String fullPath);

  <T extends IsRODAObject> CloseableIterable<OptionalWithCause<T>> list(Class<T> objectClass) throws RODAException;

  <T extends IsRODAObject> CloseableIterable<OptionalWithCause<LiteRODAObject>> listLite(Class<T> objectClass)
    throws RODAException;

  CloseableIterable<OptionalWithCause<LogEntry>> listLogEntries();

  CloseableIterable<OptionalWithCause<LogEntry>> listLogEntries(int daysToIndex);

  CloseableIterable<Resource> listLogFilesInStorage();

  boolean hasObjects(Class<? extends IsRODAObject> objectClass);

  boolean checkObjectPermission(String username, String permissionType, String objectClass, String id)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException;

  RepresentationInformation createRepresentationInformation(RepresentationInformation ri, String createdBy,
    boolean commit) throws GenericException, AuthorizationDeniedException;

  RepresentationInformation updateRepresentationInformation(RepresentationInformation ri, String updatedBy,
    boolean commit) throws GenericException, AuthorizationDeniedException;

  RepresentationInformation updateRepresentationInformationInstanceId(RepresentationInformation ri, String updatedBy,
    boolean notify) throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException;

  void deleteRepresentationInformation(String representationInformationId, boolean commit)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException;

  RepresentationInformation retrieveRepresentationInformation(String representationInformationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  DisposalHold retrieveDisposalHold(String disposalHoldId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;

  DisposalHold createDisposalHold(DisposalHold disposalHold, String createdBy) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException, AlreadyExistsException, NotFoundException;

  DisposalHold updateDisposalHoldFirstUseDate(DisposalHold disposalHold, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, IllegalOperationException,
    GenericException;

  DisposalHold updateDisposalHold(DisposalHold disposalHold, String updatedBy, String details)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, IllegalOperationException,
    GenericException;

  DisposalHold updateDisposalHold(DisposalHold disposalHold, String updatedBy, boolean updateFirstUseDate,
    String details) throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException,
    IllegalOperationException;

  void deleteDisposalHold(String disposalHoldId) throws RequestNotValidException, NotFoundException, GenericException,
    AuthorizationDeniedException, IllegalOperationException;

  DisposalHolds listDisposalHolds()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, IOException;

  DisposalAIPMetadata createDisposalHoldAssociation(String aipId, String disposalHoldId, Date associatedOn,
    String associatedBy)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  List<DisposalHold> retrieveDirectActiveDisposalHolds(String aipId)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException;

  boolean onDisposalHold(String aipId)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException;

  boolean isAIPOnDirectHold(String aipId, String holdId)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException;

  DisposalSchedule createDisposalSchedule(DisposalSchedule disposalSchedule, String createdBy)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException;

  DisposalSchedule updateDisposalSchedule(DisposalSchedule disposalSchedule, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException,
    IllegalOperationException;

  DisposalSchedule retrieveDisposalSchedule(String disposalScheduleId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  DisposalSchedules listDisposalSchedules()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, IOException;

  void deleteDisposalSchedule(String disposalScheduleId) throws NotFoundException, GenericException,
    AuthorizationDeniedException, RequestNotValidException, IllegalOperationException;

  DisposalConfirmation retrieveDisposalConfirmation(String disposalConfirmationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  void addDisposalHoldEntry(String disposalConfirmationId, DisposalHold disposalHold)
    throws GenericException, RequestNotValidException;

  void addDisposalHoldTransitiveEntry(String disposalConfirmationId, DisposalHold transitiveDisposalHold)
    throws RequestNotValidException, GenericException;

  void addDisposalScheduleEntry(String disposalConfirmationId, DisposalSchedule disposalSchedule)
    throws RequestNotValidException, GenericException;

  void addAIPEntry(String disposalConfirmationId, DisposalConfirmationAIPEntry entry)
    throws RequestNotValidException, GenericException;

  DisposalConfirmation updateDisposalConfirmation(DisposalConfirmation disposalConfirmation)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException;

  DisposalConfirmation createDisposalConfirmation(DisposalConfirmation disposalConfirmation, String createdBy)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException;

  void deleteDisposalConfirmation(String disposalConfirmationId) throws AuthorizationDeniedException,
    RequestNotValidException, NotFoundException, GenericException, IllegalOperationException;

  DisposalHoldsAIPMetadata listDisposalHoldsAssociation(String aipId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  DisposalTransitiveHoldsAIPMetadata listTransitiveDisposalHolds(String aipId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  DisposalRule createDisposalRule(DisposalRule disposalRule, String createdBy) throws RequestNotValidException,
    NotFoundException, GenericException, AlreadyExistsException, AuthorizationDeniedException;

  DisposalRule updateDisposalRule(DisposalRule disposalRule, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException;

  void deleteDisposalRule(String disposalRuleId, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, IOException, GenericException, NotFoundException;

  DisposalRule retrieveDisposalRule(String disposalRuleId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  DisposalRules listDisposalRules()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, IOException;

  DistributedInstance createDistributedInstance(DistributedInstance distributedInstance, String createdBy)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, AlreadyExistsException,
    NotFoundException, IllegalOperationException;

  DistributedInstances listDistributedInstances()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, IOException;

  DistributedInstance retrieveDistributedInstance(String distributedInstanceId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  void deleteDistributedInstance(String distributedInstanceId)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException;

  DistributedInstance updateDistributedInstance(DistributedInstance distributedInstance, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException;

  AccessKey createAccessKey(AccessKey accessKey, String createdBy) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException, AlreadyExistsException, NotFoundException;

  AccessKeys listAccessKeys() throws RequestNotValidException, AuthorizationDeniedException, GenericException;

  AccessKey retrieveAccessKey(String accessKeyId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  AccessKey updateAccessKey(AccessKey accessKey, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException;

  void deleteAccessKey(String accessKeyId)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException;

  void updateAccessKeyLastUsageDate(AccessKey accessKey)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  AccessKeys listAccessKeysByUser(String userId)
    throws RequestNotValidException, AuthorizationDeniedException, GenericException;

  void deactivateUserAccessKeys(String userId, String updatedBy)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException;

  void deleteUserAccessKeys(String userId, String updatedBy)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException;

  StorageService resolveTemporaryResourceShallow(String jobId, IsRODAObject object, String... pathPartials)
    throws GenericException, RequestNotValidException;

  StorageService resolveTemporaryResourceShallow(String jobId, StorageService storage, IsRODAObject object,
    String... pathPartials) throws GenericException, RequestNotValidException;

  StorageService resolveTemporaryResourceShallow(String jobId, LiteRODAObject object, String... pathPartials)
    throws GenericException, RequestNotValidException;

  StorageService resolveTemporaryResourceShallow(String jobId, StorageService storage, LiteRODAObject object,
    String... pathPartials) throws GenericException, RequestNotValidException;

  Binary getBinary(IsRODAObject object, String... pathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException;

  Binary getBinary(LiteRODAObject lite, String... pathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException;

  BinaryVersion getBinaryVersion(IsRODAObject object, String version, List<String> pathPartials)
    throws RequestNotValidException, NotFoundException, GenericException;

  BinaryVersion getBinaryVersion(LiteRODAObject lite, String version, List<String> pathPartials)
    throws RequestNotValidException, NotFoundException, GenericException;

  CloseableIterable<BinaryVersion> listBinaryVersions(IsRODAObject object)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException;

  CloseableIterable<BinaryVersion> listBinaryVersions(LiteRODAObject lite)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException;

  void deleteBinaryVersion(IsRODAObject object, String version)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException;

  void deleteBinaryVersion(LiteRODAObject lite, String version)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException;

  Binary updateBinaryContent(IsRODAObject object, ContentPayload payload, boolean asReference,
    boolean createIfNotExists)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException;

  Binary updateBinaryContent(LiteRODAObject lite, ContentPayload payload, boolean asReference,
    boolean createIfNotExists)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException;

  Directory createDirectory(IsRODAObject object, String... pathPartials)
    throws AuthorizationDeniedException, AlreadyExistsException, GenericException, RequestNotValidException;

  Directory createDirectory(LiteRODAObject lite, String... pathPartials)
    throws AuthorizationDeniedException, AlreadyExistsException, GenericException, RequestNotValidException;

  boolean hasDirectory(IsRODAObject object, String... pathPartials) throws RequestNotValidException;

  boolean hasDirectory(LiteRODAObject object, String... pathPartials) throws RequestNotValidException, GenericException;

  DirectResourceAccess getDirectAccess(IsRODAObject obj, StorageService storage, String... pathPartials)
    throws RequestNotValidException;

  DirectResourceAccess getDirectAccess(LiteRODAObject liteObj, StorageService storage, String... pathPartials)
    throws RequestNotValidException, GenericException;

  DirectResourceAccess getDirectAccess(IsRODAObject obj, String... pathPartials) throws RequestNotValidException;

  DirectResourceAccess getDirectAccess(LiteRODAObject liteObj, String... pathPartials)
    throws RequestNotValidException, GenericException;

  int importAll(IndexService index, final FileStorageService fromStorage, final boolean importJobs)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException;

  void exportAll(StorageService toStorage);

  void importObject(IsRODAObject object, StorageService fromStorage);

  void exportObject(IsRODAObject object, StorageService toStorage, String... toPathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException, NotFoundException,
    GenericException;

  void exportObject(LiteRODAObject lite, StorageService toStorage, String... toPathPartials)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, AlreadyExistsException,
    NotFoundException;

  void exportToPath(IsRODAObject object, Path toPath, boolean replaceExisting, String... fromPathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException, GenericException;

  void exportToPath(LiteRODAObject lite, Path toPath, boolean replaceExisting, String... fromPathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException, GenericException;

  ConsumesOutputStream exportObjectToStream(IsRODAObject object, String... pathPartials)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException;

  ConsumesOutputStream exportObjectToStream(LiteRODAObject lite, String... pathPartials)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException;

  ConsumesOutputStream exportObjectToStream(IsRODAObject object, String name, boolean addTopDirectory,
    String... pathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException;

  ConsumesOutputStream exportObjectToStream(LiteRODAObject lite, String name, boolean addTopDirectory,
    String... pathPartials)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException;

  void moveObject(LiteRODAObject fromPath, LiteRODAObject toPath) throws AuthorizationDeniedException,
    RequestNotValidException, AlreadyExistsException, NotFoundException, GenericException;

  String getObjectPathAsString(IsRODAObject object, boolean skipContainer) throws RequestNotValidException;

  String getObjectPathAsString(LiteRODAObject lite, boolean skipContainer)
    throws RequestNotValidException, GenericException;

  boolean existsInStorage(LiteRODAObject lite, String... pathPartials)
    throws RequestNotValidException, GenericException;

  Date retrieveFileCreationDate(File file) throws RequestNotValidException, GenericException;

  Date retrievePreservationMetadataCreationDate(PreservationMetadata pm)
    throws RequestNotValidException, GenericException;
}
