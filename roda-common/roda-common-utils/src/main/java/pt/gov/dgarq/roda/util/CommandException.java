package pt.gov.dgarq.roda.util;

/**
 * Thrown to indicate that a system command finished with an error code.
 * 
 * @author Rui Castro
 */
public class CommandException extends Exception {
	private static final long serialVersionUID = 1017228066365011437L;

	private int exitCode = 0;
	private String output = null;

	/**
	 * Constructs a {@link CommandException} with the given error message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public CommandException(String message) {
	}

	/**
	 * Constructs a {@link CommandException} with the given error message, exit
	 * code and output.
	 * 
	 * @param message
	 *            the error message.
	 * @param exitCode
	 *            the command exit code.
	 * @param output
	 *            the command output.
	 */
	public CommandException(String message, int exitCode, String output) {
		super(message);
	}

	/**
	 * Constructs a {@link CommandException} with the given error message and
	 * cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public CommandException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @return the exitCode
	 */
	public int getExitCode() {
		return exitCode;
	}

	/**
	 * @return the output
	 */
	public String getOutput() {
		return output;
	}

}
