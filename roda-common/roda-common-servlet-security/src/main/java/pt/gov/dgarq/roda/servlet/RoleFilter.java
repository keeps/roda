package pt.gov.dgarq.roda.servlet;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * This filter the requests based on the {@link Principal} roles.
 * 
 * @author Rui Castro
 */
public class RoleFilter extends RODAFilter {

	static final private Logger logger = Logger.getLogger(RoleFilter.class);

	private final static String PARAMETER_ROLE = "role"; //$NON-NLS-1$
	private final static String PARAMETER_AUTHENTICATE_ON_FAIL = "authenticate_on_fail"; //$NON-NLS-1$

	/**
	 * The roles necessary for passing this filter.
	 */
	protected List<String> roles = new ArrayList<String>();

	/**
	 * Ask for authentication if this filter fails
	 */
	protected boolean authenticateOnFail = false;

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException {

		super.init(filterConfig);

		if (filterConfig == null) {

			throw new ServletException("Filter configuration doesn't exist.");

		} else {

			if (filterConfig.getInitParameter(PARAMETER_ROLE) != null) {

				// role = filterConfig.getInitParameter("role");

				String roleValue = filterConfig
						.getInitParameter(PARAMETER_ROLE);
				String[] roleList = roleValue.split(","); //$NON-NLS-1$

				for (String role : roleList) {
					if (!StringUtils.isBlank(role)) {
						roles.add(role);
					}
				}

				if (roles.isEmpty()) {
					throw new ServletException(
							"role parameter doesn't contain any roles");
				}

			} else {
				// Default value ()
				throw new ServletException(
						"role parameter missing in filter configuration");
			}

			logger.debug(String.format("(%1$s)", roles));
		}

		if (filterConfig.getInitParameter(PARAMETER_AUTHENTICATE_ON_FAIL) != null) {

			this.authenticateOnFail = Boolean.parseBoolean(filterConfig
					.getInitParameter(PARAMETER_AUTHENTICATE_ON_FAIL));

		} else {
			// Default value (false)
			this.authenticateOnFail = false;
		}

		logger.debug(String.format("(%s, %b, %s)", roles, authenticateOnFail,
				realm));
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest servletRequest,
			ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		boolean hasRole = false;

		for (int i = 0; hasRole == false && i < roles.size(); i++) {
			hasRole = request.isUserInRole(roles.get(i));
		}

		if (hasRole) {

			logger.trace("isUserInRole(" + roles
					+ ") returned TRUE. Letting request continue...");

			chain.doFilter(request, servletResponse);

		} else {

			logger.trace("isUserInRole(" + roles + ") returned false");

			Principal userPrincipal = request.getUserPrincipal();

			String message = null;
			if (userPrincipal != null) {
				message = "Principal " + userPrincipal.getName()
						+ " not in any of roles " + roles;
			} else {
				message = "No principal";
			}

			if (this.authenticateOnFail) {

				logger.trace("Request " + request.getPathInfo()
						+ " blocked. Enforcing authentication.");

				enforceAuthentication(response);

			} else {

				logger.debug("Request " + request.getPathInfo()
						+ " blocked. Sending UNAUTHORIZED");

				response
						.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
				response.setContentType("text/plain");
				response.flushBuffer();
			}
		}

	}

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		logger.info(String.format("(%s, %b, %s) destroy()", roles,
				authenticateOnFail, realm));
	}

}
