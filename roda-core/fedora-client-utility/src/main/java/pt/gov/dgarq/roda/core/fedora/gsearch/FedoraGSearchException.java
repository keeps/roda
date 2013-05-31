package pt.gov.dgarq.roda.core.fedora.gsearch;

import pt.gov.dgarq.roda.core.fedora.FedoraClientException;

/**
 * @author Rui Castro
 * 
 */
public class FedoraGSearchException extends FedoraClientException {
	private static final long serialVersionUID = 3862140871562724091L;

	/**
	 * Constructs an empty {@link FedoraGSearchException}.
	 */
	public FedoraGSearchException() {
	}

	/**
	 * Constructs a {@link FedoraGSearchException} with the given error message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public FedoraGSearchException(String message) {
		super(message);
	}

	/**
	 * Constructs a {@link FedoraGSearchException} with the given cause
	 * exception.
	 * 
	 * @param cause
	 *            the cause exception.
	 */
	public FedoraGSearchException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a {@link FedoraGSearchException} with the given error message
	 * and cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public FedoraGSearchException(String message, Throwable cause) {
		super(message, cause);
	}

}
