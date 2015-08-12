package pt.gov.dgarq.roda.services.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Date;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException;
import pt.gov.dgarq.roda.core.common.GroupAlreadyExistsException;
import pt.gov.dgarq.roda.core.common.IllegalOperationException;
import pt.gov.dgarq.roda.core.common.LoginException;
import pt.gov.dgarq.roda.core.common.NoSuchGroupException;
import pt.gov.dgarq.roda.core.common.NoSuchUserException;
import pt.gov.dgarq.roda.core.common.RODAClientException;
import pt.gov.dgarq.roda.core.common.UserAlreadyExistsException;
import pt.gov.dgarq.roda.core.common.UserManagementException;
import pt.gov.dgarq.roda.core.data.Group;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.RegexFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.stubs.UserBrowser;
import pt.gov.dgarq.roda.core.stubs.UserManagement;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

/**
 * @author Rui Castro
 * 
 */
public class UserManagementTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// http://localhost:8180/
		String hostUrl = args[0];
		String username = args[1];
		String password = args[2];
		String casURL = args[3];
		String coreURL = args[4];
		String serviceURL = args[5];
		try {
			CASUtility casUtility = new CASUtility(new URL(casURL), new URL(coreURL), new URL(serviceURL));

			RODAClient rodaClient = new RODAClient(new URL(hostUrl), username,
					password,casUtility);

			UserManagement userManagement = rodaClient
					.getUserManagementService();
			UserBrowser userBrowser = rodaClient.getUserBrowserService();

			/*
			 * Count users
			 */
			System.out.println("\n***********************************");
			System.out.println("Count users ==> ");
			System.out.println("***********************************");

			int userCount = userBrowser.getUserCount(null);

			System.out.println(userCount + " users.");

			System.out.println("\n***********************************");
			System.out.println("Count users ==> ");

//			Filter userCountfilter = new Filter(new FilterParameter[] {
//					new SimpleFilterParameter("active", "true"),
//					// new OneOfManyFilterParameter("fullName", new String[] {
//					// "Rui Castro", "Tobias", }) 
//					});
//
//			System.out.println("Using " + userCountfilter);
//			System.out.println("***********************************");
//
//			userCount = userBrowser.getUserCount(userCountfilter);
//
//			System.out.println(userCount + " users in repository.");
//
//			/*
//			 * Get users
//			 */
//			System.out.println("\n***********************************");
//			System.out.println("Get first 3 active users ==> ");
//
//			Filter usersFilter = new Filter(
//					new FilterParameter[] { new SimpleFilterParameter("active",
//							"true") });
//			ContentAdapter usersContentAdapter = new ContentAdapter(
//					usersFilter, null, new Sublist(0, 3));
//
//			System.out.println("Using " + usersContentAdapter);
//			System.out.println("***********************************");
//
//			User[] users = userBrowser.getUsers(usersContentAdapter);
//			for (int i = 0; users != null && i < users.length; i++) {
//				System.out.println(users[i]);
//			}
//
//			System.out.println("\n***********************************");
//			System.out.println("Count users with name starting with 'a' ==> ");
//
//			usersFilter = new Filter(
//					new FilterParameter[] { new RegexFilterParameter("name",
//							"^a.*") });
//
//			System.out.println("Using " + usersFilter);
//			System.out.println("***********************************");
//
//			userCount = userBrowser.getUserCount(usersFilter);
//			System.out.println(userCount + " users.");
//
//			System.out.println("\n***********************************");
//			System.out.println("Get users with name starting with 'a' ==> ");
//
//			usersContentAdapter = new ContentAdapter(usersFilter, null, null);
//
//			System.out.println("Using " + usersContentAdapter);
//			System.out.println("***********************************");
//
//			users = userBrowser.getUsers(usersContentAdapter);
//			for (int i = 0; users != null && i < users.length; i++) {
//				System.out.println(users[i]);
//			}
//
//			System.out.println("\n***********************************");
//			System.out.println("Get first 3 users ==> ");
//
//			usersContentAdapter = new ContentAdapter(null, null, new Sublist(0,
//					3));
//
//			System.out.println("Using " + usersContentAdapter);
//			System.out.println("***********************************");
//
//			users = userBrowser.getUsers(usersContentAdapter);
//			for (int i = 0; users != null && i < users.length; i++) {
//				System.out.println(users[i]);
//			}
//
//			System.out.println("\n***********************************");
//			System.out
//					.println("Get all users ordered by businessCategory DESC, fullName ASC ==> ");
//
//			Sorter sorter = new Sorter(new SortParameter[] {
//					new SortParameter("businessCategory", true),
//					new SortParameter("fullName", false) });
//
//			usersContentAdapter = new ContentAdapter(null, sorter, null);
//
//			System.out.println("Using " + usersContentAdapter);
//			System.out.println("***********************************");
//
//			users = userBrowser.getUsers(usersContentAdapter);
//			for (int i = 0; users != null && i < users.length; i++) {
//				System.out.println(users[i]);
//			}
//
//			System.out.println("\n***********************************");
//			System.out.println("Get all users ==> ");
//			System.out.println("***********************************");
//
//			User[] allUsers = userBrowser.getUsers(null);
//			for (int i = 0; allUsers != null && i < allUsers.length; i++) {
//				System.out.println(allUsers[i]);
//			}
//
//			System.out.println("\n***********************************");
//			System.out.println("Get users in group administrators ==> ");
//			System.out.println("***********************************");
//
//			User[] usersInGroupAdmin = userBrowser
//					.getUsersInGroup("administrators");
//			for (int i = 0; usersInGroupAdmin != null
//					&& i < usersInGroupAdmin.length; i++) {
//				System.out.println(usersInGroupAdmin[i]);
//			}
//
//			/*
//			 * Count groups
//			 */
//			System.out
//					.println("\n********************************************");
//			System.out.println("Count groups (administrators, guests)");
//
//			Filter groupCountfilter = new Filter(new FilterParameter[] {
//					new SimpleFilterParameter("active", "true"),
//					// new OneOfManyFilterParameter("name", new String[] {
//					// "administrators", "guests", }) 
//					});
//
//			System.out.println("Using " + groupCountfilter);
//			System.out.println("********************************************");
//
//			int groupCount = userBrowser.getGroupCount(groupCountfilter);
//
//			System.out.println(groupCount + " groups.");
//
//			/*
//			 * Get groups
//			 */
//			System.out.println("\n***********************************");
//			System.out.println("Get groups (administrators, guests) ");
//
//			Filter groupfilter = new Filter(new FilterParameter[] {
//					new SimpleFilterParameter("active", "true"),
//					// new OneOfManyFilterParameter("name", new String[] {
//					// "administrators", "guests", }) 
//					});
//
//			ContentAdapter groupsContentAdapter = new ContentAdapter(
//					groupfilter, null, null);
//
//			System.out.println("Using " + groupsContentAdapter);
//			System.out.println("***********************************");
//
//			Group[] groups = userBrowser.getGroups(groupsContentAdapter);
//			for (int i = 0; groups != null && i < groups.length; i++) {
//				System.out.println(groups[i]);
//			}

			/*
			 * get all roles
			 */
			System.out.println("\n***********************************");
			System.out.println("Get all roles ==> ");
			System.out.println("***********************************");

			String[] roles = userBrowser.getRoles();
			for (int i = 0; i < roles.length; i++) {
				System.out.println(roles[i]);
			}

			/*
			 * Create new group
			 */
			System.out.println("\n***********************************");
			System.out.println("Create new group ==> ");
			System.out.println("***********************************");

			Group newGroup = new Group();
			newGroup.setName("newgroup");
			newGroup.setFullName("New Group");

			// newGroup.addGroup("guests");

			newGroup.addMemberUser("rcastro");
			newGroup.addMemberUser("lfaria");

			try {

				Group createdGroup = userManagement.addGroup(newGroup);

				System.out.println(createdGroup);

			} catch (GroupAlreadyExistsException e) {
				e.printStackTrace();
			}

			/*
			 * Modify group
			 */
			System.out.println("\n***********************************");
			System.out.println("Modify group newGroup ==> add Group");
			System.out.println("***********************************");

			Group groupToModify = userBrowser.getGroup("newgroup");
			groupToModify.setFullName("Modified New Group");

			groupToModify.addGroup("producers");

			Group modifiedGroup;
			try {

				modifiedGroup = userManagement.modifyGroup(groupToModify);

				System.out.println(modifiedGroup);

			} catch (NoSuchGroupException e) {
				e.printStackTrace();
			}

			System.out.println("\n***********************************");
			System.out
					.println("Modify group newGroup ==> remove Group and add Role");
			System.out.println("***********************************");

			groupToModify = userBrowser.getGroup("newgroup");
			groupToModify.removeGroup("producers");
			groupToModify.addDirectRole("dissemination.browse");

			try {

				modifiedGroup = userManagement.modifyGroup(groupToModify);

				System.out.println(modifiedGroup);

			} catch (NoSuchGroupException e) {
				e.printStackTrace();
			}

			System.out.println("\n***********************************");
			System.out.println("Modify group newGroup ==> add Role");
			System.out.println("***********************************");

			groupToModify = userBrowser.getGroup("newgroup");
			groupToModify.addDirectRole("administration.user");

			try {

				modifiedGroup = userManagement.modifyGroup(groupToModify);

				System.out.println(modifiedGroup);

			} catch (NoSuchGroupException e) {
				e.printStackTrace();
			}

			System.out.println("\n***********************************");
			System.out.println("Modify group newGroup ==> remove Role");
			System.out.println("***********************************");

			groupToModify = userBrowser.getGroup("newgroup");
			groupToModify.removeDirectRole("administration.user");

			try {

				modifiedGroup = userManagement.modifyGroup(groupToModify);

				System.out.println(modifiedGroup);

			} catch (NoSuchGroupException e) {
				e.printStackTrace();
			}

			/*
			 * Create new user
			 */
			System.out.println("\n***********************************");
			System.out.println("User newUser ==> ");
			System.out.println("***********************************");

			User newUser = new User();
			newUser.setName("newuser");
			newUser.setFullName("New User");

			newUser.setIdDocumentType(User.ID_TYPE_BI);
			newUser.setIdDocument("123456789");
			newUser.setIdDocumentLocation("Lisboa");
			newUser.setIdDocumentDate(new Date(System.currentTimeMillis()));
			
			newUser.setBirthCountry("Portugal");

			newUser.setPostalAddress("Aqui");
			newUser.setPostalCode("4700");
			newUser.setLocalityName("Braga");
			newUser.setCountryName("Portugal");
			newUser.setTelephoneNumber("+35101010101");
			newUser.setFax("+35102020202");
			newUser.setEmail("newuser@test.com");
			
			newUser.setBusinessCategory("Informatics");

			newUser.addDirectRole("administrator");
			newUser.addGroup("newgroup");

			try {

				User createdUser = userManagement.addUser(newUser);

				System.out.println(createdUser);

			} catch (UserAlreadyExistsException e) {
				e.printStackTrace();
			} catch (EmailAlreadyExistsException e) {
				e.printStackTrace();
			}

			System.out.println(userBrowser.getGroup("newgroup"));

			/*
			 * Modify user
			 */
			System.out.println("\n***********************************");
			System.out.println("Modify user newUser ==> ");
			System.out.println("***********************************");

			User userToModify = newUser;
			// userToModify.setName("newuser");
			userToModify.setFullName("Modified New User");
			userToModify.addDirectRole("administrator");

			User modifiedUser;
			try {

				modifiedUser = userManagement.modifyUser(userToModify);

				System.out.println(modifiedUser);

			} catch (NoSuchUserException e) {
				e.printStackTrace();
			} catch (EmailAlreadyExistsException e) {
				e.printStackTrace();
			}

			/*
			 * Mark user inactive
			 */
			System.out.println("\n***********************************");
			System.out.println("Mark user newUser inactive ==> ");
			System.out.println("***********************************");

			userToModify = userBrowser.getUser("newuser");
			userToModify.setActive(false);

			try {

				modifiedUser = userManagement.modifyUser(userToModify);

				System.out.println(modifiedUser);

			} catch (NoSuchUserException e) {
				e.printStackTrace();
			} catch (EmailAlreadyExistsException e) {
				e.printStackTrace();
			}

			/*
			 * Mark user active
			 */
			System.out.println("\n***********************************");
			System.out.println("Mark user newUser active ==> ");
			System.out.println("***********************************");

			userToModify = userBrowser.getUser("newuser");
			userToModify.setActive(true);

			try {

				modifiedUser = userManagement.modifyUser(userToModify);

				System.out.println(modifiedUser);

			} catch (NoSuchUserException e) {
				e.printStackTrace();
			} catch (EmailAlreadyExistsException e) {
				e.printStackTrace();
			}

			System.out.println("\n***********************************");
			System.out.println("Modify user wrongusername ==> ");
			System.out.println("***********************************");

			userToModify = newUser;
			userToModify.setName("wrongusername");
			userToModify.setEmail("wrongusername@test.net");

			try {

				modifiedUser = userManagement.modifyUser(userToModify);

				System.err
						.println("user with name 'wrongusername' shouldn't exist. "
								+ "A NoSuchUserException was expected");

				System.out.println(modifiedUser);

			} catch (NoSuchUserException e) {
				System.out.println("NoSuchUserException thrown. OK");
				e.printStackTrace(System.out);
			} catch (EmailAlreadyExistsException e) {
				e.printStackTrace();
			}

			System.out.println("\n*******************************************");
			System.out.println("Modifying group wronggroupname ==> ");
			System.out.println("*******************************************");
			groupToModify.setName("wronggroupname");
			try {
				modifiedGroup = userManagement.modifyGroup(groupToModify);

				System.err
						.println("group with name 'wronggroupname' shouldn't exist. "
								+ "A NoSuchGroupException was expected");
				System.out.println(modifiedGroup);

			} catch (NoSuchGroupException e) {
				System.out.println("NoSuchGroupException thrown. OK");
				e.printStackTrace(System.out);
			}

			/*
			 * Set newgroup super groups (no groups)
			 */
			// System.out.println("*******************************************");
			// System.out.println("Setting Supergroups for wronggroupname ==>
			// ");
			// System.out.println("*******************************************");
			// try {
			// userManagement.setSuperGroups("wronggroupname", null);
			// System.err
			// .println("group with name 'wronggroupname' shouldn't exist. "
			// + "A NoSuchGroupException was expected");
			//
			// } catch (NoSuchGroupException e) {
			// System.out.println("NoSuchGroupException thrown. OK");
			// e.printStackTrace(System.out);
			// }
			//
			// System.out.println("*******************************************");
			// System.out.println("Setting User groups for wrongusername ==> ");
			// System.out.println("*******************************************");
			// try {
			// userManagement.setUserGroups("wrongusername", new String[] {});
			// System.err
			// .println("group with name 'wrongusername' shouldn't exist. "
			// + "A NoSuchUserException was expected");
			// } catch (NoSuchUserException e) {
			// System.out.println("NoSuchGroupException thrown. OK");
			// e.printStackTrace(System.out);
			// }
			System.out.println("\n****************************************");
			System.out.println("Setting password for wrongusername ==> ");
			System.out.println("****************************************");

			try {
				userManagement.setUserPassword("wrongusername", "secret");
				System.err
						.println("user with name 'wronggroupname' shouldn't exist. "
								+ "A NoSuchUserException was expected");
			} catch (NoSuchUserException e) {
				System.out.println("NoSuchUserException thrown. OK");
				e.printStackTrace(System.out);
			}

			System.out
					.println("\n***********************************************");
			System.out
					.println("Getting user direct roles for wrongusername ==> ");
			System.out
					.println("\n***********************************************");
			userBrowser.getUserDirectRoles("wrongusername");

			System.out
					.println("\n*************************************************");
			System.out
					.println("Getting group direct roles for wronggroupname ==> ");
			System.out
					.println("*************************************************");
			userBrowser.getGroupDirectRoles("wronggroupname");

			/*
			 * Remove user
			 */
			System.out.println("\n***********************************");
			System.out.println("Remove user newUser ==> ");
			System.out.println("***********************************");

			boolean removed = userManagement.removeUser("newuser");

			if (userBrowser.getUser("newuser") == null) {
				System.out.println("Remove successful");
			} else {
				System.out.println("Remove failed");
			}

			System.out.println(userBrowser.getGroup("newgroup"));

			/*
			 * Remove group
			 */
			System.out.println("\n***********************************");
			System.out.println("Remove group newGroup ==> ");
			System.out.println("***********************************");

			userManagement.removeGroup("newgroup");

			if (userBrowser.getGroup("newgroup") == null) {
				System.out.println("Remove successful");
			} else {
				System.out.println("Remove failed");
			}

			/*
			 * Remove administrator
			 */
			System.out.println("\n***************************************");
			System.out.println("Trying to remove user administrator ==>");
			System.out.println("***************************************");

			try {

				removed = userManagement.removeUser("admin");

			} catch (IllegalOperationException e) {
				// OK
				System.out.println("IllegalOperationException thrown. OK - "
						+ e.getMessage());
				e.printStackTrace(System.out);
			} catch (UserManagementException e) {
				// Error
				System.out
						.println("InternalServiceException thrown, IllegalOperationException was expected. IGNORE");
				e.printStackTrace(System.out);
			}

			/*
			 * Modify administrator
			 */
			System.out.println("\n***************************************");
			System.out.println("Trying to modify user administrator ==>");
			System.out.println("***************************************");

			try {

				userManagement.modifyUser(userBrowser.getUser("admin"));

			} catch (IllegalOperationException e) {
				// OK
				System.out.println("IllegalOperationException thrown. OK - "
						+ e.getMessage());
				e.printStackTrace(System.out);
			} catch (NoSuchUserException e) {
				// Error
				System.err
						.println("NoSuchUserException thrown, IllegalOperationException was expected. IGNORE");
				e.printStackTrace();
			} catch (EmailAlreadyExistsException e) {
				System.err
						.println("EmailAlreadyExistsException thrown, IllegalOperationException was expected. IGNORE");
				e.printStackTrace();
			} catch (UserManagementException e) {
				// Error
				System.err
						.println("InternalServiceException thrown, IllegalOperationException was expected. IGNORE");
				e.printStackTrace();
			}

			/*
			 * Remove administrators
			 */
			System.out.println("\n***************************************");
			System.out.println("Trying to remove group administrators ==>");
			System.out.println("***************************************");

			try {

				userManagement.removeGroup("administrators");

			} catch (IllegalOperationException e) {
				// OK
				System.out.println("IllegalOperationException thrown. OK - "
						+ e.getMessage());
				e.printStackTrace(System.out);

			} catch (UserManagementException e) {
				// Error
				System.out
						.println("InternalServiceException thrown, IllegalOperationException was expected. IGNORE");
				e.printStackTrace(System.out);
			}

			/*
			 * Modify administrators
			 */
			System.out.println("\n***************************************");
			System.out.println("Trying to modify group administrators ==>");
			System.out.println("***************************************");

			try {

				userManagement.modifyGroup(userBrowser
						.getGroup("administrators"));

			} catch (IllegalOperationException e) {
				// OK
				System.out.println("IllegalOperationException thrown. OK - "
						+ e.getMessage());
				e.printStackTrace(System.out);

			} catch (NoSuchGroupException e) {
				// Error
				System.out
						.println("NoSuchGroupException thrown, IllegalOperationException was expected. IGNORE");
				e.printStackTrace(System.out);
			} catch (UserManagementException e) {
				// Error
				System.out
						.println("InternalServiceException thrown, IllegalOperationException was expected. IGNORE");
				e.printStackTrace(System.out);
			}

			/*
			 * Remove guest
			 */
			System.out.println("\n*******************************");
			System.out.println("Trying to remove user guest ==>");
			System.out.println("*******************************");

			try {

				removed = userManagement.removeUser("guest");

			} catch (IllegalOperationException e) {
				// OK
				System.out.println("IllegalOperationException thrown. OK - "
						+ e.getMessage());
				e.printStackTrace(System.out);
			} catch (UserManagementException e) {
				// Error
				System.out
						.println("InternalServiceException thrown, IllegalOperationException was expected. IGNORE");
				e.printStackTrace(System.out);
			} catch (NoSuchUserException e) {
				e.printStackTrace();
			}

			/*
			 * Modify guest
			 */
			System.out.println("\n***************************************");
			System.out.println("Trying to modify user guest ==>");
			System.out.println("***************************************");

			try {

				userManagement.modifyUser(userBrowser.getUser("guest"));

			} catch (IllegalOperationException e) {
				// OK
				System.out.println("IllegalOperationException thrown. OK - "
						+ e.getMessage());
				e.printStackTrace(System.out);
			} catch (NoSuchUserException e) {
				// Error
				System.out
						.println("NoSuchUserException thrown, IllegalOperationException was expected. IGNORE");
				e.printStackTrace(System.out);
			} catch (EmailAlreadyExistsException e) {
				System.err
						.println("EmailAlreadyExistsException thrown, IllegalOperationException was expected. IGNORE");
				e.printStackTrace();
			} catch (UserManagementException e) {
				// Error
				System.out
						.println("InternalServiceException thrown, IllegalOperationException was expected. IGNORE");
				e.printStackTrace(System.out);
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UserManagementException e) {
			e.printStackTrace();
		} catch (LoginException e) {
			e.printStackTrace();
		} catch (IllegalOperationException e) {
			e.printStackTrace();
		} catch (NoSuchUserException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (RODAClientException e) {
			e.printStackTrace();
		}
	}
}
