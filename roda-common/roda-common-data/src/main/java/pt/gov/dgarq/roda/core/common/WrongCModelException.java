package pt.gov.dgarq.roda.core.common;

/**
 * @author Rui Castro
 */
public class WrongCModelException extends RODAServiceRuntimeException {
  private static final long serialVersionUID = 5195466136991964793L;

  /**
   * Constructs a new WrongCModelException
   */
  public WrongCModelException() {
  }

  /**
   * Constructs a new WrongCModelException with the given error message
   * 
   * @param message
   *          the error message
   */
  public WrongCModelException(String message) {
    super(message);
  }

  /**
   * Constructs a new WrongCModelException with the given cause exception
   * 
   * @param cause
   */
  public WrongCModelException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new WrongCModelException with the given error message and
   * cause exception
   * 
   * @param message
   * @param cause
   */
  public WrongCModelException(String message, Throwable cause) {
    super(message, cause);
  }

}
