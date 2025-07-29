/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.transaction;

import org.roda.core.entity.transaction.OperationType;

public record ConsolidatedOperation(OperationType operationType, java.time.LocalDateTime updatedAt,
  String previousVersionId) {
}
