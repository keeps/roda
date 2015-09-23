package org.roda.api.controllers;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

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
    try {
      return UserLoginHelper.login(username, password, request);
    } catch (AuthenticationDeniedException e) {
      RodaUser guest = new RodaUser("guest", "guest", "guest", true);
      guest.setIpAddress(request.getRemoteAddr());
      registerAction(guest, "UserLogin", "login", null, 0,"Details","Wrong login", "username", username, "password",
        StringUtils.repeat("*", password.length()));
      throw (e);
    }
  }
}
