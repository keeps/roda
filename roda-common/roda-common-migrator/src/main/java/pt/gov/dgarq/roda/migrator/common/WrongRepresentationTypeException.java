package pt.gov.dgarq.roda.migrator.common;

/**
 * @author Rui Castro
 */
public class WrongRepresentationTypeException extends InvalidRepresentationException {
	private static final long serialVersionUID = -5160865884448460405L;

	/**
	 * Constructs a new {@link WrongRepresentationTypeException}.
	 */
	public WrongRepresentationTypeException() {
	}

	/**
	 * Constructs a new {@link WrongRepresentationTypeException}.
	 * 
	 * @param message
	 *            the error message.
	 */
	public WrongRepresentationTypeException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link WrongRepresentationTypeException}.
	 * 
	 * @param cause
	 *            the cause exception
	 */
	public WrongRepresentationTypeException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new {@link WrongRepresentationTypeException}.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception
	 */
	public WrongRepresentationTypeException(String message, Throwable cause) {
		super(message, cause);
	}

}
