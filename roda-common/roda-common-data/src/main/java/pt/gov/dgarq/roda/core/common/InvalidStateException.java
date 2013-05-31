package pt.gov.dgarq.roda.core.common;

/**
 * Thrown to indicate that an invalid state as been specified.
 * 
 * @author Rui Castro
 */
public class InvalidStateException extends RODARuntimeException {
	private static final long serialVersionUID = 8580022001626029304L;

	/**
	 * Constructs a new {@link InvalidStateException}.
	 */
	public InvalidStateException() {
	}

	/**
	 * Constructs a new {@link InvalidStateException} with the specified error
	 * message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public InvalidStateException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link InvalidStateException} with the specified cause
	 * exception.
	 * 
	 * @param cause
	 *            the cause exception.
	 */
	public InvalidStateException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new {@link InvalidStateException} with the specified error
	 * message and cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public InvalidStateException(String message, Throwable cause) {
		super(message, cause);
	}

}
