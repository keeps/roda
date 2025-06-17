package org.roda.core.repository.jobs;

import java.util.UUID;

import org.roda.core.entity.job.JobInformationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public interface JobInformationRepository extends JpaRepository<JobInformationEntity, UUID> {
}
