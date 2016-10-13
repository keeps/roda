/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.filter;

import java.io.IOException;
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
import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.common.UserUtility;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internal authentication filter for API requests.
 */
public class InternalApiAuthFilter implements Filter {
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(InternalApiAuthFilter.class);
  /** Paths excluded from being filtered. */
  private List<String> exclusions = new ArrayList<>();
  /** Authentication realm. */
  private String realm = "";

  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {
    final String realmParam = filterConfig.getInitParameter("realm");
    if (StringUtils.isNotBlank(realmParam)) {
      realm = realmParam;
    }
    final String exclusionsParam = filterConfig.getInitParameter("exclusions");
    if (StringUtils.isNotBlank(exclusionsParam)) {
      final String[] listOfExclusions = exclusionsParam.split(",");
      for (String exclusion : listOfExclusions) {
        exclusions.add(exclusion.trim());
      }
    }
  }

  @Override
  public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
    final FilterChain chain) throws IOException, ServletException {

    final HttpServletRequest request = (HttpServletRequest) servletRequest;
    final HttpServletResponse response = (HttpServletResponse) servletResponse;

    if (!isRequestUrlExcluded(request) && request.getSession().getAttribute(UserUtility.RODA_USER) == null) {
      // No user yet
      try {

        UserUtility.setUser(request, UserUtility.getApiUser(request));
        chain.doFilter(servletRequest, servletResponse);

      } catch (final AuthorizationDeniedException e) {
        LOGGER.debug(e.getMessage(), e);
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"" + realm + "\"");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      }
    } else {
      chain.doFilter(servletRequest, servletResponse);
    }

  }

  /**
   * Is the requested path in the list of exclusions?
   * 
   * @param request
   *          the request.
   * 
   * @return <code>true</code> if it is excluded and <code>false</code>
   *         otherwise.
   */
  private boolean isRequestUrlExcluded(final HttpServletRequest request) {
    for (String exclusion : this.exclusions) {
      if (request.getPathInfo().matches(exclusion)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void destroy() {
    // do nothing
  }

}
