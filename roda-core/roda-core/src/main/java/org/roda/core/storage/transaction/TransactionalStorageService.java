package org.roda.core.storage.transaction;

import org.roda.core.storage.StorageService;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface TransactionalStorageService extends StorageService {
  void commit();

  void rollback();
}
