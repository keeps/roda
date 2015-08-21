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
import org.roda.common.UserUtility;

import pt.gov.dgarq.roda.common.RodaCoreFactory;
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
				if (existUser(servletRequest.getUserPrincipal().getName())) {
					UserUtility.setUser(servletRequest, getUser(servletRequest.getUserPrincipal()));
				} else {
					RodaSimpleUser rsu = getUser(servletRequest.getUserPrincipal());
					addUserToDefault(request, rsu);
					UserUtility.setUser(servletRequest, rsu);
				}
			} else {
				if (UserUtility.getUser(servletRequest) == null) {
					UserUtility.setUser(servletRequest, getGuest());
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
		rsu.setId("guest");
		rsu.setGuest(true);
		return rsu;
	}

	private void addUserToDefault(ServletRequest request, RodaSimpleUser userPrincipal) {
		// TODO addUser...
	}

	private RodaSimpleUser getUser(Principal userPrincipal) {
		RodaSimpleUser rsu = new RodaSimpleUser();
		rsu.setId(userPrincipal.getName());
		rsu.setGuest(false);
		return rsu;
	}

	private boolean existUser(String name) {
		return true;
	}
}
