package org.roda.wui.api.v2.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class GenericOkResponse {
  private final String timestamp;
  private final String message;

  public GenericOkResponse(String message) {
    this.message = message;
    this.timestamp = Instant.now().truncatedTo(ChronoUnit.MILLIS).toString();
  }

  public String getTimestamp() {
    return timestamp;
  }

  public String getMessage() {
    return message;
  }
}
