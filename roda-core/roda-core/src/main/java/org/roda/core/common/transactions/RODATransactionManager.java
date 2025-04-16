package org.roda.core.common.transactions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.transaction.TransactionLog;
import org.roda.core.repository.transaction.TransactionLogRepository;
import org.roda.core.storage.transaction.TransactionalStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.stereotype.Service;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Service
public class RODATransactionManager {
  @Autowired
  private LockRegistry lockRegistry;

  @Autowired
  private TransactionLogRepository transactionLogRepository;

  private Map<String, TransactionalStorageService> transactionalStorageServices = new HashMap<>();
  private Map<String, ModelService> transactionalModelServices = new HashMap<>();
  private Map<String, IndexService> transactionalIndexServices = new HashMap<>();

  public void beginTransaction(String id, List<LiteOptionalWithCause> objectsToBeProcessed) throws GenericException {
    TransactionLog transactionLog = new TransactionLog(id);

    objectsToBeProcessed.forEach((LiteOptionalWithCause object) -> {
      if(object.getLite().isPresent()){
        LiteRODAObject liteRODAObject = object.getLite().get();
        transactionLog.addLiteObject(liteRODAObject.getInfo());
      }
    });

    transactionLogRepository.save(transactionLog);

    TransactionalStorageService transactionalStorageService = RodaCoreFactory
      .getTransactionalStorageService(transactionLog);
    transactionalStorageServices.put(id, transactionalStorageService);
    ModelService transactionalModelService = RodaCoreFactory.getTransactionalModelService(transactionalStorageService, transactionLog);
    transactionalModelServices.put(id, transactionalModelService);
    IndexService transactionalIndexService = RodaCoreFactory.getTransactionalIndexService(transactionalModelService);
    transactionalIndexServices.put(id, transactionalIndexService);
  }

  public TransactionalStorageService getTransactionalStorageService(String id) throws GenericException {
    TransactionalStorageService transactionalStorageService = transactionalStorageServices.get(id);
    if (transactionalStorageService == null) {
      throw new GenericException("No transactional storage service found for job ID: " + id);
    }
    return transactionalStorageService;
  }

  public ModelService getTransactionalModelService(String id) throws GenericException {
    ModelService transactionalModelService = transactionalModelServices.get(id);
    if (transactionalModelService == null) {
      throw new GenericException("No transactional model service found for job ID: " + id);
    }
    return transactionalModelService;
  }

  public IndexService getTransactionalIndexService(String id) throws GenericException {
    IndexService transactionalIndexService = transactionalIndexServices.get(id);
    if (transactionalIndexService == null) {
      throw new GenericException("No transactional index service found for job ID: " + id);
    }
    return transactionalIndexService;
  }

  public List<String> getStoragePaths(TransactionLog transaction) throws GenericException {
    Optional<TransactionLog> id = transactionLogRepository.findById(transaction.getId());
    if (id.isPresent()) {
      return id.get().getStoragePaths();
    } else {
      throw new GenericException("Transaction not found");
    }
  }

  public void endTransaction(String id) {
    TransactionalStorageService transactionalStorageService = transactionalStorageServices.get(id);
    if (transactionalStorageService != null) {
      transactionalStorageService.commit();
      transactionalStorageServices.remove(id);
    }

    transactionalModelServices.remove(id);
    transactionalIndexServices.remove(id);
  }

  public void rollbackTransaction(String id) {
    TransactionalStorageService transactionalStorageService = transactionalStorageServices.get(id);
    if (transactionalStorageService != null) {
      transactionalStorageService.rollback();
      transactionalStorageServices.remove(id);
    }

    transactionalModelServices.remove(id);
    transactionalIndexServices.remove(id);
  }

  public Boolean isTransactional(String id) {
    return transactionalStorageServices.containsKey(id);
  }

  public void acquireLock(TransactionLog transaction, StoragePath storagePath)
    throws InterruptedException, GenericException {
    Optional<TransactionLog> id = transactionLogRepository.findById(transaction.getId());
    if (id.isPresent() && !id.get().hasStoragePath(storagePath.toString())) {
//      Lock lock = lockRegistry.obtain(storagePath.toString());
//      if (lock.tryLock(10, TimeUnit.SECONDS)) {
        transaction.addStoragePath(storagePath);
        transaction.setStatus(TransactionLog.TransactionStatus.PENDING);
        transactionLogRepository.save(transaction);
//      } else {
//        throw new GenericException("Unable to acquire lock for transaction: " + transaction.getId());
//      }
    } else {
      throw new GenericException("Transaction not found");
    }

  }

  public void releaseLock(TransactionLog transaction, StoragePath storagePath) throws GenericException {
    Optional<TransactionLog> id = transactionLogRepository.findById(transaction.getId());
    if (id.isPresent()) {
      transaction.removeStoragePath(storagePath.toString());
      transaction.setStatus(TransactionLog.TransactionStatus.COMMITTED);
      transactionLogRepository.save(transaction);
    } else {
      throw new GenericException("Transaction not found");
    }
//    Lock lock = lockRegistry.obtain(storagePath.toString());
//    lock.unlock();
  }

  public void releaseLock(TransactionLog transaction) {
    Optional<TransactionLog> id = transactionLogRepository.findById(transaction.getId());
    if (id.isPresent()) {
      transaction.setStatus(TransactionLog.TransactionStatus.ROLLED_BACK);

      transaction.getStoragePaths().forEach(storagePath -> {
        transaction.removeStoragePath(storagePath);
//        Lock lock = lockRegistry.obtain(storagePath);
//        lock.unlock();
      });
    }
  }
}
