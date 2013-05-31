package pt.gov.dgarq.roda.core.services;

import java.util.Date;

import pt.gov.dgarq.roda.core.common.LoggerException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.LogEntry;

/**
 * This class implements Logger service.
 * 
 * @author Rui Castro
 */
public class Logger extends RODAWebService {

	static final private org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(Logger.class);

	/**
	 * Constructs a new {@link Logger}.
	 * 
	 * @throws RODAServiceException
	 */
	public Logger() throws RODAServiceException {
		super();

		logger.info(getClass().getSimpleName() + " initialised OK");
	}

	/**
	 * Adds a new {@link LogEntry} to the user logs.
	 * 
	 * @param logEntry
	 *            the {@link LogEntry} to add.
	 * 
	 * @throws LoggerException
	 */
	public void addLogEntry(LogEntry logEntry) throws LoggerException {
		Date start = new Date();

		if (logEntry != null) {
			logEntry.setAddress(getClientAddress());
		}

		getLoggerManager().addLogEntry(logEntry);

		long duration = new Date().getTime() - start.getTime();
		registerAction("Logger.addLogEntry", new String[] { "logEntry",
				"" + logEntry },
				"User %username% called method Logger.addLogEntry(" + logEntry
						+ ")", duration);
	}

}
