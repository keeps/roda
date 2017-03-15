/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 */
public class AuthenticationDeniedException extends RODAException {
  private static final long serialVersionUID = -8405660853143660038L;

  /**
   * Constructs a new {@link AuthenticationDeniedException}.
   */
  public AuthenticationDeniedException() {
    // do nothing
  }

  /**
   * Constructs a new {@link AuthenticationDeniedException} with the given error
   * message.
   * 
   * @param message
   *          the error message.
   */
  public AuthenticationDeniedException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link AuthenticationDeniedException} with the given cause
   * exception.
   * 
   * @param cause
   *          the cause exception.
   */
  public AuthenticationDeniedException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link AuthenticationDeniedException} with the given error
   * message and cause exception.
   * 
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public AuthenticationDeniedException(String message, Throwable cause) {
    super(message, cause);
  }

}
