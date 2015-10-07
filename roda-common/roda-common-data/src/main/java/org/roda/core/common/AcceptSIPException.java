package org.roda.core.common;

/**
 * Thrown to indicate that an error occurred in the AcceptSIP service.
 * 
 * @author Rui Castro
 * 
 */
public class AcceptSIPException extends RODAServiceException {
  private static final long serialVersionUID = 1554352078843420955L;

  /**
   * Constructs a new {@link AcceptSIPException}.
   */
  public AcceptSIPException() {
  }

  /**
   * Constructs a new {@link AcceptSIPException} with the given error message.
   * 
   * @param message
   *          the error message.
   */
  public AcceptSIPException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link AcceptSIPException} with the given cause exception.
   * 
   * @param cause
   *          the cause exception.
   */
  public AcceptSIPException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link AcceptSIPException} with the given error message
   * and cause exception.
   * 
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public AcceptSIPException(String message, Throwable cause) {
    super(message, cause);
  }

}
