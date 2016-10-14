package org.roda.wui.filter;

import java.nio.charset.Charset;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.Pair;

/**
 * A {@link HttpServletRequestWrapper} that provides a method to access username
 * and password from Basic auth information.
 * 
 * @author Rui Castro <rui.castro@gmail.com>
 */
public class BasicAuthRequestWrapper extends HttpServletRequestWrapper {

  /**
   * Constructor.
   * 
   * @param request
   *          the HTTP request.
   */
  public BasicAuthRequestWrapper(final HttpServletRequest request) {
    super(request);
  }

  /**
   * Returns a {@link Pair} of {@link String}s with the username and password
   * contained in the HTTP header <strong>Authorization</strong> or
   * <code>null</code> if the credentials could not be extracted.
   * 
   * @return a {@link Pair} with username and password.
   */
  public Pair<String, String> getCredentials() {
    Pair<String, String> ret = null;
    final String authorization = getHeader("Authorization");
    if (authorization != null && authorization.startsWith("Basic")) {
      String credentials = authorization;
      credentials = credentials.replaceFirst("[B|b]asic ", "");
      credentials = new String(Base64.getDecoder().decode(credentials),
        Charset.forName(RodaConstants.DEFAULT_ENCODING));
      final String[] values = credentials.split(":", 2);
      if (values[0] != null && values[1] != null) {
        ret = new Pair<>(values[0], values[1]);
      }
    }
    return ret;
  }
}
