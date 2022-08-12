/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

import org.roda.core.data.v2.user.User;

/**
 * Thrown to indicate that was not possible to authenticate {@link User} because
 * it is not active.
 *
 * @author Rui Castro <rui.castro@gmail.com>
 */
public class InactiveUserException extends AuthenticationDeniedException {
  private static final long serialVersionUID = -1893919532523481577L;

  /**
   * Constructs a new {@link InactiveUserException}.
   */
  public InactiveUserException() {
    // do nothing
  }

  /**
   * Constructs a new {@link InactiveUserException} with the given error message.
   *
   * @param message
   *          the error message.
   */
  public InactiveUserException(final String message) {
    super(message);
  }

  /**
   * Constructs a new {@link InactiveUserException} with the given cause
   * exception.
   *
   * @param cause
   *          the cause exception.
   */
  public InactiveUserException(final Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link InactiveUserException} with the given error message
   * and cause exception.
   *
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public InactiveUserException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
