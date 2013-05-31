package pt.gov.dgarq.roda.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * @author Rui Castro
 */
public class RemoteHostFilter implements Filter {

	static final private Logger logger = Logger
			.getLogger(RemoteHostFilter.class);

	// private String hostname = null;
	private List<String> hostnames = null;

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException {

		if (filterConfig == null) {

			logger
					.debug("init() filter configuration is null. Ignoring filter");

		} else {

			if (filterConfig.getInitParameter("hostname") != null) {

				String hostnameValue = filterConfig
						.getInitParameter("hostname");

				logger.debug("hostnameValue: " + hostnameValue);

				String[] values = hostnameValue.split(",\\e*");
				hostnames = Arrays.asList(values);

				logger.debug("hostnames: " + hostnames);

			} else {
				// Default value (localhost)
			}

		}

		logger.info(getClass().getSimpleName() + " initialized ok");
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest servletRequest,
			ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {

		logger.trace("doFilter() ...");

		if (hostnames == null || hostnames.size() == 0) {

			// IGNORE this filter. Continue filter chain
			chain.doFilter(servletRequest, servletResponse);

		} else if (hostnames.contains(servletRequest.getRemoteHost())) {

			logger.debug("doFilter() " + servletRequest.getRemoteHost()
					+ " is an allowed host. Let the request continue.");

			// continue filter chain
			chain.doFilter(servletRequest, servletResponse);

		} else {

			logger.info("doFilter() " + servletRequest.getRemoteHost()
					+ " is NOT an allowed host. Blocking request.");

			HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

			httpResponse.reset();
			httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "host "
					+ servletRequest.getRemoteHost() + " is not authorized");
			httpResponse.setContentType("text/plain");
			httpResponse.flushBuffer();
		}

	}

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		logger.info("destroy()");
	}

}
