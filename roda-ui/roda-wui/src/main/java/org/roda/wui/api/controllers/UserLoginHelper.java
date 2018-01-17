/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import javax.servlet.http.HttpServletRequest;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.UserUtility;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.EmailUnverifiedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InactiveUserException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.utils.StringUtils;

/**
 * Helper class to perform users login.
 */
public class UserLoginHelper {

  private UserLoginHelper() {
    // do nothing
  }

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
    UserUtility.setUser(request, user);
    return user;
  }

  public static User casLogin(final String username, final HttpServletRequest request) throws RODAException {
    User user = null;

    try {
      user = RodaCoreFactory.getModelService().retrieveUserByName(username);
    } catch (GenericException e) {
      if (!(e.getCause() instanceof LdapException)) {
        throw e;
      }
    }

    if (user == null) {
      User newUser = new User(username);

      // try to set user email from cas principal attributes
      if (request.getUserPrincipal() instanceof AttributePrincipal) {
        AttributePrincipal attributePrincipal = (AttributePrincipal) request.getUserPrincipal();
        Object possibleEmail = attributePrincipal.getAttributes().get("email");
        if (possibleEmail != null && possibleEmail instanceof String) {
          newUser.setEmail((String) possibleEmail);
        }
      }

      user = RodaCoreFactory.getModelService().createUser(new User(username), true);
    }

    if (!user.isActive()) {
      throw new InactiveUserException("User is not active.");
    }

    UserUtility.setUser(request, user);
    return user;
  }
}
