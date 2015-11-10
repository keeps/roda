/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.common;

/**
 * Thrown to indicate that some error occurred in the Statistics service.
 * 
 * @author Rui Castro
 */
public class StatisticsException extends RODAException {
  private static final long serialVersionUID = -3426878040855987422L;

  /**
   * Constructs a new {@link StatisticsException}.
   */
  public StatisticsException() {
  }

  /**
   * Constructs a new {@link StatisticsException} with the given error message.
   * 
   * @param message
   *          the error message.
   */
  public StatisticsException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link StatisticsException} with the given cause
   * exception.
   * 
   * @param cause
   *          the cause exception.
   */
  public StatisticsException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link StatisticsException} with the given error message
   * and cause exception.
   * 
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public StatisticsException(String message, Throwable cause) {
    super(message, cause);
  }

}
