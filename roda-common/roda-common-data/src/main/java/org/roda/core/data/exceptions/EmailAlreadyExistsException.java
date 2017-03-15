/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

/**
 * Thrown to indicate that the specified email was is already used.
 * 
 * @author Rui Castro
 */
public class EmailAlreadyExistsException extends AlreadyExistsException {
  private static final long serialVersionUID = 3392813159441368655L;

  /**
   * Constructs a new {@link EmailAlreadyExistsException}.
   */
  public EmailAlreadyExistsException() {
    // do nothing
  }

  /**
   * Construct a new {@link EmailAlreadyExistsException} with the error message.
   * 
   * @param message
   *          the error message.
   */
  public EmailAlreadyExistsException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link EmailAlreadyExistsException} with the given cause
   * exception.
   * 
   * @param cause
   *          the cause exception.
   */
  public EmailAlreadyExistsException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link EmailAlreadyExistsException} with the given error
   * message and cause exception.
   * 
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public EmailAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }

}
