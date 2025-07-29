/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.synchronization.central;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author António Lindo <alindo@keep.pt>
 */

public class CreateLocalInstanceRequest implements Serializable {
  @Serial
  private static final long serialVersionUID = -2706255856347304025L;

  private String id;
  private String accessKey;
  private String centralInstanceURL;

  public CreateLocalInstanceRequest() {
    // empty constructor
  }

  public CreateLocalInstanceRequest(String id, String accessKey, String centralInstanceURL) {
    this.id = id;
    this.accessKey = accessKey;
    this.centralInstanceURL = centralInstanceURL;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessToken(String accessToken) {
    this.accessKey = accessToken;
  }

  public String getCentralInstanceURL() {
    return centralInstanceURL;
  }

  public void setCentralInstanceURL(String centralInstanceURL) {
    this.centralInstanceURL = centralInstanceURL;
  }
}
