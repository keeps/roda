/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

public class LockingException extends RODAException {
  private static final long serialVersionUID = 8700288419110883827L;

  public LockingException() {
    super();
  }

  public LockingException(String msg) {
    super(msg);
  }

  public LockingException(String message, Throwable cause) {
    super(message, cause);
  }

  public LockingException(Throwable cause) {
    super(cause);
  }
}