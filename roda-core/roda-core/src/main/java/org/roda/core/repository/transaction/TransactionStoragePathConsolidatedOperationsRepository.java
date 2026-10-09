/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.repository.transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.roda.core.entity.transaction.OperationState;
import org.roda.core.entity.transaction.TransactionLog;
import org.roda.core.entity.transaction.TransactionStoragePathConsolidatedOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
@Repository
@Timed
@Counted
public interface TransactionStoragePathConsolidatedOperationsRepository
  extends JpaRepository<TransactionStoragePathConsolidatedOperation, UUID> {

  @Query("SELECT o FROM TransactionStoragePathConsolidatedOperation o WHERE o.transactionLog = :transactionLog ORDER BY o.updatedAt ASC")
  List<TransactionStoragePathConsolidatedOperation> getOperationsByTransactionLog(
    @Param("transactionLog") TransactionLog transactionLog);

  @Query("SELECT o FROM TransactionStoragePathConsolidatedOperation o WHERE o.transactionLog = :transactionLog AND o.operationState = SUCCESS ORDER BY o.updatedAt DESC")
  List<TransactionStoragePathConsolidatedOperation> getSuccessfulOperationsByTransactionLog(
    @Param("transactionLog") TransactionLog transactionLog);

  @Modifying
  @Query("UPDATE TransactionStoragePathConsolidatedOperation o SET o.operationState = :operationState, o.updatedAt = :updatedAt WHERE o.id = :id")
  int updateOperationState(@Param("id") UUID id, @Param("operationState") OperationState operationState,
    @Param("updatedAt") LocalDateTime updatedAt);

  @Modifying
  @Query("UPDATE TransactionStoragePathConsolidatedOperation o SET o.operationState = :operationState, o.previousVersion = :previousVersion, o.updatedAt = :updatedAt WHERE o.id = :id")
  int updateOperationState(@Param("id") UUID id, @Param("operationState") OperationState operationState,
    @Param("previousVersion") String previousVersion, @Param("updatedAt") LocalDateTime updatedAt);
}
