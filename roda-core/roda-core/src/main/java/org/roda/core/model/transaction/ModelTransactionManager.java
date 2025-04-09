package org.roda.core.model.transaction;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.events.EventsManager;
import org.roda.core.storage.StorageService;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ModelTransactionManager {
  TransactionalModelService transactionalModelService;

  public ModelTransactionManager() {

  }

  public TransactionalModelService beginTransaction() {
    return transactionalModelService;
  }
}
