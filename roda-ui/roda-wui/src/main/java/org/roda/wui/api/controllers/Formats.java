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
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.log.LogEntry.LOG_ENTRY_STATE;
import org.roda.core.data.v2.user.User;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.RodaWuiController;

/**
 * FIXME 1) verify all checkObject*Permissions (because now also a permission
 * for insert is available)
 */
public class Formats extends RodaWuiController {

  private Formats() {
    super();
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - start -----------------------------
   * ---------------------------------------------------------------------------
   */
  public static Format createFormat(User user, Format format)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    Format createdFormat = RodaCoreFactory.getModelService().createFormat(format, false);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, RodaConstants.CONTROLLER_FORMAT_PARAM, format);
    return createdFormat;
  }

  public static Format updateFormat(User user, Format format)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    Format updatedFormat = RodaCoreFactory.getModelService().updateFormat(format, false);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, RodaConstants.CONTROLLER_FORMAT_PARAM, format);
    return updatedFormat;
  }

  public static void deleteFormat(User user, String formatId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    RodaCoreFactory.getModelService().deleteFormat(formatId, false);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, RodaConstants.CONTROLLER_FORMAT_ID_PARAM,
      formatId);
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - end -------------------------------
   * ---------------------------------------------------------------------------
   */
}
