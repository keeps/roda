package org.roda.legacy.exception;

/**
 * Thrown to indicate that a certain specified batch state is invalid.
 * 
 * @author Rui Castro
 * 
 */
public class InvalidBatchStateException extends RODAServiceException {

	private static final long serialVersionUID = 3322259003511455030L;

	/**
	 * Constructs a new InvalidBatchStateException.
	 */
	public InvalidBatchStateException() {
	}

	/**
	 * Constructs a new InvalidBatchStateException with the given error message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public InvalidBatchStateException(String message) {
		super(message);
	}

	/**
	 * Constructs a new InvalidBatchStateException with the given cause
	 * exception.
	 * 
	 * @param cause
	 *            the cause exception.
	 */
	public InvalidBatchStateException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new InvalidBatchStateException with the given error message
	 * and cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public InvalidBatchStateException(String message, Throwable cause) {
		super(message, cause);
	}

}
