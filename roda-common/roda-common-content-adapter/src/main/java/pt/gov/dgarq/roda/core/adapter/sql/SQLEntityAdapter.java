package pt.gov.dgarq.roda.core.adapter.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.util.DateParser;
import org.w3c.util.InvalidDateException;

import pt.gov.dgarq.roda.core.adapter.SortParameterComparator;

/**
 * This is an abstract adapter for SQL entities.
 * 
 * @author Rui Castro
 * 
 * @param <E>
 *            the entity being adapted.
 */
public abstract class SQLEntityAdapter<E> implements SortParameterComparator<E> {
	static final private Logger logger = Logger
			.getLogger(SQLEntityAdapter.class);

	/**
	 * Returns the list of attributes for the entity.
	 * 
	 * @return a {@link List} of {@link String} names of the attributes
	 *         supported.
	 */
	public abstract List<String> getAttributeNames();

	/**
	 * Returns the list of names of the SQL columns that match the attributes in
	 * {@link SQLEntityAdapter#getAttributeNames()}.
	 * 
	 * @return a {@link List} of {@link String} names of the SQL columns
	 *         supported.
	 */
	public abstract List<String> getSQLColumnNames();

	/**
	 * @return the name of the SQL column to sort by default.
	 */
	public abstract String getDefaultSortColumnName();

	/**
	 * Returns the SQL value for a given attribute value.
	 * 
	 * @param attributeName
	 *            the name of the attribute.
	 * @param value
	 *            the value of the attribute.
	 * @return the attribute value adapted for a SQL query.
	 * 
	 * @deprecated replaced by {@link #getSQLValueForAttribute(String, Object)}
	 *             ???
	 */
	public abstract String getSQLValueForAttribute(String attributeName,
			String value);

	/**
	 * Returns the SQL value for a given attribute value.
	 * 
	 * @param attributeName
	 *            the name of the attribute.
	 * @param value
	 *            the value of the attribute.
	 * @return the attribute value adapted for a SQL query.
	 */
	public abstract String getSQLValueForAttribute(String attributeName,
			Object value);

	/**
	 * Verifies if {@link SQLEntityAdapter} has the given attribute.
	 * 
	 * @param name
	 *            the name of the attribute.
	 * @return <code>true</code> the attribute exists, <code>false</code>
	 *         otherwise.
	 */
	public boolean hasAttribute(String name) {
		return getAttributeNames().contains(name.toLowerCase());
	}

	/**
	 * Returns the SQL column name for a given attribute.
	 * 
	 * @param attributeName
	 *            the name of the attribute.
	 * @return a {@link String} with the name of the attribute adapted for a SQL
	 *         query of <code>null</code> if the attribute doesn't exist.
	 */
	public String getSQLColumnNameForAttribute(String attributeName) {
		if (getAttributeNames().contains(attributeName.toLowerCase())) {
			return getSQLColumnNames().get(
					getAttributeNames().indexOf(attributeName.toLowerCase()));
		} else {
			return null;
		}
	}

	/**
	 * Gets an entity from the given query.
	 * 
	 * @param connection
	 *            the SLQ connection.
	 * @param selectQuery
	 *            the SQL select query.
	 * 
	 * @return an entity or <code>null</code> if the query didn't return any
	 *         result.
	 * 
	 * @throws SQLException
	 */
	public E getEntity(Connection connection, String selectQuery)
			throws SQLException {

		Statement statement = null;

		try {
			statement = connection.createStatement();

			ResultSet resultSet = statement.executeQuery(selectQuery);

			E entity;

			if (resultSet.first()) {

				entity = getEntity(connection, resultSet);

			} else {
				entity = null;
			}

			resultSet.close();

			return entity;

		} catch (SQLException e) {

			throw e;

		} finally {

			if (statement != null) {
				statement.close();
				logger.debug("getEntity() - Statement closed");
			}
		}
	}

	/**
	 * Gets an entity from the given query.
	 * 
	 * @param connection
	 *            the SLQ connection.
	 * @param statement
	 * @param selectQuery
	 *            the SQL select query.
	 * 
	 * @return an entity or <code>null</code> if the query didn't return any
	 *         result.
	 * 
	 * @throws SQLException
	 */
	public E getEntity(Connection connection, PreparedStatement statement)
			throws SQLException {

		try {

			ResultSet resultSet = statement.executeQuery();

			E entity;

			if (resultSet.first()) {

				entity = getEntity(connection, resultSet);

			} else {
				entity = null;
			}

			resultSet.close();

			return entity;

		} catch (SQLException e) {

			throw e;

		} finally {

			if (statement != null) {
				statement.close();
				logger.debug("getEntity() - Statement closed");
			}
		}
	}

