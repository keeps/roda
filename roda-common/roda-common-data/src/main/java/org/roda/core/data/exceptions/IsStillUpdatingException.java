package org.roda.core.data.exceptions;

public class IsStillUpdatingException extends RODAException {

  private static final long serialVersionUID = 7420264596411093449L;

  public IsStillUpdatingException() {
  }

  public IsStillUpdatingException(String message) {
    super(message);
  }

  public IsStillUpdatingException(String message, Throwable cause) {
    super(message, cause);
  }

  public IsStillUpdatingException(Throwable cause) {
    super(cause);
  }
}
