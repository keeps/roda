package pt.gov.dgarq.roda.core.scheduler;

import pt.gov.dgarq.roda.core.common.RODAException;

/**
 * This exception is thrown to indicate that some error occurred inside the
 * {@link SchedulerManager}.
 * 
 * @author Rui Castro
 */
public class RODASchedulerException extends RODAException {
	private static final long serialVersionUID = 6236799865864865619L;

	/**
	 * Constructs an empty {@link RODASchedulerException}.
	 */
	public RODASchedulerException() {
	}

	/**
	 * Constructs a {@link RODASchedulerException} with the specified error
	 * message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public RODASchedulerException(String message) {
		super(message);
	}

	/**
	 * Constructs a {@link RODASchedulerException} with the specified cause
	 * exception.
	 * 
	 * @param cause
	 *            the cause exception.
	 */
	public RODASchedulerException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a {@link RODASchedulerException} with the specified message
	 * and cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public RODASchedulerException(String message, Throwable cause) {
		super(message, cause);
	}

}
