/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import javax.servlet.http.HttpServletRequest;

import org.roda.core.common.LdapUtilityException;
import org.roda.core.common.ServiceException;
import org.roda.core.common.UserUtility;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.user.RodaSimpleUser;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.core.data.exceptions.EmailUnverifiedException;

public class UserLoginHelper {

  public static RodaUser login(String username, String password, HttpServletRequest request)
    throws GenericException, AuthenticationDeniedException {
    try {
      RodaUser rodaUser = UserUtility.getLdapUtility().getAuthenticatedUser(username, password);
      if (!rodaUser.isActive()) {
        User user = UserUtility.getLdapUtility().getUser(rodaUser.getName());
        if (StringUtils.isNotBlank(user.getEmailConfirmationToken())) {
          throw new EmailUnverifiedException("Email is not verified.");
        }
        throw new AuthenticationDeniedException("User is not active.");
      }
      rodaUser.setIpAddress(request.getRemoteAddr());
      UserUtility.setUser(request,
        new RodaSimpleUser(rodaUser.getId(), rodaUser.getName(), rodaUser.getEmail(), rodaUser.isGuest()));
      return rodaUser;
    } catch (ServiceException | LdapUtilityException e) {
      throw new GenericException(e.getMessage(), e);
    }
  }

}
