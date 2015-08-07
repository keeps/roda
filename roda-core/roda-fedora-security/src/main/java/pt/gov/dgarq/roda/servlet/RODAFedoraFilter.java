package pt.gov.dgarq.roda.servlet;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;
import fedora.server.security.servletfilters.ExtendedHttpServletRequest;
import fedora.server.security.servletfilters.FilterSetup;

/**
 * @author Rui Castro
 * 
 */
public class RODAFedoraFilter extends FilterSetup {

	static final private Logger logger = Logger
			.getLogger(RODAFedoraFilter.class);

	/**
	 * The login user's name
	 */
	static final protected String USERNAME = "userName"; //$NON-NLS-1$

	/**
	 * The name of the role attribute to be used for Fedora 2.2 default XACML
	 * module.
	 */
	final static protected String FEDORA_ROLE = "fedoraRole"; //$NON-NLS-1$

	/**
	 * The name of Fedora attributes map in http request. This is defined in
	 * fedora.server.Context.
	 */
	static final protected String FEDORA_AUX_SUBJECT_ATTRIBUTES = "FEDORA_AUX_SUBJECT_ATTRIBUTES"; //$NON-NLS-1$

	/**
	 * LDAP filter configuration file
	 */
	protected String configFile = "cas-filter.properties"; //$NON-NLS-1$
	
	/**
	 * CAS Core Callback URL URL
	 */
	protected String coreURL = null;
	
	/**
	 * CAS URL
	 */
	protected String casURL = null;

	/**
	 * CAS Utility 
	 */
	protected CASUtility casUtility = null;

	/**
	 * 
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig filterConfig) {
		Configuration configuration = null;
		try {

			configuration = getConfiguration(configFile);

		} catch (ConfigurationException e) {
			logger.error("Error reading configuration file " + configFile
					+ " - " + e.getMessage());
		}

		if (configuration != null) {
			coreURL = configuration.getString("roda.core.url");
			casURL = configuration.getString("roda.cas.url");
		} else {
			if (filterConfig == null) {
				logger.info("init() filter configuration is null. Using defaults... "+ String.format("(%1$s ; %2$d ; %3$s)",coreURL, casURL));
			} else {
				if (filterConfig.getInitParameter("roda.cas.url") != null) {
					casURL = filterConfig.getInitParameter("roda.cas.url");
				} else {
					// Default value
				}
				if (filterConfig.getInitParameter("roda.core.url") != null) {
					coreURL = filterConfig.getInitParameter("roda.core.url");
				} else {
					// Default value
				}

			}

		}
		
		try {
			this.casUtility = new CASUtility(new URL(casURL),new URL(coreURL));
		}catch(MalformedURLException mfue){
			logger.error("Error initializing CASUtility:"+mfue.getMessage(),mfue);
		}
	}

	/**
	 * @see FilterSetup#doThisSubclass(ExtendedHttpServletRequest,
	 *      HttpServletResponse)
	 */
	@Override
	public boolean doThisSubclass(
			ExtendedHttpServletRequest extendedHttpRequest,
			HttpServletResponse httpResponse) throws Throwable {

		try {

			logger.debug("doThisSubclass() ...");

			String authHeader = extendedHttpRequest.getHeader("Authorization");

			if (authHeader == null) {

				logger.debug("Authorization header is null. Ignoring ...");

			} else {

				// if (extendedHttpRequest.getAttribute(USERNAME) == null) {

				logger.debug("Authorization header exists. Parsing username and password ...");

				String userNamePass[] = parseUserNamePass(authHeader);
				if (userNamePass == null) {

					logger.debug("Username and password are null. Ignoring...");

				} else {

					// userNamePass is set
					String username = userNamePass[0];
					String password = userNamePass[1];

					if (username == null || username.trim().length() == 0) {

						// username is null or empty
						logger.debug("username is null or empty. Ignoring ...");

					} else {

						// username and password are not empty
						// Lets autenticate user...
						logger.debug("Setting attributes for user " + username);

						setUserAttributes(username, password,
								extendedHttpRequest);
					}

				}

			}

			return false; // i.e., don't signal to terminate servlet filter
			// chain

		} catch (Throwable t) {
			logger.warn("Exception processing filter - " + t.getMessage(), t);
			throw t;
		}
	}

	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {

		//this.authenticationCache.stopCleanerTimer();

		logger.info("destroy()");
	}

