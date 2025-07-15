package org.roda.core.repository.transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import org.roda.core.entity.transaction.TransactionLog;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Repository
@Timed
@Counted
public interface TransactionLogRepository extends JpaRepository<TransactionLog, UUID> {
  @EntityGraph(attributePaths = {"storagePathsOperations", "modelOperations"})
  @NonNull
  Optional<TransactionLog> findById(@NonNull UUID id);

  List<TransactionLog> findByStatusOrderByCreatedAt(TransactionLog.TransactionStatus transactionStatus);
}
