/**
 * 
 */
package org.roda.wui.management.user.client;

import java.util.List;

import org.roda.core.common.RODAException;
import org.roda.core.data.adapter.ContentAdapter;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.v2.Group;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.LogEntry;
import org.roda.core.data.v2.RODAMember;
import org.roda.core.data.v2.RodaGroup;
import org.roda.core.data.v2.User;
import org.roda.wui.common.client.PrintReportException;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Luis Faria
 * 
 */
public interface UserManagementServiceAsync {

  public void getMemberCount(Filter filter, AsyncCallback<Long> callback);

  void findMembers(Filter filter, Sorter sorter, Sublist sublist, Facets facets, String localeString,
    AsyncCallback<IndexResult<RODAMember>> callback);

  void getGroup(String groupname, AsyncCallback<RodaGroup> callback);

  void listAllGroups(AsyncCallback<List<Group>> callback);

  void getUser(String username, AsyncCallback<User> callback);

  /**
   * Create a new user
   * 
   * @param user
   *          the user
   * @param password
   *          the user password
   * @throws RODAException
   */
  public void createUser(User user, String password, AsyncCallback<Void> callback);

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
  public void editUser(User user, String password, AsyncCallback<Void> callback);

  /**
   * Modify the authenticated user
   * 
   * @param user
   *          the modified user
   * @param password
   *          the user password if modified, or null if it remains the same
   * @throws RODAException
   */
  public void editMyUser(User user, String password, AsyncCallback<Void> callback);

  /**
   * Create a group
   * 
   * @param group
   *          the new group
   * @throws RODAException
   */
  public void createGroup(Group group, AsyncCallback<Void> callback);

  /**
   * Modify a group
   * 
   * @param group
   *          the modified group
   * @throws RODAException
   */
  public void editGroup(Group group, AsyncCallback<Void> callback);

  /**
   * Try to remove a user, if user cannot be removed it will be deactivated
   * 
   * @param username
   *          the user name
   * @return true if user was removed, false if it was only deactivated
   * @throws RODAException
   */
  public void removeUser(String username, AsyncCallback<Boolean> callback);

  /**
   * Remove a group
   * 
   * @param groupname
   *          the group name
   * @throws RODAException
   */
  public void removeGroup(String groupname, AsyncCallback<Void> callback);

  /**
   * Get the number log entries
   * 
   * @param filter
   * @return
   * @throws RODAException
   */
  public void getLogEntriesCount(Filter filter, AsyncCallback<Long> callback);

  public void findLogEntries(Filter filter, Sorter sorter, Sublist sublist, Facets facets,
    AsyncCallback<IndexResult<LogEntry>> callback);

  void retrieveLogEntry(String logEntryId, AsyncCallback<LogEntry> callback);

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
  public void register(User user, String password, String captcha, AsyncCallback<Boolean> callback);

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
  public void verifyemail(String username, String token, AsyncCallback<Boolean> callback);

  /**
   * Resend the email chalenge to a user email
   * 
   * @param username
   *          the name of the user
   * @return true if email resent, false otherwise
   * @throws RODAException
   * 
   */
  public void resendEmailVerification(String username, AsyncCallback<Boolean> callback);

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
  public void changeUnverifiedEmail(String username, String email, AsyncCallback<Boolean> callback);

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
  public void requestPassordReset(String usernameOrEmail, String captcha, AsyncCallback<Boolean> callback);

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
  public void resetPassword(String username, String resetPasswordToken, String newPassword,
    AsyncCallback<Void> callback);

  /**
   * Set user log report info
   * 
   * @param adapter
   * @param localeString
   * @throws PrintReportException
   */
  public void setUserLogReportInfo(ContentAdapter adapter, String localeString, AsyncCallback<Void> callback);

}
