/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.filter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
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
import org.roda.wui.api.controllers.UserLogin;
import org.roda.wui.client.welcome.Welcome;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internal authentication filter for web requests.
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 * @author Luis Faria <lfaria@keep.pt>
 */
public class InternalWebAuthFilter implements Filter {
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(InternalWebAuthFilter.class);

  @Override
  public void init(final FilterConfig config) throws ServletException {
    LOGGER.info("{} initialized ok", getClass().getSimpleName());
  }

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

    URIBuilder uri = new URIBuilder();
    uri.setPath(path);

    // adding all other parameters
    parameterMap.forEach((param, values) -> {
      for (String value : values) {
        uri.addParameter(param, value);
      }
    });

    LOGGER.info("URL: {} ; Request URI: {} ; Path: {} ; Hash: {}; Parameters: {}", url, requestURI, path, hash,
      parameterMap);

    if (requestURI.endsWith("/login")) {
      String safeFragment;
      if (!hash.startsWith("login" + HistoryUtils.HISTORY_SEP)) {
        safeFragment = "login" + HistoryUtils.HISTORY_SEP + hash;
      } else {
        safeFragment = hash;
      }

      try {
        httpResponse.sendRedirect(uri.build().toString() + "#" + safeFragment);
      } catch (URISyntaxException e) {
        LOGGER.error("Could not generate service URL, redirecting to base path " + path, e);
        httpResponse.sendRedirect(path);
      }
    } else if (requestURI.endsWith("/logout")) {
      UserLogin.logout(httpRequest, Collections.emptyList());

      // discard hash and set it to the welcome page
      uri.setFragment(Welcome.RESOLVER.getHistoryToken());

      try {
        httpResponse.sendRedirect(uri.build().toString());
      } catch (URISyntaxException e) {
        LOGGER.error("Could not generate service URL, redirecting to base path " + path, e);
        httpResponse.sendRedirect(path);
      }

    } else {
      chain.doFilter(request, response);
    }
  }

  @Override
  public void destroy() {
    // do nothing
  }

}
