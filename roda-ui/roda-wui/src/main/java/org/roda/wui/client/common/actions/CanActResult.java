/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class CanActResult {
  public enum Reason {
    USER, CONTEXT
  }

  private final boolean canAct;
  private final Reason reason;
  private final String reasonSummary;

  public CanActResult(boolean canAct, Reason reason, String reasonSummary) {
    this.canAct = canAct;
    this.reason = reason;
    this.reasonSummary = reasonSummary;
  }

  public boolean canAct() {
    return canAct;
  }

  public Reason getReason() {
    return reason;
  }

  public String getReasonSummary() {
    return reasonSummary;
  }
}
