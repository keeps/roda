package pt.gov.dgarq.roda.servlet.cas;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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

import pt.gov.dgarq.roda.core.data.v2.User;
import pt.gov.dgarq.roda.servlet.RODAFilter;
import pt.gov.dgarq.roda.servlet.RodaServletRequestWrapper;
import pt.gov.dgarq.roda.servlet.UserPrincipal;
import pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

/**
 * @author Rui Castro
 */
public class CASAuthenticationFilter extends RODAFilter {

	static final private Logger logger = Logger
			.getLogger(CASAuthenticationFilter.class);

	/**
	 * The login user's name
	 */
	public static final String USERNAME = "userName";

	/**
	 * The name of the role attribute to be used for Fedora 2.2.1 default XACML
	 * module.
	 */
	public static final String FEDORA_ROLE = "fedoraRole";

	/**
	 * LDAP filter configuration file
	 */
	protected String configFile = "cas-filter.properties";
	
	/**
	 * CAS Service URL
	 */
	protected String casURL = null;
	
	/**
	 * Core Callback URL
	 */
	protected String coreURL = null;
	
	

	
	/**
	 * CAS utility class
	 */
	protected CASUtility casUtility = null;

	//private AuthenticationCache authenticationCache = null;

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
			casURL = configuration.getString("roda.cas.url");
			coreURL = configuration.getString("roda.core.url");
		} else {

			if (filterConfig != null) {
				if (filterConfig.getInitParameter("roda.cas.url") != null) {
					casURL = filterConfig.getInitParameter("casURL");
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

		//this.authenticationCache = new AuthenticationCache();

		logger.info(getClass().getSimpleName() + " initialized ok");
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest servletRequest,
			ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {
		
		if (servletRequest instanceof RodaServletRequestWrapper) {
			RodaServletRequestWrapper request = (RodaServletRequestWrapper) servletRequest;
		
			UserPrincipal casUserPrincipal = request.getCASUserPrincipal();
			if (casUserPrincipal == null) {
				logger.debug("request.getCASUserPrincipal() returned null");
			} else {

				logger.debug("Request principal is "
						+ casUserPrincipal.getName() + " with roles "
						+(new ArrayList<String>(casUserPrincipal.getAllRoles())));

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
					String userNamePass[] = parseUsernamePassword(authHeader);
					if (userNamePass == null) {
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

							try {

								User user = null;

								CASUserPrincipal userWithPGT = this.casUtility.getCASUserPrincipal(username, password, request.getRemoteAddr());
								if(userWithPGT!=null){
									user = (User)userWithPGT;
									request.setCASUserPrincipal(userWithPGT);
								}
									
								if(user==null){
									throw(new AuthenticationException());
								}
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
					UserPrincipal casUserPrincipal = request.getCASUserPrincipal();
					if (casUserPrincipal == null) {
						logger.debug("request.getCASUserPrincipal() returned null");
					} else {

						logger.debug("Request principal is "
								+ casUserPrincipal.getName() + " with roles "
								+ Arrays.asList(casUserPrincipal.getAllRoles()));

					}
					chain.doFilter(request, httpResponse);
				}

			}
		}

	}

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {

		//this.authenticationCache.stopCleanerTimer();

		logger.info("destroy()");
	}

	private static String[] parseUsernamePassword(String authorizationHeader) {
		String[] usernamePassword = null;

		if (StringUtils.isBlank(authorizationHeader)) {
			logger.error("authorization header is blank");
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
			logger
			.error("End with delimiter");
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
