/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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
