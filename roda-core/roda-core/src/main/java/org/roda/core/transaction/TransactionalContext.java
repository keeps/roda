/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.transaction;

import org.roda.core.entity.transaction.TransactionLog;
import org.roda.core.index.IndexService;
import org.roda.core.model.TransactionalModelService;
import org.roda.core.storage.TransactionalStorageService;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public record TransactionalContext(TransactionLog transactionLog,
  TransactionalStorageService transactionalStorageService, TransactionalModelService transactionalModelService,
  IndexService indexService) {
}
