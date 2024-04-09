package org.roda.wui.api.v2.exceptions;

import java.io.Serial;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class RESTException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 8163053720688814940L;
  private Throwable cause;

  private RESTException() {
    // hide the constructor
  }

  public RESTException(Throwable cause) {
    super("Remote exception" + getCauseMessage(cause));
    this.cause = cause;
  }

  private static String getCauseMessage(Throwable e) {
    StringBuilder message = new StringBuilder();
    Throwable cause = e;

    while (cause != null) {
      message.append(" caused by ").append(cause.getClass().getSimpleName()).append(": ");
      if (cause.getMessage() != null) {
        message.append(cause.getMessage());
      }
      cause = cause.getCause();
    }
    return message.toString();
  }

  @Override
  public synchronized Throwable getCause() {
    return cause;
  }
}
