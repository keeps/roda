/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

/**
 * Thrown to indicate that was not possible to authenticate {@link User} because
 * it is not active.
 * 
 * @author Rui Castro <rui.castro@gmail.com>
 */
public class UserInactiveException extends AuthenticationDeniedException {
  private static final long serialVersionUID = 4619089972230221210L;

  /**
   * Constructs a new {@link UserInactiveException}.
   */
  public UserInactiveException() {
  }

  /**
   * Constructs a new {@link UserInactiveException} with the given error
   * message.
   *
   * @param message
   *          the error message.
   */
  public UserInactiveException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link UserInactiveException} with the given cause
   * exception.
   *
   * @param cause
   *          the cause exception.
   */
  public UserInactiveException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link UserInactiveException} with the given error message
   * and cause exception.
   *
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public UserInactiveException(String message, Throwable cause) {
    super(message, cause);
  }

}
