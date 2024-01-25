/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apereo.cas.client.authentication.AttributePrincipal;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.EmailUnverifiedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InactiveUserException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.common.client.tools.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

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
      user = RodaCoreFactory.getModelService().retrieveUser(username);
    } catch (GenericException e) {
      if (!(e.getCause() instanceof LdapException)) {
        throw e;
      }
    }

    if (user == null) {
      User newUser = new User(username);
      newUser = UserUtility.resetGroupsAndRoles(newUser);

      // try to set user email from cas principal attributes
      if (request.getUserPrincipal() instanceof AttributePrincipal attributePrincipal) {
        Map<String, Object> attributes = attributePrincipal.getAttributes();

        mapCasAttribute(newUser, attributes, "fullname", (u, a) -> u.setFullName(a));
        mapCasAttribute(newUser, attributes, "email", (u, a) -> u.setEmail(a));
      }

      // Try to find a user with email
      User retrievedUserByEmail = null;
      if (StringUtils.isNotBlank(newUser.getEmail())) {
        try {
          retrievedUserByEmail = RodaCoreFactory.getModelService().retrieveUserByEmail(newUser.getEmail());
        } catch (GenericException e) {
          if (!(e.getCause() instanceof LdapException)) {
            throw e;
          }
        }
      }

      if (retrievedUserByEmail != null) {
        user = retrievedUserByEmail;
      } else {
        // If no user was found with username or e-mail, a new one is created.
        user = RodaCoreFactory.getModelService().createUser(newUser, true);
      }
    }

    if (!user.isActive()) {
      throw new InactiveUserException("User is not active.");
    }

    UserUtility.setUser(request, user);
    return user;
  }

  private static void mapCasAttribute(User user, Map<String, Object> attributes, String attributeKey,
    BiConsumer<User, String> mapping) {
    Object attributeValue = attributes.get(attributeKey);
    if (attributeValue instanceof String value) {
      mapping.accept(user, value);
    }
  }
}
