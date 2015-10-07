package org.roda.core.common;

/**
 * @author Rui Castro
 */
public class NoSuchRODAObjectException extends RODAException {
  private static final long serialVersionUID = -3842855181786584436L;

  /**
   * Constructs a new NoSuchRODAObjectException.
   */
  public NoSuchRODAObjectException() {
  }

  /**
   * Constructs a new NoSuchRODAObjectException with the given error message.
   * 
   * @param message
   */
  public NoSuchRODAObjectException(String message) {
    super(message);
  }

  /**
   * Constructs a new NoSuchRODAObjectException with the given cause.
   * 
   * @param cause
   */
  public NoSuchRODAObjectException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new NoSuchRODAObjectException with the given error message and
   * cause.
   * 
   * @param message
   * @param cause
   */
  public NoSuchRODAObjectException(String message, Throwable cause) {
    super(message, cause);
  }

}
