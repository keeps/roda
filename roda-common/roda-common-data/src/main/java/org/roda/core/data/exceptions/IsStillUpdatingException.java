/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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
