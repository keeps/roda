package pt.gov.dgarq.roda.core.common;

import java.io.Serializable;

/**
 * This class is the base for all RODA runtime exceptions.
 * <p>
 * This class is abstract and cannot be instantiated. Specific service runtime
 * exceptions must extend {@link RODARuntimeException} to provide more detailed
 * error information.
 * </p>
 * 
 * @author Rui Castro
 */
public abstract class RODARuntimeException extends RuntimeException implements Serializable {
  private static final long serialVersionUID = -2472221537766705700L;

  private String message = "";

  /**
   * Constructs a new RODAServiceException.
   */
  public RODARuntimeException() {
  }

  /**
   * Constructs a new RODAServiceException with the given message.
   * 
   * @param message
   */
  public RODARuntimeException(String message) {
    super(message);
    this.message = message;
  }

  /**
   * Constructs a new RODAServiceException with the given cause Exception.
   * 
   * @param cause
   */
  public RODARuntimeException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new RODAServiceException with the given message and cause
   * Exception.
   * 
   * @param message
   * @param cause
   */
  public RODARuntimeException(String message, Throwable cause) {
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
