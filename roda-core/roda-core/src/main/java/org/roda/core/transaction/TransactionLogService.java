package org.roda.core.transaction;

import java.util.List;
import java.util.UUID;

import org.roda.core.entity.transaction.OperationState;
import org.roda.core.entity.transaction.OperationType;
import org.roda.core.entity.transaction.TransactionLog;
import org.roda.core.entity.transaction.TransactionalModelOperationLog;
import org.roda.core.entity.transaction.TransactionalStoragePathOperationLog;
import org.roda.core.repository.transaction.TransactionLogRepository;
import org.roda.core.repository.transaction.TransactionalModelOperationLogRepository;
import org.roda.core.repository.transaction.TransactionalStoragePathRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Service
@Timed
@Counted
public class TransactionLogService {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionLogService.class);

  @Autowired
  private TransactionLogRepository transactionLogRepository;
  @Autowired
  private TransactionalModelOperationLogRepository transactionalModelOperationLogRepository;
  @Autowired
  private TransactionalStoragePathRepository transactionalStoragePathRepository;

  /*
   * TransactionLogRepository
   */

  public List<TransactionLog> getUnfinishedTransactions() {
    return transactionLogRepository.findByStatusOrderByCreatedAt(TransactionLog.TransactionStatus.PENDING);
  }

  public List<TransactionLog> getCommittedTransactions() {
    return transactionLogRepository.findByStatusOrderByCreatedAt(TransactionLog.TransactionStatus.COMMITTED);
  }

  @Transactional
  public TransactionLog createTransactionLog(TransactionLog.TransactionRequestType requestType, UUID requestId) {
    TransactionLog transactionLog = new TransactionLog(requestType, requestId);
    transactionLogRepository.save(transactionLog);
    return transactionLog;
  }

  public TransactionLog getTransactionLog(UUID transactionId) throws RODATransactionException {
    if (transactionId == null) {
      throw new RODATransactionException("Transaction ID cannot be null");
    }
    return getTransactionLogById(transactionId, true);
  }

  private TransactionLog getTransactionLogById(UUID transactionId, boolean fetch) throws RODATransactionException {
    if (fetch) {
      return transactionLogRepository.findById(transactionId)
        .orElseThrow(() -> new RODATransactionException("Transaction not found for ID: " + transactionId));
    } else {
      TransactionLog transactionLog = new TransactionLog();
      transactionLog.setId(transactionId);
      return transactionLog;
    }
  }

  @Transactional
  public void changeStatus(UUID transactionId, TransactionLog.TransactionStatus status)
    throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionId, true);
    transactionLog.setStatus(status);
    transactionLogRepository.save(transactionLog);
  }

  @Transactional
  public void cleanUp(UUID transactionID) throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionID, true);
    if (null != transactionLog) {
      transactionLogRepository.delete(transactionLog);
    }
  }

  /*
   * transactionalModelOperationLogRepository
   */

  private TransactionalModelOperationLog getTransactionalModelOperationLogById(UUID operationId)
    throws RODATransactionException {
    return transactionalModelOperationLogRepository.findById(operationId)
      .orElseThrow(() -> new RODATransactionException("Model operation log not found for ID: " + operationId));
  }

  @Transactional
  public TransactionalModelOperationLog registerModelOperation(UUID transactionId, String liteObject,
    OperationType operation) throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionId, false);
    TransactionalModelOperationLog operationLog = transactionLog.addModelOperation(liteObject, operation);
    operationLog.setTransactionLog(transactionLog);
    return transactionalModelOperationLogRepository.save(operationLog);
  }

  @Transactional
  public void updateModelOperationState(UUID operationId, OperationState state) throws RODATransactionException {
    TransactionalModelOperationLog operationLog = getTransactionalModelOperationLogById(operationId);
    operationLog.setOperationState(state);
    transactionalModelOperationLogRepository.save(operationLog);
  }

  public List<TransactionalModelOperationLog> getModelOperations(UUID transactionId) throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionId, false);
    return transactionalModelOperationLogRepository.findByTransactionLogOrderByUpdatedAt(transactionLog);
  }

  public TransactionalModelOperationLog getAnyDeletedModelOperation(UUID transactionId, String liteObject)
    throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionId, false);
    List<TransactionalModelOperationLog> result = transactionalModelOperationLogRepository
      .findAnyByTransactionLogAndLiteObjectAndOperationType(transactionLog, OperationState.SUCCESS, liteObject,
        OperationType.DELETE, PageRequest.of(0, 1));
    return result.isEmpty() ? null : result.getFirst();
  }

  /*
   * transactionalStoragePathRepository
   */

  private TransactionalStoragePathOperationLog getTransactionalStoragePathOperationLogById(UUID operationId)
    throws RODATransactionException {
    return transactionalStoragePathRepository.findById(operationId)
      .orElseThrow(() -> new RODATransactionException("Storage path operation log not found for ID: " + operationId));
  }

  @Transactional
  public TransactionalStoragePathOperationLog registerStoragePathOperation(UUID transactionId, String storagePath,
    OperationType operation, String version) throws RODATransactionException {
    if (operation == OperationType.READ) {
      // TODO: add a configuration to allow logging the read operation for debugging
      // purposes
      return null;
    }
    TransactionLog transactionLog = getTransactionLogById(transactionId, false);
    TransactionalStoragePathOperationLog operationLog = transactionLog.addStoragePath(storagePath, operation, version);
    operationLog.setTransactionLog(transactionLog);
    return transactionalStoragePathRepository.save(operationLog);
  }

  @Transactional
  public void updateStoragePathOperationState(UUID operationId, OperationState state) throws RODATransactionException {
    TransactionalStoragePathOperationLog operationLog = getTransactionalStoragePathOperationLogById(operationId);
    operationLog.setOperationState(state);
    transactionalStoragePathRepository.save(operationLog);
  }

  public List<TransactionalStoragePathOperationLog> getStoragePathsOperations(UUID transactionId)
    throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionId, false);
    return transactionalStoragePathRepository.findByTransactionLogOrderByUpdatedAt(transactionLog,
      OperationState.SUCCESS);
  }

  public List<TransactionalStoragePathOperationLog> getStoragePathsOperations(UUID transactionId,
    OperationType operationType) throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionId, false);
    return transactionalStoragePathRepository.findByTransactionLogAndOperationType(transactionLog, operationType,
      OperationState.SUCCESS);
  }

  public TransactionalStoragePathOperationLog getAnyDeletedStoragePathOperation(UUID transactionId, String storagePath)
    throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionId, false);
    List<TransactionalStoragePathOperationLog> result = transactionalStoragePathRepository
      .findAnyByTransactionLogAndStoragePathAndOperationType(transactionLog, OperationState.SUCCESS, storagePath,
        OperationType.DELETE, PageRequest.of(0, 1));
    return result.isEmpty() ? null : result.getFirst();
  }

  public boolean hasModificationsUnderStoragePath(UUID transactionID, String storagePath)
    throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionID, false);
    List<TransactionalStoragePathOperationLog> results = transactionalStoragePathRepository
      .findModificationsUnderStoragePath(transactionLog, storagePath);
    return !results.isEmpty();
  }

  public List<TransactionalStoragePathOperationLog> listModificationsUnderStoragePath(UUID transactionID,
    String storagePath) throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionID, false);
    return transactionalStoragePathRepository.findModificationsUnderStoragePath(transactionLog, storagePath);
  }
}
