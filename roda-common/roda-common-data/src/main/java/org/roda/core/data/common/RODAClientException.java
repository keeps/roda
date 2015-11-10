/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.common;

/**
 * Thrown to indicate that some problem related to RODAClient has happened.
 * 
 * @author Rui Castro
 */
public class RODAClientException extends RODAException {
  private static final long serialVersionUID = 7147751142597978018L;

  /**
   * Constructs a new {@link RODAClientException}.
   */
  public RODAClientException() {
  }

  /**
   * Constructs a new {@link RODAClientException} with the given error message.
   * 
   * @param message
   *          the error message.
   */
  public RODAClientException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link RODAClientException} with the given cause
   * exception.
   * 
   * @param cause
   *          the cause exception.
   */
  public RODAClientException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link RODAClientException} with the given error message
   * and cause exception.
   * 
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public RODAClientException(String message, Throwable cause) {
    super(message, cause);
  }

}
