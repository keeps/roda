/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.filter;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.client.util.CommonUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.UserUtility;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.client.welcome.Welcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CAS authentication filter for web requests.
 */
public class CasWebAuthFilter implements Filter {
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(CasWebAuthFilter.class);

  /** URL to logout in CAS service. */
  private String casLogoutURL;

  /**
   * Default constructor.
   */
  public CasWebAuthFilter() {
    // do nothing
  }

  @Override
  public void init(final FilterConfig config) throws ServletException {
    casLogoutURL = String.format("%s/logout", config.getInitParameter("casServerLogoutUrl"));

    LOGGER.info(getClass().getSimpleName() + " initialized ok");
  }

  /**
   * @see Filter#destroy()
   */
  @Override
  public void destroy() {
    // do nothing
  }

  /**
   * @param request
   *          the request.
   * @param response
   *          the response.
   * @param chain
   *          the filter chain.
   * @throws IOException
   *           if some I/O error occurs.
   * @throws ServletException
   *           if some error occurs.
   * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
   */
  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
    throws IOException, ServletException {

    final HttpServletRequest httpRequest = (HttpServletRequest) request;
    final HttpServletResponse httpResponse = (HttpServletResponse) response;

    final String url = httpRequest.getRequestURL().toString();
    final String requestURI = httpRequest.getRequestURI();
    final String service = httpRequest.getParameter("service");
    final String hash = httpRequest.getParameter("hash");
    final String locale = httpRequest.getParameter("locale");
    final String contextPath = httpRequest.getContextPath();

    LOGGER.debug("URL: {} ; Request URI: {} ; Context Path: {}; Service: {} ; Hash: {}; Locale: {}", url, requestURI,
      contextPath, service, hash, locale);

    final Principal principal = httpRequest.getUserPrincipal();
    if (principal != null) {
      UserUtility.setUser(httpRequest, getOrCreateUser(principal.getName()));
    }

    if (url.endsWith("/login")) {

      final StringBuilder b = new StringBuilder();
      b.append(contextPath + "/");

      if (StringUtils.isNotBlank(locale)) {
        b.append("?locale=").append(locale);
      }

      if (StringUtils.isNotBlank(hash)) {
        b.append("#").append(hash);
      }

      httpResponse.sendRedirect(b.toString());

    } else if (url.endsWith("/logout")) {

      UserUtility.logout(httpRequest);

      final StringBuilder b = new StringBuilder();
      b.append(url.substring(0, url.indexOf("logout")));

      if (StringUtils.isNotBlank(locale)) {
        b.append("?locale=").append(locale);
      }

      b.append("#").append(Welcome.RESOLVER.getHistoryToken());

      httpResponse.sendRedirect(CommonUtils.constructRedirectUrl(casLogoutURL, "service", b.toString(), false, false));

    } else {
      chain.doFilter(request, response);
    }

  }

  private User getOrCreateUser(final String name) {
    User user;
    try {
      user = RodaCoreFactory.getModelService().retrieveUserByName(name);
      LOGGER.debug(String.format("User principal and user exist (%s)", name));
    } catch (final GenericException e) {
      LOGGER.debug(String.format("Error getting user '%s' - %s", name, e.getMessage()), e);
      LOGGER.debug(String.format("User principal exist but user doesn't (%s)", name));

      user = new User(name);
      LOGGER.debug("Adding user to ldap/index: " + user);
      user = createUser(user);
    }
    return user;
  }

  private User createUser(final User user) {
    try {
      return RodaCoreFactory.getModelService().createUser(user, true);
    } catch (final RODAException e) {
      LOGGER.error("Error while creating and indexing user - " + e.getMessage(), e);
      return user;
    }
  }

}
