package pt.gov.dgarq.roda.servlet;

import java.io.IOException;

import javax.naming.AuthenticationException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * @author Rui Castro
 */
public class SelectiveLdapAuthenticationFilter extends LdapAuthenticationFilter {

	static final private Logger logger = Logger
			.getLogger(SelectiveLdapAuthenticationFilter.class);

	private String guestFlag = "guest";
	private String guestUsername = "guest";
	private String guestPassword = "guest";

	/**
	 * @see LdapAuthenticationFilter#init(FilterConfig)
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		super.init(filterConfig);

		if (filterConfig == null) {

			logger
					.debug("init() filter configuration is null. Using defaults... "
							+ String.format("(%1$s)", guestFlag));

		} else {

			if (filterConfig.getInitParameter("guestFlag") != null) {
				guestFlag = filterConfig.getInitParameter("guestFlag");
			} else {
				// Default value (guest)
			}

			if (filterConfig.getInitParameter("guestUsername") != null) {
				guestUsername = filterConfig.getInitParameter("guestUsername");
			} else {
				// Default value (guest)
			}

			if (filterConfig.getInitParameter("guestPassword") != null) {
				guestPassword = filterConfig.getInitParameter("guestPassword");
			} else {
				// Default value (guest)
			}

		}
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest servletRequest,
			ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {

		logger.trace("doFilter() ...");

		HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;

		String guestFlagValue = httpRequest.getParameter(guestFlag);
		// default guest value is true
		boolean isGuest = guestFlagValue != null ? Boolean
				.parseBoolean(guestFlagValue) : true;

		logger.debug("Request is for guest? " + isGuest);

		if (isGuest) {

			try {

				RodaServletRequestWrapper request = new RodaServletRequestWrapper(
						httpRequest);

				request.setLdapUserPrincipal(new ExtendedUserPrincipal(
						ldapUtility.getAuthenticatedUser(guestUsername,
								guestPassword), guestPassword));

				super.doFilter(request, servletResponse, chain);

			} catch (AuthenticationException e) {
				logger.debug("Exception getting authenticated guest user - "
						+ e.getMessage(), e);
				throw new ServletException(
						"Exception getting authenticated guest user - "
								+ e.getMessage(), e);
			}

		} else {

			super.doFilter(httpRequest, servletResponse, chain);

		}

	}
}
