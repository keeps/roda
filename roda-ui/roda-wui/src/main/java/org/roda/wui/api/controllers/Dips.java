/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.user.User;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.RodaWuiController;

public class Dips extends RodaWuiController {

  private Dips() {
    super();
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - start -----------------------------
   * ---------------------------------------------------------------------------
   */
  public static DIP createDIP(User user, DIP dip) throws GenericException, AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return BrowserHelper.createDIP(dip);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_DIP_PARAM, dip);
    }
  }

  public static DIP updateDIP(User user, DIP dip)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      IndexedDIP idip = RodaCoreFactory.getIndexService().retrieve(IndexedDIP.class, dip.getId(),
        RodaConstants.DIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, idip);

      // delegate
      return BrowserHelper.updateDIP(dip);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, dip.getId(), state, RodaConstants.CONTROLLER_DIP_PARAM, dip);
    }
  }

  public static boolean showDIPEmbedded() {
    return RodaCoreFactory.getRodaConfiguration().getBoolean("ui.dip.externalURL.showEmbedded", false);
  }

  public static Job updateDIPPermissions(User user, SelectedItems<IndexedDIP> dips, Permissions permissions,
                                         String details) throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    controllerAssistant.checkObjectPermissions(user, dips);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return BrowserHelper.updateDIPPermissions(user, dips, permissions, details);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_DIPS_PARAM, dips,
        RodaConstants.CONTROLLER_PERMISSIONS_PARAM, permissions, RodaConstants.CONTROLLER_DETAILS_PARAM, details);
    }
  }

  public static Job deleteDIPs(User user, SelectedItems<IndexedDIP> dips, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    controllerAssistant.checkObjectPermissions(user, dips);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      return BrowserHelper.deleteDIPs(user, dips, details);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, dips,
        RodaConstants.CONTROLLER_DETAILS_PARAM, details, RodaConstants.CONTROLLER_DETAILS_PARAM, details);
    }
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - end -------------------------------
   * ---------------------------------------------------------------------------
   */
}
