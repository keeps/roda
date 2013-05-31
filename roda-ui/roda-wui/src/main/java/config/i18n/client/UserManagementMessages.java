/**
 * 
 */
package config.i18n.client;

import com.google.gwt.i18n.client.Messages;

/**
 * @author Luis Faria
 * 
 */
public interface UserManagementMessages extends Messages {

	// Action Report
	String actionReportNoUser();

	// Create
	String createUserFailure(String reason);

	String createGroupFailure(String reason);

	String createUserAlreadyExists(String username);

	String createUserEmailAlreadyExists(String email);

	String createGroupAlreadyExists(String groupname);

	// Edit
	String editUserFailure(String username, String reason);

	String editGroupFailure(String groupname, String reason);

	String editUserNotFound(String username);

	String editUserEmailAlreadyExists(String email);

	String editGroupNotFound(String groupname);

	// Remove
	String removeUserConfirm(String username);

	String removeUserFailure(String username, String reason);
	
	String removeUserNotPossible(String name);

	String removeGroupConfirm(String groupname);

	String removeGroupFailure(String groupname, String reason);

	// User/Group Alphabet Sorted List

	String userCount(int count);

	String groupCount(int count);

	// Action Report Window
	String actionResportTitle(String username);

	// User log
	String userLogEntriesTotal(int total);

	String logParameter(String name, String value);

}
