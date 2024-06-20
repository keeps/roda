package org.roda.wui.api.v2.exceptions.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.annotation.JsonInclude;

public class ErrorResponseMessage implements Serializable {

  @Serial
  private static final long serialVersionUID = -2206131216992713872L;

  private final int status;
  private final String errorId;
  private final String message;
  private final String details;
  private final Instant timestamp;
  private final String instance;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Object objectDetails;

  public ErrorResponseMessage(int status, String errorId, String message, String details, String instance) {
    this.status = status;
    this.errorId = errorId;
    this.message = message;
    this.details = details;
    this.timestamp = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    this.instance = instance;
  }

  public ErrorResponseMessage(int status, String errorId, String message, String details, String instance, Object objectDetails) {
    this.status = status;
    this.errorId = errorId;
    this.message = message;
    this.details = details;
    this.timestamp = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    this.instance = instance;
    this.objectDetails = objectDetails;
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

  public Object getObjectDetails() {
    return objectDetails;
  }
}
