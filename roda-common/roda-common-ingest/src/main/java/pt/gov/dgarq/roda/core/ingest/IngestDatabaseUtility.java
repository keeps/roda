package pt.gov.dgarq.roda.core.ingest;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.adapter.sql.SQLContentAdapterEngine;
import pt.gov.dgarq.roda.core.common.NoSuchSIPException;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.db.DatabaseUtility;

/**
 * This class provides methods to manipulate the ingest registry database.
 * 
 * @author Rui Castro
 */
public class IngestDatabaseUtility extends
		DatabaseUtility<SIPStateAdapter, SIPState> {
	static final private Logger logger = Logger
			.getLogger(IngestDatabaseUtility.class);

	/**
	 * @param jdbcDriver
	 * @param jdbcURL
	 * @param jdbcUsername
	 * @param jdbcPassword
	 * 
	 * @throws IngestRegistryException
	 */
	public IngestDatabaseUtility(String jdbcDriver, String jdbcURL,
			String jdbcUsername, String jdbcPassword)
			throws IngestRegistryException {

		super();

		try {
			setJdbcDriver(jdbcDriver);
			setJdbcParameters(jdbcURL, jdbcUsername, jdbcPassword);

		} catch (SQLException e) {
			throw new IngestRegistryException("SQLException initializing "
					+ getClass().getSimpleName() + " - " + e.getMessage(), e);
		}
	}

	/**
	 * Inserts a new {@link SIPState} into the database.
	 * 
	 * @param username
	 *            the username for this {@link SIPState}.
	 * @param originalFilename
	 *            the original {@link SIPState} filename.
	 * 
	 * @return the created {@link SIPState}.
	 * 
	 * @throws IngestRegistryException
	 *             if a database exception occurred.
	 */
	public SIPState insertSIPState(String username, String originalFilename)
			throws IngestRegistryException {

		Connection connection = null;
		Statement statement = null;
		String newSipID = null;
		try {

			connection = getConnection();
			connection.setAutoCommit(false);

			statement = connection.createStatement();

			newSipID = getNextID("SIPs_id");

			String insertQuery = String.format(
					"INSERT INTO SIPs (id, username, original_filename) "
							+ "VALUES ('%1$s', '%2$s', '%3$s')", newSipID,
					escapeValue(username), escapeValue(originalFilename));

			logger.trace("executing insert query " + insertQuery);

			statement.executeUpdate(insertQuery);

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

			logger.error("SQLException inserting new SIP - " + e.getMessage(),
					e);
			throw new IngestRegistryException(
					"SQLException inserting new SIP - " + e.getMessage(), e);

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

			return getSIPState(newSipID);

		} catch (NoSuchSIPException e) {
			throw new IngestRegistryException("Error getting new SIP - "
					+ e.getMessage(), e);
		}

	}

	/**
	 * Register a new state transition for a given {@link SIPState}.
	 * 
	 * @param sipID
	 *            the {@link SIPState} ID.
	 * @param oldState
	 *            the old state.
	 * @param newState
	 *            the new state.
	 * @param complete
	 *            a flag (<code>true</code>/<code>false</code>)
	 * @param percentage
	 * @param ingestedPID
	 *            the PID of the ingested object
	 * @param parentPID
	 *            the PID of the parent description object
	 * @param taskID
	 *            the ID of the task responsible for this transition.
	 * @param success
	 *            a flag (<code>true</code>/<code>false</code>)
	 * @param description
	 *            a message describing this transition.
	 * 
	 * @throws IngestRegistryException
	 *             if a database exception occurred.
	 */
	public void insertStateTransition(String sipID, String oldState,
			String newState, boolean complete, float percentage,
			String ingestedPID, String parentPID, String taskID,
			boolean success, String description) throws IngestRegistryException {

		Connection connection = null;
		Statement statement = null;

		try {

			connection = getConnection();
			connection.setAutoCommit(false);

			statement = connection.createStatement();

			// SIP sip = getSIP(sipID);

			String updateQuery = String.format(
					"UPDATE SIPs SET state='%s',complete=%b,percentage=%f,pid=%s,parent_pid=%s"
							+ " WHERE id='%s'", escapeValue(newState),
					complete, percentage, quoteValueOrNull(ingestedPID),
					quoteValueOrNull(parentPID), escapeValue(sipID));

			logger.trace("executing update query " + updateQuery);

			statement.executeUpdate(updateQuery);

			String insertQuery = String
					.format(
							"INSERT INTO SIPStateTransitions (SIPs_id, from_state, to_state, datetime, task_id, success, description) "
									+ "VALUES ('%1$s', %2$s, '%3$s', CURRENT_TIMESTAMP, %4$s, %5$s, %6$s)",
							escapeValue(sipID), quoteValueOrNull(oldState),
							escapeValue(newState), quoteValueOrNull(taskID),
							success, quoteAndTruncateValueOrNull(description));

			logger.trace("executing update query " + insertQuery);

			statement.executeUpdate(insertQuery);

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

			logger.error("SQLException registering state change - "
					+ e.getMessage(), e);
			throw new IngestRegistryException(
					"SQLException registering state change - " + e.getMessage(),
					e);

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
	 * Activates the processing flag of the specified {@link SIPState}.
	 * 
	 * @param sipID
	 *            the ID of the {@link SIPState}.
	 * 
	 * @throws NoSuchSIPException
	 *             if the specified {@link SIPState} deson't exist.
	 * @throws SIPAlreadyProcessingException
	 *             if the processing flag is already on.
	 * @throws IngestRegistryException
	 *             if any other error occurs.
	 */
	public void activateProcessingFlag(String sipID) throws NoSuchSIPException,
			SIPAlreadyProcessingException, IngestRegistryException {

		Connection connection = null;
		Statement statement = null;
		try {

			connection = getConnection();
			connection.setAutoCommit(false);

			statement = connection.createStatement();

			String selectQuery = String.format(
					"SELECT processing FROM SIPs WHERE id='%1$s'",
					escapeValue(sipID));

			boolean processing;

			ResultSet resultSet = statement.executeQuery(selectQuery);
			if (resultSet.first()) {
				processing = resultSet.getBoolean("processing");
			} else {
				throw new NoSuchSIPException("SIP " + sipID
						+ " doesn't exist in database.");
			}

			if (processing) {

				connection.commit();

				throw new SIPAlreadyProcessingException("SIP " + sipID
						+ " already has the processing flag turned on.");

			} else {

				String updateQuery = String.format(
						"UPDATE SIPs SET processing=TRUE WHERE id='%1$s'",
						sipID);

				logger.trace("Executing SQL UPDATE query " + updateQuery);

				statement.executeUpdate(updateQuery);

				connection.commit();
			}

		} catch (SQLException e) {

			try {
				if (connection != null) {
					connection.rollback();
				}
			} catch (SQLException e1) {
				logger.warn("SQLException executing rollback - "
						+ e.getMessage(), e1);
			}

			logger.error("Exception activating processing flag for SIP "
					+ sipID + " - " + e.getMessage(), e);
			throw new IngestRegistryException(
					"Exception activating processing flag for SIP " + sipID
							+ " - " + e.getMessage(), e);

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
	 * Activates the processing flag of the specified {@link SIPState}.
	 * 
	 * @param sipID
	 *            the ID of the {@link SIPState}.
	 * 
	 * @return a <code>boolean</code> with the value of the processing flag
	 *         before deactivation. <strong>NOTE:</strong> a value of
	 *         <code>false</code> could mean the flag was off or that the
	 *         {@link SIPState} doesn't exist in the database.
	 * 
	 * @throws IngestRegistryException
	 *             if any other error occurs.
	 */
	public boolean deactivateProcessingFlag(String sipID)
			throws IngestRegistryException {
		Connection connection = null;
		Statement statement = null;
		try {

			connection = getConnection();
			connection.setAutoCommit(true);

			statement = connection.createStatement();

			String updateQuery = String.format(
					"UPDATE SIPs SET processing=FALSE WHERE id='%1$s'",
					escapeValue(sipID));

			logger.trace("Executing SQL UPDATE query " + updateQuery);

			int rowsUpdated = statement.executeUpdate(updateQuery);

			boolean oldProcessingFlag = rowsUpdated == 1;

			return oldProcessingFlag;

			// connection.commit();

		} catch (SQLException e) {

			try {
				if (connection != null) {
					connection.rollback();
				}
			} catch (SQLException e1) {
				logger.warn("SQLException executing rollback - "
						+ e.getMessage(), e1);
			}

			logger.error("Exception deactivating processing flag for SIP "
					+ sipID + " - " + e.getMessage(), e);
			throw new IngestRegistryException(
					"Exception deactivating processing flag for SIP " + sipID
							+ " - " + e.getMessage(), e);

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
	 * Sets all processing flags to FALSE.
	 * <p>
	 * <strong>ATTENTION:</strong> this method should be used with care. It
	 * should only be called when the caller is certain that there are no SIPs
	 * being processed, like before the ingest tasks are initiated.
	 * </p>
	 * 
	 * @throws IngestRegistryException
	 */
	public void clearProcessingFlags() throws IngestRegistryException {
		Connection connection = null;
		Statement statement = null;
		try {

			connection = getConnection();
			connection.setAutoCommit(true);

			statement = connection.createStatement();

			String updateQuery = "UPDATE SIPs SET processing=FALSE";

			logger.trace("Executing SQL UPDATE query " + updateQuery);

			statement.executeUpdate(updateQuery);

		} catch (SQLException e) {

			logger.error("Exception clearing all processing flags - "
					+ e.getMessage(), e);
			throw new IngestRegistryException(
					"Exception clearing all processing flags - "
							+ e.getMessage(), e);

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
	 * Gets {@link SIPState} with the given ID.
	 * 
	 * @param sipID
	 *            the ID of the {@link SIPState}.
	 * @return the {@link SIPState} with id <code>sipID</code>.
	 * 
	 * @throws NoSuchSIPException
	 *             if there is not {@link SIPState} with id <code>sipID</code>.
	 * @throws IngestRegistryException
	 *             if a database exception occurred.
	 */
	public SIPState getSIPState(String sipID) throws NoSuchSIPException,
			IngestRegistryException {

		Connection connection = null;

		try {
			connection = getConnection();

			SIPState sip = getSIP(connection, sipID);

			connection.close();

			if (sip == null) {
				throw new NoSuchSIPException("SIP " + sipID
						+ " could not be found.");
			} else {
				return sip;
			}

		} catch (SQLException e) {

			logger.debug("Exception getting SIP info from database - "
					+ e.getMessage(), e);
			throw new IngestRegistryException(
					"Exception getting SIP info from database - "
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
	 * Returns the number of {@link SIPState}s that respect the specified
	 * filters.
	 * 
	 * @param filter
	 * 
	 * @return the number of {@link SIPState}s.
	 * 
	 * @throws IngestRegistryException
	 */
	public int getSIPStateCount(Filter filter) throws IngestRegistryException {

		SQLContentAdapterEngine<SIPStateAdapter, SIPState> adapterEngine = new SQLContentAdapterEngine<SIPStateAdapter, SIPState>(
				new SIPStateAdapter(), new ContentAdapter(filter, null, null));

		try {

			Connection connection = getConnection();

			int entityCount = adapterEngine.getEntityCount(connection, filter,
					"SIPs");

			connection.close();

			return entityCount;

		} catch (SQLException e) {

			logger.debug("SQLException counting SIPStates - " + e.getMessage(),
					e);

			throw new IngestRegistryException(
					"SQLException counting SIPStates - " + e.getMessage(), e);
		}

	}

	/**
	 * Get the list of {@link SIPState}s that match the given
	 * {@link ContentAdapter}.
	 * 
	 * @param contentAdapter
	 * @return a {@link List} of {@link SIPState}s.
	 * 
	 * @throws IngestRegistryException
	 */
	public List<SIPState> getSIPStates(ContentAdapter contentAdapter)
			throws IngestRegistryException {

		SQLContentAdapterEngine<SIPStateAdapter, SIPState> adapterEngine = new SQLContentAdapterEngine<SIPStateAdapter, SIPState>(
				new SIPStateAdapter(), contentAdapter);

		try {

			Connection connection = getConnection();

			List<SIPState> sipStates = adapterEngine.getEntities(connection,
					"SIPs");

			connection.close();

			return sipStates;

		} catch (SQLException e) {

			logger.debug("SQLException getting SIPStates - " + e.getMessage(),
					e);

			throw new IngestRegistryException(
					"SQLException getting SIPStates - " + e.getMessage(), e);
		}

	}

	/**
	 * Removes a {@link SIPState} from the database.
	 * 
	 * @param sipID
	 *            the ID of the {@link SIPState} to remove.
	 * 
	 * @throws NoSuchSIPException
	 *             if the specified {@link SIPState} doesn't exist.
	 * @throws IngestRegistryException
	 *             if a database exception occurred.
	 */
	public void removeSIPState(String sipID) throws NoSuchSIPException,
			IngestRegistryException {

		Connection connection = null;
		Statement statement = null;

		try {
			connection = getConnection();
			statement = connection.createStatement();

			// Just for testing sipID exists. throws NoSuchSIPException
			getSIP(connection, sipID);

			String deleteQuery = String.format(
					"DELETE FROM SIPs WHERE id='%1$s'", escapeValue(sipID));

			logger.trace("removeSIP(" + sipID + ") SQL Query: " + deleteQuery);

			statement.executeUpdate(deleteQuery);

		} catch (SQLException e) {

			logger.error("SQLException, executing delete - " + e.getMessage(),
					e);
			throw new IngestRegistryException(
					"SQLException, executing delete - " + e.getMessage(), e);

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

	private SIPState getSIP(Connection connection, String sipID)
			throws SQLException, NoSuchSIPException {

		String selectQuery = String.format(
				"SELECT * FROM SIPs WHERE id='%1$s'", escapeValue(sipID));

		SIPState sipState = new SIPStateAdapter().getEntity(connection,
				selectQuery);

		if (sipState == null) {

			throw new NoSuchSIPException("SIP " + sipID + " was not found");

		} else {
			return sipState;
		}

	}

}
