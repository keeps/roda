package org.roda.wui.common.model;

public class RequestHeaders {
  private String uuid;
  private String reason;
  private String type;

  public RequestHeaders() {
    // empty constructor
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
