package pt.gov.dgarq.roda.core.metadata.xacml;

import pt.gov.dgarq.roda.core.common.RODAException;

/**
 * @author Rui Castro
 */
public class PolicyMetadataException extends RODAException {
	private static final long serialVersionUID = -4554130532887668946L;

	/**
	 * Constructs a new {@link PolicyMetadataException}.
	 */
	public PolicyMetadataException() {
	}

	/**
	 * Constructs a new {@link PolicyMetadataException} with the specified error
	 * message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public PolicyMetadataException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link PolicyMetadataException} with the specified cause
	 * exception.
	 * 
	 * @param cause
	 *            the cause exception.
	 */
	public PolicyMetadataException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new {@link PolicyMetadataException} with the specified error
	 * message and cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public PolicyMetadataException(String message, Throwable cause) {
		super(message, cause);
	}

}
