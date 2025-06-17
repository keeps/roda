package org.roda.core.components;

import org.roda.core.entity.job.JobInformationEntity;
import org.roda.core.services.JobInformationService;
import org.springframework.stereotype.Component;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

@Component
public class JobInformationComponent {
  private final JobInformationService service;

  public JobInformationComponent(JobInformationService service) {
    this.service = service;
  }

  public void saveJobInformation(JobInformationEntity jobInformationEntity) {
    service.saveJob(jobInformationEntity);
  }
}
