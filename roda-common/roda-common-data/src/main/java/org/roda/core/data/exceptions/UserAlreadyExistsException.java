/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

/**
 * Thrown to indicate that an User with the same name already exists when a new
 * one is trying to be created.
 * 
 * @author Rui Castro
 */
public class UserAlreadyExistsException extends AlreadyExistsException {

  private static final long serialVersionUID = 6493339963861919270L;

  /**
   * Constructs a new UserAlreadyExistsException.
   */
  public UserAlreadyExistsException() {
    // do nothing
  }

  /**
   * Constructs a new UserAlreadyExistsException with the given error message.
   * 
   * @param message
   *          the error message
   */
  public UserAlreadyExistsException(String message) {
    super(message);
  }

  /**
   * Constructs a new UserAlreadyExistsException with the given cause exception.
   * 
   * @param cause
   *          the cause exception
   */
  public UserAlreadyExistsException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new UserAlreadyExistsException with the given error message
   * and cause exception.
   * 
   * @param message
   *          the error message
   * @param cause
   *          the cause exception
   */
  public UserAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }

}
