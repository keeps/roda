package org.roda.core.data.v2.synchronization.bundle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class CentralEntities implements Serializable {
  private static final long serialVersionUID = 1886330697587517966L;

  private List<String> aipsList;
  private List<String> dipsList;
  private List<String> risksList;
  private List<String> missingAips;
  private List<String> missingDips;
  private List<String> missingRisks;

  public CentralEntities() {
    aipsList = new ArrayList<>();
    dipsList = new ArrayList<>();
    risksList = new ArrayList<>();
    missingAips = new ArrayList<>();
    missingDips = new ArrayList<>();
    missingRisks = new ArrayList<>();
  }

  public List<String> getAipsList() {
    return aipsList;
  }

  public void setAipsList(final List<String> aipsList) {
    this.aipsList = aipsList;
  }

  public List<String> getDipsList() {
    return dipsList;
  }

  public void setDipsList(final List<String> dipsList) {
    this.dipsList = dipsList;
  }

  public List<String> getRisksList() {
    return risksList;
  }

  public void setRisksList(final List<String> risksList) {
    this.risksList = risksList;
  }

  public void setMissingAips(final List<String> missingAips) {
    this.missingAips = missingAips;
  }

  public List<String> getMissingAips() {
    return this.missingAips;
  }

  public void setMissingDips(final List<String> missingDips) {
    this.missingDips = missingDips;
  }

  public List<String> getMissingDips() {
    return this.missingDips;
  }

  public void setMissingRisks(final List<String> missingRisks) {
    this.missingRisks = missingRisks;
  }

  public List<String> getMissingRisks() {
    return this.missingRisks;
  }
}
