package org.roda.wui.api.controllers;

import org.roda.core.data.exceptions.DisposalHoldNotValidException;
import org.roda.core.data.exceptions.DisposalScheduleNotValidException;
import org.roda.core.data.v2.ip.disposal.DisposalActionCode;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.RetentionPeriodIntervalCode;
import org.roda.core.data.v2.ip.disposal.RetentionTriggerCode;
import org.roda.wui.common.client.tools.StringUtils;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class DisposalsHelper {

  public DisposalsHelper() {
  };

  public static void validateDisposalHold(DisposalHold disposalHold) throws DisposalHoldNotValidException {
    if (StringUtils.isBlank(disposalHold.getTitle())) {
      throw new DisposalHoldNotValidException("The disposal hold title is mandatory");
    }
  }

  public static void validateDisposalSchedule(DisposalSchedule disposalSchedule)
    throws DisposalScheduleNotValidException {

    if (StringUtils.isNotBlank(disposalSchedule.getTitle())) {
      throw new DisposalScheduleNotValidException("The disposal schedule title is mandatory");
    }

    if (!isDisposalActionValid(disposalSchedule.getActionCode())) {
      throw new DisposalScheduleNotValidException("The disposal action code is not valid");
    }

    if (!isRetentionTriggerValid(disposalSchedule.getActionCode(), disposalSchedule.getRetentionTriggerCode())) {
      throw new DisposalScheduleNotValidException("The retention trigger is not valid");
    }

    if (!isRetentionPeriodIntervalValid(disposalSchedule.getActionCode(),
      disposalSchedule.getRetentionPeriodIntervalCode())) {
      throw new DisposalScheduleNotValidException("The retention period interval is not valid");
    }

    if (!isRetentionPeriodDurationValid(disposalSchedule.getActionCode(),
      disposalSchedule.getRetentionPeriodIntervalCode(), disposalSchedule.getRetentionPeriodDuration())) {
      throw new DisposalScheduleNotValidException("The retention period duration is not valid.");
    }
  }

  private static boolean isNumberValid(String string) {
    boolean isNumber = true;
    try {
      Integer intNum = Integer.parseInt(string);
      if (intNum > 0) {
        return true;
      } else {
        return false;
      }
    } catch (NumberFormatException e) {
      isNumber = false;
    } finally {
      return isNumber;
    }
  }

  private static boolean isRetentionPeriodDurationValid(DisposalActionCode actionCode,
    RetentionPeriodIntervalCode retentionPeriodIntervalCode, Integer retentionPeriodDuration) {
    if (actionCode.equals(DisposalActionCode.RETAIN_PERMANENTLY)
      && retentionPeriodIntervalCode.equals(RetentionPeriodIntervalCode.NO_RETENTION_PERIOD)) {
      if (StringUtils.isBlank(retentionPeriodDuration.toString())) {
        if (isNumberValid(retentionPeriodDuration.toString())) {
          return true;
        }
      }
    } else if (isNumberValid(retentionPeriodDuration.toString())) {
      return true;
    }
    return false;
  }

  private static boolean isRetentionPeriodIntervalValid(DisposalActionCode actionCode,
    RetentionPeriodIntervalCode retentionPeriodIntervalCode) {
    if (actionCode.equals(DisposalActionCode.RETAIN_PERMANENTLY)) {
      if (StringUtils.isBlank(retentionPeriodIntervalCode.toString())) {
        return true;
      }
    } else {
      if (retentionPeriodIntervalCode.equals(RetentionPeriodIntervalCode.NO_RETENTION_PERIOD)
        || retentionPeriodIntervalCode.equals(RetentionPeriodIntervalCode.DAYS)
        || retentionPeriodIntervalCode.equals(RetentionPeriodIntervalCode.WEEKS)
        || retentionPeriodIntervalCode.equals(RetentionPeriodIntervalCode.MONTHS)
        || retentionPeriodIntervalCode.equals(RetentionPeriodIntervalCode.YEARS)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isRetentionTriggerValid(DisposalActionCode actionCode,
    RetentionTriggerCode retentionTriggerCode) {

    if (actionCode.equals(DisposalActionCode.RETAIN_PERMANENTLY)) {
      if (StringUtils.isBlank(retentionTriggerCode.toString())) {
        return true;
      }
    } else {
      if (retentionTriggerCode.equals(RetentionTriggerCode.FROM_NOW)
        || retentionTriggerCode.equals(RetentionTriggerCode.FROM_RECORD_METADATA_DATE)
        || retentionTriggerCode.equals(RetentionTriggerCode.FROM_RECORD_ORIGINATED_DATE)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isDisposalActionValid(DisposalActionCode actionCode) {
    if (StringUtils.isNotBlank(actionCode.toString())) {
      if (actionCode.equals(DisposalActionCode.RETAIN_PERMANENTLY) || actionCode.equals(DisposalActionCode.DESTROY)
        || actionCode.equals(DisposalActionCode.REVIEW)) {
        return true;
      }
    }
    return false;
  }

}
