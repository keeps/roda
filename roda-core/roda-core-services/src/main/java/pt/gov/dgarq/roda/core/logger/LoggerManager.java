package pt.gov.dgarq.roda.core.logger;

import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.RodaWebApplication;
import pt.gov.dgarq.roda.core.common.LoggerException;
import pt.gov.dgarq.roda.core.data.LogEntry;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;

/**
 * @author Rui Castro
 */
public class LoggerManager {

	static final private Logger logger = Logger.getLogger(LoggerManager.class);

	static private LoggerManager defaultLoggerManager = null;

	/**
	 * Returns the default {@link LoggerManager}. If it doesn't exist, a new
	 * {@link LoggerManager} it will be created and returned.
	 * 
	 * @return a {@link LoggerManager}.
	 * 
	 * @throws LoggerException
	 *             if the {@link LoggerManager} couldn't be created.
	 */
	public static LoggerManager getDefaultLoggerManager()
			throws LoggerException {
		if (defaultLoggerManager == null) {
			defaultLoggerManager = new LoggerManager();
		}
		return defaultLoggerManager;
	}

	private LoggerDatabaseUtility loggerDatabaseUtility = null;

	/**
	 * Creates a new {@link LoggerManager}.
	 * 
	 * @throws LoggerException
	 */
	private LoggerManager() throws LoggerException {

		try {

			Configuration configuration = RodaWebApplication.getConfiguration(
					getClass(), "logger.properties");

			String jdbcDriver = configuration.getString("jdbcDriver");
			String jdbcURL = configuration.getString("jdbcURL");
			String jdbcUsername = configuration.getString("jdbcUsername");
			String jdbcPassword = configuration.getString("jdbcPassword");

			if (loggerDatabaseUtility == null) {
				loggerDatabaseUtility = new LoggerDatabaseUtility(jdbcDriver,
						jdbcURL, jdbcUsername, jdbcPassword);
			}

		} catch (ConfigurationException e) {
			logger.debug(
					"Error reading configuration file - " + e.getMessage(), e);
			throw new LoggerException("Error reading configuration file - "
					+ e.getMessage(), e);
		}

		logger.info(getClass().getSimpleName() + " initialised OK");
	}

	/**
	 * Adds a new {@link LogEntry}.
	 * 
	 * @param logEntry
	 *            the {@link LogEntry} to add.
	 * 
	 * @throws LoggerException
	 */
	public void addLogEntry(LogEntry logEntry) throws LoggerException {
		this.loggerDatabaseUtility.addLogEntry(logEntry);
	}

	/**
	 * @param contentAdapter
	 * @return a list of of log entries that match the specified
	 *         {@link ContentAdapter}.
	 * @throws LoggerException
	 */
	public List<LogEntry> getLogEntries(ContentAdapter contentAdapter)
			throws LoggerException {
		return this.loggerDatabaseUtility.getLogEntries(contentAdapter);
	}

	/**
	 * @param contentAdapterFilter
	 * @return the number of log entries that match the specified {@link Filter}
	 *         .
	 * @throws LoggerException
	 */
	public int getLogEntriesCount(Filter contentAdapterFilter)
			throws LoggerException {
		return this.loggerDatabaseUtility
				.getLogEntriesCount(contentAdapterFilter);
	}

}
