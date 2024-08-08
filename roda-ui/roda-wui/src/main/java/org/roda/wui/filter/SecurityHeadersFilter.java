package org.roda.wui.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        httpServletResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        httpServletResponse.setHeader("Content-Security-Policy",
                "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://www.google.com " +
                        "https://www.google-analytics.com https://www.gstatic.com; style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self'; font-src 'self';");
        httpServletResponse.setHeader("X-XSS-Protection", "1; mode=block");
        httpServletResponse.setHeader("X-Permitted-Cross-Domain-Policies", "none");
        httpServletResponse.setHeader("Feature-Policy", "camera 'none'; fullscreen 'self'; geolocation *; " +
                "microphone 'self'");
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