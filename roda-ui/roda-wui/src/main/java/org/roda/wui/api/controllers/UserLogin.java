/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.roda.core.common.UserUtility;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.wui.common.RodaCoreService;

public class UserLogin extends RodaCoreService {

  private UserLogin() {
    super();
  }

  public static RodaUser login(String username, String password, HttpServletRequest request)
    throws AuthenticationDeniedException, GenericException {
    RodaUser user;
    Date startDate = new Date();

    try {
      // delegate
      user = UserLoginHelper.login(username, password, request);

      // register action
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, "Login", "loginSuccessful", null, duration, "username", username);

      return user;
    } catch (AuthenticationDeniedException e) {
      user = UserUtility.getGuest();
      user.setIpAddress(request.getRemoteAddr());
      // register action
      long duration = new Date().getTime() - startDate.getTime();
      registerAction(user, "Login", "loginFailed", null, duration, "username", username);
      throw (e);
    }
  }
}
