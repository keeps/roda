package pt.gov.dgarq.roda.core;

import pt.gov.dgarq.roda.core.common.RODAException;

/**
 * Thrown to indicate that an error occurred uploading.
 * 
 * @author Rui Castro
 */
public class UploadException extends RODAException {
	private static final long serialVersionUID = 6754171297194453122L;

	/**
	 * Constructs a new {@link UploadException}.
	 */
	public UploadException() {
	}

	/**
	 * Constructs a new {@link UploadException} with the given error message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public UploadException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link UploadException} with the given cause exception.
	 * 
	 * @param cause
	 *            the cause exception.
	 */
	public UploadException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new {@link UploadException} with the given error message and
	 * cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public UploadException(String message, Throwable cause) {
		super(message, cause);
	}

}
