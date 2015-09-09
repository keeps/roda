package pt.gov.dgarq.roda.core.common;

/**
 * Thrown when something wrong happens inside a RODA service and the request
 * could not be complete.
 * <p>
 * This class is abstract and cannot be instantiated. Specific service runtime
 * exceptions must extend {@link RODAServiceRuntimeException} to provide more
 * detailed error information.
 * </p>
 * 
 * @author Rui Castro
 */
public abstract class RODAServiceRuntimeException extends RODARuntimeException {
  private static final long serialVersionUID = 3043032404754693116L;

  /**
   * Constructs a new RODAServiceException.
   */
  public RODAServiceRuntimeException() {
  }

  /**
   * Constructs a new RODAServiceException with the given message.
   * 
   * @param message
   */
  public RODAServiceRuntimeException(String message) {
    super(message);
  }

  /**
   * Constructs a new RODAServiceException with the given cause Exception.
   * 
   * @param cause
   */
  public RODAServiceRuntimeException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new RODAServiceException with the given message and cause
   * Exception.
   * 
   * @param message
   * @param cause
   */
  public RODAServiceRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

}
