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
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.controllers.UserManagement;
import org.roda.wui.client.management.UserManagementService;
import org.roda.wui.client.management.recaptcha.RecaptchaException;
import org.roda.wui.common.I18nUtility;
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
  public Long getMemberCount(Filter filter)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return UserManagement.countMembers(user, filter);
  }

  @Override
  public IndexResult<RODAMember> findMembers(Filter filter, Sorter sorter, Sublist sublist, Facets facets,
    String localeString) throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    IndexResult<RODAMember> result = UserManagement.findMembers(user, filter, sorter, sublist, facets);
    return I18nUtility.translate(result, RODAMember.class, localeString);
  }

  @Override
  public Group getGroup(String groupname) throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return UserManagement.retrieveGroup(user, groupname);
  }

  @Override
  public List<Group> listAllGroups() throws AuthorizationDeniedException, GenericException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return UserManagement.listAllGroups(user);
  }

  @Override
  public User getUser(String username) throws RODAException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return UserManagement.retrieveUser(user, username);
  }

  @Override
  public void registerUser(User user, String password, String captcha)
    throws GenericException, UserAlreadyExistsException, EmailAlreadyExistsException, RecaptchaException {
    if (captcha != null) {
      RecaptchaUtils
        .recaptchaVerify(RodaCoreFactory.getRodaConfiguration().getString(RECAPTCHA_CODE_SECRET_PROPERTY, ""), captcha);
    }
    UserManagement.registerUser(user, password);

  }

  @Override
  public User addUser(User newUser, String password) throws AuthorizationDeniedException, NotFoundException,
    GenericException, EmailAlreadyExistsException, UserAlreadyExistsException, IllegalOperationException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return UserManagement.addUser(user, newUser, password);
  }

  @Override
  public void editMyUser(User modifiedUser, String password) throws AuthorizationDeniedException, NotFoundException,
    AlreadyExistsException, GenericException, IllegalOperationException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    UserManagement.modifyMyUser(user, modifiedUser, password);
  }

  @Override
  public void modifyUser(User modifiedUser, String password)
    throws AuthorizationDeniedException, NotFoundException, AlreadyExistsException, GenericException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    UserManagement.modifyUser(user, modifiedUser, password);
  }

  @Override
  public void removeUser(String username) throws AuthorizationDeniedException, GenericException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    UserManagement.removeUser(user, username);
  }

  @Override
  public void addGroup(Group group) throws AuthorizationDeniedException, GenericException, AlreadyExistsException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    UserManagement.addGroup(user, group);
  }

  @Override
  public void modifyGroup(Group group) throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    UserManagement.modifyGroup(user, group);
  }

  @Override
  public void removeGroup(String groupname) throws AuthorizationDeniedException, GenericException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    UserManagement.removeGroup(user, groupname);
  }

  @Override
  public Long getLogEntriesCount(Filter filter) throws RODAException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return UserManagement.countLogEntries(user, filter);
  }

  @Override
  public IndexResult<LogEntry> findLogEntries(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return UserManagement.findLogEntries(user, filter, sorter, sublist, facets);
  }

  @Override
  public LogEntry retrieveLogEntry(String logEntryId)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return UserManagement.retrieveLogEntry(user, logEntryId);
  }

  @Override
  public void sendEmailVerification(String username) throws GenericException, NotFoundException {
    String servletPath = getServletUrl(getThreadLocalRequest());
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
    String servletPath = getServletUrl(getThreadLocalRequest());
    UserManagement.requestPasswordReset(servletPath, usernameOrEmail);
  }

  @Override
  public void resetUserPassword(String username, String password, String resetPasswordToken)
    throws InvalidTokenException, IllegalOperationException, NotFoundException, GenericException {
    UserManagement.resetUserPassword(username, password, resetPasswordToken);
  }

  private static String getServletUrl(HttpServletRequest req) {
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
