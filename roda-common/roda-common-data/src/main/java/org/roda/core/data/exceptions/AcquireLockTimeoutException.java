/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

public class AcquireLockTimeoutException extends LockingException {
  private static final long serialVersionUID = -3183093939393405726L;

  public AcquireLockTimeoutException() {
    super();
  }

  public AcquireLockTimeoutException(String msg) {
    super(msg);
  }

  public AcquireLockTimeoutException(String message, Throwable cause) {
    super(message, cause);
  }

  public AcquireLockTimeoutException(Throwable cause) {
    super(cause);
  }
}