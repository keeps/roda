package org.roda.core.common;

import org.roda.core.data.Task;

/**
 * Thrown to indicate that the a supplied {@link Task} state is invalid.
 * 
 * @author Rui Castro
 */
public class InvalidTaskStateException extends RODARuntimeException {
  private static final long serialVersionUID = 6173914540008411578L;

  /**
   * Constructs an empty {@link InvalidTaskStateException}.
   */
  public InvalidTaskStateException() {
  }

  /**
   * Constructs a {@link InvalidTaskStateException} with the specified error
   * message.
   * 
   * @param message
   *          the error message.
   */
  public InvalidTaskStateException(String message) {
    super(message);
  }

  /**
   * Constructs a {@link InvalidTaskStateException} with the specified cause
   * exception.
   * 
   * @param cause
   *          the cause exception.
   */
  public InvalidTaskStateException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a {@link InvalidTaskStateException} with the specified message
   * and cause exception.
   * 
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public InvalidTaskStateException(String message, Throwable cause) {
    super(message, cause);
  }

}
