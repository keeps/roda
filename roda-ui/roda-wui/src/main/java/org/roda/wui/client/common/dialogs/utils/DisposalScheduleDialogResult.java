package org.roda.wui.client.common.dialogs.utils;

import org.roda.core.data.v2.ip.disposal.DisposalSchedule;

/**
 * @author Gabriel Barros <gbarrps@keep.pt>
 */
public class DisposalScheduleDialogResult {
  public enum ActionType {
    CLEAR, ASSOCIATE
  }

  private ActionType actionType;
  private DisposalSchedule disposalSchedule;
  private Boolean applyToHierarchy;
  private Boolean overwriteAll;

  public DisposalScheduleDialogResult(ActionType actionType, DisposalSchedule disposalSchedule, Boolean applyToHierarchy, Boolean overwriteAll) {
    this.actionType = actionType;
    this.disposalSchedule = disposalSchedule;
    this.applyToHierarchy = applyToHierarchy;
    this.overwriteAll = overwriteAll;
  }

  public DisposalSchedule getDisposalSchedule() {
    return disposalSchedule;
  }

  public void setDisposalSchedule(DisposalSchedule disposalSchedule) {
    this.disposalSchedule = disposalSchedule;
  }

  public Boolean isApplyToHierarchy() {
    return applyToHierarchy;
  }

  public void setApplyToHierarchy(Boolean applyToHierarchy) {
    this.applyToHierarchy = applyToHierarchy;
  }

  public Boolean isOverwriteAll() {
    return overwriteAll;
  }

  public void setOverwriteAll(Boolean overwriteAll) {
    this.overwriteAll = overwriteAll;
  }

  public ActionType getActionType() {
    return actionType;
  }
}
