/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

import java.io.Serial;

/**
 * Thrown to indicate that was not possible to authenticate {@link User} because
 * it's email address is not verified.
 *
 * @author Rui Castro <rui.castro@gmail.com>
 */
public class EmailUnverifiedException extends AuthenticationDeniedException {
  @Serial
  private static final long serialVersionUID = 4619089972230221210L;

  /**
   * Constructs a new {@link EmailUnverifiedException}.
   */
  public EmailUnverifiedException() {
  }

  /**
   * Constructs a new {@link EmailUnverifiedException} with the given error
   * message.
   *
   * @param message
   *          the error message.
   */
  public EmailUnverifiedException(final String message) {
    super(message);
  }

  /**
   * Constructs a new {@link EmailUnverifiedException} with the given cause
   * exception.
   *
   * @param cause
   *          the cause exception.
   */
  public EmailUnverifiedException(final Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link EmailUnverifiedException} with the given error
   * message and cause exception.
   *
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public EmailUnverifiedException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
