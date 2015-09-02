package pt.gov.dgarq.roda.wui.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.roda.common.UserUtility;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class RodaInternalAuthenticationFilter implements Filter {
	private static final Logger LOGGER = Logger.getLogger(RodaInternalAuthenticationFilter.class);

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		LOGGER.info(getClass().getSimpleName() + " initialized ok");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		LOGGER.debug("executing doFilter");

		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final HttpServletResponse httpResponse = (HttpServletResponse) response;

		String url = httpRequest.getRequestURL().toString();
		if (url.endsWith("/login")) {
			// FIXME add this to configuration
			httpRequest.getRequestDispatcher("/login.html").forward(httpRequest, httpResponse);
		} else if (url.endsWith("/logout")) {
			url = url.substring(0, url.indexOf("logout"));
			UserUtility.logout(httpRequest);
			httpRequest.getRequestDispatcher("/#home").forward(httpRequest, httpResponse);
		} else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
		// do nothing
	}

}
