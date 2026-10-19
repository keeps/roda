/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model;

import java.util.Optional;
import java.util.Set;

import org.roda.core.catalog.CatalogReader;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.transaction.TransactionalService;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface TransactionalModelService extends ModelService, TransactionalService {
    @Override
    default void createOrUpdateJob(Job job) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    }

  /**
   * @return the catalog view of this transaction (committed AIPs plus the AIPs
   *         written by it), or empty when the catalog is not available
   */
  default Optional<CatalogReader> getCatalogReader() {
    return Optional.empty();
  }

  /**
   * @return ids of the AIPs written by this transaction so far
   */
  default Set<String> getWrittenAIPIds() {
    return Set.of();
  }
}
