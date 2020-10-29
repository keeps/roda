package org.roda.wui.client.common.dialogs.utils;

import org.roda.core.data.v2.ip.disposal.DisposalSchedule;

/**
 * @author Gabriel Barros <gbarrps@keep.pt>
 */
public class DisposalScheduleDialogsResult {
  DisposalSchedule disposalSchedule;
  Boolean applyToHierarchy;
  Boolean overwriteAll;

  public DisposalScheduleDialogsResult(DisposalSchedule disposalSchedule, Boolean applyToHierarchy, Boolean overwriteAll) {
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
}
