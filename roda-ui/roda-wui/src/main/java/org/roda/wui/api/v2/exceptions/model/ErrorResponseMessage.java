package org.roda.wui.api.v2.exceptions.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class ErrorResponseMessage {

  private int status;
  private String errorId;
  private String message;
  private String details;
  private Instant timestamp;
  private String instance;

  public ErrorResponseMessage(int status, String errorId, String message, String details, String instance) {
    this.status = status;
    this.errorId = errorId;
    this.message = message;
    this.details = details;
    this.timestamp = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    this.instance = instance;
  }

  public int getStatus() {
    return status;
  }

  public String getErrorId() {
    return errorId;
  }

  public String getMessage() {
    return message;
  }

  public String getDetails() {
    return details;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public String getInstance() {
    return instance;
  }
}
