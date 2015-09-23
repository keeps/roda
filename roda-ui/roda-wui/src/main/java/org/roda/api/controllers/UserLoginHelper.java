package org.roda.api.controllers;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.roda.common.ServiceException;
import org.roda.common.UserUtility;

import pt.gov.dgarq.roda.core.common.AuthenticationDeniedException;
import pt.gov.dgarq.roda.core.data.v2.RodaSimpleUser;
import pt.gov.dgarq.roda.core.data.v2.RodaUser;
import pt.gov.dgarq.roda.wui.common.client.GenericException;

public class UserLoginHelper {
  private static final Logger LOGGER = Logger.getLogger(UserLoginHelper.class);

  public static RodaUser login(String username, String password, HttpServletRequest request) throws GenericException, AuthenticationDeniedException {
    try {
      RodaUser user = UserUtility.getLdapUtility().getAuthenticatedUser(username, password);
      UserUtility.setUser(request, new RodaSimpleUser(user.getId(), user.getName(), user.getEmail(), user.isGuest()));
      return user;
    } catch (ServiceException e) {
      throw new GenericException(e.getMessage());
    } 
  }

}
