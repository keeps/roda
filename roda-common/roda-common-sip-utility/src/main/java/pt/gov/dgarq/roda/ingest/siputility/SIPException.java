package pt.gov.dgarq.roda.ingest.siputility;

import pt.gov.dgarq.roda.core.common.RODAException;

/**
 * Thrown to indicate that some error related with a SIP has occurred.
 * 
 * @author Rui Castro
 */
public class SIPException extends RODAException {
	private static final long serialVersionUID = 4008879831540349756L;

	/**
	 * Constructs a new {@link SIPException}.
	 */
	public SIPException() {
	}

	/**
	 * Constructs a new {@link SIPException} with the given error message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public SIPException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link SIPException} with the given cause exception.
	 * 
	 * @param cause
	 *            the cause exception.
	 */
	public SIPException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new {@link SIPException} with the given error message and
	 * cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public SIPException(String message, Throwable cause) {
		super(message, cause);
	}

}
