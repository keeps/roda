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
