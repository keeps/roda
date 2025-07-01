package org.roda.core.transaction;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.roda.core.entity.transaction.OperationState;
import org.roda.core.entity.transaction.OperationType;
import org.roda.core.entity.transaction.TransactionLog;
import org.roda.core.entity.transaction.TransactionalModelOperationLog;
import org.roda.core.entity.transaction.TransactionalStoragePathOperationLog;
import org.roda.core.repository.transaction.TransactionLogRepository;
import org.roda.core.repository.transaction.TransactionalModelOperationLogRepository;
import org.roda.core.repository.transaction.TransactionalStoragePathRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Service
public class TransactionLogService {
  @Autowired
  private TransactionLogRepository transactionLogRepository;
  @Autowired
  private TransactionalModelOperationLogRepository transactionalModelOperationLogRepository;
  @Autowired
  private TransactionalStoragePathRepository transactionalStoragePathRepository;

  @Transactional
  public List<TransactionLog> getUnfinishedTransactions() {
    return transactionLogRepository.findByStatusOrderByCreatedAt(TransactionLog.TransactionStatus.PENDING);
  }

  @Transactional
  public List<TransactionLog> getCommittedTransactions() {
    return transactionLogRepository.findByStatusOrderByCreatedAt(TransactionLog.TransactionStatus.COMMITTED);
  }

  @Transactional
  public TransactionLog createTransactionLog(TransactionLog.TransactionRequestType requestType, UUID requestId) {
    TransactionLog transactionLog = new TransactionLog(requestType, requestId);
    transactionLogRepository.save(transactionLog);
    return transactionLog;
  }

  @Transactional
  public TransactionLog getTransactionLog(UUID transactionId) throws RODATransactionException {
    if (transactionId == null) {
      throw new RODATransactionException("Transaction ID cannot be null");
    }
    return getTransactionLogById(transactionId);
  }

  private TransactionLog getTransactionLogById(UUID transactionId) throws RODATransactionException {
    return transactionLogRepository.findById(transactionId)
      .orElseThrow(() -> new RODATransactionException("Transaction not found for ID: " + transactionId));
  }

  private TransactionalModelOperationLog getTransactionalModelOperationLogById(UUID operationId)
    throws RODATransactionException {
    return transactionalModelOperationLogRepository.findById(operationId)
      .orElseThrow(() -> new RODATransactionException("Model operation log not found for ID: " + operationId));
  }

  private TransactionalStoragePathOperationLog getTransactionalStoragePathOperationLogById(UUID operationId)
    throws RODATransactionException {
    return transactionalStoragePathRepository.findById(operationId)
      .orElseThrow(() -> new RODATransactionException("Storage path operation log not found for ID: " + operationId));
  }

  @Transactional
  public void changeStatus(UUID transactionId, TransactionLog.TransactionStatus status)
    throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionId);
    transactionLog.setStatus(status);
    transactionLogRepository.save(transactionLog);
  }

  @Transactional
  public TransactionalStoragePathOperationLog registerStoragePathOperation(UUID transactionId, String storagePath,
    OperationType operation, String version) throws RODATransactionException {
    if (operation == OperationType.READ) {
      // TODO: add a configuration to allow logging the read operation for debugging
      // purposes
      return null;
    }
    TransactionLog transactionLog = getTransactionLogById(transactionId);
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

  @Transactional
  public List<TransactionalStoragePathOperationLog> getStoragePathsOperations(UUID transactionId)
    throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionId);
    return transactionalStoragePathRepository.findByTransactionLogOrderByUpdatedAt(transactionLog,
      OperationState.SUCCESS);
  }

  @Transactional
  public List<TransactionalStoragePathOperationLog> getStoragePathsOperations(UUID transactionId,
    OperationType operationType) throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionId);
    return transactionalStoragePathRepository.findByTransactionLogAndOperationType(transactionLog, operationType,
      OperationState.SUCCESS);
  }

  @Transactional
  public Optional<TransactionalStoragePathOperationLog> getLastStoragePathOperation(UUID transactionId,
    String storagePath) throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionId);
    return transactionalStoragePathRepository
      .findByTransactionLogAndStoragePath(transactionLog, OperationState.SUCCESS, storagePath).stream()
      .filter(operationLog -> isMutatingOperation(operationLog.getOperationType()))
      .max(Comparator.comparing(TransactionalStoragePathOperationLog::getUpdatedAt));
  }

  private boolean isMutatingOperation(OperationType operationType) {
    return operationType == OperationType.CREATE || operationType == OperationType.UPDATE
      || operationType == OperationType.DELETE;
  }

  @Transactional
  public List<TransactionalModelOperationLog> getModelOperations(UUID transactionId) throws RODATransactionException {
    return getTransactionLogById(transactionId).getModelOperations();
  }

  @Transactional
  public TransactionalModelOperationLog registerModelOperation(UUID transactionId, String liteObject,
    OperationType operation) throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionId);
    TransactionalModelOperationLog operationLog = transactionLog.addModelOperation(liteObject, operation);
    operationLog.setTransactionLog(getTransactionLogById(transactionId));
    return transactionalModelOperationLogRepository.save(operationLog);
  }

  @Transactional
  public void updateModelOperationState(UUID operationId, OperationState state) throws RODATransactionException {
    TransactionalModelOperationLog operationLog = getTransactionalModelOperationLogById(operationId);
    operationLog.setOperationState(state);
    transactionalModelOperationLogRepository.save(operationLog);
  }

  @Transactional
  public void cleanUp(UUID transactionID) throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionID);
    if (null != transactionLog) {
      transactionLogRepository.delete(transactionLog);
    }
  }

  @Transactional
  public boolean hasModificationsUnderStoragePath(UUID transactionID, String storagePath)
    throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionID);
    // TODO create a query for this in the repository
    return transactionLog.getStoragePathsOperations().stream()
      .anyMatch(op -> op.getStoragePath().startsWith(storagePath + "/") && op.getOperationType() != OperationType.READ);
  }

  @Transactional
  public List<TransactionalStoragePathOperationLog> listModificationsUnderStoragePath(UUID transactionID,
    String storagePath) throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionID);
    // TODO create a query for this in the repository
    return transactionLog.getStoragePathsOperations().stream()
      .filter(op -> op.getStoragePath().startsWith(storagePath + "/") && op.getOperationType() != OperationType.READ)
      .toList();
  }
}
