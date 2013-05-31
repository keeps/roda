package pt.gov.dgarq.roda.core.fedora.risearch;

import pt.gov.dgarq.roda.core.fedora.FedoraClientException;

/**
 * Thrown to indicate that some error occurred related with
 * {@link FedoraRISearch}.
 * 
 * @author Rui Castro
 */
public class FedoraRISearchException extends FedoraClientException {
	private static final long serialVersionUID = 3784360154139058148L;

	/**
	 * Constructs an empty {@link FedoraRISearchException}.
	 */
	public FedoraRISearchException() {
	}

	/**
	 * Constructs a {@link FedoraRISearchException} with the given error
	 * message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public FedoraRISearchException(String message) {
		super(message);
	}

	/**
	 * Constructs a {@link FedoraRISearchException} with the given cause
	 * exception.
	 * 
	 * @param cause
	 *            the cause exception.
	 */
	public FedoraRISearchException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a {@link FedoraRISearchException} with the given error message
	 * and cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public FedoraRISearchException(String message, Throwable cause) {
		super(message, cause);
	}

}
