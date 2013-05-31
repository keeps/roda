package pt.gov.dgarq.roda.core.common;

/**
 * Thrown to indicate that an error occurred in the IngestMonitor service.
 * 
 * @author Rui Castro
 */
public class IngestMonitorException extends RODAServiceException {
	private static final long serialVersionUID = -3528777126100614753L;

	/**
	 * Constructs a new {@link IngestMonitorException}.
	 */
	public IngestMonitorException() {
	}

	/**
	 * Constructs a new {@link IngestMonitorException} with the given error
	 * message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public IngestMonitorException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link IngestMonitorException} with the given cause
	 * exception.
	 * 
	 * @param cause
	 *            the cause exception.
	 */
	public IngestMonitorException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new {@link IngestMonitorException} with the given error
	 * message and cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public IngestMonitorException(String message, Throwable cause) {
		super(message, cause);
	}

}
