package pt.gov.dgarq.roda.wui.management.user.server;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.roda.index.IndexServiceException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import config.i18n.server.UserLogMessages;
import pt.gov.dgarq.roda.common.LogUtility;
import pt.gov.dgarq.roda.common.RodaClientFactory;
import pt.gov.dgarq.roda.common.RodaCoreFactory;
import pt.gov.dgarq.roda.common.RodaCoreService;
import pt.gov.dgarq.roda.common.UserUtility;
import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.AuthorizationDeniedException;
import pt.gov.dgarq.roda.core.common.IllegalOperationException;
import pt.gov.dgarq.roda.core.common.InvalidTokenException;
import pt.gov.dgarq.roda.core.common.NoSuchUserException;
import pt.gov.dgarq.roda.core.common.RODAClientException;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.common.UserManagementException;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.facet.Facets;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.RegexFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.FacetFieldResult;
import pt.gov.dgarq.roda.core.data.v2.FacetValue;
import pt.gov.dgarq.roda.core.data.v2.Group;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.LogEntry;
import pt.gov.dgarq.roda.core.data.v2.RODAMember;
import pt.gov.dgarq.roda.core.data.v2.RodaGroup;
import pt.gov.dgarq.roda.core.data.v2.RodaSimpleUser;
import pt.gov.dgarq.roda.core.data.v2.RodaUser;
import pt.gov.dgarq.roda.core.data.v2.User;
import pt.gov.dgarq.roda.core.stubs.UserBrowser;
import pt.gov.dgarq.roda.core.stubs.UserEditor;
import pt.gov.dgarq.roda.core.stubs.UserRegistration;
import pt.gov.dgarq.roda.ds.LdapUtilityException;
import pt.gov.dgarq.roda.wui.common.captcha.server.CaptchaServiceImpl;
import pt.gov.dgarq.roda.wui.common.client.GenericException;
import pt.gov.dgarq.roda.wui.common.client.PrintReportException;
import pt.gov.dgarq.roda.wui.common.server.ServerTools;
import pt.gov.dgarq.roda.wui.common.server.VelocityMail;
import pt.gov.dgarq.roda.wui.management.user.client.UserManagementService;

/**
 * User Management service implementation
 * 
 * @author Luis Faria
 */
