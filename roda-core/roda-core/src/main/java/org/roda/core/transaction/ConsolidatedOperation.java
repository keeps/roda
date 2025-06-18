package org.roda.core.transaction;

import org.roda.core.entity.transaction.OperationType;

public record ConsolidatedOperation(OperationType operationType, java.time.LocalDateTime updatedAt) {
}
