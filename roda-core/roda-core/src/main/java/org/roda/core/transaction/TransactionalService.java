package org.roda.core.transaction;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface TransactionalService {
  void commit();

  void rollback();
}
