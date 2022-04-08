package org.roda.core.data.v2.synchronization.bundle;

import java.io.Serializable;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class EntitiesBundle implements Serializable {
  private static final long serialVersionUID = -2011949703455939774L;

  private String aipFileName;
  private String dipFileName;
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

  public String getRiskFileName() {
    return riskFileName;
  }

  public void setRiskFileName(String riskFileName) {
    this.riskFileName = riskFileName;
  }
}
