package pt.gov.dgarq.roda.core.common;


/**
 * Thrown to indicate that the login failed because of invalid credentials.
 * 
 * @author Rui Castro
 */
public class LoginException extends RODAServiceException {

	private static final long serialVersionUID = -207396196678404818L;

	/**
	 * Constructs a new LoginException.
	 */
	public LoginException() {
	}

	/**
	 * Constructs a new LoginException with the given error message.
	 * 
	 * @param message
	 *            the error message
	 */
	public LoginException(String message) {
		super(message);
	}

	/**
	 * Constructs a new LoginException with the given cause exception.
	 * 
	 * @param cause
	 *            the cause exception
	 */
	public LoginException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new LoginException with the given error message and cause
	 * exception.
	 * 
	 * @param message
	 *            the error message
	 * @param cause
	 *            the cause exception
	 */
	public LoginException(String message, Throwable cause) {
		super(message, cause);
	}

}
