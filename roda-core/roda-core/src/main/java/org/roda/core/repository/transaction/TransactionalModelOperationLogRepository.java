package org.roda.core.repository.transaction;

import org.roda.core.entity.transaction.TransactionalModelOperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
@Repository
public interface TransactionalModelOperationLogRepository extends JpaRepository<TransactionalModelOperationLog, UUID> {

}
