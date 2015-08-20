package pt.gov.dgarq.roda.core.services;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.UserRegistrationHelper;
import pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException;
import pt.gov.dgarq.roda.core.common.IllegalOperationException;
import pt.gov.dgarq.roda.core.common.InvalidTokenException;
import pt.gov.dgarq.roda.core.common.NoSuchUserException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.common.UserAlreadyExistsException;
import pt.gov.dgarq.roda.core.common.UserRegistrationException;
import pt.gov.dgarq.roda.core.data.v2.User;
import pt.gov.dgarq.roda.servlet.LdapUtility;

/**
 * This class implements the User Registration service.
 * 
 * @author Rui Castro
 */
public class UserRegistration extends RODAWebService {

	static final private Logger logger = Logger
			.getLogger(UserRegistration.class);

	private UserRegistrationHelper userRegistrationHelper = null;

	/**
	 * Constructs a new instance of the {@link UserRegistration} service.
	 * 
	 * @throws RODAServiceException
	 */
	public UserRegistration() throws RODAServiceException {

		super();

		String ldapHost = getConfiguration().getString("ldapHost");
		int ldapPort = getConfiguration().getInt("ldapPort");
		String ldapPeopleDN = getConfiguration().getString("ldapPeopleDN");
		String ldapGroupsDN = getConfiguration().getString("ldapGroupsDN");
		String ldapRolesDN = getConfiguration().getString("ldapRolesDN");
		String ldapAdminDN = getConfiguration().getString("ldapAdminDN");
		String ldapAdminPassword = getConfiguration().getString(
				"ldapAdminPassword");
		String ldapPasswordDigestAlgorithm = getConfiguration().getString(
				"ldapPasswordDigestAlgorithm");
		List<String> ldapProtectedUsers = getConfiguration().getList(
				"ldapProtectedUsers");
		List<String> ldapProtectedGroups = getConfiguration().getList(
				"ldapProtectedGroups");

		LdapUtility ldapUtility = new LdapUtility(ldapHost, ldapPort,
				ldapPeopleDN, ldapGroupsDN, ldapRolesDN, ldapAdminDN,
				ldapAdminPassword, ldapPasswordDigestAlgorithm,
				ldapProtectedUsers, ldapProtectedGroups);

		List<String> userDefaultGroups = getConfiguration().getList(
				"userDefaultGroups");

		this.userRegistrationHelper = new UserRegistrationHelper(ldapUtility,
				userDefaultGroups);

		logger.info(getClass().getSimpleName() + " initialised OK");
	}

	/**
	 * Registers a new {@link User}. The new {@link User} is inactive and has an
	 * activation token that can be used to active the {@link User}. The
	 * activation token should be sent to the specified email address.
	 * 
	 * @param user
	 *            the {@link User} to add.
	 * @param password
	 *            the new {@link User} password.
	 * 
	 * @return the newly created {@link User}.
	 * 
	 * @throws UserAlreadyExistsException
	 *             if a {@link User} with the same name or email address already
	 *             exists.
	 * @throws EmailAlreadyExistsException
	 *             if the {@link User}'s email is already used.
	 * @throws UserRegistrationException
	 *             if something goes wrong with the registration of the new
	 *             user.
	 */
	public User registerUser(User user, String password)
			throws UserAlreadyExistsException, EmailAlreadyExistsException,
			UserRegistrationException {

		Date start = new Date();
		User result = this.userRegistrationHelper.registerUser(user, password);
		long duration = new Date().getTime() - start.getTime();

		registerAction("UserRegistration.registerUser", new String[] { "user",
				user.toString() },
				"User %username% called method UserRegistration.registerUser("
						+ user + ")", duration);

		return result;
	}

	/**
	 * Returns a {@link User} whose email was not confirmed yet.
	 * 
	 * @param username
	 *            the name of the {@link User}.
	 * 
	 * @return a {@link User}.
	 * 
	 * @throws NoSuchUserException
	 *             if the specified username doesn't exist.
	 * @throws IllegalOperationException
	 *             if the {@link User} is already active or the email
	 *             confirmation token doesn't exist.
	 * @throws UserRegistrationException
	 *             if anything else goes wrong with the operation.
	 */
	public User getUnconfirmedUser(String username)
			throws IllegalOperationException, NoSuchUserException,
			UserRegistrationException {

		Date start = new Date();
		User user = this.userRegistrationHelper.getUnconfirmedUser(username);
		long duration = new Date().getTime() - start.getTime();

		registerAction("UserRegistration.getUnconfirmedUser", new String[] {
				"username", username },
				"User %username% called method UserRegistration.getUnconfirmedUser("
						+ username + ")", duration);

		return user;
	}

