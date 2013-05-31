package pt.gov.dgarq.roda.core.services;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.RodaWebApplication;
import pt.gov.dgarq.roda.core.common.LoggerException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.LogEntry;
import pt.gov.dgarq.roda.core.data.LogEntryParameter;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.logger.LoggerManager;
import pt.gov.dgarq.roda.servlet.ExtendedUserPrincipal;
import pt.gov.dgarq.roda.servlet.LdapAuthenticationFilter;
import pt.gov.dgarq.roda.servlet.RodaServletRequestWrapper;

/**
 * This {@link Servlet} should be the base class for all RODA servlets.
 * 
 * @author Rui Castro
 */
public abstract class RODAServlet extends HttpServlet implements Servlet {
	private static final long serialVersionUID = -4545517991177824948L;

	private static final Logger logger = Logger.getLogger(RODAServlet.class);

	private static LoggerManager loggerManager = null;

	private static Configuration configuration = null;

	private Queue<LogEntry> logEntryQueue = null;
	private RegisterLogEntriesThread registerLogEntriesThread = null;

	/**
	 * Initialise a {@link RODAServlet}.
	 * 
	 * 
	 * @throws RODAServiceException
	 */
	public RODAServlet() throws RODAServiceException {

		try {

			if (configuration == null) {
				RODAServlet.configuration = getConfiguration("roda-core.properties");
			}

			if (loggerManager == null) {
				loggerManager = LoggerManager.getDefaultLoggerManager();
			}

		} catch (ConfigurationException e) {

			logger.error("Error reading roda-core.properties - "
					+ e.getMessage(), e);
			throw new RODAServiceException(
					"Error reading roda-core.properties - " + e.getMessage(), e);

		} catch (LoggerException e) {

			logger.error("Error creating logger utility  - " + e.getMessage(),
					e);
			throw new RODAServiceException("Error creating logger utility  - "
					+ e.getMessage(), e);

		}

		this.logEntryQueue = new ConcurrentLinkedQueue<LogEntry>();
		this.registerLogEntriesThread = new RegisterLogEntriesThread(
				loggerManager, logEntryQueue);

		logger.info("Starting RegisterLogEntries[" + getClass().getSimpleName() //$NON-NLS-1$
				+ "] thread"); //$NON-NLS-1$
		this.registerLogEntriesThread.start();

	}

	/**
	 * Get the current {@link HttpServletRequest}.
	 * 
	 * Subclasses must implement this method to provide {@link RODAServlet} with
	 * information to implement {@link RODAServlet#getClientUser()},
	 * {@link RODAServlet#getClientAddress()} and
	 * {@link RODAServlet#getClientUserPassword()}.
	 */
	abstract protected HttpServletRequest getCurrentRequest();

	/**
	 * Return the configuration properties stored in the RODA Core configuration
	 * file (roda-core.properties).
	 * 
	 * @return a {@link Configuration} with the properties for RODA Services.
	 */
	protected Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * Return the configuration properties with the specified name.
	 * 
	 * @return a {@link Configuration} with the properties of the specified
	 *         name.
	 * 
	 * @throws ConfigurationException
	 */
	protected Configuration getConfiguration(String configurationFile)
			throws ConfigurationException {

		PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
		propertiesConfiguration.setDelimiterParsingDisabled(true);

		File externalConfigurationFile = new File(
				RodaWebApplication.RODA_CORE_CONFIG_DIRECTORY,
				configurationFile);

		if (externalConfigurationFile.isFile()) {
			propertiesConfiguration.load(externalConfigurationFile);
			logger.info("Using configuration " + externalConfigurationFile);
		} else {
			propertiesConfiguration.load(getClass().getResource(
					"/" + configurationFile));
			logger.info("Using default configuration " + configurationFile);
		}

		return propertiesConfiguration;
	}

	/**
	 * @return the rodaLogger
	 */
	protected LoggerManager getLoggerManager() {
		return loggerManager;
	}

