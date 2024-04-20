/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs.utils;

import org.roda.core.data.v2.disposal.hold.DisposalHold;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalHoldDialogResult implements Serializable {

  @Serial
  private static final long serialVersionUID = 2998935986470083956L;

  public enum ActionType {
    OVERRIDE, CLEAR, ASSOCIATE
  }

  private ActionType actionType;
  private DisposalHold disposalHold;

  public DisposalHoldDialogResult() {
  }

  public DisposalHoldDialogResult(ActionType actionType) {
    this.actionType = actionType;
  }

  public DisposalHoldDialogResult(ActionType actionType, DisposalHold disposalHold) {
    this.actionType = actionType;
    this.disposalHold = disposalHold;
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
}