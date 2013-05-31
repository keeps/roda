package pt.gov.dgarq.roda.servlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.naming.AuthenticationException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.User;

/**
 * @author Rui Castro
 */
public class LdapAuthenticationFilter extends RODAFilter {

	static final private Logger logger = Logger
			.getLogger(LdapAuthenticationFilter.class);

	/**
	 * The login user's name
	 */
	static final protected String USERNAME = "userName";

	/**
	 * The name of the role attribute to be used for Fedora 2.2.1 default XACML
	 * module.
	 */
	static final protected String FEDORA_ROLE = "fedoraRole";

	/**
	 * LDAP filter configuration file
	 */
	protected String configFile = "ldap-filter.properties";

	/**
	 * LDAP server host
	 */
	protected String host = "localhost";

	/**
	 * LDAP server port
	 */
	protected int port = 389;

	/**
	 * LDAP administrator Distinguished Name (DN)
	 */
	protected String ldapAdminDN = null;

	/**
	 * LDAP administrator password
	 */
	protected String ldapAdminPassword = null;

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
	 * LDAP utility class
	 */
	protected LdapUtility ldapUtility = null;

	private AuthenticationCache authenticationCache = null;

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException {

		super.init(filterConfig);

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

				logger
						.debug("init() filter configuration is null. Using defaults... "
								+ String
										.format(
												"(%1$s ; %2$d ; %3$s ; %4$s ; %5$s ; %6$s)",
												host, port, peopleDN, groupsDN,
												rolesDN, realm));

			} else {

				if (filterConfig.getInitParameter("host") != null) {
					host = filterConfig.getInitParameter("host");
				} else {
					// Default value (localhost)
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

		logger.info(getClass().getSimpleName() + " initialized ok");
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest servletRequest,
			ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {

		logger.trace("doFilter() ...");

		if (servletRequest instanceof RodaServletRequestWrapper) {

			RodaServletRequestWrapper request = (RodaServletRequestWrapper) servletRequest;

			// USERNAME is already set. Let the request continue ...
			logger.info("Authentication is valid for user "
					+ request.getAttribute(USERNAME));

			UserPrincipal ldapUserPrincipal = request.getLdapUserPrincipal();

			if (ldapUserPrincipal == null) {
				logger.debug("request.getLdapUserPrincipal() returned null");
			} else {

				logger.trace("Request principal is "
						+ ldapUserPrincipal.getName() + " with roles "
						+ Arrays.asList(ldapUserPrincipal.getRoles()));

			}

			// continue filter chain
			chain.doFilter(request, servletResponse);

		} else {

			RodaServletRequestWrapper request = new RodaServletRequestWrapper(
					(HttpServletRequest) servletRequest);
			HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

			String authHeader = request.getHeader("Authorization");

			if (authHeader == null) {

				logger
						.debug("Authorization header is null. Enforce authentication ...");
				this.enforceAuthentication(httpResponse);

			} else {

				// logger.trace("Authorization header is " + authHeader);

				if (request.getAttribute(USERNAME) == null) {

					logger.trace("Verify username and password ...");

					String userNamePass[] = parseUsernamePassword(authHeader);
					if (userNamePass == null) {

						logger
								.debug("Username is null. Enforce authentication ...");
						this.enforceAuthentication(httpResponse);

					} else {

						// userNamePass is set
						String username = userNamePass[0];
						String password = userNamePass[1];

						if (username == null || username.trim().length() == 0
								|| password == null
								|| password.trim().length() == 0) {

							// username or password is null or empty
							logger
									.debug("username or password is null or empty. Enforce authentication ...");
							this.enforceAuthentication(httpResponse);

						} else {

							// username and password are not empty
							// Let's autenticate user...
							logger
									.trace("Check the user against the LDAP server");

							try {

								User user = null;

								if (authenticationCache.isAuthenticated(
										username, password)) {

									logger
											.debug("User is authenticated in cache. Getting cached user...");

									user = authenticationCache.getUser(
											username, password);
								}

								if (user == null) {

									logger
											.debug("User information not available in cache. Getting user from LDAP");

									user = this.ldapUtility
											.getAuthenticatedUser(username,
													password);

									logger
											.debug("Storing user in authentication cache...");

									authenticationCache.addUser(username,
											password, user);
								}

								request
										.setLdapUserPrincipal(new ExtendedUserPrincipal(
												user, password));

								// if everything went ok with authentication,
								// let
								// the request continue ...
								logger
										.trace("Authentication is valid for user "
												+ request
														.getAttribute(USERNAME));

								logger.trace("Request principal is "
										+ request.getLdapUserPrincipal());

								// continue filter chain
								chain.doFilter(request, httpResponse);

							} catch (AuthenticationException e) {
								logger
										.error(
												"Error autenticating user. Enforce authentication ...",
												e);
								this.enforceAuthentication(httpResponse);
							}
						}

					}

				} else {
					// USERNAME is already set. Let the request continue ...
					logger.info("Authentication is valid for user "
							+ request.getAttribute(USERNAME));

					UserPrincipal ldapUserPrincipal = request
							.getLdapUserPrincipal();

					if (ldapUserPrincipal == null) {
						logger
								.debug("request.getLdapUserPrincipal() returned null");
					} else {

						logger.trace("Request principal is "
								+ ldapUserPrincipal.getName() + " with roles "
								+ Arrays.asList(ldapUserPrincipal.getRoles()));

					}

					// continue filter chain
					chain.doFilter(request, httpResponse);
				}

			}
		}

	}

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {

		this.authenticationCache.stopCleanerTimer();

		logger.info("destroy()");
	}

	private static String[] parseUsernamePassword(String authorizationHeader) {
		String[] usernamePassword = null;

		if (StringUtils.isBlank(authorizationHeader)) {
			logger.debug("authorization header is blank");
			return null;
		}

		String authschemeUsernamepassword[] = authorizationHeader.split("\\s+");
		if (authschemeUsernamepassword.length < 2) {
			logger.error("header spliting is wrong");
			return null;
		}

		String authscheme = authschemeUsernamepassword[0];
		if ((authscheme == null)
				&& !HttpServletRequest.BASIC_AUTH.equalsIgnoreCase(authscheme)) {
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
			logger
					.error("decoded user/password is lacks user . . . returning 0-length strings");
			return null;
		} else if (decoded.endsWith(DELIMITER)) { // no password, e.g., user
			return null;
		} else { // usual, expected case
			usernamePassword = decoded.split(DELIMITER);
		}

		return usernamePassword;
	}

	/**
	 * @param httpRequest
	 * @return an array of size 2. The username is the first element and the
	 *         password is the second.
	 */
	public static String[] parseUsernamePassword(HttpServletRequest httpRequest) {

		String[] usernamePassword = new String[2];

		String authorizationHeader = httpRequest.getHeader("Authorization");
		if (authorizationHeader != null) {
			usernamePassword = parseUsernamePassword(authorizationHeader);
		} else {
			// no username and password
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
			RODA_HOME = new File(System.getProperty("roda.home"));//$NON-NLS-1$
			logger.info("RODA_HOME defined as " + RODA_HOME);
		} else if (System.getenv("RODA_HOME") != null) {
			RODA_HOME = new File(System.getenv("RODA_HOME")); //$NON-NLS-1$
			logger.info("RODA_HOME defined as " + RODA_HOME);
		} else {
			RODA_HOME = new File("."); //$NON-NLS-1$
			logger.info("RODA_HOME not defined. Using current directory '" + RODA_HOME + "'");
		}
		
		File RODA_CONFIG_DIRECTORY = new File(RODA_HOME, "config"); //$NON-NLS-1$

		PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
		propertiesConfiguration.setDelimiterParsingDisabled(true);

		File externalConfigurationFile = new File(RODA_CONFIG_DIRECTORY,
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
