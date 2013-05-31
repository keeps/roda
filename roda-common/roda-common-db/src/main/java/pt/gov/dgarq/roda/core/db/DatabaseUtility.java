package pt.gov.dgarq.roda.core.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.apache.log4j.Logger;
import org.w3c.util.DateParser;

import pt.gov.dgarq.roda.core.adapter.sql.SQLEntityAdapter;

/**
 * @author Rui Castro
 * 
 * @param <EA>
 *            the entity adapter
 * @param <E>
 *            the entity being adapted
 */
public class DatabaseUtility<EA extends SQLEntityAdapter<E>, E> {
	static final private Logger logger = Logger
			.getLogger(DatabaseUtility.class);

	private static final int MAX_TEXT_LENGTH = 32000;

	private String jdbcDriver = null;
	private String jdbcURL = null;
	private String jdbcUsername = null;
	private String jdbcPassword = null;

	/**
	 * Constructs a new (<strong>uninitialized</strong>) {@link DatabaseUtility}
	 * .
	 * 
	 * {@link DatabaseUtility#setJdbcDriver(String)} and
	 * {@link DatabaseUtility#setJdbcParameters(String, String, String)} must be
	 * called to properly initialize this class.
	 */
	public DatabaseUtility() {
	}

	/**
	 * Constructs a new {@link DatabaseUtility} with the specified parameters.
	 * 
	 * @param jdbcDriver
	 * @param jdbcURL
	 * @param jdbcUsername
	 * @param jdbcPassword
	 * 
	 * @throws SQLException
	 */
	public DatabaseUtility(String jdbcDriver, String jdbcURL,
			String jdbcUsername, String jdbcPassword) throws SQLException {
		setJdbcDriver(jdbcDriver);
		setJdbcParameters(jdbcURL, jdbcUsername, jdbcPassword);
	}

	/**
	 * @return the jdbcDriver
	 */
	public String getJdbcDriver() {
		return jdbcDriver;
	}

	/**
	 * @param jdbcDriver
	 *            the jdbcDriver to set
	 * @throws SQLException
	 */
	public void setJdbcDriver(String jdbcDriver) throws SQLException {

		// Register the JDBC Driver
		try {

			Class.forName(jdbcDriver).newInstance();

			logger.debug("JDBC driver " + jdbcDriver + " registered ok.");

			this.jdbcDriver = jdbcDriver;

			// } catch (InstantiationException e) {
			// } catch (IllegalAccessException e) {
			// } catch (ClassNotFoundException e) {

		} catch (Exception e) {
			logger.error("Unable to register the JDBC Driver - "
					+ e.getMessage() + "\n"
					+ "Make sure the JDBC driver is in the classpath.\n");
			throw new SQLException("Unable to register the JDBC Driver - "
					+ e.getMessage());
			// TODO uncomment when update o java 6
			// throw new SQLException("Unable to register the JDBC Driver - " +
			// e.getMessage(), e);
		}

	}

	/**
	 * @return the jdbcURL
	 */
	public String getJdbcURL() {
		return jdbcURL;
	}

	/**
	 * @param jdbcURL
	 *            the jdbcURL to set
	 * @param jdbcUsername
	 * @param jdbcPassword
	 */
	public void setJdbcParameters(String jdbcURL, String jdbcUsername,
			String jdbcPassword) {
		this.jdbcURL = jdbcURL;
		this.jdbcUsername = jdbcUsername;
		this.jdbcPassword = jdbcPassword;
	}

	/**
	 * @return the jdbcUsername
	 */
	public String getJdbcUsername() {
		return jdbcUsername;
	}

	/**
	 * @return the jdbcPassword
	 */
	public String getJdbcPassword() {
		return jdbcPassword;
	}

	/**
	 * @return the connection
	 * @throws SQLException
	 */
	protected Connection getConnection() throws SQLException {
		// Make a new connection with the database.
		return DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
	}

	/**
	 * Gets the next id from the specified sequence table;
	 * 
	 * @param sequence_table
	 *            the name of the sequence table.
	 * 
	 * @return the next ID.
	 * 
	 * @throws SQLException
	 */
	synchronized protected String getNextID(String sequence_table)
			throws SQLException {

		Connection connection = getConnection();
		connection.setAutoCommit(false);

		Statement statement = connection.createStatement();

		try {

			String selectQuery = String.format("SELECT id FROM %s",
					sequence_table);
			ResultSet resultSet = statement.executeQuery(selectQuery);

			long lastID;
			if (resultSet.first()) {

				lastID = resultSet.getLong("id");
				lastID++;

				statement.executeUpdate(String.format("UPDATE %s SET id=%d",
						sequence_table, lastID));

			} else {
				throw new SQLException(sequence_table
						+ " didn't return a new id");
			}

			connection.commit();

			return Long.toString(lastID);

		} catch (SQLException e) {
			logger.warn("Exception getting next id from " + sequence_table
					+ " - " + e.getMessage(), e);
			throw e;
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				logger.warn("Exception closing statement - " + e.getMessage()
						+ " - IGNORING");
			}
			try {
				connection.close();
			} catch (SQLException e) {
				logger.warn("Exception closing connection - " + e.getMessage()
						+ " - IGNORING");
			}
		}
	}

	/**
	 * @param value
	 * @return
	 */
	public static String quoteValueOrNull(String value) {

		if (value != null) {
			return "'" + escapeValue(value) + "'";
		} else {
			return "NULL";
		}
	}

	/**
	 * @param value
	 * @return
	 */
	public static String quoteAndTruncateValueOrNull(String value) {
		// FIXME replace locale specific string
		if (value != null) {
			if (value.length() > MAX_TEXT_LENGTH) {
				value = value.substring(0, MAX_TEXT_LENGTH)
						+ "\n(Texto truncado).";
			}
			return "'" + escapeValue(value) + "'";
		} else {
			return "NULL";
		}
	}

	/**
	 * @param value
	 * @return
	 */
	public static String truncateValueOrNull(String value) {
		// FIXME replace locale specific string
		if (value != null) {
			if (value.length() > MAX_TEXT_LENGTH) {
				value = value.substring(0, MAX_TEXT_LENGTH)
						+ "\n(Texto truncado).";
			}
			return value;
		} else {
			return "NULL";
		}
	}

	/**
	 * @param date
	 * @return
	 */
	public static String quoteValueOrNull(Date date) {

		if (date != null) {

			String toIsoDate = DateParser.getIsoDate(date).replace("T", " ")
					.replace("Z", "");
			return "TIMESTAMP '" + toIsoDate + "'";

		} else {
			return "NULL";
		}
	}

	/**
	 * @param value
	 * @return
	 */
	public static String escapeValue(String value) {
		// logger.trace("Not escaped value: " + value);

		// This backslashes are madness!!!
		// TODO use Matcher.quoteReplacement(s)
		String escapedValue = value.replaceAll("\\\\", "\\\\\\\\");
		escapedValue = escapedValue.replaceAll("'", "\\\\'");

		// logger.trace("Escaped value: " + escapedValue);

		return escapedValue;
	}

}
