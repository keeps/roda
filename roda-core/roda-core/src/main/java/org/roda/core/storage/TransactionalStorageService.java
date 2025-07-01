package org.roda.core.storage;

import org.roda.core.transaction.TransactionalService;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface TransactionalStorageService extends StorageService, TransactionalService {
    StorageService getStagingStorageService();
}
