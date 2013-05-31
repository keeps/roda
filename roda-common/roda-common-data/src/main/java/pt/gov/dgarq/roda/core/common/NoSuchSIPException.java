package pt.gov.dgarq.roda.core.common;

import pt.gov.dgarq.roda.core.data.SIPState;

/**
 * Thrown to indicate that a specified {@link SIPState} does not exist.
 * 
 * @author Rui Castro
 */
public class NoSuchSIPException extends IngestException {
	private static final long serialVersionUID = 53365865563308596L;

	/**
	 * Constructs a new {@link NoSuchSIPException}.
	 */
	public NoSuchSIPException() {
	}

	/**
	 * Constructs a new {@link NoSuchSIPException} with the given error message.
	 * 
	 * @param message
	 *            the error message
	 */
	public NoSuchSIPException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link NoSuchSIPException} with the given error message
	 * and cause exception.
	 * 
	 * @param message
	 *            the error message
	 * @param cause
	 *            the cause exception
	 */
	public NoSuchSIPException(String message, Throwable cause) {
		super(message, cause);
	}

}
