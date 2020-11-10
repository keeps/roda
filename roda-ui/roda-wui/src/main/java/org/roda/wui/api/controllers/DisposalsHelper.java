package org.roda.wui.api.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.roda.core.data.exceptions.DisposalHoldNotValidException;
import org.roda.core.data.exceptions.DisposalScheduleNotValidException;
import org.roda.core.data.v2.ip.disposal.DisposalActionCode;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.DisposalScheduleState;
import org.roda.core.data.v2.ip.disposal.RetentionPeriodIntervalCode;
import org.roda.core.data.v2.ip.disposal.RetentionTriggerCode;
import org.roda.wui.client.browse.MetadataValue;
import org.roda.wui.client.browse.bundle.DisposalConfirmationExtraBundle;
import org.roda.wui.common.client.tools.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class DisposalsHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(DisposalsHelper.class);

  public DisposalsHelper() {
    // do nothing
  }

  public static void validateDisposalHold(DisposalHold disposalHold) throws DisposalHoldNotValidException {
    if (StringUtils.isBlank(disposalHold.getTitle())) {
      throw new DisposalHoldNotValidException("The disposal hold title is mandatory");
    }
  }

  public static void validateDisposalSchedule(DisposalSchedule disposalSchedule)
    throws DisposalScheduleNotValidException {

    if (StringUtils.isBlank(disposalSchedule.getTitle())) {
      throw new DisposalScheduleNotValidException("The disposal schedule title is mandatory");
    }

    if (!isNumberOfAIPsValid(disposalSchedule.getApiCounter(), disposalSchedule.getState())) {
      throw new DisposalScheduleNotValidException("The disposal schedule can not be deactivated");
    }

    if (!isDisposalActionValid(disposalSchedule.getActionCode())) {
      throw new DisposalScheduleNotValidException("The disposal action code is not valid");
    }
    if (!isRetentionTriggerValid(disposalSchedule.getActionCode(), disposalSchedule.getRetentionTriggerCode())) {
      throw new DisposalScheduleNotValidException("The retention trigger is not valid");
    }

    if (!isRetentionTriggerElementIdValid(disposalSchedule.getActionCode(),
      disposalSchedule.getRetentionTriggerElementId())) {
      throw new DisposalScheduleNotValidException("The retention trigger element id is not valid");
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

  private static boolean isNumberOfAIPsValid(Long numberOfAIPUnder, DisposalScheduleState state) {
    if (numberOfAIPUnder > 0 && state.equals(DisposalScheduleState.INACTIVE)) {
      return false;
    }
    return true;
  }

  private static boolean isNumberValid(String string) {
    boolean isNumber = true;
    try {
      int intNum = Integer.parseInt(string);
      if (intNum <= 0) {
        isNumber = false;
      }
    } catch (NumberFormatException e) {
      isNumber = false;
    }
    return isNumber;
  }

  private static boolean isRetentionPeriodDurationValid(DisposalActionCode actionCode,
    RetentionPeriodIntervalCode retentionPeriodIntervalCode, Integer retentionPeriodDuration) {
    if (actionCode.equals(DisposalActionCode.RETAIN_PERMANENTLY)
      || retentionPeriodIntervalCode.equals(RetentionPeriodIntervalCode.NO_RETENTION_PERIOD)) {
      return retentionPeriodDuration == null;
    }
    return isNumberValid(retentionPeriodDuration.toString());
  }

  private static boolean isRetentionPeriodIntervalValid(DisposalActionCode actionCode,
    RetentionPeriodIntervalCode retentionPeriodIntervalCode) {
    if (actionCode.equals(DisposalActionCode.RETAIN_PERMANENTLY)) {
      return retentionPeriodIntervalCode == null;
    } else {
      return retentionPeriodIntervalCode.equals(RetentionPeriodIntervalCode.NO_RETENTION_PERIOD)
        || retentionPeriodIntervalCode.equals(RetentionPeriodIntervalCode.DAYS)
        || retentionPeriodIntervalCode.equals(RetentionPeriodIntervalCode.WEEKS)
        || retentionPeriodIntervalCode.equals(RetentionPeriodIntervalCode.MONTHS)
        || retentionPeriodIntervalCode.equals(RetentionPeriodIntervalCode.YEARS);
    }
  }

  private static boolean isRetentionTriggerValid(DisposalActionCode actionCode,
    RetentionTriggerCode retentionTriggerCode) {
    if (actionCode.equals(DisposalActionCode.RETAIN_PERMANENTLY)) {
      return retentionTriggerCode == null;
    } else {
      return retentionTriggerCode.equals(RetentionTriggerCode.FROM_NOW)
        || retentionTriggerCode.equals(RetentionTriggerCode.FROM_RECORD_METADATA_DATE)
        || retentionTriggerCode.equals(RetentionTriggerCode.FROM_RECORD_ORIGINATED_DATE);
    }
  }

  private static boolean isRetentionTriggerElementIdValid(DisposalActionCode actionCode,
    String retentionTriggerElementId) {
    if (actionCode.equals(DisposalActionCode.RETAIN_PERMANENTLY)) {
      return retentionTriggerElementId == null;
    } else {
      return StringUtils.isNotBlank(retentionTriggerElementId);
    }
  }

  private static boolean isDisposalActionValid(DisposalActionCode actionCode) {
    if (StringUtils.isNotBlank(actionCode.toString())) {
      return actionCode.equals(DisposalActionCode.RETAIN_PERMANENTLY) || actionCode.equals(DisposalActionCode.DESTROY)
        || actionCode.equals(DisposalActionCode.REVIEW);
    }
    return false;
  }

  public static Map<String, String> getDisposalConfirmationExtra(DisposalConfirmationExtraBundle extra) {
    Map<String, String> data = new HashMap<>();

    if (extra != null) {
      Set<MetadataValue> values = extra.getValues();
      if (values != null) {
        values.forEach(metadataValue -> {
          String val = metadataValue.get("value");
          if (val != null) {
            val = val.replaceAll("\\s", "");
            if (!"".equals(val)) {
              data.put(metadataValue.get("name"), metadataValue.get("value"));
            }
          }
        });
      }
    }

    return data;
  }
}