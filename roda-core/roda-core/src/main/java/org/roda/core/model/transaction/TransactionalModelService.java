package org.roda.core.model.transaction;

import org.roda.core.model.ModelService;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface TransactionalModelService extends ModelService {
  void commit();

  void rollback();
}
