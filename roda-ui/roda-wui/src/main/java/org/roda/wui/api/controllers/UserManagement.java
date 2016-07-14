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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.EmailAlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.InvalidTokenException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.UserAlreadyExistsException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.core.data.v2.user.User;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.RodaCoreService;

public class UserManagement extends RodaCoreService {

  private UserManagement() {
    super();
  }

  public static Long countLogEntries(RodaUser user, Filter filter)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    Long count = UserManagementHelper.countLogEntries(filter);

    // register action
    controllerAssistant.registerAction(user, null, "filter", filter.toString());

    return count;
  }

  public static IndexResult<LogEntry> findLogEntries(RodaUser user, Filter filter, Sorter sorter, Sublist sublist,
    Facets facets) throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    IndexResult<LogEntry> ret = UserManagementHelper.findLogEntries(filter, sorter, sublist, facets);

    // register action
    controllerAssistant.registerAction(user, null, "filter", filter, "sorter", sorter, "sublist", sublist);

    return ret;
  }

  public static LogEntry retrieveLogEntry(RodaUser user, String logEntryId)
    throws GenericException, AuthorizationDeniedException, NotFoundException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    LogEntry ret = UserManagementHelper.retrieveLogEntry(logEntryId);

    // register action
    controllerAssistant.registerAction(user, null, "logEntryId", logEntryId);

    return ret;
  }

  public static Long countMembers(RodaUser user, Filter filter)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    Long count = UserManagementHelper.countMembers(filter);

    // register action
    controllerAssistant.registerAction(user, null, "filter", filter.toString());

    return count;
  }

  public static IndexResult<RODAMember> findMembers(RodaUser user, Filter filter, Sorter sorter, Sublist sublist,
    Facets facets) throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    IndexResult<RODAMember> ret = UserManagementHelper.findMembers(filter, sorter, sublist, facets);

    // register action
    controllerAssistant.registerAction(user, null, "filter", filter, "sorter", sorter, "sublist", sublist);

    return ret;
  }

  public static User retrieveUser(RodaUser user, String username)
    throws AuthorizationDeniedException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    User ret = UserManagementHelper.retrieveUser(username);

    // register action
    controllerAssistant.registerAction(user, null, "username", username);

    return ret;
  }

  public static RodaUser retrieveRodaUser(RodaUser user, String username)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    RodaUser ret = UserManagementHelper.retrieveRodaUser(username);

    // register action
    controllerAssistant.registerAction(user, null, "username", username);

    return ret;
  }

  public static Group retrieveGroup(RodaUser user, String groupname)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    Group ret = UserManagementHelper.retrieveGroup(groupname);

    // register action
    controllerAssistant.registerAction(user, null, "groupname", groupname);

    return ret;
  }

  public static List<Group> listAllGroups(RodaUser user) throws AuthorizationDeniedException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    List<Group> ret = UserManagementHelper.listAllGroups();

    // register action
    controllerAssistant.registerAction(user);

    return ret;
  }

  public static void registerUser(User user, String password)
    throws GenericException, UserAlreadyExistsException, EmailAlreadyExistsException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // delegate
    UserManagementHelper.registerUser(user, password);

    // register action
    controllerAssistant.registerAction(user, null, "user", user);
  }

  public static User addUser(RodaUser user, User newUser, String password)
    throws AuthorizationDeniedException, NotFoundException, GenericException, EmailAlreadyExistsException,
    UserAlreadyExistsException, IllegalOperationException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    User ret = UserManagementHelper.addUser(newUser, password);

    // register action
    controllerAssistant.registerAction(user, null, "user", newUser);

    return ret;
  }

  public static void modifyMyUser(RodaUser user, User modifiedUser, String password)
    throws AuthorizationDeniedException, NotFoundException, AlreadyExistsException, GenericException,
    IllegalOperationException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    if (!user.getId().equals(modifiedUser.getId())) {
      throw new IllegalOperationException("Trying to modify user information for another user");
    }

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    UserManagementHelper.modifyUser(modifiedUser, password);

    // register action
    controllerAssistant.registerAction(user, null, "user", modifiedUser);
  }

  public static void modifyUser(RodaUser user, User modifiedUser, String password)
    throws AuthorizationDeniedException, NotFoundException, AlreadyExistsException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    UserManagementHelper.modifyUser(modifiedUser, password);

    // register action
    controllerAssistant.registerAction(user, null, "user", modifiedUser);
  }

  public static void removeUser(RodaUser user, String username) throws AuthorizationDeniedException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    UserManagementHelper.removeUser(username);

    // register action
    controllerAssistant.registerAction(user, null, "username", username);
  }

  public static void addGroup(RodaUser user, Group group)
    throws AuthorizationDeniedException, GenericException, AlreadyExistsException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    UserManagementHelper.addGroup(group);

    // register action
    controllerAssistant.registerAction(user, null, "group", group);
  }

  public static void modifyGroup(RodaUser user, Group group)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    UserManagementHelper.modifyGroup(group);

    // register action
    controllerAssistant.registerAction(user, null, "group", group);
  }

  public static void removeGroup(RodaUser user, String groupname)
    throws AuthorizationDeniedException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    UserManagementHelper.removeGroup(groupname);

    // register action
    controllerAssistant.registerAction(user, null, "groupname", groupname);
  }

  // TODO: Methods bellow this line should also checkRoles? If so, a RodaUser is
  // needed.
  // TODO: The methods that call these methods don't have a RodaUser either.
  // TODO: From where should the RodaUser come from?

  public static void sendEmailVerification(String servletPath, String username)
    throws GenericException, NotFoundException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    User user = UserManagementHelper.retrieveUser(username);

    if (user == null)
      throw new NotFoundException("User " + username + " doesn't exist.");

    if (user.isActive() || user.getEmailConfirmationToken() == null)
      throw new GenericException("User " + username + " is already active or email confirmation token doesn't exist.");

    sendEmailVerification(servletPath, user);

    // register action
    controllerAssistant.registerAction(user, null, "user", user);
  }

  public static void confirmUserEmail(String username, String emailConfirmationToken)
    throws InvalidTokenException, NotFoundException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    User user = UserManagementHelper.confirmUserEmail(username, null, emailConfirmationToken);

    // register action
    controllerAssistant.registerAction(user, null, "user", user);
  }

  public static void requestPasswordReset(String servletPath, String usernameOrEmail)
    throws GenericException, NotFoundException, IllegalOperationException {
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
    sendRecoverLoginEmail(servletPath, user);

    // register action
    controllerAssistant.registerAction(user, null, "user", user);
  }

  public static void resetUserPassword(String username, String password, String resetPasswordToken)
    throws InvalidTokenException, IllegalOperationException, NotFoundException, GenericException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    User user = UserManagementHelper.resetUserPassword(username, password, resetPasswordToken);

    // register action
    controllerAssistant.registerAction(user, null, "user", user);
  }

  private static void sendEmailVerification(String servletPath, User user) throws GenericException {
    try {

      Notification notification = new Notification();
      notification.setSubject("Registration in RODA");
      notification.setFromUser("RODA Admin");
      notification.setRecipientUsers(Arrays.asList(user.getEmail()));

      String token = user.getEmailConfirmationToken();
      String username = user.getName();
      String verificationURL = servletPath + "/#verifyemail";
      String verificationCompleteURL = verificationURL + "/" + URLEncoder.encode(username, "UTF-8") + "/" + token;

      Map<String, Object> scopes = new HashMap<String, Object>();
      scopes.put("username", username);
      scopes.put("token", token);
      scopes.put("verificationURL", verificationURL);
      scopes.put("verificationCompleteURL", verificationCompleteURL);

      RodaCoreFactory.getModelService().createNotification(notification, RodaConstants.VERIFICATION_EMAIL_TEMPLATE,
        scopes);

    } catch (GenericException | UnsupportedEncodingException e) {
      throw new GenericException("Problem sending email");
    }
  }

  private static void sendRecoverLoginEmail(String servletPath, User user) throws GenericException {
    try {

      Notification notification = new Notification();
      notification.setSubject("Recover login in RODA");
      notification.setFromUser("RODA Admin");
      notification.setRecipientUsers(Arrays.asList(user.getEmail()));

      String token = user.getResetPasswordToken();
      String username = user.getName();
      String recoverLoginURL = servletPath + "/#resetpassword";
      String recoverLoginCompleteURL = recoverLoginURL + "/" + URLEncoder.encode(username, "UTF-8") + "/" + token;

      Map<String, Object> scopes = new HashMap<String, Object>();
      scopes.put("username", username);
      scopes.put("token", token);
      scopes.put("recoverLoginURL", recoverLoginURL);
      scopes.put("recoverLoginCompleteURL", recoverLoginCompleteURL);

      RodaCoreFactory.getModelService().createNotification(notification, RodaConstants.RECOVER_LOGIN_EMAIL_TEMPLATE,
        scopes);

    } catch (Exception e) {
      throw new GenericException("Problem sending email");
    }
  }

}
