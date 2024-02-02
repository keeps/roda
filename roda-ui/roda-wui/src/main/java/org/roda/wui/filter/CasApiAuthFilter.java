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

import org.apache.commons.lang3.StringUtils;
import org.roda.core.common.JwtUtils;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InactiveUserException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.controllers.UserLogin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

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
        LOGGER.error("Inactive user '{}': {}", request.getUserPrincipal().getName(), e.getMessage(), e);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error authenticating user");
      } catch (RODAException e) {
        LOGGER.error("Error authenticating user '{}': {}", request.getUserPrincipal().getName(), e.getMessage(), e);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error authenticating user");
      }
      return;
    }

    // try bearer token auth
    final String token = new BearerAuthRequestWrapper(request).getBearerToken();
    if (token != null) {
      try {
        doFilterWithToken(request, response, filterChain, token);
      } catch (NotFoundException | AuthenticationDeniedException e) {
        LOGGER.error("Error authenticating token '{}': {}", token, e.getMessage(), e);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error authenticating user");
      } catch (final GenericException e) {
        throw new ServletException(e.getMessage(), e);
      }
    }

    // try basic auth
    final Pair<String, String> credentials = new BasicAuthRequestWrapper(request).getCredentials();
    if (credentials != null) {
      try {
        doFilterWithCredentials(request, response, filterChain, credentials.getFirst(), credentials.getSecond());
      } catch (final AuthenticationDeniedException | NotFoundException e) {
        if (request.getUserPrincipal() != null) {
          LOGGER.error("Error authenticating user '{}': {}" + e.getMessage(), request.getUserPrincipal().getName(),
            e.getMessage(), e);
        } else {
          LOGGER.error("Error authenticating user {}", e.getMessage(), e);
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error authenticating user");
      } catch (final GenericException e) {
        throw new ServletException(e.getMessage(), e);
      }
      return;
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
    throws GenericException, IOException, ServletException, AuthenticationDeniedException, NotFoundException {

    UserUtility.checkUserApiBasicAuth(username);
    // check if user is internal
    if (UserUtility.getLdapUtility().isInternal(username)) {
      final User user = UserUtility.getLdapUtility().getAuthenticatedUser(username, password);
      UserUtility.setUser(request, user);
    }
    filterChain.doFilter(request, response);
  }

  private void doFilterWithToken(final HttpServletRequest request, final HttpServletResponse response,
    final FilterChain filterChain, final String token)
    throws GenericException, IOException, ServletException, AuthenticationDeniedException, NotFoundException {

    String username = JwtUtils.getSubjectFromToken(token);
    // check if user is internal
    if (UserUtility.getLdapUtility().isInternal(username)) {
      User user = UserUtility.getLdapUtility().getUser(username);
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
