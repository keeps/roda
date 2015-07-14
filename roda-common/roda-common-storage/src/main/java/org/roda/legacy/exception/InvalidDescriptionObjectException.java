package org.roda.legacy.exception;

/**
 * Thrown to indicate that a certain {@link DescriptionObject} is invalid. A
 * {@link DescriptionObject} is invalid when the corresponding EAD-C XML
 * document is invalid.
 * 
 * @author Rui Castro
 */
public class InvalidDescriptionObjectException extends RODAException {
	private static final long serialVersionUID = -9064661097297752099L;

	/**
	 * Constructs a new {@link InvalidDescriptionObjectException}.
	 */
	public InvalidDescriptionObjectException() {
	}

	/**
	 * Constructs a new {@link InvalidDescriptionObjectException} with the given
	 * error message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public InvalidDescriptionObjectException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link InvalidDescriptionObjectException} with the given
	 * cause exception.
	 * 
	 * @param cause
	 *            the cause exception.
	 */
	public InvalidDescriptionObjectException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new {@link InvalidDescriptionObjectException} with the given
	 * message and cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public InvalidDescriptionObjectException(String message, Throwable cause) {
		super(message, cause);
	}

}
