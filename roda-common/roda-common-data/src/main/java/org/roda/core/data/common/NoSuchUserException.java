/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.common;

/**
 * Thrown to indicate that the User doesn't exist.
 * 
 * @author Rui Castro
 */
public class NoSuchUserException extends RODAServiceException {

  private static final long serialVersionUID = -4266649804839237972L;

  /**
   * Constructs a new NoSuchUserException.
   */
  public NoSuchUserException() {
  }

  /**
   * Constructs a new NoSuchUserException with the given error message.
   * 
   * @param message
   *          the error message
   */
  public NoSuchUserException(String message) {
    super(message);
  }

  /**
   * Constructs a new NoSuchUserException with the given cause exception.
   * 
   * @param cause
   *          the cause exception
   */
  public NoSuchUserException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new NoSuchUserException with the given error message and cause
   * exception.
   * 
   * @param message
   *          the error message
   * @param cause
   *          the cause exception
   */
  public NoSuchUserException(String message, Throwable cause) {
    super(message, cause);
  }

}
