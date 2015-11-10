/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.common;

/**
 * Thrown to indicate that an error occurred in the Editor service.
 * 
 * @author Rui Castro
 * 
 */
public class EditorException extends RODAServiceException {
  private static final long serialVersionUID = -7078545640376891807L;

  /**
   * Constructs a new {@link EditorException}.
   */
  public EditorException() {
  }

  /**
   * Constructs a new {@link EditorException} with the given error message.
   * 
   * @param message
   *          the error message.
   */
  public EditorException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link EditorException} with the given cause exception.
   * 
   * @param cause
   *          the cause exception.
   */
  public EditorException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link EditorException} with the given error message and
   * cause exception.
   * 
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public EditorException(String message, Throwable cause) {
    super(message, cause);
  }

}
