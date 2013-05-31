package pt.gov.dgarq.roda.core.services;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.UserManagementHelper;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.common.UserManagementException;
import pt.gov.dgarq.roda.core.data.Group;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.servlet.LdapUtility;

/**
 * This class implements the User Management service.
 * 
 * @author Rui Castro
 * 
 */
public class UserBrowser extends RODAWebService {

	static final private Logger logger = Logger.getLogger(UserBrowser.class);

	private UserManagementHelper userManagementHelper = null;

	/**
	 * Constructs a new {@link UserBrowser} instance.
	 * 
	 * @throws RODAServiceException
	 * 
	 */
	public UserBrowser() throws RODAServiceException {

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
	 * Returns the number of registered groups.
	 * 
	 * @param filter
	 * 
	 * @return an <code>int</code> with the number of groups in the repository.
	 * @throws UserManagementException
	 */
	public int getGroupCount(Filter filter) throws UserManagementException {

		Date start = new Date();
		int count = this.userManagementHelper.getGroupCount(filter);
		long duration = new Date().getTime() - start.getTime();

		registerAction("UserBrowser.getGroupCount", new String[] { "filter",
				"" + filter },
				"User %username% called method UserBrowser.getGroupCount("
						+ filter + ")", duration);

		return count;
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

		Date start = new Date();
		Group group = this.userManagementHelper.getGroup(groupName);
		long duration = new Date().getTime() - start.getTime();

		registerAction("UserBrowser.getGroup", new String[] { "groupName",
				groupName },
				"User %username% called method UserBrowser.getGroup("
						+ groupName + ")", duration);

		return group;
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

		Date start = new Date();
		if (contentAdapter == null) {
			contentAdapter = new ContentAdapter();
		}

		Group[] groups = this.userManagementHelper.getGroups(contentAdapter);
		long duration = new Date().getTime() - start.getTime();

		registerAction("UserBrowser.getGroups", new String[] {
				"contentAdapter", contentAdapter.toString() },
				"User %username% called method UserBrowser.getGroups("
						+ contentAdapter + ")", duration);

		return groups;

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

		Date start = new Date();
		User[] usersInGroup = this.userManagementHelper
				.getUsersInGroup(groupName);
		long duration = new Date().getTime() - start.getTime();

		registerAction("UserBrowser.getUserMembersInGroup", new String[] {
				"groupName", groupName },
				"User %username% called method UserBrowser.getUserMembersInGroup("
						+ groupName + ")", duration);

		return usersInGroup;

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

		Date start = new Date();
		if (filter == null) {
			filter = new Filter();
		}

		int count = this.userManagementHelper.getUserCount(filter);
		long duration = new Date().getTime() - start.getTime();

		registerAction("UserBrowser.getUserCount", new String[] { "filter",
				filter.toString() },
				"User %username% called method UserBrowser.getUserCount("
						+ filter + ")", duration);

		return count;
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

		Date start = new Date();
		User user = this.userManagementHelper.getUser(name);
		long duration = new Date().getTime() - start.getTime();

		registerAction("UserBrowser.getUser", new String[] { "name", name },
				"User %username% called method UserBrowser.getUser(" + name
						+ ")", duration);

		return user;
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

		Date start = new Date();
		if (contentAdapter == null) {
			contentAdapter = new ContentAdapter();
		}

		User[] users = this.userManagementHelper.getUsers(contentAdapter);
		long duration = new Date().getTime() - start.getTime();

		registerAction("UserBrowser.getUsers", new String[] { "contentAdapter",
				contentAdapter.toString() },
				"User %username% called method UserBrowser.getUsers("
						+ contentAdapter + ")", duration);

		return users;
	}

	/**
	 * Return the user names of users that match the given
	 * {@link ContentAdapter}.
	 * 
	 * @param contentAdapter
	 *            the {@link ContentAdapter}.
	 * 
	 * @return an array of {@link String}s with the user names.
	 * 
	 * @throws UserManagementException
	 */
	public String[] getUserNames(ContentAdapter contentAdapter)
			throws UserManagementException {

		Date start = new Date();
		if (contentAdapter == null) {
			contentAdapter = new ContentAdapter();
		}

		String[] userNames = this.userManagementHelper
				.getUserNames(contentAdapter);
		long duration = new Date().getTime() - start.getTime();

		registerAction("UserBrowser.getUserNames", new String[] {
				"contentAdapter", contentAdapter.toString() },
				"User %username% called method UserBrowser.getUserNames("
						+ contentAdapter + ")", duration);

		return userNames;
	}

	/**
	 * Gets the list of roles names.
	 * 
	 * @return and array {@link String} with all the roles names.
	 * @throws UserManagementException
	 */
	public String[] getRoles() throws UserManagementException {

		Date start = new Date();
		String[] roles = this.userManagementHelper.getRoles();
		long duration = new Date().getTime() - start.getTime();

		registerAction("UserBrowser.getRoles", (String[]) null,
				"User %username% called method UserBrowser.getRoles", duration);

		return roles;
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

		Date start = new Date();
		String[] groupDirectRoles = this.userManagementHelper
				.getGroupDirectRoles(groupName);
		long duration = new Date().getTime() - start.getTime();

		registerAction("UserBrowser.getGroupDirectRoles", new String[] {
				"groupName", groupName },
				"User %username% called method UserBrowser.getGroupDirectRoles("
						+ groupName + ")", duration);

		return groupDirectRoles;
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

		Date start = new Date();
		String[] userDirectRoles = this.userManagementHelper
				.getUserDirectRoles(userName);
		long duration = new Date().getTime() - start.getTime();

		registerAction("UserBrowser.getUserDirectRoles", new String[] {
				"userName", userName },
				"User %username% called method UserBrowser.getUserDirectRoles("
						+ userName + ")", duration);

		return userDirectRoles;

	}
}
