package org.roda.core.services;

import org.roda.core.entity.job.JobInformationEntity;
import org.roda.core.repository.jobs.JobInformationRepository;
import org.springframework.stereotype.Service;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

@Service
public class JobInformationService {

  private final JobInformationRepository jobRepo;

  public JobInformationService(JobInformationRepository jobRepo) {
    this.jobRepo = jobRepo;
  }

  public void saveJob(JobInformationEntity jobInformationEntity) {
    jobRepo.save(jobInformationEntity);
  }
}
