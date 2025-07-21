/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

import java.io.Serial;

public class NotificationException extends RODAException {

  @Serial
  private static final long serialVersionUID = -1007761117626340269L;

  public NotificationException() {
    super();
  }

  public NotificationException(String message) {
    super(message);
  }

  public NotificationException(String message, Throwable cause) {
    super(message, cause);
  }

  public NotificationException(Throwable cause) {
    super(cause);
  }

}
