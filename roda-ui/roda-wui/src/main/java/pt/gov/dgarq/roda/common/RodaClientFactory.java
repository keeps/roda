/**
 * 
 */
package pt.gov.dgarq.roda.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.LoggerException;
import pt.gov.dgarq.roda.core.common.LoginException;
import pt.gov.dgarq.roda.core.common.RODAClientException;
import pt.gov.dgarq.roda.core.data.LogEntry;
import pt.gov.dgarq.roda.core.data.LogEntryParameter;

/**
 * @author Luis Faria
 * 
 */
public class RodaClientFactory {

	private static final String RODA_CLIENT_SESSION_ATTRIBUTE = "RODA_CLIENT";

	/**
	 * The string token to use on the session attribute that keeps the
	 * roda-client
	 */
	private static final Logger logger = Logger
			.getLogger(RodaClientFactory.class);

	private static Properties rodaProperties;

	private static URL rodaCoreURL;

	private static RODAClient rodaWuiClient = null;

	static {
		try {
			rodaProperties = new Properties();
			InputStream relsStream = getConfigurationFile("roda-wui.properties");
			rodaProperties.load(relsStream);
			rodaCoreURL = getRodaCoreUrl();

		} catch (IOException e) {
			logger.fatal("Error while parsing roda-wui properties", e);
		}

	}

	/**
	 * Get the properties from the main configuration file
	 * 
	 * @return the properties
	 */
	public static Properties getRodaProperties() {
		return rodaProperties;
	}

	/**
	 * Get the RODA Core services host
	 * 
	 * @return the URL of the host
	 * @throws IOException
	 */
	public static URL getRodaCoreUrl() throws IOException {
		URL rodaServices;
		rodaServices = new URL(getRodaProperties().getProperty("roda.core.url"));
		return rodaServices;
	}

	/**
	 * Get the RODA client kept in session
	 * 
	 * @param session
	 *            the request session
	 * @return the RODA client
	 * @throws LoginException
	 * @throws RODAClientException
	 */
	public static RODAClient getRodaClient(HttpSession session)
			throws LoginException, RODAClientException {
		RODAClient rodaClient = (RODAClient) session
				.getAttribute(RODA_CLIENT_SESSION_ATTRIBUTE);
		if (rodaClient == null) {
			try {
				logger.info("No RODA Client in session, creating a guest one");
				rodaClient = new RODAClient(rodaCoreURL);
			} catch (LoginException e) {
				throw new LoginException(e.getMessage());
			}
			session.setAttribute(RODA_CLIENT_SESSION_ATTRIBUTE, rodaClient);

		}
		return rodaClient;
	}

	/**
	 * Get the servlet base URL
	 * 
	 * @param req
	 *            the user HTTP request
	 * @return the request URL used to access this servlet
	 */
	public static String getServletUrl(HttpServletRequest req) {
		String scheme = req.getScheme();
		String serverName = req.getServerName();
		int serverPort = req.getServerPort();
		String contextPath = req.getContextPath();

		String url = scheme + "://" + serverName + ":" + serverPort
				+ contextPath;
		return url;
	}

	/**
	 * Login into RODA Core. Create a RODA client with a defined username and
	 * password and keep it in session
	 * 
	 * @param session
	 *            the request session
	 * @param username
	 *            the authenticating user name
	 * @param password
	 *            the authenticating user passord
	 * @throws LoginException
	 * @throws RODAClientException
	 */
	public static void login(HttpSession session, String username,
			String password) throws LoginException, RODAClientException {
		RODAClient rodaClient;
		try {
			rodaClient = new RODAClient(rodaCoreURL, username, password);
			logger.info("Login as " + username + " successful");
			session.setAttribute(RODA_CLIENT_SESSION_ATTRIBUTE, rodaClient);
		} catch (LoginException e) {
			logger.error("Login as " + username + " failed", e);
			throw new LoginException(e.getMessage());
		}

	}

