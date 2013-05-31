package pt.gov.dgarq.roda.migrator.common;

import pt.gov.dgarq.roda.core.data.RepresentationObject;

/**
 * Thrown to indicate that the {@link RepresentationObject} to convert is
 * already in the desired format.
 * 
 * @author Rui Castro
 */
public class RepresentationAlreadyConvertedException extends ConverterException {
	private static final long serialVersionUID = -4265419259366291923L;

	/**
	 * Constructs a new {@link RepresentationAlreadyConvertedException}.
	 */
	public RepresentationAlreadyConvertedException() {
	}

	/**
	 * Constructs a new {@link RepresentationAlreadyConvertedException}.
	 * 
	 * @param message
	 *            the error message.
	 */
	public RepresentationAlreadyConvertedException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link RepresentationAlreadyConvertedException}.
	 * 
	 * @param cause
	 *            the cause exception
	 */
	public RepresentationAlreadyConvertedException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new {@link RepresentationAlreadyConvertedException}.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception
	 */
	public RepresentationAlreadyConvertedException(String message,
			Throwable cause) {
		super(message, cause);
	}

}
