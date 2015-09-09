package pt.gov.dgarq.roda.core.common;

/**
 * Thrown to indicate that an error occurred in the UserManagement service.
 * 
 * @author Rui Castro
 */
public class UserManagementException extends RODAServiceException {
  private static final long serialVersionUID = 6987114795542379169L;

  /**
   * Constructs a new {@link UserManagementException}.
   */
  public UserManagementException() {
  }

  /**
   * Constructs a new {@link UserManagementException} with the given error
   * message.
   * 
   * @param message
   *          the error message.
   */
  public UserManagementException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link UserManagementException} with the given cause
   * exception.
   * 
   * @param cause
   *          the cause exception.
   */
  public UserManagementException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link UserManagementException} with the given error
   * message and cause exception.
   * 
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public UserManagementException(String message, Throwable cause) {
    super(message, cause);
  }

}
