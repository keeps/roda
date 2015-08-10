package pt.gov.dgarq.roda.wui.filter;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jasig.cas.client.authentication.DefaultGatewayResolverImpl;
import org.jasig.cas.client.authentication.GatewayResolver;
import org.jasig.cas.client.util.CommonUtils;

import pt.gov.dgarq.roda.common.RodaClientFactory;
import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.servlet.RODAFilter;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;
import pt.gov.dgarq.roda.wui.common.server.UserLoginServiceImpl;

/**
 * Servlet Filter implementation class CASUpgradedFilter
 */
public class CASUpgradedFilter extends RODAFilter {
	static final private Logger logger = Logger.getLogger(CASUpgradedFilter.class);
	private GatewayResolver gatewayStorage = new DefaultGatewayResolverImpl();
	protected String casLoginURL = null;

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
	
	protected String guestUsername = null;
	
	protected String guestPassword = null;

	/**
	 * Default constructor.
	 */
	public CASUpgradedFilter() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see Filter#destroy()
	 */
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		boolean doFilter = true;
		if (request.getParameter("ticket") != null) {	//ticket in request... login via CAS...
			HttpServletRequest servletRequest = (HttpServletRequest) request;
			HttpServletResponse servletResponse = (HttpServletResponse) response;
			try {
				String ticket = request.getParameter("ticket");
				String cleanURL = getCleanURL(servletRequest);
				UserLoginServiceImpl.getInstance().loginCAS(servletRequest.getSession(true), cleanURL.toString(),
						ticket);
				doFilter = false;
				logger.info("Redirecting to " + cleanURL.toString());
				servletResponse.sendRedirect(cleanURL.toString());
			} catch (RODAException re) {
				logger.error("Error in filter: " + re.getMessage(), re);
			}
		} else {
			HttpServletRequest servletRequest = (HttpServletRequest) request;
			HttpServletResponse servletResponse = (HttpServletResponse) response;
			if (servletRequest.getRequestURI().equalsIgnoreCase("login")) {
				logger.info("LOGIN request catched...");
				String cleanURL = getCleanURL(servletRequest);
				String urlToRedirectTo = CommonUtils.constructRedirectUrl(casLoginURL, "service", cleanURL, false,
						true);
				if (!this.gatewayStorage.hasGatewayedAlready(servletRequest, urlToRedirectTo)) {
					urlToRedirectTo = this.gatewayStorage.storeGatewayInformation(servletRequest, urlToRedirectTo);
					doFilter = false;
					servletResponse.sendRedirect(urlToRedirectTo);
				}
			} else if (servletRequest.getRequestURI().equalsIgnoreCase("logout")) {
				logger.info("LOGOUT request catched...");
			} else {
				try{
					RODAClient client = RodaClientFactory.getRodaClient(servletRequest.getSession(true));
					if(client==null){
						logger.error("RodaClient NULL...");
						doFilter=true;
					}else{
						logger.info("RodaClient not null... Guest?: "+client.isGuestLogin());
						doFilter=true;
					}
				}catch(Throwable t){
					logger.error(t.getMessage(),t);
				}
			}
			if(doFilter){
				logger.debug("DoFilter...");
				chain.doFilter(servletRequest, servletResponse);
			}
		}
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		super.init(filterConfig);

		Configuration configuration = null;
		try {

			configuration = getConfiguration(configFile);

		} catch (ConfigurationException e) {
			logger.error("Error reading configuration file " + configFile + " - " + e.getMessage());
		}

		if (configuration != null) {
			casURL = configuration.getString("roda.cas.url");
			coreURL = configuration.getString("roda.core.url");
			casLoginURL = configuration.getString("roda.cas.url") + "/login";
			guestUsername = configuration.getString("roda.cas.guest.username");
			guestPassword = configuration.getString("roda.cas.guest.password");
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
			this.casUtility = new CASUtility(new URL(casURL), new URL(coreURL));
		} catch (MalformedURLException mfue) {
			logger.error("Error initializing CASUtility:" + mfue.getMessage(), mfue);
		}

		// this.authenticationCache = new AuthenticationCache();

		logger.info(getClass().getSimpleName() + " initialized ok");
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
	private Configuration getConfiguration(String configurationFile) throws ConfigurationException {

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

		File externalConfigurationFile = new File(RODA_CONFIG_DIRECTORY, configurationFile);

		if (externalConfigurationFile.isFile()) {
			propertiesConfiguration.load(externalConfigurationFile);
			logger.debug("Loading configuration " + externalConfigurationFile);
		} else {
			propertiesConfiguration = null;
			logger.debug("Configuration " + configurationFile + " doesn't exist");
		}

		return propertiesConfiguration;
	}

	private String getCleanURL(HttpServletRequest servletRequest) {
		String url = servletRequest.getRequestURL().toString();
		String cleanQueryString = removeParameterFromQueryString(servletRequest.getQueryString(), "ticket");
		StringBuilder cleanURL = new StringBuilder();
		logger.debug("Clean query string: " + cleanQueryString);
		if (!StringUtils.isBlank(cleanQueryString)) {
			cleanURL.append(url).append('?').append(cleanQueryString);
		} else {
			cleanURL.append(url);
		}
		return cleanURL.toString();
	}

	public static String removeParameterFromQueryString(String queryString, String paramToRemove) {
		if (queryString != null) {
			String oneParam = "^" + paramToRemove + "(=[^&]*)$";
			String begin = "^" + paramToRemove + "(=[^&]*)(&?)";
			String end = "&" + paramToRemove + "(=[^&]*)$";
			String middle = "(?<=[&])" + paramToRemove + "(=[^&]*)&";
			String removedMiddleParams = queryString.replaceAll(middle, "");
			String removedBeginParams = removedMiddleParams.replaceAll(begin, "");
			String removedEndParams = removedBeginParams.replaceAll(end, "");
			String cleanURL = removedEndParams.replaceAll(oneParam, "");

			return cleanURL;
		} else {
			return null;
		}
	}
}
