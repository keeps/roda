package pt.gov.dgarq.roda.core.scheduler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.adapter.sql.SQLContentAdapterEngine;
import pt.gov.dgarq.roda.core.common.NoSuchTaskInstanceException;
import pt.gov.dgarq.roda.core.data.PluginParameter;
import pt.gov.dgarq.roda.core.data.TaskInstance;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.db.DatabaseUtility;

/**
 * This class provides methods to manipulate the task instances registry
 * database.
 * 
 * @author Rui Castro
 */
public class TaskInstanceDatabaseUtility extends
		DatabaseUtility<TaskInstanceAdapter, TaskInstance> {
	static final private Logger logger = Logger
			.getLogger(TaskInstanceDatabaseUtility.class);

	/**
	 * @param jdbcDriver
	 * @param jdbcURL
	 * @param jdbcUsername
	 * @param jdbcPassword
	 * 
	 * @throws TaskInstanceRegistryException
	 */
	public TaskInstanceDatabaseUtility(String jdbcDriver, String jdbcURL,
			String jdbcUsername, String jdbcPassword)
			throws TaskInstanceRegistryException {

		super();

		try {
			setJdbcDriver(jdbcDriver);
			setJdbcParameters(jdbcURL, jdbcUsername, jdbcPassword);

		} catch (SQLException e) {
			throw new TaskInstanceRegistryException(
					"SQLException initializing " + getClass().getSimpleName()
							+ " - " + e.getMessage(), e);
		}
	}

	/**
	 * Inserts a new {@link TaskInstance} into the database.
	 * 
	 * @param taskInstance
	 *            the {@link TaskInstance} to insert.
	 * 
	 * 
	 * @return the inserted {@link TaskInstance}.
	 * 
	 * @throws TaskInstanceRegistryException
	 *             if a database exception occurred.
	 */
	public TaskInstance insertTaskInstance(TaskInstance taskInstance)
			throws TaskInstanceRegistryException {

		Connection connection = null;
		PreparedStatement statement = null;
		String query = "INSERT INTO TaskInstances ("
				+ "id, name, description, username, state, "
				+ "completePercentage, startDate, finishDate, " + "report, "
				+ "plugin_id, plugin_name, plugin_version, plugin_description"
				+ ") VALUES (" + "?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?" + ")";
		try {

			connection = getConnection();
			connection.setAutoCommit(false);

			String newTaskInstanceID = getNextID("TaskInstances_id");
			taskInstance.setId(newTaskInstanceID);

			statement = connection.prepareStatement(query);
			statement.setString(1, taskInstance.getId());
			statement.setString(2, taskInstance.getName());
			statement.setString(3, taskInstance.getDescription());
			statement.setString(4, taskInstance.getUsername());
			statement.setString(5, taskInstance.getState());
			statement.setFloat(6, taskInstance.getCompletePercentage());
			if (taskInstance.getStartDate() == null) {
				statement.setString(7, null);
			} else {
				statement.setTimestamp(7, new Timestamp(taskInstance
						.getStartDate().getTime()));
			}
			if (taskInstance.getFinishDate() == null) {
				statement.setString(8, null);
			} else {
				statement.setTimestamp(8, new Timestamp(taskInstance
						.getFinishDate().getTime()));
			}
			statement.setString(9, taskInstance.getReportID());
			statement.setString(10, taskInstance.getPluginInfo().getId());
			statement.setString(11, taskInstance.getPluginInfo().getName());
			statement.setFloat(12, taskInstance.getPluginInfo().getVersion());
			statement.setString(13, taskInstance.getPluginInfo()
					.getDescription());

			logger.trace("Executing insert query " + statement);

			statement.executeUpdate();

			for (PluginParameter parameter : taskInstance.getPluginInfo()
					.getParameters()) {
				insertTaskInstanceParameter(connection, taskInstance.getId(),
						parameter);
			}

			connection.commit();

		} catch (SQLException e) {

			try {
				if (connection != null) {
					connection.rollback();
				}
			} catch (SQLException e1) {
				logger.warn(
						"SQLException executing rollback - " + e.getMessage(),
						e1);
			}

			logger.error(
					"SQLException inserting new TaskInstance - "
							+ e.getMessage(), e);
			throw new TaskInstanceRegistryException(
					"SQLException inserting new TaskInstance - "
							+ e.getMessage(), e);

		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e) {
				logger.warn(
						"SQLException closing statement - " + e.getMessage(), e);
			}
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				logger.warn(
						"SQLException closing connection - " + e.getMessage(),
						e);
			}
		}

		try {

			return getTaskInstance(taskInstance.getId());

		} catch (NoSuchTaskInstanceException e) {
			throw new TaskInstanceRegistryException(
					"Error getting new TaskInstance - " + e.getMessage(), e);
		}

	}

	/**
	 * Updates a {@link TaskInstance}.
	 * 
	 * @param taskInstance
	 *            the {@link TaskInstance} to update.
	 * 
	 * @return the updated {@link TaskInstance}.
	 * 
	 * @throws TaskInstanceRegistryException
	 */
	public TaskInstance updateTaskInstance(TaskInstance taskInstance)
			throws TaskInstanceRegistryException {

		Connection connection = null;
		PreparedStatement statement = null;
		try {

			connection = getConnection();
			connection.setAutoCommit(false);

			String query = "UPDATE TaskInstances SET "
					+ "name=?, description=?, username=?, state=?, "
					+ "completePercentage=?, startDate=?, finishDate=?, "
					+ "report=?, "
					+ "plugin_id=?, plugin_name=?, plugin_version=?, plugin_description=?"
					+ " WHERE id=?";
			statement = connection.prepareStatement(query);

			statement.setString(1, taskInstance.getName());
			statement.setString(2, taskInstance.getDescription());
			statement.setString(3, taskInstance.getUsername());
			statement.setString(4, taskInstance.getState());
			statement.setFloat(5, taskInstance.getCompletePercentage());
			if (taskInstance.getStartDate() == null) {
				statement.setString(6, null);
			} else {
				statement.setTimestamp(6, new Timestamp(taskInstance
						.getStartDate().getTime()));
			}
			if (taskInstance.getFinishDate() == null) {
				statement.setString(7, null);
			} else {
				statement.setTimestamp(7, new Timestamp(taskInstance
						.getFinishDate().getTime()));
			}
			statement.setString(8, taskInstance.getReportID());
			statement.setString(9, taskInstance.getPluginInfo().getId());
			statement.setString(10, taskInstance.getPluginInfo().getName());
			statement.setFloat(11, taskInstance.getPluginInfo().getVersion());
			statement.setString(12, taskInstance.getPluginInfo()
					.getDescription());
			statement.setString(13, taskInstance.getId());

			logger.trace("Executing update query " + statement);

			statement.executeUpdate();

			for (PluginParameter parameter : taskInstance.getPluginInfo()
					.getParameters()) {
				updateTaskInstanceParameter(connection, taskInstance.getId(),
						parameter);
			}

			connection.commit();

		} catch (SQLException e) {

			try {
				if (connection != null) {
					connection.rollback();
				}
			} catch (SQLException e1) {
				logger.warn(
						"SQLException executing rollback - " + e.getMessage(),
						e1);
			}

			logger.error(
					"SQLException updating TaskInstance - " + e.getMessage(), e);
			throw new TaskInstanceRegistryException(
					"SQLException updating TaskInstance - " + e.getMessage(), e);

		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e) {
				logger.warn(
						"SQLException closing statement - " + e.getMessage(), e);
			}
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				logger.warn(
						"SQLException closing connection - " + e.getMessage(),
						e);
			}
		}

		try {

			return getTaskInstance(taskInstance.getId());

		} catch (NoSuchTaskInstanceException e) {
			throw new TaskInstanceRegistryException(
					"Error getting updated TaskInstance - " + e.getMessage(), e);
		}
	}

	/**
	 * Gets {@link TaskInstance} with the given ID.
	 * 
	 * @param taskInstanceID
	 *            the ID of the {@link TaskInstance}.
	 * 
	 * @return the {@link TaskInstance} with id <code>taskInstanceID</code>.
	 * 
	 * @throws NoSuchTaskInstanceException
	 *             if there is not {@link TaskInstance} with id
	 *             <code>taskInstanceID</code>.
	 * 
	 * @throws TaskInstanceRegistryException
	 *             if a database exception occurred.
	 */
	public TaskInstance getTaskInstance(String taskInstanceID)
			throws NoSuchTaskInstanceException, TaskInstanceRegistryException {

		Connection connection = null;

		try {

			connection = getConnection();

			String selectQuery = String.format(
					"SELECT * FROM TaskInstances WHERE id='%1$s'",
					escapeValue(taskInstanceID));

			TaskInstance taskInstance = new TaskInstanceAdapter().getEntity(
					connection, selectQuery);

			connection.close();

			if (taskInstance == null) {

				throw new NoSuchTaskInstanceException("TaskInstance "
						+ taskInstanceID + " could not be found.");

			} else {
				return taskInstance;
			}

		} catch (SQLException e) {

			logger.debug("Exception getting TaskInstance info from database - "
					+ e.getMessage(), e);
			throw new TaskInstanceRegistryException(
					"Exception getting TaskInstance info from database - "
							+ e.getMessage(), e);

		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				logger.warn(
						"SQLException closing connection - " + e.getMessage(),
						e);
			}
		}
	}

	/**
	 * Returns the number of {@link TaskInstance}s that respect the specified
	 * filters.
	 * 
	 * @param filter
	 * 
	 * @return the number of {@link TaskInstance}s.
	 * 
	 * @throws TaskInstanceRegistryException
	 */
	public int getTaskInstancesCount(Filter filter)
			throws TaskInstanceRegistryException {

		SQLContentAdapterEngine<TaskInstanceAdapter, TaskInstance> adapterEngine = new SQLContentAdapterEngine<TaskInstanceAdapter, TaskInstance>(
				new TaskInstanceAdapter(), new ContentAdapter(filter, null,
						null));

		try {

			Connection connection = getConnection();

			int entityCount = adapterEngine.getEntityCount(connection, filter,
					"TaskInstances");

			connection.close();

			return entityCount;

		} catch (SQLException e) {

			logger.debug(
					"SQLException counting TaskInstances - " + e.getMessage(),
					e);

			throw new TaskInstanceRegistryException(
					"SQLException counting TaskInstances - " + e.getMessage(),
					e);
		}
	}

	/**
	 * Get the list of {@link TaskInstance}s that match the given
	 * {@link ContentAdapter}.
	 * 
	 * @param contentAdapter
	 * @return a {@link List} of {@link TaskInstance}s.
	 * 
	 * @throws TaskInstanceRegistryException
	 */
	public List<TaskInstance> getTaskInstances(ContentAdapter contentAdapter)
			throws TaskInstanceRegistryException {

		SQLContentAdapterEngine<TaskInstanceAdapter, TaskInstance> adapterEngine = new SQLContentAdapterEngine<TaskInstanceAdapter, TaskInstance>(
				new TaskInstanceAdapter(), contentAdapter);

		try {

			Connection connection = getConnection();

			List<TaskInstance> entities = adapterEngine.getEntities(connection,
					"TaskInstances");

			connection.close();

			return entities;

		} catch (SQLException e) {

			logger.debug(
					"SQLException getting TaskInstances - " + e.getMessage(), e);

			throw new TaskInstanceRegistryException(
					"SQLException getting TaskInstances - " + e.getMessage(), e);
		}

	}

	/**
	 * Removes a {@link TaskInstance} from the database.
	 * 
	 * @param taskInstanceID
	 *            the ID of the {@link TaskInstance} to remove.
	 * 
	 * @throws NoSuchTaskInstanceException
	 *             if the specified {@link TaskInstance} doesn't exist.
	 * 
	 * @throws TaskInstanceRegistryException
	 *             if a database exception occurred.
	 */
	public void removeTaskInstance(String taskInstanceID)
			throws NoSuchTaskInstanceException, TaskInstanceRegistryException {

		Connection connection = null;
		Statement statement = null;

		try {
			connection = getConnection();
			statement = connection.createStatement();

			// Just for testing taskInstanceID exists. throws
			// NoSuchTaskInstanceException
			getTaskInstance(taskInstanceID);

			String deleteQuery = String.format(
					"DELETE FROM TaskInstances WHERE id='%1$s'",
					escapeValue(taskInstanceID));

			logger.trace("executing delete query " + deleteQuery);

			statement.executeUpdate(deleteQuery);

		} catch (SQLException e) {

			logger.error("SQL Exception, executing delete - " + e.getMessage(),
					e);
			throw new TaskInstanceRegistryException(
					"SQL Exception, executing delete - " + e.getMessage(), e);

		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e) {
				logger.warn(
						"SQLException closing statement - " + e.getMessage(), e);
			}
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				logger.warn(
						"SQLException closing connection - " + e.getMessage(),
						e);
			}
		}

	}

	/**
	 * Inserts a new {@link PluginParameter} into the database.
	 * 
	 * @param connection
	 *            the current SQL connection.
	 * @param taskInstanceID
	 *            the ID of the {@link TaskInstance} related with this
	 *            parameter.
	 * @param parameter
	 *            the {@link PluginParameter} to insert.
	 * 
	 * @throws SQLException
	 *             if a database exception occurred.
	 */
	private void insertTaskInstanceParameter(Connection connection,
			String taskInstanceID, PluginParameter parameter)
			throws SQLException {

		Statement statement = connection.createStatement();

		String insertQuery = String
				.format("INSERT INTO TaskInstanceParameters "
						+ "( TaskInstances_id, name, type, value, mandatory, readonly, description )"
						+ " VALUES "
						+ " ( %1$s, %2$s, %3$s, %4$s, %5$b, %6$b, %7$s )",

				taskInstanceID, quoteValueOrNull(parameter.getName()),
						quoteValueOrNull(parameter.getType()),
						quoteValueOrNull(parameter.getValue()),
						parameter.isMandatory(), parameter.isReadonly(),
						quoteValueOrNull(parameter.getDescription()));

		logger.trace("executing insert query " + insertQuery);

		statement.executeUpdate(insertQuery);
	}

	private void updateTaskInstanceParameter(Connection connection,
			String taskInstanceID, PluginParameter parameter)
			throws SQLException {

		Statement statement = connection.createStatement();

		String insertQuery = String
				.format("UPDATE TaskInstanceParameters SET "
						+ "type=%3$s, value=%4$s, mandatory=%5$b, readonly=%6$b, description=%7$s "
						+ " WHERE TaskInstances_id=%1$s AND name=%2$s",
						taskInstanceID, quoteValueOrNull(parameter.getName()),
						quoteValueOrNull(parameter.getType()),
						quoteValueOrNull(parameter.getValue()),
						parameter.isMandatory(), parameter.isReadonly(),
						quoteValueOrNull(parameter.getDescription()));

		logger.trace("executing update query " + insertQuery);

		statement.executeUpdate(insertQuery);
	}

}
