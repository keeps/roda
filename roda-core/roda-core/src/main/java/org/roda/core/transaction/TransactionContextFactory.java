/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.transaction;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.roda.core.RodaCoreFactory;
import org.roda.core.config.ConfigurationManager;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.entity.transaction.TransactionLog;
import org.roda.core.index.IndexService;
import org.roda.core.model.DefaultModelService;
import org.roda.core.model.DefaultTransactionalModelService;
import org.roda.core.model.ModelService;
import org.roda.core.model.TransactionalModelService;
import org.roda.core.storage.DefaultTransactionalStorageService;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.TransactionalStorageService;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.micrometer.core.annotation.Timed;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
@Timed
public class TransactionContextFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionContextFactory.class);
  /** Log index reads, inside a transaction, of objects written by that transaction. */
  public static final String LOG_OWN_WRITE_READS_PROPERTY = "core.transaction.index.log_own_write_reads";
  private final ConfigurationManager configurationManager = ConfigurationManager.getInstance();
  private final TransactionLogService transactionLogService;

  public TransactionContextFactory(TransactionLogService transactionLogService) {
    this.transactionLogService = transactionLogService;
  }

  public TransactionalContext create(TransactionLog transactionLog, ModelService mainModelService)
    throws RODATransactionException {
    TransactionalStorageService storageService = createTransactionalStorageService(mainModelService.getStorage(),
      transactionLog);
    // notifications of the staging model are recorded, not delivered: nothing
    // reaches the index (or any other observer) before the commit
    DeferredNotificationJournal journal = new DeferredNotificationJournal();
    TransactionalModelService modelService = createTransactionalModelService(mainModelService, storageService,
      transactionLog, journal);
    IndexService indexService = new TransactionIndexService(RodaCoreFactory.getIndexService(), transactionLog.getId(),
      journal, Boolean.parseBoolean(configurationManager.getConfigurationString(LOG_OWN_WRITE_READS_PROPERTY, "true")));

    return new TransactionalContext(transactionLog, storageService, modelService, indexService, journal);
  }

  public TransactionalStorageService createTransactionalStorageService(StorageService mainStorageService,
    TransactionLog transactionLog) throws RODATransactionException {

    Path stagingStoragePath = Paths.get(configurationManager.getRodaConfiguration()
      .getString(RodaConstants.CORE_STAGING_STORAGE_PATH, configurationManager.getStagingStoragePath().toString()));

    Path transactionalStoragePath = stagingStoragePath.resolve(transactionLog.getId().toString());

    String newStorageService = configurationManager.getRodaConfiguration()
      .getString(RodaConstants.CORE_STORAGE_NEW_SERVICE);
    if (RodaConstants.CORE_STORAGE_FOLDER_SCATTERING_SERVICE_CLASS.equals(newStorageService)) {
      try {
        Class<?> storageClass = Class.forName(newStorageService);
        Constructor<?> constructor = storageClass.getConstructor(Path.class, boolean.class, String.class,
          boolean.class);
        LOGGER.debug("Going to instantiate '{}' on '{}'", storageClass.getSimpleName(), transactionalStoragePath);

        StorageService staging = (StorageService) constructor.newInstance(transactionalStoragePath, false, null, false);
        return new DefaultTransactionalStorageService(mainStorageService, staging, transactionLog,
          transactionLogService);
      } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException
        | InvocationTargetException e) {
        LOGGER.warn("Error instantiating storage service defined on properties, falling back to a default service", e);
      }
    }

    RodaConstants.StorageType storageType = RodaConstants.StorageType
      .valueOf(configurationManager.getRodaConfiguration().getString(RodaConstants.CORE_STORAGE_TYPE,
        RodaConstants.DEFAULT_STORAGE_TYPE.toString()));

    if (storageType == RodaConstants.StorageType.FILESYSTEM) {
      LOGGER.debug("Instantiating FileSystem storage at '{}'", transactionalStoragePath);

      try {
        StorageService staging = new FileStorageService(transactionalStoragePath, false, null, false);
        return new DefaultTransactionalStorageService(mainStorageService, staging, transactionLog,
          transactionLogService);
      } catch (GenericException e) {
        throw new RODATransactionException("Error creating staging storage service", e);
      }
    } else {
      throw new RODATransactionException("Unsupported storage type: " + storageType.name());
    }
  }

  private TransactionalModelService createTransactionalModelService(ModelService mainModelService,
    StorageService storageService, TransactionLog transactionLog, DeferredNotificationJournal journal) {
    ModelService stagingModelService = new DefaultModelService(storageService, RodaCoreFactory.getEventsManager(),
      RodaCoreFactory.getNodeType(), RodaCoreFactory.getInstanceId());
    stagingModelService.addModelObserver(journal.recorder());
    ((DefaultTransactionalStorageService) storageService).setInitialized(true);

    return new DefaultTransactionalModelService(mainModelService, stagingModelService, transactionLog,
      transactionLogService);
  }

}
