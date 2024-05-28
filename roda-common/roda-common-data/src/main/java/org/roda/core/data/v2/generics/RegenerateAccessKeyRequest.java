package org.roda.core.data.v2.generics;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class RegenerateAccessKeyRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = -2706255856347304025L;

  private Date expirationDate;

  public RegenerateAccessKeyRequest() {
    this.expirationDate = new Date();
  }

  public RegenerateAccessKeyRequest(Date expirationDate) {
    this.expirationDate = expirationDate;
  }

  public Date getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(Date expirationDate) {
    this.expirationDate = expirationDate;
  }
}
