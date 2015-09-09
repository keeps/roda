package pt.gov.dgarq.roda.core.common;

/**
 * Thrown to indicate that an error occurred in the UserEditor service.
 * 
 * @author Rui Castro
 */
public class UserEditorException extends RODAServiceException {
  private static final long serialVersionUID = -4575991211547655806L;

  /**
   * Constructs a new {@link UserEditorException}.
   */
  public UserEditorException() {
  }

  /**
   * Constructs a new {@link UserEditorException} with the given error message.
   * 
   * @param message
   *          the error message.
   */
  public UserEditorException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link UserEditorException} with the given cause
   * exception.
   * 
   * @param cause
   *          the cause exception.
   */
  public UserEditorException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link UserEditorException} with the given error message
   * and cause exception.
   * 
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public UserEditorException(String message, Throwable cause) {
    super(message, cause);
  }

}
