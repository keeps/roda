package org.roda.core.common;

import org.roda.core.data.PluginParameter;

/**
 * Thrown to indicate that a {@link PluginParameter} is wrong.
 * 
 * @author Rui Castro
 */
public class InvalidParameterException extends RODAException {
  private static final long serialVersionUID = 4040123614898012034L;

  /**
   * Constructs a new {@link InvalidParameterException}.
   */
  public InvalidParameterException() {
  }

  /**
   * Constructs a new {@link InvalidParameterException} with the given error
   * message.
   * 
   * @param message
   *          the error message.
   */
  public InvalidParameterException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link InvalidParameterException} with the given cause
   * exception.
   * 
   * @param cause
   *          the cause exception.
   */
  public InvalidParameterException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link InvalidParameterException} with the given error
   * message and cause exception.
   * 
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public InvalidParameterException(String message, Throwable cause) {
    super(message, cause);
  }

}
