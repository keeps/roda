/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.RodaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

public class SecurityHeadersFilter implements Filter {
  private static final Logger LOGGER = LoggerFactory.getLogger(SecurityHeadersFilter.class);
  private final Map<String, String> headers = new HashMap<>();
  private final static String SECURITY_HEADER_PROPERTY_PREFIX = "ui.filter.security-headers[]";
  private Boolean isInit = false;

  @Override
  public void init(FilterConfig filterConfig) {
  }

  private void initSecurityHeaders() {
    if (isInit) {
      return;
    }

    final Configuration rodaConfig = RodaCoreFactory.getRodaConfiguration();
    if (rodaConfig == null) {
      LOGGER.info("RODA configuration not available yet. Delaying init of SecurityHeadersFilter.");
    } else {
      List<String> headersNames = RodaUtils.copyList(rodaConfig.getList(SECURITY_HEADER_PROPERTY_PREFIX));

      for (String headersName : headersNames) {
        String value = rodaConfig.getString(SECURITY_HEADER_PROPERTY_PREFIX + "." + headersName, null);
        if (value != null) {
          headers.put(headersName, value);
        }
      }
      isInit = true;
      LOGGER.info("SecurityHeadersFilter initialized with {} headers", headers.size());
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {
    initSecurityHeaders();
    HttpServletResponse httpServletResponse = (HttpServletResponse) response;
    headers.forEach(httpServletResponse::setHeader);
    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
  }
}