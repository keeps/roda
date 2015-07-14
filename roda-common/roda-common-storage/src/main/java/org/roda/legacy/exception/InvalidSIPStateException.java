package org.roda.legacy.exception;

/**
 * Thrown to indicate that a certain specified SIP state is invalid.
 * 
 * @author Rui Castro
 */
public class InvalidSIPStateException extends RODAServiceException {

	private static final long serialVersionUID = -531671002931374055L;

	/**
	 * Constructs a new InvalidSIPStateException.
	 */
	public InvalidSIPStateException() {
	}

	/**
	 * Constructs a new InvalidSIPStateException with the given error message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public InvalidSIPStateException(String message) {
		super(message);
	}

	/**
	 * Constructs a new InvalidSIPStateException with the given cause exception.
	 * 
	 * @param cause
	 *            the cause exception.
	 */
	public InvalidSIPStateException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new InvalidSIPStateException with the given error message
	 * and cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public InvalidSIPStateException(String message, Throwable cause) {
		super(message, cause);
	}

}
