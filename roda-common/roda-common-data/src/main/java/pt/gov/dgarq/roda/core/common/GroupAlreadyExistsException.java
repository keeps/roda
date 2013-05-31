package pt.gov.dgarq.roda.core.common;

import pt.gov.dgarq.roda.core.data.Group;

/**
 * Thrown to indicate that a {@link Group} with the same name already exists
 * when a new one is trying to be created.
 * 
 * @author Rui Castro
 */
public class GroupAlreadyExistsException extends RODAServiceException {

	private static final long serialVersionUID = 6493339963861919270L;

	/**
	 * Constructs a new GroupAlreadyExistsException.
	 */
	public GroupAlreadyExistsException() {
	}

	/**
	 * Constructs a new GroupAlreadyExistsException with the given error
	 * message.
	 * 
	 * @param message
	 *            the error message
	 */
	public GroupAlreadyExistsException(String message) {
		super(message);
	}

	/**
	 * Constructs a new GroupAlreadyExistsException with the given cause
	 * exception.
	 * 
	 * @param cause
	 *            the cause exception
	 */
	public GroupAlreadyExistsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new GroupAlreadyExistsException with the given error message
	 * and cause exception.
	 * 
	 * @param message
	 *            the error message
	 * @param cause
	 *            the cause exception
	 */
	public GroupAlreadyExistsException(String message, Throwable cause) {
		super(message, cause);
	}

}
