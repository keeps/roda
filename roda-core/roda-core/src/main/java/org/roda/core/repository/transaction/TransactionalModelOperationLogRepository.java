package org.roda.core.repository.transaction;

import java.util.List;
import java.util.UUID;

import org.roda.core.entity.transaction.OperationState;
import org.roda.core.entity.transaction.OperationType;
import org.roda.core.entity.transaction.TransactionLog;
import org.roda.core.entity.transaction.TransactionalModelOperationLog;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
@Repository
@Timed
@Counted
public interface TransactionalModelOperationLogRepository extends JpaRepository<TransactionalModelOperationLog, UUID> {

  @Query("SELECT o FROM TransactionalModelOperationLog o WHERE o.transactionLog = :transactionLog ORDER BY o.updatedAt")
  List<TransactionalModelOperationLog> findByTransactionLogOrderByUpdatedAt(
    @Param("transactionLog") TransactionLog transactionLog);

  @Query("SELECT o FROM TransactionalModelOperationLog o WHERE o.transactionLog = :transactionLog AND o.operationState = :operationState AND o.liteObject = :liteObject AND o.operationType = :operationType")
  List<TransactionalModelOperationLog> findAnyByTransactionLogAndLiteObjectAndOperationType(
    @Param("transactionLog") TransactionLog transactionLog, @Param("operationState") OperationState operationState,
    @Param("liteObject") String liteObject, @Param("operationType") OperationType operationType, PageRequest of);
}
