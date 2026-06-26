/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs.utils;

import java.io.Serializable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DisposalScheduleDialogResult implements Serializable {
  private static final long serialVersionUID = -967014998203940151L;

  public enum ActionType {
    CLEAR, ASSOCIATE
  }

  private ActionType actionType;
  private String disposalScheduleId;

  public DisposalScheduleDialogResult() {
  }

  public DisposalScheduleDialogResult(ActionType actionType, String disposalScheduleId) {
    this.actionType = actionType;
    this.disposalScheduleId = disposalScheduleId;
  }

  public String getDisposalScheduleId() {
    return disposalScheduleId;
  }

  public void setDisposalScheduleId(String disposalScheduleId) {
    this.disposalScheduleId = disposalScheduleId;
  }

  public ActionType getActionType() {
    return actionType;
  }
}
