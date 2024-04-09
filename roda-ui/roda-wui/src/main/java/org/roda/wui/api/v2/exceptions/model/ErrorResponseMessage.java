package org.roda.wui.api.v2.exceptions.model;

import java.time.LocalDateTime;

public class ErrorResponseMessage {

  private int status;
  private String errorId;
  private String message;
  private LocalDateTime timestamp;
  private String instance;

  public ErrorResponseMessage(int status, String errorId, String message, LocalDateTime timestamp, String instance) {
    this.status = status;
    this.errorId = errorId;
    this.message = message;
    this.timestamp = timestamp;
    this.instance = instance;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public void setErrorId(String errorId) {
    this.errorId = errorId;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public void setInstance(String instance) {
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

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public String getInstance() {
    return instance;
  }
}
