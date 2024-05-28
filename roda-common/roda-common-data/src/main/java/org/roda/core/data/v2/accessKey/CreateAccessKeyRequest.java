package org.roda.core.data.v2.accessKey;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class CreateAccessKeyRequest implements Serializable {
  @Serial
  private static final long serialVersionUID = -2706255856347304025L;

  private String name;
  private Date expirationDate;
  private String userName;

  public CreateAccessKeyRequest() {
    this.name = null;
    this.userName = null;
    this.expirationDate = new Date();
  }

  public CreateAccessKeyRequest(String name, Date expirationDate, String userName) {
    this.name = name;
    this.userName = userName;
    this.expirationDate = expirationDate;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(Date expirationDate) {
    this.expirationDate = expirationDate;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

}
