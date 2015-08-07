/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.user.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.data.Group;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.LogEntry;
import pt.gov.dgarq.roda.wui.common.client.PrintReportException;

/**
 * @author Luis Faria
 * 
 */
public interface UserManagementServiceAsync {

	/**
	 * Get all users
	 * 
	 * @param letter
	 *            letter filter, null for none
	 * @param search
	 *            search filter, null for none
	 * @return
	 * @throws RODAException
	 */
	public void getUsers(Character letter, String search, AsyncCallback<User[]> callback);

	/**
	 * Get all groups
	 * 
	 * @param letter
	 *            letter filter, null for none
	 * @param search
	 *            search filter, null for none
	 * @return
	 * @throws RODAException
	 */
	public void getGroups(Character letter, String search, AsyncCallback<Group[]> callback);

	/**
	 * Get the total number of users
	 * 
	 * @return the number of users
	 * @throws RODAException
	 */
	public void getUserCount(AsyncCallback<Integer> callback);

	/**
	 * Get the number of users which name starts with the letter and/or matches
	 * a filter
	 * 
	 * @param letter
	 *            the character that all users in the list must start with. Use
	 *            null to ignore this filter.
	 * @param filter
	 *            a substring of the user name, case insensitive, that all users
	 *            in the list must match. Use null to ignore this filter.
	 * @return the number of users that pass through the conditions
	 * @throws RODAException
	 */
	public void getUserCount(Character letter, String filter, AsyncCallback<Integer> callback);

	/**
	 * Get a sub-list of users which name starts with the letter and/or matches
	 * a filter. The sub-list will be cropped by the index of the first item to
	 * show and the maximum number of users to return.
	 * 
	 * @param letter
	 *            the character that all users in the list must start with. Use
	 *            null to ignore this filter.
	 * @param filter
	 *            a substring of the user name, case insensitive, that all users
	 *            in the list must match. Use null to ignore this filter.
	 * @param startItem
	 *            The index of the first item to show. The offset of the
	 *            sub-list.
	 * @param limit
	 *            The maximum number of users to return.
	 * @return An array with the list of users that pass through the conditions
	 * 
	 * @throws RODAException
	 */
	public void getUsers(Character letter, String filter, int startItem, int limit, AsyncCallback<User[]> callback);

	/**
	 * Get the total number of groups
	 * 
	 * @return the number of groups
	 * @throws RODAException
	 */
	public void getGroupCount(AsyncCallback<Integer> callback);

	/**
	 * Get the number of groups which name starts with the letter and/or matches
	 * a filter
	 * 
	 * @param letter
	 *            the character that all groups in the list must start with. Use
	 *            null to ignore this filter.
	 * @param filter
	 *            a substring of the group name, case insensitive, that all
	 *            groups in the list must match. Use null to ignore this filter.
	 * @return the number of groups that pass through the conditions
	 * 
	 * @throws RODAException
	 */
	public void getGroupCount(Character letter, String filter, AsyncCallback<Integer> callback);

	/**
	 * Get a sub-list of groups which name starts with the letter and/or matches
	 * a filter. The sub-list will be cropped by the index of the first item to
	 * show and the maximum number of groups to return.
	 * 
	 * @param letter
	 *            the character that all groups in the list must start with. Use
	 *            null to ignore this filter.
	 * @param filter
	 *            a substring of the group name, case insensitive, that all
	 *            groups in the list must match. Use null to ignore this filter.
	 * @param startItem
	 *            The index of the first item to show. The offset of the
	 *            sub-list.
	 * @param limit
	 *            The maximum number of groups to return.
	 * @return An array with the list of groups that pass through the conditions
	 * @throws RODAException
	 */
	public void getGroups(Character letter, String filter, int startItem, int limit, AsyncCallback<Group[]> callback);

	/**
	 * Get the groups to which a user belongs to
	 * 
	 * @param username
	 *            the of the the users
	 * @return the list of groups to which the user belongs to
	 * @throws RODAException
	 */
	public void getUserGroups(String username, AsyncCallback<Group[]> callback);

	/**
	 * Get all users that belong to a group
	 * 
	 * @param groupname
	 *            the name of the groups
	 * @return The list of users that belong to the group
	 * @throws RODAException
	 */
	public void getGroupUsers(String groupname, AsyncCallback<User[]> callback);

	/**
	 * Get a group
	 * 
	 * @param groupname
	 *            the group name
	 * @return the group
	 * @throws RODAException
	 */
	public void getGroup(String groupname, AsyncCallback<Group> callback);

	/**
	 * Get a user
	 * 
	 * @param username
	 *            the user name
	 * @return the user
	 * 
	 * @throws RODAException
	 */
	public void getUser(String username, AsyncCallback<User> callback);

