package org.roda.core.common;

/**
 * Thrown to indicate that an User with the same name already exists when a new
 * one is trying to be created.
 * 
 * @author Rui Castro
 */
public class UserAlreadyExistsException extends UserManagementException {

  private static final long serialVersionUID = 6493339963861919270L;

  /**
   * Constructs a new UserAlreadyExistsException.
   */
  public UserAlreadyExistsException() {
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
