package org.roda.core.data.v2.generics;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.roda.core.data.common.SecureString;
import org.roda.core.data.v2.user.User;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class LoginRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = -2706255856347304025L;

  private String username;
  private SecureString password;

  public LoginRequest(String username, SecureString password) {
    this.username = username;
    this.password = password;
  }

  public LoginRequest() {
    this.username = null;
    this.password = new SecureString();
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public SecureString getPassword() {
    return password;
  }

  public void setPassword(SecureString password) {
    this.password = password;
  }
}
