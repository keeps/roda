package org.roda.core.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.roda.core.data.exceptions.LockingException;
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
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
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
import org.roda.core.entity.transaction.TransactionLog;
import org.roda.core.entity.transaction.TransactionalModelOperationLog;
import org.roda.core.index.IndexService;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryVersion;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.Directory;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.storage.utils.RODAInstanceUtils;
import org.roda.core.transaction.RODATransactionException;
import org.roda.core.transaction.TransactionLogService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DefaultTransactionalModelService implements TransactionalModelService {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTransactionalModelService.class);
  private final ModelService mainModelService;
  private final ModelService stagingModelService;
  private final TransactionLog transaction;
  private final TransactionLogService transactionLogService;

  public DefaultTransactionalModelService(ModelService mainModelService, ModelService stagingModelService,
    TransactionLog transaction, TransactionLogService transactionLogService) {
    this.mainModelService = mainModelService;
    this.stagingModelService = stagingModelService;
    this.transaction = transaction;
    this.transactionLogService = transactionLogService;
  }

  private ModelService getModelService() {
    return stagingModelService;
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

    registerOperationForAIP(aipId, TransactionalModelOperationLog.OperationType.READ);

    AIP aip;
    try {
      aip = stagingModelService.retrieveAIP(aipId);
    } catch (NotFoundException e) {
      aip = mainModelService.retrieveAIP(aipId);
    }
    return aip;
  }

  @Override
  public AIP createAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath, boolean notify,
    String createdBy) throws RequestNotValidException, GenericException, AuthorizationDeniedException,
    AlreadyExistsException, NotFoundException, ValidationException {
    registerOperationForAIP(aipId, TransactionalModelOperationLog.OperationType.CREATE);
    return getModelService().createAIP(aipId, sourceStorage, sourcePath, notify, createdBy);
  }

  @Override
  public AIP createAIP(String parentId, String type, Permissions permissions, List<String> ingestSIPIds,
    String ingestJobId, boolean notify, String createdBy, boolean isGhost) throws RequestNotValidException,
    NotFoundException, GenericException, AlreadyExistsException, AuthorizationDeniedException {
    AIP aip = getModelService().createAIP(parentId, type, permissions, ingestSIPIds, ingestJobId, notify, createdBy,
      isGhost);
    registerOperationForAIP(aip.getId(), TransactionalModelOperationLog.OperationType.CREATE);
    return aip;
  }

  @Override
  public AIP createAIP(String parentId, String type, Permissions permissions, String createdBy)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {
    AIP aip = getModelService().createAIP(parentId, type, permissions, createdBy);
    registerOperationForAIP(aip.getId(), TransactionalModelOperationLog.OperationType.CREATE);
    return aip;
  }

  @Override
  public AIP createAIP(AIPState state, String parentId, String type, Permissions permissions, String createdBy)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {
    AIP aip = getModelService().createAIP(state, parentId, type, permissions, createdBy);
    registerOperationForAIP(aip.getId(), TransactionalModelOperationLog.OperationType.CREATE);
    return aip;
  }

  @Override
  public AIP createAIP(AIPState state, String parentId, String type, Permissions permissions, boolean notify,
    String createdBy) throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {
    AIP aip = getModelService().createAIP(state, parentId, type, permissions, notify, createdBy);
    registerOperationForAIP(aip.getId(), TransactionalModelOperationLog.OperationType.CREATE);
    return aip;
  }

  @Override
  public AIP createAIP(AIPState state, String parentId, String type, Permissions permissions, String ingestSIPUUID,
    List<String> ingestSIPIds, String ingestJobId, boolean notify, String createdBy) throws RequestNotValidException,
    NotFoundException, GenericException, AlreadyExistsException, AuthorizationDeniedException {
    AIP aip = getModelService().createAIP(state, parentId, type, permissions, ingestSIPUUID, ingestSIPIds, ingestJobId,
      notify, createdBy);
    registerOperationForAIP(aip.getId(), TransactionalModelOperationLog.OperationType.CREATE);
    return aip;
  }

  @Override
  public AIP createAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath, String createdBy)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, AlreadyExistsException,
    NotFoundException, ValidationException {
    AIP aip = getModelService().createAIP(aipId, sourceStorage, sourcePath, createdBy);
    registerOperationForAIP(aip.getId(), TransactionalModelOperationLog.OperationType.CREATE);
    return aip;
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
    registerOperationForAIP(aipId, TransactionalModelOperationLog.OperationType.UPDATE);
    return getModelService().updateAIP(aipId, sourceStorage, sourcePath, updatedBy);
  }

  @Override
  public AIP destroyAIP(AIP aip, String updatedBy)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    registerOperationForAIP(aip.getId(), TransactionalModelOperationLog.OperationType.DELETE);
    return getModelService().destroyAIP(aip, updatedBy);
  }

  @Override
  public AIP updateAIP(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    registerOperationForAIP(aip.getId(), TransactionalModelOperationLog.OperationType.UPDATE);
    return getModelService().updateAIP(aip, updatedBy);
  }

  @Override
  public AIP updateAIPState(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    registerOperationForAIP(aip.getId(), TransactionalModelOperationLog.OperationType.UPDATE);
    return getModelService().updateAIPState(aip, updatedBy);
  }

  @Override
  public AIP updateAIPInstanceId(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    registerOperationForAIP(aip.getId(), TransactionalModelOperationLog.OperationType.UPDATE);
    return getModelService().updateAIPInstanceId(aip, updatedBy);
  }

  @Override
  public AIP moveAIP(String aipId, String parentId, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    registerOperationForAIP(aipId, TransactionalModelOperationLog.OperationType.UPDATE);
    return getModelService().moveAIP(aipId, parentId, updatedBy);
  }

  @Override
  public void deleteAIP(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    registerOperationForAIP(aipId, TransactionalModelOperationLog.OperationType.DELETE);
    getModelService().deleteAIP(aipId);
  }

  @Override
  public void changeAIPType(String aipId, String type, String updatedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    registerOperationForAIP(aipId, TransactionalModelOperationLog.OperationType.UPDATE);
    getModelService().changeAIPType(aipId, type, updatedBy);
  }

  @Override
  public Binary retrieveDescriptiveMetadataBinary(String aipId, String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    registerOperationForDescriptiveMetadata(aipId, null, descriptiveMetadataId,
      TransactionalModelOperationLog.OperationType.READ);
    return getModelService().retrieveDescriptiveMetadataBinary(aipId, descriptiveMetadataId);
  }

  @Override
  public Binary retrieveDescriptiveMetadataBinary(String aipId, String representationId, String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    registerOperationForDescriptiveMetadata(aipId, representationId, descriptiveMetadataId,
      TransactionalModelOperationLog.OperationType.READ);
    return getModelService().retrieveDescriptiveMetadataBinary(aipId, representationId, descriptiveMetadataId);
  }

  @Override
  public DescriptiveMetadata retrieveDescriptiveMetadata(String aipId, String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    registerOperationForDescriptiveMetadata(aipId, null, descriptiveMetadataId,
      TransactionalModelOperationLog.OperationType.READ);
    return getModelService().retrieveDescriptiveMetadata(aipId, descriptiveMetadataId);
  }

  @Override
  public DescriptiveMetadata retrieveDescriptiveMetadata(String aipId, String representationId,
    String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    registerOperationForDescriptiveMetadata(aipId, representationId, descriptiveMetadataId,
      TransactionalModelOperationLog.OperationType.READ);
    return getModelService().retrieveDescriptiveMetadata(aipId, representationId, descriptiveMetadataId);
  }

  @Override
  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String descriptiveMetadataId,
    ContentPayload payload, String descriptiveMetadataType, String descriptiveMetadataVersion, String createdBy,
    boolean notify) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {
    registerOperationForDescriptiveMetadata(aipId, null, descriptiveMetadataId,
      TransactionalModelOperationLog.OperationType.CREATE);
    return getModelService().createDescriptiveMetadata(aipId, descriptiveMetadataId, payload, descriptiveMetadataType,
      descriptiveMetadataVersion, createdBy, notify);
  }

  @Override
  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String descriptiveMetadataId,
    ContentPayload payload, String descriptiveMetadataType, String descriptiveMetadataVersion, String createdBy)
    throws RequestNotValidException, GenericException, AlreadyExistsException, AuthorizationDeniedException,
    NotFoundException {
    registerOperationForDescriptiveMetadata(aipId, null, descriptiveMetadataId,
      TransactionalModelOperationLog.OperationType.CREATE);
    return getModelService().createDescriptiveMetadata(aipId, descriptiveMetadataId, payload, descriptiveMetadataType,
      descriptiveMetadataVersion, createdBy);
  }

  @Override
  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String representationId,
    String descriptiveMetadataId, ContentPayload payload, String descriptiveMetadataType,
    String descriptiveMetadataVersion, String createdBy) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    registerOperationForDescriptiveMetadata(aipId, representationId, descriptiveMetadataId,
      TransactionalModelOperationLog.OperationType.CREATE);
    return getModelService().createDescriptiveMetadata(aipId, representationId, descriptiveMetadataId, payload,
      descriptiveMetadataType, descriptiveMetadataVersion, createdBy);
  }

  @Override
  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String representationId,
    String descriptiveMetadataId, ContentPayload payload, String descriptiveMetadataType,
    String descriptiveMetadataVersion, String createdBy, boolean notify) throws RequestNotValidException,
    GenericException, AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    registerOperationForDescriptiveMetadata(aipId, representationId, descriptiveMetadataId,
      TransactionalModelOperationLog.OperationType.CREATE);
    return getModelService().createDescriptiveMetadata(aipId, representationId, descriptiveMetadataId, payload,
      descriptiveMetadataType, descriptiveMetadataVersion, createdBy, notify);
  }

  @Override
  public DescriptiveMetadata updateDescriptiveMetadata(String aipId, String descriptiveMetadataId,
    ContentPayload descriptiveMetadataPayload, String descriptiveMetadataType, String descriptiveMetadataVersion,
    Map<String, String> properties, String updatedBy)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    registerOperationForDescriptiveMetadata(aipId, null, descriptiveMetadataId,
      TransactionalModelOperationLog.OperationType.UPDATE);
    return getModelService().updateDescriptiveMetadata(aipId, descriptiveMetadataId, descriptiveMetadataPayload,
      descriptiveMetadataType, descriptiveMetadataVersion, properties, updatedBy);
  }

  @Override
  public DescriptiveMetadata updateDescriptiveMetadata(String aipId, String representationId,
    String descriptiveMetadataId, ContentPayload descriptiveMetadataPayload, String descriptiveMetadataType,
    String descriptiveMetadataVersion, Map<String, String> properties, String updatedBy)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    registerOperationForDescriptiveMetadata(aipId, representationId, descriptiveMetadataId,
      TransactionalModelOperationLog.OperationType.UPDATE);
    return getModelService().updateDescriptiveMetadata(aipId, representationId, descriptiveMetadataId,
      descriptiveMetadataPayload, descriptiveMetadataType, descriptiveMetadataVersion, properties, updatedBy);
  }

  @Override
  public void deleteDescriptiveMetadata(String aipId, String descriptiveMetadataId, String deletedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    registerOperationForDescriptiveMetadata(aipId, null, descriptiveMetadataId,
      TransactionalModelOperationLog.OperationType.DELETE);
    getModelService().deleteDescriptiveMetadata(aipId, descriptiveMetadataId, deletedBy);
  }

  @Override
  public void deleteDescriptiveMetadata(String aipId, String representationId, String descriptiveMetadataId,
    String deletedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    registerOperationForDescriptiveMetadata(aipId, representationId, descriptiveMetadataId,
      TransactionalModelOperationLog.OperationType.DELETE);
    getModelService().deleteDescriptiveMetadata(aipId, representationId, descriptiveMetadataId, deletedBy);
  }

  @Override
  public CloseableIterable<BinaryVersion> listDescriptiveMetadataVersions(String aipId, String representationId,
    String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    registerOperationForDescriptiveMetadata(aipId, representationId, descriptiveMetadataId,
      TransactionalModelOperationLog.OperationType.READ);
    return getModelService().listDescriptiveMetadataVersions(aipId, representationId, descriptiveMetadataId);
  }

  @Override
  public BinaryVersion revertDescriptiveMetadataVersion(String aipId, String descriptiveMetadataId, String versionId,
    Map<String, String> properties)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    registerOperationForDescriptiveMetadata(aipId, null, descriptiveMetadataId,
      TransactionalModelOperationLog.OperationType.UPDATE);
    return getModelService().revertDescriptiveMetadataVersion(aipId, descriptiveMetadataId, versionId, properties);
  }

  @Override
  public BinaryVersion revertDescriptiveMetadataVersion(String aipId, String representationId,
    String descriptiveMetadataId, String versionId, Map<String, String> properties)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    registerOperationForDescriptiveMetadata(aipId, representationId, descriptiveMetadataId,
      TransactionalModelOperationLog.OperationType.UPDATE);
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
    registerOperationForAIP(aipId, TransactionalModelOperationLog.OperationType.READ);
    return getModelService().listDescriptiveMetadata(aipId, includeRepresentations);
  }

  @Override
  public CloseableIterable<OptionalWithCause<DescriptiveMetadata>> listDescriptiveMetadata(String aipId,
    String representationId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    registerOperationForRepresentation(aipId, representationId, TransactionalModelOperationLog.OperationType.READ);
    return getModelService().listDescriptiveMetadata(aipId, representationId);
  }

  @Override
  public Representation retrieveRepresentation(String aipId, String representationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    registerOperationForRepresentation(aipId, representationId, TransactionalModelOperationLog.OperationType.READ);

    Representation representation;
    try {
      representation = stagingModelService.retrieveRepresentation(aipId, representationId);
    } catch (NotFoundException e) {
      representation = mainModelService.retrieveRepresentation(aipId, representationId);
    }
    return representation;
  }

  @Override
  public Representation createRepresentation(String aipId, String representationId, boolean original, String type,
    boolean notify, String createdBy, List<String> representationState) throws RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException, AlreadyExistsException {
    registerOperationForRepresentation(aipId, representationId, TransactionalModelOperationLog.OperationType.CREATE);
    return getModelService().createRepresentation(aipId, representationId, original, type, notify, createdBy,
      representationState);
  }

  @Override
  public Representation createRepresentation(String aipId, String representationId, boolean original, String type,
    boolean notify, String createdBy) throws RequestNotValidException, GenericException, NotFoundException,
    AuthorizationDeniedException, AlreadyExistsException {
    registerOperationForRepresentation(aipId, representationId, TransactionalModelOperationLog.OperationType.CREATE);
    return getModelService().createRepresentation(aipId, representationId, original, type, notify, createdBy);
  }

  @Override
  public Representation createRepresentation(String aipId, String representationId, boolean original, String type,
    StorageService sourceStorage, StoragePath sourcePath, boolean justData, String createdBy)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException,
    AlreadyExistsException {
    registerOperationForRepresentation(aipId, representationId, TransactionalModelOperationLog.OperationType.CREATE);
    return getModelService().createRepresentation(aipId, representationId, original, type, sourceStorage, sourcePath,
      justData, createdBy);
  }

  @Override
  public Representation updateRepresentationInfo(Representation representation) throws GenericException {
    registerOperationForRepresentation(representation.getAipId(), representation.getId(),
      TransactionalModelOperationLog.OperationType.UPDATE);
    return getModelService().updateRepresentationInfo(representation);
  }

  @Override
  public void changeRepresentationType(String aipId, String representationId, String type, String updatedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    registerOperationForRepresentation(aipId, representationId, TransactionalModelOperationLog.OperationType.UPDATE);
    getModelService().changeRepresentationType(aipId, representationId, type, updatedBy);
  }

  @Override
  public void changeRepresentationShallowFileFlag(String aipId, String representationId, boolean hasShallowFiles,
    String updatedBy, boolean notify)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    registerOperationForRepresentation(aipId, representationId, TransactionalModelOperationLog.OperationType.UPDATE);
    getModelService().changeRepresentationShallowFileFlag(aipId, representationId, hasShallowFiles, updatedBy, notify);
  }

  @Override
  public void changeRepresentationStates(String aipId, String representationId, List<String> newStates,
    String updatedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    registerOperationForRepresentation(aipId, representationId, TransactionalModelOperationLog.OperationType.UPDATE);
    getModelService().changeRepresentationStates(aipId, representationId, newStates, updatedBy);
  }

  @Override
  public Representation updateRepresentation(String aipId, String representationId, boolean original, String type,
    StorageService sourceStorage, StoragePath sourcePath, String updatedBy) throws RequestNotValidException,
    NotFoundException, GenericException, AuthorizationDeniedException, ValidationException {
    registerOperationForRepresentation(aipId, representationId, TransactionalModelOperationLog.OperationType.UPDATE);
    return getModelService().updateRepresentation(aipId, representationId, original, type, sourceStorage, sourcePath,
      updatedBy);
  }

  @Override
  public void deleteRepresentation(String aipId, String representationId, String username)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    registerOperationForRepresentation(aipId, representationId, TransactionalModelOperationLog.OperationType.DELETE);
    getModelService().deleteRepresentation(aipId, representationId, username);
  }

  @Override
  public CloseableIterable<OptionalWithCause<File>> listFilesUnder(String aipId, String representationId,
    boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    registerOperationForRepresentation(aipId, representationId, TransactionalModelOperationLog.OperationType.READ);
    return getModelService().listFilesUnder(aipId, representationId, recursive);
  }

  @Override
  public CloseableIterable<OptionalWithCause<File>> listExternalFilesUnder(File file)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    registerOperationForFile(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(),
      TransactionalModelOperationLog.OperationType.READ);
    return getModelService().listExternalFilesUnder(file);
  }

  @Override
  public CloseableIterable<OptionalWithCause<File>> listFilesUnder(File f, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    registerOperationForFile(f.getAipId(), f.getRepresentationId(), f.getPath(), f.getId(),
      TransactionalModelOperationLog.OperationType.READ);
    return getModelService().listFilesUnder(f, recursive);
  }

  @Override
  public CloseableIterable<OptionalWithCause<File>> listFilesUnder(String aipId, String representationId,
    List<String> directoryPath, String fileId, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    registerOperationForFile(aipId, representationId, directoryPath, fileId,
      TransactionalModelOperationLog.OperationType.READ);
    return getModelService().listFilesUnder(aipId, representationId, directoryPath, fileId, recursive);
  }

  @Override
  public Long getExternalFilesTotalSize(File file)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException, IOException {
    registerOperationForFile(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(),
      TransactionalModelOperationLog.OperationType.READ);
    return getModelService().getExternalFilesTotalSize(file);
  }

  @Override
  public File retrieveFile(String aipId, String representationId, List<String> directoryPath, String fileId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    registerOperationForFile(aipId, representationId, directoryPath, fileId,
      TransactionalModelOperationLog.OperationType.READ);
    return getModelService().retrieveFile(aipId, representationId, directoryPath, fileId);
  }

  @Override
  public File createFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload, String createdBy) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    registerOperationForFile(aipId, representationId, directoryPath, fileId,
      TransactionalModelOperationLog.OperationType.CREATE);
    return getModelService().createFile(aipId, representationId, directoryPath, fileId, contentPayload, createdBy);
  }

  @Override
  public File createFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload, String createdBy, boolean notify) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    registerOperationForFile(aipId, representationId, directoryPath, fileId,
      TransactionalModelOperationLog.OperationType.CREATE);
    return getModelService().createFile(aipId, representationId, directoryPath, fileId, contentPayload, createdBy,
      notify);
  }

  @Override
  public File createFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    String dirName, String createdBy, boolean notify) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    registerOperationForFile(aipId, representationId, directoryPath, fileId,
      TransactionalModelOperationLog.OperationType.CREATE);
    return getModelService().createFile(aipId, representationId, directoryPath, fileId, dirName, createdBy, notify);
  }

  @Override
  public File updateFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload, boolean createIfNotExists, String updatedBy, boolean notify)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    registerOperationForFile(aipId, representationId, directoryPath, fileId,
      TransactionalModelOperationLog.OperationType.UPDATE);
    return getModelService().updateFile(aipId, representationId, directoryPath, fileId, contentPayload,
      createIfNotExists, updatedBy, notify);
  }

  @Override
  public File updateFile(File file, ContentPayload contentPayload, boolean createIfNotExists, String updatedBy,
    boolean notify) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    registerOperationForFile(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(),
      TransactionalModelOperationLog.OperationType.UPDATE);
    return getModelService().updateFile(file, contentPayload, createIfNotExists, updatedBy, notify);
  }

  @Override
  public void deleteFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    String deletedBy, boolean notify)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    registerOperationForFile(aipId, representationId, directoryPath, fileId,
      TransactionalModelOperationLog.OperationType.DELETE);
    getModelService().deleteFile(aipId, representationId, directoryPath, fileId, deletedBy, notify);
  }

  @Override
  public void deleteFile(File file, String deletedBy, boolean notify)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    registerOperationForFile(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(),
      TransactionalModelOperationLog.OperationType.DELETE);
    getModelService().deleteFile(file, deletedBy, notify);
  }

  @Override
  public File renameFolder(File folder, String newName, boolean reindexResources) throws AlreadyExistsException,
    GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    registerOperationForFile(folder.getAipId(), folder.getRepresentationId(), folder.getPath(), folder.getId(),
      TransactionalModelOperationLog.OperationType.UPDATE);
    return getModelService().renameFolder(folder, newName, reindexResources);
  }

  @Override
  public File moveFile(File file, String newAipId, String newRepresentationId, List<String> newDirectoryPath,
    String newId, boolean reindexResources) throws AlreadyExistsException, GenericException, NotFoundException,
    RequestNotValidException, AuthorizationDeniedException {
    registerOperationForFile(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(),
      TransactionalModelOperationLog.OperationType.UPDATE);
    registerOperationForFile(newAipId, newRepresentationId, newDirectoryPath, newId,
      TransactionalModelOperationLog.OperationType.UPDATE);
    return getModelService().moveFile(file, newAipId, newRepresentationId, newDirectoryPath, newId, reindexResources);
  }

  @Override
  public PreservationMetadata createRepositoryEvent(RodaConstants.PreservationEventType eventType,
    String eventDescription, PluginState outcomeState, String outcomeText, String outcomeDetail, String agentName,
    boolean notify) {
    PreservationMetadata event = getModelService().createRepositoryEvent(eventType, eventDescription, outcomeState,
      outcomeText, outcomeDetail, agentName, notify);
    registerOperationForEvent(event, TransactionalModelOperationLog.OperationType.CREATE);
    return event;
  }

  @Override
  public PreservationMetadata createRepositoryEvent(RodaConstants.PreservationEventType eventType,
    String eventDescription, List<LinkingIdentifier> sources, List<LinkingIdentifier> targets, PluginState outcomeState,
    String outcomeText, String outcomeDetail, String agentName, boolean notify) {
    PreservationMetadata event = getModelService().createRepositoryEvent(eventType, eventDescription, sources, targets,
      outcomeState, outcomeText, outcomeDetail, agentName, notify);
    registerOperationForEvent(event, TransactionalModelOperationLog.OperationType.CREATE);
    return event;
  }

  @Override
  public PreservationMetadata createUpdateAIPEvent(String aipId, String representationId, List<String> filePath,
    String fileId, RodaConstants.PreservationEventType eventType, String eventDescription, PluginState outcomeState,
    String outcomeText, String outcomeDetail, String agentName, boolean notify) {
    PreservationMetadata event = getModelService().createUpdateAIPEvent(aipId, representationId, filePath, fileId,
      eventType, eventDescription, outcomeState, outcomeText, outcomeDetail, agentName, notify);
    registerOperationForEvent(event, TransactionalModelOperationLog.OperationType.UPDATE);
    return event;
  }

  @Override
  public PreservationMetadata createEvent(String aipId, String representationId, List<String> filePath, String fileId,
    RodaConstants.PreservationEventType eventType, String eventDescription, List<LinkingIdentifier> sources,
    List<LinkingIdentifier> targets, PluginState outcomeState, String outcomeText, String outcomeDetail,
    String agentName, boolean notify) {
    PreservationMetadata event = getModelService().createEvent(aipId, representationId, filePath, fileId, eventType,
      eventDescription, sources, targets, outcomeState, outcomeText, outcomeDetail, agentName, notify);
    registerOperationForEvent(event, TransactionalModelOperationLog.OperationType.CREATE);
    return event;
  }

  @Override
  public PreservationMetadata createEvent(String aipId, String representationId, List<String> filePath, String fileId,
    RodaConstants.PreservationEventType eventType, String eventDescription, List<LinkingIdentifier> sources,
    List<LinkingIdentifier> targets, PluginState outcomeState, String outcomeText, String outcomeDetail,
    String agentName, String agentRole, boolean notify) {
    PreservationMetadata event = getModelService().createEvent(aipId, representationId, filePath, fileId, eventType,
      eventDescription, sources, targets, outcomeState, outcomeText, outcomeDetail, agentName, agentRole, notify);
    registerOperationForEvent(event, TransactionalModelOperationLog.OperationType.CREATE);
    return event;
  }

  @Override
  public PreservationMetadata createEvent(String aipId, String representationId, List<String> filePath, String fileId,
    RodaConstants.PreservationEventType eventType, String eventDescription, List<LinkingIdentifier> sources,
    List<LinkingIdentifier> targets, PluginState outcomeState, String outcomeDetail, String outcomeExtension,
    List<LinkingIdentifier> agentIds, String username, boolean notify) throws GenericException, ValidationException,
    NotFoundException, RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException {
    PreservationMetadata event = getModelService().createEvent(aipId, representationId, filePath, fileId, eventType,
      eventDescription, sources, targets, outcomeState, outcomeDetail, outcomeExtension, agentIds, username, notify);
    registerOperationForEvent(event, TransactionalModelOperationLog.OperationType.CREATE);
    return event;
  }

  @Override
  public PreservationMetadata retrievePreservationMetadata(String id,
    PreservationMetadata.PreservationMetadataType type) {
    registerOperationForPreservationMetadata(id, TransactionalModelOperationLog.OperationType.READ);
    return getModelService().retrievePreservationMetadata(id, type);
  }

  @Override
  public PreservationMetadata retrievePreservationMetadata(String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId, PreservationMetadata.PreservationMetadataType type) {
    registerOperationForPreservationMetadata(aipId, representationId, fileDirectoryPath, fileId, type,
      TransactionalModelOperationLog.OperationType.READ);
    return getModelService().retrievePreservationMetadata(aipId, representationId, fileDirectoryPath, fileId, type);
  }

  @Override
  public Binary retrievePreservationRepresentation(String aipId, String representationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    registerOperationForPreservationMetadata(aipId, representationId,
      TransactionalModelOperationLog.OperationType.READ);
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
    registerOperationForPreservationMetadata(file, TransactionalModelOperationLog.OperationType.READ);
    return getModelService().retrievePreservationFile(file);
  }

  @Override
  public Binary retrievePreservationFile(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    String preservationID = IdUtils.getPreservationId(PreservationMetadata.PreservationMetadataType.FILE, aipId,
      representationId, fileDirectoryPath, fileId, RODAInstanceUtils.getLocalInstanceIdentifier());
    registerOperationForPreservationMetadata(aipId, representationId, fileDirectoryPath, fileId, preservationID,
      TransactionalModelOperationLog.OperationType.READ);
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
    registerOperationForPreservationMetadata(fileId, TransactionalModelOperationLog.OperationType.READ);
    return getModelService().retrieveRepositoryPreservationEvent(fileId);
  }

  @Override
  public Binary retrievePreservationEvent(String aipId, String representationId, List<String> filePath, String fileId,
    String preservationID)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    registerOperationForPreservationMetadata(aipId, representationId, filePath, fileId, preservationID,
      TransactionalModelOperationLog.OperationType.READ);
    return getModelService().retrievePreservationEvent(aipId, representationId, filePath, fileId, preservationID);
  }

  @Override
  public Binary retrievePreservationAgent(String preservationID)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    registerOperationForPreservationMetadata(preservationID, TransactionalModelOperationLog.OperationType.READ);
    return getModelService().retrievePreservationAgent(preservationID);
  }

  @Override
  public PreservationMetadata createPreservationMetadata(PreservationMetadata.PreservationMetadataType type,
    String aipId, String representationId, List<String> fileDirectoryPath, String fileId, ContentPayload payload,
    String username, boolean notify) throws GenericException, NotFoundException, RequestNotValidException,
    AuthorizationDeniedException, AlreadyExistsException {
    PreservationMetadata preservationMetadata = getModelService().createPreservationMetadata(type, aipId,
      representationId, fileDirectoryPath, fileId, payload, username, notify);
    registerOperationForPreservationMetadata(preservationMetadata, TransactionalModelOperationLog.OperationType.CREATE);
    return preservationMetadata;
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
    PreservationMetadata preservationMetadata = getModelService().createPreservationMetadata(type, aipId,
      fileDirectoryPath, fileId, payload, username, notify);
    registerOperationForPreservationMetadata(preservationMetadata, TransactionalModelOperationLog.OperationType.CREATE);
    return preservationMetadata;
  }

  @Override
  public PreservationMetadata createPreservationMetadata(PreservationMetadata.PreservationMetadataType type,
    String aipId, String representationId, ContentPayload payload, String username, boolean notify)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException,
    AlreadyExistsException {
    PreservationMetadata preservationMetadata = getModelService().createPreservationMetadata(type, aipId,
      representationId, payload, username, notify);
    registerOperationForPreservationMetadata(preservationMetadata, TransactionalModelOperationLog.OperationType.CREATE);
    return preservationMetadata;
  }

  @Override
  public PreservationMetadata createPreservationMetadata(PreservationMetadata.PreservationMetadataType type, String id,
    ContentPayload payload, boolean notify) throws GenericException, NotFoundException, RequestNotValidException,
    AuthorizationDeniedException, AlreadyExistsException {
    registerOperationForPreservationMetadata(id, TransactionalModelOperationLog.OperationType.CREATE);
    return getModelService().createPreservationMetadata(type, id, payload, notify);
  }

  @Override
  public PreservationMetadata createPreservationMetadata(PreservationMetadata.PreservationMetadataType type, String id,
    String aipId, String representationId, List<String> fileDirectoryPath, String fileId, ContentPayload payload,
    String createdBy, boolean notify) throws GenericException, NotFoundException, RequestNotValidException,
    AuthorizationDeniedException, AlreadyExistsException {
    registerOperationForPreservationMetadata(aipId, representationId, fileDirectoryPath, fileId, id,
      TransactionalModelOperationLog.OperationType.CREATE);
    return getModelService().createPreservationMetadata(type, id, aipId, representationId, fileDirectoryPath, fileId,
      payload, createdBy, notify);
  }

  @Override
  public PreservationMetadata updatePreservationMetadata(PreservationMetadata.PreservationMetadataType type, String id,
    ContentPayload payload, boolean notify)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    registerOperationForPreservationMetadata(id, TransactionalModelOperationLog.OperationType.UPDATE);
    return getModelService().updatePreservationMetadata(type, id, payload, notify);
  }

  @Override
  public PreservationMetadata updatePreservationMetadata(String id, PreservationMetadata.PreservationMetadataType type,
    String aipId, String representationId, List<String> fileDirectoryPath, String fileId, ContentPayload payload,
    String updatedBy, boolean notify)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    registerOperationForPreservationMetadata(aipId, representationId, fileDirectoryPath, fileId, id,
      TransactionalModelOperationLog.OperationType.UPDATE);
    return getModelService().updatePreservationMetadata(id, type, aipId, representationId, fileDirectoryPath, fileId,
      payload, updatedBy, notify);
  }

  @Override
  public void deletePreservationMetadata(PreservationMetadata pm, boolean notify)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    registerOperationForPreservationMetadata(pm, TransactionalModelOperationLog.OperationType.DELETE);
    getModelService().deletePreservationMetadata(pm, notify);
  }

  @Override
  public void deletePreservationMetadata(PreservationMetadata.PreservationMetadataType type, String aipId,
    String representationId, String id, List<String> filePath, boolean notify)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    registerOperationForPreservationMetadata(aipId, representationId, filePath, null, id,
      TransactionalModelOperationLog.OperationType.DELETE);
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
    registerOperationForOtherMetadata(om.getAipId(), TransactionalModelOperationLog.OperationType.READ);
    return getModelService().retrieveOtherMetadataBinary(om);
  }

  @Override
  public Binary retrieveOtherMetadataBinary(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId, String fileSuffix, String type)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    registerOperationForOtherMetadata(aipId, TransactionalModelOperationLog.OperationType.READ);
    return getModelService().retrieveOtherMetadataBinary(aipId, representationId, fileDirectoryPath, fileId, fileSuffix,
      type);
  }

  @Override
  public OtherMetadata retrieveOtherMetadata(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId, String fileSuffix, String type)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    registerOperationForOtherMetadata(aipId, TransactionalModelOperationLog.OperationType.READ);
    return getModelService().retrieveOtherMetadata(aipId, representationId, fileDirectoryPath, fileId, fileSuffix,
      type);
  }

  @Override
  public OtherMetadata createOrUpdateOtherMetadata(String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId, String fileSuffix, String type, ContentPayload payload,
    String username, boolean notify)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    registerOperationForOtherMetadata(aipId, TransactionalModelOperationLog.OperationType.UPDATE);
    return getModelService().createOrUpdateOtherMetadata(aipId, representationId, fileDirectoryPath, fileId, fileSuffix,
      type, payload, username, notify);
  }

  @Override
  public void deleteOtherMetadata(String aipId, String representationId, List<String> fileDirectoryPath, String fileId,
    String fileSuffix, String type, String username)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    registerOperationForOtherMetadata(aipId, TransactionalModelOperationLog.OperationType.DELETE);
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
    registerOperationForLogEntry(logEntry.getUUID(), TransactionalModelOperationLog.OperationType.CREATE);
    getModelService().addLogEntry(logEntry, logDirectory, notify);
  }

  @Override
  public void addLogEntry(LogEntry logEntry, Path logDirectory)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    registerOperationForLogEntry(logEntry.getUUID(), TransactionalModelOperationLog.OperationType.CREATE);
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
    return mainModelService.retrieveAuthenticatedUser(name, password);
  }

  @Override
  public User retrieveUserByEmail(String email) throws GenericException {
    return mainModelService.retrieveUserByEmail(email);
  }

  @Override
  public User registerUser(User user, SecureString password, boolean notify)
    throws GenericException, UserAlreadyExistsException, EmailAlreadyExistsException, AuthorizationDeniedException {
    return mainModelService.registerUser(user, password, notify);
  }

  @Override
  public User createUser(User user, boolean notify) throws GenericException, EmailAlreadyExistsException,
    UserAlreadyExistsException, IllegalOperationException, NotFoundException, AuthorizationDeniedException {
    return mainModelService.createUser(user, notify);
  }

  @Override
  public User createUser(User user, SecureString password, boolean notify)
    throws EmailAlreadyExistsException, UserAlreadyExistsException, IllegalOperationException, GenericException,
    NotFoundException, AuthorizationDeniedException {
    return mainModelService.createUser(user, password, notify);
  }

  @Override
  public User createUser(User user, SecureString password, boolean notify, boolean isHandlingEvent)
    throws GenericException, EmailAlreadyExistsException, UserAlreadyExistsException, IllegalOperationException,
    NotFoundException, AuthorizationDeniedException {
    return mainModelService.createUser(user, password, notify, isHandlingEvent);
  }

  @Override
  public User updateUser(User user, SecureString password, boolean notify)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    return mainModelService.updateUser(user, password, notify);
  }

  @Override
  public User updateUser(User user, SecureString password, boolean notify, boolean isHandlingEvent)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    return mainModelService.updateUser(user, password, notify, isHandlingEvent);
  }

  @Override
  public User deActivateUser(String id, boolean activate, boolean notify)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    return mainModelService.deActivateUser(id, activate, notify);
  }

  @Override
  public User deActivateUser(String id, boolean activate, boolean notify, boolean isHandlingEvent)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    return mainModelService.deActivateUser(id, activate, notify, isHandlingEvent);
  }

  @Override
  public User updateMyUser(User user, SecureString password, boolean notify)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    return mainModelService.updateMyUser(user, password, notify);
  }

  @Override
  public User updateMyUser(User user, SecureString password, boolean notify, boolean isHandlingEvent)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    return mainModelService.updateMyUser(user, password, notify, isHandlingEvent);
  }

  @Override
  public void deleteUser(String id, boolean notify) throws GenericException, AuthorizationDeniedException {
    mainModelService.deleteUser(id, notify);
  }

  @Override
  public void deleteUser(String id, boolean notify, boolean isHandlingEvent)
    throws GenericException, AuthorizationDeniedException {
    mainModelService.deleteUser(id, notify, isHandlingEvent);
  }

  @Override
  public List<User> listUsers() throws GenericException {
    return mainModelService.listUsers();
  }

  @Override
  public User retrieveUser(String name) throws GenericException {
    return mainModelService.retrieveUser(name);
  }

  @Override
  public String retrieveExtraLdap(String name) throws GenericException {
    return mainModelService.retrieveExtraLdap(name);
  }

  @Override
  public Group retrieveGroup(String name) throws GenericException, NotFoundException {
    return mainModelService.retrieveGroup(name);
  }

  @Override
  public Group createGroup(Group group, boolean notify)
    throws GenericException, AlreadyExistsException, AuthorizationDeniedException {
    return mainModelService.createGroup(group, notify);
  }

  @Override
  public Group createGroup(Group group, boolean notify, boolean isHandlingEvent)
    throws GenericException, AlreadyExistsException, AuthorizationDeniedException {
    return mainModelService.createGroup(group, notify, isHandlingEvent);
  }

  @Override
  public Group updateGroup(Group group, boolean notify)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    return mainModelService.updateGroup(group, notify);
  }

  @Override
  public Group updateGroup(Group group, boolean notify, boolean isHandlingEvent)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    return mainModelService.updateGroup(group, notify, isHandlingEvent);
  }

  @Override
  public void deleteGroup(String id, boolean notify) throws GenericException, AuthorizationDeniedException {
    mainModelService.deleteGroup(id, notify);
  }

  @Override
  public void deleteGroup(String id, boolean notify, boolean isHandlingEvent)
    throws GenericException, AuthorizationDeniedException {
    mainModelService.deleteGroup(id, notify, isHandlingEvent);
  }

  @Override
  public List<Group> listGroups() throws GenericException {
    return mainModelService.listGroups();
  }

  @Override
  public User confirmUserEmail(String username, String email, String emailConfirmationToken, boolean useModel,
    boolean notify) throws NotFoundException, InvalidTokenException, GenericException {
    return mainModelService.confirmUserEmail(username, email, emailConfirmationToken, useModel, notify);
  }

  @Override
  public User requestPasswordReset(String username, String email, boolean useModel, boolean notify)
    throws IllegalOperationException, NotFoundException, GenericException, AuthorizationDeniedException {
    return mainModelService.requestPasswordReset(username, email, useModel, notify);
  }

  @Override
  public User resetUserPassword(String username, SecureString password, String resetPasswordToken, boolean useModel,
    boolean notify) throws NotFoundException, InvalidTokenException, IllegalOperationException, GenericException,
    AuthorizationDeniedException {
    return mainModelService.resetUserPassword(username, password, resetPasswordToken, useModel, notify);
  }

  @Override
  public void createJob(Job job)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    mainModelService.createJob(job);
  }

  @Override
  public void createOrUpdateJob(Job job)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    mainModelService.createOrUpdateJob(job);
  }

  @Override
  public Job retrieveJob(String jobId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return mainModelService.retrieveJob(jobId);
  }

  @Override
  public CloseableIterable<OptionalWithCause<Report>> listJobReports(String jobId)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    registerOperation(Job.class, List.of(jobId), TransactionalModelOperationLog.OperationType.READ);
    return getModelService().listJobReports(jobId);
  }

  @Override
  public void deleteJob(String jobId)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    mainModelService.deleteJob(jobId);
  }

  @Override
  public Report retrieveJobReport(String jobId, String jobReportId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return mainModelService.retrieveJobReport(jobId, jobReportId);
  }

  @Override
  public Report retrieveJobReport(String jobId, String sourceObjectId, String outcomeObjectId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return mainModelService.retrieveJobReport(jobId, sourceObjectId, outcomeObjectId);
  }

  @Override
  public void createOrUpdateJobReport(Report jobReport, Job cachedJob)
    throws GenericException, AuthorizationDeniedException {
    mainModelService.createOrUpdateJobReport(jobReport, cachedJob);
  }

  @Override
  public void createOrUpdateJobReport(Report jobReport, IndexedJob indexJob)
    throws GenericException, AuthorizationDeniedException {
    mainModelService.createOrUpdateJobReport(jobReport, indexJob);
  }

  @Override
  public void deleteJobReport(String jobId, String jobReportId)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    mainModelService.deleteJobReport(jobId, jobReportId);
  }

  @Override
  public void updateAIPPermissions(String aipId, Permissions permissions, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    registerOperationForAIP(aipId, TransactionalModelOperationLog.OperationType.UPDATE);
    getModelService().updateAIPPermissions(aipId, permissions, updatedBy);
  }

  @Override
  public void updateAIPPermissions(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    registerOperationForAIP(aip.getId(), TransactionalModelOperationLog.OperationType.UPDATE);
    getModelService().updateAIPPermissions(aip, updatedBy);
  }

  @Override
  public void updateDIPPermissions(DIP dip)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    registerOperationForDIP(dip.getId(), TransactionalModelOperationLog.OperationType.UPDATE);
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
    return mainModelService.updateJobInstanceId(job);
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
    registerOperationForDIP(dip.getId(), TransactionalModelOperationLog.OperationType.UPDATE);
    getModelService().updateDIPInstanceId(dip);
  }

  @Override
  public DIP createDIP(DIP dip, boolean notify) throws GenericException, AuthorizationDeniedException {
    registerOperationForDIP(dip.getId(), TransactionalModelOperationLog.OperationType.CREATE);
    return getModelService().createDIP(dip, notify);
  }

  @Override
  public DIP updateDIP(DIP dip) throws GenericException, NotFoundException, AuthorizationDeniedException {
    registerOperationForDIP(dip.getId(), TransactionalModelOperationLog.OperationType.UPDATE);
    return getModelService().updateDIP(dip);
  }

  @Override
  public void deleteDIP(String dipId) throws GenericException, NotFoundException, AuthorizationDeniedException {
    DIP dip = getModelService().retrieveDIP(dipId);
    registerOperationForDIP(dip.getId(), TransactionalModelOperationLog.OperationType.DELETE);
    getModelService().deleteDIP(dipId);
  }

  @Override
  public DIP retrieveDIP(String dipId) throws GenericException, NotFoundException, AuthorizationDeniedException {
    DIP dip = getModelService().retrieveDIP(dipId);
    registerOperationForDIP(dip.getId(), TransactionalModelOperationLog.OperationType.READ);
    return dip;
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
  public Long countDocumentationFiles(String aipId, String representationId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    if (representationId == null) {
      registerOperationForAIP(aipId, TransactionalModelOperationLog.OperationType.READ);
    }
    else {
      registerOperationForRepresentation(aipId, representationId, TransactionalModelOperationLog.OperationType.READ);
    }
    return getModelService().countDocumentationFiles(aipId, representationId);
  }

  @Override
  public Long countSubmissionFiles(String aipId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    registerOperationForAIP(aipId, TransactionalModelOperationLog.OperationType.READ);
    return getModelService().countSubmissionFiles(aipId);
  }

  @Override
  public Long countSchemaFiles(String aipId, String representationId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    if (representationId == null) {
      registerOperationForAIP(aipId, TransactionalModelOperationLog.OperationType.READ);
    }
    else {
      registerOperationForRepresentation(aipId, representationId, TransactionalModelOperationLog.OperationType.READ);
    }
    return getModelService().countSchemaFiles(aipId, representationId);
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
  public CloseableIterable<Resource> listLogFilesInStorage() {
    registerOperation(LogEntry.class, List.of(), TransactionalModelOperationLog.OperationType.READ);
    return getModelService().listLogFilesInStorage();
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
  public StorageService resolveTemporaryResourceShallow(String jobId, IsRODAObject object, String... pathPartials)
    throws GenericException, RequestNotValidException {
    registerOperation(object.getClass(), List.of(object.getId()), TransactionalModelOperationLog.OperationType.READ);
    return getModelService().resolveTemporaryResourceShallow(jobId, object, pathPartials);
  }

  @Override
  public StorageService resolveTemporaryResourceShallow(String jobId, StorageService storage, IsRODAObject object,
    String... pathPartials) throws GenericException, RequestNotValidException {
    registerOperation(object.getClass(), List.of(object.getId()), TransactionalModelOperationLog.OperationType.READ);
    return getModelService().resolveTemporaryResourceShallow(jobId, storage, object, pathPartials);
  }

  @Override
  public StorageService resolveTemporaryResourceShallow(String jobId, LiteRODAObject object, String... pathPartials)
    throws GenericException, RequestNotValidException {
    registerOperation(object, TransactionalModelOperationLog.OperationType.READ);
    return getModelService().resolveTemporaryResourceShallow(jobId, object, pathPartials);
  }

  @Override
  public StorageService resolveTemporaryResourceShallow(String jobId, StorageService storage, LiteRODAObject object,
    String... pathPartials) throws GenericException, RequestNotValidException {
    registerOperation(object, TransactionalModelOperationLog.OperationType.READ);
    return getModelService().resolveTemporaryResourceShallow(jobId, storage, object, pathPartials);
  }

  @Override
  public Binary getBinary(IsRODAObject object, String... pathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    registerOperation(object.getClass(), List.of(object.getId()), TransactionalModelOperationLog.OperationType.READ);
    return getModelService().getBinary(object, pathPartials);
  }

  @Override
  public Binary getBinary(LiteRODAObject lite, String... pathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    registerOperation(lite, TransactionalModelOperationLog.OperationType.READ);
    return getModelService().getBinary(lite, pathPartials);
  }

  @Override
  public <T extends IsRODAObject> Binary getBinary(Class<T> entityClass, String... pathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    registerOperation(entityClass, List.of(), TransactionalModelOperationLog.OperationType.READ);
    return getModelService().getBinary(entityClass, pathPartials);
  }

  @Override
  public BinaryVersion getBinaryVersion(IsRODAObject object, String version, List<String> pathPartials)
    throws RequestNotValidException, NotFoundException, GenericException {
    registerOperation(object.getClass(), List.of(object.getId()), TransactionalModelOperationLog.OperationType.READ);
    return getModelService().getBinaryVersion(object, version, pathPartials);
  }

  @Override
  public BinaryVersion getBinaryVersion(LiteRODAObject lite, String version, List<String> pathPartials)
    throws RequestNotValidException, NotFoundException, GenericException {
    registerOperation(lite, TransactionalModelOperationLog.OperationType.READ);
    return getModelService().getBinaryVersion(lite, version, pathPartials);
  }

  @Override
  public CloseableIterable<BinaryVersion> listBinaryVersions(IsRODAObject object)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    registerOperation(object.getClass(), List.of(object.getId()), TransactionalModelOperationLog.OperationType.READ);
    return getModelService().listBinaryVersions(object);
  }

  @Override
  public CloseableIterable<BinaryVersion> listBinaryVersions(LiteRODAObject lite)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    registerOperation(lite, TransactionalModelOperationLog.OperationType.READ);
    return getModelService().listBinaryVersions(lite);
  }

  @Override
  public void deleteBinaryVersion(IsRODAObject object, String version)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    registerOperation(object.getClass(), List.of(object.getId()), TransactionalModelOperationLog.OperationType.DELETE);
    getModelService().deleteBinaryVersion(object, version);
  }

  @Override
  public void deleteBinaryVersion(LiteRODAObject lite, String version)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    registerOperation(lite, TransactionalModelOperationLog.OperationType.DELETE);
    getModelService().deleteBinaryVersion(lite, version);
  }

  @Override
  public Binary updateBinaryContent(IsRODAObject object, ContentPayload payload, boolean asReference,
    boolean createIfNotExists)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    registerOperation(object.getClass(), List.of(object.getId()), TransactionalModelOperationLog.OperationType.UPDATE);
    return getModelService().updateBinaryContent(object, payload, asReference, createIfNotExists);
  }

  @Override
  public Binary updateBinaryContent(LiteRODAObject lite, ContentPayload payload, boolean asReference,
    boolean createIfNotExists)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    registerOperation(lite, TransactionalModelOperationLog.OperationType.UPDATE);
    return getModelService().updateBinaryContent(lite, payload, asReference, createIfNotExists);
  }

  @Override
  public Directory createDirectory(IsRODAObject object, String... pathPartials)
    throws AuthorizationDeniedException, AlreadyExistsException, GenericException, RequestNotValidException {
    registerOperation(object.getClass(), List.of(object.getId()), TransactionalModelOperationLog.OperationType.UPDATE);
    return getModelService().createDirectory(object, pathPartials);
  }

  @Override
  public Directory createDirectory(LiteRODAObject lite, String... pathPartials)
    throws AuthorizationDeniedException, AlreadyExistsException, GenericException, RequestNotValidException {
    registerOperation(lite, TransactionalModelOperationLog.OperationType.UPDATE);
    return getModelService().createDirectory(lite, pathPartials);
  }

  @Override
  public boolean hasDirectory(IsRODAObject object, String... pathPartials) throws RequestNotValidException {
    registerOperation(object.getClass(), List.of(object.getId()), TransactionalModelOperationLog.OperationType.READ);
    return getModelService().hasDirectory(object, pathPartials);
  }

  @Override
  public boolean hasDirectory(LiteRODAObject lite, String... pathPartials)
    throws RequestNotValidException, GenericException {
    registerOperation(lite, TransactionalModelOperationLog.OperationType.READ);
    return getModelService().hasDirectory(lite, pathPartials);
  }

  @Override
  public DirectResourceAccess getDirectAccess(IsRODAObject object, StorageService storage, String... pathPartials)
    throws RequestNotValidException {
    registerOperation(object.getClass(), List.of(object.getId()), TransactionalModelOperationLog.OperationType.READ);
    return getModelService().getDirectAccess(object, storage, pathPartials);
  }

  @Override
  public DirectResourceAccess getDirectAccess(LiteRODAObject lite, StorageService storage, String... pathPartials)
    throws RequestNotValidException, GenericException {
    registerOperation(lite, TransactionalModelOperationLog.OperationType.READ);
    return getModelService().getDirectAccess(lite, storage, pathPartials);
  }

  @Override
  public DirectResourceAccess getDirectAccess(IsRODAObject object, String... pathPartials)
    throws RequestNotValidException {
    registerOperation(object.getClass(), List.of(object.getId()), TransactionalModelOperationLog.OperationType.READ);
    return getModelService().getDirectAccess(object, pathPartials);
  }

  @Override
  public DirectResourceAccess getDirectAccess(LiteRODAObject lite, String... pathPartials)
    throws RequestNotValidException, GenericException {
    registerOperation(lite, TransactionalModelOperationLog.OperationType.READ);
    return getModelService().getDirectAccess(lite, pathPartials);
  }

  @Override
  public DirectResourceAccess getDirectAccessToVersion(IsRODAObject object, String version, List<String> pathPartials)
    throws RequestNotValidException, GenericException {
    registerOperation(object.getClass(), List.of(object.getId()), TransactionalModelOperationLog.OperationType.READ);
    return getModelService().getDirectAccessToVersion(object, version, pathPartials);
  }

  @Override
  public DirectResourceAccess getDirectAccessToVersion(LiteRODAObject lite, String version, List<String> pathPartials)
    throws RequestNotValidException, GenericException {
    registerOperation(lite, TransactionalModelOperationLog.OperationType.READ);
    return getModelService().getDirectAccessToVersion(lite, version, pathPartials);
  }

  @Override
  public <T extends IsRODAObject> DirectResourceAccess getDirectAccess(Class<T> entityClass, String... pathPartials)
    throws RequestNotValidException {
    registerOperation(entityClass, List.of(), TransactionalModelOperationLog.OperationType.READ);
    return getModelService().getDirectAccess(entityClass, pathPartials);
  }

  @Override
  public int importAll(IndexService index, FileStorageService fromStorage, boolean importJobs)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException {
    return getModelService().importAll(index, fromStorage, importJobs);
  }

  @Override
  public void exportAll(StorageService toStorage) {
    getModelService().exportAll(toStorage);
  }

  @Override
  public void importObject(IsRODAObject object, StorageService fromStorage) {
    registerOperation(object.getClass(), List.of(object.getId()), TransactionalModelOperationLog.OperationType.UPDATE);
    getModelService().importObject(object, fromStorage);
  }

  @Override
  public void exportObject(IsRODAObject object, StorageService toStorage, String... toPathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException, NotFoundException,
    GenericException {
    registerOperation(object.getClass(), List.of(object.getId()), TransactionalModelOperationLog.OperationType.READ);
    getModelService().exportObject(object, toStorage, toPathPartials);
  }

  @Override
  public void exportObject(LiteRODAObject lite, StorageService toStorage, String... toPathPartials)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, AlreadyExistsException,
    NotFoundException {
    registerOperation(lite, TransactionalModelOperationLog.OperationType.READ);
    getModelService().exportObject(lite, toStorage, toPathPartials);
  }

  @Override
  public <T extends IsRODAObject> void exportToPath(Class<T> clazz, Path toPath, boolean replaceExisting, String... fromPathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException, GenericException {
    registerOperation(clazz, List.of(fromPathPartials), TransactionalModelOperationLog.OperationType.READ);
    getModelService().exportToPath(clazz, toPath, replaceExisting, fromPathPartials);
  }

  @Override
  public <T extends IsRODAObject> void exportToPath(IsRODAObject object, Path toPath, boolean replaceExisting, String... fromPathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException, GenericException {
    registerOperation(object.getClass(), List.of(object.getId()), TransactionalModelOperationLog.OperationType.READ);
    getModelService().exportToPath(object, toPath, replaceExisting, fromPathPartials);
  }

  @Override
  public void exportToPath(LiteRODAObject lite, Path toPath, boolean replaceExisting, String... fromPathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException, GenericException {
    registerOperation(lite, TransactionalModelOperationLog.OperationType.READ);
    getModelService().exportToPath(lite, toPath, replaceExisting, fromPathPartials);
  }

  @Override
  public ConsumesOutputStream exportObjectToStream(IsRODAObject object, String... pathPartials)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    registerOperation(object.getClass(), List.of(object.getId()), TransactionalModelOperationLog.OperationType.READ);
    return getModelService().exportObjectToStream(object, pathPartials);
  }

  @Override
  public ConsumesOutputStream exportObjectToStream(LiteRODAObject lite, String... pathPartials)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    registerOperation(lite, TransactionalModelOperationLog.OperationType.READ);
    return getModelService().exportObjectToStream(lite, pathPartials);
  }

  @Override
  public ConsumesOutputStream exportObjectToStream(IsRODAObject object, String name, boolean addTopDirectory,
    String... pathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    registerOperation(object.getClass(), List.of(object.getId()), TransactionalModelOperationLog.OperationType.READ);
    return getModelService().exportObjectToStream(object, name, addTopDirectory, pathPartials);
  }

  @Override
  public ConsumesOutputStream exportObjectToStream(LiteRODAObject lite, String name, boolean addTopDirectory,
    String... pathPartials)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    registerOperation(lite, TransactionalModelOperationLog.OperationType.READ);
    return getModelService().exportObjectToStream(lite, name, addTopDirectory, pathPartials);
  }

  @Override
  public void moveObject(LiteRODAObject fromPath, LiteRODAObject toPath) throws AuthorizationDeniedException,
    RequestNotValidException, AlreadyExistsException, NotFoundException, GenericException {
    registerOperation(fromPath, TransactionalModelOperationLog.OperationType.UPDATE);
    getModelService().moveObject(fromPath, toPath);
  }

  @Override
  public String getObjectPathAsString(IsRODAObject object, boolean skipContainer) throws RequestNotValidException {
    registerOperation(object.getClass(), List.of(object.getId()), TransactionalModelOperationLog.OperationType.READ);
    return getModelService().getObjectPathAsString(object, skipContainer);
  }

  @Override
  public String getObjectPathAsString(LiteRODAObject lite, boolean skipContainer)
    throws RequestNotValidException, GenericException {
    registerOperation(lite, TransactionalModelOperationLog.OperationType.READ);
    return getModelService().getObjectPathAsString(lite, skipContainer);
  }

  @Override
  public boolean existsInStorage(LiteRODAObject lite, String... pathPartials)
    throws RequestNotValidException, GenericException {
    registerOperation(lite, TransactionalModelOperationLog.OperationType.READ);
    return getModelService().existsInStorage(lite, pathPartials);
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
    return mainModelService.notifyJobCreatedOrUpdated(job, reindexJobReports);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyJobDeleted(String jobId) {
    return mainModelService.notifyJobDeleted(jobId);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyJobReportCreatedOrUpdated(Report jobReport, Job cachedJob) {
    return mainModelService.notifyJobReportCreatedOrUpdated(jobReport, cachedJob);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyJobReportCreatedOrUpdated(Report jobReport, IndexedJob indexedJob) {
    return mainModelService.notifyJobReportCreatedOrUpdated(jobReport, indexedJob);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyJobReportDeleted(String jobReportId) {
    return mainModelService.notifyJobReportDeleted(jobReportId);
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

  private void registerOperationForAIP(String aipID, TransactionalModelOperationLog.OperationType operation) {
    acquireLock(AIP.class, aipID);
    registerOperation(AIP.class, Arrays.asList(aipID), operation);
  }

  private void registerOperationForRelatedAIP(String aipID, TransactionalModelOperationLog.OperationType operation) {
    acquireLock(AIP.class, aipID);
    if (operation != TransactionalModelOperationLog.OperationType.READ) {
      registerOperation(AIP.class, Arrays.asList(aipID), TransactionalModelOperationLog.OperationType.UPDATE);
    } else {
      registerOperation(AIP.class, Arrays.asList(aipID), TransactionalModelOperationLog.OperationType.READ);
    }
  }

  private void registerOperationForDescriptiveMetadata(String aipID, String representationId,
    String descriptiveMetadataId, TransactionalModelOperationLog.OperationType operation) {
    registerOperationForRelatedAIP(aipID, operation);
    if (representationId == null) {
      registerOperation(DescriptiveMetadata.class, Arrays.asList(aipID, descriptiveMetadataId), operation);
    } else {
      registerOperation(DescriptiveMetadata.class, Arrays.asList(aipID, representationId, descriptiveMetadataId),
        operation);
    }
  }

  private void registerOperationForRepresentation(String aipID, String representationId,
    TransactionalModelOperationLog.OperationType operation) {
    registerOperationForRelatedAIP(aipID, operation);
    registerOperation(Representation.class, Arrays.asList(aipID, representationId), operation);
  }

  private void registerOperationForFile(String aipID, String representationId, List<String> path, String fileID,
    TransactionalModelOperationLog.OperationType operation) {
    registerOperationForRelatedAIP(aipID, operation);
    List<String> list = new ArrayList<>();
    list.add(aipID);
    list.add(representationId);
    list.addAll(path);
    list.add(fileID);
    registerOperation(File.class, list, operation);
  }

  private void registerOperationForEvent(PreservationMetadata event,
    TransactionalModelOperationLog.OperationType operation) {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be null");
    }

    if (event.getAipId() == null) {
      registerOperation(IndexedPreservationEvent.class, Arrays.asList(event.getId()), operation);
    } else if (event.getRepresentationId() == null) {
      registerOperationForRelatedAIP(event.getAipId(), operation);
      registerOperation(IndexedPreservationEvent.class, Arrays.asList(event.getAipId(), event.getId()), operation);
    } else {
      registerOperationForRelatedAIP(event.getAipId(), operation);
      registerOperation(IndexedPreservationEvent.class,
        Arrays.asList(event.getAipId(), event.getRepresentationId(), event.getId()), operation);
    }
  }

  private void registerOperationForPreservationMetadata(String preservationID,
    TransactionalModelOperationLog.OperationType operation) {
    registerOperationForPreservationMetadata(null, null, null, null, preservationID, operation);
  }

  private void registerOperationForPreservationMetadata(String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId, PreservationMetadata.PreservationMetadataType type,
    TransactionalModelOperationLog.OperationType operationType) {
    String preservationID = IdUtils.getPreservationId(type, aipId, representationId, fileDirectoryPath, fileId,
      RODAInstanceUtils.getLocalInstanceIdentifier());
    registerOperationForPreservationMetadata(aipId, representationId, fileDirectoryPath, fileId, preservationID,
      operationType);
  }

  private void registerOperationForPreservationMetadata(String aipId, String representationId,
    TransactionalModelOperationLog.OperationType operationType) {
    String preservationID = IdUtils.getRepresentationPreservationId(aipId, representationId,
      RODAInstanceUtils.getLocalInstanceIdentifier());
    registerOperationForPreservationMetadata(aipId, representationId, null, null, preservationID, operationType);
  }

  private void registerOperationForPreservationMetadata(File file,
    TransactionalModelOperationLog.OperationType operationType) {
    String preservationID = IdUtils.getPreservationId(PreservationMetadata.PreservationMetadataType.FILE,
      file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(),
      RODAInstanceUtils.getLocalInstanceIdentifier());
    registerOperationForPreservationMetadata(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(),
      preservationID, operationType);
  }

  private void registerOperationForPreservationMetadata(PreservationMetadata pm,
    TransactionalModelOperationLog.OperationType operationType) {
    if (pm == null) {
      throw new IllegalArgumentException("PreservationMetadata cannot be null");
    }
    registerOperationForPreservationMetadata(pm.getAipId(), pm.getRepresentationId(), pm.getFileDirectoryPath(),
      pm.getFileId(), pm.getId(), operationType);
  }

  private void registerOperationForPreservationMetadata(String aipID, String representationId, List<String> path,
    String fileID, String preservationID, TransactionalModelOperationLog.OperationType operation) {
    if (aipID == null) {
      acquireLock(PreservationMetadata.class, preservationID);
      registerOperation(PreservationMetadata.class, Arrays.asList(preservationID), operation);
    } else if (representationId == null) {
      registerOperationForRelatedAIP(aipID, operation);
      registerOperation(PreservationMetadata.class, Arrays.asList(aipID, preservationID), operation);
    } else if (fileID == null) {
      registerOperationForRelatedAIP(aipID, operation);
      registerOperation(PreservationMetadata.class, Arrays.asList(aipID, representationId, preservationID), operation);
    } else {
      registerOperationForRelatedAIP(aipID, operation);
      List<String> list = new ArrayList<>();
      list.add(aipID);
      list.add(representationId);
      list.addAll(path);
      list.add(fileID);
      list.add(preservationID);
      registerOperation(PreservationMetadata.class, list, operation);
    }
  }

  private void registerOperationForOtherMetadata(String aipID, TransactionalModelOperationLog.OperationType operation) {
    registerOperationForRelatedAIP(aipID, operation);
  }

  private void registerOperationForDIP(String dipID, TransactionalModelOperationLog.OperationType operation) {
    acquireLock(DIP.class, dipID);
    registerOperation(LogEntry.class, Arrays.asList(dipID), operation);
  }

  private void registerOperationForLogEntry(String logEntryID, TransactionalModelOperationLog.OperationType operation) {
    acquireLock(LogEntry.class, logEntryID);
    registerOperation(LogEntry.class, Arrays.asList(logEntryID), operation);
  }

  private <T extends IsRODAObject> void registerOperation(Class<T> objectClass, List<String> ids,
    TransactionalModelOperationLog.OperationType operation) {
    if (ids == null) {
      throw new IllegalArgumentException("Object ID cannot be null");
    }
    Optional<LiteRODAObject> liteRODAObject = LiteRODAObjectFactory.get(objectClass, ids);
    if (liteRODAObject.isPresent()) {
      String lite = liteRODAObject.get().getInfo();

      try {
        transactionLogService.registerModelOperation(transaction.getId(), lite, operation);
      } catch (RODATransactionException e) {
        throw new IllegalArgumentException("Cannot register operation for object: " + liteRODAObject, e);
      }
    } else {
      throw new IllegalArgumentException("Cannot register operation for object: " + liteRODAObject);
    }
  }

  private void registerOperation(LiteRODAObject lite, TransactionalModelOperationLog.OperationType operation) {
    String liteInfo = lite.getInfo();
    try {
      transactionLogService.registerModelOperation(transaction.getId(), liteInfo, operation);
    } catch (RODATransactionException e) {
      throw new IllegalArgumentException("Cannot register operation for object: " + liteInfo, e);
    }
  }

  private <T extends IsRODAObject> void acquireLock(Class<T> objectClass, String id) {
    if (id == null) {
      throw new IllegalArgumentException("Object ID cannot be null");
    }
    Optional<LiteRODAObject> liteRODAObject = LiteRODAObjectFactory.get(objectClass, id);
    if (liteRODAObject.isPresent()) {
      try {
        String lite = liteRODAObject.get().getInfo();
        PluginHelper.acquireObjectLock(lite, transaction.getId());
      } catch (LockingException e) {
        throw new IllegalArgumentException("Cannot acquire lock for object: " + liteRODAObject);
      }
    } else {
      throw new IllegalArgumentException(
        "Cannot acquire lock for object ID: " + id + " of class: " + objectClass.getName());
    }
  }

  @Override
  public void commit() throws RODATransactionException {
    for (TransactionalModelOperationLog modelOperation : transactionLogService
      .getModelOperations(transaction.getId())) {
      PluginHelper.releaseObjectLock(modelOperation.getLiteObject(), transaction.getId());
    }
  }

  @Override
  public void rollback() throws RODATransactionException {
    for (TransactionalModelOperationLog modelOperation : transactionLogService
      .getModelOperations(transaction.getId())) {
      LiteRODAObject liteRODAObject = new LiteRODAObject(modelOperation.getLiteObject());
      OptionalWithCause<IsRODAObject> isRODAObjectOptionalWithCause = LiteRODAObjectFactory.get(this, liteRODAObject);

      if (isRODAObjectOptionalWithCause.isPresent()) {
        IsRODAObject rodaObject = isRODAObjectOptionalWithCause.get();
        if (rodaObject instanceof AIP aip) {
          handleAIPRollback(aip, modelOperation);
        } else if (rodaObject instanceof Representation representation) {
          handleRepresentationRollback(representation, modelOperation);
        } else if (rodaObject instanceof File file) {
          handleFileRollback(file, modelOperation);
        } else {
          LOGGER.warn("Cannot rollback operation for class: {} with ID: {}", rodaObject.getClass().getSimpleName(),
            rodaObject.getId());
        }
      }

      PluginHelper.releaseObjectLock(modelOperation.getLiteObject(), transaction.getId());
    }
  }

  private void handleAIPRollback(AIP aip, TransactionalModelOperationLog modelOperation) {
    if (modelOperation.getOperationType() == TransactionalModelOperationLog.OperationType.CREATE) {
      LOGGER.debug("Rollback AIP creation for AIP: {}", aip.getId());
      stagingModelService.notifyAipDeleted(aip.getId());
    } else if (modelOperation.getOperationType() != TransactionalModelOperationLog.OperationType.READ) {
      LOGGER.debug("Rollback AIP update/delete for AIP: {}", aip.getId());
      try {
        mainModelService.notifyAipUpdated(aip.getId());
      } catch (NotFoundException | AuthorizationDeniedException | RequestNotValidException | GenericException e) {
        LOGGER.error("Error clearing specific indexes of a RODA entity", e);
      }
    }
  }

  private void handleRepresentationRollback(Representation representation,
    TransactionalModelOperationLog modelOperation) {
    if (modelOperation.getOperationType() == TransactionalModelOperationLog.OperationType.CREATE) {
      LOGGER.debug("Rollback Representation creation for Representation: {}", representation.getId());
      stagingModelService.notifyRepresentationDeleted(representation.getAipId(), representation.getId());
    } else if (modelOperation.getOperationType() != TransactionalModelOperationLog.OperationType.READ) {
      LOGGER.debug("Rollback Representation update/delete for Representation: {}", representation.getId());
      mainModelService.notifyRepresentationUpdated(representation);
    }
  }

  private void handleFileRollback(File file, TransactionalModelOperationLog modelOperation) {
    if (modelOperation.getOperationType() == TransactionalModelOperationLog.OperationType.CREATE) {
      LOGGER.debug("Rollback File creation for File: {}", file.getId());
      stagingModelService.notifyFileDeleted(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId());
    } else if (modelOperation.getOperationType() != TransactionalModelOperationLog.OperationType.READ) {
      LOGGER.debug("Rollback File update/delete for File: {}", file.getId());
      mainModelService.notifyFileUpdated(file);
    }
  }
}