	/**
	 * Returns an entity from the given {@link ResultSet}.
	 * 
	 * @param connection
	 *            the SQL {@link Connection} associated with the
	 *            {@link ResultSet}.
	 * 
	 * @param resultSet
	 *            the {@link ResultSet} from where the entity data should be
	 *            taken.
	 * @return the entity being adapted by this adapter.
	 * 
	 * @throws SQLException
	 */
	public abstract E getEntity(Connection connection, ResultSet resultSet)
			throws SQLException;

	protected String getSQLString(String value) {
		if (value != null) {
			return "'" + escapeValue(value) + "'";
		} else {
			return "''";
		}
	}

	protected String getSQLString(Object value) {
		return getSQLString((value != null) ? value.toString() : null);
	}

	/**
	 * Returns the value given as a quoted string for SQL.
	 * 
	 * @param value
	 *            the string value.
	 * 
	 * @return a {@link String} with the quoted value or a {@link String} with
	 *         "NULL" if the given value is <code>null<code>.
	 */
	protected String getSQLNullableString(String value) {
		return quoteValueOrNull(value);
	}

	/**
	 * Returns the value given as a quoted string for SQL.
	 * 
	 * @param value
	 *            the string value.
	 * 
	 * @return a {@link String} with the quoted value or a {@link String} with
	 *         "NULL" if the given value is <code>null<code>.
	 */
	protected String getSQLNullableString(Object value) {
		return quoteValueOrNull(value);
	}

	/**
	 * Returns a SQL string for a date value.
	 * 
	 * @param value
	 *            the datetime
	 * 
	 * @return a {@link String} with the datetime formated for a SQL query.
	 */
	protected String getSQLDatetime(String value) {
		try {
			String toIsoDate = DateParser.getIsoDate(DateParser.parse(value))
					.replace("T", " ").replace("Z", "");

			return "TIMESTAMP '" + toIsoDate + "'";

		} catch (InvalidDateException e) {
			throw new IllegalArgumentException(
					"datetime is not a valid ISO datetime");
		}
	}

	/**
	 * Returns a SQL string for a date value.
	 * 
	 * @param value
	 *            the datetime
	 * 
	 * @return a {@link String} with the datetime formated for a SQL query.
	 */
	protected String getSQLDatetime(Object value) {
		try {
			Date dateValue;
			if (value instanceof Date) {
				dateValue = (Date) value;
			} else {
				dateValue = DateParser.parse(value.toString());
			}

			String toIsoDate = DateParser.getIsoDate(dateValue).replace("T",
					" ").replace("Z", "");

			return "TIMESTAMP '" + toIsoDate + "'";

		} catch (InvalidDateException e) {
			throw new IllegalArgumentException(
					"datetime is not a valid ISO datetime");
		}
	}

	protected String getSQLBoolean(Object value) {
		Boolean booleanValue;
		if (value instanceof Boolean) {
			booleanValue = (Boolean) value;
		} else {
			booleanValue = new Boolean(value.toString());
		}
		return booleanValue.toString();
	}

	protected String getSQLFloat(Object value) {
		Float floatValue;
		if (value instanceof Float) {
			floatValue = (Float) value;
		} else {
			floatValue = new Float(value.toString());
		}
		return floatValue.toString();
	}

	protected String getSQLLong(Object value) {
		Long longValue;
		if (value instanceof Long) {
			longValue = (Long) value;
		} else {
			longValue = new Long(value.toString());
		}
		return longValue.toString();
	}

	protected String getSQLNullableLong(Object value) {
		if (value == null) {
			return "NULL"; //$NON-NLS-1$
		} else {
			return getSQLLong(value);
		}
	}

	/**
	 * Returns the value given as a quoted string for SQL.
	 * 
	 * @param value
	 *            the string value.
	 * @return a {@link String} with the quoted value or NULL if the given value
	 *         is <code>null<code>.
	 */
	private String quoteValueOrNull(String value) {

		if (value != null) {
			return "'" + escapeValue(value) + "'";
		} else {
			return "NULL";
		}
	}

	/**
	 * Returns the value given as a quoted string for SQL.
	 * 
	 * @param value
	 *            the string value.
	 * @return a {@link String} with the quoted value or NULL if the given value
	 *         is <code>null<code>.
	 */
	private String quoteValueOrNull(Object value) {

		if (value != null) {
			return "'" + escapeValue(value.toString()) + "'";
		} else {
			return "NULL";
		}
	}

	private String escapeValue(String value) {
		// logger.trace("Not escaped value: " + value);

		// This backslashes are madness!!!
		// TODO use Matcher.quoteReplacement(s)
		String escapedValue = value.replaceAll("\\\\", "\\\\\\\\");
		escapedValue = escapedValue.replaceAll("'", "\\\\'");

		// logger.trace("Escaped value: " + escapedValue);

		return escapedValue;
	}

}
