package pt.gov.dgarq.roda.core;

import pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException;
import pt.gov.dgarq.roda.core.common.GroupAlreadyExistsException;
import pt.gov.dgarq.roda.core.common.IllegalOperationException;
import pt.gov.dgarq.roda.core.common.LoggerException;
import pt.gov.dgarq.roda.core.common.NoSuchGroupException;
import pt.gov.dgarq.roda.core.common.NoSuchUserException;
import pt.gov.dgarq.roda.core.common.UserAlreadyExistsException;
import pt.gov.dgarq.roda.core.common.UserManagementException;
import pt.gov.dgarq.roda.core.data.Group;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.logger.LoggerManager;
import pt.gov.dgarq.roda.servlet.LdapUtility;
import pt.gov.dgarq.roda.servlet.LdapUtilityException;

/**
 * @author Rui Castro
 */
public class UserManagementHelper {

	private LdapUtility ldapUtility = null;
	private LoggerManager loggerManager = null;

	/**
	 * Constructs a new {@link UserManagementHelper} with the given
	 * {@link LdapUtility} and {@link LoggerManager}.
	 * 
	 * @param ldapUtility
	 * @param loggerManager
	 */
	public UserManagementHelper(LdapUtility ldapUtility,
			LoggerManager loggerManager) {
		this.ldapUtility = ldapUtility;
		this.loggerManager = loggerManager;
	}

	/**
	 * Returns the number of registered users.
	 * 
	 * @param filter
	 * 
	 * @return an <code>int</code> with the number of users in the repository.
	 * @throws UserManagementException
	 */
	public int getUserCount(Filter filter) throws UserManagementException {
		try {

			return this.ldapUtility.getUserCount(filter);

		} catch (LdapUtilityException e) {
			throw new UserManagementException(e.getMessage(), e);
		}
	}

	/**
	 * Return the users that match the given {@link ContentAdapter}.
	 * 
	 * @param contentAdapter
	 *            the {@link ContentAdapter}.
	 * 
	 * @return an array of {@link User}'s.
	 * 
	 * @throws UserManagementException
	 */
	public User[] getUsers(ContentAdapter contentAdapter)
			throws UserManagementException {

		try {

			return this.ldapUtility.getUsers(contentAdapter);

		} catch (LdapUtilityException e) {
			throw new UserManagementException(e.getMessage(), e);
		}
	}

	/**
	 * Return the user names that match the given {@link ContentAdapter}.
	 * 
	 * @param contentAdapter
	 *            the {@link ContentAdapter}.
	 * 
	 * @return an array of {@link String} with the user names.
	 * 
	 * @throws UserManagementException
	 */
	public String[] getUserNames(ContentAdapter contentAdapter)
			throws UserManagementException {

		try {

			User[] users = this.ldapUtility.getUsers(contentAdapter);

			String[] usernames = new String[users.length];
			for (int i = 0; i < users.length; i++) {
				usernames[i] = users[i].getName();
			}

			return usernames;

		} catch (LdapUtilityException e) {
			throw new UserManagementException(e.getMessage(), e);
		}
	}

	/**
	 * Returns the number of registered groups.
	 * 
	 * @param filter
	 * 
	 * @return an <code>int</code> with the number of groups in the repository.
	 * @throws UserManagementException
	 */
	public int getGroupCount(Filter filter) throws UserManagementException {
		try {

			return this.ldapUtility.getGroupCount(filter);

		} catch (LdapUtilityException e) {
			throw new UserManagementException(e.getMessage(), e);
		}
	}

	/**
	 * Return groups that match the given {@link ContentAdapter}.
	 * 
	 * @param contentAdapter
	 * 
	 * @return an array of {@link Group}'s.
	 * 
	 * @throws UserManagementException
	 */
	public Group[] getGroups(ContentAdapter contentAdapter)
			throws UserManagementException {
		try {

			return this.ldapUtility.getGroups(contentAdapter);

		} catch (LdapUtilityException e) {
			throw new UserManagementException(e.getMessage(), e);
		}
	}

