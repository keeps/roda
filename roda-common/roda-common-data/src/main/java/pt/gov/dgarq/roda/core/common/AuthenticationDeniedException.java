package pt.gov.dgarq.roda.core.common;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 */
public class AuthenticationDeniedException extends RODAException {
	private static final long serialVersionUID = -8405660853143660038L;

	/**
	 * Constructs a new {@link AuthenticationDeniedException}.
	 */
	public AuthenticationDeniedException() {
	}

	/**
	 * Constructs a new {@link AuthenticationDeniedException} with the given
	 * error message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public AuthenticationDeniedException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link AuthenticationDeniedException} with the given
	 * cause exception.
	 * 
	 * @param cause
	 *            the cause exception.
	 */
	public AuthenticationDeniedException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new {@link AuthenticationDeniedException} with the given
	 * error message and cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public AuthenticationDeniedException(String message, Throwable cause) {
		super(message, cause);
	}

}
