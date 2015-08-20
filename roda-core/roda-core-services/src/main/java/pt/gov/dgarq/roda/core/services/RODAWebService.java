package pt.gov.dgarq.roda.core.services;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.RodaWebApplication;
import pt.gov.dgarq.roda.core.common.LoggerException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.LogEntry;
import pt.gov.dgarq.roda.core.data.LogEntryParameter;
import pt.gov.dgarq.roda.core.data.v2.User;
import pt.gov.dgarq.roda.core.logger.LoggerManager;
import pt.gov.dgarq.roda.servlet.RodaServletRequestWrapper;
import pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

/**
 * This is a base class for all RODA Web Services. It provides functionality to
 * use the RODA Logger service and to read the RODA properties file.
 * 
 * @author Rui Castro
 */
public abstract class RODAWebService {

	private static final Logger logger = Logger.getLogger(RODAWebService.class);

	private static LoggerManager loggerManager = null;

	private static Configuration configuration = null;

	private Queue<LogEntry> logEntryQueue = null;
	private RegisterLogEntriesThread registerLogEntriesThread = null;
	private CASUtility casUtility = null;
	
	

	/**
	 * Initialise a {@link RODAWebService}.
	 * 
	 * @throws RODAServiceException
	 */
	public RODAWebService() throws RODAServiceException {

		try {

			if (configuration == null) {
				configuration = RodaWebApplication.getConfiguration(getClass(),
						"roda-core.properties");
			}

			if (loggerManager == null) {
				loggerManager = LoggerManager.getDefaultLoggerManager();
			}
			
			if(casUtility==null){
				String casURL = configuration.getString("roda.cas.url");
				String coreURL = configuration.getString("roda.core.url");				
				casUtility = new CASUtility(new URL(casURL), new URL(coreURL));
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

		} catch (MalformedURLException e) {
			
			logger.error("Error creating CAS utility  - " + e.getMessage(),
					e);
			throw new RODAServiceException("Error creating CAS utility  - "
					+ e.getMessage(), e);
			
		}

		this.logEntryQueue = new ConcurrentLinkedQueue<LogEntry>();
		this.registerLogEntriesThread = new RegisterLogEntriesThread(
				loggerManager, logEntryQueue);

		logger.info("Starting RegisterLogEntries[" + getClass().getSimpleName() //$NON-NLS-1$
				+ "] thread"); //$NON-NLS-1$
		this.registerLogEntriesThread.start();
	}

	@Override
	protected void finalize() throws Throwable {

		logger.info("Stoping RegisterLogEntries[" + getClass().getSimpleName() //$NON-NLS-1$
				+ "] thread"); //$NON-NLS-1$
		this.registerLogEntriesThread.finish();

		logger.info("Waiting for RegisterLogEntries[" //$NON-NLS-1$
				+ getClass().getSimpleName() + "] thread to die"); //$NON-NLS-1$
		this.registerLogEntriesThread.join();

		logger.info("RegisterLogEntries[" + getClass().getSimpleName() //$NON-NLS-1$
				+ "] died happily. Finalize OK"); //$NON-NLS-1$
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
	 * @param duration
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

			logger.debug("registerAction(" + action
					+ ",...) failed because there's no user in context.");

		} else {

			if (parameters == null) {
				parameters = new LogEntryParameter[0];
			}

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

//			logEntryQueue.add(logEntry);
//
//			synchronized (logEntryQueue) {
//				logEntryQueue.notify();
//			}

		}
	}

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
	 * Gets the {@link User} that requested this service.
	 * 
	 * @return the {@link User} that requested this service or <code>null</code>
	 *         if it doesn't exist.
	 */
	protected CASUserPrincipal getClientUser() {

		CASUserPrincipal user = null;
		try{
			MessageContext context = MessageContext.getCurrentContext();
	
			if (context != null) {
	
				HttpServletRequest httpRequest = (HttpServletRequest) context
						.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
				httpRequest.getRemoteAddr();
	
				if (httpRequest instanceof RodaServletRequestWrapper) {
					RodaServletRequestWrapper rodaRequestWrapper = (RodaServletRequestWrapper) httpRequest;
					user = rodaRequestWrapper.getCASUserPrincipal();
				} else {
					String username = context.getUsername();
					String password = getClientUserPassword();
					if(username!=null){
						logger.debug("GETTING CAS USER FROM U/P "+username+"/"+password);
						// FIXME empty string
						user = casUtility.getCASUserPrincipal(username, password,"");
					}
				}
	
			}
		}catch(Exception e){
			logger.error("ERROR GETTING USER:"+e.getMessage());
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

		String address = null;

		MessageContext messageContext = MessageContext.getCurrentContext();

		if (messageContext != null) {

			HttpServletRequest httpRequest = (HttpServletRequest) messageContext
					.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
			address = httpRequest.getRemoteAddr();

		} else {

			address = null;

			logger
					.debug("getClientAddress() - MessageContext is null => remote address is null");
		}

		return address;
	}

	/**
	 * Gets the password used to authenticate the current request.
	 * 
	 * @return the password use to authenticate this current request or
	 *         <code>null</code> if it doesn't exist.
	 */
	protected String getClientUserPassword() {

		String password = null;

		MessageContext context = MessageContext.getCurrentContext();

		if (context != null) {
			password = context.getPassword();
		} else {
			// password = null;
		}

		return password;
	}

	/**
	 * @return the rodaLogger
	 */
	protected LoggerManager getLoggerManager() {
		return loggerManager;
	}
	
	protected String getClientProxyGrantingTicket() {
		String pgt = null;
		MessageContext context = MessageContext.getCurrentContext();

		if (context != null) {
			pgt = context.getPassword();
		} else {
			// password = null;
		}

		return pgt;
	}

	protected CASUtility getCasUtility() {
		return casUtility;
	}

	
}
