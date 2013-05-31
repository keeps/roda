package pt.gov.dgarq.roda.ingest.siputility;

/**
 * Thrown to indicate that the a given SIP Mets is not valid.
 * 
 * @author Rui Castro
 */
public class InvalidSIPMetsException extends SIPException {
	private static final long serialVersionUID = -1927420925683941492L;

	/**
	 * Constructs a new {@link InvalidSIPMetsException}.
	 */
	public InvalidSIPMetsException() {
	}

	/**
	 * Constructs a new {@link InvalidSIPMetsException} with the given error
	 * message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public InvalidSIPMetsException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link InvalidSIPMetsException} with the given error
	 * message and cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public InvalidSIPMetsException(String message, Throwable cause) {
		super(message, cause);
	}

}
