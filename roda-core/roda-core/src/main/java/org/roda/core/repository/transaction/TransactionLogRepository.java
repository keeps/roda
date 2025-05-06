package org.roda.core.repository.transaction;

import java.util.Optional;

import org.roda.core.entity.transaction.TransactionLog;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog, String> {
  @EntityGraph(attributePaths = {"storagePathsOperations", "modelOperations"})
  @NonNull
  Optional<TransactionLog> findById(@NonNull String id);
}
