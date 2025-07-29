/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.user.requests;

import org.roda.core.data.common.SecureString;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ResetPasswordRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = 1008183282342125312L;

  private String token;
  private SecureString newPassword;

  public ResetPasswordRequest() {
    // empty constructor
  }

  public ResetPasswordRequest(String token, SecureString newPassword) {
    this.token = token;
    this.newPassword = newPassword;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public SecureString getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(SecureString newPassword) {
    this.newPassword = newPassword;
  }
}
