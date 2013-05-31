package pt.gov.dgarq.roda.migrator.common;

import pt.gov.dgarq.roda.core.common.RODAServiceException;

/**
 * Thrown to indicate an exception inside a converter.
 * 
 * @author Rui Castro
 */
public class ConverterException extends RODAServiceException {
	private static final long serialVersionUID = 2914810664489391655L;

	/**
	 * Construct a new {@link ConverterException}.
	 */
	public ConverterException() {
	}

	/**
	 * Construct a new {@link ConverterException} with the given error message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public ConverterException(String message) {
		super(message);
	}

	/**
	 * Construct a new {@link ConverterException} with the given cause
	 * exception.
	 * 
	 * @param cause
	 *            the cause exception.
	 */
	public ConverterException(Throwable cause) {
		super(cause);
	}

	/**
	 * Construct a new {@link ConverterException} with the given error message
	 * and cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public ConverterException(String message, Throwable cause) {
		super(message, cause);
	}

}
