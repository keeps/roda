package pt.gov.dgarq.roda.wui.filter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.directory.SearchControls;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.jndi.CoreContextFactory;
import org.apache.log4j.Logger;
import org.jasig.cas.client.util.CommonUtils;

import pt.gov.dgarq.roda.common.RodaCoreFactory;
import pt.gov.dgarq.roda.common.UserUtility;
import pt.gov.dgarq.roda.core.data.v2.RodaSimpleUser;

/**
 * Servlet Filter implementation class RolesSetterFilter
 */
public class RolesSetterFilter implements Filter {
	static final private Logger logger = Logger.getLogger(RolesSetterFilter.class);
	FilterConfig config;

	protected String casLogoutURL = null;

	protected String configFile = "roda-wui.properties";

	public void setFilterConfig(FilterConfig config) {
		this.config = config;
	}

	public FilterConfig getFilterConfig() {
		return config;
	}

	public void init(FilterConfig config) throws ServletException {
		setFilterConfig(config);
		Configuration configuration = null;
		try {

			configuration = RodaCoreFactory.getConfiguration(configFile);

		} catch (ConfigurationException e) {
			logger.error("Error reading configuration file " + configFile + " - " + e.getMessage());
		}

		if (configuration != null) {
			casLogoutURL = configuration.getString("roda.cas.url") + "/logout";
		}

		logger.info(getClass().getSimpleName() + " initialized ok");
	}

	/**
	 * Default constructor.
	 */
	public RolesSetterFilter() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			HttpServletRequest servletRequest = (HttpServletRequest) request;
			HttpServletResponse servletResponse = (HttpServletResponse) response;
			if (servletRequest.getUserPrincipal() != null) {
				logger.error("servletRequest.getUserPrincipal() SET : " + servletRequest.getUserPrincipal().getName());
				if (existUser(servletRequest.getUserPrincipal().getName())) {
					logger.error("User exists internally...");
					UserUtility.setUser(servletRequest, getUser(servletRequest.getUserPrincipal()));
				} else {
					logger.error("User does not exist internally...");
					RodaSimpleUser rsu = getUser(servletRequest.getUserPrincipal());
					addUserToDefault(request, rsu);
					UserUtility.setUser(servletRequest, rsu);
				}
			} else {
				logger.error("servletRequest.getUserPrincipal() NOT SET");
				if (UserUtility.getUser(servletRequest) == null) {
					logger.error("User not in request... Setting guest...");
					UserUtility.setUser(servletRequest, getGuest());
				} else {
					logger.error("User in request ;) ");
				}

			}

			String url = servletRequest.getRequestURL().toString();
			if (url.endsWith("/login")) {
				url = url.substring(0, url.indexOf("login"));
				servletResponse.sendRedirect(url);
			} else if (url.endsWith("/logout")) {
				url = url.substring(0, url.indexOf("logout"));
				UserUtility.logout(servletRequest);
				String urlToRedirectTo = CommonUtils.constructRedirectUrl(casLogoutURL, "service", url, false, false);
				servletResponse.sendRedirect(urlToRedirectTo);
			} else {
				chain.doFilter(request, response);
			}

		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		}
	}

	private RodaSimpleUser getGuest() {
		RodaSimpleUser rsu = new RodaSimpleUser();
		rsu.setUsername("guest");
		rsu.setGuest(true);
		return rsu;
	}

	private void addUserToDefault(ServletRequest request, RodaSimpleUser userPrincipal) {
		// TODO addUser...
	}

	private RodaSimpleUser getUser(Principal userPrincipal) {
		logger.error("Getting  user: " + userPrincipal.getName());
		RodaSimpleUser rsu = new RodaSimpleUser();
		rsu.setUsername(userPrincipal.getName());
		rsu.setGuest(false);
		return rsu;
	}

	private boolean existUser(String name) {
		logger.error("Checking if user " + name + " exists...");
		return true;
	}

	private SearchControls getSimpleSearchControls() {
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		searchControls.setTimeLimit(30000);
		return searchControls;
	}

	protected Hashtable<Object, Object> createEnv(ServletContext servletContext) {

		// Fetch directory servive from servlet context
		DirectoryService directoryService = (DirectoryService) servletContext.getAttribute(DirectoryService.JNDI_KEY);

		Hashtable<Object, Object> env = new Hashtable<Object, Object>();
		env.put(DirectoryService.JNDI_KEY, directoryService);
		env.put(Context.PROVIDER_URL, "");
		env.put(Context.INITIAL_CONTEXT_FACTORY, CoreContextFactory.class.getName());

		// FIXME
		env.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
		env.put(Context.SECURITY_CREDENTIALS, "secret");
		env.put(Context.SECURITY_AUTHENTICATION, "simple");

		return env;
	}

}
