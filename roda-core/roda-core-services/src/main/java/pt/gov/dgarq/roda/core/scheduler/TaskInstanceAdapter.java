package pt.gov.dgarq.roda.core.scheduler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.adapter.SortParameterComparator;
import pt.gov.dgarq.roda.core.adapter.sql.SQLEntityAdapter;
import pt.gov.dgarq.roda.core.data.PluginInfo;
import pt.gov.dgarq.roda.core.data.PluginParameter;
import pt.gov.dgarq.roda.core.data.StatisticData;
import pt.gov.dgarq.roda.core.data.TaskInstance;

/**
 * 
 * @author Rui Castro
 */
public class TaskInstanceAdapter extends SQLEntityAdapter<TaskInstance> {
	static final private Logger logger = Logger
			.getLogger(TaskInstanceAdapter.class);

	private static final String[] attributeNames = new String[] { "id", "name",
			"description", "username", "state", "startdate", "finishdate",
			"report" };

	private static final String[] sqlColumnNames = new String[] { "id", "name",
			"description", "username", "state", "startDate", "finishDate",
			"report" };

	/**
	 * @see SortParameterComparator#canSortEntities()
	 */
	public boolean canSortEntities() {
		return false;
	}

	/**
	 * @param e1
	 * @param e2
	 * @param attributeName
	 * 
	 * @return always returns 0;
	 * 
	 * @see SortParameterComparator#compare(Object, Object, String)
	 */
	public int compare(TaskInstance e1, TaskInstance e2, String attributeName) {
		return 0;
	}

	/**
	 * Returns the list of attributes for a {@link TaskInstanceAdapter}.
	 * 
	 * @return a {@link List} of {@link String} names of the attributes
	 *         supported.
	 */
	public List<String> getAttributeNames() {
		return Arrays.asList(attributeNames);
	}

	/**
	 * Returns the list of names of the SQL columns that match the attributes in
	 * {@link TaskInstanceAdapter#getAttributeNames()}.
	 * 
	 * @return a {@link List} of {@link String} names of the SQL columns
	 *         supported.
	 */
	public List<String> getSQLColumnNames() {
		return Arrays.asList(sqlColumnNames);
	}

	/**
	 * Returns the name of the SQL column to sort by default (startDate).
	 * 
	 * @return a {@link String} with the value "startDate";
	 */
	public String getDefaultSortColumnName() {
		return "startDate";
	}

	/**
	 * Returns the SQL value for a given attribute value.
	 * 
	 * @param attributeName
	 *            the name of the attribute.
	 * @param value
	 *            the value of the attribute.
	 * @return the attribute value adapted for a SQL query.
	 */
	public String getSQLValueForAttribute(String attributeName, String value) {
		return getSQLValueForAttribute(attributeName, (Object) value);
	}

	/**
	 * Returns the SQL value for a given attribute value.
	 * 
	 * @param attributeName
	 *            the name of the attribute.
	 * @param value
	 *            the value of the attribute.
	 * @return the attribute value adapted for a SQL query.
	 */
	public String getSQLValueForAttribute(String attributeName, Object value) {
		String sqlValue = null;

		if ("id".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLString(value);
		} else if ("name".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLString(value);
		} else if ("description".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLNullableString(value);
		} else if ("username".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLString(value);
		} else if ("state".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLString(value);
		} else if ("startDate".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLDatetime(value);
		} else if ("finishDate".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLDatetime(value);
		} else if ("report".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLNullableString(value);
		} else {
			sqlValue = null;
		}

		return sqlValue;
	}

	/**
	 * @see SQLEntityAdapter#getEntity(Connection, ResultSet)
	 */
	@Override
	public TaskInstance getEntity(Connection connection, ResultSet resultSet)
			throws SQLException {
		PluginInfo pluginInfo = new PluginInfo(
				resultSet.getString("plugin_id"), resultSet
						.getString("plugin_name"), resultSet
						.getFloat("plugin_version"), resultSet
						.getString("plugin_description"), null);

		TaskInstance taskInstance = new TaskInstance(resultSet.getString("id"),
				resultSet.getString("name"),
				resultSet.getString("description"), resultSet
						.getString("username"), pluginInfo, resultSet
						.getString("state"), resultSet
						.getFloat("completePercentage"), resultSet
						.getTimestamp("startDate"), resultSet
						.getTimestamp("finishDate"), resultSet
						.getString("report"));

		PluginParameter[] parameters = getTaskInstanceParameters(connection,
				taskInstance.getId());
		pluginInfo.setParameters(parameters);

		return taskInstance;
	}

	private PluginParameter[] getTaskInstanceParameters(Connection connection,
			String taskInstanceID) throws SQLException {

		List<PluginParameter> parameters = new ArrayList<PluginParameter>();

		Statement statement = null;

		try {
			statement = connection.createStatement();

			String sqlQuery = String
					.format(
							"SELECT * FROM TaskInstanceParameters WHERE TaskInstances_id=%1$s",
							getSQLString(taskInstanceID));

			ResultSet resultSet = statement.executeQuery(sqlQuery);

			while (resultSet.next()) {

				PluginParameter parameter = new PluginParameter(resultSet
						.getString("name"), resultSet.getString("type"),
						resultSet.getString("value"), resultSet
								.getBoolean("mandatory"), resultSet
								.getBoolean("readonly"), resultSet
								.getString("description"));

				parameters.add(parameter);
			}

		} catch (SQLException e) {

			throw e;

		} finally {

			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e) {
				logger.warn("SQLException closing statement - "
						+ e.getMessage(), e);
			}

		}

		return parameters.toArray(new PluginParameter[parameters.size()]);
	}

}
