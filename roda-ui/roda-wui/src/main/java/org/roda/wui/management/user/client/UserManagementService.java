/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.management.user.client;

import java.util.List;

import org.roda.core.data.adapter.ContentAdapter;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.EmailAlreadyExistsException;
import org.roda.core.data.common.GroupAlreadyExistsException;
import org.roda.core.data.common.IllegalOperationException;
import org.roda.core.data.common.UserAlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.RodaGroup;
import org.roda.core.data.v2.user.User;
import org.roda.wui.common.client.PrintReportException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * @author Luis Faria
 * 
 */
public interface UserManagementService extends RemoteService {

  /**
   * Service URI path
   */
  public static final String SERVICE_URI = "UserManagementService";

  /**
   * Factory utility
   */
  public static class Util {

    /**
     * Get a new instance of the service
     * 
     * @return the instance
     */
    public static UserManagementServiceAsync getInstance() {

      UserManagementServiceAsync instance = (UserManagementServiceAsync) GWT.create(UserManagementService.class);
      ServiceDefTarget target = (ServiceDefTarget) instance;
      target.setServiceEntryPoint(GWT.getModuleBaseURL() + SERVICE_URI);
      return instance;
    }
  }

  public Long getMemberCount(Filter filter)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException;

  public IndexResult<RODAMember> findMembers(Filter filter, Sorter sorter, Sublist sublist, Facets facets,
    String localeString) throws AuthorizationDeniedException, GenericException, RequestNotValidException;

  /**
   * Get a group
   * 
   * @param groupname
   *          the group name
   * @return the group
   * @throws RODAException
   */
  public RodaGroup getGroup(String groupname) throws RODAException;

  public List<Group> listAllGroups() throws AuthorizationDeniedException, GenericException;

  /**
   * Get a user
   * 
   * @param username
   *          the user name
   * @return the user
   * 
   * @throws RODAException
   */
  public User getUser(String username) throws RODAException;

  /**
   * Create a new user
   * 
   * @param user
   *          the user
   * @param password
   *          the user password
   * @throws RODAException
   * @throws EmailAlreadyExistsException
   * @throws UserAlreadyExistsException
   * @throws NoSuchUserException
   * @throws IllegalOperationException
   */
  public void createUser(User user, String password) throws RODAException, UserAlreadyExistsException,
    EmailAlreadyExistsException, IllegalOperationException, NotFoundException;

  /**
   * Modify a user
   * 
   * @param user
   *          the modified users
   * @param password
   *          the new user password, or null to stay the same
   * @throws RODAException
   * 
   */
  public void editUser(User user, String password) throws RODAException;

  /**
   * Modify the authenticated user
   * 
   * @param user
   *          the modified user
   * @param password
   *          the user password if modified, or null if it remains the same
   * @throws RODAException
   * @throws IllegalOperationException
   * @throws NoSuchUserException
   * @throws EmailAlreadyExistsException
   */
  public void editMyUser(User user, String password)
    throws RODAException, EmailAlreadyExistsException, NotFoundException, IllegalOperationException;

  /**
   * Create a group
   * 
   * @param group
   *          the new group
   * @throws RODAException
   * @throws GroupAlreadyExistsException
   */
  public void createGroup(Group group) throws RODAException, GroupAlreadyExistsException;

  /**
   * Modify a group
   * 
   * @param group
   *          the modified group
   * @throws RODAException
   * @throws IllegalOperationException
   * @throws NoSuchGroupException
   */
  public void editGroup(Group group) throws RODAException, NotFoundException, IllegalOperationException;

  /**
   * Try to remove a user, if user cannot be removed it will be deactivated
   * 
   * @param username
   *          the user name
   * @return true if user was removed, false if it was only deactivated
   * @throws RODAException
   * @throws IllegalOperationException
   * @throws NoSuchUserException
   */
  public boolean removeUser(String username) throws RODAException, NotFoundException, IllegalOperationException;

  /**
   * Remove a group
   * 
   * @param groupname
   *          the group name
   * @throws RODAException
   * @throws IllegalOperationException
   */
  public void removeGroup(String groupname) throws RODAException, IllegalOperationException;

  /**
   * Get the number log entries
   * 
   * @param filter
   * @return
   * @throws RODAException
   */
  public Long getLogEntriesCount(Filter filter) throws RODAException;

  public IndexResult<LogEntry> findLogEntries(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException;

  public LogEntry retrieveLogEntry(String logEntryId)
    throws AuthorizationDeniedException, GenericException, NotFoundException;

  /**
   * Register a new user
   * 
   * @param user
   *          The user to register
   * @param password
   *          user password
   * @param captcha
   *          the captcha chalenge
   * @return true if passed the chalenge, false otherwise
   * @throws RODAException
   */
  public boolean register(User user, String password, String captcha) throws RODAException;

  /**
   * Verify a user email. If verified user will become active
   * 
   * @param username
   *          the name of the user
   * @param token
   *          the token used in email verification
   * @return true if email verified, false otherwise
   * @throws RODAException
   * 
   */
  public boolean verifyemail(String username, String token) throws RODAException;

  /**
   * Resend the email chalenge to a user email
   * 
   * @param username
   *          the name of the user
   * @return true if email resent, false otherwise
   * @throws RODAException
   * 
   */
  public boolean resendEmailVerification(String username) throws RODAException;

  /**
   * Change the email of a user that is still not active due to a email
   * unverified
   * 
   * @param username
   *          the name of the user
   * @param email
   *          the new email
   * @return true if email was successfully changed, false otherwise
   * @throws RODAException
   * 
   */
  public boolean changeUnverifiedEmail(String username, String email) throws RODAException;

  /**
   * Request to reset the password. An email will be sent to the user with the
   * password reset token.
   * 
   * @param usernameOrEmail
   *          the user name or email
   * @param captcha
   *          the capcha chalenge answer
   * @return true if the user passed the chalenge, false otherwise
   * @throws RODAException
   */
  public boolean requestPassordReset(String usernameOrEmail, String captcha) throws RODAException;

  /**
   * Reset a user password
   * 
   * @param username
   *          the user name
   * @param resetPasswordToken
   *          the password token that was sent by email on
   *          requestPasswordReset(String, String)
   * @param newPassword
   *          the new password
   * @throws RODAException
   * 
   */
  public void resetPassword(String username, String resetPasswordToken, String newPassword) throws RODAException;

  /**
   * Set user log report info
   * 
   * @param adapter
   * @param localeString
   * @throws PrintReportException
   */
  public void setUserLogReportInfo(ContentAdapter adapter, String localeString) throws PrintReportException;

}
