package pt.gov.dgarq.roda.core.reports;

import pt.gov.dgarq.roda.core.common.RODAException;

/**
 * This exception is thrown to indicate that some error occurred inside the
 * {@link ReportManager}.
 * 
 * @author Rui Castro
 */
public class ReportManagerException extends RODAException {
	private static final long serialVersionUID = 6236799865864865619L;

	/**
	 * Constructs an empty {@link ReportManagerException}.
	 */
	public ReportManagerException() {
	}

	/**
	 * Constructs a {@link ReportManagerException} with the specified error
	 * message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public ReportManagerException(String message) {
		super(message);
	}

	/**
	 * Constructs a {@link ReportManagerException} with the specified cause
	 * exception.
	 * 
	 * @param cause
	 *            the cause exception.
	 */
	public ReportManagerException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a {@link ReportManagerException} with the specified message
	 * and cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public ReportManagerException(String message, Throwable cause) {
		super(message, cause);
	}

}
