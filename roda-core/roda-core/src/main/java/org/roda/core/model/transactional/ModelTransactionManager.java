package org.roda.core.model.transactional;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.events.EventsManager;
import org.roda.core.storage.StorageService;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ModelTransactionManager {
  TransactionalModelService transactionalModelService;

  public ModelTransactionManager(StorageService storage, EventsManager eventsManager, RodaConstants.NodeType nodeType,
    String instanceId) {
    this.transactionalModelService = new TransactionalModelService(storage, eventsManager, nodeType, instanceId);
  }

  public TransactionalModelService beginTransaction() {
    return transactionalModelService;
  }
}
