/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.roda.core.common.ServiceException;
import org.roda.core.common.UserUtility;
import org.roda.core.data.common.AuthenticationDeniedException;
import org.roda.core.data.v2.user.RodaSimpleUser;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.wui.common.client.GenericException;

public class UserLoginHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserLoginHelper.class);

  public static RodaUser login(String username, String password, HttpServletRequest request)
    throws GenericException, AuthenticationDeniedException {
    try {
      RodaUser user = UserUtility.getLdapUtility().getAuthenticatedUser(username, password);
      user.setIpAddress(request.getRemoteAddr());
      UserUtility.setUser(request, new RodaSimpleUser(user.getId(), user.getName(), user.getEmail(), user.isGuest()));
      return user;
    } catch (ServiceException e) {
      throw new GenericException(e.getMessage());
    }
  }

}
