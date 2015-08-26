package pt.gov.dgarq.roda.wui.common.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.roda.common.UserUtility;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import pt.gov.dgarq.roda.common.RodaCoreFactory;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.data.LogEntry;
import pt.gov.dgarq.roda.core.data.LogEntryParameter;
import pt.gov.dgarq.roda.core.data.v2.RodaSimpleUser;
import pt.gov.dgarq.roda.core.data.v2.RodaUser;
import pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal;
import pt.gov.dgarq.roda.wui.common.client.AuthenticatedUser;
import pt.gov.dgarq.roda.wui.common.client.UserLoginService;

/**
 * User login servlet
 * 
 * @author Luis Faria
 * 
 */
public class UserLoginServiceImpl extends RemoteServiceServlet implements UserLoginService {

	/**
	 * 
	 */

	public static UserLoginServiceImpl getInstance() {
		return new UserLoginServiceImpl();
	}

	private static final long serialVersionUID = -6898933466651262033L;

	private static final String LOG_ACTION_WUI_LOGIN = "RODAWUI.login";

	private static Logger logger = Logger.getLogger(UserLoginServiceImpl.class);

	public AuthenticatedUser getAuthenticatedUser() throws RODAException {
		// AuthenticatedUser officeUser;
		// RODAClient rodaClient;
		//
		// rodaClient = RodaClientFactory.getRodaClient(this
		// .getThreadLocalRequest().getSession());
		// User user;
		// try {
		// user = rodaClient.getAuthenticatedUser();
		// officeUser = new AuthenticatedUser(user, rodaClient.isGuestLogin());
		// } catch (RemoteException e) {
		// throw RODAClient.parseRemoteException(e);
		// }
		//
		// return officeUser;

		RodaUser user = UserUtility.getUser(this.getThreadLocalRequest(), RodaCoreFactory.getIndexService());
		AuthenticatedUser u = new AuthenticatedUser();
		u.setId(user.getId());
		u.setName(user.getName());
		u.setFullName(user.getFullName());
		u.setGuest(user.isGuest());
		Set<String> roles = user.getAllRoles();

		u.setAllRoles(roles);

		logger.info("Serving user " + u + " from user " + user);
		return u;

	}

	public AuthenticatedUser loginCUP(HttpServletRequest request, CASUserPrincipal cup) throws RODAException {
		logger.info("Login with CUP: " + cup);
		UserUtility.setUser(request, new RodaSimpleUser());

		AuthenticatedUser authenticatedUser;
		authenticatedUser = getAuthenticatedUser();
		return authenticatedUser;
	}

	public AuthenticatedUser login(String username, String password) throws RODAException {
		AuthenticatedUser authenticatedUser = null;

		// RodaClientFactory.login(this.getThreadLocalRequest(),
		// username, password);
		// logLogin(username);
		// authenticatedUser = getAuthenticatedUser();

		return authenticatedUser;
	}

	protected void logLogin(String username) {
		try {
			LogEntryParameter[] parameters = new LogEntryParameter[] {
					new LogEntryParameter("hostname", getThreadLocalRequest().getRemoteHost()),
					new LogEntryParameter("address", getThreadLocalRequest().getRemoteAddr()),
					new LogEntryParameter("port", getThreadLocalRequest().getRemotePort() + "") };

			LogEntry logEntry = new LogEntry();
			logEntry.setAction(LOG_ACTION_WUI_LOGIN);
			logEntry.setParameters(parameters);
			logEntry.setUsername(username);

			// RodaClientFactory.getRodaWuiClient().getLoggerService()
			// .addLogEntry(logEntry);

			// } catch (RemoteException e) {
			// logger.error("Error logging login", e);
		} catch (Exception e) {
			logger.error("Error logging login", e);
		}
	}

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
