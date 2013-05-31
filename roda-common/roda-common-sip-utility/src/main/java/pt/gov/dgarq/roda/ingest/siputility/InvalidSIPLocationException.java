package pt.gov.dgarq.roda.ingest.siputility;

/**
 * Thrown to indicate that a given location of a SIP is not valid.
 * 
 * @author Rui Castro
 */
public class InvalidSIPLocationException extends SIPException {
	private static final long serialVersionUID = 269946002467463052L;

	/**
	 * Constructs a new {@link InvalidSIPLocationException}.
	 */
	public InvalidSIPLocationException() {
	}

	/**
	 * Constructs a new {@link InvalidSIPLocationException} with the given error
	 * message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public InvalidSIPLocationException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link InvalidSIPLocationException} with the given error
	 * message and cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public InvalidSIPLocationException(String message, Throwable cause) {
		super(message, cause);
	}

}
