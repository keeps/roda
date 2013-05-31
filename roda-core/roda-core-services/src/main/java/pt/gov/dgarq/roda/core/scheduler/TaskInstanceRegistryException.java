package pt.gov.dgarq.roda.core.scheduler;

/**
 * Thrown to indicate that an error occurred related with the task registry.
 * 
 * @author Rui Castro
 */
public class TaskInstanceRegistryException extends RODASchedulerException {
	private static final long serialVersionUID = -203585170076088324L;

	/**
	 * Constructs a new {@link TaskInstanceRegistryException}.
	 */
	public TaskInstanceRegistryException() {
	}

	/**
	 * Constructs a new {@link TaskInstanceRegistryException} with the given error
	 * message.
	 * 
	 * @param message
	 *            the error message
	 */
	public TaskInstanceRegistryException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link TaskInstanceRegistryException} with the given error
	 * message and cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public TaskInstanceRegistryException(String message, Throwable cause) {
		super(message, cause);
	}

}
