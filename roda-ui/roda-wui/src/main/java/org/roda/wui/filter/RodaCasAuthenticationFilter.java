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

import org.apache.log4j.Logger;
import org.jasig.cas.client.util.CommonUtils;
import org.roda.common.RodaCoreFactory;
import org.roda.common.UserUtility;
import org.roda.core.common.RodaConstants;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.v2.RODAMember;
import org.roda.core.data.v2.RodaSimpleUser;
import org.roda.core.data.v2.RodaUser;
import org.roda.core.data.v2.User;
import org.roda.index.IndexServiceException;
import org.roda.model.ModelServiceException;

/**
 * Servlet Filter implementation class RolesSetterFilter
 */
public class RodaCasAuthenticationFilter implements Filter {
  private static final Logger logger = Logger.getLogger(RodaCasAuthenticationFilter.class);

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
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
    ServletException {
    try {
      HttpServletRequest servletRequest = (HttpServletRequest) request;
      HttpServletResponse servletResponse = (HttpServletResponse) response;
      if (servletRequest.getUserPrincipal() != null) {
        if (userExists(servletRequest.getUserPrincipal().getName())) {
          logger.debug("User principal and user exist (" + servletRequest.getUserPrincipal().getName() + ")");
          UserUtility.setUser(servletRequest, getUser(servletRequest.getUserPrincipal()));
        } else {
          logger.debug("User principal exist but user doesn't (" + servletRequest.getUserPrincipal().getName() + ")");
          RodaSimpleUser rsu = getUser(servletRequest.getUserPrincipal());
          logger.debug("Adding user to ldap/index: " + rsu);
          addUserToLdapAndIndex(request, rsu);
          UserUtility.setUser(servletRequest, rsu);
        }
      } else {
        UserUtility.setUser(servletRequest, UserUtility.getUser(servletRequest, RodaCoreFactory.getIndexService()));
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

    } catch (Throwable t) {
      logger.error(t.getMessage(), t);
    }
  }

  // TODO test this
  private void addUserToLdapAndIndex(ServletRequest request, RodaSimpleUser userPrincipal) {
    try {
      User user = new User(new RodaUser(userPrincipal));
      RodaCoreFactory.getModelService().addUser(user, true, true);
    } catch (ModelServiceException e) {
      logger.error("Error while creating and indexing user", e);
    }
  }

  private RodaSimpleUser getUser(Principal userPrincipal) {
    RodaSimpleUser rsu = new RodaSimpleUser();
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
    } catch (IndexServiceException e) {
      exist = false;
    }

    return exist;
  }
}
