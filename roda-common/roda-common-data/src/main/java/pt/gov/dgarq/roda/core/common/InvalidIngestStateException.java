package pt.gov.dgarq.roda.core.common;

/**
 * Thrown to indicate that an invalid ingest state is being used.
 * 
 * @author Rui Castro
 */
public class InvalidIngestStateException extends IngestException {
	private static final long serialVersionUID = 653466841612941161L;

	/**
	 * Constructs a new {@link InvalidIngestStateException}.
	 */
	public InvalidIngestStateException() {
	}

	/**
	 * Constructs a new {@link InvalidIngestStateException} with the given error
	 * message.
	 * 
	 * @param message
	 */
	public InvalidIngestStateException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link InvalidIngestStateException} with the given error
	 * message and cause exception.
	 * 
	 * @param message
	 * @param cause
	 */
	public InvalidIngestStateException(String message, Throwable cause) {
		super(message, cause);
	}

}
