package pt.gov.dgarq.roda.core.services;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException;
import pt.gov.dgarq.roda.core.common.IllegalOperationException;
import pt.gov.dgarq.roda.core.common.NoSuchUserException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.common.UserEditorException;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.servlet.LdapUtility;
import pt.gov.dgarq.roda.servlet.LdapUtilityException;

/**
 * This class implements the User Editor service.
 * 
 * @author Rui Castro
 */
public class UserEditor extends RODAWebService {

	static final private Logger logger = Logger.getLogger(UserEditor.class);

	private LdapUtility ldapUtility = null;

	/**
	 * Constructs a new instance of the {@link UserEditor} service.
	 * 
	 * @throws RODAServiceException
	 */
	public UserEditor() throws RODAServiceException {

		super();

		String ldapHost = getConfiguration().getString("ldapHost");
		int ldapPort = getConfiguration().getInt("ldapPort");
		String ldapPeopleDN = getConfiguration().getString("ldapPeopleDN");
		String ldapGroupsDN = getConfiguration().getString("ldapGroupsDN");
		String ldapRolesDN = getConfiguration().getString("ldapRolesDN");
		List<String> ldapProtectedUsers = getConfiguration().getList(
				"ldapProtectedUsers");
		List<String> ldapProtectedGroups = getConfiguration().getList(
				"ldapProtectedGroups");

		this.ldapUtility = new LdapUtility(ldapHost, ldapPort, ldapPeopleDN,
				ldapGroupsDN, ldapRolesDN, ldapProtectedUsers,
				ldapProtectedGroups);

		logger.info(getClass().getSimpleName() + " initialised OK");
	}

	/**
	 * Modify the {@link User}'s information.
	 * 
	 * @param modifiedUser
	 *            the {@link User} to modify.
	 * @param newPassword
	 *            the new {@link User}'s password. To maintain the current
	 *            password, use <code>null</code>.
	 * 
	 * @return the modified {@link User}.
	 * 
	 * @throws NoSuchUserException
	 *             if the Use being modified doesn't exist.
	 * @throws EmailAlreadyExistsException
	 *             if the specified email is already used by another user.
	 * @throws IllegalOperationException
	 *             if the user is one of the protected users or the user to
	 *             modify is not the client user.
	 * @throws UserEditorException
	 */
	public User modifyUser(User modifiedUser, String newPassword)
			throws NoSuchUserException, EmailAlreadyExistsException,
			IllegalOperationException, UserEditorException {

		try {

			Date start = new Date();
			if (modifiedUser.getName().equals(getClientUser().getName())) {

				User user = ldapUtility.modifySelfUser(modifiedUser,
						getClientUserPassword(), newPassword);
				long duration = new Date().getTime() - start.getTime();

				registerAction("UserEditor.modifyUser",
						new String[] { "modifiedUser", modifiedUser + "",
								"newPassword", "*" },
						"User %username% called method UserEditor.modifyUser("
								+ modifiedUser + ", " + "*" + ")", duration);

				return user;

			} else {
				throw new IllegalOperationException(
						"Trying to modify user information for another user");
			}

		} catch (LdapUtilityException e) {
			throw new UserEditorException(e.getMessage(), e);
		}
	}

}
