/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

import org.roda.core.data.v2.jobs.PluginParameter;

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
    // do nothing
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
