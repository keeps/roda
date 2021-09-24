package org.roda.wui.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * A {@link HttpServletRequestWrapper} that provides a method to access token
 * from Bearer auth information.
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class BearerAuthRequestWrapper extends HttpServletRequestWrapper {
  public BearerAuthRequestWrapper(HttpServletRequest request) {
    super(request);
  }

  public String getBearerToken() {
    String token = null;
    final String authorization = getHeader("Authorization");
    if (authorization != null && authorization.startsWith("Bearer")) {
      token = authorization.replaceFirst("[B|b]earer ", "");
    }
    return token;
  }
}
