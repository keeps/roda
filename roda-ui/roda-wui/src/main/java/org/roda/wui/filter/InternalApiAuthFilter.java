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

import org.apache.commons.lang3.StringUtils;
import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.user.User;
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
        UserUtility.setUser(request, getBasicAuthUser(request));
        chain.doFilter(servletRequest, servletResponse);
      } catch (final AuthenticationDeniedException | GenericException e) {
        LOGGER.debug(e.getMessage(), e);
        response.setHeader(RodaConstants.HTTP_HEADERS_WWW_AUTHENTICATE, "Basic realm=\"" + realm + "\"");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      }
    } else {
      chain.doFilter(servletRequest, servletResponse);
    }

  }

  /**
   * Return a {@link User} from the HTTP Basic auth header information.
   * 
   * @param request
   *          the HTTP request.
   * @return the {@link User}.
   * @throws AuthenticationDeniedException
   *           if the credentials are invalid.
   * @throws GenericException
   *           if some other error occurs.
   */
  private User getBasicAuthUser(final HttpServletRequest request)
    throws AuthenticationDeniedException, GenericException {
    final Pair<String, String> credentials = new BasicAuthRequestWrapper(request).getCredentials();
    if (credentials == null) {
      throw new AuthenticationDeniedException("No credentials!");
    } else {
      final User user = UserUtility.getLdapUtility().getAuthenticatedUser(credentials.getFirst(),
        credentials.getSecond());
      return user;
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
      String pathInfo = request.getPathInfo();
      if (pathInfo != null && exclusion != null && pathInfo.matches(exclusion)) {
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
