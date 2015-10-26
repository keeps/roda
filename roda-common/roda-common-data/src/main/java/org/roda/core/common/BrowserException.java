/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

/**
 * Thrown to indicate that an error occurred in the Browser.
 * 
 * @author Rui Castro
 */
public class BrowserException extends RODAServiceException {
  private static final long serialVersionUID = -2590419594550583083L;

  /**
   * Constructs a new {@link BrowserException}.
   */
  public BrowserException() {
  }

  /**
   * Constructs a new {@link BrowserException} with the given error message.
   * 
   * @param message
   *          the error message.
   */
  public BrowserException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link BrowserException} with the given cause exception.
   * 
   * @param cause
   *          the cause exception.
   */
  public BrowserException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link BrowserException} with the given error message and
   * cause exception.
   * 
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public BrowserException(String message, Throwable cause) {
    super(message, cause);
  }

}
