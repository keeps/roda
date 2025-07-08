package org.roda.core.repository.transaction;

import java.util.List;
import java.util.UUID;

import org.roda.core.entity.transaction.OperationState;
import org.roda.core.entity.transaction.OperationType;
import org.roda.core.entity.transaction.TransactionLog;
import org.roda.core.entity.transaction.TransactionalStoragePathOperationLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Repository
@Timed
@Counted
public interface TransactionalStoragePathRepository extends JpaRepository<TransactionalStoragePathOperationLog, UUID> {

  @Query("SELECT o FROM TransactionalStoragePathOperationLog o WHERE o.transactionLog = :transactionLog AND o.operationState = :operationState AND o.storagePath = :storagePath AND o.operationType = :operationType")
  List<TransactionalStoragePathOperationLog> findAnyByTransactionLogAndStoragePathAndOperationType(
    @Param("transactionLog") TransactionLog transactionLog, @Param("operationState") OperationState operationState,
    @Param("storagePath") String storagePath, @Param("operationType") OperationType operationType, Pageable pageable);

  @Query("SELECT o FROM TransactionalStoragePathOperationLog o WHERE o.transactionLog = :transactionLog AND o.operationState = :operationState ORDER BY o.updatedAt")
  List<TransactionalStoragePathOperationLog> findByTransactionLogOrderByUpdatedAt(
    @Param("transactionLog") TransactionLog transactionLog, @Param("operationState") OperationState operationState);

  @Query("SELECT o FROM TransactionalStoragePathOperationLog o WHERE o.transactionLog = :transactionLog AND o.operationState = :operationState AND o.operationType = :operationType ORDER BY o.updatedAt")
  List<TransactionalStoragePathOperationLog> findByTransactionLogAndOperationType(
    @Param("transactionLog") TransactionLog transactionLog, @Param("operationType") OperationType operationType,
    @Param("operationState") OperationState operationState);

  @Query("SELECT o FROM TransactionalStoragePathOperationLog o WHERE o.transactionLog = :transactionLog AND o.storagePath LIKE CONCAT(:storagePath, '/%') AND o.operationType <> 'READ'")
  List<TransactionalStoragePathOperationLog> findModificationsUnderStoragePath(
    @Param("transactionLog") TransactionLog transactionLog, @Param("storagePath") String storagePath);
}
