package pt.gov.dgarq.roda.migrator.services;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.v2.User;
import pt.gov.dgarq.roda.servlet.RodaServletRequestWrapper;

/**
 * This is a base class for all RODA Converter Web Services. It provides
 * functionality to read the RODA Migrator properties file.
 * 
 * @author Rui Castro
 */
public abstract class ConverterWebService {

	private static final Logger logger = Logger
			.getLogger(ConverterWebService.class);

	public static File RODA_HOME;
	public static File RODA_CONFIG_DIRECTORY;

	private static Configuration configuration = null;

	private static File cacheDirectory = null;

	/**
	 * Initialise a {@link ConverterWebService}.
	 * 
	 * @throws RODAServiceException
	 */
	public ConverterWebService() throws RODAServiceException {

		if (System.getProperty("roda.home") != null) {
			RODA_HOME = new File(System.getProperty("roda.home"));
		} else if (System.getenv("RODA_HOME") != null) {
			RODA_HOME = new File(System.getenv("RODA_HOME")); //$NON-NLS-1$
		} else {
			RODA_HOME = new File(".");
			logger.info("RODA_HOME not defined. Using default " + RODA_HOME);
		}

		RODA_CONFIG_DIRECTORY = new File(RODA_HOME, "config"); //$NON-NLS-1$

		try {

			if (configuration == null) {
				configuration = getConfiguration("roda-migrator.properties");
			}

		} catch (ConfigurationException e) {

			logger.error(
					"Error reading roda-migrator.properties - "
							+ e.getMessage(), e);
			throw new RODAServiceException(
					"Error reading roda-migrator.properties - "
							+ e.getMessage(), e);

		}

		cacheDirectory = new File(configuration.getString("cacheDirectory"));

	}

	/**
	 * Return the configuration properties stored in the RODA Core configuration
	 * file (roda-migrator.properties).
	 * 
	 * @return a {@link Configuration} with the properties for RODA Services.
	 */
	protected Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * Return the configuration properties with the specified name.
	 * 
	 * @param configurationFile
	 *            the name of the configuration file.
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

		File externalConfigurationFile = new File(RODA_CONFIG_DIRECTORY,
				configurationFile);

		if (externalConfigurationFile.isFile()) {
			propertiesConfiguration.load(externalConfigurationFile);
			logger.debug("Loading configuration " + externalConfigurationFile);
		} else {
			propertiesConfiguration.load(getClass().getResource(
					"/" + configurationFile));
			logger.debug("Loading default configuration " + configurationFile);
		}

		return propertiesConfiguration;
	}

	/**
	 * Gets the {@link User} that requested this service.
	 * 
	 * @return the {@link User} that requested this service or <code>null</code>
	 *         if it doesn't exist.
	 */
	protected User getClientUser() {

		User user = null;

		MessageContext context = MessageContext.getCurrentContext();

		if (context != null) {

			HttpServletRequest httpRequest = (HttpServletRequest) context
					.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
			httpRequest.getRemoteAddr();

			if (httpRequest instanceof RodaServletRequestWrapper) {
				RodaServletRequestWrapper rodaRequestWrapper = (RodaServletRequestWrapper) httpRequest;
				return rodaRequestWrapper.getCASUserPrincipal();
			} else {
				// user = null
				return new User(context.getUsername());
			}

		} else {
			// user = null
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

			logger.debug("HTTP Request remote address is " + address);

		} else {

			address = null;

			logger.debug("MessageContext is null => remote address is null");
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

	protected HttpServletRequest getRequest() {

		MessageContext messageContext = MessageContext.getCurrentContext();

		if (messageContext != null) {

			return (HttpServletRequest) messageContext
					.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);

		} else {
			logger.debug("MessageContext is null => remote address is null");
			return null;
		}

	}

	protected ServletContext getContext() {

		MessageContext messageContext = MessageContext.getCurrentContext();

		if (messageContext != null) {
			HttpServlet srv = (HttpServlet) MessageContext.getCurrentContext()
					.getProperty(HTTPConstants.MC_HTTP_SERVLET);
			ServletContext context = srv.getServletContext();

			return context;

		} else {
			logger.debug("MessageContext is null => remote address is null");
			return null;
		}

	}

	/**
	 * @return the cacheDirectory
	 */
	protected File getCacheDirectory() {
		return cacheDirectory;
	}

	protected String getCacheURL(File directory, String id)
			throws MalformedURLException {

		HttpServletRequest request = getRequest();

		String urlFile = String.format("%1$s/cache/%2$s/%3$s",
				request.getContextPath(), directory.getName(), id);

		return new URL(request.getScheme(), request.getServerName(),
				request.getServerPort(), urlFile).toString();
	}
}
