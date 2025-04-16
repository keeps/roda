package org.roda.core.repository.transaction;

import org.roda.core.model.transaction.TransactionalStoragePath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Repository
public interface TransactionalStoragePathRepository extends JpaRepository<TransactionalStoragePath, Long> {
}
