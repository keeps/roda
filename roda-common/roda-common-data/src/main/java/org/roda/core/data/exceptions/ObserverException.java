/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

public class ObserverException extends RODAException {
  private static final long serialVersionUID = -1006839588088229886L;

  public ObserverException() {
    super();
  }

  public ObserverException(String message) {
    super(message);
  }

  public ObserverException(String message, Throwable cause) {
    super(message, cause);
  }

  public ObserverException(Throwable cause) {
    super(cause);
  }
}