/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.risks;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RiskMitigationProperties implements Serializable {

  @Serial
  private static final long serialVersionUID = -7437948328796925775L;

  private List<String> probabilities;
  private List<String> impacts;

  private int severityLowLimit;
  private int severityHighLimit;

  public RiskMitigationProperties() {
    probabilities = new ArrayList<>();
    impacts = new ArrayList<>();
    severityLowLimit = 5;
    severityHighLimit = 15;
  }

  public RiskMitigationProperties(int lowLimit, int highLimit, List<String> probabilityList, List<String> impactList) {
    probabilities = probabilityList;
    impacts = impactList;
    severityLowLimit = lowLimit;
    severityHighLimit = highLimit;
  }

  public List<String> getProbabilities() {
    return probabilities;
  }

  public void setProbabilities(List<String> probabilities) {
    this.probabilities = probabilities;
  }

  public List<String> getImpacts() {
    return impacts;
  }

  public void setImpacts(List<String> impacts) {
    this.impacts = impacts;
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
}
