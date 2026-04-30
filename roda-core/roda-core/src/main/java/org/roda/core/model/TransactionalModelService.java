/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model;

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
}
