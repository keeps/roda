package org.roda.wui.api.controllers;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.user.User;
import org.roda.wui.common.ControllerAssistant;

import java.util.List;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class Representations {

  Representations() {
    // do nothing
  }

  public static Representation createRepresentation(User user, String aipId, String representationId, String type,
    String details) throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException,
    AlreadyExistsException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // check state
      controllerAssistant.checkAIPstate(aip);

      // check if AIP is in a disposal confirmation
      controllerAssistant.checkIfAIPInConfirmation(aip);

      // delegate
      return BrowserHelper.createRepresentation(user, aipId, representationId, type, details);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_TYPE_PARAM, type,
        RodaConstants.CONTROLLER_DETAILS_PARAM, details);
    }
  }

  public static Job deleteRepresentation(User user, SelectedItems<IndexedRepresentation> representations,
    String details) throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {
    };

    // check user permissions
    controllerAssistant.checkRoles(user);
    controllerAssistant.checkObjectPermissions(user, representations);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return BrowserHelper.deleteRepresentation(user, representations, details);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, representations,
        RodaConstants.CONTROLLER_DETAILS_PARAM, details);
    }
  }

  public static void changeRepresentationStates(User user, IndexedRepresentation representation, List<String> newStates,
    String details) throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    controllerAssistant.checkObjectPermissions(user, representation);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      BrowserHelper.changeRepresentationStates(user, representation, newStates, details);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_REPRESENTATION_PARAM, representation,
        RodaConstants.CONTROLLER_STATES_PARAM, newStates, RodaConstants.CONTROLLER_DETAILS_PARAM, details);
    }
  }
}
