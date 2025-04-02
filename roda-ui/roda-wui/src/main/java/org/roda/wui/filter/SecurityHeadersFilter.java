package org.roda.wui.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

public class SecurityHeadersFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {
    HttpServletResponse httpServletResponse = (HttpServletResponse) response;

    httpServletResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
    httpServletResponse.setHeader("Content-Security-Policy",
      "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://www.google.com "
        + "https://www.google-analytics.com https://www.gstatic.com http://127.0.0.1:9876 http://localhost:9876; "
        + "style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self';");
    httpServletResponse.setHeader("X-XSS-Protection", "1; mode=block");
    httpServletResponse.setHeader("X-Permitted-Cross-Domain-Policies", "none");
    httpServletResponse.setHeader("Feature-Policy",
      "camera 'none'; fullscreen 'self'; geolocation *; " + "microphone 'self'");
    httpServletResponse.setHeader("X-Frame-Options", "SAMEORIGIN");
    httpServletResponse.setHeader("X-Content-Type-Options", "nosniff");
    httpServletResponse.setHeader("Referrer-Policy", "no-referrer");
    httpServletResponse.setHeader("Permissions-Policy", "geolocation=(self)");

    chain.doFilter(request, response);
  }

  @Override
  public void init(FilterConfig filterConfig) {
  }

  @Override
  public void destroy() {
  }
}