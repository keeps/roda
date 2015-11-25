/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.roda.core.common.UserUtility;
import org.roda.wui.common.client.tools.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class RodaInternalAuthenticationFilter implements Filter {
  private static final Logger LOGGER = LoggerFactory.getLogger(RodaInternalAuthenticationFilter.class);

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
    String requestURI = httpRequest.getRequestURI();
    String service = httpRequest.getParameter("service");
    String hash = httpRequest.getParameter("hash");

    LOGGER.debug("URL: " + url);
    LOGGER.debug("Request URI: " + requestURI);
    LOGGER.debug("Service: " + service);
    LOGGER.debug("hash: " + hash);

    if (requestURI.equals("/login")) {
      // FIXME add this to configuration
      String redirect = "/#login";

      if (hash != null) {
        redirect += Tools.HISTORY_SEP + hash;
      }

      httpResponse.sendRedirect(redirect);
    } else if (requestURI.equals("/logout")) {
      UserUtility.logout(httpRequest);
      httpResponse.sendRedirect("/#home");
    } else {
      chain.doFilter(request, response);
    }
  }

  @Override
  public void destroy() {
    // do nothing
  }

}