	/**
	 * Logout from RODA Core. Remove the logged RODA client from session.
	 * 
	 * @param session
	 *            the request session
	 * @throws LoginException
	 * @throws RODAClientException
	 */
	public static void logout(HttpSession session) throws LoginException,
			RODAClientException {
		RODAClient rodaClient;
		try {
			rodaClient = new RODAClient(rodaCoreURL);
			session.setAttribute(RODA_CLIENT_SESSION_ATTRIBUTE, rodaClient);
		} catch (LoginException e) {
			throw new LoginException(e.getMessage());
		}

	}

	/**
	 * Get the roda-wui RODA client
	 * 
	 * @return the RODA client with roda-wui login
	 * @throws LoginException
	 * @throws RODAClientException
	 */
	public static RODAClient getRodaWuiClient() throws LoginException,
			RODAClientException {
		if (rodaWuiClient == null) {
			try {
				rodaWuiClient = new RODAClient(rodaCoreURL, rodaProperties
						.getProperty("roda.wui.user.name"), rodaProperties
						.getProperty("roda.wui.user.password"));
			} catch (LoginException e) {
				throw new LoginException(e.getMessage());
			}
		}
		return rodaWuiClient;

	}

	/**
	 * Log disseminator access
	 * 
	 * @param disseminator
	 * @param hit
	 * @param action
	 * @param request
	 */
	public static void log(String disseminator, boolean hit, String pid,
			HttpServletRequest request) {
		String username;
		try {
			username = getRodaClient(request.getSession()).getUsername();
			List<LogEntryParameter> parameters = new Vector<LogEntryParameter>();
			parameters.add(new LogEntryParameter("hostname", request
					.getRemoteHost()));
			parameters.add(new LogEntryParameter("address", request
					.getRemoteAddr()));
			parameters.add(new LogEntryParameter("port", request
					.getRemotePort()
					+ ""));
			parameters.add(new LogEntryParameter("pid", pid));
			LogEntry logEntry = new LogEntry();
			logEntry.setAction("disseminator." + (hit ? "hit" : "miss") + "."
					+ disseminator);
			logEntry.setParameters(parameters
					.toArray(new LogEntryParameter[parameters.size()]));
			logEntry.setUsername(username);
			getRodaWuiClient().getLoggerService().addLogEntry(logEntry);
		} catch (LoginException e) {
			logger.error("Error sending log", e);
		} catch (RODAClientException e) {
			logger.error("Error sending log", e);
		} catch (LoggerException e) {
			logger.error("Error sending log", e);
		} catch (RemoteException e) {
			logger.error("Error sending log", e);
		}
	}

	/**
	 * Get configuration file
	 * 
	 * @param relativePath
	 *            the file path relative to the config folder (e.g.
	 *            "roda-wui.properties)"
	 * @return properties file input stream
	 */
	public static InputStream getConfigurationFile(String relativePath) {
		InputStream ret;
		String roda_home;
		if (System.getProperty("roda.home") != null) {
			roda_home = System.getProperty("roda.home");
		} else if (System.getenv("RODA_HOME") != null) {
			roda_home = System.getenv("RODA_HOME");
		} else {
			roda_home = null;
		}
		
		File staticConfig = new File(roda_home, "config"
				+ File.separator + relativePath);

		if (staticConfig.exists()) {
			try {
				ret = new FileInputStream(staticConfig);
				logger.info("Using static configuration");
			} catch (FileNotFoundException e) {
				logger.info("Using internal configuration");
				ret = RodaClientFactory.class.getResourceAsStream("/config/"
						+ relativePath);
			}
		} else {
			logger.info("Using internal configuration");
			ret = RodaClientFactory.class.getResourceAsStream("/config/"
					+ relativePath);
		}
		return ret;
	}

	/**
	 * Get the Ingest Submit service URL. This URL can be used to submit SIPs.
	 * 
	 * @return the Ingest Submit service URL
	 * @throws IOException
	 */
	public static URL getIngestSubmitUrl() throws IOException {
		return new URL(getRodaCoreUrl() + "/SIPUpload");
	}

}
