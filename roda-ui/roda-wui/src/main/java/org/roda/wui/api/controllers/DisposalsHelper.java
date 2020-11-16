package org.roda.wui.api.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.roda.core.data.exceptions.DisposalHoldNotValidException;
import org.roda.core.data.exceptions.DisposalRuleNotValidException;
import org.roda.core.data.exceptions.DisposalScheduleNotValidException;
import org.roda.core.data.v2.ip.disposal.ConditionType;
import org.roda.core.data.v2.ip.disposal.DisposalActionCode;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.data.v2.ip.disposal.DisposalRule;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.DisposalScheduleState;
import org.roda.core.data.v2.ip.disposal.DisposalSchedules;
import org.roda.core.data.v2.ip.disposal.RetentionPeriodIntervalCode;
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

  public static void validateDisposalRule(DisposalRule disposalRule, DisposalSchedules disposalSchedules)
    throws DisposalRuleNotValidException {
    if (StringUtils.isBlank(disposalRule.getTitle())) {
      throw new DisposalRuleNotValidException("The disposal rule title is mandatory");
    }

    if (!isConditionTypeValid(disposalRule.getType())) {
      throw new DisposalRuleNotValidException("The disposal rule condition type is not valid");
    }

    if (!isRuleScheduleValid(disposalRule, disposalSchedules)) {
      throw new DisposalRuleNotValidException("The disposal rule schedule is not valid");
    }

  }

  private static boolean isConditionTypeValid(ConditionType type) {
    if (StringUtils.isNotBlank(type.toString())) {
      return type.equals(ConditionType.IS_CHILD_OF) || type.equals(ConditionType.METADATA_FIELD);
    }
    return false;
  }

  private static boolean isRuleScheduleValid(DisposalRule rule, DisposalSchedules disposalSchedules) {
    boolean ret = false;

    for (DisposalSchedule schedule : disposalSchedules.getObjects()) {
      if (schedule.getId().equals(rule.getDisposalScheduleId())
        && schedule.getState().equals(DisposalScheduleState.ACTIVE)) {
        ret = true;
      }
    }
    return ret;
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