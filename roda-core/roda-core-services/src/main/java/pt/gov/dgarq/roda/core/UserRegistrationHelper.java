package pt.gov.dgarq.roda.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.w3c.util.DateParser;

import pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException;
import pt.gov.dgarq.roda.core.common.IllegalOperationException;
import pt.gov.dgarq.roda.core.common.InvalidTokenException;
import pt.gov.dgarq.roda.core.common.NoSuchUserException;
import pt.gov.dgarq.roda.core.common.UserAlreadyExistsException;
import pt.gov.dgarq.roda.core.common.UserRegistrationException;
import pt.gov.dgarq.roda.core.data.v2.User;
import pt.gov.dgarq.roda.servlet.LdapUtility;
import pt.gov.dgarq.roda.servlet.LdapUtilityException;

/**
 * @author Rui Castro
 */
public class UserRegistrationHelper {

	static final private Logger logger = Logger
			.getLogger(UserRegistrationHelper.class);

	private LdapUtility ldapUtility = null;

	/**
	 * List of groups that are assigned to a user at registration time.
	 */
	private List<String> userDefaultGroups = new ArrayList<String>();

	/**
	 * Constructs a new {@link UserRegistrationHelper} with the given
	 * {@link LdapUtility}.
	 * 
	 * @param ldapUtility
	 * @param userDefaultGroups
	 *            list of groups that are assigned to a registered user.
	 */
	public UserRegistrationHelper(LdapUtility ldapUtility,
			List<String> userDefaultGroups) {
		this.ldapUtility = ldapUtility;

		this.userDefaultGroups.clear();
		if (userDefaultGroups != null) {
			this.userDefaultGroups.addAll(userDefaultGroups);
			logger.debug("user default groups: " + this.userDefaultGroups);
		}
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

		// A new registered user, is always inactive.
		user.setActive(false);

		// Generate an email verification token with 1 day expiration date.
		UUID uuidToken = UUID.randomUUID();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		String isoDateNoMillis = DateParser.getIsoDateNoMillis(calendar
				.getTime());

		user.setEmailConfirmationToken(uuidToken.toString());
		user.setEmailConfirmationTokenExpirationDate(isoDateNoMillis);

		// New users have no roles and have only the default groups defined in
		// the configuration file.
		user.setDirectRoles(new HashSet<String>());
		user.setDirectGroups(new HashSet<String>(userDefaultGroups));

		try {

			User newUser = this.ldapUtility.addUser(user);

			this.ldapUtility.setUserPassword(newUser.getName(), password);

			return newUser;

		} catch (IllegalOperationException e) {

			logger.debug("Throwing UserRegistrationException - "
					+ e.getMessage(), e);
			throw new UserRegistrationException(
					"Error setting user password - " + e.getMessage(), e);

		} catch (NoSuchUserException e) {

			logger.debug("Throwing UserRegistrationException - "
					+ e.getMessage(), e);
			throw new UserRegistrationException(
					"Error setting user password - " + e.getMessage(), e);

		} catch (LdapUtilityException e) {

			logger.debug("Throwing UserRegistrationException - "
					+ e.getMessage(), e);
			throw new UserRegistrationException(e.getMessage(), e);

		}

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

		try {

			User user = this.ldapUtility.getUser(username);

			if (user == null) {

				logger.debug("User " + username
						+ " doesn't exist. Throwing NoSuchUserException.");

				throw new NoSuchUserException("User " + username
						+ " doesn't exist.");

			} else if (user.isActive()
					|| user.getEmailConfirmationToken() == null) {

				logger
						.debug("User "
								+ username
								+ " is already active or email confirmation token doesn't exist.");

				throw new IllegalOperationException(
						"User "
								+ username
								+ " is already active or email confirmation token doesn't exist.");
			} else {

				return user;

			}

		} catch (LdapUtilityException e) {

			logger.debug("Throwing UserRegistrationException - "
					+ e.getMessage(), e);

			throw new UserRegistrationException(e.getMessage(), e);
		}
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

		User unconfirmedUser = getUnconfirmedUser(username);

		unconfirmedUser.setEmail(email);

		// Generate an email verification token with 1 day expiration date.
		UUID uuidToken = UUID.randomUUID();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		String isoDateNoMillis = DateParser.getIsoDateNoMillis(calendar
				.getTime());

		unconfirmedUser.setEmailConfirmationToken(uuidToken.toString());
		unconfirmedUser
				.setEmailConfirmationTokenExpirationDate(isoDateNoMillis);

		try {

			return this.ldapUtility.modifyUser(unconfirmedUser);

		} catch (LdapUtilityException e) {
			logger.debug("Throwing UserRegistrationException - "
					+ e.getMessage(), e);

			throw new UserRegistrationException(e.getMessage(), e);
		}
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

		try {

			return ldapUtility.confirmUserEmail(username, email,
					emailConfirmationToken);

		} catch (LdapUtilityException e) {
			throw new UserRegistrationException(e.getMessage(), e);
		}

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

		try {

			return ldapUtility.requestPasswordReset(username, email);

		} catch (LdapUtilityException e) {
			throw new UserRegistrationException(e.getMessage(), e);
		}
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

		try {

			return ldapUtility.resetUserPassword(username, password,
					resetPasswordToken);

		} catch (LdapUtilityException e) {
			throw new UserRegistrationException(e.getMessage(), e);
		}

	}

}
