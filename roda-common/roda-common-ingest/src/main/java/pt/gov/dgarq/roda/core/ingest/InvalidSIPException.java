package pt.gov.dgarq.roda.core.ingest;

import pt.gov.dgarq.roda.core.common.IngestException;
import pt.gov.dgarq.roda.core.data.SIPState;

/**
 * Thrown to indicate that a {@link SIPState} is invalid.
 * 
 * @author Rui Castro
 * 
 */
public class InvalidSIPException extends IngestException {
	private static final long serialVersionUID = 394161455104958443L;

	/**
	 * Constructs a new {@link InvalidSIPException}.
	 */
	public InvalidSIPException() {
	}

	/**
	 * Constructs a new {@link InvalidSIPException} with the given error
	 * message.
	 * 
	 * @param message
	 *            the error message
	 */
	public InvalidSIPException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link InvalidSIPException} with the given error message
	 * and cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public InvalidSIPException(String message, Throwable cause) {
		super(message, cause);
	}

}
