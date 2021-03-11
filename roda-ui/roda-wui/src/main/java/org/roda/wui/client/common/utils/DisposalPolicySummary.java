/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.utils;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalPolicySummary {

  public enum PolicyStatus {
    OVERDUE, REVIEW, DESTROY, RETAIN, HOLD, CONFIRMATION, ERROR, NONE;
  }

  private final PolicyStatus policyStatus;
  private final String message;

  public DisposalPolicySummary() {
    this.policyStatus = PolicyStatus.NONE;
    this.message = "";
  }

  public DisposalPolicySummary(PolicyStatus policyStatus, String message) {
    this.policyStatus = policyStatus;
    this.message = message;
  }

  public PolicyStatus getPolicyStatus() {
    return this.policyStatus;
  }

  public String getMessage() {
    return this.message;
  }
}
