package pt.gov.dgarq.roda.servlet;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.naming.AuthenticationException;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.User;
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
	protected String configFile = "ldap-filter.properties"; //$NON-NLS-1$

	/**
	 * LDAP server host
	 */
	protected String host = null;

	/**
	 * LDAP server port
	 */
	protected int port = 389;

	/**
	 * LDAP DN of the people entry
	 */
	protected String peopleDN = null;

	/**
	 * LDAP DN of the groups entry
	 */
	protected String groupsDN = null;

	/**
	 * LDAP DN of the roles entry
	 */
	protected String rolesDN = null;

	/**
	 * List of protected users. Users in the protected list cannot be modified.
	 */
	protected List<String> protectedUsers = new ArrayList<String>();

	/**
	 * List of protected groups. Groups in the protected list cannot be
	 * modified.
	 */
	protected List<String> protectedGroups = new ArrayList<String>();

	/**
	 * LDAP utility object
	 */
	protected LdapUtility ldapUtility = null;

	private AuthenticationCache authenticationCache = null;

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

			host = configuration.getString("host");
			port = configuration.getInt("port");

			peopleDN = configuration.getString("peopleDN");
			groupsDN = configuration.getString("groupsDN");
			rolesDN = configuration.getString("rolesDN");

			protectedUsers = Arrays.asList(configuration
					.getStringArray("protectedUsers"));
			protectedGroups = Arrays.asList(configuration
					.getStringArray("protectedGroups"));

		} else {

			if (filterConfig == null) {

				logger.info("init() filter configuration is null. Using defaults... "
						+ String.format("(%1$s ; %2$d ; %3$s ; %4$s ; %5$s)",
								host, port, peopleDN, groupsDN, rolesDN));

			} else {

				if (filterConfig.getInitParameter("host") != null) {
					host = filterConfig.getInitParameter("host");
				} else {
					// Default value (localhost)
					host = "localhost";
				}

				try {
					port = Integer.parseInt(filterConfig
							.getInitParameter("port"));
				} catch (NumberFormatException e) {
					// Default value (389)
					port = 389;
				}

				if (filterConfig.getInitParameter("peopleDN") != null) {
					peopleDN = filterConfig.getInitParameter("peopleDN");
				} else {
					// Default value ("")
				}

				if (filterConfig.getInitParameter("groupsDN") != null) {
					groupsDN = filterConfig.getInitParameter("groupsDN");
				} else {
					// Default value ("")
				}

				if (filterConfig.getInitParameter("rolesDN") != null) {
					rolesDN = filterConfig.getInitParameter("rolesDN");
				} else {
					// Default value ("")
				}

				if (filterConfig.getInitParameter("protectedUsers") != null) {

					String protectedUsersValue = filterConfig
							.getInitParameter("protectedUsers");

					logger.debug("protectedUsersValue: " + protectedUsersValue);

					String[] values = protectedUsersValue.split(",\\e*");
					protectedUsers = Arrays.asList(values);

					logger.debug("protectedUsers: " + protectedUsers);

				} else {
					// Default value (null)
				}

				if (filterConfig.getInitParameter("protectedGroups") != null) {

					String protectedGroupsValue = filterConfig
							.getInitParameter("protectedGroups");

					logger.debug("protectedGroupsValue: "
							+ protectedGroupsValue);

					String[] values = protectedGroupsValue.split(",\\e*");
					protectedGroups = Arrays.asList(values);

					logger.debug("protectedGroups: " + protectedGroups);

				} else {
					// Default value (null)
				}

			}

		}

		this.ldapUtility = new LdapUtility(host, port, peopleDN, groupsDN,
				rolesDN, protectedUsers, protectedGroups);

		this.authenticationCache = new AuthenticationCache();

		logger.info(String.format("(%1$s ; %2$d ; %3$s ; %4$s ; %5$s)", host,
				port, peopleDN, groupsDN, rolesDN));

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

		this.authenticationCache.stopCleanerTimer();

		logger.info("destroy()");
	}

	private void setUserAttributes(String username, String password,
			ExtendedHttpServletRequest requestWrapper) {
		try {

			// User user = this.ldapUtility.getUser(username);

			User user = null;

			if (authenticationCache.isAuthenticated(username, password)) {

				logger.debug("User is authenticated in cache. Getting cached user...");

				user = authenticationCache.getUser(username, password);
			}

			if (user == null) {

				logger.debug("User information not available in cache. Getting user from LDAP");

				user = this.ldapUtility
						.getAuthenticatedUser(username, password);

				logger.debug("Storing user in authentication cache...");

				authenticationCache.addUser(username, password, user);
			}

			if (user == null) {

				logger.debug("No user named " + username);

			} else {

				UserPrincipal ldapUserPrincipal = new UserPrincipal(user);

				logger.debug("User " + username + " is " + ldapUserPrincipal);

				Map<String, String[]> fedoraAttributeMap = ldapUserPrincipal
						.getFedoraAttributeMap();

				requestWrapper.setAttribute(FEDORA_AUX_SUBJECT_ATTRIBUTES,
						fedoraAttributeMap);
			}

		} catch (AuthenticationException e) {
			logger.warn("Exception getting authenticated information from LDAP for user "
					+ username
					+ " - "
					+ e.getMessage()
					+ ". User attributes not set!");
			logger.debug(
					"Exception getting authenticated information from LDAP for user "
							+ username + " - " + e.getMessage()
							+ ". User attributes not set!", e);
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
