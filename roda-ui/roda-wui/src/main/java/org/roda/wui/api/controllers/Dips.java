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
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.log.LogEntry.LOG_ENTRY_STATE;
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
  public static DIP createDIP(User user, DIP dip)
    throws GenericException, AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      return BrowserHelper.createDIP(dip);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
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

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      IndexedDIP idip = RodaCoreFactory.getIndexService().retrieve(IndexedDIP.class, dip.getId(),
        RodaConstants.DIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, idip);

      // delegate
      return BrowserHelper.updateDIP(dip);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, dip.getId(), state, RodaConstants.CONTROLLER_DIP_PARAM, dip);
    }
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - end -------------------------------
   * ---------------------------------------------------------------------------
   */
}
