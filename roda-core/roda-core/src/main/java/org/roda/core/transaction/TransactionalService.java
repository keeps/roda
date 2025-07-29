/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.transaction;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface TransactionalService {
  void commit() throws RODATransactionException;

  void rollback()  throws RODATransactionException;
}
