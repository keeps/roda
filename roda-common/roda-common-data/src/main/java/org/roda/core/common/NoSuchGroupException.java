/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

/**
 * Thrown to indicate that the Group doesn't exist.
 * 
 * @author Rui Castro
 */
public class NoSuchGroupException extends RODAServiceException {

  private static final long serialVersionUID = -3435084231182716467L;

  /**
   * Constructs a new NoSuchGroupException.
   */
  public NoSuchGroupException() {
  }

  /**
   * Constructs a new NoSuchGroupException with the given error message.
   * 
   * @param message
   *          the error message
   */
  public NoSuchGroupException(String message) {
    super(message);
  }

  /**
   * Constructs a new NoSuchGroupException with the given cause exception.
   * 
   * @param cause
   *          the cause exception
   */
  public NoSuchGroupException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new NoSuchGroupException with the given error message and
   * cause exception.
   * 
   * @param message
   *          the error message
   * @param cause
   *          the cause exception
   */
  public NoSuchGroupException(String message, Throwable cause) {
    super(message, cause);
  }

}
