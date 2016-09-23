/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

/**
 * Thrown to indicate the the email confirmation token or password change token
 * is invalid. A token can be invalid because is not the expected token or
 * because it already expired.
 * 
 * @author Rui Castro
 */
public class InvalidTokenException extends RODAException {
  private static final long serialVersionUID = -182450030953675819L;

  /**
   * Constructs a new {@link InvalidTokenException}.
   */
  public InvalidTokenException() {
  }

  /**
   * Constructs a new {@link InvalidTokenException} with the given error
   * message.
   * 
   * @param message
   *          the error message.
   */
  public InvalidTokenException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link InvalidTokenException} with the given cause
   * exception.
   * 
   * @param cause
   *          the cause exception.
   */
  public InvalidTokenException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link InvalidTokenException} with the given error message
   * and cause exception.
   * 
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public InvalidTokenException(String message, Throwable cause) {
    super(message, cause);
  }

}
