/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.server.management;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.SecureString;
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
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.controllers.UserManagement;
import org.roda.wui.client.browse.bundle.UserExtraBundle;
import org.roda.wui.client.management.UserManagementService;
import org.roda.wui.client.management.recaptcha.RecaptchaException;
import org.roda.wui.common.client.tools.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;

/**
 * User Management service implementation
 *
 * @author Luis Faria
 */
public class UserManagementServiceImpl extends RemoteServiceServlet implements UserManagementService {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private static final String RECAPTCHA_CODE_SECRET_PROPERTY = "ui.google.recaptcha.code.secret";
  private final transient Logger logger = LoggerFactory.getLogger(this.getClass().getName());

  /**
   * User Management Service implementation constructor
   *
   */
  public UserManagementServiceImpl() {
    // do nothing
  }

  private static String retrieveServletUrl(HttpServletRequest req) {
    String scheme = req.getScheme();
    String serverName = req.getServerName();
    int serverPort = req.getServerPort();
    String contextPath = req.getContextPath();

    String url = scheme + "://" + serverName + ":" + serverPort + contextPath;
    if (("http".equalsIgnoreCase(scheme) && serverPort == 80)
      || ("https".equalsIgnoreCase(scheme) && serverPort == 443)) {
      url = scheme + "://" + serverName + contextPath;
    }

    return url;
  }

  @Override
  public Group getGroup(String groupname) throws AuthorizationDeniedException, GenericException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return UserManagement.retrieveGroup(user, groupname);
  }

  @Override
  public User retrieveUser(String username) throws RODAException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return UserManagement.retrieveUser(user, username);
  }

  @Override
  public User registerUser(User user, SecureString password, String captcha, UserExtraBundle extra, String localeString)
    throws GenericException, UserAlreadyExistsException, EmailAlreadyExistsException, RecaptchaException,
    AuthorizationDeniedException {
    boolean userRegistrationDisabled = RodaCoreFactory.getRodaConfiguration()
      .getBoolean(RodaConstants.USER_REGISTRATION_DISABLED, false);
    if (userRegistrationDisabled) {
      throw new GenericException("User registration is disabled");
    }

    String recaptchakey = RodaCoreFactory.getRodaConfiguration()
      .getString(RodaConstants.UI_GOOGLE_RECAPTCHA_CODE_PROPERTY);

    if (captcha != null) {
      RecaptchaUtils
        .recaptchaVerify(RodaCoreFactory.getRodaConfiguration().getString(RECAPTCHA_CODE_SECRET_PROPERTY, ""), captcha);
    } else if (StringUtils.isNotBlank(recaptchakey)) {
      throw new RecaptchaException("The Captcha can not be null.");
    }

    HttpServletRequest request = getThreadLocalRequest();
    String ipAddress = request.getRemoteAddr();
    String servletPath = retrieveServletUrl(request);
    user.setIpAddress(ipAddress);

    return UserManagement.registerUser(user, password, extra, localeString, servletPath);
  }

  @Override
  public User createUser(User newUser, SecureString password, UserExtraBundle extra)
    throws AuthorizationDeniedException, NotFoundException, GenericException, AlreadyExistsException,
    IllegalOperationException, RequestNotValidException, ValidationException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return UserManagement.createUser(user, newUser, password, extra);
  }

  @Override
  public User updateMyUser(User modifiedUser, SecureString password, UserExtraBundle extra)
    throws AuthorizationDeniedException, NotFoundException, AlreadyExistsException, GenericException,
    IllegalOperationException, ValidationException, RequestNotValidException {
    HttpServletRequest request = getThreadLocalRequest();
    User user = UserUtility.getUser(request);
    User finalModifiedUser = UserManagement.updateMyUser(user, modifiedUser, password, extra);
    UserUtility.setUser(request, finalModifiedUser);
    return finalModifiedUser;
  }

  @Override
  public void updateUser(User modifiedUser, SecureString password, UserExtraBundle extra)
    throws AuthorizationDeniedException, NotFoundException, AlreadyExistsException, GenericException,
    ValidationException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    UserManagement.updateUser(user, modifiedUser, password, extra);
  }

  @Override
  public void deleteUser(String username) throws AuthorizationDeniedException, GenericException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    UserManagement.deleteUser(user, username);
  }

  @Override
  public void createGroup(Group group) throws AuthorizationDeniedException, GenericException, AlreadyExistsException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    UserManagement.createGroup(user, group);
  }

  @Override
  public void updateGroup(Group group) throws AuthorizationDeniedException, GenericException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    UserManagement.updateGroup(user, group);
  }

  @Override
  public void deleteGroup(String groupname) throws AuthorizationDeniedException, GenericException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    UserManagement.deleteGroup(user, groupname);
  }

  @Override
  public Notification sendEmailVerification(final String username, final boolean generateNewToken, String localeString)
    throws GenericException, NotFoundException {
    HttpServletRequest request = getThreadLocalRequest();
    final String servletPath = retrieveServletUrl(request);
    return UserManagement.sendEmailVerification(servletPath, username, generateNewToken, request.getRemoteAddr(),
      localeString);
  }

  @Override
  public void confirmUserEmail(String username, String emailConfirmationToken)
    throws InvalidTokenException, NotFoundException, GenericException {
    UserManagement.confirmUserEmail(username, emailConfirmationToken, getThreadLocalRequest().getRemoteAddr());
  }

  @Override
  public void requestPasswordReset(String usernameOrEmail, String captcha, String localeString) throws GenericException,
    NotFoundException, IllegalOperationException, RecaptchaException, AuthorizationDeniedException {
    boolean userRegistrationDisabled = RodaCoreFactory.getRodaConfiguration()
      .getBoolean(RodaConstants.USER_REGISTRATION_DISABLED, false);
    if (userRegistrationDisabled) {
      throw new GenericException("User registration is disabled");
    }

    if (captcha != null) {
      RecaptchaUtils
        .recaptchaVerify(RodaCoreFactory.getRodaConfiguration().getString(RECAPTCHA_CODE_SECRET_PROPERTY, ""), captcha);
    }
    HttpServletRequest request = getThreadLocalRequest();
    String servletPath = retrieveServletUrl(request);
    UserManagement.requestPasswordReset(servletPath, usernameOrEmail, localeString, request.getRemoteAddr());
  }

  @Override
  public void resetUserPassword(String username, SecureString password, String resetPasswordToken)
    throws InvalidTokenException, IllegalOperationException, NotFoundException, GenericException,
    AuthorizationDeniedException {
    UserManagement.resetUserPassword(username, password, resetPasswordToken, getThreadLocalRequest().getRemoteAddr());
  }

  @Override
  public UserExtraBundle retrieveUserExtraBundle(String name)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());

    UserExtraBundle ret;

    if (user.getName().equals(name)) {
      ret = UserManagement.retrieveOwnUserExtraBundle(user);
    } else {
      ret = UserManagement.retrieveUserExtraBundle(user, name);
    }

    return ret;
  }

  @Override
  public UserExtraBundle retrieveDefaultExtraBundle() {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return UserManagement.retrieveDefaultExtraBundle(user);
  }

  @Override
  public void deleteRODAMembers(SelectedItems<RODAMember> members)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    UserManagement.deleteMembers(user, members);
  }

  @Override
  public void changeActiveRODAMembers(SelectedItems<RODAMember> members, boolean active)
    throws AuthorizationDeniedException, AlreadyExistsException, NotFoundException, GenericException,
    RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    UserManagement.changeActiveMembers(user, members, active);
  }
}
