package org.roda.wui.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.JwtUtils;
import org.roda.core.common.UserUtility;
import org.roda.core.data.v2.user.User;
import org.springframework.web.filter.GenericFilterBean;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class JwtFilter extends GenericFilterBean {
  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
    throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
    HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

    String authHeader = httpRequest.getHeader("Authorization");
    if (authHeader != null) {
      String[] authHeaderArr = authHeader.split("Bearer ");
      if (authHeaderArr.length > 1 && authHeaderArr[1] != null) {
        String token = authHeaderArr[1];
        try {
          String subject = JwtUtils.getSubjectFromToken(token);
          User user = UserUtility.getLdapUtility().getUser(subject);
          UserUtility.setUser(httpRequest, user);
        } catch (Exception e) {
          httpResponse.sendError(HttpStatus.SC_FORBIDDEN, "invalid/expired token");
          return;
        }
      } else {
        httpResponse.sendError(HttpStatus.SC_FORBIDDEN, "Authorization token must be Bearer [token]");
        return;
      }
    } else {
      httpResponse.sendError(HttpStatus.SC_FORBIDDEN, "Authorization token must be provider");
      return;
    }

    filterChain.doFilter(servletRequest, servletResponse);
  }
}
