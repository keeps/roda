package pt.gov.dgarq.roda.core.services;

import java.util.List;

import pt.gov.dgarq.roda.core.common.LoggerException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.LogEntry;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;

/**
 * This class implements LogMonitor service.
 * 
 * @author Rui Castro
 */
public class LogMonitor extends RODAWebService {

	static final private org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(LogMonitor.class);

	/**
	 * Constructs a new {@link LogMonitor}.
	 * 
	 * @throws RODAServiceException
	 */
	public LogMonitor() throws RODAServiceException {
		super();

		logger.info(getClass().getSimpleName() + " initialised OK");
	}

	/**
	 * Gets the number of {@link LogEntry}s that match the given
	 * {@link ContentAdapter}.
	 * 
	 * @param filter
	 * 
	 * @return the number of {@link LogEntry}s that match the specified
	 *         {@link Filter}.
	 * 
	 * @throws LoggerException
	 */
	public int getLogEntriesCount(Filter filter) throws LoggerException {

		// registerAction("Logger.getLogEntriesCount", new String[] { "filter",
		// "" + filter },
		// "User %username% called method Logger.getLogEntriesCount("
		// + filter + ")");

		return getLoggerManager().getLogEntriesCount(filter);
	}

	/**
	 * Gets the {@link LogEntry}s that match the given {@link ContentAdapter}.
	 * 
	 * @param contentAdapter
	 * 
	 * @return an array of {@link LogEntry}s that match the specified
	 *         {@link ContentAdapter}.
	 * 
	 * @throws LoggerException
	 */
	public LogEntry[] getLogEntries(ContentAdapter contentAdapter)
			throws LoggerException {

		// registerAction("Logger.getLogEntries", new String[] {
		// "contentAdapter",
		// "" + contentAdapter },
		// "User %username% called method Logger.getLogEntries("
		// + contentAdapter + ")");
		try {

			List<LogEntry> logEntries = getLoggerManager().getLogEntries(
					contentAdapter);
			return logEntries.toArray(new LogEntry[logEntries.size()]);

		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
			throw new LoggerException(t.getMessage(), t);
		}
	}

}
