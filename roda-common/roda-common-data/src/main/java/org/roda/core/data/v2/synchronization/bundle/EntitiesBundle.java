package org.roda.core.data.v2.synchronization.bundle;

import java.io.Serializable;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class EntitiesBundle implements Serializable {
  private static final long serialVersionUID = -2011949703455939774L;

  private String aipFileName;
  private String dipFileName;
  private String jobFileName;
  private String preservationAgentFileName;
  private String repositoryEventFileName;
  private String riskFileName;

  public EntitiesBundle() {
  }

  public String getAipFileName() {
    return aipFileName;
  }

  public void setAipFileName(String aipFileName) {
    this.aipFileName = aipFileName;
  }

  public String getDipFileName() {
    return dipFileName;
  }

  public void setDipFileName(String dipFileName) {
    this.dipFileName = dipFileName;
  }

  public String getJobFileName() {
    return jobFileName;
  }

  public void setJobFileName(String jobFileName) {
    this.jobFileName = jobFileName;
  }

  public String getPreservationAgentFileName() {
    return preservationAgentFileName;
  }

  public void setPreservationAgentFileName(String preservationAgentFileName) {
    this.preservationAgentFileName = preservationAgentFileName;
  }

  public String getRepositoryEventFileName() {
    return repositoryEventFileName;
  }

  public void setRepositoryEventFileName(String repositoryEventFileName) {
    this.repositoryEventFileName = repositoryEventFileName;
  }

  public String getRiskFileName() {
    return riskFileName;
  }

  public void setRiskFileName(String riskFileName) {
    this.riskFileName = riskFileName;
  }
}
