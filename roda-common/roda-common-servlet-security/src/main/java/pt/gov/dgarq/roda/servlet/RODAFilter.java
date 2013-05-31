package pt.gov.dgarq.roda.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * @author Rui Castro
 */
public abstract class RODAFilter implements Filter {

	static final private Logger logger = Logger.getLogger(RODAFilter.class);

	private final static String PARAMETER_REALM = "realm"; //$NON-NLS-1$

	/**
	 * Authentication Realm
	 */
	protected String realm = null;

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException {

		if (filterConfig.getInitParameter(PARAMETER_REALM) != null) {
			realm = filterConfig.getInitParameter(PARAMETER_REALM);
		} else {
			// Default value (null)
		}

	}

	protected void enforceAuthentication(HttpServletResponse response)
			throws IOException {

		logger.debug("Enforcing autentication...");

		String value = String.format("%1$s realm=\"%2$s\"",
				HttpServletRequest.BASIC_AUTH, realm);
		String name = "WWW-Authenticate";

		response.reset();

		if (response.containsHeader(name)) {
			response.setHeader(name, value);
		} else {
			response.addHeader(name, value);
		}

		try {

			response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
					"supply credentials");

		} catch (IOException e) {
			logger.error("Error sending error message to client - "
					+ e.getMessage(), e);
			throw e;
		}

		response.setContentType("text/plain");

		try {

			response.flushBuffer();

		} catch (IOException e) {
			logger.error("Error flushing buffer - " + e.getMessage(), e);
			throw e;
		}

	}

}
