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
    uri.setFragment(hash);

    if (uri.isAbsolute()) {
      LOGGER.error("ERROR");
      httpResponse.sendRedirect("/");
    }

    // adding all other parameters
    parameterMap.forEach((param, values) -> {
      for (String value : values) {
        uri.addParameter(param, value);
      }
    });

    LOGGER.info("URL: {} ; Request URI: {} ; Path: {} ; Hash: {}; Parameters: {}", url, requestURI, path, hash,
      parameterMap);

    if (requestURI.endsWith("/login")) {
      if (!hash.startsWith("login" + HistoryUtils.HISTORY_SEP)) {
        uri.setFragment("login" + HistoryUtils.HISTORY_SEP + hash);
      }

      redirect(httpResponse, uri);
    } else if (requestURI.endsWith("/logout")) {
      UserLogin.logout(httpRequest, Collections.emptyList());

      redirect(httpResponse, uri);

    } else {
      chain.doFilter(request, response);
    }
  }

  private void redirect(HttpServletResponse httpResponse, URIBuilder uri) throws IOException {
    try {
      URI redirectURL = uri.build();
      if (redirectURL.isAbsolute()) {
        httpResponse.sendRedirect("/");
      } else {
        httpResponse.sendRedirect(redirectURL.toString());
      }

    } catch (URISyntaxException e) {
      LOGGER.error("Could not generate service URL", e);
      httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        "Unexpected error, see server logs for details");
    }
  }

  @Override
  public void destroy() {
    // do nothing
  }

}
