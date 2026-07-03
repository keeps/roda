package org.roda.core.repository.disposal;

import org.roda.core.entity.disposal.confirmation.DisposalConfirmations;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public interface DisposalConfirmationRepository extends JpaRepository<DisposalConfirmations, String> {

    DisposalConfirmations findByJobId(String jobId);
}
