/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.planning;

import java.io.Serializable;

public class RiskMitigationBundle implements Serializable {

  private static final long serialVersionUID = -5597089097774406159L;

  private String preMitigationProbability;
  private String preMitigationImpact;
  private String posMitigationProbability;
  private String posMitigationImpact;

  private int severityLowLimit;
  private int severityHighLimit;

  public RiskMitigationBundle() {
    preMitigationProbability = "";
    preMitigationImpact = "";
    posMitigationProbability = "";
    posMitigationImpact = "";

    severityLowLimit = 5;
    severityHighLimit = 15;
  }

  public RiskMitigationBundle(int lowLimit, int highLimit, String preProbability, String preImpact,
    String posProbability, String posImpact) {

    preMitigationProbability = preProbability;
    preMitigationImpact = preImpact;
    posMitigationProbability = posProbability;
    posMitigationImpact = posImpact;

    severityLowLimit = lowLimit;
    severityHighLimit = highLimit;
  }

  public int getSeverityLowLimit() {
    return severityLowLimit;
  }

  public void setSeverityLowLimit(int severityLowLimit) {
    this.severityLowLimit = severityLowLimit;
  }

  public int getSeverityHighLimit() {
    return severityHighLimit;
  }

  public void setSeverityHighLimit(int severityHighLimit) {
    this.severityHighLimit = severityHighLimit;
  }

  public String getPreMitigationProbability() {
    return preMitigationProbability;
  }

  public void setPreMitigationProbability(String preMitigationProbability) {
    this.preMitigationProbability = preMitigationProbability;
  }

  public String getPreMitigationImpact() {
    return preMitigationImpact;
  }

  public void setPreMitigationImpact(String preMitigationImpact) {
    this.preMitigationImpact = preMitigationImpact;
  }

  public String getPosMitigationProbability() {
    return posMitigationProbability;
  }

  public void setPosMitigationProbability(String posMitigationProbability) {
    this.posMitigationProbability = posMitigationProbability;
  }

  public String getPosMitigationImpact() {
    return posMitigationImpact;
  }

  public void setPosMitigationImpact(String posMitigationImpact) {
    this.posMitigationImpact = posMitigationImpact;
  }

}
