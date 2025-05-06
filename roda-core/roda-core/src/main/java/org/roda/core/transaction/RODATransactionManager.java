package org.roda.core.transaction;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.entity.transaction.TransactionLog;
import org.roda.core.plugins.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Service
public class RODATransactionManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(RODATransactionManager.class);
  private final TransactionLogService transactionLogService;
  private final TransactionContextFactory transactionContextFactory;
  private final Map<String, TransactionContext> transactionsContext = new ConcurrentHashMap<>();

  public RODATransactionManager(TransactionLogService transactionLogService,
    TransactionContextFactory transactionContextFactory) {
    this.transactionLogService = transactionLogService;
    this.transactionContextFactory = transactionContextFactory;
  }

  public TransactionLogService getTransactionLogService() {
    return transactionLogService;
  }

  public TransactionContext beginTransaction(String transactionID, List<LiteOptionalWithCause> objectsToBeProcessed)
    throws RODATransactionException {

    TransactionLog transactionLog = transactionLogService.createTransactionLog(transactionID, objectsToBeProcessed);
    TransactionContext context = transactionContextFactory.create(transactionLog);

    transactionsContext.put(transactionID, context);
    return context;
  }

  public void endTransaction(String transactionID) throws RODATransactionException {

    TransactionContext context = transactionsContext.get(transactionID);
    if (context == null) {
      throw new RODATransactionException("No transaction context found for ID: " + transactionID);
    }

    if (context.transactionalStorageService() != null) {
      context.transactionalStorageService().commit();
    }

    if (context.transactionalModelService() != null) {
      context.transactionalModelService().commit();
    }

    transactionsContext.remove(transactionID);
    transactionLogService.cleanUp(transactionID);
    transactionLogService.changeStatus(transactionID, TransactionLog.TransactionStatus.COMMITTED);
  }

  public void runPluginInTransaction(String transactionId, List<LiteOptionalWithCause> objectsToBeProcessed,
    Plugin<IsRODAObject> plugin) throws RODATransactionException {
    try {
      TransactionContext context = beginTransaction(transactionId, objectsToBeProcessed);
      plugin.execute(context.indexService(), context.transactionalModelService(), context.transactionalStorageService(),
        objectsToBeProcessed);
      endTransaction(transactionId);
    } catch (Exception e) {
      try {
        rollbackTransaction(transactionId);
      } catch (RODATransactionException ex) {
        LOGGER.error("Error during rollback", ex);
      }
      throw new RODATransactionException("Error during plugin execution", e);
    }
  }

  public void rollbackTransaction(String transactionID) throws RODATransactionException {
    TransactionContext context = transactionsContext.get(transactionID);
    if (context == null) {
      throw new RODATransactionException("No transaction context found for ID: " + transactionID);
    }

    if (context.transactionalStorageService() != null) {
      context.transactionalStorageService().rollback();
    }

    if (context.transactionalModelService() != null) {
      context.transactionalModelService().rollback();
    }

    transactionsContext.remove(transactionID);
    transactionLogService.cleanUp(transactionID);
    transactionLogService.changeStatus(transactionID, TransactionLog.TransactionStatus.ROLLED_BACK);
  }
}
