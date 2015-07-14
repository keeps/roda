package org.roda.legacy.exception;

/**
 * Thrown to indicate that an error occurred in the Search service.
 * 
 * @author Rui Castro
 */
public class SearchException extends RODAServiceException {
	private static final long serialVersionUID = 1296596416985054136L;

	/**
	 * Constructs a new {@link SearchException}.
	 */
	public SearchException() {
	}

	/**
	 * Constructs a new {@link SearchException} with the given error message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public SearchException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link SearchException} with the given cause exception.
	 * 
	 * @param cause
	 *            the cause exception.
	 */
	public SearchException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new {@link SearchException} with the given error message and
	 * cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public SearchException(String message, Throwable cause) {
		super(message, cause);
	}

}
