package org.roda.core.repository.transaction;

import java.util.List;
import java.util.UUID;

import org.roda.core.entity.transaction.OperationState;
import org.roda.core.entity.transaction.TransactionLog;
import org.roda.core.entity.transaction.TransactionalStoragePathOperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Repository
public interface TransactionalStoragePathRepository extends JpaRepository<TransactionalStoragePathOperationLog, UUID> {
  // find All By transaction id and storage path
  @Query("SELECT o FROM TransactionalStoragePathOperationLog o WHERE o.transactionLog = :transactionLog AND o.operationState = :operationState AND o.storagePath = :storagePath ORDER BY o.updatedAt")
  List<TransactionalStoragePathOperationLog> findByTransactionLogAndStoragePath(
    @Param("transactionLog") TransactionLog transactionLog, @Param("operationState") OperationState operationState,
    @Param("storagePath") String storagePath);

  @Query("SELECT o FROM TransactionalStoragePathOperationLog o WHERE o.transactionLog = :transactionLog AND o.operationState = :operationState ORDER BY o.updatedAt")
  List<TransactionalStoragePathOperationLog> findByTransactionLogOrderByUpdatedAt(
    @Param("transactionLog") TransactionLog transactionLog, @Param("operationState") OperationState operationState);
}
