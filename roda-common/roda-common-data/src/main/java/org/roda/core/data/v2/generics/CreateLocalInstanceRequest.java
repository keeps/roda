package org.roda.core.data.v2.generics;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class CreateLocalInstanceRequest implements Serializable {
  @Serial
  private static final long serialVersionUID = -2706255856347304025L;

  private String id;
  private String accessKey;
  private String centralInstanceURL;

  public CreateLocalInstanceRequest() {

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
