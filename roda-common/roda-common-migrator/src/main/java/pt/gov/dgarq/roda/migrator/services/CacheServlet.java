package pt.gov.dgarq.roda.migrator.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.v2.User;
import pt.gov.dgarq.roda.servlet.cas.CASAuthenticationFilter;
import pt.gov.dgarq.roda.servlet.RodaServletRequestWrapper;
import pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal;

/**
 * This is a servlet to access files in RODA Migrator Cache.
 */
public class CacheServlet extends HttpServlet {
	private static final long serialVersionUID = 6987278817703515246L;

	private static final Logger logger = Logger.getLogger(CacheServlet.class);

	public static File RODA_HOME;
	public static File RODA_CONFIG_DIRECTORY;

	private static Configuration configuration = null;

	private HttpServletRequest currentRequest = null;

	private File cacheDirectory = null;

	/**
	 * @throws RODAServiceException
	 * 
	 * @see HttpServlet#HttpServlet()
	 */
	public CacheServlet() throws RODAServiceException {

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
				configuration = getConfiguration("roda-migrator.properties"); //$NON-NLS-1$
			}

		} catch (ConfigurationException e) {

			logger.error("Error reading roda-migrator.properties - " //$NON-NLS-1$
					+ e.getMessage(), e);
			throw new RODAServiceException(
					"Error reading roda-migrator.properties - " //$NON-NLS-1$
							+ e.getMessage(), e);

		}

		cacheDirectory = new File(configuration.getString("cacheDirectory")); //$NON-NLS-1$

		logger.debug(getClass().getSimpleName() + " initialised ok"); //$NON-NLS-1$
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		this.currentRequest = request;

		// Remove first '/' from pathInfo
		String pathInfo = request.getPathInfo().substring(1);

		try {

			String[] representationAndFile = pathInfo.split("/");

			if (representationAndFile.length == 2) {

				File file = new File(new File(this.cacheDirectory,
						representationAndFile[0]), representationAndFile[1]);

				logger.debug("HTTP GET " + pathInfo);

				// response.setContentType(FormatUtility.getMimetype(file));

				// Set the content disposition
				response.setHeader("Content-disposition",
						"filename=" + file.getName());

				response.setContentLength((int) file.length());

				FileInputStream fileInputStream = new FileInputStream(file);
				IOUtils.copyLarge(fileInputStream, response.getOutputStream());
				fileInputStream.close();

				response.flushBuffer();

				logger.debug("Served file " + file);

			} else {

				logger.debug("HTTP GET " + pathInfo + ". Sending BAD_REQUEST");

				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				response.flushBuffer();
			}

		} catch (Throwable t) {
			logger.error(t.getMessage(), t);

			logger.debug("HTTP GET " + pathInfo
					+ ". Sending INTERNAL_SERVER_ERROR");

			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					t.getMessage());
			response.flushBuffer();
		}

	}

	@Override
	protected void doDelete(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		this.currentRequest = request;

		String pathInfo = request.getPathInfo().substring(1);

		try {

			String[] representationAndFile = pathInfo.split("/");

			if (representationAndFile.length == 1) {

				File directory = new File(this.cacheDirectory,
						representationAndFile[0]);
				if (directory.exists()) {

					logger.debug("HTTP DELETE " + pathInfo);

					FileUtils.deleteDirectory(directory);

					logger.debug("Deleted cached representation " + directory);

				} else {

					logger.debug("HTTP DELETE " + pathInfo
							+ ". Sending SC_NOT_FOUND");

					response.sendError(HttpServletResponse.SC_NOT_FOUND);
					response.flushBuffer();
				}

			} else {

				logger.debug("HTTP DELETE " + pathInfo
						+ ". Sending BAD_REQUEST");

				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				response.flushBuffer();
			}

		} catch (Throwable t) {
			logger.error(t.getMessage(), t);

			logger.debug("HTTP GET " + pathInfo
					+ ". Sending INTERNAL_SERVER_ERROR");

			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					t.getMessage());
			response.flushBuffer();
		}

	}

	protected HttpServletRequest getCurrentRequest() {
		return this.currentRequest;
	}

	/**
	 * Return the configuration properties stored in the RODA Migrator
	 * configuration file (roda-migrator.properties).
	 * 
	 * @return a {@link Configuration} with the properties for RODA Migrator.
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
	protected User getClientUser() {

		User user = null;

		if (getCurrentRequest() instanceof RodaServletRequestWrapper) {
			RodaServletRequestWrapper rodaRequestWrapper = (RodaServletRequestWrapper) getCurrentRequest();
			user = rodaRequestWrapper.getCASUserPrincipal();
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
			CASUserPrincipal userPrincipal = (CASUserPrincipal) rodaRequestWrapper
					.getCASUserPrincipal();
			password = userPrincipal.getProxyGrantingTicket();

		} else {

			String[] usernamePassword = CASAuthenticationFilter
					.parseUsernamePassword(getCurrentRequest());
			password = usernamePassword[1];

		}

		return password;
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
			logger.debug("Loading configuration " + externalConfigurationFile); //$NON-NLS-1$
		} else {
			propertiesConfiguration.load(getClass().getResource(
					"/" + configurationFile)); //$NON-NLS-1$
			logger.debug("Loading default configuration " + configurationFile); //$NON-NLS-1$
		}

		return propertiesConfiguration;
	}

}