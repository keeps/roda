/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.common;

import java.io.Serializable;

/**
 * This class is the base for all RODA exceptions.
 * <p>
 * This class is abstract and cannot be instantiated. Specific service
 * exceptions must extend {@link RODAException} to provide more detailed error
 * information.
 * </p>
 * 
 * @deprecated use {@link org.roda.core.data.exceptions.RODAException} instead
 * 
 * @author Rui Castro
 */
@Deprecated
public abstract class RODAException extends Exception implements Serializable {
  private static final long serialVersionUID = -6932662689717864495L;

  private String message = "";

  /**
   * Constructs a new RODAServiceException.
   */
  public RODAException() {
  }

  /**
   * Constructs a new RODAServiceException with the given message.
   * 
   * @param message
   */
  public RODAException(String message) {
    super(message);
    this.message = message;
  }

  /**
   * Constructs a new RODAServiceException with the given cause Exception.
   * 
   * @param cause
   */
  public RODAException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new RODAServiceException with the given message and cause
   * Exception.
   * 
   * @param message
   * @param cause
   */
  public RODAException(String message, Throwable cause) {
    super(message, cause);
    this.message = message;
  }

  /**
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  /**
   * @param message
   *          the message to set
   */
  public void setMessage(String message) {
    this.message = message;
  }
}