public class UserManagementServiceImpl extends RemoteServiceServlet implements UserManagementService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * User Management Service implementation constructor
	 * 
	 */
	public UserManagementServiceImpl() {

	}

	public Integer getUserCount() throws RODAException {
		return getUserCount(null, null);
	}

	public Integer getGroupCount() throws RODAException {
		return getGroupCount(null, null);
	}

	public User[] getUsers(Character letter, String search) throws RODAException {
		User[] users;
		try {
			Filter filterGroup = new Filter();
			filterGroup.add(new SimpleFilterParameter(RodaConstants.MEMBER_IS_USER, "true"));
			Filter searchFilter = getFilter(letter, search);
			if(searchFilter.getParameters()!=null && searchFilter.getParameters().size()>0){
				for(FilterParameter fp : searchFilter.getParameters()){
					filterGroup.add(fp);
				}
			}
			ContentAdapter contentAdapter = new ContentAdapter();
			contentAdapter.setFilter(filterGroup);
			
			SortParameter[] sortParameters = new SortParameter[1];
			sortParameters[0] = new SortParameter("name", false);
			contentAdapter.setSorter(new Sorter(sortParameters));

			users = UserUtility.getLdapUtility().getUsers(contentAdapter);
			return users;
		}catch(LdapUtilityException e){
			throw new RODAException(e.getMessage(),e) {
			};
		}
	}

	protected String[] getUserNames(Character letter, String search) throws RODAException {
		String[] usernames;
		// try {
		ContentAdapter contentAdapter = new ContentAdapter();
		contentAdapter.setFilter(getFilter(letter, search));
		SortParameter[] sortParameters = new SortParameter[1];
		sortParameters[0] = new SortParameter("name", false);
		contentAdapter.setSorter(new Sorter(sortParameters));
		// TODO move to new implementation
		// usernames =
		// RodaClientFactory.getRodaClient(getThreadLocalRequest().getSession()).getUserBrowserService()
		// .getUserNames(contentAdapter);
		usernames = new String[] {};
		// } catch (RemoteException e) {
		// logger.error("Remote Exception", e);
		// throw RODAClient.parseRemoteException(e);
		// }

		return usernames;
	}

	public Group[] getGroups(Character letter, String search) throws RODAException {
		Group[] groups;
		// try {
		ContentAdapter contentAdapter = new ContentAdapter();
		contentAdapter.setFilter(getFilter(letter, search));
		SortParameter[] sortParameters = new SortParameter[1];
		sortParameters[0] = new SortParameter("name", false);
		contentAdapter.setSorter(new Sorter(sortParameters));
		// TODO migrate to new implementation
		// groups = RodaClientFactory.getRodaClient(
		// getThreadLocalRequest().getSession())
		// .getUserBrowserService().getGroups(contentAdapter);
		groups = new Group[] {};
		// } catch (RemoteException e) {
		// logger.error("Remote Exception", e);
		// throw RODAClient.parseRemoteException(e);
		// }

		return groups;
	}

	protected Filter getFilter(Character letter, String search) {
		Filter filter;
		FilterParameter searchFilter = null;
		FilterParameter letterFilter = null;

		if (letter != null) {
			letterFilter = new RegexFilterParameter("name", "^(?i)" + letter + ".*");
		}

		if (search != null && search.length() > 0) {
			searchFilter = new RegexFilterParameter("fullname", ".*(?i)" + search + ".*");
		}

		if (searchFilter != null && letterFilter != null) {
			filter = new Filter();
			filter.add(searchFilter);
			filter.add(letterFilter);
		} else if (letterFilter != null) {
			filter = new Filter(letterFilter);

		} else if (searchFilter != null) {
			filter = new Filter(searchFilter);

		} else {
			filter = null;
		}

		return filter;
	}

	public Integer getUserCount(Character letter, String search) throws RODAException {
		int ret;
		// try {
		// TODO migrate to new implementation
		// ret =
		// RodaClientFactory.getRodaClient(getThreadLocalRequest().getSession()).getUserBrowserService()
		// .getUserCount(getFilter(letter, search));
		ret = 0;
		logger.debug("Got " + ret + " users with letter " + letter + " and search '" + search + "'");
		// } catch (RemoteException e) {
		// logger.error("Remote Exception", e);
		// throw RODAClient.parseRemoteException(e);
		// }

		return Integer.valueOf(ret);
	}

	public Integer getGroupCount(Character letter, String search) throws RODAException {
		int ret;
		// try {
		// TODO migrate to new implementation
		// ret =
		// RodaClientFactory.getRodaClient(getThreadLocalRequest().getSession()).getUserBrowserService()
		// .getGroupCount(getFilter(letter, search));
		ret = 0;
		// } catch (RemoteException e) {
		// logger.error("Remote Exception", e);
		// throw RODAClient.parseRemoteException(e);
		// }

		return Integer.valueOf(ret);
	}

	public User[] getUsers(Character letter, String search, int startItem, int limit) throws RODAException {
		User[] users;
		// try {
		ContentAdapter contentAdapter = new ContentAdapter();
		contentAdapter.setFilter(getFilter(letter, search));
		contentAdapter.setSublist(new Sublist(startItem, limit));
		SortParameter[] sortParameters = new SortParameter[1];
		sortParameters[0] = new SortParameter("name", false);
		contentAdapter.setSorter(new Sorter(sortParameters));
		// TODO migrate to new implementation
		// users =
		// RodaClientFactory.getRodaClient(getThreadLocalRequest().getSession()).getUserBrowserService()
		// .getUsers(contentAdapter);
		users = new User[] {};
		if (users == null) {
			users = new User[] {};
		}

		// } catch (RemoteException e) {
		// logger.error("Remote Exception", e);
		// throw RODAClient.parseRemoteException(e);
		// }

		return users;
	}

	public Group[] getGroups(Character letter, String search, int startItem, int limit) throws RODAException {
		Group[] groups;
		// try {
		ContentAdapter contentAdapter = new ContentAdapter();
		contentAdapter.setFilter(getFilter(letter, search));
		contentAdapter.setSublist(new Sublist(startItem, limit));
		SortParameter[] sortParameters = new SortParameter[1];
		sortParameters[0] = new SortParameter("name", false);
		contentAdapter.setSorter(new Sorter(sortParameters));
		// groups =
		// RodaClientFactory.getRodaClient(getThreadLocalRequest().getSession()).getUserBrowserService()
		// .getGroups(contentAdapter);
		groups = new Group[] {};
		if (groups == null) {
			groups = new Group[] {};
		}

		logger.debug("Groups: " + Arrays.asList(groups));
		// } catch (RemoteException e) {
		// logger.error("Remote Exception", e);
		// throw RODAClient.parseRemoteException(e);
		// }

		return groups;
	}

	public Group[] getUserGroups(String username) throws RODAException {
		TreeSet<Group> sortGroupNames = new TreeSet<Group>(new Comparator<Group>() {

			public int compare(Group group0, Group group1) {
				return group0.getName().compareToIgnoreCase(group1.getName());
			}

		});
		try {
			Date start = new Date();
			User user = UserUtility.getLdapUtility().getUser(username);
			long duration = new Date().getTime() - start.getTime();
			LogUtility.registerAction(UserUtility.getClientUser(getThreadLocalRequest().getSession()), "UM.getUser", new String[] { "name", username },
					"User %username% called method UM.getUser(" + username
							+ ")", duration);
			Set<String> groupNames = user.getAllGroups();
			if (groupNames != null) {
				Iterator<String> it = groupNames.iterator();
				while(it.hasNext()){
					String groupName = it.next();
					Group group = UserUtility.getLdapUtility().getGroup(groupName);
					sortGroupNames.add(group);
				}
			}
			duration = new Date().getTime() - start.getTime();
			LogUtility.registerAction(UserUtility.getClientUser(getThreadLocalRequest().getSession()), "UM.getUserGroups", new String[] { "name", username },
					"User %username% called method UM.getUserGroups(" + username
							+ ")", duration);
		} catch (LdapUtilityException e) {
			logger.error("LdapUtility Exception", e);
			throw new RODAException(e.getMessage(),e) {
			};
		}
		return sortGroupNames.toArray(new Group[] {});
	}

	public User[] getGroupUsers(String groupname) throws RODAException {
		User[] userMembersInGroup;

		try {
			Date start = new Date();
			userMembersInGroup = UserUtility.getLdapUtility().getUsersInGroup(groupname);
			long duration = new Date().getTime() - start.getTime();

			LogUtility.registerAction(UserUtility.getClientUser(getThreadLocalRequest().getSession()), "UM.getUserMembersInGroup", new String[] {
					"groupName", groupname },
					"User %username% called method UM.getUserMembersInGroup("
							+ groupname + ")", duration);
			
			if (userMembersInGroup == null) {
				userMembersInGroup = new User[] {};
			}

		} catch (LdapUtilityException e) {
			logger.error("LdapUtility Exception", e);
			throw new RODAException(e.getMessage(),e) {
			};
		}
		return userMembersInGroup;
	}

	public Group getGroup(String groupname) throws RODAException {
		Group group;
		try {
			Date start = new Date();
			group = UserUtility.getLdapUtility().getGroup(groupname);
			long duration = new Date().getTime() - start.getTime();

			LogUtility.registerAction(UserUtility.getClientUser(getThreadLocalRequest().getSession()), "UM.getGroup", new String[] { "groupName",
					groupname },
					"User %username% called method UM.getGroup("
							+ groupname + ")", duration);
		} catch (LdapUtilityException e) {
			logger.error("Remote Exception", e);
			throw new RODAException(e.getMessage(),e) {
			};
		}
		return group;
	}

	public User getUser(String username) throws RODAException {
		User user;
		try {
			Date start = new Date();
			user = UserUtility.getLdapUtility().getUser(username);
			long duration = new Date().getTime() - start.getTime();

			LogUtility.registerAction(UserUtility.getClientUser(getThreadLocalRequest().getSession()), "UM.getUser", new String[] { "userName",
					username },
					"User %username% called method UM.getUser("
							+ username + ")", duration);
		} catch (LdapUtilityException e) {
			logger.error("Remote Exception", e);
			throw new RODAException(e.getMessage(),e) {
			};
		}
		return user;
	}

	public void createUser(User user, String password) throws RODAException {
		try{
			logger.debug("Creating user " + user.getName());
			Date start = new Date();
			User result = UserUtility.getLdapUtility().addUser(user);
			UserUtility.getLdapUtility().setUserPassword(result.getName(), password);
			long duration = new Date().getTime() - start.getTime();
			LogUtility.registerAction(UserUtility.getClientUser(getThreadLocalRequest().getSession()), "UM.createUser", new String[] { "user",
					user.toString() },
					"User %username% called method UM.createUser(" + user
							+ ")", duration);
		}catch(LdapUtilityException e){
			throw new RODAException(e.getMessage(),e) {
			};
		}
	}

	public void editMyUser(User modifiedUser, String password) throws RODAException {
		try {
			Date start = new Date();
			if (modifiedUser.getName().equals(UserUtility.getClientUser(getThreadLocalRequest().getSession()))) {

				User user = UserUtility.getLdapUtility().modifySelfUser(modifiedUser,
						UserUtility.getClientUserPassword(getThreadLocalRequest().getSession()), password);
				long duration = new Date().getTime() - start.getTime();

				LogUtility.registerAction(UserUtility.getClientUser(getThreadLocalRequest().getSession()), "UM.editMyUser",
						new String[] { "modifiedUser", modifiedUser + "",
								"newPassword", "*" },
						"User %username% called method UM.editMyUser("
								+ modifiedUser + ", " + "*" + ")", duration);
			} else {
				throw new IllegalOperationException(
						"Trying to modify user information for another user");
			}
		} catch (LdapUtilityException e) {
			logger.error("LdapUtility Exception", e);
			throw new RODAException(e.getMessage(),e) {
			};
		}

	}

	public void editUser(User user, String password) throws RODAException {
		// try {
		// UserManagement userManagementService = RodaClientFactory
		// .getRodaClient(this.getThreadLocalRequest().getSession()).getUserManagementService();
		//
		// userManagementService.modifyUser(user);
		// if (password != null) {
		// userManagementService.setUserPassword(user.getName(), password);
		// }
		//
		// logger.debug("Editing user: " + user);
		//
		// } catch (RemoteException e) {
		// logger.error("Remote Exception", e);
		// throw RODAClient.parseRemoteException(e);
		// }

	}

	public void createGroup(Group group) throws RODAException {
		try{
			logger.debug("Creating group " + group.getName());
			Date start = new Date();
			Group result = UserUtility.getLdapUtility().addGroup(group);
			long duration = new Date().getTime() - start.getTime();
			LogUtility.registerAction(UserUtility.getClientUser(getThreadLocalRequest().getSession()), "UM.createGroup", new String[] { "group",
					result.toString() },
					"User %username% called method UM.createGroup(" + result
							+ ")", duration);
		}catch(LdapUtilityException e){
			throw new RODAException(e.getMessage(),e) {
			};
		}

	}

	public void editGroup(Group group) throws RODAException {
		try{
			logger.debug("Editing group " + group.getName());
			Date start = new Date();
			UserUtility.getLdapUtility().modifyGroup(group);
			long duration = new Date().getTime() - start.getTime();
			LogUtility.registerAction(UserUtility.getClientUser(getThreadLocalRequest().getSession()), "UM.editGroup", new String[] { "group",
					group.toString() },
					"User %username% called method UM.editGroup(" + group
							+ ")", duration);
		}catch(LdapUtilityException e){
			throw new RODAException(e.getMessage(),e) {
			};
		}
	}

	public boolean removeUser(String username) throws RODAException {
		boolean result = false;
		try{
			logger.debug("Removing user " + username);
			Date start = new Date();
			long logEntriesCount = RodaCoreFactory.getIndexService().count(LogEntry.class, new Filter(new SimpleFilterParameter(
					"username", username)));

			if (logEntriesCount > 0) {
				UserUtility.getLdapUtility().deactivateUser(username);
				result = false;
			} else {
				UserUtility.getLdapUtility().removeUser(username);
				result = true;
			}
			
			long duration = new Date().getTime() - start.getTime();
			LogUtility.registerAction(UserUtility.getClientUser(getThreadLocalRequest().getSession()), "UM.removeUser", new String[] { "user",
					username },
					"User %username% called method UM.removeUser(" + username
							+ ")", duration);
		}catch(LdapUtilityException e){
			throw new RODAException(e.getMessage(),e) {
			};
		} catch (IndexServiceException e) {
			throw new RODAException(e.getMessage(),e) {
			};
		}
		return result;
	}

	public void removeGroup(String groupname) throws RODAException {
		try{
		Date start = new Date();
		UserUtility.getLdapUtility().removeGroup(groupname);
		long duration = new Date().getTime() - start.getTime();

		LogUtility.registerAction(UserUtility.getClientUser(getThreadLocalRequest().getSession()), "UserManagement.removeGroup", new String[] { "groupname",
				groupname },
				"User %username% called method UserManagement.removeGroup("
						+ groupname + ")", duration);
		}catch(LdapUtilityException e){
			throw new RODAException(e.getMessage(),e) {
			};
		} 
	}

	public Set<String> getGroupsRoles(Set<String> groupname) throws RODAException {
		HashSet<String> roleSet = new HashSet<String>();

		try {
			Iterator<String> it = groupname.iterator();
			while(it.hasNext()){
				String groupName = it.next();
				Set<String> roles = UserUtility.getLdapUtility().getGroup(groupName).getAllRoles();
				if (roles != null) {
					roleSet.addAll(roles);
				}
			}
		} catch (LdapUtilityException e) {
			logger.error("LdapUtility Exception", e);
			throw new RODAException(e.getMessage(),e) {
			};
		}
		return roleSet;
	}

	public Set<String> getUserDirectRoles(String username) throws RODAException {
		Set<String> roles;
		try {
			Date start = new Date();
			roles = UserUtility.getLdapUtility().getUserDirectRoles(username);
			long duration = new Date().getTime() - start.getTime();
			LogUtility.registerAction(UserUtility.getClientUser(getThreadLocalRequest().getSession()), "getUserDirectRoles", new String[] {
					"userName", username },
					"User %username% called method UserBrowser.getUserDirectRoles("
							+ username + ")", duration);
			
			if (roles == null) {
				roles = new HashSet<String>();
			}
		} catch (LdapUtilityException e) {
			logger.error("LdapUtility Exception", e);
			throw new RODAClientException(e.getMessage(),e);
		}
		return roles;
	}

	public String[] getGroupDirectRoles(String groupname) throws RODAException {
		String[] roles;
		try {
			Date start = new Date();
			roles = UserUtility.getLdapUtility().getGroupDirectRoles(groupname);
			long duration = new Date().getTime() - start.getTime();
			LogUtility.registerAction(UserUtility.getClientUser(getThreadLocalRequest().getSession()), "getGroupDirectRoles", new String[] {
					"groupName", groupname },
					"User %username% called method UM.getGroupDirectRoles("
							+ groupname + ")", duration);
			
			if (roles == null) {
				roles = new String[] {};
			}
		} catch (LdapUtilityException e) {
			logger.error("LdapUtility Exception", e);
			throw new RODAClientException(e.getMessage(),e);
		}
		return roles;
	}

	public Character[] getUserLetterList(String search) throws RODAException {
		logger.debug("Getting user letter list");

		String[] usernames = getUserNames(null, search);
		Set<Character> letters = new TreeSet<Character>();

		for (int i = 0; i < usernames.length; i++) {
			String username = usernames[i];
			Character letter = Character.toUpperCase(username.charAt(0));
			letters.add(letter);
		}

		return (Character[]) letters.toArray(new Character[] {});
	}

	public Character[] getGroupLetterList(String search) throws RODAException {
		Group[] groups = getGroups(null, search);
		Set<Character> letters = new TreeSet<Character>();

		for (int i = 0; i < groups.length; i++) {
			Group group = groups[i];
			char letter = group.getName().toUpperCase().charAt(0);
			letters.add(Character.valueOf(letter));
		}

		return letters.toArray(new Character[] {});
	}

	public Long getLogEntriesCount(Filter filter) throws RODAException {
		RodaSimpleUser user = UserUtility.getUser(getThreadLocalRequest());
		return UserManagement.countLogEntries(user, filter);
	}

	public IndexResult<LogEntry> findLogEntries(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
			throws AuthorizationDeniedException, GenericException {
		RodaSimpleUser user = UserUtility.getUser(getThreadLocalRequest());
		return UserManagement.findLogEntries(user, filter, sorter, sublist, facets);
	}

	
	//TODO:
	public boolean register(User user, String password, String captcha) throws RODAException {
		boolean successful = false;
		if (CaptchaServiceImpl.check(getThreadLocalRequest().getSession().getId(), captcha).booleanValue()) {
			UserRegistration userRegistrationService;
			User registeredUser;
			//try {
				userRegistrationService = RodaClientFactory.getRodaWuiClient().getUserRegistrationService();
				user.setAllGroups(new HashSet<String>(Arrays.asList("guests")));
				user.setAllRoles(new HashSet<String>());
				//registeredUser = userRegistrationService.registerUser(user, password);
				//successful = sendEmailVerification(registeredUser);

			//} catch (RemoteException e) {
			//	logger.error("Remote Exception", e);
			//	throw RODAClient.parseRemoteException(e);
			//}
		}
		return successful;
	}

	private boolean sendEmailVerification(pt.gov.dgarq.roda.core.data.v2.User user) throws RODAException {
		boolean success = false;

		String token = user.getEmailConfirmationToken();
		String username = user.getName();
		String email = user.getEmail();
		String servletPath = RodaClientFactory.getServletUrl(getThreadLocalRequest());
		String verificationURL = servletPath + "/#verifyemail";
		String verificationCompleteURL;
		try {
			verificationCompleteURL = verificationURL + "." + URLEncoder.encode(username, "UTF-8") + "." + token;
		} catch (UnsupportedEncodingException e1) {
			verificationCompleteURL = verificationURL + "." + username + "." + token;
			logger.error("Error encoding email verification URLs", e1);
		}

		Map<String, String> contextMap = new HashMap<String, String>();
		contextMap.put("username", username);
		contextMap.put("token", token);
		contextMap.put("verificationURL", verificationURL);
		contextMap.put("verificationCompleteURL", verificationCompleteURL);

		try {
			VelocityMail vmail = VelocityMail.getDefaultInstance();
			InternetAddress address = new InternetAddress(email);
			vmail.send("emailverification", address, new VelocityContext(contextMap));
			success = true;
		} catch (AddressException e) {
			Throwable caught = (e.getCause() == null) ? e : e.getCause();
			logger.error("Error sending verification email to " + username + " email address: " + email, caught);
			throw new GenericException(caught.getMessage());
		} catch (UserManagementException e) {
			Throwable caught = (e.getCause() == null) ? e : e.getCause();
			logger.error("Error sending verification email to " + username + " email address: " + email, caught);
		} catch (RemoteException e) {
			throw RODAClient.parseRemoteException(e);
		} catch (Exception e) {
			Throwable caught = (e.getCause() == null) ? e : e.getCause();
			logger.error("Error sending verification email to " + username, caught);
			throw new GenericException(caught.getMessage());
		}

		return success;
	}

	public boolean verifyemail(String username, String token) throws RODAException {
		boolean verified;

		UserRegistration userRegistrationService;
		try {
			userRegistrationService = RodaClientFactory.getRodaWuiClient().getUserRegistrationService();
			userRegistrationService.confirmUserEmail(username, null, token);
			verified = true;
		} catch (InvalidTokenException e) {
			verified = false;
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}
		return verified;
	}

	//TODO
	public boolean resendEmailVerification(String username) throws RODAException {
		boolean success = false;
		User user;
		String error = null;
		try {
			Date start = new Date();
			user = UserUtility.getLdapUtility().getUser(username);
			if (user == null) {
				logger.debug("User " + username
						+ " doesn't exist. Throwing NoSuchUserException.");

				error = "User " + username
						+ " doesn't exist.";
			} else if (user.isActive()
					|| user.getEmailConfirmationToken() == null) {

				logger
						.debug("User "
								+ username
								+ " is already active or email confirmation token doesn't exist.");

				error = "User "
								+ username
								+ " is already active or email confirmation token doesn't exist.";
			}
			
			
			if(error==null){
				//success = sendEmailVerification(user);
				long duration = new Date().getTime() - start.getTime();
				LogUtility.registerAction(UserUtility.getClientUser(getThreadLocalRequest().getSession()), "UM.resendEmailVerification", new String[] {
						"username", username },
						"User %username% called method UM.resendEmailVerification("
								+ username + ")", duration);
			}
			
		} catch (LdapUtilityException e) {
			logger.error("LdapUtility Exception", e);
			throw new RODAException(e.getMessage(),e) {
			};
		}
		if(error!=null){
			throw new RODAException(error) {
			};
		}
		return success;
	}

	public boolean changeUnverifiedEmail(String username, String email) throws RODAException {
		boolean successful = false;
		try {
			pt.gov.dgarq.roda.core.data.v2.User user = RodaClientFactory.getRodaWuiClient().getUserRegistrationService()
					.modifyUnconfirmedEmail(username, email);
			if (user.getEmailConfirmationToken() != null && user.getEmailConfirmationToken().length() > 0) {
				successful = sendEmailVerification(user);
			}
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}
		return successful;
	}

	//TODO
	public boolean requestPassordReset(String usernameOrEmail, String captcha) throws RODAException {
		boolean captchaSuccess;
		if (CaptchaServiceImpl.check(getThreadLocalRequest().getSession().getId(), captcha).booleanValue()) {
			captchaSuccess = true;
			String username = null;
			String email = null;
			if (usernameOrEmail.matches(
					"^[\\w-]+(\\.[\\w-]+)*@([a-z0-9-]+(\\.[a-z0-9-]+)*?\\.[a-z]{2,6}|(\\d{1,3}\\.){3}\\d{1,3})(:\\d{4})?$")) {
				email = usernameOrEmail;
			} else {
				username = usernameOrEmail;
			}

			try {
				pt.gov.dgarq.roda.core.data.v2.User user = RodaClientFactory.getRodaWuiClient().getUserRegistrationService()
						.requestPasswordReset(username, email);
				//sendRecoverLoginEmail(user);
			} catch (RemoteException e) {
				logger.error("Remote Exception", e);
				throw RODAClient.parseRemoteException(e);
			}
		} else {
			captchaSuccess = false;
		}
		return captchaSuccess;
	}

	private boolean sendRecoverLoginEmail(User user) throws RODAException {
		boolean success = false;

		String token = user.getResetPasswordToken();
		String username = user.getName();
		String email = user.getEmail();
		String servletPath = RodaClientFactory.getServletUrl(getThreadLocalRequest());
		String recoverLoginURL = servletPath + "/#resetpassword";
		String recoverLoginCompleteURL;
		try {
			recoverLoginCompleteURL = recoverLoginURL + "." + URLEncoder.encode(username, "UTF-8") + "." + token;
		} catch (UnsupportedEncodingException e1) {
			recoverLoginCompleteURL = recoverLoginURL + "." + username + "." + token;
			logger.error("Error encoding email verification URLs", e1);
		}

		Map<String, String> contextMap = new HashMap<String, String>();
		contextMap.put("username", username);
		contextMap.put("token", token);
		contextMap.put("recoverLoginURL", recoverLoginURL);
		contextMap.put("recoverLoginCompleteURL", recoverLoginCompleteURL);

		try {
			VelocityMail vmail = VelocityMail.getDefaultInstance();
			InternetAddress address = new InternetAddress(email);
			vmail.send("recoverlogin", address, new VelocityContext(contextMap));
			success = true;
		} catch (AddressException e) {
			Throwable caught = (e.getCause() == null) ? e : e.getCause();
			logger.error("Error sending recover login mail to " + username + " email address: " + email, caught);
			throw new GenericException(caught.getMessage());
		} catch (UserManagementException e) {
			Throwable caught = (e.getCause() == null) ? e : e.getCause();
			logger.error("Error sending recover login mail to " + username + " email address: " + email, caught);
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		} catch (Exception e) {
			Throwable caught = (e.getCause() == null) ? e : e.getCause();
			logger.error("Error sending recover login mail to " + username + " email address: " + email, caught);
			throw new GenericException(caught.getMessage());
		}

		return success;
	}

	public void resetPassword(String username, String resetPasswordToken, String newPassword) throws RODAException {
		try {
			RodaClientFactory.getRodaWuiClient().getUserRegistrationService().resetUserPassword(username, newPassword,
					resetPasswordToken);
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}
	}

	public void setUserLogReportInfo(ContentAdapter adapter, String localeString) throws PrintReportException {
		final Locale locale = ServerTools.parseLocale(localeString);
		final UserLogMessages messages = new UserLogMessages(locale);
		// TODO move to new implementation
		// ReportDownload.getInstance().createPDFReport(getThreadLocalRequest().getSession(),
		// new ReportContentSource<LogEntry>() {
		//
		// public int getCount(HttpSession session, Filter filter) throws
		// Exception {
		// return getLogEntriesCount(session, filter);
		// }
		//
		// public LogEntry[] getElements(HttpSession session, ContentAdapter
		// adapter) throws Exception {
		// return getLogEntries(session, adapter);
		// }
		//
		// public Map<String, String> getElementFields(HttpServletRequest req,
		// LogEntry log) {
		// return UserManagementServiceImpl.this.getLogEntryFields(log,
		// messages);
		// }
		//
		// public String getElementId(LogEntry log) {
		// return String.format(messages.getString("log.title"), log.getId());
		//
		// }
		//
		// public String getReportTitle() {
		// return messages.getString("log.report.title");
		// }
		//
		// public String getFieldNameTranslation(String name) {
		// String translation;
		// try {
		// translation = messages.getString("log.label." + name);
		// } catch (MissingResourceException e) {
		// translation = name;
		// }
		//
		// return translation;
		// }
		//
		// public String getFieldValueTranslation(String value) {
		// String translation;
		// try {
		// translation = messages.getString("log.value." + value);
		// } catch (MissingResourceException e) {
		// translation = value;
		// }
		//
		// return translation;
		// }
		//
		// }, adapter);
	}
/*
	private static final Set<String> ALL_ROLES = new HashSet<>(
			Arrays.asList("browse", "search", "administration.user"));
	private static final Set<String> DIRECT_ROLES = new HashSet<>(
			Arrays.asList("browse", "search", "administration.user"));

	private static final Set<String> LFARIA_GROUPS = new HashSet<String>(Arrays.asList("admin"));
	private static final Set<String> ADMIN_ALL_GROUPS = new HashSet<>(
			Arrays.asList("users", "producers", "archivists"));
	private static final Set<String> ADMIN_DIRECT_GROUPS = ADMIN_ALL_GROUPS;

	private static final List<RODAMember> TEST_MEMBERS = new ArrayList<>(Arrays.asList(
			new RodaUser("lfaria", "Luis Faria", "lfaria@keep.pt", false, ALL_ROLES, new HashSet<String>(),
					LFARIA_GROUPS, LFARIA_GROUPS),
			new RodaGroup("admin", "Administrators", ALL_ROLES, DIRECT_ROLES, ADMIN_ALL_GROUPS, ADMIN_DIRECT_GROUPS)));
*/
	@Override
	public Long getMemberCount(Filter filter) throws RODAException {
		try{
			return RodaCoreFactory.getIndexService().count(RODAMember.class, filter)+RodaCoreFactory.getIndexService().count(RODAMember.class, filter);
		}catch(IndexServiceException ise){
			logger.error(ise.getMessage(),ise);
			throw new RODAException(ise.getMessage(),ise) {
			};
		}
		//return Long.valueOf(TEST_MEMBERS.size());
	}

	@Override
	public IndexResult<RODAMember> findMember(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
			throws AuthorizationDeniedException, GenericException {
		try{
			return RodaCoreFactory.getIndexService().find(RODAMember.class, filter, sorter, sublist, facets);
		}catch(IndexServiceException e){
			logger.error(e.getMessage(),e);
			throw new GenericException(e.getMessage());
		}
		/*
		return new IndexResult<RODAMember>(
				sublist.getFirstElementIndex(), sublist
						.getMaximumElementCount(),
				Long.valueOf(TEST_MEMBERS.size()), TEST_MEMBERS,
				Arrays.asList(new FacetFieldResult(RodaConstants.MEMBER_IS_ACTIVE, 2,
						Arrays.asList(new FacetValue("true", 2), new FacetValue("false", 0)), new ArrayList<String>()),
						new FacetFieldResult(RodaConstants.MEMBER_IS_USER, 2,
								Arrays.asList(new FacetValue("true", 1), new FacetValue("false", 1)),
								new ArrayList<String>()),
						new FacetFieldResult(RodaConstants.MEMBER_GROUPS_ALL, 4,
								Arrays.asList(new FacetValue("admin", 1), new FacetValue("archivists", 1),
										new FacetValue("users", 1), new FacetValue("producers", 1)),
								new ArrayList<String>())));*/
	}

	// protected final DateFormat FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd
	// hh:mm:ss.SSS");

	// protected Map<String, String> getLogEntryFields(LogEntry log,
	// UserLogMessages messages) {
	// Map<String, String> ret = new LinkedHashMap<String, String>();
	// ret.put(messages.getString("log.label.address"), log.getAddress());
	// ret.put(messages.getString("log.label.datetime"), log.getDatetime());
	// ret.put(messages.getString("log.label.duration"),
	// FORMAT_DATE.format(log.getDuration()));
	// ret.put(messages.getString("log.label.username"), log.getUsername());
	// String action;
	// try {
	// action = messages.getString("log.value.action." + log.getAction());
	// } catch (MissingResourceException e) {
	// action = log.getAction();
	// }
	// ret.put(messages.getString("log.label.action"), action);
	// // if (log.getDescription() != null) {
	// // ret.put(messages.getString("log.label.description"), log
	// // .getDescription());
	// // }
	// if (log.getRelatedObjectPID() != null) {
	// ret.put(messages.getString("log.label.relatedObjectPID"),
	// log.getRelatedObjectPID());
	// }
	// for (LogEntryParameter parameter : log.getParameters()) {
	// String parameterName;
	// try {
	// parameterName = messages.getString("log.label." + parameter.getName());
	// } catch (MissingResourceException e) {
	// parameterName = parameter.getName();
	// }
	// ret.put(parameterName, parameter.getValue());
	// }
	// return ret;
	// }
}
