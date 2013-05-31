package pt.gov.dgarq.roda.core.reports;


/**
 * Thrown to indicate that an error occurred related with the task registry.
 * 
 * @author Rui Castro
 */
public class ReportRegistryException extends ReportManagerException {
	private static final long serialVersionUID = -203585170076088324L;

	/**
	 * Constructs a new {@link ReportRegistryException}.
	 */
	public ReportRegistryException() {
	}

	/**
	 * Constructs a new {@link ReportRegistryException} with the given error
	 * message.
	 * 
	 * @param message
	 *            the error message
	 */
	public ReportRegistryException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link ReportRegistryException} with the given error
	 * message and cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public ReportRegistryException(String message, Throwable cause) {
		super(message, cause);
	}

}
