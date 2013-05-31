package pt.gov.dgarq.roda.migrator.common;

import pt.gov.dgarq.roda.core.data.RepresentationObject;

/**
 * Thrown to indicate that the {@link RepresentationObject} to convert is not
 * valid.
 * 
 * @author Rui Castro
 */
public class InvalidRepresentationException extends ConverterException {
	private static final long serialVersionUID = 4225828949081383651L;

	/**
	 * Constructs a new {@link InvalidRepresentationException}.
	 */
	public InvalidRepresentationException() {
	}

	/**
	 * Constructs a new {@link InvalidRepresentationException}.
	 * 
	 * @param message
	 *            the error message.
	 */
	public InvalidRepresentationException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link InvalidRepresentationException}.
	 * 
	 * @param cause
	 *            the cause exception
	 */
	public InvalidRepresentationException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new {@link InvalidRepresentationException}.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception
	 */
	public InvalidRepresentationException(String message, Throwable cause) {
		super(message, cause);
	}

}
