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
package org.roda.wui.client.management;

import java.util.List;

import org.roda.core.common.LdapUtilityException;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.exceptions.*;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RodaSimpleUser;
import org.roda.wui.client.management.recaptcha.RecaptchaException;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Luis Faria
 *
 */
public interface UserManagementServiceAsync {

  /**
   * Get a group
   *
   * @param groupname
   *          the group name
   * @return the group
   * @throws RODAException
   */
  void getGroup(String groupname, AsyncCallback<Group> callback);

  void listAllGroups(AsyncCallback<List<Group>> callback);

  /**
   * Get a user
   *
   * @param username
   *          the user name
   * @return the user
   *
   * @throws RODAException
   */
  void retrieveUser(String username, AsyncCallback<RodaSimpleUser> callback);

  /**
   * Register a new user
   *
   * @param user
   *          The user to register
   * @param password
   *          user password
   * @param captcha
   *          the captcha challenge
   * @return true if passed the challenge, false otherwise
   * @throws GenericException
   * @throws UserAlreadyExistsException
   * @throws EmailAlreadyExistsException
   * @throws RecaptchaException
   */
  public void registerUser(RodaSimpleUser user, String password, String captcha, AsyncCallback<Void> callback);

  void createUser(RodaSimpleUser user, String password, AsyncCallback<RodaSimpleUser> callback);

  /**
   * Modify a user
   *
   * @param user
   *          the modified users
   * @param password
   *          the new user password, or null to stay the same
   * @throws AuthorizationDeniedException
   * @throws NotFoundException
   * @throws AlreadyExistsException
   * @throws GenericException
   */
  public void updateUser(RodaSimpleUser user, String password, AsyncCallback<Void> callback);

  /**
   * Modify the authenticated user
   *
   * @param user
   *          the modified user
   * @param password
   *          the user password if modified, or null if it remains the same
   * @throws AuthorizationDeniedException
   * @throws NotFoundException
   * @throws AlreadyExistsException
   * @throws GenericException
   * @throws IllegalOperationException
   */
  public void updateMyUser(RodaSimpleUser user, String password, AsyncCallback<Void> callback);

  /**
   * Try to remove a user, if user cannot be removed it will be deactivated
   *
   * @param username
   *          the user name
   * @return true if user was removed, false if it was only deactivated
   * @throws AuthorizationDeniedException
   * @throws GenericException
   */
  public void deleteUser(String username, AsyncCallback<Void> callback);

  /**
   * Create a group
   *
   * @param group
   *          the new group
   * @throws AuthorizationDeniedException
   * @throws GenericException
   * @throws AlreadyExistsException
   */
  public void createGroup(Group group, AsyncCallback<Void> callback);

  /**
   * Modify a group
   *
   * @param group
   *          the modified group
   * @throws AuthorizationDeniedException
   * @throws GenericException
   * @throws NotFoundException
   */
  public void updateGroup(Group group, AsyncCallback<Void> callback);

  /**
   * Remove a group
   *
   * @param groupname
   *          the group name
   * @throws AuthorizationDeniedException
   * @throws GenericException
   */
  public void deleteGroup(String groupname, AsyncCallback<Void> callback);

  /**
   * Get the number log entries
   *
   * @param filter
   * @return
   * @throws RODAException
   */
  public void retrieveLogEntriesCount(Filter filter, AsyncCallback<Long> callback);

  public void findLogEntries(Filter filter, Sorter sorter, Sublist sublist, Facets facets,
    AsyncCallback<IndexResult<LogEntry>> callback);

  void retrieveLogEntry(String logEntryId, AsyncCallback<LogEntry> callback);

  /**
   * Resend the email challenge to a user email
   *
   * @param username
   *          the name of the user
   * @throws GenericException
   * @throws NotFoundException
   */
  public void sendEmailVerification(String username, AsyncCallback<Void> callback);

  /**
   * Verify a user email. If verified user will become active
   *
   * @param username
   *          the name of the user
   * @param token
   *          the token used in email verification
   * @throws InvalidTokenException
   * @throws LdapUtilityException
   * @throws NotFoundException
   */
  public void confirmUserEmail(String username, String emailConfirmationToken, AsyncCallback<Void> callback);

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
   *          the captcha challenge answer
   * @throws GenericException
   * @throws NotFoundException
   * @throws IllegalOperationException
   * @throws LdapUtilityException
   * @throws RecaptchaException
   */
  public void requestPasswordReset(String usernameOrEmail, String captcha, AsyncCallback<Void> callback);

  /**
   * Reset a user password
   *
   * @param username
   *          the user name
   * @param password
   *          the new password
   * @param resetPasswordToken
   *          the password token that was sent by email on
   *          requestPasswordReset(String, String)
   * @throws InvalidTokenException
   * @throws IllegalOperationException
   * @throws LdapUtilityException
   * @throws NotFoundException
   */
  public void resetUserPassword(String username, String password, String resetPasswordToken,
    AsyncCallback<Void> callback);

}
