package org.roda.core.data.v2.log;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditLogRequestHeaders implements Serializable {

  @Serial
  private static final long serialVersionUID = -166139273669982053L;

  private String uuid;
  private String reason;
  private String requestType;

  public AuditLogRequestHeaders() {
    // empty constructor
  }

  public AuditLogRequestHeaders(String uuid, String reason, String requestType) {
    this.uuid = uuid;
    this.reason = reason;
    this.requestType = requestType;
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

  public String getRequestType() {
    return requestType;
  }

  public void setRequestType(String requestType) {
    this.requestType = requestType;
  }
}
