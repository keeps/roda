package org.roda.core.transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.LockingException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.entity.transaction.OperationState;
import org.roda.core.entity.transaction.OperationType;
import org.roda.core.entity.transaction.TransactionLog;
import org.roda.core.entity.transaction.TransactionalModelOperationLog;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.storage.utils.RODAInstanceUtils;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class TransactionalModelOperationRegistry {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionalModelOperationRegistry.class);
  private final TransactionLog transaction;
  private final TransactionLogService transactionLogService;
  private final ModelService mainModelService;

  public TransactionalModelOperationRegistry(TransactionLog transaction, TransactionLogService transactionLogService,
    ModelService mainModelService) {
    this.transaction = transaction;
    this.transactionLogService = transactionLogService;
    this.mainModelService = mainModelService;
  }

  public TransactionalModelOperationLog registerCreateOperationForAIP(String aipID)
    throws RequestNotValidException, AlreadyExistsException, GenericException {
    acquireLockAndCheckPreconditions(AIP.class, aipID, OperationType.CREATE);
    return registerOperation(AIP.class, Arrays.asList(aipID), OperationType.CREATE);
  }

  public TransactionalModelOperationLog registerReadOperationForAIP(String aipID) {
    acquireLock(AIP.class, aipID, OperationType.READ);
    return registerOperation(AIP.class, Arrays.asList(aipID), OperationType.READ);
  }

  public TransactionalModelOperationLog registerUpdateOperationForAIP(String aipID) {
    acquireLock(AIP.class, aipID, OperationType.UPDATE);
    return registerOperation(AIP.class, Arrays.asList(aipID), OperationType.UPDATE);
  }

  public TransactionalModelOperationLog registerDeleteOperationForAIP(String aipID) {
    acquireLock(AIP.class, aipID, OperationType.DELETE);
    return registerOperation(AIP.class, Arrays.asList(aipID), OperationType.DELETE);
  }

  private TransactionalModelOperationLog registerOperationForRelatedAIP(String aipID, OperationType operation) {
    acquireLock(AIP.class, aipID, operation);
    if (operation != OperationType.READ) {
      return registerOperation(AIP.class, Arrays.asList(aipID), OperationType.UPDATE);
    } else {
      return registerOperation(AIP.class, Arrays.asList(aipID), OperationType.READ);
    }
  }

  public List<TransactionalModelOperationLog> registerOperationForDescriptiveMetadata(String aipID,
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

  public List<TransactionalModelOperationLog> registerOperationForRepresentation(String aipID, String representationId,
    OperationType operation) {
    List<TransactionalModelOperationLog> operationLogs = new ArrayList<>();
    operationLogs.add(registerOperationForRelatedAIP(aipID, operation));
    operationLogs.add(registerOperation(Representation.class, Arrays.asList(aipID, representationId), operation));
    return operationLogs;
  }

  public List<TransactionalModelOperationLog> registerOperationForFile(String aipID, String representationId,
    List<String> path, String fileID, OperationType operation) {
    return registerOperationForFile(aipID, representationId, path, fileID, null, operation);
  }

  public List<TransactionalModelOperationLog> registerOperationForFile(String aipID, String representationId,
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

  /*
   * Registers an operation for PreservationMetadata event.
   */

  public List<TransactionalModelOperationLog> registerCreateOperationForEvent(String aipID, String representationId,
    String eventID, RodaConstants.PreservationEventType eventType)
    throws RequestNotValidException, AlreadyExistsException, GenericException {
    OperationType operation = OperationType.CREATE;
    if (aipID == null) {
      acquireLockAndCheckPreconditions(PreservationMetadata.class, eventID, operation);
    }
    return registerOperationForEvent(aipID, representationId, eventID, eventType, operation);
  }

  public List<TransactionalModelOperationLog> registerUpdateOperationForEvent(String aipID, String representationId,
    String eventID, RodaConstants.PreservationEventType eventType) {
    OperationType operation = OperationType.UPDATE;
    if (aipID == null) {
      acquireLock(PreservationMetadata.class, eventID, operation);
    }
    return registerOperationForEvent(aipID, representationId, eventID, eventType, operation);
  }

  private List<TransactionalModelOperationLog> registerOperationForEvent(String aipID, String representationId,
    String eventID, RodaConstants.PreservationEventType eventType, OperationType operation) {

    List<TransactionalModelOperationLog> operationLogs = new ArrayList<>();

    if (aipID == null) {
      operationLogs.add(registerOperation(PreservationMetadata.class, Collections.singletonList(eventID), operation));
    } else if (representationId == null) {
      operationLogs.add(registerOperationForRelatedAIP(aipID, operation));
      operationLogs.add(registerOperation(PreservationMetadata.class, Arrays.asList(aipID, eventID), operation));
    } else {
      operationLogs.add(registerOperationForRelatedAIP(aipID, operation));
      operationLogs
        .add(registerOperation(PreservationMetadata.class, Arrays.asList(aipID, representationId, eventID), operation));
    }

    return operationLogs;
  }

  /*
   * Registers an operation for PreservationMetadata.
   */

  public List<TransactionalModelOperationLog> registerCreateOperationForPreservationMetadata(String aipID,
    String representationId, List<String> path, String fileID, String preservationID,
    PreservationMetadata.PreservationMetadataType type)
    throws RequestNotValidException, AlreadyExistsException, GenericException {
    OperationType operation = OperationType.CREATE;

    if (preservationID == null) {
      preservationID = IdUtils.getPreservationId(type, aipID, representationId, path, fileID,
        RODAInstanceUtils.getLocalInstanceIdentifier());
    }

    if (aipID == null) {
      acquireLockAndCheckPreconditions(PreservationMetadata.class, preservationID, operation);
    }
    return registerOperationForPreservationMetadata(aipID, representationId, path, fileID, preservationID, operation);
  }

  public List<TransactionalModelOperationLog> registerReadOperationForPreservationMetadata(String aipID,
    String representationId, List<String> path, String fileID, String preservationID,
    PreservationMetadata.PreservationMetadataType type) {
    OperationType operation = OperationType.READ;

    if (preservationID == null) {
      preservationID = IdUtils.getPreservationId(type, aipID, representationId, path, fileID,
        RODAInstanceUtils.getLocalInstanceIdentifier());
    }

    if (aipID == null) {
      acquireLock(PreservationMetadata.class, preservationID, operation);
    }
    return registerOperationForPreservationMetadata(aipID, representationId, path, fileID, preservationID, operation);
  }

  public List<TransactionalModelOperationLog> registerUpdateOperationForPreservationMetadata(String aipID,
    String representationId, List<String> path, String fileID, String preservationID,
    PreservationMetadata.PreservationMetadataType type) {
    OperationType operation = OperationType.UPDATE;
    if (preservationID == null) {
      preservationID = IdUtils.getPreservationId(type, aipID, representationId, path, fileID,
        RODAInstanceUtils.getLocalInstanceIdentifier());
    }

    if (aipID == null) {
      acquireLock(PreservationMetadata.class, preservationID, operation);
    }
    return registerOperationForPreservationMetadata(aipID, representationId, path, fileID, preservationID, operation);
  }

  public List<TransactionalModelOperationLog> registerDeleteOperationForPreservationMetadata(String aipID,
    String representationId, List<String> path, String fileID, String preservationID,
    PreservationMetadata.PreservationMetadataType type) {
    OperationType operation = OperationType.DELETE;

    if (preservationID == null) {
      preservationID = IdUtils.getPreservationId(PreservationMetadata.PreservationMetadataType.FILE, aipID,
        representationId, path, fileID, RODAInstanceUtils.getLocalInstanceIdentifier());
    }

    if (aipID == null) {
      acquireLock(PreservationMetadata.class, preservationID, operation);
    }
    return registerOperationForPreservationMetadata(aipID, representationId, path, fileID, preservationID, operation);
  }

  private List<TransactionalModelOperationLog> registerOperationForPreservationMetadata(String aipID,
    String representationId, List<String> path, String fileID, String preservationID, OperationType operation) {
    List<TransactionalModelOperationLog> operationLogs = new ArrayList<>();
    if (aipID == null) {
      // Lock is already acquired in the caller method
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

  /*
   * Registers an operation for other metadata
   */

  public TransactionalModelOperationLog registerOperationForOtherMetadata(String aipID, OperationType operation) {
    return registerOperationForRelatedAIP(aipID, operation);
  }

  public TransactionalModelOperationLog registerOperationForDIP(String dipID, OperationType operation) {
    acquireLock(DIP.class, dipID, operation);
    return registerOperation(DIP.class, Arrays.asList(dipID), operation);
  }

  public TransactionalModelOperationLog registerOperationForLogEntry(String logEntryID, OperationType operation) {
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

  public TransactionalModelOperationLog registerOperationForTransferredResource(String fullPath,
    OperationType operation) {
    return registerOperation(TransferredResource.class, Arrays.asList(fullPath), operation);
  }

  public TransactionalModelOperationLog registerOperationForRisk(String riskID, OperationType operation) {
    return registerOperation(Risk.class, Arrays.asList(riskID), operation);
  }

  public TransactionalModelOperationLog registerOperationForRiskIncidence(String incidenceID, OperationType operation) {
    return registerOperation(RiskIncidence.class, Arrays.asList(incidenceID), operation);
  }

  public TransactionalModelOperationLog registerOperationForNotification(String notificationID,
    OperationType operation) {
    return registerOperation(Notification.class, Arrays.asList(notificationID), operation);
  }

  public TransactionalModelOperationLog registerOperationForDIPFile(String dipID, List<String> path, String id,
    OperationType operation) {
    List<String> ids = new ArrayList<>();
    ids.add(dipID);
    ids.addAll(path);
    ids.add(id);
    return registerOperation(DIPFile.class, ids, operation);
  }

  public TransactionalModelOperationLog registerOperationForRepresentationInformation(String id,
    OperationType operation) {
    return registerOperation(RepresentationInformation.class, Arrays.asList(id), operation);
  }

  public TransactionalModelOperationLog registerOperationForDisposalHold(String id, OperationType operation) {
    return registerOperation(DisposalHold.class, Arrays.asList(id), operation);
  }

  public TransactionalModelOperationLog registerOperationForDisposalSchedule(String id, OperationType operation) {
    return registerOperation(DisposalSchedule.class, Arrays.asList(id), operation);
  }

  public TransactionalModelOperationLog registerOperationForDisposalConfirmation(String id, OperationType operation) {
    return registerOperation(DisposalConfirmation.class, Arrays.asList(id), operation);
  }

  public TransactionalModelOperationLog registerOperationForDisposalRule(String id, OperationType operation) {
    return registerOperation(DisposalRule.class, Arrays.asList(id), operation);
  }

  public TransactionalModelOperationLog registerOperationForDistributedInstance(String id, OperationType operation) {
    return registerOperation(DistributedInstance.class, Arrays.asList(id), operation);
  }

  public <T extends IsRODAObject> TransactionalModelOperationLog registerOperation(T object, OperationType operation) {
    Optional<LiteRODAObject> objectLite = LiteRODAObjectFactory.get(object);
    if (objectLite.isEmpty()) {
      throw new IllegalArgumentException(
        "[transactionId:" + transaction.getId() + "] Cannot register operation for object: " + object);
    } else {
      return registerOperation(objectLite.get().getInfo(), operation);
    }
  }

  public <T extends IsRODAObject> TransactionalModelOperationLog registerOperation(Class<T> objectClass,
    List<String> ids, OperationType operation) {
    if (ids == null || ids.isEmpty()) {
      throw new IllegalArgumentException(
        "[transactionId:" + transaction.getId() + "] Object IDs cannot be null or a empty list");
    }
    Optional<LiteRODAObject> liteRODAObject = LiteRODAObjectFactory.get(objectClass, ids);
    if (liteRODAObject.isPresent()) {
      return registerOperation(liteRODAObject.get().getInfo(), operation);
    } else {
      throw new IllegalArgumentException(
        "[transactionId:" + transaction.getId() + "] Cannot register operation for object: " + liteRODAObject);
    }
  }

  public TransactionalModelOperationLog registerOperation(String liteInfo, OperationType operation) {
    if (operation == OperationType.READ) {
      // TODO: add a configuration to allow logging the read operation for debugging
      // purposes
      return null;
    }
    try {
      LOGGER.debug("[transactionId:{}] Registering operation {} for liteInfo {}", transaction.getId(), operation,
        liteInfo);
      return transactionLogService.registerModelOperation(transaction.getId(), liteInfo, operation);
    } catch (RODATransactionException e) {
      throw new IllegalArgumentException(
        "[transactionId:" + transaction.getId() + "] Cannot register operation for liteInfo: " + liteInfo, e);
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
      throw new IllegalArgumentException(
        "[transactionId:" + transaction.getId() + "] Cannot update operation state: " + operationLog.getId(), e);
    }
  }

  private void acquireLockAndCheckPreconditions(Class<? extends IsRODAObject> clazz, String id, OperationType operation)
    throws AlreadyExistsException, RequestNotValidException, GenericException {
    acquireLock(clazz, id, operation);
    if (operation == OperationType.CREATE) {
      // Verify if the object already exists
      try {
        checkIfEntityExistsAndThrowException(clazz, id);
      } catch (AlreadyExistsException e) {
        LOGGER.debug(
          "[transactionId:{}] Entity with ID {} already exists, cannot create a new one. Releasing lock and informing requester method.",
          transaction.getId(), id);
        releaseLock(clazz, id, operation);
        throw e;
      } catch (RequestNotValidException | GenericException e) {
        LOGGER.error(
          "[transactionId:{}] Error checking if entity with ID {} exists, releasing lock and informing requester method.",
          transaction.getId(), id, e);
        releaseLock(clazz, id, operation);
        throw e;
      }
    }
    // Cannot check pre-conditions for UPDATE or DELETE operations, such as checking
    // if the object exists in main storage, because some updates or deletes may
    // only be on the scope of staging and due to previous CREATE operations in
    // staging.
  }

  private <T extends IsRODAObject> void acquireLock(Class<T> objectClass, String id, OperationType operation) {
    if (id == null) {
      throw new IllegalArgumentException("[transactionId:" + transaction.getId() + "] Object ID cannot be null");
    }

    if (operation == OperationType.READ) {
      // DO NOT acquire lock for READ operation
      return;
    }

    if (!isLockableClass(objectClass)) {
      throw new IllegalArgumentException(
        "[transactionId:" + transaction.getId() + "] Object class is not lockable: " + objectClass.getName());
    }

    if (PreservationMetadata.class.isAssignableFrom(objectClass)) {
      PreservationMetadata.PreservationMetadataType preservationType = IdUtils.getPreservationTypeFromId(id);
      if (preservationType != null && preservationType.equals(PreservationMetadata.PreservationMetadataType.AGENT)) {
        // AGENT type is a special case, do not acquire lock for it
        LOGGER.debug("[transactionId:{}] Skipping lock for agent {} and operation {}", transaction.getId(), id,
          operation);
        return;
      }
    }

    Optional<LiteRODAObject> liteRODAObject = LiteRODAObjectFactory.get(objectClass, id);
    if (liteRODAObject.isPresent()) {
      try {
        String lite = liteRODAObject.get().getInfo();
        PluginHelper.acquireObjectLock(lite, transaction.getRequestId().toString());
      } catch (LockingException e) {
        throw new IllegalArgumentException(
          "[transactionId:" + transaction.getId() + "] Cannot acquire lock for object: " + liteRODAObject);
      }
    } else {
      throw new IllegalArgumentException("[transactionId:" + transaction.getId()
        + "] Cannot acquire lock for object ID: " + id + " of class: " + objectClass.getName());
    }
  }

  public <T extends IsRODAObject> void releaseLock(Class<T> objectClass, String id, OperationType operation) {
    if (id == null) {
      throw new IllegalArgumentException("[transactionId:" + transaction.getId() + "] Object ID cannot be null");
    }

    if (operation == OperationType.READ) {
      // DO NOT acquire lock for READ operation, so also do not release it.
      return;
    }

    if (!isLockableClass(objectClass)) {
      throw new IllegalArgumentException(
        "[transactionId:" + transaction.getId() + "] Object class is not lockable: " + objectClass.getName());
    }

    if (PreservationMetadata.class.isAssignableFrom(objectClass)) {
      PreservationMetadata.PreservationMetadataType preservationType = IdUtils.getPreservationTypeFromId(id);
      if (preservationType != null && preservationType.equals(PreservationMetadata.PreservationMetadataType.AGENT)) {
        // AGENT type is a special case, do not acquire lock for it
        LOGGER.debug("[transactionId:{}] Skipping release lock for agent {} and operation {}", transaction.getId(), id,
          operation);
        return;
      }
    }

    Optional<LiteRODAObject> liteRODAObject = LiteRODAObjectFactory.get(objectClass, id);
    if (liteRODAObject.isPresent()) {
      String lite = liteRODAObject.get().getInfo();
      PluginHelper.releaseObjectLock(lite, transaction.getRequestId().toString());
    } else {
      throw new IllegalArgumentException("[transactionId:" + transaction.getId()
        + "] Cannot release lock for object ID: " + id + " of class: " + objectClass.getName());
    }
  }

  public boolean isLockableClass(Class<? extends IsRODAObject> objectClass) {
    return AIP.class.isAssignableFrom(objectClass) || DIP.class.isAssignableFrom(objectClass)
      || LogEntry.class.isAssignableFrom(objectClass) || PreservationMetadata.class.isAssignableFrom(objectClass);
  }

  public <T extends IsRODAObject> void checkIfEntityExistsAndThrowException(Class<T> objectClass, String... ids)
    throws AlreadyExistsException, RequestNotValidException, GenericException {
    String[] filteredIds = Arrays.stream(ids).filter(Objects::nonNull).toArray(String[]::new);
    Optional<LiteRODAObject> liteRODAObject = LiteRODAObjectFactory.get(objectClass, filteredIds);
    if (liteRODAObject.isPresent()) {
      if (mainModelService.existsInStorage(liteRODAObject.get())) {
        throw new AlreadyExistsException("[transactionId:" + transaction.getId() + "] Entity '" + liteRODAObject.get()
          + "' already exists in the storage");
      }
    }
  }
}
