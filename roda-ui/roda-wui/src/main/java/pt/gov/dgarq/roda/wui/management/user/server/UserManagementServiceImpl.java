package pt.gov.dgarq.roda.wui.management.user.server;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.roda.common.LdapUtilityException;
import org.roda.common.UserUtility;
import org.roda.index.IndexServiceException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import config.i18n.server.UserLogMessages;
import pt.gov.dgarq.roda.common.I18nUtility;
import pt.gov.dgarq.roda.common.LogUtility;
import pt.gov.dgarq.roda.common.RodaCoreFactory;
import pt.gov.dgarq.roda.core.common.AuthorizationDeniedException;
import pt.gov.dgarq.roda.core.common.IllegalOperationException;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.facet.Facets;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.Group;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.LogEntry;
import pt.gov.dgarq.roda.core.data.v2.RODAMember;
import pt.gov.dgarq.roda.core.data.v2.RodaGroup;
import pt.gov.dgarq.roda.core.data.v2.RodaUser;
import pt.gov.dgarq.roda.core.data.v2.User;
import pt.gov.dgarq.roda.wui.common.client.GenericException;
import pt.gov.dgarq.roda.wui.common.client.PrintReportException;
import pt.gov.dgarq.roda.wui.common.server.ServerTools;
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

  @Override
  public Long getMemberCount(Filter filter) throws AuthorizationDeniedException, GenericException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return UserManagement.countMembers(user, filter);
  }

  @Override
  public IndexResult<RODAMember> findMembers(Filter filter, Sorter sorter, Sublist sublist, Facets facets,
    String localeString) throws AuthorizationDeniedException, GenericException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    IndexResult<RODAMember> result = UserManagement.findMembers(user, filter, sorter, sublist, facets);
    return I18nUtility.translate(result, localeString);
  }

  @Override
  public RodaGroup getGroup(String groupname) throws AuthorizationDeniedException, GenericException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return UserManagement.retrieveGroup(user, groupname);
  }

  @Override
  public List<Group> listAllGroups() throws AuthorizationDeniedException, GenericException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return UserManagement.listAllGroups(user);
  }

  @Override
  public User getUser(String username) throws RODAException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return UserManagement.retrieveUser(user, username);
  }

  @Override
  public void createUser(User user, String password) throws RODAException {
    try {
      logger.debug("Creating user " + user.getName());
      Date start = new Date();
      User result = UserUtility.getLdapUtility().addUser(user);
      UserUtility.getLdapUtility().setUserPassword(result.getName(), password);
      long duration = new Date().getTime() - start.getTime();
      LogUtility.registerAction(UserUtility.getClientUser(getThreadLocalRequest().getSession()), "UM.createUser",
        new String[] {"user", user.toString()}, "User %username% called method UM.createUser(" + user + ")", duration);
    } catch (LdapUtilityException e) {
      throw new RODAException(e.getMessage(), e) {
      };
    }
  }

  @Override
  public void editMyUser(User modifiedUser, String password) throws RODAException {
    try {
      Date start = new Date();
      if (modifiedUser.getName().equals(UserUtility.getClientUser(getThreadLocalRequest().getSession()))) {

        User user = UserUtility.getLdapUtility().modifySelfUser(modifiedUser,
          UserUtility.getClientUserPassword(getThreadLocalRequest().getSession()), password);
        long duration = new Date().getTime() - start.getTime();

        LogUtility.registerAction(UserUtility.getClientUser(getThreadLocalRequest().getSession()), "UM.editMyUser",
          new String[] {"modifiedUser", modifiedUser + "", "newPassword", "*"},
          "User %username% called method UM.editMyUser(" + modifiedUser + ", " + "*" + ")", duration);
      } else {
        throw new IllegalOperationException("Trying to modify user information for another user");
      }
    } catch (LdapUtilityException e) {
      logger.error("LdapUtility Exception", e);
      throw new RODAException(e.getMessage(), e) {
      };
    }

  }

  @Override
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

  @Override
  public void createGroup(Group group) throws RODAException {
    try {
      logger.debug("Creating group " + group.getName());
      Date start = new Date();
      Group result = UserUtility.getLdapUtility().addGroup(group);
      long duration = new Date().getTime() - start.getTime();
      LogUtility.registerAction(UserUtility.getClientUser(getThreadLocalRequest().getSession()), "UM.createGroup",
        new String[] {"group", result.toString()}, "User %username% called method UM.createGroup(" + result + ")",
        duration);
    } catch (LdapUtilityException e) {
      throw new RODAException(e.getMessage(), e) {
      };
    }

  }

  @Override
  public void editGroup(Group group) throws RODAException {
    try {
      logger.debug("Editing group " + group.getName());
      Date start = new Date();
      UserUtility.getLdapUtility().modifyGroup(group);
      long duration = new Date().getTime() - start.getTime();
      LogUtility.registerAction(UserUtility.getClientUser(getThreadLocalRequest().getSession()), "UM.editGroup",
        new String[] {"group", group.toString()}, "User %username% called method UM.editGroup(" + group + ")",
        duration);
    } catch (LdapUtilityException e) {
      throw new RODAException(e.getMessage(), e) {
      };
    }
  }

  @Override
  public boolean removeUser(String username) throws RODAException {
    boolean result = false;
    try {
      logger.debug("Removing user " + username);
      Date start = new Date();
      long logEntriesCount = RodaCoreFactory.getIndexService().count(LogEntry.class,
        new Filter(new SimpleFilterParameter("username", username)));

      if (logEntriesCount > 0) {
        UserUtility.getLdapUtility().deactivateUser(username);
        result = false;
      } else {
        UserUtility.getLdapUtility().removeUser(username);
        result = true;
      }

      long duration = new Date().getTime() - start.getTime();
      LogUtility.registerAction(UserUtility.getClientUser(getThreadLocalRequest().getSession()), "UM.removeUser",
        new String[] {"user", username}, "User %username% called method UM.removeUser(" + username + ")", duration);
    } catch (LdapUtilityException e) {
      throw new RODAException(e.getMessage(), e) {
      };
    } catch (IndexServiceException e) {
      throw new RODAException(e.getMessage(), e) {
      };
    }
    return result;
  }

  @Override
  public void removeGroup(String groupname) throws RODAException {
    try {
      Date start = new Date();
      UserUtility.getLdapUtility().removeGroup(groupname);
      long duration = new Date().getTime() - start.getTime();

      LogUtility.registerAction(UserUtility.getClientUser(getThreadLocalRequest().getSession()),
        "UserManagement.removeGroup", new String[] {"groupname", groupname},
        "User %username% called method UserManagement.removeGroup(" + groupname + ")", duration);
    } catch (LdapUtilityException e) {
      throw new RODAException(e.getMessage(), e) {
      };
    }
  }

  @Override
  public Long getLogEntriesCount(Filter filter) throws RODAException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return UserManagement.countLogEntries(user, filter);
  }

  @Override
  public IndexResult<LogEntry> findLogEntries(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws AuthorizationDeniedException, GenericException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return UserManagement.findLogEntries(user, filter, sorter, sublist, facets);
  }

  @Override
  public boolean register(User user, String password, String captcha) throws RODAException {
    boolean successful = false;
    // FIXME
    // if
    // (CaptchaServiceImpl.check(getThreadLocalRequest().getSession().getId(),
    // captcha).booleanValue()) {
    // UserRegistration userRegistrationService;
    // User registeredUser;
    // // try {
    // userRegistrationService =
    // RodaClientFactory.getRodaWuiClient().getUserRegistrationService();
    // user.setAllGroups(new HashSet<String>(Arrays.asList("guests")));
    // user.setAllRoles(new HashSet<String>());
    // // registeredUser = userRegistrationService.registerUser(user,
    // // password);
    // // successful = sendEmailVerification(registeredUser);
    //
    // // } catch (RemoteException e) {
    // // logger.error("Remote Exception", e);
    // // throw RODAClient.parseRemoteException(e);
    // // }
    // }
    return successful;
  }

  private boolean sendEmailVerification(pt.gov.dgarq.roda.core.data.v2.User user) throws RODAException {
    boolean success = false;

    // FIXME
    // String token = user.getEmailConfirmationToken();
    // String username = user.getName();
    // String email = user.getEmail();
    // String servletPath =
    // RodaClientFactory.getServletUrl(getThreadLocalRequest());
    // String verificationURL = servletPath + "/#verifyemail";
    // String verificationCompleteURL;
    // try {
    // verificationCompleteURL = verificationURL + "." +
    // URLEncoder.encode(username, "UTF-8") + "." + token;
    // } catch (UnsupportedEncodingException e1) {
    // verificationCompleteURL = verificationURL + "." + username + "." +
    // token;
    // logger.error("Error encoding email verification URLs", e1);
    // }
    //
    // Map<String, String> contextMap = new HashMap<String, String>();
    // contextMap.put("username", username);
    // contextMap.put("token", token);
    // contextMap.put("verificationURL", verificationURL);
    // contextMap.put("verificationCompleteURL", verificationCompleteURL);
    //
    // try {
    // VelocityMail vmail = VelocityMail.getDefaultInstance();
    // InternetAddress address = new InternetAddress(email);
    // vmail.send("emailverification", address, new
    // VelocityContext(contextMap));
    // success = true;
    // } catch (AddressException e) {
    // Throwable caught = (e.getCause() == null) ? e : e.getCause();
    // logger.error("Error sending verification email to " + username + "
    // email address: " + email, caught);
    // throw new GenericException(caught.getMessage());
    // } catch (UserManagementException e) {
    // Throwable caught = (e.getCause() == null) ? e : e.getCause();
    // logger.error("Error sending verification email to " + username + "
    // email address: " + email, caught);
    // } catch (RemoteException e) {
    // throw RODAClient.parseRemoteException(e);
    // } catch (Exception e) {
    // Throwable caught = (e.getCause() == null) ? e : e.getCause();
    // logger.error("Error sending verification email to " + username,
    // caught);
    // throw new GenericException(caught.getMessage());
    // }

    return success;
  }

  @Override
  public boolean verifyemail(String username, String token) throws RODAException {
    boolean verified = false;

    // FIXME
    // UserRegistration userRegistrationService;
    // try {
    // userRegistrationService =
    // RodaClientFactory.getRodaWuiClient().getUserRegistrationService();
    // userRegistrationService.confirmUserEmail(username, null, token);
    // verified = true;
    // } catch (InvalidTokenException e) {
    // verified = false;
    // } catch (RemoteException e) {
    // logger.error("Remote Exception", e);
    // throw RODAClient.parseRemoteException(e);
    // }
    return verified;
  }

  @Override
  public boolean resendEmailVerification(String username) throws RODAException {
    boolean success = false;
    User user;
    String error = null;
    try {
      Date start = new Date();
      user = UserUtility.getLdapUtility().getUser(username);
      if (user == null) {
        logger.debug("User " + username + " doesn't exist. Throwing NoSuchUserException.");

        error = "User " + username + " doesn't exist.";
      } else if (user.isActive() || user.getEmailConfirmationToken() == null) {

        logger.debug("User " + username + " is already active or email confirmation token doesn't exist.");

        error = "User " + username + " is already active or email confirmation token doesn't exist.";
      }

      if (error == null) {
        // success = sendEmailVerification(user);
        long duration = new Date().getTime() - start.getTime();
        LogUtility.registerAction(UserUtility.getClientUser(getThreadLocalRequest().getSession()),
          "UM.resendEmailVerification", new String[] {"username", username},
          "User %username% called method UM.resendEmailVerification(" + username + ")", duration);
      }

    } catch (LdapUtilityException e) {
      logger.error("LdapUtility Exception", e);
      throw new RODAException(e.getMessage(), e) {
      };
    }
    if (error != null) {
      throw new RODAException(error) {
      };
    }
    return success;
  }

  @Override
  public boolean changeUnverifiedEmail(String username, String email) throws RODAException {
    boolean successful = false;
    // FIXME
    // try {
    // pt.gov.dgarq.roda.core.data.v2.User user =
    // RodaClientFactory.getRodaWuiClient().getUserRegistrationService()
    // .modifyUnconfirmedEmail(username, email);
    // if (user.getEmailConfirmationToken() != null &&
    // user.getEmailConfirmationToken().length() > 0) {
    // successful = sendEmailVerification(user);
    // }
    // } catch (RemoteException e) {
    // logger.error("Remote Exception", e);
    // throw RODAClient.parseRemoteException(e);
    // }
    return successful;
  }

  @Override
  public boolean requestPassordReset(String usernameOrEmail, String captcha) throws RODAException {
    boolean captchaSuccess = false;
    // FIXME
    // if
    // (CaptchaServiceImpl.check(getThreadLocalRequest().getSession().getId(),
    // captcha).booleanValue()) {
    // captchaSuccess = true;
    // String username = null;
    // String email = null;
    // if (usernameOrEmail.matches(
    // "^[\\w-]+(\\.[\\w-]+)*@([a-z0-9-]+(\\.[a-z0-9-]+)*?\\.[a-z]{2,6}|(\\d{1,3}\\.){3}\\d{1,3})(:\\d{4})?$"))
    // {
    // email = usernameOrEmail;
    // } else {
    // username = usernameOrEmail;
    // }
    //
    // try {
    // pt.gov.dgarq.roda.core.data.v2.User user =
    // RodaClientFactory.getRodaWuiClient()
    // .getUserRegistrationService().requestPasswordReset(username, email);
    // // sendRecoverLoginEmail(user);
    // } catch (RemoteException e) {
    // logger.error("Remote Exception", e);
    // throw RODAClient.parseRemoteException(e);
    // }
    // } else {
    // captchaSuccess = false;
    // }
    return captchaSuccess;
  }

  private boolean sendRecoverLoginEmail(User user) throws RODAException {
    boolean success = false;
    // FIXME
    // String token = user.getResetPasswordToken();
    // String username = user.getName();
    // String email = user.getEmail();
    // String servletPath =
    // RodaClientFactory.getServletUrl(getThreadLocalRequest());
    // String recoverLoginURL = servletPath + "/#resetpassword";
    // String recoverLoginCompleteURL;
    // try {
    // recoverLoginCompleteURL = recoverLoginURL + "." +
    // URLEncoder.encode(username, "UTF-8") + "." + token;
    // } catch (UnsupportedEncodingException e1) {
    // recoverLoginCompleteURL = recoverLoginURL + "." + username + "." +
    // token;
    // logger.error("Error encoding email verification URLs", e1);
    // }
    //
    // Map<String, String> contextMap = new HashMap<String, String>();
    // contextMap.put("username", username);
    // contextMap.put("token", token);
    // contextMap.put("recoverLoginURL", recoverLoginURL);
    // contextMap.put("recoverLoginCompleteURL", recoverLoginCompleteURL);
    //
    // try {
    // VelocityMail vmail = VelocityMail.getDefaultInstance();
    // InternetAddress address = new InternetAddress(email);
    // vmail.send("recoverlogin", address, new VelocityContext(contextMap));
    // success = true;
    // } catch (AddressException e) {
    // Throwable caught = (e.getCause() == null) ? e : e.getCause();
    // logger.error("Error sending recover login mail to " + username + "
    // email address: " + email, caught);
    // throw new GenericException(caught.getMessage());
    // } catch (UserManagementException e) {
    // Throwable caught = (e.getCause() == null) ? e : e.getCause();
    // logger.error("Error sending recover login mail to " + username + "
    // email address: " + email, caught);
    // } catch (RemoteException e) {
    // logger.error("Remote Exception", e);
    // throw RODAClient.parseRemoteException(e);
    // } catch (Exception e) {
    // Throwable caught = (e.getCause() == null) ? e : e.getCause();
    // logger.error("Error sending recover login mail to " + username + "
    // email address: " + email, caught);
    // throw new GenericException(caught.getMessage());
    // }

    return success;
  }

  @Override
  public void resetPassword(String username, String resetPasswordToken, String newPassword) throws RODAException {
    // FIXME
    // try {
    // RodaClientFactory.getRodaWuiClient().getUserRegistrationService().resetUserPassword(username,
    // newPassword,
    // resetPasswordToken);
    // } catch (RemoteException e) {
    // logger.error("Remote Exception", e);
    // throw RODAClient.parseRemoteException(e);
    // }
  }

  @Override
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
   * private static final Set<String> ALL_ROLES = new HashSet<>(
   * Arrays.asList("browse", "search", "administration.user")); private static
   * final Set<String> DIRECT_ROLES = new HashSet<>( Arrays.asList("browse",
   * "search", "administration.user"));
   * 
   * private static final Set<String> LFARIA_GROUPS = new
   * HashSet<String>(Arrays.asList("admin")); private static final Set<String>
   * ADMIN_ALL_GROUPS = new HashSet<>( Arrays.asList("users", "producers",
   * "archivists")); private static final Set<String> ADMIN_DIRECT_GROUPS =
   * ADMIN_ALL_GROUPS;
   * 
   * private static final List<RODAMember> TEST_MEMBERS = new
   * ArrayList<>(Arrays.asList( new RodaUser("lfaria", "Luis Faria",
   * "lfaria@keep.pt", false, ALL_ROLES, new HashSet<String>(), LFARIA_GROUPS,
   * LFARIA_GROUPS), new RodaGroup("admin", "Administrators", ALL_ROLES,
   * DIRECT_ROLES, ADMIN_ALL_GROUPS, ADMIN_DIRECT_GROUPS)));
   */

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
