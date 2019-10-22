/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.user.User;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.RodaWuiController;

public class UserLogin extends RodaWuiController {

  private UserLogin() {
    super();
  }

  /**
   * Logs in the roda user using the provided username and password
   *
   * @param request
   *          the http request
   * @throws ServletException
   *           if the roda user info could not be retrieved
   */
  public static User login(String username, String password, HttpServletRequest request)
    throws AuthenticationDeniedException, GenericException {
    User user;
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    try {
      // delegate
      user = UserLoginHelper.login(username, password, request);

      // register action
      controllerAssistant.registerAction(user, LogEntryState.SUCCESS, RodaConstants.CONTROLLER_USERNAME_PARAM,
        username);

      return user;

    } catch (AuthenticationDeniedException e) {
      user = UserUtility.getGuest(request.getRemoteAddr());
      // register action
      controllerAssistant.registerAction(user, LogEntryState.FAILURE, RodaConstants.CONTROLLER_USERNAME_PARAM,
        username);
      throw e;
    }
  }

  /**
   * Logs in the roda user from the username that was authenticated via CAS,
   * creating a new user if it does not exist in RODA yet
   *
   * @param request
   *          the http request
   * @throws ServletException
   *           if the roda user info could not be retrieved
   */
  public static void casLogin(String username, HttpServletRequest request) throws RODAException {
    User user;
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    try {
      // delegate
      user = UserLoginHelper.casLogin(username, request);

      // register action
      controllerAssistant.registerAction(user, LogEntryState.SUCCESS, RodaConstants.CONTROLLER_USERNAME_PARAM,
        username);
    } catch (AuthenticationDeniedException e) {
      user = UserUtility.getGuest(request.getRemoteAddr());
      // register action
      controllerAssistant.registerAction(user, LogEntryState.FAILURE, RodaConstants.CONTROLLER_USERNAME_PARAM,
        username);
      throw e;
    }
  }

  /**
   * Logs out the user
   *
   * @param request
   *          the http request
   */
  public static void logout(HttpServletRequest request, List<String> extraAttributesToBeRemovedFromSession) {
    User user = UserUtility.getUser(request);
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // delegate
    UserUtility.removeUserFromSession(request, extraAttributesToBeRemovedFromSession);

    // register action
    controllerAssistant.registerAction(user, LogEntryState.SUCCESS, RodaConstants.CONTROLLER_USERNAME_PARAM,
      user.getName());
  }
}
