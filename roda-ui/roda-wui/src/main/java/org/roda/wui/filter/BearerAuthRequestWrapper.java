/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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
