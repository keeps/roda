package org.roda.core.repository.transaction;

import org.roda.core.entity.transaction.TransactionLog;
import org.roda.core.entity.transaction.TransactionalStoragePathOperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Repository
public interface TransactionalStoragePathRepository extends JpaRepository<TransactionalStoragePathOperationLog, UUID> {
    // find All By transaction id and storage path
    List<TransactionalStoragePathOperationLog> findByTransactionLogAndStoragePath(TransactionLog transactionLog, String storagePath);

}
