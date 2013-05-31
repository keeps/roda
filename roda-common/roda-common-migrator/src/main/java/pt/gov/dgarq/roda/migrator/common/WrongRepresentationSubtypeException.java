package pt.gov.dgarq.roda.migrator.common;

/**
 * @author Rui Castro
 */
public class WrongRepresentationSubtypeException extends InvalidRepresentationException {
	private static final long serialVersionUID = -5160865884448460405L;

	/**
	 * Constructs a new {@link WrongRepresentationSubtypeException}.
	 */
	public WrongRepresentationSubtypeException() {
	}

	/**
	 * Constructs a new {@link WrongRepresentationSubtypeException}.
	 * 
	 * @param message
	 *            the error message.
	 */
	public WrongRepresentationSubtypeException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link WrongRepresentationSubtypeException}.
	 * 
	 * @param cause
	 *            the cause exception
	 */
	public WrongRepresentationSubtypeException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new {@link WrongRepresentationSubtypeException}.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception
	 */
	public WrongRepresentationSubtypeException(String message, Throwable cause) {
		super(message, cause);
	}

}
