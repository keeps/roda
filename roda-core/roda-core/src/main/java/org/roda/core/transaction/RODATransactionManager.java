/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.transaction;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.roda.core.config.ConfigurationManager;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.entity.transaction.TransactionLog;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.storage.DefaultTransactionalStorageService;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.TransactionalStorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.micrometer.core.annotation.Timed;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Service
@Timed
public class RODATransactionManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(RODATransactionManager.class);
  private final TransactionLogService transactionLogService;
  private final TransactionContextFactory transactionContextFactory;
  private final Map<UUID, TransactionalContext> transactionsContext = new ConcurrentHashMap<>();

  private ModelService mainModelService;
  private boolean initialized = false;

  public RODATransactionManager(TransactionLogService transactionLogService,
    TransactionContextFactory transactionContextFactory) {
    this.transactionLogService = transactionLogService;
    this.transactionContextFactory = transactionContextFactory;
  }

  public void setMainModelService(ModelService mainModelService) {
    this.mainModelService = mainModelService;
  }

  public boolean isInitialized() {
    return initialized;
  }

  public void setInitialized(boolean initialized) {
    this.initialized = initialized;
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

  public void runPluginInTransaction(Plugin<IsRODAObject> plugin, List<LiteOptionalWithCause> objectsToBeProcessed)
    throws PluginException {
    String requestUUID = plugin.getParameterValues().getOrDefault(RodaConstants.PLUGIN_PARAMS_LOCK_REQUEST_UUID,
      IdUtils.createUUID());
    plugin.getParameterValues().put(RodaConstants.PLUGIN_PARAMS_LOCK_REQUEST_UUID, requestUUID);

    TransactionalContext context;
    try {
      context = beginTransaction(TransactionLog.TransactionRequestType.JOB, UUID.fromString(requestUUID));
    } catch (RODATransactionException e) {
      throw new PluginException("Failed to begin transaction for plugin execution", e);
    }

    UUID transactionId = context.transactionLog().getId();
    LOGGER.debug("[transactionId:{}] Running the plugin {} in a transaction", transactionId, plugin.getName());

    Date initDate = new Date();
    List<Report> reports;
    try {
      plugin.execute(context.indexService(), context.transactionalModelService(), objectsToBeProcessed);
      reports = RODATransactionManagerUtils.getReportsForTransaction(plugin, transactionId, mainModelService);
    } catch (PluginException e) {
      LOGGER.error("[transactionId:{}] Plugin execution failed: {}", transactionId, e.getMessage(), e);
      rollbackTransaction(transactionId);
      throw e;
    } catch (RODATransactionException e) {
      LOGGER.error("[transactionId:{}] Failed to retrieve transaction reports, rolling back transaction. Error: {}",
        transactionId, e.getMessage(), e);
      rollbackTransaction(transactionId);
      throw new PluginException("Failed to retrieve transaction reports, transaction was rolled back", e);
    } finally {
      // remove locks if any
      PluginHelper.releaseObjectLock(plugin);
    }

    // Check if any of the reports indicate that the transaction should be rolled
    // back
    if (RODATransactionManagerUtils.shouldRollback(plugin, RODATransactionManagerUtils.getFailedReports(reports))) {
      rollbackTransaction(transactionId);
      processPluginExecutionResult(transactionId, initDate, reports, false);
    } else {
      // If everything is fine, commit the transaction
      try {
        endTransaction(transactionId);
        processPluginExecutionResult(transactionId, initDate, reports, true);
      } catch (RODATransactionException e) {
        // If commit fails, we should attempt to rollback and log the error
        rollbackTransaction(transactionId);
        processPluginExecutionResult(transactionId, initDate, reports, false);
        throw new PluginException("Failed to commit transaction for plugin execution, transaction was rolled back", e);
      }
    }
  }

  private void processPluginExecutionResult(UUID transactionId, Date initDate, List<Report> relatedReports,
    boolean success) {
    try {
      if (success) {
        RODATransactionManagerUtils.createTransactionSuccessReports(relatedReports, transactionId, initDate,
          mainModelService);
      } else {
        List<Report> failedReports = RODATransactionManagerUtils.getFailedReports(relatedReports);
        List<Report> nonFailedReports = RODATransactionManagerUtils.getNonFailedReports(relatedReports);

        RODATransactionManagerUtils.createTransactionFailureReports(failedReports, nonFailedReports, transactionId,
          initDate, mainModelService);
      }
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException e) {
      LOGGER.error("Critical: Failed to generate reports for transaction {}", transactionId, e);
    }
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
    transactionLogService.changeStatus(transactionID, TransactionLog.TransactionStatus.COMMITTED);
  }

  public void rollbackTransaction(UUID transactionID) {
    TransactionalContext context = transactionsContext.get(transactionID);
    if (context == null) {
      LOGGER.error("No transaction context found for ID: {}", transactionID);
    } else {
      try {
        transactionLogService.changeStatus(transactionID, TransactionLog.TransactionStatus.ROLLING_BACK);
        if (context.transactionalStorageService() != null) {
          context.transactionalStorageService().rollback();
        }

        if (context.transactionalModelService() != null) {
          context.transactionalModelService().rollback();
        }

        transactionsContext.remove(transactionID);
        transactionLogService.changeStatus(transactionID, TransactionLog.TransactionStatus.ROLLED_BACK);
      } catch (Exception e) {
        LOGGER.error("Error during rollback of transaction: {}", transactionID, e);
        try {
          transactionLogService.changeStatus(transactionID, TransactionLog.TransactionStatus.ROLL_BACK_FAILED);
        } catch (RODATransactionException ex) {
          LOGGER.error("Error updating transaction log status to ROLL_BACK_FAILED for transaction: {}", transactionID,
            ex);
        }
      }
    }
  }

  public void cleanUnfinishedTransactions() {
    for (TransactionLog unfinishedTransaction : transactionLogService.getUnfinishedTransactions()) {
      try {
        TransactionalContext context = getContext(unfinishedTransaction);
        transactionsContext.put(unfinishedTransaction.getId(), context);
        rollbackTransaction(unfinishedTransaction.getId());
      } catch (RODATransactionException e) {
        LOGGER.error("Error during cleanup of unfinished transaction: {}", unfinishedTransaction.getId(), e);
      }
    }
  }

  public void cleanCommittedTransactions(UUID transactionId)
    throws RODATransactionException, NotFoundException, GenericException {

    TransactionLog transactionLog = transactionLogService.getTransactionLog(transactionId);

    // Remove the transactional history storage path if it exists
    TransactionalContext context = getContext(transactionLog);

    TransactionalStorageService storageService = context.transactionalStorageService();
    StorageService stagingStorageService = storageService.getStagingStorageService();
    if (stagingStorageService instanceof FileStorageService fileStorageService) {
      Path historyPath = fileStorageService.getHistoryPath();
      if (FSUtils.exists(historyPath)) {
        FSUtils.deletePath(historyPath);
      }
    }

    // Remove the transactional storage path if it exists
    Path transactionPath = ConfigurationManager.getInstance().getStagingStoragePath().resolve(transactionId.toString());
    if (FSUtils.exists(transactionPath)) {
      FSUtils.deletePath(transactionPath);
    }

    // Clean up the transaction log
    transactionLogService.cleanUp(transactionId);
  }

  private TransactionalContext getContext(TransactionLog transactionLog) throws RODATransactionException {
    TransactionalContext context = transactionsContext.get(transactionLog.getId());
    if (context == null) {
      context = transactionContextFactory.create(transactionLog, mainModelService);
    }

    return context;
  }

  /**
   * Transaction methods for unit testing purposes.
   */

  public TransactionalContext beginTransaction() throws RODATransactionException {
    return this.beginTransaction(TransactionLog.TransactionRequestType.NON_DEFINED);
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

  public void commitTestTransactionWithoutRemoving(UUID transactionID) throws RODATransactionException {
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

    transactionLogService.changeStatus(transactionID, TransactionLog.TransactionStatus.COMMITTED);
  }
}
