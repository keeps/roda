package org.roda.core.transaction;

import java.nio.file.Path;
import java.nio.file.Paths;

import io.micrometer.core.annotation.Timed;
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
@Timed
public class TransactionContextFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionContextFactory.class);
  private final ConfigurationManager configurationManager = ConfigurationManager.getInstance();
  private final TransactionLogService transactionLogService;

  public TransactionContextFactory(TransactionLogService transactionLogService) {
    this.transactionLogService = transactionLogService;
  }

  public TransactionalContext create(TransactionLog transactionLog, ModelService mainModelService)
    throws RODATransactionException {
    TransactionalStorageService storageService = createTransactionalStorageService(mainModelService.getStorage(),
      transactionLog);
    TransactionalModelService modelService = createTransactionalModelService(mainModelService, storageService,
      transactionLog);
    IndexService indexService = createTransactionalIndexService(modelService);

    return new TransactionalContext(transactionLog, storageService, modelService, indexService);
  }

  public TransactionalStorageService createTransactionalStorageService(StorageService mainStorageService,
    TransactionLog transactionLog) throws RODATransactionException {

    Path stagingStoragePath = Paths.get(configurationManager.getRodaConfiguration()
      .getString(RodaConstants.CORE_STAGING_STORAGE_PATH, configurationManager.getStagingStoragePath().toString()));

    Path transactionalStoragePath = stagingStoragePath.resolve(transactionLog.getId().toString());

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
    StorageService storageService, TransactionLog transactionLog) {
    ModelService stagingModelService = new DefaultModelService(storageService, RodaCoreFactory.getEventsManager(),
      RodaCoreFactory.getNodeType(), RodaCoreFactory.getInstanceId());
    ((DefaultTransactionalStorageService) storageService).setInitialized(true);

    return new DefaultTransactionalModelService(mainModelService, stagingModelService, transactionLog,
      transactionLogService);
  }

  private IndexService createTransactionalIndexService(ModelService modelService) {
    return new IndexService(RodaCoreFactory.getIndexService().getSolrClient(), modelService,
      RodaCoreFactory.getMetrics(), configurationManager.getRodaConfiguration(), RodaCoreFactory.getNodeType());
  }
}