	/**
	 * Modify the email of a {@link User} whose email was not confirmed yet.
	 * 
	 * @param username
	 *            the name of the {@link User}.
	 * @param email
	 *            the new email address for the specified {@link User}.
	 * 
	 * @return the modified {@link User}.
	 * 
	 * @throws NoSuchUserException
	 *             if the specified username doesn't exist.
	 * @throws IllegalOperationException
	 *             if the {@link User} is already active or the email
	 *             confirmation token doesn't exist.
	 * @throws UserRegistrationException
	 *             if anything else goes wrong with the operation.
	 */
	public User modifyUnconfirmedEmail(String username, String email)
			throws IllegalOperationException, NoSuchUserException,
			UserRegistrationException {

		Date start = new Date();
		User user = this.userRegistrationHelper.modifyUnconfirmedEmail(
				username, email);
		long duration = new Date().getTime() - start.getTime();

		registerAction("UserRegistration.modifyUnconfirmedEmail", new String[] {
				"username", username, "email", email },
				"User %username% called method UserRegistration.modifyUnconfirmedEmail("
						+ username + ", " + email + ")", duration);

		return user;
	}

	/**
	 * Confirms the {@link User} email using the token supplied at register time
	 * and activate the {@link User}.
	 * <p>
	 * The <code>username</code> and <code>email</code> are used to identify the
	 * user. One of them can be <code>null</code>, but not both at the same
	 * time.
	 * </p>
	 * 
	 * @param username
	 *            the name of the {@link User}.
	 * @param email
	 *            the email address of the {@link User}.
	 * @param emailConfirmationToken
	 * 
	 * @return the {@link User} whose email has been confirmed.
	 * 
	 * @throws NoSuchUserException
	 *             if the username and email don't exist.
	 * @throws InvalidTokenException
	 *             if the specified token doesn't exist, has already expired or
	 *             it doesn't correspond to the stored token.
	 * @throws IllegalArgumentException
	 *             if username and email are <code>null</code>.
	 * @throws UserRegistrationException
	 *             if something else goes wrong.
	 */
	public User confirmUserEmail(String username, String email,
			String emailConfirmationToken) throws NoSuchUserException,
			InvalidTokenException, IllegalArgumentException,
			UserRegistrationException {

		Date start = new Date();
		User user = this.userRegistrationHelper.confirmUserEmail(username,
				email, emailConfirmationToken);
		long duration = new Date().getTime() - start.getTime();

		registerAction("UserRegistration.confirmUserEmail", new String[] {
				"username", username, "email", email, "emailConfirmationToken",
				emailConfirmationToken },
				"User %username% called method UserRegistration.confirmUserEmail("
						+ username + ", " + email + ", "
						+ emailConfirmationToken + ")", duration);

		return user;
	}

	/**
	 * Generate a password reset token for the {@link User} with the given
	 * username or email.
	 * <p>
	 * The <code>username</code> and <code>email</code> are used to identify the
	 * user. One of them can be <code>null</code>, but not both at the same
	 * time.
	 * </p>
	 * 
	 * @param username
	 *            the username of the {@link User} for whom the password needs
	 *            to be reset.
	 * 
	 * @param email
	 *            the email of the {@link User} for whom the password needs to
	 *            be reset.
	 * 
	 * @return the {@link User} with the password reset token and expiration
	 *         date.
	 * 
	 * @throws NoSuchUserException
	 *             if username or email doesn't correspond to any registered
	 *             {@link User}.
	 * @throws IllegalOperationException
	 *             if email corresponds to a protected {@link User}.
	 * @throws UserRegistrationException
	 *             if something goes wrong with the operation.
	 */
	public User requestPasswordReset(String username, String email)
			throws NoSuchUserException, IllegalOperationException,
			UserRegistrationException {

		Date start = new Date();
		User user = this.userRegistrationHelper.requestPasswordReset(username,
				email);
		long duration = new Date().getTime() - start.getTime();

		registerAction("UserRegistration.requestPasswordReset", new String[] {
				"username", username, "email", email },
				"User %username% called method UserRegistration.requestPasswordReset("
						+ username + ", " + email + ")", duration);

		return user;
	}

	/**
	 * Reset {@link User}'s password given a previously generated token.
	 * 
	 * @param username
	 *            the {@link User}'s username.
	 * @param password
	 *            the {@link User}'s password.
	 * @param resetPasswordToken
	 *            the token to reset {@link User}'s password.
	 * 
	 * @return the modified {@link User}.
	 * 
	 * @throws NoSuchUserException
	 *             if a {@link User} with the same name already exists.
	 * @throws InvalidTokenException
	 *             if the specified token doesn't exist, has already expired or
	 *             it doesn't correspond to the stored token.
	 * @throws IllegalOperationException
	 *             if the username corresponds to a protected {@link User}.
	 * @throws UserRegistrationException
	 *             if something goes wrong with the operation.
	 */
	public User resetUserPassword(String username, String password,
			String resetPasswordToken) throws NoSuchUserException,
			InvalidTokenException, IllegalOperationException,
			UserRegistrationException {

		Date start = new Date();
		User user = this.userRegistrationHelper.resetUserPassword(username,
				password, resetPasswordToken);
		long duration = new Date().getTime() - start.getTime();

		registerAction("UserRegistration.resetUserPassword", new String[] {
				"username", username, "password", "*",
				"emailConfirmationToken", resetPasswordToken },
				"User %username% called method UserRegistration.resetUserPassword("
						+ username + ", " + "*" + ", " + resetPasswordToken
						+ ")", duration);

		return user;
	}

}
