package pt.gov.dgarq.roda.wui.common.server;

import java.util.Map;

import javax.naming.AuthenticationException;

import org.apache.log4j.Logger;
import org.roda.common.LdapUtilityException;
import org.roda.common.UserUtility;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import pt.gov.dgarq.roda.common.RodaCoreFactory;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.data.v2.RodaSimpleUser;
import pt.gov.dgarq.roda.core.data.v2.RodaUser;
import pt.gov.dgarq.roda.core.data.v2.User;
import pt.gov.dgarq.roda.wui.common.client.AuthenticatedUser;
import pt.gov.dgarq.roda.wui.common.client.UserLoginService;

/**
 * User login servlet
 * 
 * @author Luis Faria
 * 
 */
public class UserLoginServiceImpl extends RemoteServiceServlet implements UserLoginService {

	private static final long serialVersionUID = -6898933466651262033L;
	private static final String LOG_ACTION_WUI_LOGIN = "RODAWUI.login";
	private static Logger logger = Logger.getLogger(UserLoginServiceImpl.class);

	public static UserLoginServiceImpl getInstance() {
		return new UserLoginServiceImpl();
	}

	public AuthenticatedUser getAuthenticatedUser() throws RODAException {
		RodaUser user = UserUtility.getUser(this.getThreadLocalRequest(), RodaCoreFactory.getIndexService());
		AuthenticatedUser u = new AuthenticatedUser(new User(user), user.isGuest());
		u.setAllRoles(user.getAllRoles());
		logger.debug("Serving user " + u + " from user " + user);
		return u;
	}

	// public AuthenticatedUser loginCUP(HttpServletRequest request,
	// CASUserPrincipal cup) throws RODAException {
	// logger.info("Login with CUP: " + cup);
	// UserUtility.setUser(request, new RodaSimpleUser());
	//
	// AuthenticatedUser authenticatedUser;
	// authenticatedUser = getAuthenticatedUser();
	// return authenticatedUser;
	// }

	public AuthenticatedUser login(String username, String password) throws RODAException {
		// FIXME log action
		try {
			User user = UserUtility.getLdapUtility().getAuthenticatedUser(username, password);
			AuthenticatedUser u = new AuthenticatedUser(user, user.isGuest());
			u.setAllRoles(user.getAllRoles());
			UserUtility.setUser(this.getThreadLocalRequest(),
					new RodaSimpleUser(u.getId(), u.getName(), u.getEmail(), u.isGuest()));
			return u;
		} catch (AuthenticationException e) {
			// FIXME see if this is the best exception to be thrown
			throw new LdapUtilityException(e);
		}
	}

	// protected void logLogin(String username) {
	// try {
	// LogEntryParameter[] parameters = new LogEntryParameter[] {
	// new LogEntryParameter("hostname",
	// getThreadLocalRequest().getRemoteHost()),
	// new LogEntryParameter("address",
	// getThreadLocalRequest().getRemoteAddr()),
	// new LogEntryParameter("port", getThreadLocalRequest().getRemotePort() +
	// "") };
	//
	// LogEntry logEntry = new LogEntry();
	// logEntry.setAction(LOG_ACTION_WUI_LOGIN);
	// logEntry.setParameters(parameters);
	// logEntry.setUsername(username);
	//
	// // RodaClientFactory.getRodaWuiClient().getLoggerService()
	// // .addLogEntry(logEntry);
	//
	// // } catch (RemoteException e) {
	// // logger.error("Error logging login", e);
	// } catch (Exception e) {
	// logger.error("Error logging login", e);
	// }
	// }

	// FIXME perhaps this should be removed
	public AuthenticatedUser logout() throws RODAException {
		AuthenticatedUser authenticatedUser = null;

		// RodaClientFactory.logout(this.getThreadLocalRequest().getSession());
		// authenticatedUser = getAuthenticatedUser();

		return authenticatedUser;
	}

	public Map<String, String> getRodaProperties() {
		return RodaCoreFactory.getLoginRelatedProperties();

	}

	@Override
	public String getRodaCasURL() {
		return RodaCoreFactory.getRodaConfiguration().getString("roda.cas.external.url", "");
	}
}
