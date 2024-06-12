package org.roda.core.plugins;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InactiveUserException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.plugins.base.security.AbstractSecurityConfiguration;
import org.roda.core.security.SecurityObservable;
import org.roda.core.security.SecurityObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.NamingException;
import org.springframework.util.ClassUtils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SecurityManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(SecurityManager.class);
  private static SecurityManager instance;
  private List<String> allowedSecurityPlugins;
  private final SecurityService securityService;

  private SecurityManager(boolean hasMultiMethodAuthentication) {
    this.securityService = new SecurityService(hasMultiMethodAuthentication);
  }

  protected static SecurityManager getInstance(List<String> allowedSecurityPlugins) {
    if (instance == null) {
      boolean hasMultiMethodAuthentication = allowedSecurityPlugins.size() > 1;
      instance = new SecurityManager(hasMultiMethodAuthentication);
      instance.registerAllowedPlugin(allowedSecurityPlugins);
    }
    return instance;
  }

  private void registerAllowedPlugin(List<String> allowedSecurityPlugins) {
    this.allowedSecurityPlugins = allowedSecurityPlugins;
  }

  public void registerAuthPlugin(AbstractSecurityConfiguration securityConfiguration) {
    String className = ClassUtils.getUserClass(securityConfiguration).getName();
    if (allowedSecurityPlugins.contains(className)) {
      securityConfiguration.setSecurityService(securityService);
    }
  }

  public void registerObserver(SecurityObserver observer) {
    securityService.addObserver(observer);
  }

  public static class SecurityService extends SecurityObservable {
    private boolean multiMethodAuthentication = false;
    private final String RODA_LOGIN_METHOD = "RODA_LOGIN_METHOD";

    private SecurityService(boolean multiMethodAuthentication) {
      this.multiMethodAuthentication = multiMethodAuthentication;
    }

    public boolean isMultiMethodAuthentication() {
      return multiMethodAuthentication;
    }

    public void login(String username, final String loginMethod, final Map<String, Object> attributes,
      final HttpServletRequest request) throws RODAException {
      try {
        User requestUser = UserUtility.getUser(request, false);
        request.getSession(true).setAttribute(RODA_LOGIN_METHOD, loginMethod);

        if (requestUser == null || !username.equals(requestUser.getName())) {
          // delegate
          User loggedUser = processLogin(username, attributes, request);
          // register action
          notifyLogin(loggedUser, LogEntryState.SUCCESS, RodaConstants.CONTROLLER_USERNAME_PARAM, loggedUser.getName(),
            RodaConstants.CONTROLLER_LOGIN_METHOD_PARAM, loginMethod);
        }
      } catch (AuthenticationDeniedException e) {
        User guest = UserUtility.getGuest(request.getRemoteAddr());
        // register action
        notifyLogin(guest, LogEntryState.FAILURE, RodaConstants.CONTROLLER_USERNAME_PARAM, guest.getName(),
          RodaConstants.CONTROLLER_LOGIN_METHOD_PARAM, loginMethod);
        throw e;
      }
    }

    public User processLogin(final String username, final Map<String, Object> attributes,
      final HttpServletRequest request) throws RODAException {
      User user = null;

      try {
        user = RodaCoreFactory.getModelService().retrieveUser(username);
      } catch (GenericException e) {
        if (!(e.getCause() instanceof NamingException)) {
          throw e;
        }
      }

      if (user == null || user.equals(new User())) {
        User newUser = new User(username);
        newUser = UserUtility.resetGroupsAndRoles(newUser);

        // try to set user email from cas principal attributes
        mapAttribute(newUser, attributes, "fullname", (u, a) -> u.setFullName(a));
        mapAttribute(newUser, attributes, "email", (u, a) -> u.setEmail(a));

        // Try to find a user with email
        User retrievedUserByEmail = null;
        if (StringUtils.isNotBlank(newUser.getEmail())) {
          try {
            retrievedUserByEmail = RodaCoreFactory.getModelService().retrieveUserByEmail(newUser.getEmail());
          } catch (GenericException e) {
            if (!(e.getCause() instanceof NamingException)) {
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

    private void mapAttribute(User user, Map<String, Object> attributes, String attributeKey,
      BiConsumer<User, String> mapping) {
      Object attributeValue = attributes.get(attributeKey);
      if (attributeValue instanceof String value) {
        mapping.accept(user, value);
      }
    }

    public void logout(HttpServletRequest request, List<String> extraAttributesToBeRemovedFromSession) {
      String loginMethod = (String) request.getSession().getAttribute(RODA_LOGIN_METHOD);
      request.getSession().removeAttribute(RODA_LOGIN_METHOD);
      User user = UserUtility.getUser(request);
      UserUtility.removeUserFromSession(request, extraAttributesToBeRemovedFromSession);
      // register action
      notifyLogout(user, LogEntryState.SUCCESS, RodaConstants.CONTROLLER_USERNAME_PARAM, user.getName(),
        RodaConstants.CONTROLLER_LOGIN_METHOD_PARAM, loginMethod);
    }

  }
}
