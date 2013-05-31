package pt.gov.dgarq.roda.core.reports;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.adapter.sql.SQLContentAdapterEngine;
import pt.gov.dgarq.roda.core.common.NoSuchReportException;
import pt.gov.dgarq.roda.core.data.Attribute;
import pt.gov.dgarq.roda.core.data.Report;
import pt.gov.dgarq.roda.core.data.ReportItem;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.db.DatabaseUtility;

/**
 * This class provides methods to manipulate the reports database.
 * 
 * @author Rui Castro
 */
public class ReportDatabaseUtility extends
		DatabaseUtility<ReportAdapter, Report> {
	static final private Logger logger = Logger
			.getLogger(ReportDatabaseUtility.class);

	/**
	 * @param jdbcDriver
	 * @param jdbcURL
	 * @param jdbcUsername
	 * @param jdbcPassword
	 * 
	 * @throws ReportRegistryException
	 */
	public ReportDatabaseUtility(String jdbcDriver, String jdbcURL,
			String jdbcUsername, String jdbcPassword)
			throws ReportRegistryException {

		super();

		try {
			setJdbcDriver(jdbcDriver);
			setJdbcParameters(jdbcURL, jdbcUsername, jdbcPassword);

		} catch (SQLException e) {
			throw new ReportRegistryException("SQLException initializing "
					+ getClass().getSimpleName() + " - " + e.getMessage(), e);
		}
	}

	/**
	 * Inserts a new {@link Report} into the database.
	 * 
	 * @param report
	 *            the {@link Report} to insert.
	 * 
	 * 
	 * @return the inserted {@link Report}.
	 * 
	 * @throws ReportRegistryException
	 *             if a database exception occurred.
	 */
	public Report insertReport(Report report) throws ReportRegistryException {

		Connection connection = null;
		Statement statement = null;
		try {

			connection = getConnection();
			connection.setAutoCommit(false);

			statement = connection.createStatement();

			String newReportID = getNextID("Reports_id");
			report.setId(newReportID);

			String insertQuery = String.format(
					"INSERT INTO Reports (id, type, title)"
							+ " VALUES (%1$s, %2$s, %3$s)",
					quoteValueOrNull(report.getId()), quoteValueOrNull(report
							.getType()), quoteValueOrNull(report.getTitle()));

			logger.trace("executing insert query " + insertQuery);

			statement.executeUpdate(insertQuery);

			for (Attribute attribute : report.getAttributes()) {
				insertReportAttribute(connection, report.getId(), attribute);
			}

			for (int position = 0; position < report.getItems().length; position++) {
				insertReportItem(connection, report.getId(), position, report
						.getItems()[position]);
			}

			connection.commit();

		} catch (SQLException e) {

			try {
				if (connection != null) {
					connection.rollback();
				}
			} catch (SQLException e1) {
				logger.warn("SQLException executing rollback - "
						+ e.getMessage(), e1);
			}

			logger.error("SQLException inserting new Report - "
					+ e.getMessage(), e);
			throw new ReportRegistryException(
					"SQLException inserting new Report - " + e.getMessage(), e);

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

		try {

			return getReport(report.getId());

		} catch (NoSuchReportException e) {
			throw new ReportRegistryException("Error getting new Report - "
					+ e.getMessage(), e);
		}

	}

	/**
	 * Gets {@link Report} with the given ID.
	 * 
	 * @param reportID
	 *            the ID of the {@link Report}.
	 * 
	 * @return the {@link Report} with id <code>reportID</code>.
	 * 
	 * @throws NoSuchReportException
	 *             if there is not {@link Report} with id <code>reportID</code>.
	 * 
	 * @throws ReportRegistryException
	 *             if a database exception occurred.
	 */
	public Report getReport(String reportID) throws NoSuchReportException,
			ReportRegistryException {

		Connection connection = null;

		try {

			connection = getConnection();

			PreparedStatement statement = connection
					.prepareStatement("SELECT * FROM Reports WHERE id=?"); //$NON-NLS-1$
			statement.setString(1, reportID);

			Report report = new ReportAdapter()
					.getEntity(connection, statement);

			connection.close();

			if (report == null) {
				throw new NoSuchReportException("Report " + reportID
						+ " could not be found.");
			} else {
				return report;
			}

		} catch (SQLException e) {

			logger.debug("Exception getting Report info from database - "
					+ e.getMessage(), e);
			throw new ReportRegistryException(
					"Exception getting Report info from database - "
							+ e.getMessage(), e);

		} finally {
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
	 * Returns the number of {@link Report}s that respect the specified filters.
	 * 
	 * @param filter
	 * 
	 * @return the number of {@link Report}s.
	 * 
	 * @throws ReportRegistryException
	 */
	public int getReportsCount(Filter filter) throws ReportRegistryException {

		SQLContentAdapterEngine<ReportAdapter, Report> adapterEngine = new SQLContentAdapterEngine<ReportAdapter, Report>(
				new ReportAdapter(), new ContentAdapter(filter, null, null));

		try {

			Connection connection = getConnection();

			int entityCount = adapterEngine.getEntityCount(connection, filter,
					"Reports");

			connection.close();

			return entityCount;

		} catch (SQLException e) {

			logger
					.debug("SQLException counting Reports - " + e.getMessage(),
							e);

			throw new ReportRegistryException(
					"SQLException counting Reports - " + e.getMessage(), e);
		}
	}

	/**
	 * Get the list of {@link Report}s that match the given
	 * {@link ContentAdapter}.
	 * 
	 * @param contentAdapter
	 * @return a {@link List} of {@link Report}s.
	 * 
	 * @throws ReportRegistryException
	 */
	public List<Report> getReports(ContentAdapter contentAdapter)
			throws ReportRegistryException {

		SQLContentAdapterEngine<ReportAdapter, Report> adapterEngine = new SQLContentAdapterEngine<ReportAdapter, Report>(
				new ReportAdapter(), contentAdapter);
		try {

			Connection connection = getConnection();

			List<Report> entities = adapterEngine.getEntities(connection,
					"Reports");

			connection.close();

			return entities;

		} catch (SQLException e) {

			logger.debug("SQLException getting Reports - " + e.getMessage(), e);

			throw new ReportRegistryException("SQLException getting Reports - "
					+ e.getMessage(), e);
		}

	}

	/**
	 * Removes a {@link Report} from the database.
	 * 
	 * @param taskInstanceID
	 *            the ID of the {@link Report} to remove.
	 * 
	 * @throws NoSuchReportException
	 *             if the specified {@link Report} doesn't exist.
	 * 
	 * @throws ReportRegistryException
	 *             if a database exception occurred.
	 */
	public void removeReport(String taskInstanceID)
			throws NoSuchReportException, ReportRegistryException {

		Connection connection = null;
		Statement statement = null;

		try {
			connection = getConnection();
			statement = connection.createStatement();

			// Just for testing taskInstanceID exists. throws
			// NoSuchReportException
			getReport(taskInstanceID);

			String deleteQuery = String.format(
					"DELETE FROM Reports WHERE id='%1$s'",
					escapeValue(taskInstanceID));

			logger.trace("executing delete query " + deleteQuery);

			statement.executeUpdate(deleteQuery);

		} catch (SQLException e) {

			logger.error("SQL Exception, executing delete - " + e.getMessage(),
					e);
			throw new ReportRegistryException(
					"SQL Exception, executing delete - " + e.getMessage(), e);

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
	 * Inserts a new {@link Report} {@link Attribute} into the database.
	 * 
	 * @param connection
	 *            the current SQL connection.
	 * @param reportID
	 *            the ID of the {@link Report} related with this
	 *            {@link Attribute}.
	 * @param attribute
	 *            the {@link Attribute} to insert.
	 * 
	 * @throws SQLException
	 *             if a database exception occurred.
	 */
	private void insertReportAttribute(Connection connection, String reportID,
			Attribute attribute) throws SQLException {

		Statement statement = connection.createStatement();

		String insertQuery = String.format("INSERT INTO ReportAttributes ("
				+ "Reports_id, name, value" + ") VALUES ("
				+ "%1$s, %2$s, %3$s " + ")",

		reportID, quoteValueOrNull(attribute.getName()),
				quoteValueOrNull(attribute.getValue()));

		logger.trace("executing insert query " + insertQuery);

		statement.executeUpdate(insertQuery);

		try {
			if (statement != null) {
				statement.close();
			}
		} catch (SQLException e) {
			logger
					.warn("SQLException closing statement - " + e.getMessage(),
							e);
		}
	}

	private void insertReportItem(Connection connection, String reportID,
			int position, ReportItem reportItem) throws SQLException {

		Statement statement = connection.createStatement();

		String insertQuery = String.format("INSERT INTO ReportItems "
				+ "(Reports_id, position, title) VALUES (%1$s, %2$s, %3$s)",
				quoteValueOrNull(reportID), position,
				quoteValueOrNull(reportItem.getTitle()));

		logger.trace("executing insert query " + insertQuery);

		statement.executeUpdate(insertQuery);

		for (Attribute attribute : reportItem.getAttributes()) {
			insertReportItemAttribute(connection, reportID, position, attribute);
		}

		try {
			if (statement != null) {
				statement.close();
			}
		} catch (SQLException e) {
			logger
					.warn("SQLException closing statement - " + e.getMessage(),
							e);
		}
	}

	private void insertReportItemAttribute(Connection connection,
			String reportID, int reportItemPosition, Attribute attribute)
			throws SQLException {

		Statement statement = connection.createStatement();

		String insertQuery = String.format("INSERT INTO ReportItemAttributes ("
				+ "Reports_id, ReportItems_position, name, value"
				+ ") VALUES (" + "%1$s, %2$d, %3$s, %4$s)",

		quoteValueOrNull(reportID), reportItemPosition,
				quoteValueOrNull(attribute.getName()),
				quoteValueOrNull(attribute.getValue()));

		logger.trace("executing insert query " + insertQuery);

		statement.executeUpdate(insertQuery);

		try {
			if (statement != null) {
				statement.close();
			}
		} catch (SQLException e) {
			logger
					.warn("SQLException closing statement - " + e.getMessage(),
							e);
		}
	}

}
