package org.roda.wui.filter;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class JwtClientFilter implements ClientRequestFilter {
  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {

  }
}
