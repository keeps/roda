/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.user.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.roda.core.data.common.SecureString;

import java.io.Serial;
import java.io.Serializable;

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
