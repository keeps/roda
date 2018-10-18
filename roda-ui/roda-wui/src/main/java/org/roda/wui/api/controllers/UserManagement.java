/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.Messages;
import org.roda.core.common.notifications.EmailNotificationProcessor;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.EmailAlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.InvalidTokenException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.UserAlreadyExistsException;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.log.LogEntry.LOG_ENTRY_STATE;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.core.util.IdUtils;
import org.roda.wui.client.browse.bundle.UserExtraBundle;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.RodaWuiController;
import org.roda.wui.common.server.ServerTools;
import org.w3c.util.DateParser;

public class UserManagement extends RodaWuiController {

  private UserManagement() {
    super();
  }

  public static User retrieveUser(User user, String username) throws AuthorizationDeniedException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return UserManagementHelper.retrieveUser(username);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_USERNAME_PARAM, username);
    }
  }

  public static Group retrieveGroup(User user, String groupname)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return UserManagementHelper.retrieveGroup(groupname);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_GROUPNAME_PARAM, groupname);
    }
  }

  public static List<Group> listAllGroups(User user) throws AuthorizationDeniedException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return UserManagementHelper.listAllGroups();
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state);
    }
  }

  public static User registerUser(User user, String password, UserExtraBundle extra, String localeString,
    String servletPath) throws GenericException, UserAlreadyExistsException, EmailAlreadyExistsException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return UserManagementHelper.registerUser(user, password, extra, localeString, servletPath);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_USER_PARAM, user);
    }
  }

  public static User createUser(User user, User newUser, String password, UserExtraBundle extra)
    throws AuthorizationDeniedException, NotFoundException, GenericException, EmailAlreadyExistsException,
    UserAlreadyExistsException, IllegalOperationException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return UserManagementHelper.createUser(newUser, password, extra);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_USER_PARAM, newUser);
    }
  }

  public static User updateMyUser(User user, User modifiedUser, String password, UserExtraBundle extra)
    throws AuthorizationDeniedException, NotFoundException, AlreadyExistsException, GenericException,
    IllegalOperationException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    if (!user.getId().equals(modifiedUser.getId())) {
      throw new IllegalOperationException("Trying to modify user information for another user");
    }

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      return UserManagementHelper.updateMyUser(modifiedUser, password, extra);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_USER_PARAM, modifiedUser);
    }
  }

  public static void updateUser(User user, User modifiedUser, String password, UserExtraBundle extra)
    throws AuthorizationDeniedException, NotFoundException, AlreadyExistsException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      UserManagementHelper.updateUser(modifiedUser, password, extra);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_USER_PARAM, modifiedUser);
    }
  }

  public static void deleteUser(User user, String username) throws AuthorizationDeniedException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      UserManagementHelper.deleteUser(username);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_USERNAME_PARAM, username);
    }
  }

  public static void createGroup(User user, Group group)
    throws AuthorizationDeniedException, GenericException, AlreadyExistsException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      UserManagementHelper.createGroup(group);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_GROUP_PARAM, group);
    }
  }

  public static void updateGroup(User user, Group group)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      UserManagementHelper.updateGroup(group);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_GROUP_PARAM, group);
    }
  }

  public static void deleteGroup(User user, String groupname) throws AuthorizationDeniedException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      // delegate
      UserManagementHelper.deleteGroup(groupname);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_GROUPNAME_PARAM, groupname);
    }
  }

  // TODO: Methods below this line should also checkRoles? If so, a User is
  // needed.
  // TODO: The methods that call these methods don't have a User either.
  // TODO: From where should the User come from?
  // return true if notification was sent, false if the mail cannot be sent and
  // the user was activated...
  public static Notification sendEmailVerification(final String servletPath, final String username,
    final boolean generateNewToken, String ipAddress, String localeString) throws GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserManagementHelper.retrieveUser(username);
    // 20170523 hsilva: need to set ip address for registering the action
    user.setIpAddress(ipAddress);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;

    try {
      if (generateNewToken) {
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        final String isoDateNoMillis = DateParser.getIsoDateNoMillis(calendar.getTime());

        user.setEmailConfirmationToken(IdUtils.createUUID());
        user.setEmailConfirmationTokenExpirationDate(isoDateNoMillis);

        try {
          user = UserManagementHelper.updateUser(user, null, null);
          // 20170523 hsilva: need to set ip address for registering the action
          // as the above method returns a new instante of the modified user
          user.setIpAddress(ipAddress);
        } catch (final AlreadyExistsException | AuthorizationDeniedException e) {
          throw new GenericException("Error updating email confirmation token - " + e.getMessage(), e);
        }
      }

      if (user.isActive() || user.getEmailConfirmationToken() == null) {
        throw new GenericException(
          "User " + username + " is already active or email confirmation token doesn't exist.");
      }

      return sendEmailVerification(servletPath, user, localeString);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_USER_PARAM, user);
    }
  }

  public static void confirmUserEmail(String username, String emailConfirmationToken, String ipAddress)
    throws InvalidTokenException, NotFoundException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    User user = UserManagementHelper.confirmUserEmail(username, null, emailConfirmationToken);
    // 20180112 hsilva: need to set ip address for registering the action
    user.setIpAddress(ipAddress);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, RodaConstants.CONTROLLER_USER_PARAM, user);
  }

  public static void requestPasswordReset(String servletPath, String usernameOrEmail, String localeString,
    String ipAddress) throws GenericException, NotFoundException, IllegalOperationException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    String username = null;
    String email = null;

    if (usernameOrEmail.matches(
      "^[\\w-]+(\\.[\\w-]+)*@([a-z0-9-]+(\\.[a-z0-9-]+)*?\\.[a-z]{2,6}|(\\d{1,3}\\.){3}\\d{1,3})(:\\d{4})?$")) {
      email = usernameOrEmail;
    } else {
      username = usernameOrEmail;
    }

    User user = UserManagementHelper.requestPasswordReset(username, email);
    // 20180112 hsilva: need to set ip address for registering the action
    user.setIpAddress(ipAddress);
    sendRecoverLoginEmail(servletPath, user, localeString);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, RodaConstants.CONTROLLER_USER_PARAM, user);
  }

  public static void resetUserPassword(String username, String password, String resetPasswordToken, String ipAddress)
    throws InvalidTokenException, IllegalOperationException, NotFoundException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    User user = UserManagementHelper.resetUserPassword(username, password, resetPasswordToken);
    // 20180112 hsilva: need to set ip address for registering the action
    user.setIpAddress(ipAddress);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, RodaConstants.CONTROLLER_USER_PARAM, user);
  }

  private static Notification sendEmailVerification(String servletPath, User user, String localeString)
    throws GenericException {
    try {
      Messages messages = RodaCoreFactory.getI18NMessages(ServerTools.parseLocale(localeString));

      Notification notification = new Notification();
      notification.setSubject(messages.getTranslation(RodaConstants.VERIFICATION_EMAIL_TEMPLATE_SUBJECT_TRANSLATION));
      notification.setFromUser(messages.getTranslation(RodaConstants.VERIFICATION_EMAIL_TEMPLATE_FROM_TRANSLATION));
      notification.setRecipientUsers(Arrays.asList(user.getEmail()));

      String token = user.getEmailConfirmationToken();
      String username = user.getName();
      String verificationURL = servletPath + "/#verifyemail";
      String verificationCompleteURL = verificationURL + "/"
        + URLEncoder.encode(username, RodaConstants.DEFAULT_ENCODING) + "/" + token;

      Map<String, Object> scopes = new HashMap<>();
      scopes.put("username", username);
      scopes.put("token", token);
      scopes.put("verificationURL", verificationURL);
      scopes.put("verificationCompleteURL", verificationCompleteURL);

      return RodaCoreFactory.getModelService().createNotification(notification,
        new EmailNotificationProcessor(RodaConstants.VERIFICATION_EMAIL_TEMPLATE, scopes, localeString));
    } catch (UnsupportedEncodingException | AuthorizationDeniedException e) {
      throw new GenericException("Error sending email verification", e);
    }
  }

  private static void sendRecoverLoginEmail(String servletPath, User user, String localeString)
    throws GenericException {
    try {
      Messages messages = RodaCoreFactory.getI18NMessages(ServerTools.parseLocale(localeString));

      Notification notification = new Notification();
      notification.setSubject(messages.getTranslation(RodaConstants.RECOVER_LOGIN_EMAIL_TEMPLATE_SUBJECT_TRANSLATION));
      notification.setFromUser(messages.getTranslation(RodaConstants.RECOVER_LOGIN_EMAIL_TEMPLATE_FROM_TRANSLATION));
      notification.setRecipientUsers(Arrays.asList(user.getEmail()));

      String token = user.getResetPasswordToken();
      String username = user.getName();
      String recoverLoginURL = servletPath + "/#resetpassword";
      String recoverLoginCompleteURL = recoverLoginURL + "/"
        + URLEncoder.encode(username, RodaConstants.DEFAULT_ENCODING) + "/" + token;

      Map<String, Object> scopes = new HashMap<>();
      scopes.put("username", username);
      scopes.put("token", token);
      scopes.put("recoverLoginURL", recoverLoginURL);
      scopes.put("recoverLoginCompleteURL", recoverLoginCompleteURL);
      RodaCoreFactory.getModelService().createNotification(notification,
        new EmailNotificationProcessor(RodaConstants.RECOVER_LOGIN_EMAIL_TEMPLATE, scopes, localeString));

    } catch (Exception e) {
      throw new GenericException("Problem sending email", e);
    }
  }
  
  public static UserExtraBundle retrieveOwnUserExtraBundle(User user) throws AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check permissions
    controllerAssistant.checkRoles(user);

    // delegate
    UserExtraBundle extraBudle = UserManagementHelper.retrieveUserExtraBundle(user.getName());

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS);

    return extraBudle;
  }

  public static UserExtraBundle retrieveUserExtraBundle(User user, String name) throws AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check permissions
    controllerAssistant.checkRoles(user);

    // delegate
    UserExtraBundle extraBudle = UserManagementHelper.retrieveUserExtraBundle(name);

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS, RodaConstants.CONTROLLER_NAME_PARAM, name);

    return extraBudle;
  }

  public static UserExtraBundle retrieveDefaultExtraBundle(User user) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // delegate
    UserExtraBundle extraBundle = UserManagementHelper.retrieveDefaultExtraBundle();

    // register action
    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS);

    return extraBundle;
  }

  public static void deleteMembers(User user, SelectedItems<RODAMember> members)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;
    try {
      // delegate
      UserManagementHelper.deleteMembers(members);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, members);
    }
  }

  public static void changeActiveMembers(User user, SelectedItems<RODAMember> members, boolean activate)
    throws GenericException, RequestNotValidException, AlreadyExistsException, NotFoundException,
    AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LOG_ENTRY_STATE state = LOG_ENTRY_STATE.SUCCESS;
    try {
      // delegate
      UserManagementHelper.changeActiveMembers(members, activate);
    } catch (RODAException e) {
      state = LOG_ENTRY_STATE.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, members,
        RodaConstants.CONTROLLER_ACTIVATE_PARAM, activate);
    }
  }
}
