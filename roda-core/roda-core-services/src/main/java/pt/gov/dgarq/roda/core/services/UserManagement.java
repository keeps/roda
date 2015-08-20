package pt.gov.dgarq.roda.core.services;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.UserManagementHelper;
import pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException;
import pt.gov.dgarq.roda.core.common.GroupAlreadyExistsException;
import pt.gov.dgarq.roda.core.common.IllegalOperationException;
import pt.gov.dgarq.roda.core.common.NoSuchGroupException;
import pt.gov.dgarq.roda.core.common.NoSuchUserException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.common.UserAlreadyExistsException;
import pt.gov.dgarq.roda.core.common.UserManagementException;
import pt.gov.dgarq.roda.core.data.v2.Group;
import pt.gov.dgarq.roda.core.data.v2.User;
import pt.gov.dgarq.roda.servlet.LdapUtility;

/**
 * This class implements the User Management service.
 * 
 * @author Rui Castro
 * 
 */
public class UserManagement extends RODAWebService {

	static final private Logger logger = Logger.getLogger(UserManagement.class);

	private UserManagementHelper userManagementHelper = null;

	/**
	 * Constructs a new {@link UserManagement} instance.
	 * 
	 * @throws RODAServiceException
	 * 
	 */
	public UserManagement() throws RODAServiceException {

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
		List<String> ldapProtectedUsers = Arrays.asList(getConfiguration()
				.getStringArray("ldapProtectedUsers"));
		List<String> ldapProtectedGroups = Arrays.asList(getConfiguration()
				.getStringArray("ldapProtectedGroups"));

		LdapUtility ldapUtility = new LdapUtility(ldapHost, ldapPort,
				ldapPeopleDN, ldapGroupsDN, ldapRolesDN, ldapAdminDN,
				ldapAdminPassword, ldapPasswordDigestAlgorithm,
				ldapProtectedUsers, ldapProtectedGroups);

		this.userManagementHelper = new UserManagementHelper(ldapUtility,
				getLoggerManager());

		logger.info(getClass().getSimpleName() + " initialised OK");
	}

	/**
	 * Adds a new {@link Group}.
	 * 
	 * @param group
	 *            the {@link Group} to add.
	 * @return the newly created {@link Group}.
	 * @throws GroupAlreadyExistsException
	 *             if a Group with the same name already exists.
	 * @throws UserManagementException
	 *             if something goes wrong with the creation of the new group.
	 */
	public Group addGroup(Group group) throws UserManagementException,
			GroupAlreadyExistsException {

		Date start = new Date();
		Group result = this.userManagementHelper.addGroup(group);
		long duration = new Date().getTime() - start.getTime();

		registerAction("UserManagement.addGroup", new String[] { "group",
				group.toString() },
				"User %username% called method UserManagement.addGroup("
						+ group + ")", duration);
		return result;
	}

	/**
	 * Modify the {@link Group}'s information.
	 * 
	 * @param modifiedGroup
	 *            the {@link Group} to modify.
	 * @return the modified {@link Group}.
	 * @throws NoSuchGroupException
	 *             if the group with being modified doesn't exist.
	 * @throws IllegalOperationException
	 * @throws UserManagementException
	 */
	public Group modifyGroup(Group modifiedGroup)
			throws UserManagementException, NoSuchGroupException,
			IllegalOperationException {

		Date start = new Date();
		Group group = this.userManagementHelper.modifyGroup(modifiedGroup);
		long duration = new Date().getTime() - start.getTime();

		registerAction("UserManagement.modifyGroup", new String[] {
				"modifiedGroup", modifiedGroup.toString() },
				"User %username% called method UserManagement.modifyGroup("
						+ modifiedGroup + ")", duration);

		return group;
	}

	/**
	 * Removes a group.
	 * 
	 * @param groupname
	 *            the name of the group to remove.
	 * @throws IllegalOperationException
	 * @throws UserManagementException
	 */
	public void removeGroup(String groupname) throws UserManagementException,
			IllegalOperationException {

		Date start = new Date();
		this.userManagementHelper.removeGroup(groupname);
		long duration = new Date().getTime() - start.getTime();

		registerAction("UserManagement.removeGroup", new String[] {
				"groupname", groupname },
				"User %username% called method UserManagement.removeGroup("
						+ groupname + ")", duration);

	}

	/**
	 * Adds a new {@link User}.
	 * 
	 * @param user
	 *            the {@link User} to add.
	 * @return the newly created {@link User}.
	 * @throws UserAlreadyExistsException
	 *             if a User with the same name already exists.
	 * @throws EmailAlreadyExistsException
	 *             if the specified email is already used by another user.
	 * @throws UserManagementException
	 *             if something goes wrong with the creation of the new user.
	 */
	public User addUser(User user) throws UserManagementException,
			UserAlreadyExistsException, EmailAlreadyExistsException {

		Date start = new Date();
		User result = this.userManagementHelper.addUser(user);
		long duration = new Date().getTime() - start.getTime();

		registerAction("UserManagement.addUser", new String[] { "user",
				user.toString() },
				"User %username% called method UserManagement.addUser(" + user
						+ ")", duration);
		return result;

	}

	/**
	 * Modify the {@link User}'s information.
	 * 
	 * @param modifiedUser
	 *            the {@link User} to modify.
	 * @return the modified {@link User}.
	 * @throws NoSuchUserException
	 *             if the Use being modified doesn't exist.
	 * @throws EmailAlreadyExistsException
	 *             if the specified email is already used by another user.
	 * @throws IllegalOperationException
	 * @throws UserManagementException
	 */
	public User modifyUser(User modifiedUser) throws UserManagementException,
			NoSuchUserException, IllegalOperationException,
			EmailAlreadyExistsException {

		Date start = new Date();
		User result = this.userManagementHelper.modifyUser(modifiedUser);
		long duration = new Date().getTime() - start.getTime();

		registerAction("UserManagement.modifyUser", new String[] {
				"modifiedUser", modifiedUser.toString() },
				"User %username% called method UserManagement.modifyUser("
						+ modifiedUser + ")", duration);

		return result;
	}

	/**
	 * Removes a user.
	 * 
	 * @param username
	 *            the name of the user to remove.
	 * 
	 * @return <code>true</code> if the {@link User} was removed and
	 *         <code>false</code> if it was disabled.
	 * 
	 * @throws IllegalOperationException
	 * @throws NoSuchUserException
	 * @throws UserManagementException
	 */
	public boolean removeUser(String username) throws UserManagementException,
			IllegalOperationException, NoSuchUserException {

		Date start = new Date();
		boolean result = this.userManagementHelper.removeUser(username);
		long duration = new Date().getTime() - start.getTime();

		registerAction("UserManagement.removeUser", new String[] { "username",
				username },
				"User %username% called method UserManagement.removeUser("
						+ username + ")", duration);

		return result;
	}

	/**
	 * Sets the user's password.
	 * 
	 * @param username
	 * @param password
	 * @throws NoSuchUserException
	 *             if specified User doesn't exist.
	 * @throws IllegalOperationException
	 * @throws UserManagementException
	 */
	public void setUserPassword(String username, String password)
			throws UserManagementException, NoSuchUserException,
			IllegalOperationException {

		Date start = new Date();
		this.userManagementHelper.setUserPassword(username, password);
		long duration = new Date().getTime() - start.getTime();

		registerAction("UserManagement.setUserPassword", new String[] {
				"username", username },
				"User %username% called method UserManagement.setUserPassword("
						+ username + ")", duration);
	}

}
