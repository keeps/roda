package pt.gov.dgarq.roda.core.ingest;

import pt.gov.dgarq.roda.core.common.IngestException;
import pt.gov.dgarq.roda.core.data.SIPState;

/**
 * Thrown to indicate that someone tried to activate the processing flag of a
 * {@link SIPState}, that was already active.
 * 
 * @author Rui Castro
 */
public class SIPAlreadyProcessingException extends IngestException {
	private static final long serialVersionUID = 2351438303801705864L;

	/**
	 * Constructs a new {@link SIPAlreadyProcessingException}.
	 */
	public SIPAlreadyProcessingException() {
	}

	/**
	 * Constructs a new {@link SIPAlreadyProcessingException} with the given
	 * error message.
	 * 
	 * @param message
	 *            the error message
	 */
	public SIPAlreadyProcessingException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link SIPAlreadyProcessingException} with the given
	 * error message and cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public SIPAlreadyProcessingException(String message, Throwable cause) {
		super(message, cause);
	}

}
