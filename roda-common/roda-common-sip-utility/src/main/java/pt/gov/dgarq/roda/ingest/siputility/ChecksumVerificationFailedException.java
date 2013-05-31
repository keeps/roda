package pt.gov.dgarq.roda.ingest.siputility;

/**
 * Thrown to indicate that a checksum verification failed.
 * 
 * @author Rui Castro
 */
public class ChecksumVerificationFailedException extends SIPException {
	private static final long serialVersionUID = -2887071679366168354L;

	/**
	 * Constructs a new {@link ChecksumVerificationFailedException}.
	 */
	public ChecksumVerificationFailedException() {
	}

	/**
	 * Constructs a new {@link ChecksumVerificationFailedException} with the
	 * given error message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public ChecksumVerificationFailedException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link ChecksumVerificationFailedException} with the
	 * given cause exception.
	 * 
	 * @param cause
	 *            the cause exception.
	 */
	public ChecksumVerificationFailedException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new {@link ChecksumVerificationFailedException} with the
	 * given error message and cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public ChecksumVerificationFailedException(String message, Throwable cause) {
		super(message, cause);
	}

}
