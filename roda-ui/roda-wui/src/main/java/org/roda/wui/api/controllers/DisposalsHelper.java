package org.roda.wui.api.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.HandlebarsUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.DisposalHoldNotValidException;
import org.roda.core.data.exceptions.DisposalRuleNotValidException;
import org.roda.core.data.exceptions.DisposalScheduleNotValidException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.disposal.ConditionType;
import org.roda.core.data.v2.ip.disposal.DisposalActionCode;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.data.v2.ip.disposal.DisposalRule;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.DisposalScheduleState;
import org.roda.core.data.v2.ip.disposal.DisposalSchedules;
import org.roda.core.data.v2.ip.disposal.RetentionPeriodIntervalCode;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;
import org.roda.wui.client.browse.MetadataValue;
import org.roda.wui.client.browse.bundle.DisposalConfirmationExtraBundle;
import org.roda.wui.common.client.tools.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class DisposalsHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(DisposalsHelper.class);

  private static final String DISPOSAL_CONFIRMATION_COMMAND_PROPERTY = "core.confirmation.generate.report.command";

  private static final String METADATA_FILE_PLACEHOLDER = "metadataFile";
  private static final String AIPS_FILE_PLACEHOLDER = "aipsFile";
  private static final String SCHEDULES_FILE_PLACEHOLDER = "schedulesFile";
  private static final String HOLDS_FILE_PLACEHOLDER = "holdsFile";

  private static final String DISPOSAL_CONFIRMATION_REPORT_HBS = "disposal_confirmation_report.html.hbs";
  private static final String DISPOSAL_CONFIRMATION_REPORT_PRINT_HBS = "disposal_confirmation_report_print.html.hbs";

  private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
  private static final String HBS_DATEFORMAT_HELPER_NAME = "humanize";

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
      return ConditionType.IS_CHILD_OF.equals(type) || ConditionType.METADATA_FIELD.equals(type);
    }
    return false;
  }

  private static boolean isRuleScheduleValid(DisposalRule rule, DisposalSchedules disposalSchedules) {
    for (DisposalSchedule schedule : disposalSchedules.getObjects()) {
      if (schedule.getId().equals(rule.getDisposalScheduleId())
        && DisposalScheduleState.ACTIVE.equals(schedule.getState())) {
        return true;
      }
    }
    return false;
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
    return numberOfAIPUnder <= 0 || !DisposalScheduleState.INACTIVE.equals(state);
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
    if (DisposalActionCode.RETAIN_PERMANENTLY.equals(actionCode)) {
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
    if (DisposalActionCode.RETAIN_PERMANENTLY.equals(actionCode)) {
      return retentionTriggerElementId == null;
    } else {
      return StringUtils.isNotBlank(retentionTriggerElementId);
    }
  }

  private static boolean isDisposalActionValid(DisposalActionCode actionCode) {
    if (StringUtils.isNotBlank(actionCode.toString())) {
      return DisposalActionCode.RETAIN_PERMANENTLY.equals(actionCode) || DisposalActionCode.DESTROY.equals(actionCode)
        || DisposalActionCode.REVIEW.equals(actionCode);
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

  public static Path getDisposalConfirmationMetadataPath(String confirmationId) throws RequestNotValidException {
    DefaultStoragePath confirmationPath = DefaultStoragePath
      .parse(ModelUtils.getDisposalConfirmationStoragePath(confirmationId));

    Path entityPath = FSUtils.getEntityPath(RodaCoreFactory.getStoragePath(), confirmationPath);

    Path metadataFile = entityPath.resolve(RodaConstants.STORAGE_DIRECTORY_DISPOSAL_CONFIRMATION_METADATA_FILENAME);

    return metadataFile;
  }

  public static Path getDisposalConfirmationAIPsPath(String confirmationId) throws RequestNotValidException {
    DefaultStoragePath confirmationPath = DefaultStoragePath
      .parse(ModelUtils.getDisposalConfirmationStoragePath(confirmationId));

    Path entityPath = FSUtils.getEntityPath(RodaCoreFactory.getStoragePath(), confirmationPath);

    Path aipsFile = entityPath.resolve(RodaConstants.STORAGE_DIRECTORY_DISPOSAL_CONFIRMATION_AIPS_FILENAME);

    return aipsFile;
  }

  public static Path getDisposalConfirmationSchedulesPath(String confirmationId) throws RequestNotValidException {
    DefaultStoragePath confirmationPath = DefaultStoragePath
      .parse(ModelUtils.getDisposalConfirmationStoragePath(confirmationId));

    Path entityPath = FSUtils.getEntityPath(RodaCoreFactory.getStoragePath(), confirmationPath);

    Path schedulesFile = entityPath.resolve(RodaConstants.STORAGE_DIRECTORY_DISPOSAL_CONFIRMATION_SCHEDULES_FILENAME);

    return schedulesFile;
  }

  public static Path getDisposalConfirmationHoldsPath(String confirmationId) throws RequestNotValidException {
    DefaultStoragePath confirmationPath = DefaultStoragePath
      .parse(ModelUtils.getDisposalConfirmationStoragePath(confirmationId));

    Path entityPath = FSUtils.getEntityPath(RodaCoreFactory.getStoragePath(), confirmationPath);

    Path holdsFile = entityPath.resolve(RodaConstants.STORAGE_DIRECTORY_DISPOSAL_CONFIRMATION_HOLDS_FILENAME);

    return holdsFile;
  }

  public static String createDisposalConfirmationReport(String confirmationId, boolean isToPrint)
    throws RODAException, IOException {
    String jqCommandTemplate = RodaCoreFactory.getRodaConfigurationAsString(DISPOSAL_CONFIRMATION_COMMAND_PROPERTY);

    Path metadataPath = getDisposalConfirmationMetadataPath(confirmationId);
    Path aipsPath = getDisposalConfirmationAIPsPath(confirmationId);
    Path schedulesPath = getDisposalConfirmationSchedulesPath(confirmationId);
    Path holdsPath = getDisposalConfirmationHoldsPath(confirmationId);

    Map<String, String> values = new HashMap<>();

    values.put(METADATA_FILE_PLACEHOLDER, metadataPath.toString());
    values.put(AIPS_FILE_PLACEHOLDER, aipsPath.toString());
    values.put(SCHEDULES_FILE_PLACEHOLDER, schedulesPath.toString());
    values.put(HOLDS_FILE_PLACEHOLDER, holdsPath.toString());

    String jqCommandParams = HandlebarsUtility.executeHandlebars(jqCommandTemplate, values);

    List<String> jqCommand = new ArrayList<>();
    for (String param : jqCommandParams.split(" ")) {
      jqCommand.add(param);
    }

    String output = null;
    try {
      output = CommandUtility.execute(jqCommand);
    } catch (CommandException e) {
      throw new RODAException(e);
    }

    Map<String, Object> confirmationValues = new ObjectMapper().readValue(output, HashMap.class);
    InputStream templateStream;

    if (isToPrint) {
      templateStream = RodaCoreFactory.getConfigurationFileAsStream(
        RodaConstants.DISPOSAL_CONFIRMATION_INFORMATION_TEMPLATE_FOLDER + "/" + DISPOSAL_CONFIRMATION_REPORT_PRINT_HBS);
    } else {
      templateStream = RodaCoreFactory.getConfigurationFileAsStream(
        RodaConstants.DISPOSAL_CONFIRMATION_INFORMATION_TEMPLATE_FOLDER + "/" + DISPOSAL_CONFIRMATION_REPORT_HBS);
    }

    String reportTemplate = IOUtils.toString(templateStream, RodaConstants.DEFAULT_ENCODING);

    Handlebars handlebars = new Handlebars();
    handlebars.registerHelper(HBS_DATEFORMAT_HELPER_NAME, new Helper<Long>() {
      @Override
      public Object apply(Long value, Options options) throws IOException {
        ZonedDateTime date = Instant.ofEpochMilli(value).atZone(ZoneOffset.UTC);
        String result = DateTimeFormatter.ofPattern(DATETIME_FORMAT).format(date);
        return result;
      }
    });
    Template template = handlebars.compileInline(reportTemplate);
    String report = template.apply(confirmationValues);

    return report;
  }

}