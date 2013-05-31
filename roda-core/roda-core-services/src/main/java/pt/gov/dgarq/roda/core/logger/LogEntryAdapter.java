package pt.gov.dgarq.roda.core.logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.gov.dgarq.roda.core.adapter.SortParameterComparator;
import pt.gov.dgarq.roda.core.adapter.sql.SQLEntityAdapter;
import pt.gov.dgarq.roda.core.data.LogEntry;
import pt.gov.dgarq.roda.core.data.LogEntryParameter;
import pt.gov.dgarq.roda.core.data.SimpleRepresentationPreservationObject;

/**
 * 
 * @author Rui Castro
 */
public class LogEntryAdapter extends SQLEntityAdapter<LogEntry> {

	private static final String[] attributeNames = new String[] { "username",
			"action", "address", "datetime", "object", "duration" };

	private static final String[] sqlColumnNames = new String[] { "username",
			"action", "address", "datetime", "relatedobjectpid", "duration" };

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
	public int compare(LogEntry e1, LogEntry e2, String attributeName) {
		return 0;
	}

	/**
	 * Returns the list of attributes for a {@link LogEntry}.
	 * 
	 * @return a {@link List} of {@link String} names of the attributes
	 *         supported.
	 */
	public List<String> getAttributeNames() {
		return Arrays.asList(attributeNames);
	}

	/**
	 * Returns the list of names of the SQL columns that match the attributes in
	 * {@link LogEntryAdapter#getAttributeNames()}.
	 * 
	 * @return a {@link List} of {@link String} names of the SQL columns
	 *         supported.
	 */
	public List<String> getSQLColumnNames() {
		return Arrays.asList(sqlColumnNames);
	}

	/**
	 * Returns the name of the SQL column to sort by default (datetime).
	 * 
	 * @return a {@link String} with the value "datetime";
	 */
	public String getDefaultSortColumnName() {
		return "datetime";
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

		if ("username".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLString(value);
		} else if ("action".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLString(value);
		} else if ("address".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLNullableString(value);
		} else if ("relatedobjectpid".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLNullableString(value);
		} else if ("datetime".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLDatetime(value);
		} else if ("duration".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLNullableLong(value);
		} else {
			sqlValue = null;
		}

		return sqlValue;
	}

	/**
	 * @see SQLEntityAdapter#getEntity(Connection, ResultSet)
	 */
	@Override
	public LogEntry getEntity(Connection connection, ResultSet resultSet)
			throws SQLException {

		LogEntry entry = new LogEntry(resultSet.getString("id"), resultSet
				.getString("address"), resultSet.getString("datetime"),
				resultSet.getString("username"), resultSet.getString("action"),
				null, resultSet.getString("description"), resultSet
						.getString("object"), resultSet.getLong("duration"));

		LogEntryParameter[] parameters = getLogActionParameters(connection,
				entry.getId());
		entry.setParameters(parameters);

		return entry;
	}

	private LogEntryParameter[] getLogActionParameters(Connection connection,
			String logEntryID) throws SQLException {

		String query = String
				.format(
						"SELECT * FROM LogParameters WHERE Logs_id=%1$s ORDER BY position",
						getSQLString(logEntryID));

		LogEntryParameter[] parameters = null;

		Statement statement = connection.createStatement();

		ResultSet resultSet = statement.executeQuery(query);

		Map<Integer, LogEntryParameter> parametersTemp = new HashMap<Integer, LogEntryParameter>();

		while (resultSet.next()) {

			parametersTemp.put(new Integer(resultSet.getInt("position")),
					new LogEntryParameter(resultSet.getString("name"),
							resultSet.getString("value")));

		}
		resultSet.close();
		statement.close();

		// Put parameters in order inside array.
		parameters = new LogEntryParameter[parametersTemp.size()];

		for (Integer order : parametersTemp.keySet()) {
			parameters[order.intValue()] = parametersTemp.get(order);
		}

		statement.close();

		return parameters;
	}

}
