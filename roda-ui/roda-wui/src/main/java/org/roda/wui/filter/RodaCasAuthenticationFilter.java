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
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet Filter implementation class RolesSetterFilter
 */
public class RodaCasAuthenticationFilter implements Filter {
  private static final Logger logger = LoggerFactory.getLogger(RodaCasAuthenticationFilter.class);

  private FilterConfig config;
  protected String casLogoutURL = null;

  public void setFilterConfig(FilterConfig config) {
    this.config = config;
  }

  public FilterConfig getFilterConfig() {
    return config;
  }

  public void init(FilterConfig config) throws ServletException {
    setFilterConfig(config);

    casLogoutURL = RodaCoreFactory.getRodaConfiguration().getString("roda.cas.url") + "/logout";

    logger.info(getClass().getSimpleName() + " initialized ok");
  }

  /**
   * Default constructor.
   */
  public RodaCasAuthenticationFilter() {
    // do nothing
  }

  /**
   * @see Filter#destroy()
   */
  public void destroy() {
    // do nothing
  }

  /**
   * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
   */
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {
    try {
      HttpServletRequest servletRequest = (HttpServletRequest) request;
      HttpServletResponse servletResponse = (HttpServletResponse) response;
      if (servletRequest.getUserPrincipal() != null) {
        if (userExists(servletRequest.getUserPrincipal().getName())) {
          logger.debug("User principal and user exist (" + servletRequest.getUserPrincipal().getName() + ")");
          UserUtility.setUser(servletRequest, getUser(servletRequest.getUserPrincipal()));
        } else {
          logger.debug("User principal exist but user doesn't (" + servletRequest.getUserPrincipal().getName() + ")");
          User rsu = getUser(servletRequest.getUserPrincipal());
          logger.debug("Adding user to ldap/index: " + rsu);
          addUserToLdapAndIndex(request, rsu);
          UserUtility.setUser(servletRequest, rsu);
        }
      } else {
        UserUtility.setUser(servletRequest, UserUtility.getUser(servletRequest));
      }

      String url = servletRequest.getRequestURL().toString();
      if (url.endsWith("/login")) {
        url = url.substring(0, url.indexOf("login"));
        servletResponse.sendRedirect(url);
      } else if (url.endsWith("/logout")) {
        url = url.substring(0, url.indexOf("logout"));
        UserUtility.logout(servletRequest);
        String urlToRedirectTo = CommonUtils.constructRedirectUrl(casLogoutURL, "service", url, false, false);
        servletResponse.sendRedirect(urlToRedirectTo);
      } else {
        chain.doFilter(request, response);
      }

    } catch (RuntimeException t) {
      logger.error(t.getMessage(), t);
    }
  }

  // TODO test this
  private void addUserToLdapAndIndex(ServletRequest request, User userPrincipal) {
    try {
      final User user = new User(userPrincipal);
      RodaCoreFactory.getModelService().addUser(user, true);
    } catch (RODAException e) {
      logger.error("Error while creating and indexing user", e);
    }
  }

  private User getUser(Principal userPrincipal) {
    User rsu = new User();
    rsu.setId(userPrincipal.getName());
    rsu.setGuest(false);
    return rsu;
  }

  private boolean userExists(String name) {
    boolean exist;
    org.roda.core.data.adapter.filter.Filter filter = new org.roda.core.data.adapter.filter.Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.MEMBERS_ID, name));
    filter.add(new SimpleFilterParameter(RodaConstants.MEMBERS_IS_USER, "true"));
    try {
      Long count = RodaCoreFactory.getIndexService().count(RODAMember.class, filter);
      exist = (count == 1);
    } catch (RODAException e) {
      exist = false;
    }

    return exist;
  }
}
