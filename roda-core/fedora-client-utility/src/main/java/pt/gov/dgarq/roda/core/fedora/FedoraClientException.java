package pt.gov.dgarq.roda.core.fedora;

import pt.gov.dgarq.roda.core.common.RODAException;

/**
 * Thrown to indicate that an error related with the {@link FedoraClientUtility}
 * happened.
 * 
 * @author Rui Castro
 */
public class FedoraClientException extends RODAException {
	private static final long serialVersionUID = 7671067817877836266L;

	/**
	 * Constructs a new {@link FedoraClientException}.
	 */
	public FedoraClientException() {
	}

	/**
	 * Constructs a new {@link FedoraClientException} with the given error
	 * message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public FedoraClientException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link FedoraClientException} with the given cause
	 * exception.
	 * 
	 * @param cause
	 *            the cause exception.
	 */
	public FedoraClientException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new {@link FedoraClientException} with the given message and
	 * cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public FedoraClientException(String message, Throwable cause) {
		super(message, cause);
	}

}
