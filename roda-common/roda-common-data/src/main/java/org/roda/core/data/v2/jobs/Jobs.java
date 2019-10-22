/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.jobs;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;


import org.roda.core.data.common.RodaConstants;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_JOBS)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Jobs {
  private List<Job> jobList;

  public Jobs() {
    super();
    jobList = new ArrayList<>();
  }

  public Jobs(List<Job> jobs) {
    super();
    this.jobList = jobs;
  }

  @JsonProperty(value = RodaConstants.RODA_OBJECT_JOBS)
  @XmlElement(name = RodaConstants.RODA_OBJECT_JOB)
  public List<Job> getJobs() {
    return jobList;
  }

  public void setJobs(List<Job> jobs) {
    this.jobList = jobs;
  }

  public void addJob(Job job) {
    this.jobList.add(job);
  }

}
