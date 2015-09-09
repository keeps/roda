package pt.gov.dgarq.roda.core.common;

/**
 * Thrown to indicate that the execution of some operation is not permited.
 * 
 * @author Rui Castro
 */
public class IllegalOperationException extends RODAServiceException {
  private static final long serialVersionUID = -8118340939329992654L;

  /**
   * Constructs a new IllegalOperationException.
   */
  public IllegalOperationException() {
  }

  /**
   * Constructs a new IllegalOperationException with the given message.
   * 
   * @param message
   */
  public IllegalOperationException(String message) {
    super(message);
  }

  /**
   * Constructs a new IllegalOperationException with the given cause Exception.
   * 
   * @param cause
   */
  public IllegalOperationException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new IllegalOperationException with the given message and cause
   * Exception.
   * 
   * @param message
   * @param cause
   */
  public IllegalOperationException(String message, Throwable cause) {
    super(message, cause);
  }

}
