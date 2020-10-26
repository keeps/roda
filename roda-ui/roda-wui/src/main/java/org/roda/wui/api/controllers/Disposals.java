package org.roda.wui.api.controllers;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmationMetadata;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.user.User;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.RodaWuiController;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class Disposals extends RodaWuiController {

  private Disposals() {
    super();
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - start -----------------------------
   * ---------------------------------------------------------------------------
   */
  public static DisposalSchedule createDisposalSchedule(User user, DisposalSchedule disposalSchedule)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException,
    AlreadyExistsException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return BrowserHelper.createDisposalSchedule(disposalSchedule, user);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_DISPOSAL_SCHEDULE_PARAM,
        disposalSchedule);
    }
  }

  public static DisposalSchedule updateDisposalSchedule(User user, DisposalSchedule disposalSchedule)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {

      // delegate
      return BrowserHelper.updateDisposalSchedule(disposalSchedule, user);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, disposalSchedule.getId(), state,
        RodaConstants.CONTROLLER_DISPOSAL_SCHEDULE_PARAM, disposalSchedule);
    }
  }

  public static void deleteDisposalSchedule(User user, String disposalScheduleId) throws RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException, IllegalOperationException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      BrowserHelper.deleteDisposalSchedule(disposalScheduleId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, disposalScheduleId, state,
        RodaConstants.CONTROLLER_DISPOSAL_SCHEDULE_ID_PARAM, disposalScheduleId);
    }
  }

  public static DisposalHold createDisposalHold(User user, DisposalHold disposalHold) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException, NotFoundException, AlreadyExistsException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return BrowserHelper.createDisposalHold(disposalHold, user);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_DISPOSAL_HOLD_PARAM, disposalHold);
    }
  }

  public static DisposalHold updateDisposalHold(User user, DisposalHold disposalHold)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {

      // delegate
      return BrowserHelper.updateDisposalHold(disposalHold, user);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, disposalHold.getId(), state,
        RodaConstants.CONTROLLER_DISPOSAL_HOLD_PARAM, disposalHold);
    }
  }

  public static void deleteDisposalHold(User user, String disposalHoldId) throws RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException, IllegalOperationException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      BrowserHelper.deleteDisposalHold(disposalHoldId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, disposalHoldId, state, RodaConstants.CONTROLLER_DISPOSAL_HOLD_ID_PARAM,
        disposalHoldId);
    }
  }

  public static DisposalConfirmationMetadata recoverDisposalConfirmation(User user, String disposalConfirmationId) {
    return null;
  }

  public static DisposalConfirmationMetadata createDisposalConfirmation(User user,
    DisposalConfirmationMetadata confirmationMetadata) throws AuthorizationDeniedException, AlreadyExistsException,
    NotFoundException, GenericException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {

      // Create the AIP.json with all the overdue AIP

      // Copy the disposal schedules

      // Copy the disposal holds

      // Create the disposal confirmation metadata (storage & index)

      return BrowserHelper.createDisposalConfirmationMetadata(confirmationMetadata, user);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_DISPOSAL_CONFIRMATION_METADATA_PARAM,
        confirmationMetadata);
    }
  }

  public static void destroyAIP(User user, String disposalConfirmationId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    // fetch the overdue AIPs
    // create the job to destroy the overdue AIPs

    // return BrowserHelper.destroyOverdueAIPs(user, null);

  }

  public static void deleteDisposalConfirmation(User user, String disposalConfirmationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException, IllegalOperationException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      BrowserHelper.deleteDisposalConfirmation(disposalConfirmationId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, disposalConfirmationId, state,
        RodaConstants.CONTROLLER_DISPOSAL_CONFIRMATION_ID_PARAM, disposalConfirmationId);
    }
  }

  public static Job changeDisposalSchedule(User user, SelectedItems<IndexedAIP> selected, String scheduleId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;
    try {
      // delegate
      return BrowserHelper.changeDisposalSchedule(user, selected, scheduleId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selected);
    }
  }
}
