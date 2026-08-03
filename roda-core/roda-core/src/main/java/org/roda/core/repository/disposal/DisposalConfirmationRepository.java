/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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