	/**
	 * Create a new user
	 * 
	 * @param user
	 *            the user
	 * @param password
	 *            the user password
	 * @throws RODAException
	 */
	public void createUser(User user, String password, AsyncCallback<Void> callback);

	/**
	 * Modify a user
	 * 
	 * @param user
	 *            the modified users
	 * @param password
	 *            the new user password, or null to stay the same
	 * @throws RODAException
	 * 
	 */
	public void editUser(User user, String password, AsyncCallback<Void> callback);

	/**
	 * Modify the authenticated user
	 * 
	 * @param user
	 *            the modified user
	 * @param password
	 *            the user password if modified, or null if it remains the same
	 * @throws RODAException
	 */
	public void editMyUser(User user, String password, AsyncCallback<Void> callback);

	/**
	 * Create a group
	 * 
	 * @param group
	 *            the new group
	 * @throws RODAException
	 */
	public void createGroup(Group group, AsyncCallback<Void> callback);

	/**
	 * Modify a group
	 * 
	 * @param group
	 *            the modified group
	 * @throws RODAException
	 */
	public void editGroup(Group group, AsyncCallback<Void> callback);

	/**
	 * Try to remove a user, if user cannot be removed it will be deactivated
	 * 
	 * @param username
	 *            the user name
	 * @return true if user was removed, false if it was only deactivated
	 * @throws RODAException
	 */
	public void removeUser(String username, AsyncCallback<Boolean> callback);

	/**
	 * Remove a group
	 * 
	 * @param groupname
	 *            the group name
	 * @throws RODAException
	 */
	public void removeGroup(String groupname, AsyncCallback<Void> callback);

	/**
	 * Get group roles
	 * 
	 * @param groupname
	 *            the group name
	 * @return a list with the role codes that this group is enabled
	 * @throws RODAException
	 */
	public void getGroupsRoles(String[] groupname, AsyncCallback<String[]> callback);

	/**
	 * Get a list of roles that are set directly to this user, i.e. are not
	 * inherited.
	 * 
	 * @param username
	 *            the user name
	 * @return the list of direct roles
	 * @throws RODAException
	 */
	public void getUserDirectRoles(String username, AsyncCallback<String[]> callback);

	/**
	 * Get a list of roles that are set directly to this group, i.e. are not
	 * inherited.
	 * 
	 * @param groupname
	 *            the group name
	 * @return the list of direct roles
	 * @throws RODAException
	 */
	public void getGroupDirectRoles(String groupname, AsyncCallback<String[]> callback);

	/**
	 * Get a set that contains the first character of each user name, for all
	 * users.
	 * 
	 * @param search
	 *            search filter, null if none.
	 * 
	 * @return the list of characters
	 * @throws RODAException
	 */
	public void getUserLetterList(String search, AsyncCallback<Character[]> callback);

	/**
	 * Get a set that contains the first character of each group name, for all
	 * groups.
	 * 
	 * @param search
	 *            search filter, null if none
	 * 
	 * @return the list of characters
	 * @throws RODAException
	 */
	public void getGroupLetterList(String search, AsyncCallback<Character[]> callback);

	/**
	 * Get the number log entries
	 * 
	 * @param filter
	 * @return
	 * @throws RODAException
	 */
	void getLogEntriesCount(Filter filter, AsyncCallback<Long> callback);

	void findLogEntries(Filter filter, Sorter sorter, Sublist sublist, AsyncCallback<IndexResult<LogEntry>> callback);

	/**
	 * Register a new user
	 * 
	 * @param user
	 *            The user to register
	 * @param password
	 *            user password
	 * @param captcha
	 *            the captcha chalenge
	 * @return true if passed the chalenge, false otherwise
	 * @throws RODAException
	 */
	public void register(User user, String password, String captcha, AsyncCallback<Boolean> callback);

	/**
	 * Verify a user email. If verified user will become active
	 * 
	 * @param username
	 *            the name of the user
	 * @param token
	 *            the token used in email verification
	 * @return true if email verified, false otherwise
	 * @throws RODAException
	 * 
	 */
	public void verifyemail(String username, String token, AsyncCallback<Boolean> callback);

	/**
	 * Resend the email chalenge to a user email
	 * 
	 * @param username
	 *            the name of the user
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
	 *            the name of the user
	 * @param email
	 *            the new email
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
	 *            the user name or email
	 * @param captcha
	 *            the capcha chalenge answer
	 * @return true if the user passed the chalenge, false otherwise
	 * @throws RODAException
	 */
	public void requestPassordReset(String usernameOrEmail, String captcha, AsyncCallback<Boolean> callback);

	/**
	 * Reset a user password
	 * 
	 * @param username
	 *            the user name
	 * @param resetPasswordToken
	 *            the password token that was sent by email on
	 *            requestPasswordReset(String, String)
	 * @param newPassword
	 *            the new password
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
