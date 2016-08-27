/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.server.management;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.UserUtility;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.exceptions.*;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RodaSimpleUser;
import org.roda.wui.api.controllers.UserManagement;
import org.roda.wui.client.management.UserManagementService;
import org.roda.wui.client.management.recaptcha.RecaptchaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

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

  @SuppressWarnings("unused")
  private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

  private static String RECAPTCHA_CODE_SECRET_PROPERTY = "ui.google.recaptcha.code.secret";

  /**
   * User Management Service implementation constructor
   *
   */
  public UserManagementServiceImpl() {

  }

  @Override
  public Group getGroup(String groupname) throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaSimpleUser user = UserUtility.getUser(getThreadLocalRequest());
    return UserManagement.retrieveGroup(user, groupname);
  }

  @Override
  public List<Group> listAllGroups() throws AuthorizationDeniedException, GenericException {
    RodaSimpleUser user = UserUtility.getUser(getThreadLocalRequest());
    return UserManagement.listAllGroups(user);
  }

  @Override
  public RodaSimpleUser retrieveUser(String username) throws RODAException {
    RodaSimpleUser user = UserUtility.getUser(getThreadLocalRequest());
    return UserManagement.retrieveUser(user, username);
  }

  @Override
  public void registerUser(RodaSimpleUser user, String password, String captcha)
    throws GenericException, UserAlreadyExistsException, EmailAlreadyExistsException, RecaptchaException {
    if (captcha != null) {
      RecaptchaUtils
        .recaptchaVerify(RodaCoreFactory.getRodaConfiguration().getString(RECAPTCHA_CODE_SECRET_PROPERTY, ""), captcha);
    }
    UserManagement.registerUser(user, password);

  }

  @Override
  public RodaSimpleUser createUser(RodaSimpleUser newUser, String password) throws AuthorizationDeniedException, NotFoundException,
    GenericException, EmailAlreadyExistsException, UserAlreadyExistsException, IllegalOperationException {
    RodaSimpleUser user = UserUtility.getUser(getThreadLocalRequest());
    return UserManagement.createUser(user, newUser, password);
  }

  @Override
  public void updateMyUser(RodaSimpleUser modifiedUser, String password) throws AuthorizationDeniedException, NotFoundException,
    AlreadyExistsException, GenericException, IllegalOperationException {
    RodaSimpleUser user = UserUtility.getUser(getThreadLocalRequest());
    UserManagement.updateMyUser(user, modifiedUser, password);
  }

  @Override
  public void updateUser(RodaSimpleUser modifiedUser, String password)
    throws AuthorizationDeniedException, NotFoundException, AlreadyExistsException, GenericException {
    RodaSimpleUser user = UserUtility.getUser(getThreadLocalRequest());
    UserManagement.updateUser(user, modifiedUser, password);
  }

  @Override
  public void deleteUser(String username) throws AuthorizationDeniedException, GenericException {
    RodaSimpleUser user = UserUtility.getUser(getThreadLocalRequest());
    UserManagement.deleteUser(user, username);
  }

  @Override
  public void createGroup(Group group) throws AuthorizationDeniedException, GenericException, AlreadyExistsException {
    RodaSimpleUser user = UserUtility.getUser(getThreadLocalRequest());
    UserManagement.createGroup(user, group);
  }

  @Override
  public void updateGroup(Group group) throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaSimpleUser user = UserUtility.getUser(getThreadLocalRequest());
    UserManagement.updateGroup(user, group);
  }

  @Override
  public void deleteGroup(String groupname) throws AuthorizationDeniedException, GenericException {
    RodaSimpleUser user = UserUtility.getUser(getThreadLocalRequest());
    UserManagement.deleteGroup(user, groupname);
  }

  @Override
  public Long retrieveLogEntriesCount(Filter filter) throws RODAException {
    RodaSimpleUser user = UserUtility.getUser(getThreadLocalRequest());
    return UserManagement.countLogEntries(user, filter);
  }

  @Override
  public IndexResult<LogEntry> findLogEntries(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    RodaSimpleUser user = UserUtility.getUser(getThreadLocalRequest());
    return UserManagement.findLogEntries(user, filter, sorter, sublist, facets);
  }

  @Override
  public LogEntry retrieveLogEntry(String logEntryId)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaSimpleUser user = UserUtility.getUser(getThreadLocalRequest());
    return UserManagement.retrieveLogEntry(user, logEntryId);
  }

  @Override
  public void sendEmailVerification(String username) throws GenericException, NotFoundException {
    String servletPath = retrieveServletUrl(getThreadLocalRequest());
    UserManagement.sendEmailVerification(servletPath, username);
  }

  @Override
  public void confirmUserEmail(String username, String emailConfirmationToken)
    throws InvalidTokenException, NotFoundException, GenericException {
    UserManagement.confirmUserEmail(username, emailConfirmationToken);
  }

  @Override
  public boolean changeUnverifiedEmail(String username, String email) throws RODAException {
    boolean successful = false;
    // FIXME
    // try {
    // org.roda.core.data.v2.User user =
    // RodaClientFactory.getRodaWuiClient().getUserRegistrationService()
    // .modifyUnconfirmedEmail(username, email);
    // if (user.getEmailConfirmationToken() != null &&
    // user.getEmailConfirmationToken().length() > 0) {
    // successful = sendEmailVerification(user);
    // }
    // } catch (RemoteException e) {
    // logger.error("Remote Exception", e);
    // throw RODAClient.parseRemoteException(e);
    // }
    return successful;
  }

  @Override
  public void requestPasswordReset(String usernameOrEmail, String captcha)
    throws GenericException, NotFoundException, IllegalOperationException, RecaptchaException {
    if (captcha != null) {
      RecaptchaUtils
        .recaptchaVerify(RodaCoreFactory.getRodaConfiguration().getString(RECAPTCHA_CODE_SECRET_PROPERTY, ""), captcha);
    }
    String servletPath = retrieveServletUrl(getThreadLocalRequest());
    UserManagement.requestPasswordReset(servletPath, usernameOrEmail);
  }

  @Override
  public void resetUserPassword(String username, String password, String resetPasswordToken)
    throws InvalidTokenException, IllegalOperationException, NotFoundException, GenericException {
    UserManagement.resetUserPassword(username, password, resetPasswordToken);
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

}
