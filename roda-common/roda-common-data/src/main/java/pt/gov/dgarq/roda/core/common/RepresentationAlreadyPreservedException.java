package pt.gov.dgarq.roda.core.common;

/**
 * Thrown to indicate that some representation is already being preserved.
 * 
 * @author Rui Castro
 * 
 */
public class RepresentationAlreadyPreservedException extends
		RODAServiceException {
	private static final long serialVersionUID = 8726625449662287462L;

	/**
	 * Constructs a new {@link RepresentationAlreadyPreservedException}.
	 */
	public RepresentationAlreadyPreservedException() {
	}

	/**
	 * Constructs a new {@link RepresentationAlreadyPreservedException} with the
	 * given error message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public RepresentationAlreadyPreservedException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link RepresentationAlreadyPreservedException} with the
	 * given cause exception.
	 * 
	 * @param cause
	 *            the cause exception.
	 */
	public RepresentationAlreadyPreservedException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new {@link RepresentationAlreadyPreservedException} with the
	 * given error message and cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public RepresentationAlreadyPreservedException(String message,
			Throwable cause) {
		super(message, cause);
	}

}
