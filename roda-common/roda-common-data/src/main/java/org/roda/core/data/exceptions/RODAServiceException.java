/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

/**
 * Thrown when something wrong happens inside a RODA service and the request
 * could not be complete.
 * 
 * @author Rui Castro
 */
public class RODAServiceException extends RODAException {
  private static final long serialVersionUID = 4068129466745626125L;

  /**
   * Constructs a new RODAServiceException.
   */
  public RODAServiceException() {
  }

  /**
   * Constructs a new RODAServiceException with the given message.
   * 
   * @param message
   */
  public RODAServiceException(String message) {
    super(message);
  }

  /**
   * Constructs a new RODAServiceException with the given cause Exception.
   * 
   * @param cause
   */
  public RODAServiceException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new RODAServiceException with the given message and cause
   * Exception.
   * 
   * @param message
   * @param cause
   */
  public RODAServiceException(String message, Throwable cause) {
    super(message, cause);
  }

}
