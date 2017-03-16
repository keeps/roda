/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import javax.servlet.http.HttpServletRequest;

import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.log.LogEntry.LOG_ENTRY_STATE;
import org.roda.core.data.v2.user.User;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.RodaWuiController;

public class UserLogin extends RodaWuiController {

  private UserLogin() {
    super();
  }

  public static User login(String username, String password, HttpServletRequest request)
    throws AuthenticationDeniedException, GenericException {
    User user;
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    try {
      // delegate
      user = UserLoginHelper.login(username, password, request);

      // register action
      controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, RodaConstants.CONTROLLER_USERNAME_PARAM,
        username);

      return user;

    } catch (AuthenticationDeniedException e) {
      user = UserUtility.getGuest();
      user.setIpAddress(request.getRemoteAddr());
      // register action
      controllerAssistant.registerAction(user, LOG_ENTRY_STATE.FAILURE, RodaConstants.CONTROLLER_USERNAME_PARAM,
        username);
      throw e;
    }
  }
}
