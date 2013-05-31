package pt.gov.dgarq.roda.common;

/**
 * Thrown to indicate that an error occurred inside {@link JhoveUtility}.
 * 
 * @author Rui Castro
 */
public class JhoveUtilityException extends Exception {
	private static final long serialVersionUID = 7068305640169301278L;

	/**
	 * Construct a new {@link JhoveUtilityException}.
	 */
	public JhoveUtilityException() {
	}

	/**
	 * Construct a new {@link JhoveUtilityException} with the given error
	 * message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public JhoveUtilityException(String message) {
		super(message);
	}

	/**
	 * Construct a new {@link JhoveUtilityException} with the given cause
	 * exception.
	 * 
	 * @param cause
	 *            the cause exception.
	 */
	public JhoveUtilityException(Throwable cause) {
		super(cause);
	}

	/**
	 * Construct a new {@link JhoveUtilityException} with the given error
	 * message and cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public JhoveUtilityException(String message, Throwable cause) {
		super(message, cause);
	}

}
