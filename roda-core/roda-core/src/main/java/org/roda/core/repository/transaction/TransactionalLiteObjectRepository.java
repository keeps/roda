package org.roda.core.repository.transaction;

import org.roda.core.entity.transaction.TransactionalLiteObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Repository
public interface TransactionalLiteObjectRepository extends JpaRepository<TransactionalLiteObject, Long> {
}
