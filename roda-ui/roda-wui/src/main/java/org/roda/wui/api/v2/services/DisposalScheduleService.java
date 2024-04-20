package org.roda.wui.api.v2.services;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.DisposalScheduleNotValidException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.disposal.schedule.DisposalActionCode;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.disposal.schedule.DisposalScheduleState;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedules;
import org.roda.core.data.v2.disposal.schedule.RetentionPeriodIntervalCode;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.user.User;
import org.roda.core.plugins.base.disposal.schedule.AssociateDisposalScheduleToAIPPlugin;
import org.roda.core.plugins.base.disposal.schedule.DisassociateDisposalScheduleToAIPPlugin;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Service
public class DisposalScheduleService {

  public DisposalSchedules getDisposalSchedules()
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, IOException {
    return RodaCoreFactory.getModelService().listDisposalSchedules();
  }

  public DisposalSchedule retrieveDisposalSchedule(String id)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    return RodaCoreFactory.getModelService().retrieveDisposalSchedule(id);
  }

  public DisposalSchedule updateDisposalSchedule(DisposalSchedule schedule, User user)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, IllegalOperationException,
    GenericException {
    return RodaCoreFactory.getModelService().updateDisposalSchedule(schedule, user.getName());
  }

  public void deleteDisposalSchedule(String disposalScheduleId) throws GenericException, RequestNotValidException,
    NotFoundException, AuthorizationDeniedException, IllegalOperationException {
    RodaCoreFactory.getModelService().deleteDisposalSchedule(disposalScheduleId);
  }

  public Job associateDisposalSchedule(User user, SelectedItems<IndexedAIP> selected, String disposalScheduleId)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISPOSAL_SCHEDULE_ID, disposalScheduleId);

    return CommonServicesUtils.createAndExecuteInternalJob("Associate disposal schedule", selected,
      AssociateDisposalScheduleToAIPPlugin.class, user, pluginParameters,
      "Could not execute associate disposal schedule action");
  }

  public Job disassociateDisposalSchedule(User user, SelectedItems<IndexedAIP> selected)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    return CommonServicesUtils.createAndExecuteInternalJob("Disassociate disposal schedule", selected,
      DisassociateDisposalScheduleToAIPPlugin.class, user, Collections.emptyMap(),
      "Could not execute disassociate disposal schedule action");
  }

  public void validateDisposalSchedule(DisposalSchedule disposalSchedule) throws DisposalScheduleNotValidException {

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

  private boolean isNumberOfAIPsValid(Long numberOfAIPUnder, DisposalScheduleState state) {
    return numberOfAIPUnder <= 0 || !DisposalScheduleState.INACTIVE.equals(state);
  }

  private boolean isNumberValid(String string) {
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

  private boolean isRetentionPeriodDurationValid(DisposalActionCode actionCode,
    RetentionPeriodIntervalCode retentionPeriodIntervalCode, Integer retentionPeriodDuration) {
    if (actionCode.equals(DisposalActionCode.RETAIN_PERMANENTLY)
      || retentionPeriodIntervalCode.equals(RetentionPeriodIntervalCode.NO_RETENTION_PERIOD)) {
      return retentionPeriodDuration == null;
    }
    return isNumberValid(retentionPeriodDuration.toString());
  }

  private boolean isRetentionPeriodIntervalValid(DisposalActionCode actionCode,
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

  private boolean isRetentionTriggerElementIdValid(DisposalActionCode actionCode, String retentionTriggerElementId) {
    if (DisposalActionCode.RETAIN_PERMANENTLY.equals(actionCode)) {
      return retentionTriggerElementId == null;
    } else {
      return StringUtils.isNotBlank(retentionTriggerElementId);
    }
  }

  private boolean isDisposalActionValid(DisposalActionCode actionCode) {
    if (StringUtils.isNotBlank(actionCode.toString())) {
      return DisposalActionCode.RETAIN_PERMANENTLY.equals(actionCode) || DisposalActionCode.DESTROY.equals(actionCode)
        || DisposalActionCode.REVIEW.equals(actionCode);
    }
    return false;
  }

  public void validateDisposalScheduleWhenUpdating(DisposalSchedule disposalSchedule)
    throws DisposalScheduleNotValidException {
    if (StringUtils.isBlank(disposalSchedule.getTitle())) {
      throw new DisposalScheduleNotValidException("The disposal schedule title is mandatory");
    }
  }

  public DisposalSchedule createDisposalSchedule(DisposalSchedule disposalSchedule, User user) throws GenericException,
    AuthorizationDeniedException, AlreadyExistsException, NotFoundException, RequestNotValidException {
    return RodaCoreFactory.getModelService().createDisposalSchedule(disposalSchedule, user.getName());
  }
}
