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
import org.roda.core.data.exceptions.UserAlreadyExistsException;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.browse.bundle.UserExtraBundle;
import org.roda.wui.client.management.recaptcha.RecaptchaException;

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

  /**
   * Get a group
   *
   * @param groupname
   *          the group name
   * @return the group
   * @throws RODAException
   */
  public Group getGroup(String groupname) throws RODAException;

  /**
   * Get a user
   *
   * @param username
   *          the user name
   * @return the user
   *
   * @throws RODAException
   */
  public User retrieveUser(String username) throws RODAException;

  /**
   * Register a new user
   *
   * @param user
   *          The user to register
   * @param password
   *          user password
   * @param captcha
   *          the captcha challenge
   * @param localeString
   *          the locale string
   * @return true if passed the challenge, false otherwise
   * @throws GenericException
   * @throws UserAlreadyExistsException
   * @throws EmailAlreadyExistsException
   * @throws RecaptchaException
   */
  public User registerUser(User user, String password, String captcha, UserExtraBundle extra, String localeString)
    throws GenericException, UserAlreadyExistsException, EmailAlreadyExistsException, RecaptchaException;

  /**
   * Create a new user
   *
   * @param user
   *          the user
   * @param password
   *          the user password
   * @return
   * @throws AuthorizationDeniedException
   * @throws NotFoundException
   * @throws EmailAlreadyExistsException
   * @throws UserAlreadyExistsException
   * @throws GenericException
   */
  public User createUser(User user, String password, UserExtraBundle extra)
    throws AuthorizationDeniedException, NotFoundException, EmailAlreadyExistsException, UserAlreadyExistsException,
    GenericException, IllegalOperationException;

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
  public void updateUser(User user, String password, UserExtraBundle extra)
    throws AuthorizationDeniedException, NotFoundException, AlreadyExistsException, GenericException;

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
  public void updateMyUser(User user, String password, UserExtraBundle extra) throws AuthorizationDeniedException,
    NotFoundException, AlreadyExistsException, GenericException, IllegalOperationException;

  /**
   * Try to remove a user, if user cannot be removed it will be deactivated
   *
   * @param username
   *          the user name
   * @return true if user was removed, false if it was only deactivated
   * @throws AuthorizationDeniedException
   * @throws GenericException
   */
  public void deleteUser(String username) throws AuthorizationDeniedException, GenericException;

  /**
   * Create a group
   *
   * @param group
   *          the new group
   * @throws AuthorizationDeniedException
   * @throws GenericException
   * @throws AlreadyExistsException
   */
  public void createGroup(Group group) throws AuthorizationDeniedException, GenericException, AlreadyExistsException;

  /**
   * Modify a group
   *
   * @param group
   *          the modified group
   * @throws AuthorizationDeniedException
   * @throws GenericException
   * @throws NotFoundException
   */
  public void updateGroup(Group group) throws AuthorizationDeniedException, GenericException, NotFoundException;

  /**
   * Remove a group
   *
   * @param groupname
   *          the group name
   * @throws AuthorizationDeniedException
   * @throws GenericException
   */
  public void deleteGroup(String groupname) throws AuthorizationDeniedException, GenericException;

  /**
   * Send the email challenge to a user email
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
  public Notification sendEmailVerification(String username, boolean generateNewToken, String localeString)
    throws GenericException, NotFoundException;

  /**
   * Confirm a user email. If verified user will become active
   *
   * @param username
   *          the name of the user
   * @param token
   *          the token used in email verification
   * @throws InvalidTokenException
   * @throws NotFoundException
   * @throws GenericException
   */
  public void confirmUserEmail(String username, String emailConfirmationToken)
    throws InvalidTokenException, NotFoundException, GenericException;

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
  public void requestPasswordReset(String usernameOrEmail, String captcha, String localeString)
    throws GenericException, NotFoundException, IllegalOperationException, RecaptchaException;

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
   * @throws GenericException
   */
  public void resetUserPassword(String username, String password, String resetPasswordToken)
    throws InvalidTokenException, IllegalOperationException, NotFoundException, GenericException;

  /**
   * Retrieve the default extra bundle
   *
   * @throws AuthorizationDeniedException
   */
  public UserExtraBundle retrieveDefaultExtraBundle() throws AuthorizationDeniedException;

  public UserExtraBundle retrieveUserExtraBundle(String name)
    throws AuthorizationDeniedException, GenericException, NotFoundException;

}
