package org.roda.api.controllers;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.roda.common.UserUtility;

import pt.gov.dgarq.roda.common.RodaCoreService;
import pt.gov.dgarq.roda.core.common.AuthenticationDeniedException;
import pt.gov.dgarq.roda.core.data.v2.RodaUser;
import pt.gov.dgarq.roda.wui.common.client.GenericException;

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
