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

  private String uuid = null;
  private String reason = null;
  private String type = null;

  public AuditLogRequestHeaders() {
    // empty constructor
  }

  public AuditLogRequestHeaders(String uuid, String reason, String type) {
    this.uuid = uuid;
    this.reason = reason;
    this.type = type;
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
