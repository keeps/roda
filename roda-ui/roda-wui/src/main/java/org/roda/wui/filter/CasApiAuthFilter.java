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
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.common.UserUtility;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InactiveUserException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.controllers.UserLogin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CAS authentication filter for API requests.
 *
 * @author Rui Castro <rui.castro@gmail.com>
 */
public class CasApiAuthFilter implements Filter {
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(CasApiAuthFilter.class);
  /** List of excluded URLs. */
  private final List<String> exclusions = new ArrayList<>();

  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {
    final String exclusionsParam = filterConfig.getInitParameter("exclusions");
    if (StringUtils.isNotBlank(exclusionsParam)) {
      final String[] listOfExclusions = exclusionsParam.split(",");
      for (String exclusion : listOfExclusions) {
        this.exclusions.add(exclusion.trim());
      }
    }
  }

  @Override
  public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
    final FilterChain filterChain) throws IOException, ServletException {

    final HttpServletRequest request = (HttpServletRequest) servletRequest;
    final HttpServletResponse response = (HttpServletResponse) servletResponse;

    if (isRequestUrlExcluded(request)) {
      LOGGER.debug("Request is ignored.");
      filterChain.doFilter(request, response);
      return;
    }

    // check CAS auth
    if (request.getUserPrincipal() != null) {
      try {
        UserLogin.casLogin(request.getUserPrincipal().getName(), request);
        filterChain.doFilter(request, response);
      } catch (InactiveUserException e) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
          "Inactive user '" + request.getUserPrincipal().getName() + "': " + e.getMessage());
      } catch (RODAException e) {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Error authenticating CAS user '" + request.getUserPrincipal().getName() + "': " + e.getMessage());
      }
      return;
    }

    // try basic auth
    try {
      final Pair<String, String> credentials = new BasicAuthRequestWrapper(request).getCredentials();
      if (credentials != null) {
        doFilterWithCredentials(request, response, filterChain, credentials.getFirst(), credentials.getSecond());
      }
    } catch (final AuthenticationDeniedException e) {
      LOGGER.debug(e.getMessage(), e);
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
    } catch (final GenericException e) {
      throw new ServletException(e.getMessage(), e);
    }

    // try the user in session
    User user = UserUtility.getUser(request);
    if (user != null) {
      filterChain.doFilter(request, response);
      return;
    }

    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No credentials");
  }

  @Override
  public void destroy() {
    // do nothing
  }

  private void doFilterWithCredentials(final HttpServletRequest request, final HttpServletResponse response,
    final FilterChain filterChain, final String username, final String password)
    throws GenericException, IOException, ServletException, AuthenticationDeniedException {

    // check if user is internal
    if (UserUtility.getLdapUtility().isInternal(username)) {
      final User user = UserUtility.getLdapUtility().getAuthenticatedUser(username, password);
      UserUtility.setUser(request, user);
    }

    filterChain.doFilter(request, response);
  }

  private String constructServiceUrl(final HttpServletRequest request) {
    return String.format("%s?%s", request.getRequestURL(), request.getQueryString());
  }

  /**
   * Is the requested path in the list of exclusions?
   *
   * @param request
   *          the request.
   *
   * @return <code>true</code> if it is excluded and <code>false</code> otherwise.
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

  /**
   * A {@link HttpServletRequestWrapper} that adds a <code>ticket</code> query
   * string parameter.
   * 
   * @author Rui Castro <rui.castro@gmai.com>
   */
  private class ServiceTicketRequestWrapper extends HttpServletRequestWrapper {

    /**
     * CAS service ticket.
     */
    private final String serviceTicket;
    /**
     * The query string.
     */
    private String queryString = null;

    /**
     * Constructor.
     * 
     * @param request
     *          the HTTP request.
     * @param serviceTicket
     *          the CAS service ticket.
     */
    ServiceTicketRequestWrapper(final HttpServletRequest request, final String serviceTicket) {
      super(request);
      setAttribute("ticket", serviceTicket);
      this.serviceTicket = serviceTicket;
    }

    @Override
    public String getQueryString() {
      if (this.queryString == null) {
        String qs = super.getQueryString();
        if (StringUtils.isBlank(qs)) {
          qs = "";
        } else {
          qs = qs + "&";
        }
        this.queryString = String.format("%sticket=%s", qs, this.serviceTicket);
      }
      return this.queryString;
    }

    @Override
    public String getParameter(final String name) {
      if ("ticket".equals(name)) {
        return this.serviceTicket;
      } else {
        return super.getParameter(name);
      }
    }
  }
}
