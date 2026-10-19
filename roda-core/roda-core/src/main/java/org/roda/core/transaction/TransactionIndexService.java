/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.transaction;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.index.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Index service handed to plugins running inside a RODA transaction. It reads
 * the shared index (committed data only, since notifications of the
 * transaction are deferred until commit) and logs reads of AIPs,
 * representations or files written by the same transaction: such reads cannot
 * see the transaction's own changes and should use the model or the catalog
 * instead.
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class TransactionIndexService extends IndexService {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionIndexService.class);

  private final UUID transactionId;
  private final DeferredNotificationJournal journal;
  private final boolean logOwnWriteReads;

  public TransactionIndexService(IndexService mainIndex, UUID transactionId, DeferredNotificationJournal journal,
    boolean logOwnWriteReads) {
    super(mainIndex);
    this.transactionId = transactionId;
    this.journal = journal;
    this.logOwnWriteReads = logOwnWriteReads;
  }

  @Override
  public <T extends IsIndexed> T retrieve(Class<T> returnClass, String id, List<String> fieldsToReturn,
    boolean appendChildren) throws NotFoundException, GenericException {
    if (logOwnWriteReads && journal.isTouched(id)) {
      LOGGER.warn("[transactionId:{}] Index read of {} {} written by the same transaction (sees committed data only)"
        + " at {}", transactionId, returnClass.getSimpleName(), id, caller());
    }
    return super.retrieve(returnClass, id, fieldsToReturn, appendChildren);
  }

  /**
   * Commits requested inside the transaction are deferred until the
   * transaction's changes have been published to the index.
   */
  @Override
  @SafeVarargs
  public final void commit(boolean handleAuthorizationExceptionSilently,
    Class<? extends IsIndexed>... classToCommit) {
    journal.deferCommit(classToCommit);
  }

  private static String caller() {
    return Arrays.stream(Thread.currentThread().getStackTrace()).skip(2)
      .filter(e -> !e.getClassName().startsWith(IndexService.class.getName())
        && !e.getClassName().equals(TransactionIndexService.class.getName()))
      .limit(3).map(StackTraceElement::toString).collect(Collectors.joining(" <- "));
  }
}
