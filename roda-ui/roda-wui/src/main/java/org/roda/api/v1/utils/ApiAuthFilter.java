package org.roda.api.v1.utils;

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
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.lang3.StringUtils;

public class ApiAuthFilter implements Filter {
  private List<String> exclusions = new ArrayList<String>();
  private String realm = "";

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    String realmParam = filterConfig.getInitParameter("realm");
    if (StringUtils.isNotBlank(realmParam)) {
      realm = realmParam;
    }
    String exclusionsParam = filterConfig.getInitParameter("exclusions");
    if (StringUtils.isNotBlank(exclusionsParam)) {
      String[] listOfExclusins = exclusionsParam.split(",");
      for (String exclusion : listOfExclusins) {
        exclusions.add(exclusion.trim());
      }
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {
    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
    HttpServletResponse httpServletResponse = (HttpServletResponse) response;

    String path = httpServletRequest.getPathInfo();
    if (StringUtils.isNotBlank(path) && !exclusions.contains(path)) {

      String authorization = httpServletRequest.getHeader("Authorization");

      if (authorization == null) {
        httpServletResponse.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"" + realm + "\"");
        httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }
    }

    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
    // do nothing
  }

}
