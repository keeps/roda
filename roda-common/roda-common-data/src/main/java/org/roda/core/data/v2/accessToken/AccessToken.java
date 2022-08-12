/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.accessToken;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.roda.core.data.common.RodaConstants;

import java.io.Serializable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_ACCESS_TOKEN)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccessToken implements Serializable {
  private static final long serialVersionUID = -5623439180546915134L;
  private String token;
  private long expiresIn;

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public long getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(long expiresIn) {
    this.expiresIn = expiresIn;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AccessToken that = (AccessToken) o;

    if (expiresIn != that.expiresIn) return false;
    return token != null ? token.equals(that.token) : that.token == null;
  }

  @Override
  public int hashCode() {
    int result = token != null ? token.hashCode() : 0;
    result = 31 * result + (int) (expiresIn ^ (expiresIn >>> 32));
    return result;
  }
}
