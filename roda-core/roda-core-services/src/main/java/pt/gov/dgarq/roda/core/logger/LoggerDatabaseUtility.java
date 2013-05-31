package pt.gov.dgarq.roda.core.logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.adapter.sql.SQLContentAdapterEngine;
import pt.gov.dgarq.roda.core.common.LoggerException;
import pt.gov.dgarq.roda.core.data.LogEntry;
import pt.gov.dgarq.roda.core.data.LogEntryParameter;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.db.DatabaseUtility;

/**
 * @author Rui Castro
 */
public class LoggerDatabaseUtility extends
		DatabaseUtility<LogEntryAdapter, LogEntry> {
	static final private Logger logger = Logger
			.getLogger(LoggerDatabaseUtility.class);

	/**
	 * @param jdbcDriver
	 * @param jdbcURL
	 * @param jdbcUsername
	 * @param jdbcPassword
	 * 
	 * @throws LoggerException
	 */
	public LoggerDatabaseUtility(String jdbcDriver, String jdbcURL,
			String jdbcUsername, String jdbcPassword) throws LoggerException {
		super();

		try {

			setJdbcDriver(jdbcDriver);
			setJdbcParameters(jdbcURL, jdbcUsername, jdbcPassword);

		} catch (SQLException e) {
			throw new LoggerException("SQLException initializing "
					+ getClass().getSimpleName() + " - " + e.getMessage(), e);
		}
	}

	/**
	 * Adds a new {@link LogEntry}.
	 * 
	 * @param logEntry
	 * 
	 * @throws LoggerException
	 */
	public void addLogEntry(LogEntry logEntry) throws LoggerException {

		Connection connection = null;
		Statement statement = null;
		String newLogID = null;
		try {

			connection = getConnection();
			connection.setAutoCommit(false);

			statement = connection.createStatement();

			newLogID = getNextID("Logs_id");

			String query = String
					.format(
							"INSERT INTO Logs (id, datetime, address, username, action, description, object, duration) "
									+ "VALUES ('%1$s', CURRENT_TIMESTAMP, %2$s, %3$s, %4$s, %5$s, %6$s, %7$d);",
							newLogID, quoteValueOrNull(logEntry.getAddress()),
							quoteValueOrNull(logEntry.getUsername()),
							quoteValueOrNull(logEntry.getAction()),
							quoteValueOrNull(logEntry.getDescription()),
							quoteValueOrNull(logEntry.getRelatedObjectPID()),
							logEntry.getDuration());

			logger.trace("addLogEntry(" + logEntry.getId()
					+ ", ...) Executing SQL " + query);

			@SuppressWarnings("unused")
			int affectedRows = statement.executeUpdate(query);

			LogEntryParameter[] parameters = logEntry.getParameters();
			for (int i = 0; parameters != null && i < parameters.length; i++) {

				query = String.format(
						"INSERT INTO LogParameters (Logs_id, position, name, value) "
								+ "VALUES ('%1$s', %2$s, %3$s, %4$s);",
						newLogID, i, quoteValueOrNull(parameters[i].getName()),
						quoteValueOrNull(parameters[i].getValue()));

				logger.trace("addLogEntry(" + logEntry.getId()
						+ ", ...) Executing SQL " + query);

				affectedRows = statement.executeUpdate(query);
			}

			connection.commit();

			logger.trace("Added " + logEntry);

		} catch (SQLException e) {
			logger.debug("An error occured. " + "The SQLException message is: "
					+ e.getMessage(), e);
			throw new LoggerException("An error occured. "
					+ "The SQLException message is: " + e.getMessage(), e);
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e) {
				logger.warn("SQLException closing statement - "
						+ e.getMessage(), e);
			}
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				logger.warn("SQLException closing connection - "
						+ e.getMessage(), e);
			}
		}

	}

	/**
	 * @param filter
	 * @return the number of log entries that match the specified {@link Filter}
	 *         .
	 * @throws LoggerException
	 */
	public int getLogEntriesCount(Filter filter) throws LoggerException {

		SQLContentAdapterEngine<LogEntryAdapter, LogEntry> adapterEngine = new SQLContentAdapterEngine<LogEntryAdapter, LogEntry>(
				new LogEntryAdapter(), new ContentAdapter(filter, null, null));

		try {

			Connection connection = getConnection();

			int entityCount = adapterEngine.getEntityCount(connection, filter,
					"Logs");

			connection.close();

			return entityCount;

		} catch (SQLException e) {

			logger.debug(
					"SQLException counting LogEntries - " + e.getMessage(), e);

			throw new LoggerException("SQLException counting LogEntries - "
					+ e.getMessage(), e);
		}
	}

	/**
	 * @param contentAdapter
	 * @return a list of of log entries that match the specified
	 *         {@link ContentAdapter}.
	 * @throws LoggerException
	 */
	public List<LogEntry> getLogEntries(ContentAdapter contentAdapter)
			throws LoggerException {

		SQLContentAdapterEngine<LogEntryAdapter, LogEntry> adapterEngine = new SQLContentAdapterEngine<LogEntryAdapter, LogEntry>(
				new LogEntryAdapter(), contentAdapter);

		try {

			Connection connection = getConnection();

			List<LogEntry> entities = adapterEngine.getEntities(connection,
					"Logs");

			connection.close();

			return entities;

		} catch (SQLException e) {

			logger.debug("SQLException getting LogEntries - " + e.getMessage(),
					e);

			throw new LoggerException("SQLException getting LogEntries - "
					+ e.getMessage(), e);
		}

	}

}
