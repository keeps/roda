package org.roda.core.storage.transactional;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class StorageTransactionManager {
  TransactionalStorageService transactionalStorageService;

  public StorageTransactionManager(TransactionalStorageService storage) {
    this.transactionalStorageService = storage;
  }

  public TransactionalStorageService beginTransaction() {
    return transactionalStorageService;
  }
}
