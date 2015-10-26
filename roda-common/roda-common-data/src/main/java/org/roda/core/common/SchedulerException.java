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
public class SchedulerException extends RODAServiceException {
  private static final long serialVersionUID = 4495120033204181505L;

  /**
   * Constructs a new {@link SchedulerException}.
   */
  public SchedulerException() {
  }

  /**
   * Constructs a new {@link SchedulerException} with the given error message.
   * 
   * @param message
   *          the error message.
   */
  public SchedulerException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link SchedulerException} with the cause exception.
   * 
   * @param cause
   *          the cause exception.
   */
  public SchedulerException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link SchedulerException} with an error message and a
   * cause exception.
   * 
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public SchedulerException(String message, Throwable cause) {
    super(message, cause);
  }

}
