package org.roda.core.transaction;

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

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class TransactionContextFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionContextFactory.class);
  private final ConfigurationManager configurationManager = ConfigurationManager.getInstance();
  private final TransactionLogService transactionLogService;

  public TransactionContextFactory(TransactionLogService transactionLogService) {
    this.transactionLogService = transactionLogService;
  }

  public TransactionalContext create(TransactionLog transactionLog) throws RODATransactionException {
    TransactionalStorageService storageService = createTransactionalStorageService(transactionLog);
    TransactionalModelService modelService = createTransactionalModelService(storageService, transactionLog);
    IndexService indexService = createTransactionalIndexService(modelService);

    return new TransactionalContext(transactionLog, storageService, modelService, indexService);
  }

  private TransactionalStorageService createTransactionalStorageService(TransactionLog transactionLog)
    throws RODATransactionException {

    Path stagingStoragePath = Paths.get(configurationManager.getRodaConfiguration()
      .getString(RodaConstants.CORE_STAGING_STORAGE_PATH, configurationManager.getStagingStoragePath().toString()));

    Path transactionalStoragePath = stagingStoragePath.resolve(transactionLog.getId());

    RodaConstants.StorageType storageType = RodaConstants.StorageType
      .valueOf(configurationManager.getRodaConfiguration().getString(RodaConstants.CORE_STORAGE_TYPE,
        RodaConstants.DEFAULT_STORAGE_TYPE.toString()));

    if (storageType == RodaConstants.StorageType.FILESYSTEM) {
      LOGGER.debug("Instantiating FileSystem storage at '{}'", transactionalStoragePath);

      try {
        StorageService staging = new FileStorageService(transactionalStoragePath, false, null, false);
        return new DefaultTransactionalStorageService(RodaCoreFactory.getStorageService(), staging, transactionLog,
          transactionLogService);
      } catch (GenericException e) {
        throw new RODATransactionException("Error creating staging storage service", e);
      }
    } else {
      throw new RODATransactionException("Unsupported storage type: " + storageType.name());
    }
  }

  private TransactionalModelService createTransactionalModelService(StorageService storageService,
    TransactionLog transactionLog) {
    ModelService stagingModelService = new DefaultModelService(storageService, RodaCoreFactory.getEventsManager(),
      RodaCoreFactory.getNodeType(), RodaCoreFactory.getInstanceId());

    return new DefaultTransactionalModelService(RodaCoreFactory.getModelService(), stagingModelService, transactionLog,
      transactionLogService);
  }

  private IndexService createTransactionalIndexService(ModelService modelService) {
    return new IndexService(RodaCoreFactory.getIndexService().getSolrClient(), modelService,
      RodaCoreFactory.getMetrics(), configurationManager.getRodaConfiguration(), RodaCoreFactory.getNodeType());
  }
}
