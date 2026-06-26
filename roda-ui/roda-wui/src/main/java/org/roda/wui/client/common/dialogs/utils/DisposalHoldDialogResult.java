/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs.utils;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import org.roda.core.data.v2.disposal.hold.DisposalHold;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalHoldDialogResult implements Serializable {

  @Serial
  private static final long serialVersionUID = 2998935986470083956L;
  private ActionType actionType;
  private List<String> holdIds;
  private DisposalHold disposalHold;
  public DisposalHoldDialogResult() {
  }

  public DisposalHoldDialogResult(ActionType actionType, List<String> holdIds) {
    this.actionType = actionType;
    this.holdIds = holdIds;
  }

  public DisposalHoldDialogResult(ActionType actionType, DisposalHold disposalHold) {
    this.actionType = actionType;
    this.disposalHold = disposalHold;
  }

  public ActionType getActionType() {
    return actionType;
  }

  public void setActionType(ActionType actionType) {
    this.actionType = actionType;
  }

  public List<String> getHoldIds() {
    return holdIds;
  }

  public void setHoldIds(List<String> holdIds) {
    this.holdIds = holdIds;
  }

  public enum ActionType {
    OVERRIDE, CLEAR, ASSOCIATE
  }
}