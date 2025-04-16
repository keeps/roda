package org.roda.core.model;

import org.roda.core.transaction.TransactionalService;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface TransactionalModelService extends ModelService, TransactionalService {
}
