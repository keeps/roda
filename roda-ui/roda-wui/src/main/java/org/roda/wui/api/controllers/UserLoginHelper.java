/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import javax.servlet.http.HttpServletRequest;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.UserUtility;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.EmailUnverifiedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InactiveUserException;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.utils.StringUtils;

/**
 * Helper class to perform users login.
 */
public class UserLoginHelper {

  /**
   * Login the specified user.
   * 
   * @param username
   *          the username.
   * @param password
   *          the user password.
   * @param request
   *          the HTTP request.
   * @return the authenticated {@link User}.
   * @throws GenericException
   *           if some error occurs.
   * @throws AuthenticationDeniedException
   *           if authentication was denied for the provided credentials.
   *           Authentication can be denied by bag credentials, unverified user
   *           email or inactive user.
   */
  public static User login(final String username, final String password, final HttpServletRequest request)
    throws GenericException, AuthenticationDeniedException {
    final User user = RodaCoreFactory.getModelService().retrieveAuthenticatedUser(username, password);
    if (!user.isActive()) {
      if (StringUtils.isNotBlank(user.getEmailConfirmationToken())) {
        throw new EmailUnverifiedException("Email is not verified.");
      }
      throw new InactiveUserException("User is not active.");
    }
    user.setIpAddress(request.getRemoteAddr());
    UserUtility.setUser(request, user);
    return user;
  }

}
