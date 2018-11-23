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
import org.roda.wui.client.browse.bundle.UserExtraBundle;
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

  /**
   * Get a user
   *
   * @param username
   *          the user name
   * @return the user
   *
   * @throws RODAException
   */
  void retrieveUser(String username, AsyncCallback<User> callback);

  /**
   * Register a new user
   *
   * @param user
   *          The user to register
   * @param password
   *          user password
   * @param captcha
   *          the captcha challenge
   * @param extra
   *          the extra user fields
   * @param localeString
   *          the locale string
   * @return true if passed the challenge, false otherwise
   * @throws GenericException
   * @throws UserAlreadyExistsException
   * @throws EmailAlreadyExistsException
   * @throws RecaptchaException
   */
  public void registerUser(User user, String password, String captcha, UserExtraBundle extra, String localeString,
    AsyncCallback<User> callback);

  void createUser(User user, String password, UserExtraBundle extra, AsyncCallback<User> callback);

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
  public void updateUser(User user, String password, UserExtraBundle extra, AsyncCallback<Void> callback);

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
  public void updateMyUser(User user, String password, UserExtraBundle extra, AsyncCallback<User> callback);

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
   * Resend the email challenge to a user email
   *
   * @param username
   *          the name of the user
   * @param generateNewToken
   *          generate a new token before sending the email?
   * @param localeString
   *          the locale string
   * @throws GenericException
   * @throws NotFoundException
   */
  public void sendEmailVerification(String username, boolean generateNewToken, String localeString,
    AsyncCallback<Notification> callback);

  /**
   * Verify a user email. If verified user will become active
   *
   * @param username
   *          the name of the user
   * @param emailConfirmationToken
   *          the token used in email verification
   * @throws InvalidTokenException
   * @throws NotFoundException
   */
  public void confirmUserEmail(String username, String emailConfirmationToken, AsyncCallback<Void> callback);

  /**
   * Request to reset the password. An email will be sent to the user with the
   * password reset token.
   *
   * @param usernameOrEmail
   *          the user name or email
   * @param captcha
   *          the captcha challenge answer
   * @param localeString
   *          the locale string
   * @throws GenericException
   * @throws NotFoundException
   * @throws IllegalOperationException
   * @throws RecaptchaException
   */
  public void requestPasswordReset(String usernameOrEmail, String captcha, String localeString,
    AsyncCallback<Void> callback);

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
   * @throws NotFoundException
   */
  public void resetUserPassword(String username, String password, String resetPasswordToken,
    AsyncCallback<Void> callback);

  void retrieveDefaultExtraBundle(AsyncCallback<UserExtraBundle> asyncCallback);

  void retrieveUserExtraBundle(String name, AsyncCallback<UserExtraBundle> asyncCallback);

  void deleteRODAMembers(SelectedItems<RODAMember> members, AsyncCallback<Void> async);

  void changeActiveRODAMembers(SelectedItems<RODAMember> members, boolean active, AsyncCallback<Void> async);
}
