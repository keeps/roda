/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

public class NotLockableAtTheTimeException extends LockingException {
  private static final long serialVersionUID = 7257377928175910597L;

  public NotLockableAtTheTimeException() {
    super();
  }

  public NotLockableAtTheTimeException(String msg) {
    super(msg);
  }

  public NotLockableAtTheTimeException(String message, Throwable cause) {
    super(message, cause);
  }

  public NotLockableAtTheTimeException(Throwable cause) {
    super(cause);
  }
}