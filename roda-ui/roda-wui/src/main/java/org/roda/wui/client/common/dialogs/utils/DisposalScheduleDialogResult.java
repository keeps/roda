/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs.utils;

import java.io.Serializable;

import org.roda.core.data.v2.ip.disposal.DisposalSchedule;

/**
 * @author Gabriel Barros <gbarrps@keep.pt>
 */
public class DisposalScheduleDialogResult implements Serializable {
  private static final long serialVersionUID = -967014998203940151L;

  public enum ActionType {
    CLEAR, ASSOCIATE
  }

  public DisposalScheduleDialogResult() {
  }

  private ActionType actionType;
  private DisposalSchedule disposalSchedule;

  public DisposalScheduleDialogResult(ActionType actionType, DisposalSchedule disposalSchedule) {
    this.actionType = actionType;
    this.disposalSchedule = disposalSchedule;
  }

  public DisposalSchedule getDisposalSchedule() {
    return disposalSchedule;
  }

  public void setDisposalSchedule(DisposalSchedule disposalSchedule) {
    this.disposalSchedule = disposalSchedule;
  }

  public ActionType getActionType() {
    return actionType;
  }
}
