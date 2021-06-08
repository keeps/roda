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
      String[] authHeaderArr = authHeader.split("Barear ");
      if (authHeaderArr.length > 1 && authHeaderArr[1] != null) {
        String token = authHeaderArr[1];
        try {
          Claims claims = Jwts.parser().setSigningKey(RodaCoreFactory.getApiSecretKey()).parseClaimsJws(token)
            .getBody();
          httpRequest.setAttribute("accessTokenId", claims.get("accessTokenId").toString());
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
