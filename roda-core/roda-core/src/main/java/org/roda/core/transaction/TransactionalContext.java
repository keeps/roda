package org.roda.core.transaction;

import org.roda.core.index.IndexService;
import org.roda.core.model.TransactionalModelService;
import org.roda.core.storage.TransactionalStorageService;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public record TransactionalContext(org.roda.core.entity.transaction.TransactionLog transactionLog,
                                   TransactionalStorageService transactionalStorageService,
                                   TransactionalModelService transactionalModelService, IndexService indexService) {
}
