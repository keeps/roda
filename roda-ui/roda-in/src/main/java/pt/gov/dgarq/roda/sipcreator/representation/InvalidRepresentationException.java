package pt.gov.dgarq.roda.sipcreator.representation;

import pt.gov.dgarq.roda.ingest.siputility.SIPException;

/**
 * Thrown to indicate that some representation is invalid.
 * 
 * @author Rui Castro
 * @author Luis Faria
 */
public class InvalidRepresentationException extends SIPException {
	private static final long serialVersionUID = -1750721324739546524L;

	/**
	 * Construct a new {@link InvalidRepresentationException}.
	 */
	public InvalidRepresentationException() {
	}

	/**
	 * Construct a new {@link InvalidRepresentationException} with the given
	 * error message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public InvalidRepresentationException(String message) {
		super(message);
	}

	/**
	 * Construct a new {@link InvalidRepresentationException} with the given
	 * cause exception.
	 * 
	 * @param cause
	 *            the cause exception.
	 */
	public InvalidRepresentationException(Throwable cause) {
		super(cause);
	}

	/**
	 * Construct a new {@link InvalidRepresentationException} with the given
	 * error message and cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public InvalidRepresentationException(String message, Throwable cause) {
		super(message, cause);
	}

}
