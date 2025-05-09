package org.roda.core.transaction;

import java.util.List;

import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.entity.transaction.TransactionLog;
import org.roda.core.entity.transaction.TransactionalModelOperationLog;
import org.roda.core.entity.transaction.TransactionalStoragePathOperationLog;
import org.roda.core.repository.transaction.TransactionLogRepository;
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

  @Transactional
  public TransactionLog createTransactionLog(TransactionLog.TransactionRequestType requestType, String requestId) {
    TransactionLog transactionLog = new TransactionLog(requestType, requestId);
    transactionLogRepository.save(transactionLog);
    return transactionLog;
  }

  private TransactionLog getTransactionLogById(String transactionId) throws RODATransactionException {
    return transactionLogRepository.findById(transactionId)
      .orElseThrow(() -> new RODATransactionException("Transaction not found for ID: " + transactionId));
  }

  @Transactional
  public void changeStatus(String transactionId, TransactionLog.TransactionStatus status)
    throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionId);
    transactionLog.setStatus(status);
    transactionLogRepository.save(transactionLog);
  }

  @Transactional
  public void registerStoragePathOperation(String transactionId, StoragePath storagePath,
    TransactionalStoragePathOperationLog.OperationType operation) throws RODATransactionException {
    if (operation == TransactionalStoragePathOperationLog.OperationType.READ) {
      // TODO: add a configuration to allow logging the read operation for debugging purposes
      return;
    }
    TransactionLog transactionLog = getTransactionLogById(transactionId);
    transactionLog.addStoragePath(storagePath, operation);
    transactionLogRepository.save(transactionLog);
  }

  @Transactional
  public List<TransactionalStoragePathOperationLog> getStoragePathsOperations(String transactionId)
    throws RODATransactionException {
    return getTransactionLogById(transactionId).getStoragePathsOperations();
  }

  @Transactional
  public List<TransactionalModelOperationLog> getModelOperations(String transactionId) throws RODATransactionException {
    return getTransactionLogById(transactionId).getModelOperations();
  }

  @Transactional
  public void registerModelOperation(String transactionId, String liteObject,
    TransactionalModelOperationLog.OperationType operation) throws RODATransactionException {
    if(operation == TransactionalModelOperationLog.OperationType.READ) {
      //TODO: add a configuration to allow logging the read operation for debugging purposes
      return;
    }

    TransactionLog transactionLog = getTransactionLogById(transactionId);
    transactionLog.addModelOperation(liteObject, operation);
    transactionLogRepository.save(transactionLog);
  }

  @Transactional
  public void cleanUp(String transactionID) throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionID);
    transactionLog.getStoragePathsOperations().clear();
    transactionLog.getModelOperations().clear();
    transactionLogRepository.save(transactionLog);
  }

  @Transactional
  public boolean hasModificationsUnderStoragePath(String transactionID, String storagePath)
    throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionID);
    // TODO create a query for this in the repository
    return transactionLog.getStoragePathsOperations().stream()
      .anyMatch(op -> op.getStoragePath().startsWith(storagePath + "/")
        && op.getOperationType() != TransactionalStoragePathOperationLog.OperationType.READ);
  }

  @Transactional
  public List<TransactionalStoragePathOperationLog> listModificationsUnderStoragePath(String transactionID,
    String storagePath) throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionID);
    // TODO create a query for this in the repository
    return transactionLog.getStoragePathsOperations().stream()
      .filter(op -> op.getStoragePath().startsWith(storagePath + "/")
        && op.getOperationType() != TransactionalStoragePathOperationLog.OperationType.READ)
      .toList();
  }
}
