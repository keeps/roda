package pt.gov.dgarq.roda.core.common;

/**
 * Thrown to indicate that some error occured in the Logger.
 * 
 * @author Rui Castro
 */
public class LoggerException extends RODAException {
	private static final long serialVersionUID = 9061875666831921195L;

	/**
	 * Constructs a new {@link LoggerException}.
	 */
	public LoggerException() {
	}

	/**
	 * Constructs a new {@link LoggerException} with the given error message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public LoggerException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link LoggerException} with the given cause exception.
	 * 
	 * @param cause
	 *            the cause exception.
	 */
	public LoggerException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new {@link LoggerException} with the given error message and
	 * cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public LoggerException(String message, Throwable cause) {
		super(message, cause);
	}

}
