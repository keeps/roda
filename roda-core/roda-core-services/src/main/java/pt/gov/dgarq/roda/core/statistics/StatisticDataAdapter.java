package pt.gov.dgarq.roda.core.statistics;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import pt.gov.dgarq.roda.core.adapter.SortParameterComparator;
import pt.gov.dgarq.roda.core.adapter.sql.SQLEntityAdapter;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.data.StatisticData;

/**
 * 
 * @author Rui Castro
 */
public class StatisticDataAdapter extends SQLEntityAdapter<StatisticData> {

	private static final String[] attributeNames = new String[] { "timestamp",
			"type", "value" };

	private static final String[] sqlColumnNames = new String[] { "datetime",
			"type", "value" };

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
	public int compare(StatisticData e1, StatisticData e2, String attributeName) {
		return 0;
	}

	/**
	 * Returns the list of attributes for a {@link StatisticDataAdapter}.
	 * 
	 * @return a {@link List} of {@link String} names of the attributes
	 *         supported.
	 */
	public List<String> getAttributeNames() {
		return Arrays.asList(attributeNames);
	}

	/**
	 * Returns the list of names of the SQL columns that match the attributes in
	 * {@link StatisticDataAdapter#getAttributeNames()}.
	 * 
	 * @return a {@link List} of {@link String} names of the SQL columns
	 *         supported.
	 */
	public List<String> getSQLColumnNames() {
		return Arrays.asList(sqlColumnNames);
	}

	/**
	 * Returns the name of the SQL column to sort by default (timestamp).
	 * 
	 * @return a {@link String} with the value "timestamp";
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

		if ("timestamp".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLDatetime(value);
		} else if ("type".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLString(value);
		} else if ("value".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLNullableString(value);
		} else {
			sqlValue = null;
		}

		return sqlValue;
	}

	/**
	 * @see SQLEntityAdapter#getEntity(Connection, ResultSet)
	 */
	public StatisticData getEntity(Connection connection, ResultSet resultSet)
			throws SQLException {
		return new StatisticData(resultSet.getTimestamp("datetime"), resultSet
				.getString("type"), resultSet.getString("value"));
	}

}
