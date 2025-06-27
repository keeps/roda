package org.roda.core.transaction;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.entity.transaction.TransactionLog;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.storage.DefaultTransactionalStorageService;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.TransactionalStorageService;
import org.roda.core.util.IdUtils;
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
  private final Map<UUID, TransactionalContext> transactionsContext = new ConcurrentHashMap<>();

  private ModelService mainModelService;
  private RodaConstants.NodeType nodeType;

  public RODATransactionManager(TransactionLogService transactionLogService,
    TransactionContextFactory transactionContextFactory) {
    this.transactionLogService = transactionLogService;
    this.transactionContextFactory = transactionContextFactory;
  }

  public void setMainModelService(ModelService mainModelService) {
    this.mainModelService = mainModelService;
  }

  public void setNodeType(RodaConstants.NodeType nodeType) {
    this.nodeType = nodeType;
  }

  public TransactionalContext beginTransaction() throws RODATransactionException {
    return this.beginTransaction(TransactionLog.TransactionRequestType.NON_DEFINED);
  }

  public TransactionalContext beginTransaction(TransactionLog.TransactionRequestType requestType)
    throws RODATransactionException {
    return this.beginTransaction(requestType, UUID.randomUUID());
  }

  public TransactionalContext beginTransaction(TransactionLog.TransactionRequestType requestType, UUID requestId)
    throws RODATransactionException {

    TransactionLog transactionLog = transactionLogService.createTransactionLog(requestType, requestId);
    TransactionalContext context = transactionContextFactory.create(transactionLog, mainModelService);

    transactionsContext.put(transactionLog.getId(), context);
    return context;
  }

  public TransactionalContext beginTestTransaction(StorageService mainStorage) throws RODATransactionException {
    TransactionLog transactionLog = transactionLogService
      .createTransactionLog(TransactionLog.TransactionRequestType.NON_DEFINED, UUID.randomUUID());
    TransactionalStorageService transactionalStorageService = transactionContextFactory
      .createTransactionalStorageService(mainStorage, transactionLog);
    TransactionalContext context = new TransactionalContext(transactionLog, transactionalStorageService, null, null);
    ((DefaultTransactionalStorageService) transactionalStorageService).setInitialized(true);

    transactionsContext.put(transactionLog.getId(), context);
    return context;
  }

  public void endTransaction(UUID transactionID) throws RODATransactionException {

    transactionLogService.changeStatus(transactionID, TransactionLog.TransactionStatus.COMMITTING);
    TransactionalContext context = transactionsContext.get(transactionID);
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
    // transactionLogService.cleanUp(transactionID);
    transactionLogService.changeStatus(transactionID, TransactionLog.TransactionStatus.COMMITTED);
  }

  public void runPluginInTransaction(Plugin<IsRODAObject> plugin, List<LiteOptionalWithCause> objectsToBeProcessed)
    throws RODATransactionException {
    String requestUUID = plugin.getParameterValues().getOrDefault(RodaConstants.PLUGIN_PARAMS_LOCK_REQUEST_UUID,
      IdUtils.createUUID());
    plugin.getParameterValues().put(RodaConstants.PLUGIN_PARAMS_LOCK_REQUEST_UUID, requestUUID);

    TransactionalContext context = beginTransaction(TransactionLog.TransactionRequestType.JOB,
      UUID.fromString(requestUUID));
    UUID transactionId = context.transactionLog().getId();
    try {
      Report report = plugin.execute(context.indexService(), context.transactionalModelService(), objectsToBeProcessed);
      if (report.getPluginState().equals(PluginState.FAILURE)) {
        rollbackTransaction(transactionId);
      } else {
        endTransaction(transactionId);
      }
    } catch (Exception e) {
      try {
        rollbackTransaction(transactionId);
      } catch (RODATransactionException ex) {
        LOGGER.error("Error during rollback", ex);
      }
      throw new RODATransactionException("Error during plugin execution", e);
    }
  }

  public void rollbackTransaction(UUID transactionID) throws RODATransactionException {
    TransactionalContext context = transactionsContext.get(transactionID);
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
    // transactionLogService.cleanUp(transactionID);
    transactionLogService.changeStatus(transactionID, TransactionLog.TransactionStatus.ROLLED_BACK);
  }

  public void cleanUnfinishedTransactions() {
    for (TransactionLog unfinishedTransaction : transactionLogService.getUnfinishedTransactions()) {
      try {
        if (transactionsContext.get(unfinishedTransaction.getId()) == null) {
          TransactionalContext context = transactionContextFactory.create(unfinishedTransaction, mainModelService);
          transactionsContext.put(unfinishedTransaction.getId(), context);
        }

        rollbackTransaction(unfinishedTransaction.getId());
      } catch (RODATransactionException e) {
        LOGGER.error("Error during cleanup of unfinished transaction: {}", unfinishedTransaction.getId(), e);
      }
    }
  }
}
