package org.roda.wui.client.common.dialogs.utils;

import java.io.Serializable;

import org.roda.core.data.v2.ip.disposal.DisposalHold;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalHoldDialogResult implements Serializable {

  private static final long serialVersionUID = 2998935986470083956L;

  public enum ActionType {
    OVERRIDE, CLEAR, ASSOCIATE
  }

  private ActionType actionType;
  private DisposalHold disposalHold;
  private boolean applyToHierarchy;

  public DisposalHoldDialogResult() {
  }

  public DisposalHoldDialogResult(ActionType actionType, boolean applyToHierarchy) {
    this.actionType = actionType;
    this.applyToHierarchy = applyToHierarchy;
  }

  public DisposalHoldDialogResult(ActionType actionType, DisposalHold disposalHold, boolean applyToHierarchy) {
    this.actionType = actionType;
    this.disposalHold = disposalHold;
    this.applyToHierarchy = applyToHierarchy;
  }

  public void setActionType(ActionType actionType) {
    this.actionType = actionType;
  }

  public void setDisposalHold(DisposalHold disposalHold) {
    this.disposalHold = disposalHold;
  }

  public ActionType getActionType() {
    return actionType;
  }

  public DisposalHold getDisposalHold() {
    return disposalHold;
  }

  public boolean isApplyToHierarchy() {
    return applyToHierarchy;
  }

  public void setApplyToHierarchy(Boolean applyToHierarchy) {
    this.applyToHierarchy = applyToHierarchy;
  }
}