	/**
	 * Register some <code>action</code> in the RODA Logger.
	 * 
	 * @param action
	 *            the action code (ex: Browser.getCollections)
	 * @param parameters
	 *            an array of {@link String} with the action parameters. This
	 *            array must have the arguments name and value (ex:
	 *            ["firstItemIndex", "0", "maxItems", 10]).
	 * @param description
	 *            the description of the action.
	 *            <p>
	 *            <strong>NOTE: </strong>the description field can use the
	 *            variable %username% to be replaced by the actual username of
	 *            the User calling the service.
	 *            </p>
	 */
	protected void registerAction(String action, String[] parameters,
			String description, long duration) {

		if (parameters != null && (parameters.length % 2) != 0) {
			logger
					.warn("registerAction("
							+ action
							+ ",...) failed because parameters array must have pairs of elements (even length)");
		} else {

			LogEntryParameter[] logParameters = null;

			if (parameters != null) {

				logParameters = new LogEntryParameter[parameters.length / 2];

				for (int i = 0, j = 0; i < logParameters.length; i++, j = j + 2) {

					logParameters[i] = new LogEntryParameter(parameters[j],
							parameters[j + 1]);
				}
			} else {
				// no parameters
			}

			registerAction(action, logParameters, description, duration);
		}
	}

	/**
	 * Register some <code>action</code> in the RODA Logger.
	 * 
	 * @param action
	 *            the action code (ex: Browser.getCollections)
	 * @param parameters
	 *            an array of {@link String} with the action parameters
	 * @param description
	 *            the description of the action.
	 *            <p>
	 *            <strong>NOTE: </strong>the description field can use the
	 *            variable %username% to be replaced by the actual username of
	 *            the User calling the service.
	 *            </p>
	 */
	protected void registerAction(String action,
			LogEntryParameter[] parameters, String description, long duration) {

		User user = getClientUser();
		if (user == null) {
			logger.warn("registerAction(" + action
					+ ",...) failed because there's no user in context.");
		} else {

			LogEntry logEntry = new LogEntry();
			logEntry.setAddress(getClientAddress());
			logEntry.setUsername(user.getName());
			logEntry.setAction(action);
			logEntry.setParameters(parameters);
			logEntry.setDescription(description.replaceAll("%username%", user
					.getName()));
			logEntry.setDuration(duration);

			try {

				getLoggerManager().addLogEntry(logEntry);

			} catch (LoggerException e) {
				logger.error("registerAction(" + logEntry.getAction()
						+ ",...) failed because of a LoggerException - "
						+ e.getMessage(), e);
			}

			// logEntryQueue.add(logEntry);
			//
			// synchronized (logEntryQueue) {
			// logEntryQueue.notify();
			// }

		}
	}

	/**
	 * Gets the {@link User} that requested this service.
	 * 
	 * @return the {@link User} that requested this service or <code>null</code>
	 *         if it doesn't exist.
	 */
	protected User getClientUser() {

		User user = null;

		if (getCurrentRequest() instanceof RodaServletRequestWrapper) {
			RodaServletRequestWrapper rodaRequestWrapper = (RodaServletRequestWrapper) getCurrentRequest();
			user = rodaRequestWrapper.getLdapUserPrincipal();
		} else {
			// user = null
			user = new User(getCurrentRequest().getUserPrincipal().getName());
		}

		return user;
	}

	/**
	 * Gets the IP address of the client that requested this service.
	 * 
	 * @return the the IP address of the client that requested this service or
	 *         <code>null</code> if it doesn't exist.
	 */
	protected String getClientAddress() {
		return getCurrentRequest().getRemoteAddr();
	}

	/**
	 * Gets the password used to authenticate the current request.
	 * 
	 * This method
	 * 
	 * @return the password use to authenticate this current request or
	 *         <code>null</code> if it doesn't exist.
	 */
	protected String getClientUserPassword() {

		String password = null;

		if (getCurrentRequest() instanceof RodaServletRequestWrapper) {

			RodaServletRequestWrapper rodaRequestWrapper = (RodaServletRequestWrapper) getCurrentRequest();
			ExtendedUserPrincipal userPrincipal = (ExtendedUserPrincipal) rodaRequestWrapper
					.getLdapUserPrincipal();
			password = userPrincipal.getPassword();

		} else {

			String[] usernamePassword = LdapAuthenticationFilter
					.parseUsernamePassword(getCurrentRequest());
			password = usernamePassword[1];

		}

		return password;
	}

}
