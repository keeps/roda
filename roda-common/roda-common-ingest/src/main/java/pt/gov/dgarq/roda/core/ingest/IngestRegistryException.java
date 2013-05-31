package pt.gov.dgarq.roda.core.ingest;

import pt.gov.dgarq.roda.core.common.IngestException;


/**
 * Thrown to indicate that an error occurred related with the ingest registry.
 * 
 * @author Rui Castro
 */
public class IngestRegistryException extends IngestException {
	private static final long serialVersionUID = -7612762434556929187L;

	/**
	 * Constructs a new {@link IngestRegistryException}.
	 */
	public IngestRegistryException() {
	}

	/**
	 * Constructs a new {@link IngestRegistryException} with the given error
	 * message.
	 * 
	 * @param message
	 *            the error message
	 */
	public IngestRegistryException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link IngestRegistryException} with the given error
	 * message and cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public IngestRegistryException(String message, Throwable cause) {
		super(message, cause);
	}

}
