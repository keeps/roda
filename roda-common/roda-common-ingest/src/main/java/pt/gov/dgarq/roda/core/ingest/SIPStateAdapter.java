package pt.gov.dgarq.roda.core.ingest;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.gov.dgarq.roda.core.adapter.SortParameterComparator;
import pt.gov.dgarq.roda.core.adapter.sql.SQLEntityAdapter;
import pt.gov.dgarq.roda.core.data.Report;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.data.SIPStateTransition;

/**
 * 
 * @author Rui Castro
 */
public class SIPStateAdapter extends SQLEntityAdapter<SIPState> {

	private static final String[] attributeNames = new String[] { "id",
			"username", "originalfilename", "state", "complete",
			"completePercentage", "parentpid", "ingestedpid", "datetime",
			"processing" };

	private static final String[] sqlColumnNames = new String[] { "id",
			"username", "original_filename", "state", "complete", "percentage",
			"parent_pid", "pid", "datetime", "processing" };

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
	public int compare(SIPState e1, SIPState e2, String attributeName) {
		return 0;
	}

	/**
	 * Returns the list of attributes for a {@link SIPStateAdapter}.
	 * 
	 * @return a {@link List} of {@link String} names of the attributes
	 *         supported.
	 */
	public List<String> getAttributeNames() {
		return Arrays.asList(attributeNames);
	}

	/**
	 * Returns the list of names of the SQL columns that match the attributes in
	 * {@link SIPStateAdapter#getAttributeNames()}.
	 * 
	 * @return a {@link List} of {@link String} names of the SQL columns
	 *         supported.
	 */
	public List<String> getSQLColumnNames() {
		return Arrays.asList(sqlColumnNames);
	}

	/**
	 * Returns the name of the SQL column to sort by default
	 * (SIPStateTransitions.datetime).
	 * 
	 * @return a {@link String} with the value "SIPStateTransitions.datetime";
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

		if ("id".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLString(value);
		} else if ("username".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLString(value);
		} else if ("originalFilename".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLString(value);
		} else if ("state".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLNullableString(value);
		} else if ("complete".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLBoolean(value);
		} else if ("percentage".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLFloat(value);
		} else if ("parentPID".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLNullableString(value);
		} else if ("ingestedPID".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLNullableString(value);
		} else if ("datetime".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLDatetime(value);
		} else if ("processing".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLBoolean(value);
		} else {
			sqlValue = null;
		}

		return sqlValue;
	}

	/**
	 * @see SQLEntityAdapter#getEntity(Connection, ResultSet)
	 */
	@Override
	public SIPState getEntity(Connection connection, ResultSet resultSet)
			throws SQLException {

		SIPState sip = new SIPState(resultSet.getString("id"), resultSet
				.getString("username"), resultSet
				.getString("original_filename"), resultSet.getString("state"),
				null, resultSet.getBoolean("complete"), resultSet
						.getFloat("percentage"), resultSet.getString("pid"),
				resultSet.getString("parent_pid"), resultSet
						.getTimestamp("datetime"), resultSet
						.getBoolean("processing"));

		SIPStateTransition[] stateTransitions = getSIPStateTransitions(
				connection, sip.getId());
		sip.setStateTransitions(stateTransitions);

		return sip;
	}

	private SIPStateTransition[] getSIPStateTransitions(Connection connection,
			String sipID) throws SQLException {

		List<SIPStateTransition> stateTransitions = new ArrayList<SIPStateTransition>();

		Statement statement = null;

		try {
			statement = connection.createStatement();

			String sqlQuery = String
					.format(
							"SELECT * FROM SIPStateTransitions WHERE SIPs_id='%1$s' ORDER BY datetime",
							sipID);

			ResultSet resultSet = statement.executeQuery(sqlQuery);

			while (resultSet.next()) {

				String id = resultSet.getString(1);
				SIPStateTransition stateTransition = new SIPStateTransition(id,
						resultSet.getString("from_state"), resultSet
								.getString("to_state"), resultSet
								.getTimestamp("datetime"), resultSet
								.getString("task_id"), resultSet
								.getBoolean("success"), resultSet
								.getString("description"));

				stateTransitions.add(stateTransition);
			}

		} catch (SQLException e) {

			throw e;

		} finally {

			if (statement != null) {
				statement.close();
			}
		}

		return stateTransitions.toArray(new SIPStateTransition[stateTransitions
				.size()]);
	}

}
