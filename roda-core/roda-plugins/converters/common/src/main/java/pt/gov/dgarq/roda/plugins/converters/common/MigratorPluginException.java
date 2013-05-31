package pt.gov.dgarq.roda.plugins.converters.common;

import pt.gov.dgarq.roda.core.data.Report;
import pt.gov.dgarq.roda.core.data.ReportItem;
import pt.gov.dgarq.roda.core.plugins.PluginException;

/**
 * Thrown to indicate that an migrator error had occurred.
 * 
 * @author Rui Castro
 */
public class MigratorPluginException extends PluginException {
	private static final long serialVersionUID = -2181083030576330601L;

	/**
	 * Constructs a new {@link MigratorPluginException}.
	 */
	public MigratorPluginException() {
	}

	/**
	 * Constructs a new {@link MigratorPluginException} with the specified error
	 * message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public MigratorPluginException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link MigratorPluginException} with the specified cause
	 * exception.
	 * 
	 * @param cause
	 *            the cause exception.
	 */
	public MigratorPluginException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new {@link MigratorPluginException} with the specified error
	 * message and cause exception.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 */
	public MigratorPluginException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new {@link MigratorPluginException} with the specified error
	 * message, cause exception and execution {@link Report}.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 * @param report
	 *            the execution report until the error.
	 */
	public MigratorPluginException(String message, Throwable cause,
			Report report) {
		super(message, cause, report);
	}

	/**
	 * Constructs a new {@link MigratorPluginException} with the specified error
	 * message, cause exception and execution {@link ReportItem}.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 * @param reportItem
	 *            the execution report item until the error.
	 */
	public MigratorPluginException(String message, Throwable cause,
			ReportItem reportItem) {
		super(message, cause, reportItem);
	}

	/**
	 * Constructs a new {@link MigratorPluginException} with the specified error
	 * message, cause exception, execution {@link ReportItem} and
	 * {@link ReportItem}.
	 * 
	 * @param message
	 *            the error message.
	 * @param cause
	 *            the cause exception.
	 * @param report
	 *            the execution report until the error.
	 * @param reportItem
	 *            the execution report item until the error.
	 */
	public MigratorPluginException(String message, Throwable cause,
			Report report, ReportItem reportItem) {
		super(message, cause, report, reportItem);
	}

}
