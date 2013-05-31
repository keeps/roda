package pt.gov.dgarq.roda.plugins.converters.common;

import pt.gov.dgarq.roda.core.plugins.PluginException;

/**
 * Thrown to indicate that some error occurred in a
 * {@link RepresentationConverterPlugin}.
 * 
 * @author Rui Castro
 */
public class RepresentationConverterException extends PluginException {
	private static final long serialVersionUID = 6245068352861966443L;

	/**
	 * Constructs a new {@link RepresentationConverterException}.
	 */
	public RepresentationConverterException() {
	}

	/**
	 * Constructs a new {@link RepresentationConverterException} with the given
	 * error message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public RepresentationConverterException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link RepresentationConverterException} with the given
	 * cause exception.
	 * 
	 * @param cause
	 *            the cause exception.
	 */
	public RepresentationConverterException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new {@link RepresentationConverterException} with the given
	 * error message and cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public RepresentationConverterException(String message, Throwable cause) {
		super(message, cause);
	}

}
