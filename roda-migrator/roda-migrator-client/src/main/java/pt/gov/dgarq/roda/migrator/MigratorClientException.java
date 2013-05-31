package pt.gov.dgarq.roda.migrator;

import pt.gov.dgarq.roda.core.common.RODAException;

/**
 * Thrown to indicate that some problem related to {@link MigratorClient} has
 * happened.
 * 
 * @author Rui Castro
 */
public class MigratorClientException extends RODAException {
	private static final long serialVersionUID = 5510869187925463737L;

	/**
	 * Constructs a new {@link MigratorClientException}.
	 */
	public MigratorClientException() {
	}

	/**
	 * Constructs a new {@link MigratorClientException} with the given error
	 * message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public MigratorClientException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link MigratorClientException} with the given cause
	 * exception.
	 * 
	 * @param cause
	 *            the cause exception.
	 */
	public MigratorClientException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new {@link MigratorClientException} with the given error
	 * message and cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public MigratorClientException(String message, Throwable cause) {
		super(message, cause);
	}

}
