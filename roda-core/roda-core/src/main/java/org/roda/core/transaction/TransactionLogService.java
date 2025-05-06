package org.roda.core.transaction;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.LiteOptionalWithCause;
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
  public TransactionLog createTransactionLog(String transactionId, List<LiteOptionalWithCause> objectsToBeProcessed) {
    TransactionLog transactionLog = new TransactionLog(transactionId);
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
    TransactionLog transactionLog = getTransactionLogById(transactionId);
    transactionLog.addModelOperation(liteObject, operation);
    transactionLogRepository.save(transactionLog);
  }

  public void cleanUp(String transactionID) throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionID);
    transactionLog.setStoragePathsOperations(null);
    transactionLog.setModelOperations(null);
    transactionLogRepository.save(transactionLog);
  }
}
