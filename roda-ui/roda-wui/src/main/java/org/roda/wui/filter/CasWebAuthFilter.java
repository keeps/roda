/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.filter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.utils.URIBuilder;
import org.jasig.cas.client.util.CommonUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.RODAException;
import org.roda.wui.api.controllers.UserLogin;
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
    casLogoutURL = config.getInitParameter("casServerLogoutUrl");

    LOGGER.info("CAS logout URL = " + casLogoutURL);
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
    final String path = httpRequest.getParameter("path");
    final String hash = httpRequest.getParameter("hash");

    Map<String, String[]> parameterMap = new HashMap<>(httpRequest.getParameterMap());
    parameterMap.remove("path");
    parameterMap.remove("hash");
    parameterMap.remove("ticket");

    URIBuilder uri = new URIBuilder();

    // Setting RODA host via config
    String rodaServerName = RodaCoreFactory.getRodaConfiguration().getString("ui.filter.cas.serverName", "");
    if (StringUtils.isNotBlank(rodaServerName)) {
      URI rodaServerNameUri = URI.create(rodaServerName);
      uri.setScheme(rodaServerNameUri.getScheme());
      uri.setHost(rodaServerNameUri.getHost());
      uri.setPort(rodaServerNameUri.getPort());
    }

    uri.setPath(path);

    // adding all other parameters
    parameterMap.forEach((param, values) -> {
      for (String value : values) {
        uri.addParameter(param, value);
      }
    });

    LOGGER.info("URL: {} ; Request URI: {} ; Path: {} ; Hash: {}; Parameters: {}", url, requestURI, path, hash,
      parameterMap);

    if (url.endsWith("/login")) {
      if (httpRequest.getUserPrincipal() != null) {
        try {
          UserLogin.casLogin(httpRequest.getUserPrincipal().getName(), httpRequest);
        } catch (RODAException e) {
          LOGGER.error("Error authenticating CAS user", e);
          httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
          return;
        }
      }

      uri.setFragment(hash);

      try {
        httpResponse.sendRedirect(uri.build().toString());
      } catch (URISyntaxException e) {
        LOGGER.error("Could not generate service URL, redirecting to base path " + path, e);
        httpResponse.sendRedirect(path);
      }

    } else if (url.endsWith("/logout")) {

      UserLogin.logout(httpRequest, Arrays.asList("edu.yale.its.tp.cas.client.filter.user", "_const_cas_assertion_"));

      // TODO add RODA host from config

      // discard hash and set it to the welcome page
      uri.setFragment(Welcome.RESOLVER.getHistoryToken());

      String service;
      try {
        service = uri.build().toString();
      } catch (URISyntaxException e) {
        LOGGER.error("Could not generate service URL, redirecting to base path " + path, e);
        service = path;
      }

      httpResponse.sendRedirect(CommonUtils.constructRedirectUrl(casLogoutURL, "service", service, false, false));

    } else {
      chain.doFilter(request, response);
    }

  }
}
