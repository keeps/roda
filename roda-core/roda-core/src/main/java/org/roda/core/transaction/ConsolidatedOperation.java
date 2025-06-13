package org.roda.core.transaction;

import org.roda.core.entity.transaction.OperationType;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public record ConsolidatedOperation(OperationType operationType, String version, java.time.LocalDateTime updatedAt) {
}
