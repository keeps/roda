package pt.gov.dgarq.roda.core;

import pt.gov.dgarq.roda.core.common.RODAException;

/**
 * Thrown to indicate that some problem related to {@link Downloader} has
 * happened.
 * 
 * @author Rui Castro
 */
public class DownloaderException extends RODAException {
	private static final long serialVersionUID = 9023809048623098271L;

	/**
	 * Constructs a new {@link DownloaderException}.
	 */
	public DownloaderException() {
	}

	/**
	 * Constructs a new {@link DownloaderException} with the given error
	 * message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public DownloaderException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link DownloaderException} with the given cause
	 * exception.
	 * 
	 * @param cause
	 *            the cause exception.
	 */
	public DownloaderException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new {@link DownloaderException} with the given error message
	 * and cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public DownloaderException(String message, Throwable cause) {
		super(message, cause);
	}

}
