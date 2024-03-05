/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.Messages;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.browse.bundle.RepresentationInformationExtraBundle;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.RodaWuiController;
import org.roda.wui.common.server.ServerTools;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * FIXME 1) verify all checkObject*Permissions (because now also a permission
 * for insert is available)
 */
public class RepresentationInformations extends RodaWuiController {

  private RepresentationInformations() {
    super();
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - start -----------------------------
   * ---------------------------------------------------------------------------
   */
  public static RepresentationInformation createRepresentationInformation(User user, org.roda.core.data.v2.ri.RepresentationInformation ri)
    throws AuthorizationDeniedException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      return BrowserHelper.createRepresentationInformation(ri, null, user.getName(), false);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_REPRESENTATION_INFORMATION_PARAM, ri);
    }
  }

  public static RepresentationInformation updateRepresentationInformation(User user, org.roda.core.data.v2.ri.RepresentationInformation ri)
    throws AuthorizationDeniedException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      return BrowserHelper.updateRepresentationInformation(ri, null, user.getName(), false);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, ri.getId(), state,
        RodaConstants.CONTROLLER_REPRESENTATION_INFORMATION_PARAM, ri);
    }
  }

  public static void deleteRepresentationInformation(User user, String representationInformationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      BrowserHelper.deleteRepresentationInformation(representationInformationId, false);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, representationInformationId, state,
        RodaConstants.CONTROLLER_REPRESENTATION_INFORMATION_ID_PARAM, representationInformationId);
    }
  }

  public static String retrieveRepresentationInformationFamilyOptions(String family, String localeString) {
    Locale locale = ServerTools.parseLocale(localeString);
    return RodaCoreFactory.getI18NMessages(locale).getTranslation("ri.family." + family, "");
  }

  public static Map<String, String> retrieveRepresentationInformationFamilyOptions(String localeString) {
    Locale locale = ServerTools.parseLocale(localeString);
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    List<String> families = RodaCoreFactory.getRodaConfigurationAsList("ui.ri.family");
    Map<String, String> familyAndTranslation = new HashMap<>();

    for (String family : families) {
      familyAndTranslation.put(family, messages.getTranslation("ri.family." + family));
    }

    return familyAndTranslation;
  }

  public static RepresentationInformation createRepresentationInformation(User user, RepresentationInformation ri,
    RepresentationInformationExtraBundle extra) throws AuthorizationDeniedException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      return BrowserHelper.createRepresentationInformation(ri, extra, user.getName(), true);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_REPRESENTATION_INFORMATION_ID_PARAM,
        ri.getId());
    }
  }

  public static void updateRepresentationInformation(User user, RepresentationInformation ri,
    RepresentationInformationExtraBundle extra) throws AuthorizationDeniedException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      BrowserHelper.updateRepresentationInformation(ri, extra, user.getName(), true);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, ri.getId(), state,
        RodaConstants.CONTROLLER_REPRESENTATION_INFORMATION_ID_PARAM, ri.getId());
    }
  }

  public static Job updateRepresentationInformationListWithFilter(User user,
    SelectedItems<RepresentationInformation> representationInformationItems, String filterToAdd)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;
    try {
      return BrowserHelper.updateRepresentationInformationListWithFilter(representationInformationItems, filterToAdd,
        user);
    } catch (RequestNotValidException | GenericException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_REPRESENTATION_INFORMATION_PARAM,
        representationInformationItems);
    }
  }

  public static Job deleteRepresentationInformation(User user, SelectedItems<RepresentationInformation> selected)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      return BrowserHelper.deleteRepresentationInformation(user, selected);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selected);
    }
  }

  public static Pair<String, Integer> retrieveRepresentationInformationWithFilter(User user, String riFilter)
    throws RODAException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      return BrowserHelper.retrieveRepresentationInformationWithFilter(riFilter);
    } catch (GenericException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_REPRESENTATION_INFORMATION_FILTER_PARAM,
        riFilter);
    }
  }
  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - end -------------------------------
   * ---------------------------------------------------------------------------
   */
}
