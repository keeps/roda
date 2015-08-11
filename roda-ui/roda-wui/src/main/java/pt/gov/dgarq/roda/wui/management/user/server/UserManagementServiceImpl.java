package pt.gov.dgarq.roda.wui.management.user.server;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import config.i18n.server.UserLogMessages;
import pt.gov.dgarq.roda.common.RodaClientFactory;
import pt.gov.dgarq.roda.common.UserUtility;
import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.AuthorizationDeniedException;
import pt.gov.dgarq.roda.core.common.InvalidTokenException;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.common.UserManagementException;
import pt.gov.dgarq.roda.core.data.Group;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.facet.Facets;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.RegexFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.LogEntry;
import pt.gov.dgarq.roda.core.stubs.UserBrowser;
import pt.gov.dgarq.roda.core.stubs.UserEditor;
import pt.gov.dgarq.roda.core.stubs.UserRegistration;
import pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal;
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
		// try {
		ContentAdapter contentAdapter = new ContentAdapter();
		contentAdapter.setFilter(getFilter(letter, search));
		SortParameter[] sortParameters = new SortParameter[1];
		sortParameters[0] = new SortParameter("name", false);
		contentAdapter.setSorter(new Sorter(sortParameters));
		// TODO migrate to new implementation
		// users = RodaClientFactory.getRodaClient(
		// getThreadLocalRequest().getSession())
		// .getUserBrowserService().getUsers(contentAdapter);
		users = new User[] {};
		// } catch (RemoteException e) {
		// logger.error("Remote Exception", e);
		// throw RODAClient.parseRemoteException(e);
		// }

		return users;
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
			filter = new Filter(new FilterParameter[] { searchFilter, letterFilter });
		} else if (letterFilter != null) {
			filter = new Filter(new FilterParameter[] { letterFilter });

		} else if (searchFilter != null) {
			filter = new Filter(new FilterParameter[] { searchFilter });

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
			UserBrowser userBrowser = RodaClientFactory.getRodaClient(this.getThreadLocalRequest().getSession())
					.getUserBrowserService();
			String[] groupNames = userBrowser.getUser(username).getGroups();
			if (groupNames != null) {
				for (int i = 0; i < groupNames.length; i++) {
					String groupName = groupNames[i];
					Group group = userBrowser.getGroup(groupName);
					sortGroupNames.add(group);
				}
			}
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}
		return sortGroupNames.toArray(new Group[] {});
	}

	public User[] getGroupUsers(String groupname) throws RODAException {
		User[] userMembersInGroup;

		try {
			UserBrowser userBrowser = RodaClientFactory.getRodaClient(this.getThreadLocalRequest().getSession())
					.getUserBrowserService();

			userMembersInGroup = userBrowser.getUsersInGroup(groupname);

			if (userMembersInGroup == null) {
				userMembersInGroup = new User[] {};
			}

		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		} catch (Exception e) {
			throw new GenericException(e.getMessage());
		}

		return userMembersInGroup;
	}

	public Group getGroup(String groupname) throws RODAException {
		Group group;
		try {
			UserBrowser userBrowser = RodaClientFactory.getRodaClient(this.getThreadLocalRequest().getSession())
					.getUserBrowserService();
			group = userBrowser.getGroup(groupname);
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}
		return group;
	}

	public User getUser(String username) throws RODAException {
		User user;
		try {
			UserBrowser userBrowser = RodaClientFactory.getRodaClient(this.getThreadLocalRequest().getSession())
					.getUserBrowserService();
			user = userBrowser.getUser(username);
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}
		return user;
	}

	public void createUser(User user, String password) throws RODAException {
		logger.debug("Creating user " + user.getName());

		// try {
		// UserManagement userManagement =
		// RodaClientFactory.getRodaClient(this.getThreadLocalRequest().getSession())
		// .getUserManagementService();
		// userManagement.addUser(user);
		// if (password != null) {
		// userManagement.setUserPassword(user.getName(), password);
		// }
		// } catch (RemoteException e) {
		// logger.error("Remote Exception", e);
		// throw RODAClient.parseRemoteException(e);
		// } catch (NoSuchUserException e) {
		// logger.error("Created user could not be found" + " when changing
		// password: " + user, e);
		// }
	}

	public void editMyUser(User user, String password) throws RODAException {
		try {
			UserEditor userEditorService = RodaClientFactory.getRodaClient(this.getThreadLocalRequest().getSession())
					.getUserEditorService();

			userEditorService.modifyUser(user, password);

		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
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
		// try {
		// UserManagement userManagement =
		// RodaClientFactory.getRodaClient(this.getThreadLocalRequest().getSession())
		// .getUserManagementService();
		// userManagement.addGroup(group);
		// } catch (RemoteException e) {
		// logger.error("Remote Exception", e);
		// throw RODAClient.parseRemoteException(e);
		// }

	}

	public void editGroup(Group group) throws RODAException {
		// try {
		// UserManagement userManagement =
		// RodaClientFactory.getRodaClient(this.getThreadLocalRequest().getSession())
		// .getUserManagementService();
		// userManagement.modifyGroup(group);
		// logger.debug("Editing group: " + group);
		//
		// } catch (RemoteException e) {
		// logger.error("Remote Exception", e);
		// throw RODAClient.parseRemoteException(e);
		// }
	}

	public boolean removeUser(String username) throws RODAException {
		// boolean removed;
		// try {
		// UserManagement userManagement =
		// RodaClientFactory.getRodaClient(this.getThreadLocalRequest().getSession())
		// .getUserManagementService();
		// removed = userManagement.removeUser(username);
		// } catch (RemoteException e) {
		// logger.error("Remote Exception", e);
		// throw RODAClient.parseRemoteException(e);
		// }
		// return removed;
		return false;
	}

	public void removeGroup(String groupname) throws RODAException {
		// try {
		// UserManagement userManagement =
		// RodaClientFactory.getRodaClient(this.getThreadLocalRequest().getSession())
		// .getUserManagementService();
		// userManagement.removeGroup(groupname);
		// } catch (RemoteException e) {
		// logger.error("Remote Exception", e);
		// throw RODAClient.parseRemoteException(e);
		// }

	}

	public String[] getGroupsRoles(String[] groupname) throws RODAException {
		HashSet<String> roleSet = new HashSet<String>();

		try {
			RODAClient rodaClient = RodaClientFactory.getRodaClient(this.getThreadLocalRequest().getSession());
			UserBrowser userBrowser = rodaClient.getUserBrowserService();
			for (int i = 0; i < groupname.length; i++) {
				String[] roles = userBrowser.getGroup(groupname[i]).getRoles();
				if (roles != null) {
					roleSet.addAll(Arrays.asList(roles));
				}
			}
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}

		String[] roleArray = new String[roleSet.size()];
		Object[] roleSetArray = roleSet.toArray();
		for (int i = 0; i < roleSet.size(); i++) {
			roleArray[i] = (String) roleSetArray[i];
		}

		return roleArray;
	}

	public String[] getUserDirectRoles(String username) throws RODAException {
		String[] roles;
		try {
			RODAClient rodaClient = RodaClientFactory.getRodaClient(this.getThreadLocalRequest().getSession());
			roles = rodaClient.getUserBrowserService().getUserDirectRoles(username);
			if (roles == null) {
				roles = new String[] {};
			}
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}
		return roles;
	}

	public String[] getGroupDirectRoles(String groupname) throws RODAException {

		String[] roles;
		try {
			RODAClient rodaClient = RodaClientFactory.getRodaClient(this.getThreadLocalRequest().getSession());
			roles = rodaClient.getUserBrowserService().getGroupDirectRoles(groupname);
			if (roles == null) {
				roles = new String[] {};
			}
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
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
		CASUserPrincipal user = UserUtility.getUser(getThreadLocalRequest());
		return UserManagement.countLogEntries(user, filter);
	}

	public IndexResult<LogEntry> findLogEntries(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
			throws AuthorizationDeniedException, GenericException {
		CASUserPrincipal user = UserUtility.getUser(getThreadLocalRequest());
		return UserManagement.findLogEntries(user, filter, sorter, sublist, facets);
	}

	public boolean register(User user, String password, String captcha) throws RODAException {
		boolean successful = false;
		if (CaptchaServiceImpl.check(getThreadLocalRequest().getSession().getId(), captcha).booleanValue()) {
			UserRegistration userRegistrationService;
			User registeredUser;
			try {
				userRegistrationService = RodaClientFactory.getRodaWuiClient().getUserRegistrationService();
				user.setGroups(new String[] { "guests" });
				user.setRoles(new String[] {});
				registeredUser = userRegistrationService.registerUser(user, password);
				successful = sendEmailVerification(registeredUser);

			} catch (RemoteException e) {
				logger.error("Remote Exception", e);
				throw RODAClient.parseRemoteException(e);
			}
		}
		return successful;
	}

	private boolean sendEmailVerification(User user) throws RODAException {
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

	public boolean resendEmailVerification(String username) throws RODAException {
		boolean success = false;
		User user;
		try {
			user = RodaClientFactory.getRodaWuiClient().getUserRegistrationService().getUnconfirmedUser(username);
			success = sendEmailVerification(user);
		} catch (RemoteException e) {
			logger.error("Remote Exception", e);
			throw RODAClient.parseRemoteException(e);
		}

		return success;
	}

	public boolean changeUnverifiedEmail(String username, String email) throws RODAException {
		boolean successful = false;
		try {
			User user = RodaClientFactory.getRodaWuiClient().getUserRegistrationService()
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
				User user = RodaClientFactory.getRodaWuiClient().getUserRegistrationService()
						.requestPasswordReset(username, email);
				sendRecoverLoginEmail(user);
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
