package org.roda.core.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
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
import org.roda.core.entity.transaction.OperationState;
import org.roda.core.entity.transaction.OperationType;
import org.roda.core.entity.transaction.TransactionLog;
import org.roda.core.entity.transaction.TransactionalModelOperationLog;
import org.roda.core.index.IndexService;
import org.roda.core.model.iterables.LogEntryFileSystemIterable;
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
import org.roda.core.transaction.TransactionModelRollbackHandler;
import org.roda.core.transaction.TransactionalModelOperationRegistry;
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
  private final TransactionalModelOperationRegistry operationRegistry;

  public DefaultTransactionalModelService(ModelService mainModelService, ModelService stagingModelService,
    TransactionLog transaction, TransactionLogService transactionLogService) {
    this.mainModelService = mainModelService;
    this.stagingModelService = stagingModelService;
    this.transaction = transaction;
    this.transactionLogService = transactionLogService;
    this.operationRegistry = new TransactionalModelOperationRegistry(transaction, transactionLogService,
      mainModelService);
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
    TransactionalModelOperationLog operationLog = operationRegistry.registerReadOperationForAIP(aipId);
    try {
      AIP aip = stagingModelService.retrieveAIP(aipId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return aip;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public AIP createAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath, boolean notify,
    String createdBy) throws RequestNotValidException, GenericException, AuthorizationDeniedException,
    AlreadyExistsException, NotFoundException, ValidationException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerCreateOperationForAIP(aipId);
    try {
      AIP ret = getModelService().createAIP(aipId, sourceStorage, sourcePath, notify, createdBy);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | AlreadyExistsException
      | NotFoundException | ValidationException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public AIP createAIP(String parentId, String type, Permissions permissions, List<String> ingestSIPIds,
    String ingestJobId, boolean notify, String createdBy, boolean isGhost, String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {
    if (aipId == null) {
      aipId = IdUtils.createUUID();
    }
    TransactionalModelOperationLog operationLog = operationRegistry.registerCreateOperationForAIP(aipId);
    try {
      AIP ret = getModelService().createAIP(parentId, type, permissions, ingestSIPIds, ingestJobId, notify, createdBy,
        isGhost, aipId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AlreadyExistsException
      | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public AIP createAIP(String parentId, String type, Permissions permissions, String createdBy, String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {
    if (aipId == null) {
      aipId = IdUtils.createUUID();
    }
    TransactionalModelOperationLog operationLog = operationRegistry.registerCreateOperationForAIP(aipId);
    try {
      AIP ret = getModelService().createAIP(parentId, type, permissions, createdBy, aipId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AlreadyExistsException
      | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public AIP createAIP(AIPState state, String parentId, String type, Permissions permissions, String createdBy,
    String aipId) throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {
    if (aipId == null) {
      aipId = IdUtils.createUUID();
    }
    TransactionalModelOperationLog operationLog = operationRegistry.registerCreateOperationForAIP(aipId);
    try {
      AIP ret = getModelService().createAIP(state, parentId, type, permissions, createdBy, aipId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AlreadyExistsException
      | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public AIP createAIP(AIPState state, String parentId, String type, Permissions permissions, boolean notify,
    String createdBy, String aipId) throws RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException {
    if (aipId == null) {
      aipId = IdUtils.createUUID();
    }
    TransactionalModelOperationLog operationLog = operationRegistry.registerCreateOperationForAIP(aipId);
    try {
      AIP ret = getModelService().createAIP(state, parentId, type, permissions, notify, createdBy, aipId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AlreadyExistsException
      | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public AIP createAIP(AIPState state, String parentId, String type, Permissions permissions, String ingestSIPUUID,
    List<String> ingestSIPIds, String ingestJobId, boolean notify, String createdBy, String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {
    if (aipId == null) {
      aipId = IdUtils.createUUID();
    }
    TransactionalModelOperationLog operationLog = operationRegistry.registerCreateOperationForAIP(aipId);
    try {
      AIP ret = getModelService().createAIP(state, parentId, type, permissions, ingestSIPUUID, ingestSIPIds,
        ingestJobId, notify, createdBy, aipId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AlreadyExistsException
      | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public AIP createAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath, String createdBy)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, AlreadyExistsException,
    NotFoundException, ValidationException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerCreateOperationForAIP(aipId);
    try {
      AIP ret = getModelService().createAIP(aipId, sourceStorage, sourcePath, createdBy);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | AlreadyExistsException
      | NotFoundException | ValidationException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
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
    TransactionalModelOperationLog operationLog = operationRegistry.registerUpdateOperationForAIP(aipId);
    try {
      AIP ret = getModelService().updateAIP(aipId, sourceStorage, sourcePath, updatedBy);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | AlreadyExistsException | ValidationException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public AIP destroyAIP(AIP aip, String updatedBy)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerDeleteOperationForAIP(aip.getId());
    try {
      AIP ret = getModelService().destroyAIP(aip, updatedBy);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | GenericException | NotFoundException | RequestNotValidException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public AIP updateAIP(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerUpdateOperationForAIP(aip.getId());
    try {
      AIP ret = getModelService().updateAIP(aip, updatedBy);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public AIP updateAIPState(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerUpdateOperationForAIP(aip.getId());
    try {
      AIP ret = getModelService().updateAIPState(aip, updatedBy);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public AIP updateAIPInstanceId(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerUpdateOperationForAIP(aip.getId());
    try {
      AIP ret = getModelService().updateAIPInstanceId(aip, updatedBy);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public AIP moveAIP(String aipId, String parentId, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerUpdateOperationForAIP(aipId);
    try {
      AIP ret = getModelService().moveAIP(aipId, parentId, updatedBy);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteAIP(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerDeleteOperationForAIP(aipId);
    try {
      getModelService().deleteAIP(aipId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void changeAIPType(String aipId, String type, String updatedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerUpdateOperationForAIP(aipId);
    try {
      getModelService().changeAIPType(aipId, type, updatedBy);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Binary retrieveDescriptiveMetadataBinary(String aipId, String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerOperationForDescriptiveMetadata(aipId, null, descriptiveMetadataId, OperationType.READ);

    try {
      Binary binary = stagingModelService.retrieveDescriptiveMetadataBinary(aipId, descriptiveMetadataId);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return binary;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Binary retrieveDescriptiveMetadataBinary(String aipId, String representationId, String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerOperationForDescriptiveMetadata(aipId, representationId, descriptiveMetadataId, OperationType.READ);

    try {
      Binary binary = stagingModelService.retrieveDescriptiveMetadataBinary(aipId, representationId,
        descriptiveMetadataId);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return binary;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DescriptiveMetadata retrieveDescriptiveMetadata(String aipId, String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerOperationForDescriptiveMetadata(aipId, null, descriptiveMetadataId, OperationType.READ);

    try {
      DescriptiveMetadata descriptiveMetadata = stagingModelService.retrieveDescriptiveMetadata(aipId,
        descriptiveMetadataId);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return descriptiveMetadata;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DescriptiveMetadata retrieveDescriptiveMetadata(String aipId, String representationId,
    String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerOperationForDescriptiveMetadata(aipId, representationId, descriptiveMetadataId, OperationType.READ);

    try {
      DescriptiveMetadata descriptiveMetadata = stagingModelService.retrieveDescriptiveMetadata(aipId, representationId,
        descriptiveMetadataId);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return descriptiveMetadata;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String descriptiveMetadataId,
    ContentPayload payload, String descriptiveMetadataType, String descriptiveMetadataVersion, String createdBy,
    boolean notify) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {

    operationRegistry.checkIfEntityExistsAndThrowException(DescriptiveMetadata.class, aipId, null,
      descriptiveMetadataId);

    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerOperationForDescriptiveMetadata(aipId, null, descriptiveMetadataId, OperationType.CREATE);
    try {
      DescriptiveMetadata ret = getModelService().createDescriptiveMetadata(aipId, descriptiveMetadataId, payload,
        descriptiveMetadataType, descriptiveMetadataVersion, createdBy, notify);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;

    } catch (RequestNotValidException | GenericException | AlreadyExistsException | AuthorizationDeniedException
      | NotFoundException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String descriptiveMetadataId,
    ContentPayload payload, String descriptiveMetadataType, String descriptiveMetadataVersion, String createdBy)
    throws RequestNotValidException, GenericException, AlreadyExistsException, AuthorizationDeniedException,
    NotFoundException {

    operationRegistry.checkIfEntityExistsAndThrowException(DescriptiveMetadata.class, aipId, null,
      descriptiveMetadataId);

    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerOperationForDescriptiveMetadata(aipId, null, descriptiveMetadataId, OperationType.CREATE);
    try {
      DescriptiveMetadata ret = getModelService().createDescriptiveMetadata(aipId, descriptiveMetadataId, payload,
        descriptiveMetadataType, descriptiveMetadataVersion, createdBy);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AlreadyExistsException | AuthorizationDeniedException
      | NotFoundException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String representationId,
    String descriptiveMetadataId, ContentPayload payload, String descriptiveMetadataType,
    String descriptiveMetadataVersion, String createdBy) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {

    operationRegistry.checkIfEntityExistsAndThrowException(DescriptiveMetadata.class, aipId, representationId,
      descriptiveMetadataId);

    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerOperationForDescriptiveMetadata(aipId, representationId, descriptiveMetadataId, OperationType.CREATE);
    try {
      DescriptiveMetadata ret = getModelService().createDescriptiveMetadata(aipId, representationId,
        descriptiveMetadataId, payload, descriptiveMetadataType, descriptiveMetadataVersion, createdBy);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AlreadyExistsException | AuthorizationDeniedException
      | NotFoundException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String representationId,
    String descriptiveMetadataId, ContentPayload payload, String descriptiveMetadataType,
    String descriptiveMetadataVersion, String createdBy, boolean notify) throws RequestNotValidException,
    GenericException, AlreadyExistsException, AuthorizationDeniedException, NotFoundException {

    operationRegistry.checkIfEntityExistsAndThrowException(DescriptiveMetadata.class, aipId, representationId,
      descriptiveMetadataId);

    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerOperationForDescriptiveMetadata(aipId, representationId, descriptiveMetadataId, OperationType.CREATE);
    try {
      DescriptiveMetadata ret = getModelService().createDescriptiveMetadata(aipId, representationId,
        descriptiveMetadataId, payload, descriptiveMetadataType, descriptiveMetadataVersion, createdBy, notify);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AlreadyExistsException | AuthorizationDeniedException
      | NotFoundException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DescriptiveMetadata updateDescriptiveMetadata(String aipId, String descriptiveMetadataId,
    ContentPayload descriptiveMetadataPayload, String descriptiveMetadataType, String descriptiveMetadataVersion,
    Map<String, String> properties, String updatedBy)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerOperationForDescriptiveMetadata(aipId, null, descriptiveMetadataId, OperationType.UPDATE);
    try {
      DescriptiveMetadata ret = getModelService().updateDescriptiveMetadata(aipId, descriptiveMetadataId,
        descriptiveMetadataPayload, descriptiveMetadataType, descriptiveMetadataVersion, properties, updatedBy);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DescriptiveMetadata updateDescriptiveMetadata(String aipId, String representationId,
    String descriptiveMetadataId, ContentPayload descriptiveMetadataPayload, String descriptiveMetadataType,
    String descriptiveMetadataVersion, Map<String, String> properties, String updatedBy)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerOperationForDescriptiveMetadata(aipId, representationId, descriptiveMetadataId, OperationType.UPDATE);

    try {
      DescriptiveMetadata ret = getModelService().updateDescriptiveMetadata(aipId, representationId,
        descriptiveMetadataId, descriptiveMetadataPayload, descriptiveMetadataType, descriptiveMetadataVersion,
        properties, updatedBy);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteDescriptiveMetadata(String aipId, String descriptiveMetadataId, String deletedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerOperationForDescriptiveMetadata(aipId, null, descriptiveMetadataId, OperationType.DELETE);
    try {
      getModelService().deleteDescriptiveMetadata(aipId, descriptiveMetadataId, deletedBy);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteDescriptiveMetadata(String aipId, String representationId, String descriptiveMetadataId,
    String deletedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerOperationForDescriptiveMetadata(aipId, representationId, descriptiveMetadataId, OperationType.DELETE);
    try {
      getModelService().deleteDescriptiveMetadata(aipId, representationId, descriptiveMetadataId, deletedBy);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public CloseableIterable<BinaryVersion> listDescriptiveMetadataVersions(String aipId, String representationId,
    String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerOperationForDescriptiveMetadata(aipId, representationId, descriptiveMetadataId, OperationType.READ);
    try {
      CloseableIterable<BinaryVersion> ret = getModelService().listDescriptiveMetadataVersions(aipId, representationId,
        descriptiveMetadataId);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public BinaryVersion revertDescriptiveMetadataVersion(String aipId, String descriptiveMetadataId, String versionId,
    Map<String, String> properties)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerOperationForDescriptiveMetadata(aipId, null, descriptiveMetadataId, OperationType.UPDATE);
    try {
      BinaryVersion ret = getModelService().revertDescriptiveMetadataVersion(aipId, descriptiveMetadataId, versionId,
        properties);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public BinaryVersion revertDescriptiveMetadataVersion(String aipId, String representationId,
    String descriptiveMetadataId, String versionId, Map<String, String> properties)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerOperationForDescriptiveMetadata(aipId, representationId, descriptiveMetadataId, OperationType.UPDATE);
    try {
      BinaryVersion ret = getModelService().revertDescriptiveMetadataVersion(aipId, representationId,
        descriptiveMetadataId, versionId, properties);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
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
    TransactionalModelOperationLog operationLog = operationRegistry.registerReadOperationForAIP(aipId);
    try {
      CloseableIterable<OptionalWithCause<DescriptiveMetadata>> ret = getModelService().listDescriptiveMetadata(aipId,
        includeRepresentations);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public CloseableIterable<OptionalWithCause<DescriptiveMetadata>> listDescriptiveMetadata(String aipId,
    String representationId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLog = operationRegistry.registerOperationForRepresentation(aipId,
      representationId, OperationType.READ);
    try {
      CloseableIterable<OptionalWithCause<DescriptiveMetadata>> ret = getModelService().listDescriptiveMetadata(aipId,
        representationId);
      for (TransactionalModelOperationLog log : operationLog) {
        operationRegistry.updateOperationState(log, OperationState.SUCCESS);
      }
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      for (TransactionalModelOperationLog log : operationLog) {
        operationRegistry.updateOperationState(log, OperationState.FAILURE);
      }
      throw e;
    }
  }

  @Override
  public Representation retrieveRepresentation(String aipId, String representationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerOperationForRepresentation(aipId,
      representationId, OperationType.READ);

    try {
      Representation representation = stagingModelService.retrieveRepresentation(aipId, representationId);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return representation;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Representation createRepresentation(String aipId, String representationId, boolean original, String type,
    boolean notify, String createdBy, List<String> representationState) throws RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException, AlreadyExistsException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerOperationForRepresentation(aipId,
      representationId, OperationType.CREATE);
    try {
      Representation ret = getModelService().createRepresentation(aipId, representationId, original, type, notify,
        createdBy, representationState);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException
      | AlreadyExistsException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Representation createRepresentation(String aipId, String representationId, boolean original, String type,
    boolean notify, String createdBy) throws RequestNotValidException, GenericException, NotFoundException,
    AuthorizationDeniedException, AlreadyExistsException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerOperationForRepresentation(aipId,
      representationId, OperationType.CREATE);
    try {
      Representation ret = getModelService().createRepresentation(aipId, representationId, original, type, notify,
        createdBy);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException
      | AlreadyExistsException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Representation createRepresentation(String aipId, String representationId, boolean original, String type,
    StorageService sourceStorage, StoragePath sourcePath, boolean justData, String createdBy)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException,
    AlreadyExistsException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerOperationForRepresentation(aipId,
      representationId, OperationType.CREATE);
    try {
      Representation ret = getModelService().createRepresentation(aipId, representationId, original, type,
        sourceStorage, sourcePath, justData, createdBy);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException
      | AlreadyExistsException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Representation updateRepresentationInfo(Representation representation) throws GenericException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerOperationForRepresentation(representation.getAipId(), representation.getId(), OperationType.UPDATE);
    try {
      Representation ret = getModelService().updateRepresentationInfo(representation);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (GenericException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void changeRepresentationType(String aipId, String representationId, String type, String updatedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerOperationForRepresentation(aipId,
      representationId, OperationType.UPDATE);
    try {
      getModelService().changeRepresentationType(aipId, representationId, type, updatedBy);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);

    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void changeRepresentationShallowFileFlag(String aipId, String representationId, boolean hasShallowFiles,
    String updatedBy, boolean notify)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerOperationForRepresentation(aipId,
      representationId, OperationType.UPDATE);
    try {
      getModelService().changeRepresentationShallowFileFlag(aipId, representationId, hasShallowFiles, updatedBy,
        notify);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
    } catch (AuthorizationDeniedException | GenericException | NotFoundException | RequestNotValidException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void changeRepresentationStates(String aipId, String representationId, List<String> newStates,
    String updatedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerOperationForRepresentation(aipId,
      representationId, OperationType.UPDATE);
    try {
      getModelService().changeRepresentationStates(aipId, representationId, newStates, updatedBy);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Representation updateRepresentation(String aipId, String representationId, boolean original, String type,
    StorageService sourceStorage, StoragePath sourcePath, String updatedBy) throws RequestNotValidException,
    NotFoundException, GenericException, AuthorizationDeniedException, ValidationException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerOperationForRepresentation(aipId,
      representationId, OperationType.UPDATE);
    try {
      Representation ret = getModelService().updateRepresentation(aipId, representationId, original, type,
        sourceStorage, sourcePath, updatedBy);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | ValidationException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteRepresentation(String aipId, String representationId, String username)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerOperationForRepresentation(aipId,
      representationId, OperationType.DELETE);
    try {
      getModelService().deleteRepresentation(aipId, representationId, username);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public CloseableIterable<OptionalWithCause<File>> listFilesUnder(String aipId, String representationId,
    boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerOperationForRepresentation(aipId,
      representationId, OperationType.READ);
    try {
      CloseableIterable<OptionalWithCause<File>> ret = getModelService().listFilesUnder(aipId, representationId,
        recursive);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public CloseableIterable<OptionalWithCause<File>> listExternalFilesUnder(File file)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLog = operationRegistry.registerOperationForFile(file.getAipId(),
      file.getRepresentationId(), file.getPath(), file.getId(), OperationType.READ);
    try {
      CloseableIterable<OptionalWithCause<File>> ret = getModelService().listExternalFilesUnder(file);
      for (TransactionalModelOperationLog log : operationLog) {
        operationRegistry.updateOperationState(log, OperationState.SUCCESS);
      }
      return ret;
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      for (TransactionalModelOperationLog log : operationLog) {
        operationRegistry.updateOperationState(log, OperationState.FAILURE);
      }
      throw e;
    }
  }

  @Override
  public CloseableIterable<OptionalWithCause<File>> listFilesUnder(File f, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerOperationForFile(f.getAipId(),
      f.getRepresentationId(), f.getPath(), f.getId(), OperationType.READ);
    try {
      CloseableIterable<OptionalWithCause<File>> ret = getModelService().listFilesUnder(f, recursive);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public CloseableIterable<OptionalWithCause<File>> listFilesUnder(String aipId, String representationId,
    List<String> directoryPath, String fileId, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerOperationForFile(aipId,
      representationId, directoryPath, fileId, OperationType.READ);
    try {
      CloseableIterable<OptionalWithCause<File>> ret = getModelService().listFilesUnder(aipId, representationId,
        directoryPath, fileId, recursive);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Long getExternalFilesTotalSize(File file)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException, IOException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerOperationForFile(file.getAipId(),
      file.getRepresentationId(), file.getPath(), file.getId(), OperationType.READ);
    try {
      Long ret = getModelService().getExternalFilesTotalSize(file);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | AuthorizationDeniedException | NotFoundException | GenericException
      | IOException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public File retrieveFile(String aipId, String representationId, List<String> directoryPath, String fileId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerOperationForFile(aipId,
      representationId, directoryPath, fileId, OperationType.READ);
    try {
      File file = stagingModelService.retrieveFile(aipId, representationId, directoryPath, fileId);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return file;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public File createFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload, String createdBy) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {

    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerCreateOperationForFile(aipId,
      representationId, directoryPath, fileId, null);
    try {
      File ret = getModelService().createFile(aipId, representationId, directoryPath, fileId, contentPayload,
        createdBy);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AlreadyExistsException | AuthorizationDeniedException
      | NotFoundException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public File createFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload, String createdBy, boolean notify) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {

    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerCreateOperationForFile(aipId,
      representationId, directoryPath, fileId, null);
    try {
      File ret = getModelService().createFile(aipId, representationId, directoryPath, fileId, contentPayload, createdBy,
        notify);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AlreadyExistsException | AuthorizationDeniedException
      | NotFoundException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public File createFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    String dirName, String createdBy, boolean notify) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {

    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerCreateOperationForFile(aipId,
      representationId, directoryPath, fileId, dirName);
    try {
      File ret = getModelService().createFile(aipId, representationId, directoryPath, fileId, dirName, createdBy,
        notify);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AlreadyExistsException | AuthorizationDeniedException
      | NotFoundException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public File updateFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload, boolean createIfNotExists, String updatedBy, boolean notify)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerOperationForFile(aipId,
      representationId, directoryPath, fileId, OperationType.UPDATE);
    try {
      File ret = getModelService().updateFile(aipId, representationId, directoryPath, fileId, contentPayload,
        createIfNotExists, updatedBy, notify);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public File updateFile(File file, ContentPayload contentPayload, boolean createIfNotExists, String updatedBy,
    boolean notify) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerOperationForFile(file.getAipId(),
      file.getRepresentationId(), file.getPath(), file.getId(), OperationType.UPDATE);
    try {
      return getModelService().updateFile(file, contentPayload, createIfNotExists, updatedBy, notify);
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    String deletedBy, boolean notify)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerOperationForFile(aipId,
      representationId, directoryPath, fileId, OperationType.DELETE);
    try {
      getModelService().deleteFile(aipId, representationId, directoryPath, fileId, deletedBy, notify);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteFile(File file, String deletedBy, boolean notify)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerOperationForFile(file.getAipId(),
      file.getRepresentationId(), file.getPath(), file.getId(), OperationType.DELETE);
    try {
      getModelService().deleteFile(file, deletedBy, notify);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public File renameFolder(File folder, String newName, boolean reindexResources) throws AlreadyExistsException,
    GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {

    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerOperationForFile(folder.getAipId(),
      folder.getRepresentationId(), folder.getPath(), folder.getId(), OperationType.DELETE);
    List<TransactionalModelOperationLog> renameOperationLogs = operationRegistry.registerOperationForFile(
      folder.getAipId(), folder.getRepresentationId(), folder.getPath(), newName, OperationType.CREATE);
    try {
      File ret = getModelService().renameFolder(folder, newName, reindexResources);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      for (TransactionalModelOperationLog renameOperationLog : renameOperationLogs) {
        operationRegistry.updateOperationState(renameOperationLog, OperationState.SUCCESS);
      }
      return ret;
    } catch (AlreadyExistsException | GenericException | NotFoundException | RequestNotValidException
      | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      for (TransactionalModelOperationLog renameOperationLog : renameOperationLogs) {
        operationRegistry.updateOperationState(renameOperationLog, OperationState.FAILURE);
      }
      throw e;
    }
  }

  @Override
  public File moveFile(File file, String newAipId, String newRepresentationId, List<String> newDirectoryPath,
    String newId, boolean reindexResources) throws AlreadyExistsException, GenericException, NotFoundException,
    RequestNotValidException, AuthorizationDeniedException {

    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerOperationForFile(file.getAipId(),
      file.getRepresentationId(), file.getPath(), file.getId(), OperationType.DELETE);
    List<TransactionalModelOperationLog> moveOperationLogs = operationRegistry.registerOperationForFile(newAipId,
      newRepresentationId, newDirectoryPath, newId, OperationType.CREATE);
    operationLogs.addAll(operationRegistry.registerOperationForFile(newAipId, newRepresentationId, newDirectoryPath,
      newId, OperationType.UPDATE));
    try {
      File ret = getModelService().moveFile(file, newAipId, newRepresentationId, newDirectoryPath, newId,
        reindexResources);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      for (TransactionalModelOperationLog moveOperationLog : moveOperationLogs) {
        operationRegistry.updateOperationState(moveOperationLog, OperationState.SUCCESS);
      }
      return ret;
    } catch (AlreadyExistsException | GenericException | NotFoundException | RequestNotValidException
      | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      for (TransactionalModelOperationLog moveOperationLog : moveOperationLogs) {
        operationRegistry.updateOperationState(moveOperationLog, OperationState.FAILURE);
      }
      throw e;
    }
  }

  @Override
  public PreservationMetadata createRepositoryEvent(RodaConstants.PreservationEventType eventType,
    String eventDescription, PluginState outcomeState, String outcomeText, String outcomeDetail, String agentName,
    boolean notify, String eventId) {
    try {
      if (eventId == null) {
        eventId = IdUtils.createPreservationMetadataId(PreservationMetadata.PreservationMetadataType.EVENT,
          RODAInstanceUtils.getLocalInstanceIdentifier());
      }
      List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerCreateOperationForEvent(null, null,
        eventId, eventType);

      PreservationMetadata event = getModelService().createRepositoryEvent(eventType, eventDescription, outcomeState,
        outcomeText, outcomeDetail, agentName, notify, eventId);

      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return event;
    } catch (RequestNotValidException | AlreadyExistsException | GenericException e) {
      // Do nothing because the event cannot be created and the calling methods do not
      // do anything with the event
      return null;
    }
  }

  @Override
  public PreservationMetadata createRepositoryEvent(RodaConstants.PreservationEventType eventType,
    String eventDescription, List<LinkingIdentifier> sources, List<LinkingIdentifier> targets, PluginState outcomeState,
    String outcomeText, String outcomeDetail, String agentName, boolean notify, String eventId) {
    try {
      if (eventId == null) {
        eventId = IdUtils.createPreservationMetadataId(PreservationMetadata.PreservationMetadataType.EVENT,
          RODAInstanceUtils.getLocalInstanceIdentifier());
      }
      List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerCreateOperationForEvent(null, null,
        eventId, eventType);

      PreservationMetadata event = getModelService().createRepositoryEvent(eventType, eventDescription, sources,
        targets, outcomeState, outcomeText, outcomeDetail, agentName, notify, eventId);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return event;
    } catch (RequestNotValidException | AlreadyExistsException | GenericException e) {
      // Do nothing because the event cannot be created and the calling methods do not
      // do anything with the event
      return null;
    }
  }

  @Override
  public PreservationMetadata createUpdateAIPEvent(String aipId, String representationId, List<String> filePath,
    String fileId, RodaConstants.PreservationEventType eventType, String eventDescription, PluginState outcomeState,
    String outcomeText, String outcomeDetail, String agentName, boolean notify, String eventId) {

    if (eventId == null) {
      eventId = IdUtils.createPreservationMetadataId(PreservationMetadata.PreservationMetadataType.EVENT,
        RODAInstanceUtils.getLocalInstanceIdentifier());
    }
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerUpdateOperationForEvent(aipId,
      representationId, eventId, eventType);

    PreservationMetadata event = getModelService().createUpdateAIPEvent(aipId, representationId, filePath, fileId,
      eventType, eventDescription, outcomeState, outcomeText, outcomeDetail, agentName, notify, eventId);
    operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
    return event;
  }

  @Override
  public PreservationMetadata createEvent(String aipId, String representationId, List<String> filePath, String fileId,
    RodaConstants.PreservationEventType eventType, String eventDescription, List<LinkingIdentifier> sources,
    List<LinkingIdentifier> targets, PluginState outcomeState, String outcomeText, String outcomeDetail,
    String agentName, boolean notify, String eventId) {
    try {
      if (eventId == null) {
        eventId = IdUtils.createPreservationMetadataId(PreservationMetadata.PreservationMetadataType.EVENT,
          RODAInstanceUtils.getLocalInstanceIdentifier());
      }
      List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerCreateOperationForEvent(aipId,
        representationId, eventId, eventType);
      PreservationMetadata event = getModelService().createEvent(aipId, representationId, filePath, fileId, eventType,
        eventDescription, sources, targets, outcomeState, outcomeText, outcomeDetail, agentName, notify, eventId);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return event;
    } catch (RequestNotValidException | AlreadyExistsException | GenericException e) {
      // Do nothing because the event cannot be created and the calling methods do not
      // do anything with the event
      return null;
    }
  }

  @Override
  public PreservationMetadata createEvent(String aipId, String representationId, List<String> filePath, String fileId,
    RodaConstants.PreservationEventType eventType, String eventDescription, List<LinkingIdentifier> sources,
    List<LinkingIdentifier> targets, PluginState outcomeState, String outcomeText, String outcomeDetail,
    String agentName, String agentRole, boolean notify, String eventId) {
    try {
      if (eventId == null) {
        eventId = IdUtils.createPreservationMetadataId(PreservationMetadata.PreservationMetadataType.EVENT,
          RODAInstanceUtils.getLocalInstanceIdentifier());
      }
      List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerCreateOperationForEvent(aipId,
        representationId, eventId, eventType);

      PreservationMetadata event = getModelService().createEvent(aipId, representationId, filePath, fileId, eventType,
        eventDescription, sources, targets, outcomeState, outcomeText, outcomeDetail, agentName, agentRole, notify,
        eventId);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return event;
    } catch (RequestNotValidException | AlreadyExistsException | GenericException e) {
      // Do nothing because the event cannot be created and the calling methods do not
      // do anything with the event
      return null;
    }
  }

  @Override
  public PreservationMetadata createEvent(String aipId, String representationId, List<String> filePath, String fileId,
    RodaConstants.PreservationEventType eventType, String eventDescription, List<LinkingIdentifier> sources,
    List<LinkingIdentifier> targets, PluginState outcomeState, String outcomeDetail, String outcomeExtension,
    List<LinkingIdentifier> agentIds, String username, boolean notify, String eventId)
    throws GenericException, ValidationException, NotFoundException, RequestNotValidException,
    AuthorizationDeniedException, AlreadyExistsException {
    if (eventId == null) {
      eventId = IdUtils.createPreservationMetadataId(PreservationMetadata.PreservationMetadataType.EVENT,
        RODAInstanceUtils.getLocalInstanceIdentifier());
    }
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerCreateOperationForEvent(aipId,
      representationId, eventId, eventType);
    try {
      PreservationMetadata event = getModelService().createEvent(aipId, representationId, filePath, fileId, eventType,
        eventDescription, sources, targets, outcomeState, outcomeDetail, outcomeExtension, agentIds, username, notify,
        eventId);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return event;
    } catch (AuthorizationDeniedException | RequestNotValidException | AlreadyExistsException | NotFoundException
      | GenericException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public PreservationMetadata retrievePreservationMetadata(String id,
    PreservationMetadata.PreservationMetadataType type) {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerReadOperationForPreservationMetadata(null, null, null, null, id, type);
    operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
    return getModelService().retrievePreservationMetadata(id, type);
  }

  @Override
  public PreservationMetadata retrievePreservationMetadata(String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId, PreservationMetadata.PreservationMetadataType type) {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerReadOperationForPreservationMetadata(aipId, representationId, fileDirectoryPath, fileId, null, type);
    operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
    return getModelService().retrievePreservationMetadata(aipId, representationId, fileDirectoryPath, fileId, type);
  }

  @Override
  public Binary retrievePreservationRepresentation(String aipId, String representationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerReadOperationForPreservationMetadata(
      aipId, representationId, null, null, null, PreservationMetadata.PreservationMetadataType.REPRESENTATION);
    try {
      Binary binary = stagingModelService.retrievePreservationRepresentation(aipId, representationId);
      return binary;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public boolean preservationRepresentationExists(String aipId, String representationId)
    throws RequestNotValidException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerReadOperationForPreservationMetadata(
      aipId, representationId, null, null, null, PreservationMetadata.PreservationMetadataType.REPRESENTATION);
    try {
      boolean ret;
      if (!stagingModelService.preservationRepresentationExists(aipId, representationId)) {
        ret = mainModelService.preservationRepresentationExists(aipId, representationId);
      } else {
        ret = true;
      }
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Binary retrievePreservationFile(File file)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerReadOperationForPreservationMetadata(
      file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(), null,
      PreservationMetadata.PreservationMetadataType.FILE);
    try {
      Binary binary = stagingModelService.retrievePreservationFile(file);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return binary;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Binary retrievePreservationFile(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerReadOperationForPreservationMetadata(
      aipId, representationId, fileDirectoryPath, fileId, null, PreservationMetadata.PreservationMetadataType.FILE);
    try {
      Binary binary = stagingModelService.retrievePreservationFile(aipId, representationId, fileDirectoryPath, fileId);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return binary;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public boolean preservationFileExists(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId) throws RequestNotValidException, GenericException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerReadOperationForPreservationMetadata(
      aipId, representationId, fileDirectoryPath, fileId, null, PreservationMetadata.PreservationMetadataType.FILE);
    try {
      boolean ret;
      if (!stagingModelService.preservationFileExists(aipId, representationId, fileDirectoryPath, fileId)) {
        ret = mainModelService.preservationFileExists(aipId, representationId, fileDirectoryPath, fileId);
      } else {
        ret = true;
      }
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Binary retrieveRepositoryPreservationEvent(String fileId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerReadOperationForPreservationMetadata(
      null, null, null, null, fileId, PreservationMetadata.PreservationMetadataType.EVENT);
    try {
      Binary binary = stagingModelService.retrieveRepositoryPreservationEvent(fileId);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return binary;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }

  }

  @Override
  public Binary retrievePreservationEvent(String aipId, String representationId, List<String> filePath, String fileId,
    String preservationID)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerReadOperationForPreservationMetadata(aipId, representationId, filePath, fileId, preservationID, null);
    try {
      Binary binary = stagingModelService.retrievePreservationEvent(aipId, representationId, filePath, fileId,
        preservationID);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return binary;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Binary retrievePreservationAgent(String preservationID)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerReadOperationForPreservationMetadata(
      null, null, null, null, preservationID, PreservationMetadata.PreservationMetadataType.AGENT);
    try {
      Binary binary = stagingModelService.retrievePreservationAgent(preservationID);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return binary;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public PreservationMetadata createPreservationMetadata(PreservationMetadata.PreservationMetadataType type,
    String aipId, String representationId, List<String> fileDirectoryPath, String fileId, ContentPayload payload,
    String username, boolean notify) throws GenericException, NotFoundException, RequestNotValidException,
    AuthorizationDeniedException, AlreadyExistsException {

    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerCreateOperationForPreservationMetadata(aipId, representationId, fileDirectoryPath, fileId, null, type);

    try {
      PreservationMetadata preservationMetadata = getModelService().createPreservationMetadata(type, aipId,
        representationId, fileDirectoryPath, fileId, payload, username, notify);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return preservationMetadata;
    } catch (AuthorizationDeniedException | RequestNotValidException | AlreadyExistsException | NotFoundException
      | GenericException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void createTechnicalMetadata(String aipId, String representationId, String metadataType, String fileId,
    ContentPayload payload, String createdBy, boolean notify) throws AuthorizationDeniedException,
    RequestNotValidException, AlreadyExistsException, NotFoundException, GenericException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerOperationForRepresentation(aipId,
      representationId, OperationType.CREATE);
    try {
      getModelService().createTechnicalMetadata(aipId, representationId, metadataType, fileId, payload, createdBy,
        notify);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
    } catch (AuthorizationDeniedException | RequestNotValidException | AlreadyExistsException | NotFoundException
      | GenericException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public PreservationMetadata createPreservationMetadata(PreservationMetadata.PreservationMetadataType type,
    String aipId, List<String> fileDirectoryPath, String fileId, ContentPayload payload, String username,
    boolean notify) throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException,
    AlreadyExistsException {

    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerCreateOperationForPreservationMetadata(aipId, null, fileDirectoryPath, fileId, null, type);

    try {
      PreservationMetadata preservationMetadata = getModelService().createPreservationMetadata(type, aipId,
        fileDirectoryPath, fileId, payload, username, notify);

      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return preservationMetadata;
    } catch (AuthorizationDeniedException | RequestNotValidException | AlreadyExistsException | NotFoundException
      | GenericException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public PreservationMetadata createPreservationMetadata(PreservationMetadata.PreservationMetadataType type,
    String aipId, String representationId, ContentPayload payload, String username, boolean notify)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException,
    AlreadyExistsException {

    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerCreateOperationForPreservationMetadata(aipId, representationId, null, null, null, type);

    try {
      PreservationMetadata preservationMetadata = getModelService().createPreservationMetadata(type, aipId,
        representationId, payload, username, notify);

      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return preservationMetadata;
    } catch (AuthorizationDeniedException | RequestNotValidException | AlreadyExistsException | NotFoundException
      | GenericException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public PreservationMetadata createPreservationMetadata(PreservationMetadata.PreservationMetadataType type, String id,
    ContentPayload payload, boolean notify) throws GenericException, NotFoundException, RequestNotValidException,
    AuthorizationDeniedException, AlreadyExistsException {

    // TODO: Temporary fix "Agent" preservation metadata need to be transactional,
    // but during multi-threaded ingestion, one thread locks the resource and blocks
    // the others.
    // if (type.equals(PreservationMetadata.PreservationMetadataType.AGENT)) {
    // return mainModelService.createPreservationMetadata(type, id, payload,
    // notify);
    // }

    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerCreateOperationForPreservationMetadata(null, null, null, null, id, type);

    try {
      PreservationMetadata ret = getModelService().createPreservationMetadata(type, id, payload, notify);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    } catch (AlreadyExistsException e) {
      // if the agent already exists we do nothing register failure
      if (!type.equals(PreservationMetadata.PreservationMetadataType.AGENT)) {
        operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      }
      throw e;
    }
  }

  @Override
  public PreservationMetadata createPreservationMetadata(PreservationMetadata.PreservationMetadataType type, String id,
    String aipId, String representationId, List<String> fileDirectoryPath, String fileId, ContentPayload payload,
    String createdBy, boolean notify) throws GenericException, NotFoundException, RequestNotValidException,
    AuthorizationDeniedException, AlreadyExistsException {

    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerCreateOperationForPreservationMetadata(aipId, representationId, fileDirectoryPath, fileId, id, type);

    try {
      PreservationMetadata ret = getModelService().createPreservationMetadata(type, id, aipId, representationId,
        fileDirectoryPath, fileId, payload, createdBy, notify);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException
      | AlreadyExistsException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public PreservationMetadata updatePreservationMetadata(PreservationMetadata.PreservationMetadataType type, String id,
    ContentPayload payload, boolean notify)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {

    // TODO: Temporary fix "Agent" preservation metadata need to be transactional,
    // but during multi-threaded ingestion, one thread locks the resource and blocks
    // the others.
    // if (type.equals(PreservationMetadata.PreservationMetadataType.AGENT)) {
    // return mainModelService.updatePreservationMetadata(type, id, payload,
    // notify);
    // }

    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerUpdateOperationForPreservationMetadata(null, null, null, null, id, type);
    try {
      PreservationMetadata ret = getModelService().updatePreservationMetadata(type, id, payload, notify);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public PreservationMetadata updatePreservationMetadata(String id, PreservationMetadata.PreservationMetadataType type,
    String aipId, String representationId, List<String> fileDirectoryPath, String fileId, ContentPayload payload,
    String updatedBy, boolean notify)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerUpdateOperationForPreservationMetadata(aipId, representationId, fileDirectoryPath, fileId, id, type);
    try {
      PreservationMetadata ret = getModelService().updatePreservationMetadata(id, type, aipId, representationId,
        fileDirectoryPath, fileId, payload, updatedBy, notify);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deletePreservationMetadata(PreservationMetadata pm, boolean notify)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerDeleteOperationForPreservationMetadata(pm.getAipId(), pm.getRepresentationId(),
        pm.getFileDirectoryPath(), pm.getFileId(), pm.getId(), pm.getType());
    try {
      getModelService().deletePreservationMetadata(pm, notify);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deletePreservationMetadata(PreservationMetadata.PreservationMetadataType type, String aipId,
    String representationId, String id, List<String> filePath, boolean notify)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry
      .registerDeleteOperationForPreservationMetadata(aipId, representationId, filePath, null, id, type);
    try {
      getModelService().deletePreservationMetadata(type, aipId, representationId, id, filePath, notify);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);

    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
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
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForOtherMetadata(om.getAipId(),
      OperationType.READ);
    try {
      Binary binary = stagingModelService.retrieveOtherMetadataBinary(om);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return binary;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Binary retrieveOtherMetadataBinary(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId, String fileSuffix, String type)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForOtherMetadata(aipId,
      OperationType.READ);
    try {
      Binary binary = stagingModelService.retrieveOtherMetadataBinary(aipId, representationId, fileDirectoryPath,
        fileId, fileSuffix, type);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return binary;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public OtherMetadata retrieveOtherMetadata(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId, String fileSuffix, String type)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForOtherMetadata(aipId,
      OperationType.READ);
    try {
      OtherMetadata otherMetadata = stagingModelService.retrieveOtherMetadata(aipId, representationId,
        fileDirectoryPath, fileId, fileSuffix, type);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return otherMetadata;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public OtherMetadata createOrUpdateOtherMetadata(String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId, String fileSuffix, String type, ContentPayload payload,
    String username, boolean notify)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForOtherMetadata(aipId,
      OperationType.UPDATE);
    try {
      OtherMetadata ret = getModelService().createOrUpdateOtherMetadata(aipId, representationId, fileDirectoryPath,
        fileId, fileSuffix, type, payload, username, notify);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteOtherMetadata(String aipId, String representationId, List<String> fileDirectoryPath, String fileId,
    String fileSuffix, String type, String username)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForOtherMetadata(aipId,
      OperationType.DELETE);
    try {
      getModelService().deleteOtherMetadata(aipId, representationId, fileDirectoryPath, fileId, fileSuffix, type,
        username);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | NotFoundException | AuthorizationDeniedException | RequestNotValidException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
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
  public List<LogEntry> importLogEntries(InputStream inputStream, String filename) throws AuthorizationDeniedException,
    GenericException, AlreadyExistsException, RequestNotValidException, NotFoundException {
    List<LogEntry> ret = getModelService().importLogEntries(inputStream, filename);
    for (LogEntry log : ret) {
      TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForLogEntry(log.getUUID(),
        OperationType.CREATE);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    }
    return ret;
  }

  @Override
  public void addLogEntry(LogEntry logEntry, Path logDirectory, boolean notify)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    // TODO: review this in order to add support for storageService
    mainModelService.addLogEntry(logEntry, logDirectory, notify);
  }

  @Override
  public void addLogEntry(LogEntry logEntry, Path logDirectory)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    // TODO: review this in order to add support for storageService
    mainModelService.addLogEntry(logEntry, logDirectory);
  }

  @Override
  public void findOldLogsAndSendThemToMaster(Path logDirectory, Path currentLogFile) throws IOException {
    // TODO: review this in order to add support for storageService
    mainModelService.findOldLogsAndSendThemToMaster(logDirectory, currentLogFile);
    // List<TransactionalModelOperationLog> operationLogs = new ArrayList<>();
    // try (LogEntryFileSystemIterable logEntries = new
    // LogEntryFileSystemIterable(logDirectory)) {
    // for (OptionalWithCause<LogEntry> logEntry : logEntries) {
    // if (logEntry.isPresent()) {
    // operationLogs.add(operationRegistry.registerOperationForLogEntry(logEntry.get().getId(),
    // OperationType.CREATE));
    // }
    // }
    // getModelService().findOldLogsAndSendThemToMaster(logDirectory,
    // currentLogFile);
    // } catch (IOException e) {
    // operationRegistry.updateOperationState(operationLogs,
    // OperationState.FAILURE);
    // throw e;
    // }
  }

  @Override
  public void findOldLogsAndMoveThemToStorage(Path logDirectory, Path currentLogFile)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, IOException {
    List<TransactionalModelOperationLog> operationLogs = new ArrayList<>();
    try (LogEntryFileSystemIterable logEntries = new LogEntryFileSystemIterable(logDirectory)) {
      for (OptionalWithCause<LogEntry> logEntry : logEntries) {
        if (logEntry.isPresent()) {
          operationLogs
            .add(operationRegistry.registerOperationForLogEntry(logEntry.get().getId(), OperationType.CREATE));
        }
      }
      getModelService().findOldLogsAndMoveThemToStorage(logDirectory, currentLogFile);
    } catch (IOException | RequestNotValidException | AuthorizationDeniedException | NotFoundException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
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
    return mainModelService.listJobReports(jobId);
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
    jobReport.setTransactionId(transaction.getId().toString());
    mainModelService.createOrUpdateJobReport(jobReport, cachedJob);
  }

  @Override
  public void createOrUpdateJobReport(Report jobReport, IndexedJob indexJob)
    throws GenericException, AuthorizationDeniedException {
    jobReport.setTransactionId(transaction.getId().toString());
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
    TransactionalModelOperationLog operationLog = operationRegistry.registerUpdateOperationForAIP(aipId);
    try {
      getModelService().updateAIPPermissions(aipId, permissions, updatedBy);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void updateAIPPermissions(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerUpdateOperationForAIP(aip.getId());
    try {
      getModelService().updateAIPPermissions(aip, updatedBy);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void updateDIPPermissions(DIP dip)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForDIP(dip.getId(),
      OperationType.UPDATE);
    try {
      getModelService().updateDIPPermissions(dip);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteTransferredResource(TransferredResource transferredResource)
    throws GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForTransferredResource(transferredResource.getFullPath(), OperationType.DELETE);
    try {
      getModelService().deleteTransferredResource(transferredResource);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Job updateJobInstanceId(Job job)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    return mainModelService.updateJobInstanceId(job);
  }

  @Override
  public Risk createRisk(Risk risk, boolean commit) throws GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForRisk(risk.getId(),
      OperationType.CREATE);
    try {
      Risk ret = getModelService().createRisk(risk, commit);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Risk updateRiskInstanceId(Risk risk, boolean commit) throws GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForRisk(risk.getId(),
      OperationType.UPDATE);
    try {
      Risk ret = getModelService().updateRiskInstanceId(risk, commit);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Risk updateRisk(Risk risk, Map<String, String> properties, boolean commit, int incidences)
    throws GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForRisk(risk.getId(),
      OperationType.UPDATE);
    try {
      Risk ret = getModelService().updateRisk(risk, properties, commit, incidences);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteRisk(String riskId, boolean commit)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForRisk(riskId,
      OperationType.DELETE);
    try {
      getModelService().deleteRisk(riskId, commit);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | NotFoundException | AuthorizationDeniedException | RequestNotValidException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Risk retrieveRisk(String riskId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForRisk(riskId,
      OperationType.READ);
    try {
      Risk ret = getModelService().retrieveRisk(riskId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public BinaryVersion retrieveVersion(String riskId, String versionId)
    throws RequestNotValidException, GenericException, NotFoundException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForRisk(riskId,
      OperationType.READ);
    try {
      BinaryVersion ret = getModelService().retrieveVersion(riskId, versionId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public BinaryVersion revertRiskVersion(String riskId, String versionId, Map<String, String> properties,
    boolean commit, int incidences)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForRisk(riskId,
      OperationType.UPDATE);
    try {
      BinaryVersion ret = getModelService().revertRiskVersion(riskId, versionId, properties, commit, incidences);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public RiskIncidence createRiskIncidence(RiskIncidence riskIncidence, boolean commit)
    throws AlreadyExistsException, NotFoundException, AuthorizationDeniedException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForRiskIncidence(riskIncidence.getId(), OperationType.CREATE);
    try {
      RiskIncidence ret = getModelService().createRiskIncidence(riskIncidence, commit);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AlreadyExistsException | NotFoundException | AuthorizationDeniedException | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public RiskIncidence updateRiskIncidenceInstanceId(RiskIncidence riskIncidence, boolean commit)
    throws GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForRiskIncidence(riskIncidence.getId(), OperationType.UPDATE);
    try {
      RiskIncidence ret = getModelService().updateRiskIncidenceInstanceId(riskIncidence, commit);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public RiskIncidence updateRiskIncidence(RiskIncidence riskIncidence, boolean commit)
    throws GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForRiskIncidence(riskIncidence.getId(), OperationType.UPDATE);
    try {
      RiskIncidence ret = getModelService().updateRiskIncidence(riskIncidence, commit);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteRiskIncidence(String riskIncidenceId, boolean commit)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForRiskIncidence(riskIncidenceId,
      OperationType.DELETE);
    try {
      getModelService().deleteRiskIncidence(riskIncidenceId, commit);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | NotFoundException | AuthorizationDeniedException | RequestNotValidException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public RiskIncidence retrieveRiskIncidence(String incidenceId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForRiskIncidence(incidenceId,
      OperationType.READ);
    try {
      RiskIncidence ret = getModelService().retrieveRiskIncidence(incidenceId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Notification createNotification(Notification notification, NotificationProcessor processor)
    throws GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForNotification(notification.getId(), OperationType.CREATE);
    try {
      Notification ret = getModelService().createNotification(notification, processor);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Notification updateNotificationInstanceId(Notification notification)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForNotification(notification.getId(), OperationType.UPDATE);
    try {
      Notification ret = getModelService().updateNotificationInstanceId(notification);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Notification updateNotification(Notification notification)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForNotification(notification.getId(), OperationType.UPDATE);
    try {
      Notification ret = getModelService().updateNotification(notification);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteNotification(String notificationId)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForNotification(notificationId,
      OperationType.DELETE);
    try {
      getModelService().deleteNotification(notificationId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Notification retrieveNotification(String notificationId)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForNotification(notificationId,
      OperationType.READ);
    try {
      Notification notification = getModelService().retrieveNotification(notificationId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return notification;
    } catch (GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Notification acknowledgeNotification(String notificationId, String token)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForNotification(notificationId,
      OperationType.UPDATE);
    try {
      Notification notification = getModelService().acknowledgeNotification(notificationId, token);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return notification;
    } catch (GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public CloseableIterable<OptionalWithCause<DIPFile>> listDIPFilesUnder(DIPFile f, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForDIPFile(f.getDipId(),
      f.getPath(), f.getId(), OperationType.READ);
    try {
      CloseableIterable<OptionalWithCause<DIPFile>> ret = getModelService().listDIPFilesUnder(f, recursive);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public CloseableIterable<OptionalWithCause<DIPFile>> listDIPFilesUnder(String dipId, List<String> directoryPath,
    String fileId, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForDIPFile(dipId, directoryPath,
      fileId, OperationType.READ);
    try {
      CloseableIterable<OptionalWithCause<DIPFile>> ret = getModelService().listDIPFilesUnder(dipId, directoryPath,
        fileId, recursive);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public CloseableIterable<OptionalWithCause<DIPFile>> listDIPFilesUnder(String dipId, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForDIP(dipId, OperationType.READ);
    try {
      CloseableIterable<OptionalWithCause<DIPFile>> ret = getModelService().listDIPFilesUnder(dipId, recursive);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void updateDIPInstanceId(DIP dip)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForDIP(dip.getId(),
      OperationType.UPDATE);
    try {
      getModelService().updateDIPInstanceId(dip);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DIP createDIP(DIP dip, boolean notify) throws GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForDIP(dip.getId(),
      OperationType.CREATE);
    try {
      DIP ret = getModelService().createDIP(dip, notify);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DIP updateDIP(DIP dip) throws GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForDIP(dip.getId(),
      OperationType.UPDATE);
    try {
      DIP ret = getModelService().updateDIP(dip);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteDIP(String dipId) throws GenericException, NotFoundException, AuthorizationDeniedException {
    DIP dip = getModelService().retrieveDIP(dipId);
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForDIP(dip.getId(),
      OperationType.DELETE);
    operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    getModelService().deleteDIP(dipId);
  }

  @Override
  public DIP retrieveDIP(String dipId) throws GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForDIP(dipId, OperationType.READ);
    try {
      DIP dip = stagingModelService.retrieveDIP(dipId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return dip;
    } catch (GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DIPFile createDIPFile(String dipId, List<String> directoryPath, String fileId, long size,
    ContentPayload contentPayload, boolean notify) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForDIPFile(dipId, directoryPath,
      fileId, OperationType.CREATE);
    try {
      DIPFile ret = getModelService().createDIPFile(dipId, directoryPath, fileId, size, contentPayload, notify);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AlreadyExistsException | AuthorizationDeniedException
      | NotFoundException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DIPFile createDIPFile(String dipId, List<String> directoryPath, String fileId, String dirName, boolean notify)
    throws RequestNotValidException, GenericException, AlreadyExistsException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForDIPFile(dipId, directoryPath,
      fileId, OperationType.CREATE);
    try {
      DIPFile ret = getModelService().createDIPFile(dipId, directoryPath, fileId, dirName, notify);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AlreadyExistsException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DIPFile updateDIPFile(String dipId, List<String> directoryPath, String oldFileId, String fileId, long size,
    ContentPayload contentPayload, boolean createIfNotExists, boolean notify) throws RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException, AlreadyExistsException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForDIPFile(dipId, directoryPath,
      oldFileId, OperationType.UPDATE);
    try {
      DIPFile ret = getModelService().updateDIPFile(dipId, directoryPath, oldFileId, fileId, size, contentPayload,
        createIfNotExists, notify);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException
      | AlreadyExistsException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteDIPFile(String dipId, List<String> directoryPath, String fileId, boolean notify)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForDIPFile(dipId, directoryPath,
      fileId, OperationType.DELETE);
    try {
      getModelService().deleteDIPFile(dipId, directoryPath, fileId, notify);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DIPFile retrieveDIPFile(String dipId, List<String> directoryPath, String fileId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForDIPFile(dipId, directoryPath,
      fileId, OperationType.READ);
    try {
      DIPFile ret = getModelService().retrieveDIPFile(dipId, directoryPath, fileId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void createSubmission(StorageService submissionStorage, StoragePath submissionStoragePath, String aipId)
    throws AlreadyExistsException, GenericException, RequestNotValidException, NotFoundException,
    AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerUpdateOperationForAIP(aipId);
    try {
      getModelService().createSubmission(submissionStorage, submissionStoragePath, aipId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (AlreadyExistsException | GenericException | RequestNotValidException | NotFoundException
      | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void createSubmission(Path submissionPath, String aipId) throws AlreadyExistsException, GenericException,
    RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerUpdateOperationForAIP(aipId);
    try {
      getModelService().createSubmission(submissionPath, aipId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (AlreadyExistsException | GenericException | RequestNotValidException | NotFoundException
      | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public File createDocumentation(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {
    List<TransactionalModelOperationLog> operationLogs = new ArrayList<>();
    if (representationId != null) {
      operationLogs = operationRegistry.registerOperationForRepresentation(aipId, representationId, OperationType.READ);
    } else {
      operationLogs.add(operationRegistry.registerReadOperationForAIP(aipId));
    }
    try {
      File ret = getModelService().createDocumentation(aipId, representationId, directoryPath, fileId, contentPayload);
      for (TransactionalModelOperationLog operation : operationLogs) {
        operationRegistry.updateOperationState(operation, OperationState.SUCCESS);
      }
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | AlreadyExistsException e) {
      for (TransactionalModelOperationLog operation : operationLogs) {
        operationRegistry.updateOperationState(operation, OperationState.FAILURE);
      }
      throw e;
    }
  }

  @Override
  public Long countDocumentationFiles(String aipId, String representationId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    List<TransactionalModelOperationLog> operationLogs = new ArrayList<>();
    if (representationId == null) {
      operationLogs.add(operationRegistry.registerReadOperationForAIP(aipId));
    } else {
      operationLogs = operationRegistry.registerOperationForRepresentation(aipId, representationId, OperationType.READ);
    }
    try {
      Long ret = getModelService().countDocumentationFiles(aipId, representationId);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Long countSubmissionFiles(String aipId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerReadOperationForAIP(aipId);
    try {
      Long ret = getModelService().countSubmissionFiles(aipId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Long countSchemaFiles(String aipId, String representationId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    List<TransactionalModelOperationLog> operationLogs = new ArrayList<>();
    if (representationId == null) {
      operationLogs.add(operationRegistry.registerReadOperationForAIP(aipId));
    } else {
      operationLogs = operationRegistry.registerOperationForRepresentation(aipId, representationId, OperationType.READ);
    }
    try {
      Long ret = getModelService().countSchemaFiles(aipId, representationId);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public boolean checkIfSchemaExists(String aipId, String representationId, List<String> directoryPath, String fileId)
    throws RequestNotValidException {
    if (!stagingModelService.checkIfSchemaExists(aipId, representationId, directoryPath, fileId)) {
      return mainModelService.checkIfSchemaExists(aipId, representationId, directoryPath, fileId);
    }
    return true;
  }

  private void checkIfSchemaExistsAndThrowException(String aipId, String representationId, List<String> directoryPath,
    String fileId) throws AlreadyExistsException, RequestNotValidException {
    if (checkIfSchemaExists(aipId, representationId, directoryPath, fileId)) {
      throw new AlreadyExistsException("Schema with id '" + fileId + "' already exists in AIP '" + aipId
        + "' and representation '" + representationId + "'.");
    }
  }

  @Override
  public void createSchema(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {

    checkIfSchemaExistsAndThrowException(aipId, representationId, directoryPath, fileId);

    TransactionalModelOperationLog operationLog = operationRegistry.registerUpdateOperationForAIP(aipId);
    try {
      getModelService().createSchema(aipId, representationId, directoryPath, fileId, contentPayload);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);

    } catch (RequestNotValidException | GenericException | AlreadyExistsException | AuthorizationDeniedException
      | NotFoundException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public <T extends IsRODAObject> Optional<LiteRODAObject> retrieveLiteFromObject(T object) {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(object, OperationType.READ);
    Optional<LiteRODAObject> ret = getModelService().retrieveLiteFromObject(object);
    operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    return ret;
  }

  @Override
  public <T extends IsModelObject> OptionalWithCause<T> retrieveObjectFromLite(LiteRODAObject liteRODAObject) {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(liteRODAObject.getInfo(),
      OperationType.READ);
    OptionalWithCause<T> ret = getModelService().retrieveObjectFromLite(liteRODAObject);
    operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    return ret;
  }

  @Override
  public TransferredResource retrieveTransferredResource(String fullPath) {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForTransferredResource(fullPath,
      OperationType.READ);
    TransferredResource ret = getModelService().retrieveTransferredResource(fullPath);
    operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    return ret;
  }

  @Override
  public <T extends IsRODAObject> CloseableIterable<OptionalWithCause<T>> list(Class<T> objectClass)
    throws RODAException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(objectClass.getName(),
      OperationType.READ);
    try {
      CloseableIterable<OptionalWithCause<T>> ret = getModelService().list(objectClass);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RODAException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public <T extends IsRODAObject> CloseableIterable<OptionalWithCause<LiteRODAObject>> listLite(Class<T> objectClass)
    throws RODAException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(objectClass.getName(),
      OperationType.READ);
    try {
      CloseableIterable<OptionalWithCause<LiteRODAObject>> ret = getModelService().listLite(objectClass);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RODAException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public CloseableIterable<OptionalWithCause<LogEntry>> listLogEntries() {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(LogEntry.class.getName(),
      OperationType.READ);
    CloseableIterable<OptionalWithCause<LogEntry>> ret = getModelService().listLogEntries();
    operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    return ret;
  }

  @Override
  public CloseableIterable<OptionalWithCause<LogEntry>> listLogEntries(int daysToIndex) {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(LogEntry.class.getName(),
      OperationType.READ);
    CloseableIterable<OptionalWithCause<LogEntry>> ret = getModelService().listLogEntries(daysToIndex);
    operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    return ret;
  }

  @Override
  public CloseableIterable<Resource> listLogFilesInStorage() {
    return mainModelService.listLogFilesInStorage();
  }

  @Override
  public boolean hasObjects(Class<? extends IsRODAObject> objectClass) {
    return mainModelService.hasObjects(objectClass);
  }

  @Override
  public boolean checkObjectPermission(String username, String permissionType, String objectClass, String id)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = null;
    if (DIP.class.getName().equals(objectClass)) {
      operationLog = operationRegistry.registerOperationForDIP(id, OperationType.READ);
    } else if (AIP.class.getName().equals(objectClass)) {
      operationLog = operationRegistry.registerReadOperationForAIP(id);
    } else {
      LOGGER.warn(
        "Can't register read operation for checking object permission for unsupported object class ({} of class {})",
        objectClass, id);
    }
    try {
      boolean ret = getModelService().checkObjectPermission(username, permissionType, objectClass, id);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | NotFoundException | AuthorizationDeniedException | RequestNotValidException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public RepresentationInformation createRepresentationInformation(RepresentationInformation ri, String createdBy,
    boolean commit) throws GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForRepresentationInformation(ri.getId(), OperationType.CREATE);
    try {
      RepresentationInformation ret = getModelService().createRepresentationInformation(ri, createdBy, commit);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public RepresentationInformation updateRepresentationInformation(RepresentationInformation ri, String updatedBy,
    boolean commit) throws GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForRepresentationInformation(ri.getId(), OperationType.UPDATE);
    try {
      RepresentationInformation ret = getModelService().updateRepresentationInformation(ri, updatedBy, commit);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public RepresentationInformation updateRepresentationInformationInstanceId(RepresentationInformation ri,
    String updatedBy, boolean notify)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForRepresentationInformation(ri.getId(), OperationType.UPDATE);
    try {
      RepresentationInformation ret = getModelService().updateRepresentationInformationInstanceId(ri, updatedBy,
        notify);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | NotFoundException | AuthorizationDeniedException | RequestNotValidException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteRepresentationInformation(String representationInformationId, boolean commit)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForRepresentationInformation(representationInformationId, OperationType.DELETE);
    try {
      getModelService().deleteRepresentationInformation(representationInformationId, commit);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | NotFoundException | AuthorizationDeniedException | RequestNotValidException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public RepresentationInformation retrieveRepresentationInformation(String representationInformationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForRepresentationInformation(representationInformationId, OperationType.READ);
    try {
      RepresentationInformation ret = getModelService().retrieveRepresentationInformation(representationInformationId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalHold retrieveDisposalHold(String disposalHoldId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForDisposalHold(disposalHoldId,
      OperationType.READ);
    try {
      DisposalHold ret = getModelService().retrieveDisposalHold(disposalHoldId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalHold createDisposalHold(DisposalHold disposalHold, String createdBy) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException, AlreadyExistsException, NotFoundException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForDisposalHold(disposalHold.getId(), OperationType.CREATE);
    try {
      DisposalHold ret = getModelService().createDisposalHold(disposalHold, createdBy);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | AlreadyExistsException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalHold updateDisposalHoldFirstUseDate(DisposalHold disposalHold, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, IllegalOperationException,
    GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForDisposalHold(disposalHold.getId(), OperationType.UPDATE);
    try {
      DisposalHold ret = getModelService().updateDisposalHoldFirstUseDate(disposalHold, updatedBy);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | IllegalOperationException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalHold updateDisposalHold(DisposalHold disposalHold, String updatedBy, String details)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, IllegalOperationException,
    GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForDisposalHold(disposalHold.getId(), OperationType.UPDATE);
    try {
      DisposalHold ret = getModelService().updateDisposalHold(disposalHold, updatedBy, details);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | IllegalOperationException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalHold updateDisposalHold(DisposalHold disposalHold, String updatedBy, boolean updateFirstUseDate,
    String details) throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException,
    IllegalOperationException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForDisposalHold(disposalHold.getId(), OperationType.UPDATE);
    try {
      DisposalHold ret = getModelService().updateDisposalHold(disposalHold, updatedBy, updateFirstUseDate, details);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | IllegalOperationException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteDisposalHold(String disposalHoldId) throws RequestNotValidException, NotFoundException,
    GenericException, AuthorizationDeniedException, IllegalOperationException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForDisposalHold(disposalHoldId,
      OperationType.DELETE);
    try {
      getModelService().deleteDisposalHold(disposalHoldId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | IllegalOperationException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalHolds listDisposalHolds()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, IOException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(DisposalHold.class.getName(),
      OperationType.READ);
    try {
      DisposalHolds ret = getModelService().listDisposalHolds();
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | IOException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalAIPMetadata createDisposalHoldAssociation(String aipId, String disposalHoldId, Date associatedOn,
    String associatedBy)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    List<TransactionalModelOperationLog> operationLogs = new ArrayList<>();
    operationLogs.add(operationRegistry.registerOperationForDisposalHold(disposalHoldId, OperationType.READ));
    operationLogs.add(operationRegistry.registerUpdateOperationForAIP(aipId));
    try {
      DisposalAIPMetadata ret = getModelService().createDisposalHoldAssociation(aipId, disposalHoldId, associatedOn,
        associatedBy);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | GenericException | NotFoundException | RequestNotValidException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public List<DisposalHold> retrieveDirectActiveDisposalHolds(String aipId)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    List<TransactionalModelOperationLog> operationLogs = new ArrayList<>();
    operationLogs.add(operationRegistry.registerReadOperationForAIP(aipId));
    try {
      List<DisposalHold> ret = getModelService().retrieveDirectActiveDisposalHolds(aipId);
      for (DisposalHold hold : ret) {
        operationLogs.add(operationRegistry.registerOperationForDisposalHold(hold.getId(), OperationType.READ));
      }
      for (TransactionalModelOperationLog operation : operationLogs) {
        operationRegistry.updateOperationState(operation, OperationState.SUCCESS);
      }
      return ret;
    } catch (NotFoundException | AuthorizationDeniedException | GenericException | RequestNotValidException e) {
      for (TransactionalModelOperationLog operation : operationLogs) {
        operationRegistry.updateOperationState(operation, OperationState.FAILURE);
      }
      throw e;
    }
  }

  @Override
  public boolean onDisposalHold(String aipId)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerReadOperationForAIP(aipId);
    try {
      boolean ret = getModelService().onDisposalHold(aipId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | AuthorizationDeniedException | GenericException | RequestNotValidException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public boolean isAIPOnDirectHold(String aipId, String holdId)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    List<TransactionalModelOperationLog> operationLogs = new ArrayList<>();
    operationLogs.add(operationRegistry.registerReadOperationForAIP(aipId));
    operationLogs.add(operationRegistry.registerOperationForDisposalHold(holdId, OperationType.READ));
    try {
      boolean ret = getModelService().isAIPOnDirectHold(aipId, holdId);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | AuthorizationDeniedException | GenericException | RequestNotValidException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalSchedule createDisposalSchedule(DisposalSchedule disposalSchedule, String createdBy)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForDisposalSchedule(disposalSchedule.getId(), OperationType.CREATE);
    try {
      DisposalSchedule ret = getModelService().createDisposalSchedule(disposalSchedule, createdBy);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | AuthorizationDeniedException | GenericException | RequestNotValidException
      | AlreadyExistsException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalSchedule updateDisposalSchedule(DisposalSchedule disposalSchedule, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException,
    IllegalOperationException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForDisposalSchedule(disposalSchedule.getId(), OperationType.UPDATE);
    try {
      DisposalSchedule ret = getModelService().updateDisposalSchedule(disposalSchedule, updatedBy);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | AuthorizationDeniedException | GenericException | RequestNotValidException
      | IllegalOperationException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalSchedule retrieveDisposalSchedule(String disposalScheduleId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForDisposalSchedule(disposalScheduleId, OperationType.READ);
    try {
      DisposalSchedule ret = getModelService().retrieveDisposalSchedule(disposalScheduleId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | AuthorizationDeniedException | GenericException | RequestNotValidException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalSchedules listDisposalSchedules()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, IOException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(DisposalSchedule.class.getName(),
      OperationType.READ);
    try {
      DisposalSchedules ret = getModelService().listDisposalSchedules();
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | IOException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteDisposalSchedule(String disposalScheduleId) throws NotFoundException, GenericException,
    AuthorizationDeniedException, RequestNotValidException, IllegalOperationException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForDisposalSchedule(disposalScheduleId, OperationType.DELETE);
    try {
      getModelService().deleteDisposalSchedule(disposalScheduleId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
      | IllegalOperationException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalConfirmation retrieveDisposalConfirmation(String disposalConfirmationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForDisposalConfirmation(disposalConfirmationId, OperationType.READ);
    try {
      DisposalConfirmation ret = getModelService().retrieveDisposalConfirmation(disposalConfirmationId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void addDisposalHoldEntry(String disposalConfirmationId, DisposalHold disposalHold)
    throws GenericException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForDisposalConfirmation(disposalConfirmationId, OperationType.READ);
    try {
      getModelService().addDisposalHoldEntry(disposalConfirmationId, disposalHold);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | RequestNotValidException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void addDisposalHoldTransitiveEntry(String disposalConfirmationId, DisposalHold transitiveDisposalHold)
    throws RequestNotValidException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForDisposalConfirmation(disposalConfirmationId, OperationType.READ);
    try {
      getModelService().addDisposalHoldTransitiveEntry(disposalConfirmationId, transitiveDisposalHold);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (RequestNotValidException | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void addDisposalScheduleEntry(String disposalConfirmationId, DisposalSchedule disposalSchedule)
    throws RequestNotValidException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForDisposalConfirmation(disposalConfirmationId, OperationType.UPDATE);
    try {
      getModelService().addDisposalScheduleEntry(disposalConfirmationId, disposalSchedule);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (RequestNotValidException | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void addAIPEntry(String disposalConfirmationId, DisposalConfirmationAIPEntry entry)
    throws RequestNotValidException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForDisposalConfirmation(disposalConfirmationId, OperationType.UPDATE);
    try {
      getModelService().addAIPEntry(disposalConfirmationId, entry);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (RequestNotValidException | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalConfirmation updateDisposalConfirmation(DisposalConfirmation disposalConfirmation)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForDisposalConfirmation(disposalConfirmation.getId(), OperationType.UPDATE);
    try {
      DisposalConfirmation ret = getModelService().updateDisposalConfirmation(disposalConfirmation);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalConfirmation createDisposalConfirmation(DisposalConfirmation disposalConfirmation, String createdBy)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForDisposalConfirmation(disposalConfirmation.getId(), OperationType.CREATE);
    try {
      DisposalConfirmation ret = getModelService().createDisposalConfirmation(disposalConfirmation, createdBy);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException
      | AlreadyExistsException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteDisposalConfirmation(String disposalConfirmationId) throws AuthorizationDeniedException,
    RequestNotValidException, NotFoundException, GenericException, IllegalOperationException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForDisposalConfirmation(disposalConfirmationId, OperationType.DELETE);
    try {
      getModelService().deleteDisposalConfirmation(disposalConfirmationId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException
      | IllegalOperationException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }

  }

  @Override
  public DisposalHoldsAIPMetadata listDisposalHoldsAssociation(String aipId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerReadOperationForAIP(aipId);
    try {
      DisposalHoldsAIPMetadata ret = getModelService().listDisposalHoldsAssociation(aipId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalTransitiveHoldsAIPMetadata listTransitiveDisposalHolds(String aipId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerReadOperationForAIP(aipId);
    try {
      DisposalTransitiveHoldsAIPMetadata ret = getModelService().listTransitiveDisposalHolds(aipId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalRule createDisposalRule(DisposalRule disposalRule, String createdBy) throws RequestNotValidException,
    NotFoundException, GenericException, AlreadyExistsException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForDisposalRule(disposalRule.getId(), OperationType.CREATE);
    try {
      DisposalRule ret = getModelService().createDisposalRule(disposalRule, createdBy);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AlreadyExistsException
      | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalRule updateDisposalRule(DisposalRule disposalRule, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForDisposalRule(disposalRule.getId(), OperationType.UPDATE);
    try {
      DisposalRule ret = getModelService().updateDisposalRule(disposalRule, updatedBy);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteDisposalRule(String disposalRuleId, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, IOException, GenericException, NotFoundException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForDisposalRule(disposalRuleId,
      OperationType.DELETE);
    try {
      getModelService().deleteDisposalRule(disposalRuleId, updatedBy);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | IOException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalRule retrieveDisposalRule(String disposalRuleId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperationForDisposalRule(disposalRuleId,
      OperationType.READ);
    try {
      DisposalRule ret = getModelService().retrieveDisposalRule(disposalRuleId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalRules listDisposalRules()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, IOException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(DisposalRule.class.getName(),
      OperationType.READ);
    try {
      DisposalRules ret = getModelService().listDisposalRules();
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | IOException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DistributedInstance createDistributedInstance(DistributedInstance distributedInstance, String createdBy)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, AlreadyExistsException,
    NotFoundException, IllegalOperationException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForDistributedInstance(distributedInstance.getId(), OperationType.CREATE);
    try {
      DistributedInstance ret = getModelService().createDistributedInstance(distributedInstance, createdBy);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | AuthorizationDeniedException | RequestNotValidException | AlreadyExistsException
      | NotFoundException | IllegalOperationException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DistributedInstances listDistributedInstances()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, IOException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperation(DistributedInstance.class.getName(), OperationType.READ);
    try {
      DistributedInstances ret = getModelService().listDistributedInstances();
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | IOException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DistributedInstance retrieveDistributedInstance(String distributedInstanceId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForDistributedInstance(distributedInstanceId, OperationType.READ);
    try {
      DistributedInstance ret = getModelService().retrieveDistributedInstance(distributedInstanceId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | AuthorizationDeniedException | RequestNotValidException | NotFoundException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteDistributedInstance(String distributedInstanceId)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForDistributedInstance(distributedInstanceId, OperationType.DELETE);
    try {
      getModelService().deleteDistributedInstance(distributedInstanceId);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | AuthorizationDeniedException | RequestNotValidException | NotFoundException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DistributedInstance updateDistributedInstance(DistributedInstance distributedInstance, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry
      .registerOperationForDistributedInstance(distributedInstance.getId(), OperationType.UPDATE);
    try {
      DistributedInstance ret = getModelService().updateDistributedInstance(distributedInstance, updatedBy);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | AuthorizationDeniedException | RequestNotValidException | NotFoundException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
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
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(object, OperationType.READ);
    try {
      StorageService ret = getModelService().resolveTemporaryResourceShallow(jobId, object, pathPartials);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | RequestNotValidException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public StorageService resolveTemporaryResourceShallow(String jobId, StorageService storage, IsRODAObject object,
    String... pathPartials) throws GenericException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(object, OperationType.READ);
    try {
      StorageService ret = getModelService().resolveTemporaryResourceShallow(jobId, storage, object, pathPartials);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | RequestNotValidException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public StorageService resolveTemporaryResourceShallow(String jobId, LiteRODAObject object, String... pathPartials)
    throws GenericException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(object.getInfo(),
      OperationType.READ);
    try {
      StorageService ret = getModelService().resolveTemporaryResourceShallow(jobId, object, pathPartials);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | RequestNotValidException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public StorageService resolveTemporaryResourceShallow(String jobId, StorageService storage, LiteRODAObject object,
    String... pathPartials) throws GenericException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(object.getInfo(),
      OperationType.READ);
    try {
      StorageService ret = getModelService().resolveTemporaryResourceShallow(jobId, storage, object, pathPartials);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | RequestNotValidException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Binary getBinary(IsRODAObject object, String... pathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(object, OperationType.READ);
    try {
      Binary ret = getModelService().getBinary(object, pathPartials);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | AuthorizationDeniedException | NotFoundException | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Binary getBinary(LiteRODAObject lite, String... pathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(lite.getInfo(),
      OperationType.READ);
    try {
      Binary ret = getModelService().getBinary(lite, pathPartials);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | AuthorizationDeniedException | NotFoundException | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public BinaryVersion getBinaryVersion(IsRODAObject object, String version, List<String> pathPartials)
    throws RequestNotValidException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(object, OperationType.READ);
    try {
      BinaryVersion ret = getModelService().getBinaryVersion(object, version, pathPartials);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public BinaryVersion getBinaryVersion(LiteRODAObject lite, String version, List<String> pathPartials)
    throws RequestNotValidException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(lite.getInfo(),
      OperationType.READ);
    try {
      BinaryVersion ret = getModelService().getBinaryVersion(lite, version, pathPartials);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public CloseableIterable<BinaryVersion> listBinaryVersions(IsRODAObject object)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(object, OperationType.READ);
    try {
      CloseableIterable<BinaryVersion> ret = getModelService().listBinaryVersions(object);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;

    } catch (RequestNotValidException | AuthorizationDeniedException | NotFoundException | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public CloseableIterable<BinaryVersion> listBinaryVersions(LiteRODAObject lite)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(lite.getInfo(),
      OperationType.READ);
    try {
      CloseableIterable<BinaryVersion> ret = getModelService().listBinaryVersions(lite);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteBinaryVersion(IsRODAObject object, String version)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(object, OperationType.DELETE);
    try {
      getModelService().deleteBinaryVersion(object, version);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (RequestNotValidException | AuthorizationDeniedException | NotFoundException | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteBinaryVersion(LiteRODAObject lite, String version)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(lite.getInfo(),
      OperationType.DELETE);
    try {
      getModelService().deleteBinaryVersion(lite, version);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Binary updateBinaryContent(IsRODAObject object, ContentPayload payload, boolean asReference,
    boolean createIfNotExists, boolean snapshotCurrentVersion, Map<String, String> properties)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(object, OperationType.UPDATE);
    try {
      Binary ret = getModelService().updateBinaryContent(object, payload, asReference, createIfNotExists,
        snapshotCurrentVersion, properties);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Binary updateBinaryContent(LiteRODAObject lite, ContentPayload payload, boolean asReference,
    boolean createIfNotExists, boolean snapshotCurrentVersion, Map<String, String> properties)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(lite.getInfo(),
      OperationType.UPDATE);
    try {
      Binary ret = getModelService().updateBinaryContent(lite, payload, asReference, createIfNotExists,
        snapshotCurrentVersion, properties);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Directory createDirectory(IsRODAObject object, String... pathPartials)
    throws AuthorizationDeniedException, AlreadyExistsException, GenericException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(object, OperationType.UPDATE);
    try {
      Directory ret = getModelService().createDirectory(object, pathPartials);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | AlreadyExistsException | GenericException | RequestNotValidException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Directory createDirectory(LiteRODAObject lite, String... pathPartials)
    throws AuthorizationDeniedException, AlreadyExistsException, GenericException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(lite.getInfo(),
      OperationType.UPDATE);
    try {
      Directory ret = getModelService().createDirectory(lite, pathPartials);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | AlreadyExistsException | GenericException | RequestNotValidException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public boolean hasDirectory(IsRODAObject object, String... pathPartials) throws RequestNotValidException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(object, OperationType.READ);
    try {
      boolean ret = getModelService().hasDirectory(object, pathPartials);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public boolean hasDirectory(LiteRODAObject lite, String... pathPartials)
    throws RequestNotValidException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(lite.getInfo(),
      OperationType.READ);
    try {
      boolean ret = getModelService().hasDirectory(lite, pathPartials);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DirectResourceAccess getDirectAccess(IsRODAObject object, StorageService storage, String... pathPartials)
    throws RequestNotValidException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(object, OperationType.READ);
    try {
      DirectResourceAccess ret = getModelService().getDirectAccess(object, storage, pathPartials);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DirectResourceAccess getDirectAccess(LiteRODAObject lite, StorageService storage, String... pathPartials)
    throws RequestNotValidException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(lite.getInfo(),
      OperationType.READ);
    try {
      DirectResourceAccess ret = getModelService().getDirectAccess(lite, storage, pathPartials);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DirectResourceAccess getDirectAccess(IsRODAObject object, String... pathPartials)
    throws RequestNotValidException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(object, OperationType.READ);
    try {
      DirectResourceAccess ret = getModelService().getDirectAccess(object, pathPartials);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DirectResourceAccess getDirectAccess(LiteRODAObject lite, String... pathPartials)
    throws RequestNotValidException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(lite.getInfo(),
      OperationType.READ);
    try {
      DirectResourceAccess ret = getModelService().getDirectAccess(lite, pathPartials);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;

    } catch (RequestNotValidException | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public int importAll(IndexService index, FileStorageService fromStorage, boolean importJobs)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException {
    // TODO: This method should be reviewed
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(IsRODAObject.class.getName(),
      OperationType.CREATE);
    try {
      int ret = getModelService().importAll(index, fromStorage, importJobs);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException
      | AlreadyExistsException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void exportAll(StorageService toStorage) {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(IsRODAObject.class.getName(),
      OperationType.READ);
    getModelService().exportAll(toStorage);
    operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
  }

  @Override
  public void importObject(IsRODAObject object, StorageService fromStorage) {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(object, OperationType.UPDATE);
    getModelService().importObject(object, fromStorage);
    operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
  }

  @Override
  public void exportObject(IsRODAObject object, StorageService toStorage, String... toPathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException, NotFoundException,
    GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(object, OperationType.READ);
    try {
      getModelService().exportObject(object, toStorage, toPathPartials);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (RequestNotValidException | AuthorizationDeniedException | AlreadyExistsException | NotFoundException
      | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }

  }

  @Override
  public void exportObject(LiteRODAObject lite, StorageService toStorage, String... toPathPartials)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, AlreadyExistsException,
    NotFoundException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(lite.getInfo(),
      OperationType.READ);
    try {
      getModelService().exportObject(lite, toStorage, toPathPartials);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | AlreadyExistsException
      | NotFoundException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void exportToPath(IsRODAObject object, Path toPath, boolean replaceExisting, String... fromPathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(object, OperationType.READ);
    try {
      getModelService().exportToPath(object, toPath, replaceExisting, fromPathPartials);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (RequestNotValidException | AuthorizationDeniedException | AlreadyExistsException | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void exportToPath(LiteRODAObject lite, Path toPath, boolean replaceExisting, String... fromPathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(lite.getInfo(),
      OperationType.READ);
    try {
      getModelService().exportToPath(lite, toPath, replaceExisting, fromPathPartials);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (RequestNotValidException | AuthorizationDeniedException | AlreadyExistsException | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public ConsumesOutputStream exportObjectToStream(IsRODAObject object, String... pathPartials)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(object, OperationType.READ);
    try {
      ConsumesOutputStream ret = getModelService().exportObjectToStream(object, pathPartials);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public ConsumesOutputStream exportObjectToStream(LiteRODAObject lite, String... pathPartials)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(lite.getInfo(),
      OperationType.READ);
    try {
      ConsumesOutputStream ret = getModelService().exportObjectToStream(lite, pathPartials);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public ConsumesOutputStream exportObjectToStream(IsRODAObject object, String name, boolean addTopDirectory,
    String... pathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(object, OperationType.READ);
    try {
      ConsumesOutputStream ret = getModelService().exportObjectToStream(object, name, addTopDirectory, pathPartials);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | AuthorizationDeniedException | NotFoundException | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public ConsumesOutputStream exportObjectToStream(LiteRODAObject lite, String name, boolean addTopDirectory,
    String... pathPartials)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(lite.getInfo(),
      OperationType.READ);
    try {
      ConsumesOutputStream ret = getModelService().exportObjectToStream(lite, name, addTopDirectory, pathPartials);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void moveObject(LiteRODAObject fromPath, LiteRODAObject toPath) throws AuthorizationDeniedException,
    RequestNotValidException, AlreadyExistsException, NotFoundException, GenericException {

    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(fromPath.getInfo(),
      OperationType.DELETE);
    TransactionalModelOperationLog moveOperationLog = operationRegistry.registerOperation(toPath.getInfo(),
      OperationType.CREATE);
    try {
      getModelService().moveObject(fromPath, toPath);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      operationRegistry.updateOperationState(moveOperationLog, OperationState.SUCCESS);
    } catch (AuthorizationDeniedException | RequestNotValidException | AlreadyExistsException | NotFoundException
      | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      operationRegistry.updateOperationState(moveOperationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public String getObjectPathAsString(IsRODAObject object, boolean skipContainer) throws RequestNotValidException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(object, OperationType.READ);
    try {
      String ret = getModelService().getObjectPathAsString(object, skipContainer);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public String getObjectPathAsString(LiteRODAObject lite, boolean skipContainer)
    throws RequestNotValidException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(lite.getInfo(),
      OperationType.READ);
    try {
      String ret = getModelService().getObjectPathAsString(lite, skipContainer);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public boolean existsInStorage(LiteRODAObject lite, String... pathPartials)
    throws RequestNotValidException, GenericException {
    TransactionalModelOperationLog operationLog = operationRegistry.registerOperation(lite.getInfo(),
      OperationType.READ);
    try {
      boolean ret = getModelService().existsInStorage(lite, pathPartials);
      operationRegistry.updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException e) {
      operationRegistry.updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Date retrieveFileCreationDate(File file) throws RequestNotValidException, GenericException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerOperationForFile(file.getAipId(),
      file.getRepresentationId(), file.getPath(), file.getId(), OperationType.READ);
    try {
      Date ret = getModelService().retrieveFileCreationDate(file);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Date retrievePreservationMetadataCreationDate(PreservationMetadata pm)
    throws RequestNotValidException, GenericException {
    List<TransactionalModelOperationLog> operationLogs = operationRegistry.registerReadOperationForPreservationMetadata(
      pm.getAipId(), pm.getRepresentationId(), pm.getFileDirectoryPath(), pm.getFileId(), pm.getId(), pm.getType());
    try {
      Date ret = getModelService().retrievePreservationMetadataCreationDate(pm);
      operationRegistry.updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException e) {
      operationRegistry.updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
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
    return mainModelService.notifyUserCreated(user);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyUserUpdated(User user) {
    return mainModelService.notifyUserUpdated(user);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyUserDeleted(String userID) {
    return mainModelService.notifyUserDeleted(userID);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyGroupCreated(Group group) {
    return mainModelService.notifyGroupCreated(group);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyGroupUpdated(Group group) {
    return mainModelService.notifyGroupUpdated(group);
  }

  @Override
  public ReturnWithExceptionsWrapper notifyGroupDeleted(String groupID) {
    return mainModelService.notifyGroupDeleted(groupID);
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

  @Override
  public void commit() throws RODATransactionException {
    for (TransactionalModelOperationLog modelOperation : transactionLogService
      .getModelOperations(transaction.getId())) {
      LiteRODAObject liteRODAObject = new LiteRODAObject(modelOperation.getLiteObject());
      OptionalWithCause<Class<IsRODAObject>> isRODAObjectClassOptionalWithCause = LiteRODAObjectFactory
        .getClass(liteRODAObject);

      if (isRODAObjectClassOptionalWithCause.isPresent()) {
        Class<IsRODAObject> isRODAObjectClass = isRODAObjectClassOptionalWithCause.get();
        if (operationRegistry.isLockableClass(isRODAObjectClass)) {
          PluginHelper.releaseObjectLock(modelOperation.getLiteObject(), transaction.getRequestId().toString());
        }
      }
    }
  }

  @Override
  public void rollback() throws RODATransactionException {
    for (TransactionalModelOperationLog modelOperation : transactionLogService
      .getModelOperations(transaction.getId())) {
      transactionLogService.updateModelOperationState(modelOperation.getId(), OperationState.ROLLING_BACK);
      LiteRODAObject liteRODAObject = new LiteRODAObject(modelOperation.getLiteObject());
      OptionalWithCause<IsRODAObject> isRODAObjectOptionalWithCause = LiteRODAObjectFactory.get(this, liteRODAObject);

      if (isRODAObjectOptionalWithCause.isPresent()) {
        IsRODAObject rodaObject = isRODAObjectOptionalWithCause.get();
        try {
          TransactionModelRollbackHandler.processObject(rodaObject, modelOperation, mainModelService,
            stagingModelService);
          transactionLogService.updateModelOperationState(modelOperation.getId(), OperationState.ROLLED_BACK);
        } catch (AuthorizationDeniedException | GenericException | NotFoundException | RequestNotValidException e) {
          transactionLogService.updateModelOperationState(modelOperation.getId(), OperationState.ROLL_BACK_FAILURE);
          throw new RODATransactionException("Error during rollback for object: " + rodaObject.getId(), e);
        } finally {
          PluginHelper.releaseObjectLock(modelOperation.getLiteObject(), transaction.getRequestId().toString());
        }
      } else {
        PluginHelper.releaseObjectLock(modelOperation.getLiteObject(), transaction.getRequestId().toString());
      }
    }
  }
}
