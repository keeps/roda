package org.roda.core.repository.transaction;

import java.util.List;
import java.util.UUID;

import org.roda.core.entity.transaction.TransactionLog;
import org.roda.core.entity.transaction.TransactionalModelOperationLog;
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
}
