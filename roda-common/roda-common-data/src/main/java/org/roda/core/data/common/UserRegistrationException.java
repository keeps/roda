/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.common;

/**
 * Thrown to indicate that an error occurred in the UserRegistration service.
 * 
 * @author Rui Castro
 */
public class UserRegistrationException extends RODAServiceException {
  private static final long serialVersionUID = -305895377694019394L;

  /**
   * Constructs a new {@link UserRegistrationException}.
   */
  public UserRegistrationException() {
  }

  /**
   * Constructs a new {@link UserRegistrationException} with the given error
   * message.
   * 
   * @param message
   *          the error message.
   */
  public UserRegistrationException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link UserRegistrationException} with the given cause
   * exception.
   * 
   * @param cause
   *          the cause exception.
   */
  public UserRegistrationException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link UserRegistrationException} with the given error
   * message and cause exception.
   * 
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public UserRegistrationException(String message, Throwable cause) {
    super(message, cause);
  }

}
