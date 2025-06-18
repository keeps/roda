package org.roda.core.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

import org.roda.core.common.ReturnWithExceptionsWrapper;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.notifications.NotificationProcessor;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.SecureString;
import org.roda.core.data.exceptions.*;
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
import org.roda.core.data.v2.ip.*;
import org.roda.core.data.v2.ip.metadata.*;
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
import org.roda.core.storage.*;
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
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aipId, OperationType.READ);
    try {
      AIP aip = stagingModelService.retrieveAIP(aipId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return aip;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public AIP createAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath, boolean notify,
    String createdBy) throws RequestNotValidException, GenericException, AuthorizationDeniedException,
    AlreadyExistsException, NotFoundException, ValidationException {
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aipId, OperationType.CREATE);
    try {
      AIP ret = getModelService().createAIP(aipId, sourceStorage, sourcePath, notify, createdBy);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | AlreadyExistsException
      | NotFoundException | ValidationException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
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
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aipId, OperationType.CREATE);
    try {
      AIP ret = getModelService().createAIP(parentId, type, permissions, ingestSIPIds, ingestJobId, notify, createdBy,
        isGhost, aipId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AlreadyExistsException
      | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
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
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aipId, OperationType.CREATE);
    try {
      AIP ret = getModelService().createAIP(parentId, type, permissions, createdBy, aipId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AlreadyExistsException
      | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
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
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aipId, OperationType.CREATE);
    try {
      AIP ret = getModelService().createAIP(state, parentId, type, permissions, createdBy, aipId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AlreadyExistsException
      | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
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
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aipId, OperationType.CREATE);
    try {
      AIP ret = getModelService().createAIP(state, parentId, type, permissions, notify, createdBy, aipId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AlreadyExistsException
      | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
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
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aipId, OperationType.CREATE);
    try {
      AIP ret = getModelService().createAIP(state, parentId, type, permissions, ingestSIPUUID, ingestSIPIds,
        ingestJobId, notify, createdBy, aipId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AlreadyExistsException
      | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public AIP createAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath, String createdBy)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, AlreadyExistsException,
    NotFoundException, ValidationException {
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aipId, OperationType.CREATE);
    try {
      AIP ret = getModelService().createAIP(aipId, sourceStorage, sourcePath, createdBy);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | AlreadyExistsException
      | NotFoundException | ValidationException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
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
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aipId, OperationType.UPDATE);
    try {
      AIP ret = getModelService().updateAIP(aipId, sourceStorage, sourcePath, updatedBy);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | AlreadyExistsException | ValidationException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public AIP destroyAIP(AIP aip, String updatedBy)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aip.getId(), OperationType.DELETE);
    try {
      AIP ret = getModelService().destroyAIP(aip, updatedBy);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | GenericException | NotFoundException | RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public AIP updateAIP(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aip.getId(), OperationType.UPDATE);
    try {
      AIP ret = getModelService().updateAIP(aip, updatedBy);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public AIP updateAIPState(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aip.getId(), OperationType.UPDATE);
    try {
      AIP ret = getModelService().updateAIPState(aip, updatedBy);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public AIP updateAIPInstanceId(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aip.getId(), OperationType.UPDATE);
    try {
      AIP ret = getModelService().updateAIPInstanceId(aip, updatedBy);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public AIP moveAIP(String aipId, String parentId, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aipId, OperationType.UPDATE);
    try {
      AIP ret = getModelService().moveAIP(aipId, parentId, updatedBy);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteAIP(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aipId, OperationType.DELETE);
    try {
      getModelService().deleteAIP(aipId);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void changeAIPType(String aipId, String type, String updatedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aipId, OperationType.UPDATE);
    try {
      getModelService().changeAIPType(aipId, type, updatedBy);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Binary retrieveDescriptiveMetadataBinary(String aipId, String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForDescriptiveMetadata(aipId, null,
      descriptiveMetadataId, OperationType.READ);

    try {
      Binary binary = stagingModelService.retrieveDescriptiveMetadataBinary(aipId, descriptiveMetadataId);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return binary;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Binary retrieveDescriptiveMetadataBinary(String aipId, String representationId, String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForDescriptiveMetadata(aipId,
      representationId, descriptiveMetadataId, OperationType.READ);

    try {
      Binary binary = stagingModelService.retrieveDescriptiveMetadataBinary(aipId, representationId,
        descriptiveMetadataId);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return binary;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DescriptiveMetadata retrieveDescriptiveMetadata(String aipId, String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForDescriptiveMetadata(aipId, null,
      descriptiveMetadataId, OperationType.READ);

    try {
      DescriptiveMetadata descriptiveMetadata = stagingModelService.retrieveDescriptiveMetadata(aipId,
        descriptiveMetadataId);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return descriptiveMetadata;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DescriptiveMetadata retrieveDescriptiveMetadata(String aipId, String representationId,
    String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    List<TransactionalModelOperationLog> operationLogs = registerOperationForDescriptiveMetadata(aipId,
      representationId, descriptiveMetadataId, OperationType.READ);

    try {
      DescriptiveMetadata descriptiveMetadata = stagingModelService.retrieveDescriptiveMetadata(aipId, representationId,
        descriptiveMetadataId);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return descriptiveMetadata;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  private <T extends IsRODAObject> void checkIfEntityExistsAndThrowException(Class<T> objectClass, String... ids)
    throws AlreadyExistsException, RequestNotValidException, GenericException {
    String[] filteredIds = Arrays.stream(ids).filter(Objects::nonNull).toArray(String[]::new);
    Optional<LiteRODAObject> liteRODAObject = LiteRODAObjectFactory.get(objectClass, filteredIds);
    if (liteRODAObject.isPresent()) {
      if (existsInStorage(liteRODAObject.get())) {
        throw new AlreadyExistsException(" Entity '" + liteRODAObject.get() + "' already exists in the storage");
      }
    }
  }

  @Override
  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String descriptiveMetadataId,
    ContentPayload payload, String descriptiveMetadataType, String descriptiveMetadataVersion, String createdBy,
    boolean notify) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {

    checkIfEntityExistsAndThrowException(DescriptiveMetadata.class, aipId, descriptiveMetadataId);

    List<TransactionalModelOperationLog> operationLogs = registerOperationForDescriptiveMetadata(aipId, null,
      descriptiveMetadataId, OperationType.CREATE);
    try {
      DescriptiveMetadata ret = getModelService().createDescriptiveMetadata(aipId, descriptiveMetadataId, payload,
        descriptiveMetadataType, descriptiveMetadataVersion, createdBy, notify);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;

    } catch (RequestNotValidException | GenericException | AlreadyExistsException | AuthorizationDeniedException
      | NotFoundException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String descriptiveMetadataId,
    ContentPayload payload, String descriptiveMetadataType, String descriptiveMetadataVersion, String createdBy)
    throws RequestNotValidException, GenericException, AlreadyExistsException, AuthorizationDeniedException,
    NotFoundException {

    checkIfEntityExistsAndThrowException(DescriptiveMetadata.class, aipId, descriptiveMetadataId);

    List<TransactionalModelOperationLog> operationLogs = registerOperationForDescriptiveMetadata(aipId, null,
      descriptiveMetadataId, OperationType.CREATE);
    try {
      DescriptiveMetadata ret = getModelService().createDescriptiveMetadata(aipId, descriptiveMetadataId, payload,
        descriptiveMetadataType, descriptiveMetadataVersion, createdBy);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AlreadyExistsException | AuthorizationDeniedException
      | NotFoundException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String representationId,
    String descriptiveMetadataId, ContentPayload payload, String descriptiveMetadataType,
    String descriptiveMetadataVersion, String createdBy) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {

    checkIfEntityExistsAndThrowException(DescriptiveMetadata.class, aipId, representationId, descriptiveMetadataId);

    List<TransactionalModelOperationLog> operationLogs = registerOperationForDescriptiveMetadata(aipId,
      representationId, descriptiveMetadataId, OperationType.CREATE);
    try {
      DescriptiveMetadata ret = getModelService().createDescriptiveMetadata(aipId, representationId,
        descriptiveMetadataId, payload, descriptiveMetadataType, descriptiveMetadataVersion, createdBy);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AlreadyExistsException | AuthorizationDeniedException
      | NotFoundException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String representationId,
    String descriptiveMetadataId, ContentPayload payload, String descriptiveMetadataType,
    String descriptiveMetadataVersion, String createdBy, boolean notify) throws RequestNotValidException,
    GenericException, AlreadyExistsException, AuthorizationDeniedException, NotFoundException {

    checkIfEntityExistsAndThrowException(DescriptiveMetadata.class, aipId, representationId, descriptiveMetadataId);

    List<TransactionalModelOperationLog> operationLogs = registerOperationForDescriptiveMetadata(aipId,
      representationId, descriptiveMetadataId, OperationType.CREATE);
    try {
      DescriptiveMetadata ret = getModelService().createDescriptiveMetadata(aipId, representationId,
        descriptiveMetadataId, payload, descriptiveMetadataType, descriptiveMetadataVersion, createdBy, notify);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AlreadyExistsException | AuthorizationDeniedException
      | NotFoundException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DescriptiveMetadata updateDescriptiveMetadata(String aipId, String descriptiveMetadataId,
    ContentPayload descriptiveMetadataPayload, String descriptiveMetadataType, String descriptiveMetadataVersion,
    Map<String, String> properties, String updatedBy)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    List<TransactionalModelOperationLog> operationLogs = registerOperationForDescriptiveMetadata(aipId, null,
      descriptiveMetadataId, OperationType.UPDATE);
    try {
      DescriptiveMetadata ret = getModelService().updateDescriptiveMetadata(aipId, descriptiveMetadataId,
        descriptiveMetadataPayload, descriptiveMetadataType, descriptiveMetadataVersion, properties, updatedBy);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DescriptiveMetadata updateDescriptiveMetadata(String aipId, String representationId,
    String descriptiveMetadataId, ContentPayload descriptiveMetadataPayload, String descriptiveMetadataType,
    String descriptiveMetadataVersion, Map<String, String> properties, String updatedBy)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForDescriptiveMetadata(aipId,
      representationId, descriptiveMetadataId, OperationType.UPDATE);

    try {
      DescriptiveMetadata ret = getModelService().updateDescriptiveMetadata(aipId, representationId,
        descriptiveMetadataId, descriptiveMetadataPayload, descriptiveMetadataType, descriptiveMetadataVersion,
        properties, updatedBy);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteDescriptiveMetadata(String aipId, String descriptiveMetadataId, String deletedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForDescriptiveMetadata(aipId, null,
      descriptiveMetadataId, OperationType.DELETE);
    try {
      getModelService().deleteDescriptiveMetadata(aipId, descriptiveMetadataId, deletedBy);
      updateOperationState(operationLogs, OperationState.SUCCESS);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteDescriptiveMetadata(String aipId, String representationId, String descriptiveMetadataId,
    String deletedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForDescriptiveMetadata(aipId,
      representationId, descriptiveMetadataId, OperationType.DELETE);
    try {
      getModelService().deleteDescriptiveMetadata(aipId, representationId, descriptiveMetadataId, deletedBy);
      updateOperationState(operationLogs, OperationState.SUCCESS);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public CloseableIterable<BinaryVersion> listDescriptiveMetadataVersions(String aipId, String representationId,
    String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForDescriptiveMetadata(aipId,
      representationId, descriptiveMetadataId, OperationType.READ);
    try {
      CloseableIterable<BinaryVersion> ret = getModelService().listDescriptiveMetadataVersions(aipId, representationId,
        descriptiveMetadataId);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public BinaryVersion revertDescriptiveMetadataVersion(String aipId, String descriptiveMetadataId, String versionId,
    Map<String, String> properties)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForDescriptiveMetadata(aipId, null,
      descriptiveMetadataId, OperationType.UPDATE);
    try {
      BinaryVersion ret = getModelService().revertDescriptiveMetadataVersion(aipId, descriptiveMetadataId, versionId,
        properties);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public BinaryVersion revertDescriptiveMetadataVersion(String aipId, String representationId,
    String descriptiveMetadataId, String versionId, Map<String, String> properties)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForDescriptiveMetadata(aipId,
      representationId, descriptiveMetadataId, OperationType.UPDATE);
    try {
      BinaryVersion ret = getModelService().revertDescriptiveMetadataVersion(aipId, representationId,
        descriptiveMetadataId, versionId, properties);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
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
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aipId, OperationType.READ);
    try {
      CloseableIterable<OptionalWithCause<DescriptiveMetadata>> ret = getModelService().listDescriptiveMetadata(aipId,
        includeRepresentations);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public CloseableIterable<OptionalWithCause<DescriptiveMetadata>> listDescriptiveMetadata(String aipId,
    String representationId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLog = registerOperationForRepresentation(aipId, representationId,
      OperationType.READ);
    try {
      CloseableIterable<OptionalWithCause<DescriptiveMetadata>> ret = getModelService().listDescriptiveMetadata(aipId,
        representationId);
      for (TransactionalModelOperationLog log : operationLog) {
        updateOperationState(log, OperationState.SUCCESS);
      }
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      for (TransactionalModelOperationLog log : operationLog) {
        updateOperationState(log, OperationState.FAILURE);
      }
      throw e;
    }
  }

  @Override
  public Representation retrieveRepresentation(String aipId, String representationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForRepresentation(aipId, representationId,
      OperationType.READ);

    try {
      Representation representation = stagingModelService.retrieveRepresentation(aipId, representationId);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return representation;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Representation createRepresentation(String aipId, String representationId, boolean original, String type,
    boolean notify, String createdBy, List<String> representationState) throws RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException, AlreadyExistsException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForRepresentation(aipId, representationId,
      OperationType.CREATE);
    try {
      Representation ret = getModelService().createRepresentation(aipId, representationId, original, type, notify,
        createdBy, representationState);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException
      | AlreadyExistsException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Representation createRepresentation(String aipId, String representationId, boolean original, String type,
    boolean notify, String createdBy) throws RequestNotValidException, GenericException, NotFoundException,
    AuthorizationDeniedException, AlreadyExistsException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForRepresentation(aipId, representationId,
      OperationType.CREATE);
    try {
      Representation ret = getModelService().createRepresentation(aipId, representationId, original, type, notify,
        createdBy);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException
      | AlreadyExistsException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Representation createRepresentation(String aipId, String representationId, boolean original, String type,
    StorageService sourceStorage, StoragePath sourcePath, boolean justData, String createdBy)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException,
    AlreadyExistsException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForRepresentation(aipId, representationId,
      OperationType.CREATE);
    try {
      Representation ret = getModelService().createRepresentation(aipId, representationId, original, type,
        sourceStorage, sourcePath, justData, createdBy);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException
      | AlreadyExistsException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Representation updateRepresentationInfo(Representation representation) throws GenericException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForRepresentation(representation.getAipId(),
      representation.getId(), OperationType.UPDATE);
    try {
      Representation ret = getModelService().updateRepresentationInfo(representation);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (GenericException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void changeRepresentationType(String aipId, String representationId, String type, String updatedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForRepresentation(aipId, representationId,
      OperationType.UPDATE);
    try {
      getModelService().changeRepresentationType(aipId, representationId, type, updatedBy);
      updateOperationState(operationLogs, OperationState.SUCCESS);

    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void changeRepresentationShallowFileFlag(String aipId, String representationId, boolean hasShallowFiles,
    String updatedBy, boolean notify)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForRepresentation(aipId, representationId,
      OperationType.UPDATE);
    try {
      getModelService().changeRepresentationShallowFileFlag(aipId, representationId, hasShallowFiles, updatedBy,
        notify);
      updateOperationState(operationLogs, OperationState.SUCCESS);
    } catch (AuthorizationDeniedException | GenericException | NotFoundException | RequestNotValidException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void changeRepresentationStates(String aipId, String representationId, List<String> newStates,
    String updatedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForRepresentation(aipId, representationId,
      OperationType.UPDATE);
    try {
      getModelService().changeRepresentationStates(aipId, representationId, newStates, updatedBy);
      updateOperationState(operationLogs, OperationState.SUCCESS);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Representation updateRepresentation(String aipId, String representationId, boolean original, String type,
    StorageService sourceStorage, StoragePath sourcePath, String updatedBy) throws RequestNotValidException,
    NotFoundException, GenericException, AuthorizationDeniedException, ValidationException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForRepresentation(aipId, representationId,
      OperationType.UPDATE);
    try {
      Representation ret = getModelService().updateRepresentation(aipId, representationId, original, type,
        sourceStorage, sourcePath, updatedBy);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | ValidationException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteRepresentation(String aipId, String representationId, String username)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForRepresentation(aipId, representationId,
      OperationType.DELETE);
    try {
      getModelService().deleteRepresentation(aipId, representationId, username);
      updateOperationState(operationLogs, OperationState.SUCCESS);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public CloseableIterable<OptionalWithCause<File>> listFilesUnder(String aipId, String representationId,
    boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForRepresentation(aipId, representationId,
      OperationType.READ);
    try {
      CloseableIterable<OptionalWithCause<File>> ret = getModelService().listFilesUnder(aipId, representationId,
        recursive);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public CloseableIterable<OptionalWithCause<File>> listExternalFilesUnder(File file)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLog = registerOperationForFile(file.getAipId(),
      file.getRepresentationId(), file.getPath(), file.getId(), OperationType.READ);
    try {
      CloseableIterable<OptionalWithCause<File>> ret = getModelService().listExternalFilesUnder(file);
      for (TransactionalModelOperationLog log : operationLog) {
        updateOperationState(log, OperationState.SUCCESS);
      }
      return ret;
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      for (TransactionalModelOperationLog log : operationLog) {
        updateOperationState(log, OperationState.FAILURE);
      }
      throw e;
    }
  }

  @Override
  public CloseableIterable<OptionalWithCause<File>> listFilesUnder(File f, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForFile(f.getAipId(), f.getRepresentationId(),
      f.getPath(), f.getId(), OperationType.READ);
    try {
      CloseableIterable<OptionalWithCause<File>> ret = getModelService().listFilesUnder(f, recursive);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public CloseableIterable<OptionalWithCause<File>> listFilesUnder(String aipId, String representationId,
    List<String> directoryPath, String fileId, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForFile(aipId, representationId,
      directoryPath, fileId, OperationType.READ);
    try {
      CloseableIterable<OptionalWithCause<File>> ret = getModelService().listFilesUnder(aipId, representationId,
        directoryPath, fileId, recursive);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Long getExternalFilesTotalSize(File file)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException, IOException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForFile(file.getAipId(),
      file.getRepresentationId(), file.getPath(), file.getId(), OperationType.READ);
    try {
      Long ret = getModelService().getExternalFilesTotalSize(file);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | AuthorizationDeniedException | NotFoundException | GenericException
      | IOException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public File retrieveFile(String aipId, String representationId, List<String> directoryPath, String fileId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForFile(aipId, representationId,
      directoryPath, fileId, OperationType.READ);
    try {
      File file = stagingModelService.retrieveFile(aipId, representationId, directoryPath, fileId);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return file;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public File createFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload, String createdBy) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForFile(aipId, representationId,
      directoryPath, fileId, OperationType.CREATE);
    try {
      File ret = getModelService().createFile(aipId, representationId, directoryPath, fileId, contentPayload,
        createdBy);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AlreadyExistsException | AuthorizationDeniedException
      | NotFoundException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public File createFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload, String createdBy, boolean notify) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForFile(aipId, representationId,
      directoryPath, fileId, OperationType.CREATE);
    try {
      File ret = getModelService().createFile(aipId, representationId, directoryPath, fileId, contentPayload, createdBy,
        notify);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AlreadyExistsException | AuthorizationDeniedException
      | NotFoundException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public File createFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    String dirName, String createdBy, boolean notify) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForFile(aipId, representationId,
      directoryPath, fileId, dirName, OperationType.CREATE);
    try {
      File ret = getModelService().createFile(aipId, representationId, directoryPath, fileId, dirName, createdBy,
        notify);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AlreadyExistsException | AuthorizationDeniedException
      | NotFoundException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public File updateFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload, boolean createIfNotExists, String updatedBy, boolean notify)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForFile(aipId, representationId,
      directoryPath, fileId, OperationType.UPDATE);
    try {
      File ret = getModelService().updateFile(aipId, representationId, directoryPath, fileId, contentPayload,
        createIfNotExists, updatedBy, notify);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public File updateFile(File file, ContentPayload contentPayload, boolean createIfNotExists, String updatedBy,
    boolean notify) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForFile(file.getAipId(),
      file.getRepresentationId(), file.getPath(), file.getId(), OperationType.UPDATE);
    try {
      return getModelService().updateFile(file, contentPayload, createIfNotExists, updatedBy, notify);
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    String deletedBy, boolean notify)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForFile(aipId, representationId,
      directoryPath, fileId, OperationType.DELETE);
    try {
      getModelService().deleteFile(aipId, representationId, directoryPath, fileId, deletedBy, notify);
      updateOperationState(operationLogs, OperationState.SUCCESS);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteFile(File file, String deletedBy, boolean notify)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForFile(file.getAipId(),
      file.getRepresentationId(), file.getPath(), file.getId(), OperationType.DELETE);
    try {
      getModelService().deleteFile(file, deletedBy, notify);
      updateOperationState(operationLogs, OperationState.SUCCESS);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public File renameFolder(File folder, String newName, boolean reindexResources) throws AlreadyExistsException,
    GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {

    List<TransactionalModelOperationLog> operationLogs = registerOperationForFile(folder.getAipId(),
      folder.getRepresentationId(), folder.getPath(), folder.getId(), OperationType.DELETE);
    List<TransactionalModelOperationLog> renameOperationLogs = registerOperationForFile(folder.getAipId(),
      folder.getRepresentationId(), folder.getPath(), newName, OperationType.CREATE);
    try {
      File ret = getModelService().renameFolder(folder, newName, reindexResources);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      for (TransactionalModelOperationLog renameOperationLog : renameOperationLogs) {
        updateOperationState(renameOperationLog, OperationState.SUCCESS);
      }
      return ret;
    } catch (AlreadyExistsException | GenericException | NotFoundException | RequestNotValidException
      | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      for (TransactionalModelOperationLog renameOperationLog : renameOperationLogs) {
        updateOperationState(renameOperationLog, OperationState.FAILURE);
      }
      throw e;
    }
  }

  @Override
  public File moveFile(File file, String newAipId, String newRepresentationId, List<String> newDirectoryPath,
    String newId, boolean reindexResources) throws AlreadyExistsException, GenericException, NotFoundException,
    RequestNotValidException, AuthorizationDeniedException {

    List<TransactionalModelOperationLog> operationLogs = registerOperationForFile(file.getAipId(),
      file.getRepresentationId(), file.getPath(), file.getId(), OperationType.DELETE);
    List<TransactionalModelOperationLog> moveOperationLogs = registerOperationForFile(newAipId, newRepresentationId,
      newDirectoryPath, newId, OperationType.CREATE);
    operationLogs
      .addAll(registerOperationForFile(newAipId, newRepresentationId, newDirectoryPath, newId, OperationType.UPDATE));
    try {
      File ret = getModelService().moveFile(file, newAipId, newRepresentationId, newDirectoryPath, newId,
        reindexResources);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      for (TransactionalModelOperationLog moveOperationLog : moveOperationLogs) {
        updateOperationState(moveOperationLog, OperationState.SUCCESS);
      }
      return ret;
    } catch (AlreadyExistsException | GenericException | NotFoundException | RequestNotValidException
      | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      for (TransactionalModelOperationLog moveOperationLog : moveOperationLogs) {
        updateOperationState(moveOperationLog, OperationState.FAILURE);
      }
      throw e;
    }
  }

  @Override
  public PreservationMetadata createRepositoryEvent(RodaConstants.PreservationEventType eventType,
    String eventDescription, PluginState outcomeState, String outcomeText, String outcomeDetail, String agentName,
    boolean notify) {
    PreservationMetadata event = getModelService().createRepositoryEvent(eventType, eventDescription, outcomeState,
      outcomeText, outcomeDetail, agentName, notify);
    List<TransactionalModelOperationLog> operationLogs = registerOperationForEvent(event, OperationType.CREATE);
    updateOperationState(operationLogs, OperationState.SUCCESS);
    return event;
  }

  @Override
  public PreservationMetadata createRepositoryEvent(RodaConstants.PreservationEventType eventType,
    String eventDescription, List<LinkingIdentifier> sources, List<LinkingIdentifier> targets, PluginState outcomeState,
    String outcomeText, String outcomeDetail, String agentName, boolean notify) {
    PreservationMetadata event = getModelService().createRepositoryEvent(eventType, eventDescription, sources, targets,
      outcomeState, outcomeText, outcomeDetail, agentName, notify);
    List<TransactionalModelOperationLog> operationLogs = registerOperationForEvent(event, OperationType.CREATE);
    updateOperationState(operationLogs, OperationState.SUCCESS);
    return event;
  }

  @Override
  public PreservationMetadata createUpdateAIPEvent(String aipId, String representationId, List<String> filePath,
    String fileId, RodaConstants.PreservationEventType eventType, String eventDescription, PluginState outcomeState,
    String outcomeText, String outcomeDetail, String agentName, boolean notify) {
    PreservationMetadata event = getModelService().createUpdateAIPEvent(aipId, representationId, filePath, fileId,
      eventType, eventDescription, outcomeState, outcomeText, outcomeDetail, agentName, notify);
    List<TransactionalModelOperationLog> operationLogs = registerOperationForEvent(event, OperationType.UPDATE);
    updateOperationState(operationLogs, OperationState.SUCCESS);
    return event;
  }

  @Override
  public PreservationMetadata createEvent(String aipId, String representationId, List<String> filePath, String fileId,
    RodaConstants.PreservationEventType eventType, String eventDescription, List<LinkingIdentifier> sources,
    List<LinkingIdentifier> targets, PluginState outcomeState, String outcomeText, String outcomeDetail,
    String agentName, boolean notify) {
    PreservationMetadata event = getModelService().createEvent(aipId, representationId, filePath, fileId, eventType,
      eventDescription, sources, targets, outcomeState, outcomeText, outcomeDetail, agentName, notify);
    List<TransactionalModelOperationLog> operationLogs = registerOperationForEvent(event, OperationType.CREATE);
    updateOperationState(operationLogs, OperationState.SUCCESS);
    return event;
  }

  @Override
  public PreservationMetadata createEvent(String aipId, String representationId, List<String> filePath, String fileId,
    RodaConstants.PreservationEventType eventType, String eventDescription, List<LinkingIdentifier> sources,
    List<LinkingIdentifier> targets, PluginState outcomeState, String outcomeText, String outcomeDetail,
    String agentName, String agentRole, boolean notify) {
    PreservationMetadata event = getModelService().createEvent(aipId, representationId, filePath, fileId, eventType,
      eventDescription, sources, targets, outcomeState, outcomeText, outcomeDetail, agentName, agentRole, notify);
    List<TransactionalModelOperationLog> operationLogs = registerOperationForEvent(event, OperationType.CREATE);
    updateOperationState(operationLogs, OperationState.SUCCESS);
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
    List<TransactionalModelOperationLog> operationLogs = registerOperationForEvent(event, OperationType.CREATE);
    updateOperationState(operationLogs, OperationState.SUCCESS);
    return event;
  }

  @Override
  public PreservationMetadata retrievePreservationMetadata(String id,
    PreservationMetadata.PreservationMetadataType type) {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForPreservationMetadata(id,
      OperationType.READ);
    updateOperationState(operationLogs, OperationState.SUCCESS);
    return getModelService().retrievePreservationMetadata(id, type);
  }

  @Override
  public PreservationMetadata retrievePreservationMetadata(String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId, PreservationMetadata.PreservationMetadataType type) {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForPreservationMetadata(aipId,
      representationId, fileDirectoryPath, fileId, type, OperationType.READ);
    updateOperationState(operationLogs, OperationState.SUCCESS);
    return getModelService().retrievePreservationMetadata(aipId, representationId, fileDirectoryPath, fileId, type);
  }

  @Override
  public Binary retrievePreservationRepresentation(String aipId, String representationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForPreservationMetadata(aipId,
      representationId, OperationType.READ);
    try {
      Binary binary = stagingModelService.retrievePreservationRepresentation(aipId, representationId);
      return binary;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public boolean preservationRepresentationExists(String aipId, String representationId)
    throws RequestNotValidException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForPreservationMetadata(aipId,
      representationId, OperationType.READ);
    try {
      boolean ret;
      if (!stagingModelService.preservationRepresentationExists(aipId, representationId)) {
        ret = mainModelService.preservationRepresentationExists(aipId, representationId);
      } else {
        ret = true;
      }
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Binary retrievePreservationFile(File file)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForPreservationMetadata(file,
      OperationType.READ);
    try {
      Binary binary = stagingModelService.retrievePreservationFile(file);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return binary;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Binary retrievePreservationFile(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    String preservationID = IdUtils.getPreservationId(PreservationMetadata.PreservationMetadataType.FILE, aipId,
      representationId, fileDirectoryPath, fileId, RODAInstanceUtils.getLocalInstanceIdentifier());
    List<TransactionalModelOperationLog> operationLogs = registerOperationForPreservationMetadata(aipId,
      representationId, fileDirectoryPath, fileId, preservationID, OperationType.READ);
    try {
      Binary binary = stagingModelService.retrievePreservationFile(aipId, representationId, fileDirectoryPath, fileId);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return binary;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public boolean preservationFileExists(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId) throws RequestNotValidException, GenericException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForPreservationMetadata(aipId,
      representationId, fileDirectoryPath, fileId, PreservationMetadata.PreservationMetadataType.FILE,
      OperationType.READ);
    try {
      boolean ret;
      if (!stagingModelService.preservationFileExists(aipId, representationId, fileDirectoryPath, fileId)) {
        ret = mainModelService.preservationFileExists(aipId, representationId, fileDirectoryPath, fileId);
      } else {
        ret = true;
      }
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Binary retrieveRepositoryPreservationEvent(String fileId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForPreservationMetadata(fileId,
      OperationType.READ);
    try {
      Binary binary = stagingModelService.retrieveRepositoryPreservationEvent(fileId);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return binary;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }

  }

  @Override
  public Binary retrievePreservationEvent(String aipId, String representationId, List<String> filePath, String fileId,
    String preservationID)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForPreservationMetadata(aipId,
      representationId, filePath, fileId, preservationID, OperationType.READ);
    try {
      Binary binary = stagingModelService.retrievePreservationEvent(aipId, representationId, filePath, fileId,
        preservationID);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return binary;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Binary retrievePreservationAgent(String preservationID)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForPreservationMetadata(preservationID,
      OperationType.READ);
    try {
      Binary binary = stagingModelService.retrievePreservationAgent(preservationID);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return binary;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public PreservationMetadata createPreservationMetadata(PreservationMetadata.PreservationMetadataType type,
    String aipId, String representationId, List<String> fileDirectoryPath, String fileId, ContentPayload payload,
    String username, boolean notify) throws GenericException, NotFoundException, RequestNotValidException,
    AuthorizationDeniedException, AlreadyExistsException {

    checkIfEntityExistsAndThrowException(PreservationMetadata.class, aipId, IdUtils.getPreservationId(type, aipId,
      representationId, fileDirectoryPath, fileId, RODAInstanceUtils.getLocalInstanceIdentifier()));

    PreservationMetadata preservationMetadata = getModelService().createPreservationMetadata(type, aipId,
      representationId, fileDirectoryPath, fileId, payload, username, notify);
    List<TransactionalModelOperationLog> operationLogs = registerOperationForPreservationMetadata(preservationMetadata,
      OperationType.CREATE);
    updateOperationState(operationLogs, OperationState.SUCCESS);
    return preservationMetadata;
  }

  @Override
  public void createTechnicalMetadata(String aipId, String representationId, String metadataType, String fileId,
    ContentPayload payload, String createdBy, boolean notify) throws AuthorizationDeniedException,
    RequestNotValidException, AlreadyExistsException, NotFoundException, GenericException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForRepresentation(aipId, representationId,
      OperationType.CREATE);
    try {
      getModelService().createTechnicalMetadata(aipId, representationId, metadataType, fileId, payload, createdBy,
        notify);
      updateOperationState(operationLogs, OperationState.SUCCESS);
    } catch (AuthorizationDeniedException | RequestNotValidException | AlreadyExistsException | NotFoundException
      | GenericException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public PreservationMetadata createPreservationMetadata(PreservationMetadata.PreservationMetadataType type,
    String aipId, List<String> fileDirectoryPath, String fileId, ContentPayload payload, String username,
    boolean notify) throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException,
    AlreadyExistsException {

    checkIfEntityExistsAndThrowException(PreservationMetadata.class, aipId, IdUtils.getPreservationId(type, aipId, null,
      fileDirectoryPath, fileId, RODAInstanceUtils.getLocalInstanceIdentifier()));

    PreservationMetadata preservationMetadata = getModelService().createPreservationMetadata(type, aipId,
      fileDirectoryPath, fileId, payload, username, notify);
    List<TransactionalModelOperationLog> operationLogs = registerOperationForPreservationMetadata(preservationMetadata,
      OperationType.CREATE);
    updateOperationState(operationLogs, OperationState.SUCCESS);
    return preservationMetadata;
  }

  @Override
  public PreservationMetadata createPreservationMetadata(PreservationMetadata.PreservationMetadataType type,
    String aipId, String representationId, ContentPayload payload, String username, boolean notify)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException,
    AlreadyExistsException {

    checkIfEntityExistsAndThrowException(PreservationMetadata.class, aipId, representationId, IdUtils
      .getPreservationId(type, aipId, representationId, null, null, RODAInstanceUtils.getLocalInstanceIdentifier()));

    PreservationMetadata preservationMetadata = getModelService().createPreservationMetadata(type, aipId,
      representationId, payload, username, notify);
    List<TransactionalModelOperationLog> operationLogs = registerOperationForPreservationMetadata(preservationMetadata,
      OperationType.CREATE);
    updateOperationState(operationLogs, OperationState.SUCCESS);
    return preservationMetadata;
  }

  @Override
  public PreservationMetadata createPreservationMetadata(PreservationMetadata.PreservationMetadataType type, String id,
    ContentPayload payload, boolean notify) throws GenericException, NotFoundException, RequestNotValidException,
    AuthorizationDeniedException, AlreadyExistsException {

    checkIfEntityExistsAndThrowException(PreservationMetadata.class, id);

    List<TransactionalModelOperationLog> operationLogs = registerOperationForPreservationMetadata(id,
      OperationType.CREATE);
    try {
      PreservationMetadata ret = getModelService().createPreservationMetadata(type, id, payload, notify);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException
      | AlreadyExistsException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public PreservationMetadata createPreservationMetadata(PreservationMetadata.PreservationMetadataType type, String id,
    String aipId, String representationId, List<String> fileDirectoryPath, String fileId, ContentPayload payload,
    String createdBy, boolean notify) throws GenericException, NotFoundException, RequestNotValidException,
    AuthorizationDeniedException, AlreadyExistsException {

    checkIfEntityExistsAndThrowException(PreservationMetadata.class, id);

    List<TransactionalModelOperationLog> operationLogs = registerOperationForPreservationMetadata(aipId,
      representationId, fileDirectoryPath, fileId, id, OperationType.CREATE);
    try {
      PreservationMetadata ret = getModelService().createPreservationMetadata(type, id, aipId, representationId,
        fileDirectoryPath, fileId, payload, createdBy, notify);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException
      | AlreadyExistsException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public PreservationMetadata updatePreservationMetadata(PreservationMetadata.PreservationMetadataType type, String id,
    ContentPayload payload, boolean notify)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForPreservationMetadata(id,
      OperationType.UPDATE);
    try {
      PreservationMetadata ret = getModelService().updatePreservationMetadata(type, id, payload, notify);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public PreservationMetadata updatePreservationMetadata(String id, PreservationMetadata.PreservationMetadataType type,
    String aipId, String representationId, List<String> fileDirectoryPath, String fileId, ContentPayload payload,
    String updatedBy, boolean notify)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForPreservationMetadata(aipId,
      representationId, fileDirectoryPath, fileId, id, OperationType.UPDATE);
    try {
      PreservationMetadata ret = getModelService().updatePreservationMetadata(id, type, aipId, representationId,
        fileDirectoryPath, fileId, payload, updatedBy, notify);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deletePreservationMetadata(PreservationMetadata pm, boolean notify)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForPreservationMetadata(pm,
      OperationType.DELETE);
    try {
      getModelService().deletePreservationMetadata(pm, notify);
      updateOperationState(operationLogs, OperationState.SUCCESS);
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deletePreservationMetadata(PreservationMetadata.PreservationMetadataType type, String aipId,
    String representationId, String id, List<String> filePath, boolean notify)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForPreservationMetadata(aipId,
      representationId, filePath, null, id, OperationType.DELETE);
    try {
      getModelService().deletePreservationMetadata(type, aipId, representationId, id, filePath, notify);
      updateOperationState(operationLogs, OperationState.SUCCESS);

    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
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
    TransactionalModelOperationLog operationLog = registerOperationForOtherMetadata(om.getAipId(), OperationType.READ);
    try {
      Binary binary = stagingModelService.retrieveOtherMetadataBinary(om);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return binary;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Binary retrieveOtherMetadataBinary(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId, String fileSuffix, String type)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForOtherMetadata(aipId, OperationType.READ);
    try {
      Binary binary = stagingModelService.retrieveOtherMetadataBinary(aipId, representationId, fileDirectoryPath,
        fileId, fileSuffix, type);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return binary;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public OtherMetadata retrieveOtherMetadata(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId, String fileSuffix, String type)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForOtherMetadata(aipId, OperationType.READ);
    try {
      OtherMetadata otherMetadata = stagingModelService.retrieveOtherMetadata(aipId, representationId,
        fileDirectoryPath, fileId, fileSuffix, type);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return otherMetadata;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public OtherMetadata createOrUpdateOtherMetadata(String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId, String fileSuffix, String type, ContentPayload payload,
    String username, boolean notify)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForOtherMetadata(aipId, OperationType.UPDATE);
    try {
      OtherMetadata ret = getModelService().createOrUpdateOtherMetadata(aipId, representationId, fileDirectoryPath,
        fileId, fileSuffix, type, payload, username, notify);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteOtherMetadata(String aipId, String representationId, List<String> fileDirectoryPath, String fileId,
    String fileSuffix, String type, String username)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = registerOperationForOtherMetadata(aipId, OperationType.DELETE);
    try {
      getModelService().deleteOtherMetadata(aipId, representationId, fileDirectoryPath, fileId, fileSuffix, type,
        username);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | NotFoundException | AuthorizationDeniedException | RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
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
      TransactionalModelOperationLog operationLog = registerOperationForLogEntry(log.getUUID(), OperationType.CREATE);
      updateOperationState(operationLog, OperationState.SUCCESS);
    }
    return ret;
  }

  @Override
  public void addLogEntry(LogEntry logEntry, Path logDirectory, boolean notify)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    // TODO: review this, check the JsonUtils methods to add support to storage
    // service
    TransactionalModelOperationLog operationLog = registerOperationForLogEntry(logEntry.getUUID(),
      OperationType.CREATE);
    try {
      getModelService().addLogEntry(logEntry, logDirectory, notify);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void addLogEntry(LogEntry logEntry, Path logDirectory)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    TransactionalModelOperationLog operationLog = registerOperationForLogEntry(logEntry.getUUID(),
      OperationType.CREATE);
    try {
      getModelService().addLogEntry(logEntry, logDirectory);
      updateOperationState(operationLog, OperationState.SUCCESS);

    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void findOldLogsAndSendThemToMaster(Path logDirectory, Path currentLogFile) throws IOException {
    List<TransactionalModelOperationLog> operationLogs = new ArrayList<>();
    try (LogEntryFileSystemIterable logEntries = new LogEntryFileSystemIterable(logDirectory)) {
      for (OptionalWithCause<LogEntry> logEntry : logEntries) {
        if (logEntry.isPresent()) {
          operationLogs.add(registerOperationForLogEntry(logEntry.get().getId(), OperationType.CREATE));
        }
      }
      getModelService().findOldLogsAndSendThemToMaster(logDirectory, currentLogFile);
    } catch (IOException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void findOldLogsAndMoveThemToStorage(Path logDirectory, Path currentLogFile)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, IOException {
    List<TransactionalModelOperationLog> operationLogs = new ArrayList<>();
    try (LogEntryFileSystemIterable logEntries = new LogEntryFileSystemIterable(logDirectory)) {
      for (OptionalWithCause<LogEntry> logEntry : logEntries) {
        if (logEntry.isPresent()) {
          operationLogs.add(registerOperationForLogEntry(logEntry.get().getId(), OperationType.CREATE));
        }
      }
      getModelService().findOldLogsAndMoveThemToStorage(logDirectory, currentLogFile);
    } catch (IOException | RequestNotValidException | AuthorizationDeniedException | NotFoundException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
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
    TransactionalModelOperationLog operationLog = registerOperationForJob(job.getId(), OperationType.CREATE);
    try {
      mainModelService.createJob(job);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void createOrUpdateJob(Job job)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForJob(job.getId(), OperationType.UPDATE);
    try {
      mainModelService.createOrUpdateJob(job);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Job retrieveJob(String jobId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForJob(jobId, OperationType.READ);
    try {
      Job ret = mainModelService.retrieveJob(jobId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public CloseableIterable<OptionalWithCause<Report>> listJobReports(String jobId)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperation(Job.class, List.of(jobId), OperationType.READ);
    try {
      CloseableIterable<OptionalWithCause<Report>> ret = getModelService().listJobReports(jobId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | AuthorizationDeniedException | NotFoundException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteJob(String jobId)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = registerOperationForJob(jobId, OperationType.DELETE);
    try {
      mainModelService.deleteJob(jobId);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Report retrieveJobReport(String jobId, String jobReportId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForJobReport(jobId, jobReportId, OperationType.READ);
    try {
      Report ret = mainModelService.retrieveJobReport(jobId, jobReportId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Report retrieveJobReport(String jobId, String sourceObjectId, String outcomeObjectId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    Report ret = mainModelService.retrieveJobReport(jobId, sourceObjectId, outcomeObjectId);
    TransactionalModelOperationLog operationLog = registerOperationForJobReport(jobId, ret.getId(), OperationType.READ);
    updateOperationState(operationLog, OperationState.SUCCESS);
    return ret;
  }

  @Override
  public void createOrUpdateJobReport(Report jobReport, Job cachedJob)
    throws GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForJobReport(jobReport.getJobId(), jobReport.getId(),
      OperationType.UPDATE);
    try {
      mainModelService.createOrUpdateJobReport(jobReport, cachedJob);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void createOrUpdateJobReport(Report jobReport, IndexedJob indexJob)
    throws GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForJobReport(jobReport.getJobId(), jobReport.getId(),
      OperationType.UPDATE);
    try {
      mainModelService.createOrUpdateJobReport(jobReport, indexJob);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteJobReport(String jobId, String jobReportId)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = registerOperationForJobReport(jobId, jobReportId,
      OperationType.DELETE);
    try {
      mainModelService.deleteJobReport(jobId, jobReportId);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void updateAIPPermissions(String aipId, Permissions permissions, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aipId, OperationType.UPDATE);
    try {
      getModelService().updateAIPPermissions(aipId, permissions, updatedBy);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void updateAIPPermissions(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aip.getId(), OperationType.UPDATE);
    try {
      getModelService().updateAIPPermissions(aip, updatedBy);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void updateDIPPermissions(DIP dip)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForDIP(dip.getId(), OperationType.UPDATE);
    try {
      getModelService().updateDIPPermissions(dip);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteTransferredResource(TransferredResource transferredResource)
    throws GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForTransferredResource(
      transferredResource.getFullPath(), OperationType.DELETE);
    try {
      getModelService().deleteTransferredResource(transferredResource);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Job updateJobInstanceId(Job job)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForJob(job.getId(), OperationType.UPDATE);
    try {
      Job ret = mainModelService.updateJobInstanceId(job);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Risk createRisk(Risk risk, boolean commit) throws GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForRisk(risk.getId(), OperationType.CREATE);
    try {
      Risk ret = getModelService().createRisk(risk, commit);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Risk updateRiskInstanceId(Risk risk, boolean commit) throws GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForRisk(risk.getId(), OperationType.UPDATE);
    try {
      Risk ret = getModelService().updateRiskInstanceId(risk, commit);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Risk updateRisk(Risk risk, Map<String, String> properties, boolean commit, int incidences)
    throws GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForRisk(risk.getId(), OperationType.UPDATE);
    try {
      Risk ret = getModelService().updateRisk(risk, properties, commit, incidences);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteRisk(String riskId, boolean commit)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = registerOperationForRisk(riskId, OperationType.DELETE);
    try {
      getModelService().deleteRisk(riskId, commit);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | NotFoundException | AuthorizationDeniedException | RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Risk retrieveRisk(String riskId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForRisk(riskId, OperationType.READ);
    try {
      Risk ret = getModelService().retrieveRisk(riskId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public BinaryVersion retrieveVersion(String riskId, String versionId)
    throws RequestNotValidException, GenericException, NotFoundException {
    TransactionalModelOperationLog operationLog = registerOperationForRisk(riskId, OperationType.READ);
    try {
      BinaryVersion ret = getModelService().retrieveVersion(riskId, versionId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public BinaryVersion revertRiskVersion(String riskId, String versionId, Map<String, String> properties,
    boolean commit, int incidences)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForRisk(riskId, OperationType.UPDATE);
    try {
      BinaryVersion ret = getModelService().revertRiskVersion(riskId, versionId, properties, commit, incidences);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public RiskIncidence createRiskIncidence(RiskIncidence riskIncidence, boolean commit)
    throws AlreadyExistsException, NotFoundException, AuthorizationDeniedException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperationForRiskIncidence(riskIncidence.getId(),
      OperationType.CREATE);
    try {
      RiskIncidence ret = getModelService().createRiskIncidence(riskIncidence, commit);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AlreadyExistsException | NotFoundException | AuthorizationDeniedException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public RiskIncidence updateRiskIncidenceInstanceId(RiskIncidence riskIncidence, boolean commit)
    throws GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForRiskIncidence(riskIncidence.getId(),
      OperationType.UPDATE);
    try {
      RiskIncidence ret = getModelService().updateRiskIncidenceInstanceId(riskIncidence, commit);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public RiskIncidence updateRiskIncidence(RiskIncidence riskIncidence, boolean commit)
    throws GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForRiskIncidence(riskIncidence.getId(),
      OperationType.UPDATE);
    try {
      RiskIncidence ret = getModelService().updateRiskIncidence(riskIncidence, commit);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteRiskIncidence(String riskIncidenceId, boolean commit)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = registerOperationForRiskIncidence(riskIncidenceId,
      OperationType.DELETE);
    try {
      getModelService().deleteRiskIncidence(riskIncidenceId, commit);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | NotFoundException | AuthorizationDeniedException | RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public RiskIncidence retrieveRiskIncidence(String incidenceId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForRiskIncidence(incidenceId, OperationType.READ);
    try {
      RiskIncidence ret = getModelService().retrieveRiskIncidence(incidenceId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Notification createNotification(Notification notification, NotificationProcessor processor)
    throws GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForNotification(notification.getId(),
      OperationType.CREATE);
    try {
      Notification ret = getModelService().createNotification(notification, processor);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Notification updateNotificationInstanceId(Notification notification)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForNotification(notification.getId(),
      OperationType.UPDATE);
    try {
      Notification ret = getModelService().updateNotificationInstanceId(notification);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Notification updateNotification(Notification notification)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForNotification(notification.getId(),
      OperationType.UPDATE);
    try {
      Notification ret = getModelService().updateNotification(notification);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteNotification(String notificationId)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForNotification(notificationId,
      OperationType.DELETE);
    try {
      getModelService().deleteNotification(notificationId);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Notification retrieveNotification(String notificationId)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForNotification(notificationId, OperationType.READ);
    try {
      Notification notification = getModelService().retrieveNotification(notificationId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return notification;
    } catch (GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Notification acknowledgeNotification(String notificationId, String token)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForNotification(notificationId,
      OperationType.UPDATE);
    try {
      Notification notification = getModelService().acknowledgeNotification(notificationId, token);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return notification;
    } catch (GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public CloseableIterable<OptionalWithCause<DIPFile>> listDIPFilesUnder(DIPFile f, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForDIPFile(f.getDipId(), f.getPath(), f.getId(),
      OperationType.READ);
    try {
      CloseableIterable<OptionalWithCause<DIPFile>> ret = getModelService().listDIPFilesUnder(f, recursive);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public CloseableIterable<OptionalWithCause<DIPFile>> listDIPFilesUnder(String dipId, List<String> directoryPath,
    String fileId, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForDIPFile(dipId, directoryPath, fileId,
      OperationType.READ);
    try {
      CloseableIterable<OptionalWithCause<DIPFile>> ret = getModelService().listDIPFilesUnder(dipId, directoryPath,
        fileId, recursive);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public CloseableIterable<OptionalWithCause<DIPFile>> listDIPFilesUnder(String dipId, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForDIP(dipId, OperationType.READ);
    try {
      CloseableIterable<OptionalWithCause<DIPFile>> ret = getModelService().listDIPFilesUnder(dipId, recursive);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void updateDIPInstanceId(DIP dip)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForDIP(dip.getId(), OperationType.UPDATE);
    try {
      getModelService().updateDIPInstanceId(dip);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DIP createDIP(DIP dip, boolean notify) throws GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForDIP(dip.getId(), OperationType.CREATE);
    try {
      DIP ret = getModelService().createDIP(dip, notify);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DIP updateDIP(DIP dip) throws GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForDIP(dip.getId(), OperationType.UPDATE);
    try {
      DIP ret = getModelService().updateDIP(dip);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteDIP(String dipId) throws GenericException, NotFoundException, AuthorizationDeniedException {
    DIP dip = getModelService().retrieveDIP(dipId);
    TransactionalModelOperationLog operationLog = registerOperationForDIP(dip.getId(), OperationType.DELETE);
    updateOperationState(operationLog, OperationState.SUCCESS);
    getModelService().deleteDIP(dipId);
  }

  @Override
  public DIP retrieveDIP(String dipId) throws GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForDIP(dipId, OperationType.READ);
    try {
      DIP dip = stagingModelService.retrieveDIP(dipId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return dip;
    } catch (GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DIPFile createDIPFile(String dipId, List<String> directoryPath, String fileId, long size,
    ContentPayload contentPayload, boolean notify) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    TransactionalModelOperationLog operationLog = registerOperationForDIPFile(dipId, directoryPath, fileId,
      OperationType.CREATE);
    try {
      DIPFile ret = getModelService().createDIPFile(dipId, directoryPath, fileId, size, contentPayload, notify);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AlreadyExistsException | AuthorizationDeniedException
      | NotFoundException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DIPFile createDIPFile(String dipId, List<String> directoryPath, String fileId, String dirName, boolean notify)
    throws RequestNotValidException, GenericException, AlreadyExistsException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForDIPFile(dipId, directoryPath, fileId,
      OperationType.CREATE);
    try {
      DIPFile ret = getModelService().createDIPFile(dipId, directoryPath, fileId, dirName, notify);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AlreadyExistsException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DIPFile updateDIPFile(String dipId, List<String> directoryPath, String oldFileId, String fileId, long size,
    ContentPayload contentPayload, boolean createIfNotExists, boolean notify) throws RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException, AlreadyExistsException {
    TransactionalModelOperationLog operationLog = registerOperationForDIPFile(dipId, directoryPath, oldFileId,
      OperationType.UPDATE);
    try {
      DIPFile ret = getModelService().updateDIPFile(dipId, directoryPath, oldFileId, fileId, size, contentPayload,
        createIfNotExists, notify);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException
      | AlreadyExistsException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteDIPFile(String dipId, List<String> directoryPath, String fileId, boolean notify)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForDIPFile(dipId, directoryPath, fileId,
      OperationType.DELETE);
    try {
      getModelService().deleteDIPFile(dipId, directoryPath, fileId, notify);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DIPFile retrieveDIPFile(String dipId, List<String> directoryPath, String fileId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForDIPFile(dipId, directoryPath, fileId,
      OperationType.READ);
    try {
      DIPFile ret = getModelService().retrieveDIPFile(dipId, directoryPath, fileId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Directory getSubmissionDirectory(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aipId, OperationType.READ);
    try {
      Directory ret = getModelService().getSubmissionDirectory(aipId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void createSubmission(StorageService submissionStorage, StoragePath submissionStoragePath, String aipId)
    throws AlreadyExistsException, GenericException, RequestNotValidException, NotFoundException,
    AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aipId, OperationType.UPDATE);
    try {
      getModelService().createSubmission(submissionStorage, submissionStoragePath, aipId);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (AlreadyExistsException | GenericException | RequestNotValidException | NotFoundException
      | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void createSubmission(Path submissionPath, String aipId) throws AlreadyExistsException, GenericException,
    RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aipId, OperationType.UPDATE);
    try {
      getModelService().createSubmission(submissionPath, aipId);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (AlreadyExistsException | GenericException | RequestNotValidException | NotFoundException
      | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Directory getDocumentationDirectory(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aipId, OperationType.READ);
    try {
      Directory ret = getModelService().getDocumentationDirectory(aipId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Directory getDocumentationDirectory(String aipId, String representationId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = new ArrayList<>();
    if (representationId != null) {
      operationLogs = registerOperationForRepresentation(aipId, representationId, OperationType.READ);
    } else {
      operationLogs.add(registerOperationForAIP(aipId, OperationType.READ));
    }
    try {
      Directory ret = getModelService().getDocumentationDirectory(aipId, representationId);
      for (TransactionalModelOperationLog operation : operationLogs) {
        updateOperationState(operation, OperationState.SUCCESS);
      }
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      for (TransactionalModelOperationLog operation : operationLogs) {
        updateOperationState(operation, OperationState.FAILURE);
      }
      throw e;
    }
  }

  @Override
  public File createDocumentation(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {
    List<TransactionalModelOperationLog> operationLogs = new ArrayList<>();
    if (representationId != null) {
      operationLogs = registerOperationForRepresentation(aipId, representationId, OperationType.READ);
    } else {
      operationLogs.add(registerOperationForAIP(aipId, OperationType.READ));
    }
    try {
      File ret = getModelService().createDocumentation(aipId, representationId, directoryPath, fileId, contentPayload);
      for (TransactionalModelOperationLog operation : operationLogs) {
        updateOperationState(operation, OperationState.SUCCESS);
      }
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | AlreadyExistsException e) {
      for (TransactionalModelOperationLog operation : operationLogs) {
        updateOperationState(operation, OperationState.FAILURE);
      }
      throw e;
    }
  }

  @Override
  public Long countDocumentationFiles(String aipId, String representationId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    List<TransactionalModelOperationLog> operationLogs = new ArrayList<>();
    if (representationId == null) {
      operationLogs.add(registerOperationForAIP(aipId, OperationType.READ));
    } else {
      operationLogs = registerOperationForRepresentation(aipId, representationId, OperationType.READ);
    }
    try {
      Long ret = getModelService().countDocumentationFiles(aipId, representationId);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Long countSubmissionFiles(String aipId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aipId, OperationType.READ);
    try {
      Long ret = getModelService().countSubmissionFiles(aipId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Long countSchemaFiles(String aipId, String representationId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    List<TransactionalModelOperationLog> operationLogs = new ArrayList<>();
    if (representationId == null) {
      operationLogs.add(registerOperationForAIP(aipId, OperationType.READ));
    } else {
      operationLogs = registerOperationForRepresentation(aipId, representationId, OperationType.READ);
    }
    try {
      Long ret = getModelService().countSchemaFiles(aipId, representationId);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Directory getSchemasDirectory(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aipId, OperationType.READ);
    try {
      Directory ret = getModelService().getSchemasDirectory(aipId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Directory getSchemasDirectory(String aipId, String representationId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    List<TransactionalModelOperationLog> operationLogs = new ArrayList<>();
    if (representationId == null) {
      operationLogs.add(registerOperationForAIP(aipId, OperationType.READ));
    } else {
      operationLogs = registerOperationForRepresentation(aipId, representationId, OperationType.READ);
    }
    try {
      Directory ret = getModelService().getSchemasDirectory(aipId, representationId);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
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
  public File createSchema(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {

    checkIfSchemaExistsAndThrowException(aipId, representationId, directoryPath, fileId);

    List<TransactionalModelOperationLog> operationLog = registerOperationForFile(aipId, representationId, directoryPath,
      fileId, OperationType.CREATE);
    try {
      File ret = getModelService().createSchema(aipId, representationId, directoryPath, fileId, contentPayload);
      for (TransactionalModelOperationLog log : operationLog) {
        updateOperationState(log, OperationState.SUCCESS);
      }
      return ret;
    } catch (RequestNotValidException | GenericException | AlreadyExistsException | AuthorizationDeniedException
      | NotFoundException e) {
      for (TransactionalModelOperationLog log : operationLog) {
        updateOperationState(log, OperationState.FAILURE);
      }
      throw e;
    }
  }

  @Override
  public <T extends IsRODAObject> Optional<LiteRODAObject> retrieveLiteFromObject(T object) {
    TransactionalModelOperationLog operationLog = registerOperation(object, OperationType.READ);
    Optional<LiteRODAObject> ret = getModelService().retrieveLiteFromObject(object);
    updateOperationState(operationLog, OperationState.SUCCESS);
    return ret;
  }

  @Override
  public <T extends IsModelObject> OptionalWithCause<T> retrieveObjectFromLite(LiteRODAObject liteRODAObject) {
    TransactionalModelOperationLog operationLog = registerOperation(liteRODAObject.getInfo(), OperationType.READ);
    OptionalWithCause<T> ret = getModelService().retrieveObjectFromLite(liteRODAObject);
    updateOperationState(operationLog, OperationState.SUCCESS);
    return ret;
  }

  @Override
  public TransferredResource retrieveTransferredResource(String fullPath) {
    TransactionalModelOperationLog operationLog = registerOperationForTransferredResource(fullPath, OperationType.READ);
    TransferredResource ret = getModelService().retrieveTransferredResource(fullPath);
    updateOperationState(operationLog, OperationState.SUCCESS);
    return ret;
  }

  @Override
  public <T extends IsRODAObject> CloseableIterable<OptionalWithCause<T>> list(Class<T> objectClass)
    throws RODAException {
    TransactionalModelOperationLog operationLog = registerOperation(objectClass.getName(), OperationType.READ);
    try {
      CloseableIterable<OptionalWithCause<T>> ret = getModelService().list(objectClass);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RODAException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public <T extends IsRODAObject> CloseableIterable<OptionalWithCause<LiteRODAObject>> listLite(Class<T> objectClass)
    throws RODAException {
    TransactionalModelOperationLog operationLog = registerOperation(objectClass.getName(), OperationType.READ);
    try {
      CloseableIterable<OptionalWithCause<LiteRODAObject>> ret = getModelService().listLite(objectClass);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RODAException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public CloseableIterable<OptionalWithCause<LogEntry>> listLogEntries() {
    TransactionalModelOperationLog operationLog = registerOperation(LogEntry.class.getName(), OperationType.READ);
    CloseableIterable<OptionalWithCause<LogEntry>> ret = getModelService().listLogEntries();
    updateOperationState(operationLog, OperationState.SUCCESS);
    return ret;
  }

  @Override
  public CloseableIterable<OptionalWithCause<LogEntry>> listLogEntries(int daysToIndex) {
    TransactionalModelOperationLog operationLog = registerOperation(LogEntry.class.getName(), OperationType.READ);
    CloseableIterable<OptionalWithCause<LogEntry>> ret = getModelService().listLogEntries(daysToIndex);
    updateOperationState(operationLog, OperationState.SUCCESS);
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
      operationLog = registerOperationForDIP(id, OperationType.READ);
    } else if (AIP.class.getName().equals(objectClass)) {
      operationLog = registerOperationForAIP(id, OperationType.READ);
    } else {
      LOGGER.warn(
        "Can't register read operation for checking object permission for unsupported object class ({} of class {})",
        objectClass, id);
    }
    try {
      boolean ret = getModelService().checkObjectPermission(username, permissionType, objectClass, id);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | NotFoundException | AuthorizationDeniedException | RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public RepresentationInformation createRepresentationInformation(RepresentationInformation ri, String createdBy,
    boolean commit) throws GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForRepresentationInformation(ri.getId(),
      OperationType.CREATE);
    try {
      RepresentationInformation ret = getModelService().createRepresentationInformation(ri, createdBy, commit);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public RepresentationInformation updateRepresentationInformation(RepresentationInformation ri, String updatedBy,
    boolean commit) throws GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForRepresentationInformation(ri.getId(),
      OperationType.UPDATE);
    try {
      RepresentationInformation ret = getModelService().updateRepresentationInformation(ri, updatedBy, commit);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public RepresentationInformation updateRepresentationInformationInstanceId(RepresentationInformation ri,
    String updatedBy, boolean notify)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForRepresentationInformation(ri.getId(),
      OperationType.UPDATE);
    try {
      RepresentationInformation ret = getModelService().updateRepresentationInformationInstanceId(ri, updatedBy,
        notify);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | NotFoundException | AuthorizationDeniedException | RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteRepresentationInformation(String representationInformationId, boolean commit)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = registerOperationForRepresentationInformation(
      representationInformationId, OperationType.DELETE);
    try {
      getModelService().deleteRepresentationInformation(representationInformationId, commit);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | NotFoundException | AuthorizationDeniedException | RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public RepresentationInformation retrieveRepresentationInformation(String representationInformationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForRepresentationInformation(
      representationInformationId, OperationType.READ);
    try {
      RepresentationInformation ret = getModelService().retrieveRepresentationInformation(representationInformationId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalHold retrieveDisposalHold(String disposalHoldId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForDisposalHold(disposalHoldId, OperationType.READ);
    try {
      DisposalHold ret = getModelService().retrieveDisposalHold(disposalHoldId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalHold createDisposalHold(DisposalHold disposalHold, String createdBy) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException, AlreadyExistsException, NotFoundException {
    TransactionalModelOperationLog operationLog = registerOperationForDisposalHold(disposalHold.getId(),
      OperationType.CREATE);
    try {
      DisposalHold ret = getModelService().createDisposalHold(disposalHold, createdBy);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | AlreadyExistsException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalHold updateDisposalHoldFirstUseDate(DisposalHold disposalHold, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, IllegalOperationException,
    GenericException {
    TransactionalModelOperationLog operationLog = registerOperationForDisposalHold(disposalHold.getId(),
      OperationType.UPDATE);
    try {
      DisposalHold ret = getModelService().updateDisposalHoldFirstUseDate(disposalHold, updatedBy);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | IllegalOperationException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalHold updateDisposalHold(DisposalHold disposalHold, String updatedBy, String details)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, IllegalOperationException,
    GenericException {
    TransactionalModelOperationLog operationLog = registerOperationForDisposalHold(disposalHold.getId(),
      OperationType.UPDATE);
    try {
      DisposalHold ret = getModelService().updateDisposalHold(disposalHold, updatedBy, details);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | IllegalOperationException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalHold updateDisposalHold(DisposalHold disposalHold, String updatedBy, boolean updateFirstUseDate,
    String details) throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException,
    IllegalOperationException {
    TransactionalModelOperationLog operationLog = registerOperationForDisposalHold(disposalHold.getId(),
      OperationType.UPDATE);
    try {
      DisposalHold ret = getModelService().updateDisposalHold(disposalHold, updatedBy, updateFirstUseDate, details);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | IllegalOperationException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteDisposalHold(String disposalHoldId) throws RequestNotValidException, NotFoundException,
    GenericException, AuthorizationDeniedException, IllegalOperationException {
    TransactionalModelOperationLog operationLog = registerOperationForDisposalHold(disposalHoldId,
      OperationType.DELETE);
    try {
      getModelService().deleteDisposalHold(disposalHoldId);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | IllegalOperationException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalHolds listDisposalHolds()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, IOException {
    TransactionalModelOperationLog operationLog = registerOperation(DisposalHold.class.getName(), OperationType.READ);
    try {
      DisposalHolds ret = getModelService().listDisposalHolds();
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | IOException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalAIPMetadata createDisposalHoldAssociation(String aipId, String disposalHoldId, Date associatedOn,
    String associatedBy)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    List<TransactionalModelOperationLog> operationLogs = new ArrayList<>();
    operationLogs.add(registerOperationForDisposalHold(disposalHoldId, OperationType.READ));
    operationLogs.add(registerOperationForAIP(aipId, OperationType.UPDATE));
    try {
      DisposalAIPMetadata ret = getModelService().createDisposalHoldAssociation(aipId, disposalHoldId, associatedOn,
        associatedBy);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | GenericException | NotFoundException | RequestNotValidException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public List<DisposalHold> retrieveDirectActiveDisposalHolds(String aipId)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    List<TransactionalModelOperationLog> operationLogs = new ArrayList<>();
    operationLogs.add(registerOperationForAIP(aipId, OperationType.READ));
    try {
      List<DisposalHold> ret = getModelService().retrieveDirectActiveDisposalHolds(aipId);
      for (DisposalHold hold : ret) {
        operationLogs.add(registerOperationForDisposalHold(hold.getId(), OperationType.READ));
      }
      for (TransactionalModelOperationLog operation : operationLogs) {
        updateOperationState(operation, OperationState.SUCCESS);
      }
      return ret;
    } catch (NotFoundException | AuthorizationDeniedException | GenericException | RequestNotValidException e) {
      for (TransactionalModelOperationLog operation : operationLogs) {
        updateOperationState(operation, OperationState.FAILURE);
      }
      throw e;
    }
  }

  @Override
  public boolean onDisposalHold(String aipId)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aipId, OperationType.READ);
    try {
      boolean ret = getModelService().onDisposalHold(aipId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | AuthorizationDeniedException | GenericException | RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public boolean isAIPOnDirectHold(String aipId, String holdId)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    List<TransactionalModelOperationLog> operationLogs = new ArrayList<>(
      List.of(registerOperationForAIP(aipId, OperationType.READ)));
    operationLogs.add(registerOperationForDisposalHold(holdId, OperationType.READ));
    try {
      boolean ret = getModelService().isAIPOnDirectHold(aipId, holdId);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | AuthorizationDeniedException | GenericException | RequestNotValidException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalSchedule createDisposalSchedule(DisposalSchedule disposalSchedule, String createdBy)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForDisposalSchedule(disposalSchedule.getId(),
      OperationType.CREATE);
    try {
      DisposalSchedule ret = getModelService().createDisposalSchedule(disposalSchedule, createdBy);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | AuthorizationDeniedException | GenericException | RequestNotValidException
      | AlreadyExistsException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalSchedule updateDisposalSchedule(DisposalSchedule disposalSchedule, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException,
    IllegalOperationException {
    TransactionalModelOperationLog operationLog = registerOperationForDisposalSchedule(disposalSchedule.getId(),
      OperationType.UPDATE);
    try {
      DisposalSchedule ret = getModelService().updateDisposalSchedule(disposalSchedule, updatedBy);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | AuthorizationDeniedException | GenericException | RequestNotValidException
      | IllegalOperationException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalSchedule retrieveDisposalSchedule(String disposalScheduleId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForDisposalSchedule(disposalScheduleId,
      OperationType.READ);
    try {
      DisposalSchedule ret = getModelService().retrieveDisposalSchedule(disposalScheduleId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | AuthorizationDeniedException | GenericException | RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalSchedules listDisposalSchedules()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, IOException {
    TransactionalModelOperationLog operationLog = registerOperation(DisposalSchedule.class.getName(),
      OperationType.READ);
    try {
      DisposalSchedules ret = getModelService().listDisposalSchedules();
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | IOException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteDisposalSchedule(String disposalScheduleId) throws NotFoundException, GenericException,
    AuthorizationDeniedException, RequestNotValidException, IllegalOperationException {
    TransactionalModelOperationLog operationLog = registerOperationForDisposalSchedule(disposalScheduleId,
      OperationType.DELETE);
    try {
      getModelService().deleteDisposalSchedule(disposalScheduleId);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
      | IllegalOperationException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalConfirmation retrieveDisposalConfirmation(String disposalConfirmationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForDisposalConfirmation(disposalConfirmationId,
      OperationType.READ);
    try {
      DisposalConfirmation ret = getModelService().retrieveDisposalConfirmation(disposalConfirmationId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void addDisposalHoldEntry(String disposalConfirmationId, DisposalHold disposalHold)
    throws GenericException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = registerOperationForDisposalConfirmation(disposalConfirmationId,
      OperationType.READ);
    try {
      getModelService().addDisposalHoldEntry(disposalConfirmationId, disposalHold);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void addDisposalHoldTransitiveEntry(String disposalConfirmationId, DisposalHold transitiveDisposalHold)
    throws RequestNotValidException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperationForDisposalConfirmation(disposalConfirmationId,
      OperationType.READ);
    try {
      getModelService().addDisposalHoldTransitiveEntry(disposalConfirmationId, transitiveDisposalHold);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (RequestNotValidException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void addDisposalScheduleEntry(String disposalConfirmationId, DisposalSchedule disposalSchedule)
    throws RequestNotValidException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperationForDisposalConfirmation(disposalConfirmationId,
      OperationType.UPDATE);
    try {
      getModelService().addDisposalScheduleEntry(disposalConfirmationId, disposalSchedule);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (RequestNotValidException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void addAIPEntry(String disposalConfirmationId, DisposalConfirmationAIPEntry entry)
    throws RequestNotValidException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperationForDisposalConfirmation(disposalConfirmationId,
      OperationType.UPDATE);
    try {
      getModelService().addAIPEntry(disposalConfirmationId, entry);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (RequestNotValidException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalConfirmation updateDisposalConfirmation(DisposalConfirmation disposalConfirmation)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperationForDisposalConfirmation(disposalConfirmation.getId(),
      OperationType.UPDATE);
    try {
      DisposalConfirmation ret = getModelService().updateDisposalConfirmation(disposalConfirmation);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalConfirmation createDisposalConfirmation(DisposalConfirmation disposalConfirmation, String createdBy)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForDisposalConfirmation(disposalConfirmation.getId(),
      OperationType.CREATE);
    try {
      DisposalConfirmation ret = getModelService().createDisposalConfirmation(disposalConfirmation, createdBy);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException
      | AlreadyExistsException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteDisposalConfirmation(String disposalConfirmationId) throws AuthorizationDeniedException,
    RequestNotValidException, NotFoundException, GenericException, IllegalOperationException {
    TransactionalModelOperationLog operationLog = registerOperationForDisposalConfirmation(disposalConfirmationId,
      OperationType.DELETE);
    try {
      getModelService().deleteDisposalConfirmation(disposalConfirmationId);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException
      | IllegalOperationException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }

  }

  @Override
  public DisposalHoldsAIPMetadata listDisposalHoldsAssociation(String aipId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aipId, OperationType.READ);
    try {
      DisposalHoldsAIPMetadata ret = getModelService().listDisposalHoldsAssociation(aipId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalTransitiveHoldsAIPMetadata listTransitiveDisposalHolds(String aipId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForAIP(aipId, OperationType.READ);
    try {
      DisposalTransitiveHoldsAIPMetadata ret = getModelService().listTransitiveDisposalHolds(aipId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalRule createDisposalRule(DisposalRule disposalRule, String createdBy) throws RequestNotValidException,
    NotFoundException, GenericException, AlreadyExistsException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForDisposalRule(disposalRule.getId(),
      OperationType.CREATE);
    try {
      DisposalRule ret = getModelService().createDisposalRule(disposalRule, createdBy);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AlreadyExistsException
      | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalRule updateDisposalRule(DisposalRule disposalRule, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperationForDisposalRule(disposalRule.getId(),
      OperationType.UPDATE);
    try {
      DisposalRule ret = getModelService().updateDisposalRule(disposalRule, updatedBy);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteDisposalRule(String disposalRuleId, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, IOException, GenericException, NotFoundException {
    TransactionalModelOperationLog operationLog = registerOperationForDisposalRule(disposalRuleId,
      OperationType.DELETE);
    try {
      getModelService().deleteDisposalRule(disposalRuleId, updatedBy);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | IOException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalRule retrieveDisposalRule(String disposalRuleId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForDisposalRule(disposalRuleId, OperationType.READ);
    try {
      DisposalRule ret = getModelService().retrieveDisposalRule(disposalRuleId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DisposalRules listDisposalRules()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, IOException {
    TransactionalModelOperationLog operationLog = registerOperation(DisposalRule.class.getName(), OperationType.READ);
    try {
      DisposalRules ret = getModelService().listDisposalRules();
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | IOException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DistributedInstance createDistributedInstance(DistributedInstance distributedInstance, String createdBy)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, AlreadyExistsException,
    NotFoundException, IllegalOperationException {
    TransactionalModelOperationLog operationLog = registerOperationForDistributedInstance(distributedInstance.getId(),
      OperationType.CREATE);
    try {
      DistributedInstance ret = getModelService().createDistributedInstance(distributedInstance, createdBy);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | AuthorizationDeniedException | RequestNotValidException | AlreadyExistsException
      | NotFoundException | IllegalOperationException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DistributedInstances listDistributedInstances()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, IOException {
    TransactionalModelOperationLog operationLog = registerOperation(DistributedInstance.class.getName(),
      OperationType.READ);
    try {
      DistributedInstances ret = getModelService().listDistributedInstances();
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | IOException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DistributedInstance retrieveDistributedInstance(String distributedInstanceId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TransactionalModelOperationLog operationLog = registerOperationForDistributedInstance(distributedInstanceId,
      OperationType.READ);
    try {
      DistributedInstance ret = getModelService().retrieveDistributedInstance(distributedInstanceId);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | AuthorizationDeniedException | RequestNotValidException | NotFoundException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteDistributedInstance(String distributedInstanceId)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = registerOperationForDistributedInstance(distributedInstanceId,
      OperationType.DELETE);
    try {
      getModelService().deleteDistributedInstance(distributedInstanceId);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (GenericException | AuthorizationDeniedException | RequestNotValidException | NotFoundException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DistributedInstance updateDistributedInstance(DistributedInstance distributedInstance, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperationForDistributedInstance(distributedInstance.getId(),
      OperationType.UPDATE);
    try {
      DistributedInstance ret = getModelService().updateDistributedInstance(distributedInstance, updatedBy);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | AuthorizationDeniedException | RequestNotValidException | NotFoundException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
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
    TransactionalModelOperationLog operationLog = registerOperation(object, OperationType.READ);
    try {
      StorageService ret = getModelService().resolveTemporaryResourceShallow(jobId, object, pathPartials);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public StorageService resolveTemporaryResourceShallow(String jobId, StorageService storage, IsRODAObject object,
    String... pathPartials) throws GenericException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = registerOperation(object, OperationType.READ);
    try {
      StorageService ret = getModelService().resolveTemporaryResourceShallow(jobId, storage, object, pathPartials);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public StorageService resolveTemporaryResourceShallow(String jobId, LiteRODAObject object, String... pathPartials)
    throws GenericException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = registerOperation(object.getInfo(), OperationType.READ);
    try {
      StorageService ret = getModelService().resolveTemporaryResourceShallow(jobId, object, pathPartials);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public StorageService resolveTemporaryResourceShallow(String jobId, StorageService storage, LiteRODAObject object,
    String... pathPartials) throws GenericException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = registerOperation(object.getInfo(), OperationType.READ);
    try {
      StorageService ret = getModelService().resolveTemporaryResourceShallow(jobId, storage, object, pathPartials);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Binary getBinary(IsRODAObject object, String... pathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperation(object, OperationType.READ);
    try {
      Binary ret = getModelService().getBinary(object, pathPartials);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | AuthorizationDeniedException | NotFoundException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Binary getBinary(LiteRODAObject lite, String... pathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperation(lite.getInfo(), OperationType.READ);
    try {
      Binary ret = getModelService().getBinary(lite, pathPartials);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | AuthorizationDeniedException | NotFoundException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public BinaryVersion getBinaryVersion(IsRODAObject object, String version, List<String> pathPartials)
    throws RequestNotValidException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperation(object, OperationType.READ);
    try {
      BinaryVersion ret = getModelService().getBinaryVersion(object, version, pathPartials);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public BinaryVersion getBinaryVersion(LiteRODAObject lite, String version, List<String> pathPartials)
    throws RequestNotValidException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperation(lite.getInfo(), OperationType.READ);
    try {
      BinaryVersion ret = getModelService().getBinaryVersion(lite, version, pathPartials);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public CloseableIterable<BinaryVersion> listBinaryVersions(IsRODAObject object)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperation(object, OperationType.READ);
    try {
      CloseableIterable<BinaryVersion> ret = getModelService().listBinaryVersions(object);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;

    } catch (RequestNotValidException | AuthorizationDeniedException | NotFoundException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public CloseableIterable<BinaryVersion> listBinaryVersions(LiteRODAObject lite)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperation(lite.getInfo(), OperationType.READ);
    try {
      CloseableIterable<BinaryVersion> ret = getModelService().listBinaryVersions(lite);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteBinaryVersion(IsRODAObject object, String version)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperation(object, OperationType.DELETE);
    try {
      getModelService().deleteBinaryVersion(object, version);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (RequestNotValidException | AuthorizationDeniedException | NotFoundException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteBinaryVersion(LiteRODAObject lite, String version)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperation(lite.getInfo(), OperationType.DELETE);
    try {
      getModelService().deleteBinaryVersion(lite, version);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Binary updateBinaryContent(IsRODAObject object, ContentPayload payload, boolean asReference,
    boolean createIfNotExists)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperation(object, OperationType.UPDATE);
    try {
      Binary ret = getModelService().updateBinaryContent(object, payload, asReference, createIfNotExists);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Binary updateBinaryContent(LiteRODAObject lite, ContentPayload payload, boolean asReference,
    boolean createIfNotExists)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperation(lite.getInfo(), OperationType.UPDATE);
    try {
      Binary ret = getModelService().updateBinaryContent(lite, payload, asReference, createIfNotExists);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Directory createDirectory(IsRODAObject object, String... pathPartials)
    throws AuthorizationDeniedException, AlreadyExistsException, GenericException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = registerOperation(object, OperationType.UPDATE);
    try {
      Directory ret = getModelService().createDirectory(object, pathPartials);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | AlreadyExistsException | GenericException | RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Directory createDirectory(LiteRODAObject lite, String... pathPartials)
    throws AuthorizationDeniedException, AlreadyExistsException, GenericException, RequestNotValidException {
    TransactionalModelOperationLog operationLog = registerOperation(lite.getInfo(), OperationType.UPDATE);
    try {
      Directory ret = getModelService().createDirectory(lite, pathPartials);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | AlreadyExistsException | GenericException | RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public boolean hasDirectory(IsRODAObject object, String... pathPartials) throws RequestNotValidException {
    TransactionalModelOperationLog operationLog = registerOperation(object, OperationType.READ);
    try {
      boolean ret = getModelService().hasDirectory(object, pathPartials);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public boolean hasDirectory(LiteRODAObject lite, String... pathPartials)
    throws RequestNotValidException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperation(lite.getInfo(), OperationType.READ);
    try {
      boolean ret = getModelService().hasDirectory(lite, pathPartials);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DirectResourceAccess getDirectAccess(IsRODAObject object, StorageService storage, String... pathPartials)
    throws RequestNotValidException {
    TransactionalModelOperationLog operationLog = registerOperation(object, OperationType.READ);
    try {
      DirectResourceAccess ret = getModelService().getDirectAccess(object, storage, pathPartials);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DirectResourceAccess getDirectAccess(LiteRODAObject lite, StorageService storage, String... pathPartials)
    throws RequestNotValidException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperation(lite.getInfo(), OperationType.READ);
    try {
      DirectResourceAccess ret = getModelService().getDirectAccess(lite, storage, pathPartials);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DirectResourceAccess getDirectAccess(IsRODAObject object, String... pathPartials)
    throws RequestNotValidException {
    TransactionalModelOperationLog operationLog = registerOperation(object, OperationType.READ);
    try {
      DirectResourceAccess ret = getModelService().getDirectAccess(object, pathPartials);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public DirectResourceAccess getDirectAccess(LiteRODAObject lite, String... pathPartials)
    throws RequestNotValidException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperation(lite.getInfo(), OperationType.READ);
    try {
      DirectResourceAccess ret = getModelService().getDirectAccess(lite, pathPartials);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;

    } catch (RequestNotValidException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public int importAll(IndexService index, FileStorageService fromStorage, boolean importJobs)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException {
    // TODO: This method should be reviewed
    TransactionalModelOperationLog operationLog = registerOperation(IsRODAObject.class.getName(), OperationType.CREATE);
    try {
      int ret = getModelService().importAll(index, fromStorage, importJobs);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException
      | AlreadyExistsException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void exportAll(StorageService toStorage) {
    TransactionalModelOperationLog operationLog = registerOperation(IsRODAObject.class.getName(), OperationType.READ);
    getModelService().exportAll(toStorage);
    updateOperationState(operationLog, OperationState.SUCCESS);
  }

  @Override
  public void importObject(IsRODAObject object, StorageService fromStorage) {
    TransactionalModelOperationLog operationLog = registerOperation(object, OperationType.UPDATE);
    getModelService().importObject(object, fromStorage);
    updateOperationState(operationLog, OperationState.SUCCESS);
  }

  @Override
  public void exportObject(IsRODAObject object, StorageService toStorage, String... toPathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException, NotFoundException,
    GenericException {
    TransactionalModelOperationLog operationLog = registerOperation(object, OperationType.READ);
    try {
      getModelService().exportObject(object, toStorage, toPathPartials);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (RequestNotValidException | AuthorizationDeniedException | AlreadyExistsException | NotFoundException
      | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }

  }

  @Override
  public void exportObject(LiteRODAObject lite, StorageService toStorage, String... toPathPartials)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, AlreadyExistsException,
    NotFoundException {
    TransactionalModelOperationLog operationLog = registerOperation(lite.getInfo(), OperationType.READ);
    try {
      getModelService().exportObject(lite, toStorage, toPathPartials);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | AlreadyExistsException
      | NotFoundException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void exportToPath(IsRODAObject object, Path toPath, boolean replaceExisting, String... fromPathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperation(object, OperationType.READ);
    try {
      getModelService().exportToPath(object, toPath, replaceExisting, fromPathPartials);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (RequestNotValidException | AuthorizationDeniedException | AlreadyExistsException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void exportToPath(LiteRODAObject lite, Path toPath, boolean replaceExisting, String... fromPathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperation(lite.getInfo(), OperationType.READ);
    try {
      getModelService().exportToPath(lite, toPath, replaceExisting, fromPathPartials);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (RequestNotValidException | AuthorizationDeniedException | AlreadyExistsException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public ConsumesOutputStream exportObjectToStream(IsRODAObject object, String... pathPartials)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperation(object, OperationType.READ);
    try {
      ConsumesOutputStream ret = getModelService().exportObjectToStream(object, pathPartials);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public ConsumesOutputStream exportObjectToStream(LiteRODAObject lite, String... pathPartials)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperation(lite.getInfo(), OperationType.READ);
    try {
      ConsumesOutputStream ret = getModelService().exportObjectToStream(lite, pathPartials);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public ConsumesOutputStream exportObjectToStream(IsRODAObject object, String name, boolean addTopDirectory,
    String... pathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperation(object, OperationType.READ);
    try {
      ConsumesOutputStream ret = getModelService().exportObjectToStream(object, name, addTopDirectory, pathPartials);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | AuthorizationDeniedException | NotFoundException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public ConsumesOutputStream exportObjectToStream(LiteRODAObject lite, String name, boolean addTopDirectory,
    String... pathPartials)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperation(lite.getInfo(), OperationType.READ);
    try {
      ConsumesOutputStream ret = getModelService().exportObjectToStream(lite, name, addTopDirectory, pathPartials);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void moveObject(LiteRODAObject fromPath, LiteRODAObject toPath) throws AuthorizationDeniedException,
    RequestNotValidException, AlreadyExistsException, NotFoundException, GenericException {

    TransactionalModelOperationLog operationLog = registerOperation(fromPath.getInfo(), OperationType.DELETE);
    TransactionalModelOperationLog moveOperationLog = registerOperation(toPath.getInfo(), OperationType.CREATE);
    try {
      getModelService().moveObject(fromPath, toPath);
      updateOperationState(operationLog, OperationState.SUCCESS);
      updateOperationState(moveOperationLog, OperationState.SUCCESS);
    } catch (AuthorizationDeniedException | RequestNotValidException | AlreadyExistsException | NotFoundException
      | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      updateOperationState(moveOperationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public String getObjectPathAsString(IsRODAObject object, boolean skipContainer) throws RequestNotValidException {
    TransactionalModelOperationLog operationLog = registerOperation(object, OperationType.READ);
    try {
      String ret = getModelService().getObjectPathAsString(object, skipContainer);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public String getObjectPathAsString(LiteRODAObject lite, boolean skipContainer)
    throws RequestNotValidException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperation(lite.getInfo(), OperationType.READ);
    try {
      String ret = getModelService().getObjectPathAsString(lite, skipContainer);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public boolean existsInStorage(LiteRODAObject lite, String... pathPartials)
    throws RequestNotValidException, GenericException {
    TransactionalModelOperationLog operationLog = registerOperation(lite.getInfo(), OperationType.READ);
    try {
      boolean ret = getModelService().existsInStorage(lite, pathPartials);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Date retrieveFileCreationDate(File file) throws RequestNotValidException, GenericException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForFile(file.getAipId(),
      file.getRepresentationId(), file.getPath(), file.getId(), OperationType.READ);
    try {
      Date ret = getModelService().retrieveFileCreationDate(file);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Date retrievePreservationMetadataCreationDate(PreservationMetadata pm)
    throws RequestNotValidException, GenericException {
    List<TransactionalModelOperationLog> operationLogs = registerOperationForPreservationMetadata(pm,
      OperationType.READ);
    try {
      Date ret = getModelService().retrievePreservationMetadataCreationDate(pm);
      updateOperationState(operationLogs, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | GenericException e) {
      updateOperationState(operationLogs, OperationState.FAILURE);
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

  private TransactionalModelOperationLog registerOperationForAIP(String aipID, OperationType operation) {
    acquireLock(AIP.class, aipID, operation);
    return registerOperation(AIP.class, Arrays.asList(aipID), operation);
  }

  private TransactionalModelOperationLog registerOperationForRelatedAIP(String aipID, OperationType operation) {
    acquireLock(AIP.class, aipID, operation);
    if (operation != OperationType.READ) {
      return registerOperation(AIP.class, Arrays.asList(aipID), OperationType.UPDATE);
    } else {
      return registerOperation(AIP.class, Arrays.asList(aipID), OperationType.READ);
    }
  }

  private List<TransactionalModelOperationLog> registerOperationForDescriptiveMetadata(String aipID,
    String representationId, String descriptiveMetadataId, OperationType operation) {
    List<TransactionalModelOperationLog> operationLogs = new ArrayList<>();
    operationLogs.add(registerOperationForRelatedAIP(aipID, operation));
    if (representationId == null) {
      operationLogs
        .add(registerOperation(DescriptiveMetadata.class, Arrays.asList(aipID, descriptiveMetadataId), operation));
    } else {
      operationLogs.add(registerOperation(DescriptiveMetadata.class,
        Arrays.asList(aipID, representationId, descriptiveMetadataId), operation));
    }
    return operationLogs;
  }

  private List<TransactionalModelOperationLog> registerOperationForRepresentation(String aipID, String representationId,
    OperationType operation) {
    List<TransactionalModelOperationLog> operationLogs = new ArrayList<>();
    operationLogs.add(registerOperationForRelatedAIP(aipID, operation));
    operationLogs.add(registerOperation(Representation.class, Arrays.asList(aipID, representationId), operation));
    return operationLogs;
  }

  private List<TransactionalModelOperationLog> registerOperationForFile(String aipID, String representationId,
    List<String> path, String fileID, OperationType operation) {
    return registerOperationForFile(aipID, representationId, path, fileID, null, operation);
  }

  private List<TransactionalModelOperationLog> registerOperationForFile(String aipID, String representationId,
    List<String> path, String fileID, String folderName, OperationType operation) {
    List<TransactionalModelOperationLog> operationLogs = new ArrayList<>();
    operationLogs.add(registerOperationForRelatedAIP(aipID, operation));
    List<String> list = new ArrayList<>();
    list.add(aipID);
    list.add(representationId);
    if (path != null) {
      list.addAll(path);
    }
    if (fileID != null) {
      list.add(fileID);
    }
    if (folderName != null) {
      list.add(folderName);
    }
    operationLogs.add(registerOperation(File.class, list, operation));
    return operationLogs;
  }

  private List<TransactionalModelOperationLog> registerOperationForEvent(PreservationMetadata event,
    OperationType operation) {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be null");
    }

    List<TransactionalModelOperationLog> operationLogs = new ArrayList<>();

    if (event.getAipId() == null) {
      operationLogs.add(registerOperation(IndexedPreservationEvent.class, Arrays.asList(event.getId()), operation));
    } else if (event.getRepresentationId() == null) {
      operationLogs.add(registerOperationForRelatedAIP(event.getAipId(), operation));
      operationLogs.add(
        registerOperation(IndexedPreservationEvent.class, Arrays.asList(event.getAipId(), event.getId()), operation));
    } else {
      operationLogs.add(registerOperationForRelatedAIP(event.getAipId(), operation));
      operationLogs.add(registerOperation(IndexedPreservationEvent.class,
        Arrays.asList(event.getAipId(), event.getRepresentationId(), event.getId()), operation));
    }

    return operationLogs;
  }

  private List<TransactionalModelOperationLog> registerOperationForPreservationMetadata(String preservationID,
    OperationType operation) {
    return registerOperationForPreservationMetadata(null, null, null, null, preservationID, operation);
  }

  private List<TransactionalModelOperationLog> registerOperationForPreservationMetadata(String aipId,
    String representationId, List<String> fileDirectoryPath, String fileId,
    PreservationMetadata.PreservationMetadataType type, OperationType operationType) {
    String preservationID = IdUtils.getPreservationId(type, aipId, representationId, fileDirectoryPath, fileId,
      RODAInstanceUtils.getLocalInstanceIdentifier());
    return registerOperationForPreservationMetadata(aipId, representationId, fileDirectoryPath, fileId, preservationID,
      operationType);
  }

  private List<TransactionalModelOperationLog> registerOperationForPreservationMetadata(String aipId,
    String representationId, OperationType operationType) {
    String preservationID = IdUtils.getRepresentationPreservationId(aipId, representationId,
      RODAInstanceUtils.getLocalInstanceIdentifier());
    return registerOperationForPreservationMetadata(aipId, representationId, null, null, preservationID, operationType);
  }

  private List<TransactionalModelOperationLog> registerOperationForPreservationMetadata(File file,
    OperationType operationType) {
    String preservationID = IdUtils.getPreservationId(PreservationMetadata.PreservationMetadataType.FILE,
      file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(),
      RODAInstanceUtils.getLocalInstanceIdentifier());
    return registerOperationForPreservationMetadata(file.getAipId(), file.getRepresentationId(), file.getPath(),
      file.getId(), preservationID, operationType);
  }

  private List<TransactionalModelOperationLog> registerOperationForPreservationMetadata(PreservationMetadata pm,
    OperationType operationType) {
    if (pm == null) {
      throw new IllegalArgumentException("PreservationMetadata cannot be null");
    }
    return registerOperationForPreservationMetadata(pm.getAipId(), pm.getRepresentationId(), pm.getFileDirectoryPath(),
      pm.getFileId(), pm.getId(), operationType);
  }

  private List<TransactionalModelOperationLog> registerOperationForPreservationMetadata(String aipID,
    String representationId, List<String> path, String fileID, String preservationID, OperationType operation) {
    List<TransactionalModelOperationLog> operationLogs = new ArrayList<>();
    if (aipID == null) {
      acquireLock(PreservationMetadata.class, preservationID, operation);
      operationLogs.add(registerOperation(PreservationMetadata.class, Arrays.asList(preservationID), operation));
    } else if (representationId == null) {
      operationLogs.add(registerOperationForRelatedAIP(aipID, operation));
      operationLogs.add(registerOperation(PreservationMetadata.class, Arrays.asList(aipID, preservationID), operation));
    } else if (fileID == null) {
      operationLogs.add(registerOperationForRelatedAIP(aipID, operation));
      operationLogs.add(registerOperation(PreservationMetadata.class,
        Arrays.asList(aipID, representationId, preservationID), operation));
    } else {
      operationLogs.add(registerOperationForRelatedAIP(aipID, operation));
      List<String> list = new ArrayList<>();
      list.add(aipID);
      list.add(representationId);
      list.addAll(path);
      list.add(fileID);
      list.add(preservationID);
      operationLogs.add(registerOperation(PreservationMetadata.class, list, operation));
    }
    return operationLogs;
  }

  private TransactionalModelOperationLog registerOperationForOtherMetadata(String aipID, OperationType operation) {
    return registerOperationForRelatedAIP(aipID, operation);
  }

  private TransactionalModelOperationLog registerOperationForDIP(String dipID, OperationType operation) {
    acquireLock(DIP.class, dipID, operation);
    return registerOperation(DIP.class, Arrays.asList(dipID), operation);
  }

  private TransactionalModelOperationLog registerOperationForLogEntry(String logEntryID, OperationType operation) {
    acquireLock(LogEntry.class, logEntryID, operation);
    return registerOperation(LogEntry.class, Arrays.asList(logEntryID), operation);
  }

  private TransactionalModelOperationLog registerOperationForJob(String jobID, OperationType operation) {
    return registerOperation(Job.class, Arrays.asList(jobID), operation);
  }

  private TransactionalModelOperationLog registerOperationForJobReport(String jobID, String jobReportID,
    OperationType operation) {
    return registerOperation(Report.class, Arrays.asList(jobID, jobReportID), operation);
  }

  private TransactionalModelOperationLog registerOperationForTransferredResource(String fullPath,
    OperationType operation) {
    return registerOperation(TransferredResource.class, Arrays.asList(fullPath), operation);
  }

  private TransactionalModelOperationLog registerOperationForRisk(String riskID, OperationType operation) {
    return registerOperation(Risk.class, Arrays.asList(riskID), operation);
  }

  private TransactionalModelOperationLog registerOperationForRiskIncidence(String incidenceID,
    OperationType operation) {
    return registerOperation(RiskIncidence.class, Arrays.asList(incidenceID), operation);
  }

  private TransactionalModelOperationLog registerOperationForNotification(String notificationID,
    OperationType operation) {
    return registerOperation(Notification.class, Arrays.asList(notificationID), operation);
  }

  private TransactionalModelOperationLog registerOperationForDIPFile(String dipID, List<String> path, String id,
    OperationType operation) {
    List<String> ids = new ArrayList<>();
    ids.add(dipID);
    ids.addAll(path);
    ids.add(id);
    return registerOperation(DIPFile.class, ids, operation);
  }

  private TransactionalModelOperationLog registerOperationForRepresentationInformation(String id,
    OperationType operation) {
    return registerOperation(RepresentationInformation.class, Arrays.asList(id), operation);
  }

  private TransactionalModelOperationLog registerOperationForDisposalHold(String id, OperationType operation) {
    return registerOperation(DisposalHold.class, Arrays.asList(id), operation);
  }

  private TransactionalModelOperationLog registerOperationForDisposalSchedule(String id, OperationType operation) {
    return registerOperation(DisposalSchedule.class, Arrays.asList(id), operation);
  }

  private TransactionalModelOperationLog registerOperationForDisposalConfirmation(String id, OperationType operation) {
    return registerOperation(DisposalConfirmation.class, Arrays.asList(id), operation);
  }

  private TransactionalModelOperationLog registerOperationForDisposalRule(String id, OperationType operation) {
    return registerOperation(DisposalRule.class, Arrays.asList(id), operation);
  }

  private TransactionalModelOperationLog registerOperationForDistributedInstance(String id, OperationType operation) {
    return registerOperation(DistributedInstance.class, Arrays.asList(id), operation);
  }

  private <T extends IsRODAObject> TransactionalModelOperationLog registerOperation(T object, OperationType operation) {
    Optional<LiteRODAObject> objectLite = LiteRODAObjectFactory.get(object);
    if (objectLite.isEmpty()) {
      throw new IllegalArgumentException("Cannot register operation for object: " + object);
    } else {
      return registerOperation(objectLite.get().getInfo(), operation);
    }
  }

  private <T extends IsRODAObject> TransactionalModelOperationLog registerOperation(Class<T> objectClass,
    List<String> ids, OperationType operation) {
    if (ids == null || ids.isEmpty()) {
      throw new IllegalArgumentException("Object IDs cannot be null or a empty list");
    }
    Optional<LiteRODAObject> liteRODAObject = LiteRODAObjectFactory.get(objectClass, ids);
    if (liteRODAObject.isPresent()) {
      return registerOperation(liteRODAObject.get().getInfo(), operation);
    } else {
      throw new IllegalArgumentException("Cannot register operation for object: " + liteRODAObject);
    }
  }

  private TransactionalModelOperationLog registerOperation(String liteInfo, OperationType operation) {
    if (operation == OperationType.READ) {
      // TODO: add a configuration to allow logging the read operation for debugging
      // purposes
      return null;
    }
    try {
      LOGGER.debug("Registering operation {} for liteInfo {}", operation, liteInfo);
      return transactionLogService.registerModelOperation(transaction.getId(), liteInfo, operation);
    } catch (RODATransactionException e) {
      throw new IllegalArgumentException("Cannot register operation for liteInfo: " + liteInfo, e);
    }
  }

  public void updateOperationState(List<TransactionalModelOperationLog> operationLogs, OperationState state) {
    for (TransactionalModelOperationLog operationLog : operationLogs) {
      updateOperationState(operationLog, state);
    }
  }

  public void updateOperationState(TransactionalModelOperationLog operationLog, OperationState state) {
    try {
      if (operationLog != null) {
        transactionLogService.updateModelOperationState(operationLog.getId(), state);
      }
    } catch (RODATransactionException e) {
      throw new IllegalArgumentException("Cannot update operation state: " + operationLog.getId(), e);
    }
  }

  private <T extends IsRODAObject> void acquireLock(Class<T> objectClass, String id, OperationType operation) {
    if (id == null) {
      throw new IllegalArgumentException("Object ID cannot be null");
    }

    if (operation == OperationType.READ) {
      // DO NOT acquire lock for READ operation
      return;
    }

    if (!isLockableClass(objectClass)) {
      throw new IllegalArgumentException("Object class is not lockable: " + objectClass.getName());
    }

    Optional<LiteRODAObject> liteRODAObject = LiteRODAObjectFactory.get(objectClass, id);
    if (liteRODAObject.isPresent()) {
      try {
        String lite = liteRODAObject.get().getInfo();
        PluginHelper.acquireObjectLock(lite, transaction.getRequestId().toString());
      } catch (LockingException e) {
        throw new IllegalArgumentException("Cannot acquire lock for object: " + liteRODAObject);
      }
    } else {
      throw new IllegalArgumentException(
        "Cannot acquire lock for object ID: " + id + " of class: " + objectClass.getName());
    }
  }

  private boolean isLockableClass(Class<? extends IsRODAObject> objectClass) {
    return AIP.class.isAssignableFrom(objectClass) || DIP.class.isAssignableFrom(objectClass)
      || LogEntry.class.isAssignableFrom(objectClass) || PreservationMetadata.class.isAssignableFrom(objectClass);
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
        if (isLockableClass(isRODAObjectClass)) {
          PluginHelper.releaseObjectLock(modelOperation.getLiteObject(), transaction.getRequestId().toString());
        }
      }
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
        } else if (rodaObject instanceof DIP dip) {
          handleDIPRollback(dip, modelOperation);
        } else {
          LOGGER.warn("Cannot rollback operation for class: {} with ID: {}", rodaObject.getClass().getSimpleName(),
            rodaObject.getId());
        }
      }

      PluginHelper.releaseObjectLock(modelOperation.getLiteObject(), transaction.getRequestId().toString());
    }
  }

  private void handleAIPRollback(AIP aip, TransactionalModelOperationLog modelOperation) {
    if (modelOperation.getOperationType() == OperationType.CREATE) {
      LOGGER.debug("Rollback AIP creation for AIP: {}", aip.getId());
      stagingModelService.notifyAipDeleted(aip.getId());
    } else if (modelOperation.getOperationType() != OperationType.READ) {
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
    if (modelOperation.getOperationType() == OperationType.CREATE) {
      LOGGER.debug("Rollback Representation creation for Representation: {}", representation.getId());
      stagingModelService.notifyRepresentationDeleted(representation.getAipId(), representation.getId());
    } else if (modelOperation.getOperationType() != OperationType.READ) {
      LOGGER.debug("Rollback Representation update/delete for Representation: {}", representation.getId());
      mainModelService.notifyRepresentationUpdated(representation);
    }
  }

  private void handleFileRollback(File file, TransactionalModelOperationLog modelOperation) {
    if (modelOperation.getOperationType() == OperationType.CREATE) {
      LOGGER.debug("Rollback File creation for File: {}", file.getId());
      stagingModelService.notifyFileDeleted(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId());
    } else if (modelOperation.getOperationType() != OperationType.READ) {
      LOGGER.debug("Rollback File update/delete for File: {}", file.getId());
      mainModelService.notifyFileUpdated(file);
    }
  }

  private void handleDIPRollback(DIP dip, TransactionalModelOperationLog modelOperation) {
    if (modelOperation.getOperationType() == OperationType.CREATE) {
      LOGGER.debug("Rollback DIP creation for DIP: {}", dip.getId());
      stagingModelService.notifyDIPDeleted(dip.getId(), true);
    } else if (modelOperation.getOperationType() != OperationType.READ) {
      LOGGER.debug("Rollback DIP update/delete for DIP: {}", dip.getId());
      try {
        DIP retrieveDIP = mainModelService.retrieveDIP(dip.getId());
        mainModelService.notifyDIPUpdated(retrieveDIP, true);
      } catch (GenericException | NotFoundException | AuthorizationDeniedException e) {
        throw new RuntimeException(e);
      }
    }
  }

}