	private void setUserAttributes(String username, String password,
			ExtendedHttpServletRequest requestWrapper) {
		logger.debug("SETUSERATTRIBUTE "+username+" / "+password);
		CASUserPrincipal user = null;
		try{
			// FIXME empty string
			user = this.casUtility.getCASUserPrincipal(username, password,"");
			if (user == null) {
				logger.debug("No user named " + username);
			} else {
				Map<String, String[]> fedoraAttributeMap = user.getFedoraAttributeMap();
				requestWrapper.setAuthenticated((UserPrincipal)user, null);
				requestWrapper.setAttribute(FEDORA_AUX_SUBJECT_ATTRIBUTES,fedoraAttributeMap);
			}
		}catch(Exception e){
			logger.error("Error setting authenticated used:"+e.getMessage());
		}
	}

	private String[] parseUserNamePass(String header) {
		String[] usernamePassword = null;

		if ((header == null) || "".equals(header)) {
			logger.debug("authorization header is null");
			return null;
		}

		String authschemeUsernamepassword[] = header.split("\\s+");
		if (authschemeUsernamepassword.length < 2) {
			logger.error("header spliting is wrong");
			return null;
		}

		String authscheme = authschemeUsernamepassword[0];
		if ((authscheme == null) && !"Basic".equalsIgnoreCase(authscheme)) {
			logger.error("it is not the basic authentication");
			return null;
		}

		String usernamepassword = authschemeUsernamepassword[1];
		if ((usernamepassword == null) || "".equals(usernamepassword)) {
			logger.error("usernamepassword is null");
			return null;
		}

		byte[] encoded = usernamepassword.getBytes();
		if (!Base64.isArrayByteBase64(encoded)) {
			logger.error("the byte array is not base64 encoded");
			return null;
		}

		byte[] decodedAsByteArray = Base64.decodeBase64(encoded);
		String decoded = new String(decodedAsByteArray); // decodedAsByteArray.toString();
		String DELIMITER = ":";

		if (decoded.indexOf(DELIMITER) < 0) {
			logger.error("decoded user/password lacks delimiter");
			return null;
		} else if (decoded.startsWith(DELIMITER)) {
			logger.error("decoded user/password is lacks user . . . returning 0-length strings");
			return null;
		} else if (decoded.endsWith(DELIMITER)) { // no password, e.g., user
			return null;
		} else { // usual, expected case
			usernamePassword = decoded.split(DELIMITER);
		}

		return usernamePassword;
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
	private Configuration getConfiguration(String configurationFile)
			throws ConfigurationException {

		File RODA_HOME = null;
		if (System.getProperty("roda.home") != null) {
			RODA_HOME = new File(System.getProperty("roda.home"));
			logger.info("RODA_HOME defined as " + RODA_HOME);
		} else if (System.getenv("RODA_HOME") != null) {
			RODA_HOME = new File(System.getenv("RODA_HOME")); //$NON-NLS-1$
			logger.info("RODA_HOME defined as " + RODA_HOME);
		} else {
			RODA_HOME = new File("/usr/local/roda"); //$NON-NLS-1$
			logger.info("RODA_HOME not defined. Using default " + RODA_HOME);
		}

		File RODA_CORE_CONFIG_DIRECTORY = new File(RODA_HOME, "config"); //$NON-NLS-1$

		PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
		propertiesConfiguration.setDelimiterParsingDisabled(true);

		File externalConfigurationFile = new File(RODA_CORE_CONFIG_DIRECTORY,
				configurationFile);

		if (externalConfigurationFile.isFile()) {
			propertiesConfiguration.load(externalConfigurationFile);
			logger.debug("Loading configuration " + externalConfigurationFile);
		} else {
			propertiesConfiguration = null;
			logger.debug("Configuration " + configurationFile
					+ " doesn't exist");
		}

		return propertiesConfiguration;
	}

}