	/**
	 * Gets the list of roles names.
	 * 
	 * @return and array {@link String} with all the roles names.
	 * @throws UserManagementException
	 */
	public String[] getRoles() throws UserManagementException {
		try {

			return this.ldapUtility.getRoles();

		} catch (LdapUtilityException e) {
			throw new UserManagementException(e.getMessage(), e);
		}
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

		try {

			return ldapUtility.addUser(user);

		} catch (LdapUtilityException e) {
			throw new UserManagementException(e.getMessage(), e);
		}
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

		try {

			return ldapUtility.modifyUser(modifiedUser);

		} catch (LdapUtilityException e) {
			throw new UserManagementException(e.getMessage(), e);
		}
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

		try {

			return ldapUtility.addGroup(group);

		} catch (LdapUtilityException e) {
			throw new UserManagementException(e.getMessage(), e);
		}
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

		try {

			return ldapUtility.modifyGroup(modifiedGroup);

		} catch (LdapUtilityException e) {
			throw new UserManagementException(e.getMessage(), e);
		}
	}

	/**
	 * Returns the User with name <code>uid</code> or <code>null</code> if it
	 * doesn't exist.
	 * 
	 * @param name
	 *            the name of the desired User.
	 * 
	 * @return the User with name <code>name</code> or <code>null</code> if it
	 *         doesn't exist.
	 * 
	 * @throws UserManagementException
	 *             if the user information could not be retrieved from the LDAP
	 *             server.
	 */
	public User getUser(String name) throws UserManagementException {

		try {

			return ldapUtility.getUser(name);

		} catch (LdapUtilityException e) {
			throw new UserManagementException(e.getMessage(), e);
		}
	}

	/**
	 * Returns list of {@link User}s that belong to a group named
	 * <code>groupName</code>.
	 * 
	 * @param groupName
	 *            the name of the group.
	 * 
	 * @return an array of {@link User}s with all users that belong to a group
	 *         named <code>groupName</code>.
	 * 
	 * @throws UserManagementException
	 *             if the group or user information could not be retrieved from
	 *             the LDAP server.
	 */
	public User[] getUsersInGroup(String groupName)
			throws UserManagementException {

		try {

			return this.ldapUtility.getUsersInGroup(groupName);

		} catch (LdapUtilityException e) {
			throw new UserManagementException(e.getMessage(), e);
		}
	}

	/**
	 * Returns the group named <code>grpName</code>.
	 * 
	 * @param groupName
	 *            the name of the group.
	 * @return a Group if the group exists, otherwise <code>null</code>.
	 * @throws UserManagementException
	 *             if the group information could not be retrieved from the LDAP
	 *             server.
	 */
	public Group getGroup(String groupName) throws UserManagementException {

		try {

			return ldapUtility.getGroup(groupName);

		} catch (LdapUtilityException e) {
			throw new UserManagementException(e.getMessage(), e);
		}
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

		try {

			int logEntriesCount = this.loggerManager
					.getLogEntriesCount(new Filter(new SimpleFilterParameter(
									"username", username)));

			if (logEntriesCount > 0) {
				this.ldapUtility.deactivateUser(username);
				return false;
			} else {
				this.ldapUtility.removeUser(username);
				return true;
			}

		} catch (LdapUtilityException e) {
			throw new UserManagementException(e.getMessage(), e);
		} catch (LoggerException e) {
			throw new UserManagementException(
					"Error getting user log entries - " + e.getMessage(), e);
		}
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

		try {

			ldapUtility.removeGroup(groupname);

		} catch (LdapUtilityException e) {
			throw new UserManagementException(e.getMessage(), e);
		}
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

		try {

			ldapUtility.setUserPassword(username, password);

		} catch (LdapUtilityException e) {
			throw new UserManagementException(e.getMessage(), e);
		}
	}

	/**
	 * Returns the roles directly assigned to a given group.
	 * 
	 * @param groupName
	 *            the name of the group.
	 * 
	 * @return an array of roles directly assigned to a user.
	 * 
	 * @throws UserManagementException
	 */
	public String[] getGroupDirectRoles(String groupName)
			throws UserManagementException {

		try {

			return ldapUtility.getGroupDirectRoles(groupName);

		} catch (LdapUtilityException e) {
			throw new UserManagementException(e.getMessage(), e);
		}
	}

	/**
	 * Returns the roles directly assigned to a given user.
	 * 
	 * @param userName
	 *            the name of the user.
	 * 
	 * @return an array of roles directly assigned to a user.
	 * 
	 * @throws UserManagementException
	 */
	public String[] getUserDirectRoles(String userName)
			throws UserManagementException {

		try {

			return ldapUtility.getUserDirectRoles(userName);

		} catch (LdapUtilityException e) {
			throw new UserManagementException(e.getMessage(), e);
		}
	}

}
