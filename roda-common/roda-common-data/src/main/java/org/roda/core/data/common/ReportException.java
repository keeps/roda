/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.common;

/**
 * Thrown to indicate that some error related with the Report service has
 * occurred.
 * 
 * @author Rui Castro
 */
public class ReportException extends RODAServiceException {
  private static final long serialVersionUID = -2633339190058691398L;

  /**
   * Constructs a new {@link ReportException}.
   */
  public ReportException() {
  }

  /**
   * Constructs a new {@link ReportException} with the given error message.
   * 
   * @param message
   *          the error message.
   */
  public ReportException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link ReportException} with the cause exception.
   * 
   * @param cause
   *          the cause exception.
   */
  public ReportException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link ReportException} with an error message and a cause
   * exception.
   * 
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public ReportException(String message, Throwable cause) {
    super(message, cause);
  }

}
