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

import org.apache.http.client.utils.URIBuilder;
import org.apereo.cas.client.util.CommonUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.InactiveUserException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.wui.api.controllers.UserLogin;
import org.roda.wui.common.client.tools.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

    LOGGER.info("CAS logout URL = {}", casLogoutURL);
    LOGGER.info("{} initialized ok", getClass().getSimpleName());
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
      uri.setFragment(hash);
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
      try {
        if (httpRequest.getUserPrincipal() != null) {
          try {
            UserLogin.casLogin(httpRequest.getUserPrincipal().getName(), httpRequest);
          } catch (InactiveUserException e) {
            LOGGER.error("Error authenticating CAS user", e);
            httpResponse.sendRedirect(uri.build().toString() + "#theme/ErrorInactiveAccount.html");
            return;
          } catch (RODAException e) {
            LOGGER.error("Error authenticating CAS user", e);
            httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error authenticating CAS user");
            return;
          }
        }

        httpResponse.sendRedirect(uri.build().toString());
      } catch (URISyntaxException e) {
        LOGGER.error("Could not generate service URL, redirecting to base path " + path, e);
        httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Unexpected error, see server logs for details");
      }

    } else if (url.endsWith("/logout")) {

      UserLogin.logout(httpRequest, Arrays.asList("edu.yale.its.tp.cas.client.filter.user", "_const_cas_assertion_"));

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
