package pt.gov.dgarq.roda.core.reports;

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
import pt.gov.dgarq.roda.core.data.Attribute;
import pt.gov.dgarq.roda.core.data.LogEntry;
import pt.gov.dgarq.roda.core.data.Report;
import pt.gov.dgarq.roda.core.data.ReportItem;

/**
 * @author Rui Castro
 */
public class ReportAdapter extends SQLEntityAdapter<Report> {
	static final private Logger logger = Logger.getLogger(ReportAdapter.class);

	private static final String[] attributeNames = new String[] { "id", "type",
			"title" };

	private static final String[] sqlColumnNames = new String[] { "id", "type",
			"title" };

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
	public int compare(Report e1, Report e2, String attributeName) {
		return 0;
	}

	/**
	 * Returns the list of attributes for a {@link ReportAdapter}.
	 * 
	 * @return a {@link List} of {@link String} names of the attributes
	 *         supported.
	 */
	public List<String> getAttributeNames() {
		return Arrays.asList(attributeNames);
	}

	/**
	 * Returns the list of names of the SQL columns that match the attributes in
	 * {@link ReportAdapter#getAttributeNames()}.
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
		} else if ("type".equalsIgnoreCase(attributeName)) {
			sqlValue = getSQLString(value);
		} else if ("title".equalsIgnoreCase(attributeName)) {
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
	public Report getEntity(Connection connection, ResultSet resultSet)
			throws SQLException {

		Report report = new Report(resultSet.getString("id"), resultSet
				.getString("type"), resultSet.getString("title"), null, null);

		report.setAttributes(getReportAttributes(connection, report.getId()));
		report.setItems(getReportItems(connection, report.getId()));

		return report;
	}

	private Attribute[] getReportAttributes(Connection connection,
			String reportID) throws SQLException {

		List<Attribute> parameters = new ArrayList<Attribute>();

		Statement statement = null;

		try {
			statement = connection.createStatement();

			String sqlQuery = String.format(
					"SELECT * FROM ReportAttributes WHERE Reports_id=%1$s",
					getSQLString(reportID));

			ResultSet resultSet = statement.executeQuery(sqlQuery);

			while (resultSet.next()) {

				Attribute parameter = new Attribute(
						resultSet.getString("name"), resultSet
								.getString("value"));

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

		return parameters.toArray(new Attribute[parameters.size()]);
	}

	private ReportItem[] getReportItems(Connection connection, String reportID)
			throws SQLException {

		List<ReportItem> attributes = new ArrayList<ReportItem>();

		Statement statement = null;

		try {
			statement = connection.createStatement();

			String sqlQuery = String.format("SELECT * FROM ReportItems "
					+ "WHERE Reports_id=%1$s " + "ORDER BY position",
					getSQLString(reportID));

			ResultSet resultSet = statement.executeQuery(sqlQuery);

			while (resultSet.next()) {
				int position = resultSet.getInt("position");
				attributes.add(position, new ReportItem(resultSet
						.getString("title"), null));
			}

			resultSet.close();

			for (int position = 0; position < attributes.size(); position++) {
				attributes.get(position)
						.setAttributes(
								getReportItemAttributes(connection, reportID,
										position));
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

		return attributes.toArray(new ReportItem[attributes.size()]);
	}

	private Attribute[] getReportItemAttributes(Connection connection,
			String reportID, int reportItemPosition) throws SQLException {

		List<Attribute> attributes = new ArrayList<Attribute>();

		Statement statement = null;

		try {
			statement = connection.createStatement();

			String sqlQuery = String
					.format(
							"SELECT * FROM ReportItemAttributes "
									+ "WHERE Reports_id=%1$s AND ReportItems_position=%2$d "
									+ "ORDER BY ReportItems_position",
							getSQLString(reportID), reportItemPosition);

			ResultSet resultSet = statement.executeQuery(sqlQuery);

			while (resultSet.next()) {

				attributes.add(new Attribute(resultSet.getString("name"),
						resultSet.getString("value")));
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

		return attributes.toArray(new Attribute[attributes.size()]);
	}

}
