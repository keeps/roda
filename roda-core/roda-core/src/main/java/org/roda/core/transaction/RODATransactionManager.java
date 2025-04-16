package org.roda.core.transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.LockingException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.entity.transaction.TransactionLog;
import org.roda.core.entity.transaction.TransactionalStoragePath;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.TransactionalModelService;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.repository.transaction.TransactionLogRepository;
import org.roda.core.storage.TransactionalStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Service
public class RODATransactionManager {
  @Autowired
  private TransactionLogRepository transactionLogRepository;

  private Map<String, TransactionalStorageService> transactionalStorageServices = new HashMap<>();
  private Map<String, TransactionalModelService> transactionalModelServices = new HashMap<>();
  private Map<String, IndexService> transactionalIndexServices = new HashMap<>();

  public void beginTransaction(String id, List<LiteOptionalWithCause> objectsToBeProcessed) throws GenericException {
    TransactionLog transactionLog = new TransactionLog(id);

    objectsToBeProcessed.forEach((LiteOptionalWithCause object) -> {
      if (object.getLite().isPresent()) {
        LiteRODAObject liteRODAObject = object.getLite().get();
        transactionLog.addLiteObject(liteRODAObject.getInfo());
      }
    });

    transactionLogRepository.save(transactionLog);

    TransactionalStorageService transactionalStorageService = RodaCoreFactory
      .getTransactionalStorageService(transactionLog);
    transactionalStorageServices.put(id, transactionalStorageService);
    TransactionalModelService transactionalModelService = RodaCoreFactory
      .getTransactionalModelService(transactionalStorageService, transactionLog);
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

  public List<TransactionalStoragePath> getStoragePathsOperations(TransactionLog transaction) throws GenericException {
    Optional<TransactionLog> id = transactionLogRepository.findById(transaction.getId());
    if (id.isPresent()) {
      return id.get().getStoragePaths();
    } else {
      throw new GenericException("Transaction not found");
    }
  }

  public void endTransaction(String transactionID) {
    transactionLogRepository.findById(transactionID).ifPresent(transactionLog -> {
      transactionLog.setStatus(TransactionLog.TransactionStatus.COMMITTED);
      transactionLogRepository.save(transactionLog);

      TransactionalStorageService transactionalStorageService = transactionalStorageServices.get(transactionID);
      if (transactionalStorageService != null) {
        transactionalStorageService.commit();
        transactionalStorageServices.remove(transactionID);
      }

      TransactionalModelService transactionalModelService = transactionalModelServices.get(transactionID);
      if (transactionalStorageService != null) {
        transactionalModelService.commit();
        transactionalModelServices.remove(transactionID);
      }

      transactionalIndexServices.remove(transactionID);
    });
  }

  public void rollbackTransaction(String transactionID) {
    transactionLogRepository.findById(transactionID).ifPresent(transactionLog -> {
      transactionLog.setStatus(TransactionLog.TransactionStatus.ROLLED_BACK);
      transactionLogRepository.save(transactionLog);

      TransactionalStorageService transactionalStorageService = transactionalStorageServices.get(transactionID);

      if (transactionalStorageService != null) {
        transactionalStorageService.rollback();
        transactionalStorageServices.remove(transactionID);
      }

      transactionalModelServices.remove(transactionID);
      transactionalIndexServices.remove(transactionID);
    });
  }

  public void acquireLock(String transactionID, String lite) throws LockingException, GenericException {
    transactionLogRepository.findById(transactionID).ifPresent(transactionLog -> {
      if (!transactionLog.hasLiteObject(lite)) {
        transactionLog.addLiteObject(lite);
        transactionLog.setStatus(TransactionLog.TransactionStatus.PENDING);
        transactionLogRepository.save(transactionLog);
      }
    });

    PluginHelper.acquireObjectLock(lite, transactionID);
  }

  public void releaseLock(String transactionID, String lite) {
    transactionLogRepository.findById(transactionID).ifPresent(transactionLog -> {
      transactionLog.removeLiteObject(lite);
      transactionLogRepository.save(transactionLog);
    });

    PluginHelper.releaseObjectLock(lite, transactionID);
  }

  public void registerOperation(TransactionLog transaction, StoragePath storagePath,
    TransactionalStoragePath.OperationType operation) throws InterruptedException, GenericException {
    Optional<TransactionLog> id = transactionLogRepository.findById(transaction.getId());
    if (id.isPresent()) {
      transaction.addStoragePath(storagePath, operation);
      transactionLogRepository.save(transaction);
    } else {
      throw new GenericException("Transaction not found");
    }

  }

  public void removeOperation(TransactionLog transaction, TransactionalStoragePath storagePathLog)
    throws GenericException {
    Optional<TransactionLog> id = transactionLogRepository.findById(transaction.getId());
    if (id.isPresent()) {
      transaction.removeStoragePath(storagePathLog);
      transactionLogRepository.save(transaction);
    } else {
      throw new GenericException("Transaction not found");
    }
  }

  public void releaseLock(TransactionLog transaction) {
    Optional<TransactionLog> id = transactionLogRepository.findById(transaction.getId());
    if (id.isPresent()) {
      transaction.setStatus(TransactionLog.TransactionStatus.ROLLED_BACK);
      transaction.getStoragePaths().forEach(transaction::removeStoragePath);

      transaction.getLiteObjects().forEach(liteObject -> {
        transaction.removeLiteObject(liteObject);
        PluginHelper.releaseObjectLock(liteObject, transaction.getId());
      });
    }
  }
}
