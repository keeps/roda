/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.common;

/**
 * Thrown to indicate that the access to some service or resource is not
 * authorised.
 * 
 * @author Rui Castro
 */
public class AuthorizationDeniedException extends RODAException {
  private static final long serialVersionUID = -8405660853143660038L;

  /**
   * Constructs a new {@link AuthorizationDeniedException}.
   */
  public AuthorizationDeniedException() {
  }

  /**
   * Constructs a new {@link AuthorizationDeniedException} with the given error
   * message.
   * 
   * @param message
   *          the error message.
   */
  public AuthorizationDeniedException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link AuthorizationDeniedException} with the given cause
   * exception.
   * 
   * @param cause
   *          the cause exception.
   */
  public AuthorizationDeniedException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link AuthorizationDeniedException} with the given error
   * message and cause exception.
   * 
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public AuthorizationDeniedException(String message, Throwable cause) {
    super(message, cause);
  }

}
