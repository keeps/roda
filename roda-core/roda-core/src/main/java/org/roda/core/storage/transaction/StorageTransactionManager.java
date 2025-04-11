package org.roda.core.storage.transaction;

import java.util.HashMap;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.transaction.LockService;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.model.transaction.Transaction;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class StorageTransactionManager {
  private Map<String, TransactionalStorageService> transactionalStorageServices;
  private LockService lockService;

  public StorageTransactionManager() {
    this.transactionalStorageServices = new HashMap<>();
    this.lockService = new LockService();
  }

  public TransactionalStorageService beginTransaction(String transactionId) throws GenericException {
    Transaction transaction = new Transaction(transactionId);
    TransactionalStorageService transactionalStorageService = RodaCoreFactory.getTransactionalStorageService(transaction);
    transactionalStorageServices.put(transactionId, transactionalStorageService);
    return transactionalStorageService;
  }

  public TransactionalStorageService getTransactionalStorageService(String jobId) throws GenericException {
    TransactionalStorageService transactionalStorageService = transactionalStorageServices.get(jobId);
    if (transactionalStorageService == null) {
      throw new GenericException("No transactional storage service found for job ID: " + jobId);
    }
    return transactionalStorageService;
  }

  public LockService getInMemoryLockService() {
    return lockService;
  }

  public void commitTransaction(String jobId) throws GenericException {
    TransactionalStorageService transactionalStorageService = getTransactionalStorageService(jobId);
    transactionalStorageService.commit();
    transactionalStorageServices.remove(jobId);
  }
}
