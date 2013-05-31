package pt.gov.dgarq.roda.core.adapter.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.adapter.ContentAdapterEngine;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.LikeFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.RangeFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.RegexFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;

/**
 * This is the {@link ContentAdapterEngine} for the SQL databases.
 * 
 * @author Rui Castro
 * @param <EA>
 *            the entity adapter type.
 * @param <E>
 *            the entity being adapted
 */
public class SQLContentAdapterEngine<EA extends SQLEntityAdapter<E>, E> extends
		ContentAdapterEngine<EA, E> {
	static final private Logger logger = Logger
			.getLogger(SQLContentAdapterEngine.class);

	/**
	 * @param entityAdapter
	 * @param contentAdapter
	 */
	public SQLContentAdapterEngine(EA entityAdapter,
			ContentAdapter contentAdapter) {
		super(entityAdapter, contentAdapter);
	}

	/**
	 * @see ContentAdapterEngine#getFilterParameterAdapter (FilterParameter)
	 */
	public SQLFilterParameterAdapter<EA, E> getFilterParameterAdapter(
			FilterParameter filterParameter) {

		SQLFilterParameterAdapter<EA, E> sqlParameterAdapter = null;

		if (filterParameter == null) {
			// Ignore null parameter
			logger.warn("null FilterParameter found. Ignored.");
		} else if (filterParameter instanceof SimpleFilterParameter) {
			sqlParameterAdapter = new SQLSimpleFilterParameter<EA, E>(
					getEntityAdapter(), (SimpleFilterParameter) filterParameter);
		} else if (filterParameter instanceof RangeFilterParameter) {
			sqlParameterAdapter = new SQLRangeFilterParameter<EA, E>(
					getEntityAdapter(), (RangeFilterParameter) filterParameter);
		} else if (filterParameter instanceof OneOfManyFilterParameter) {
			sqlParameterAdapter = new SQLOneOfManyFilterParameter<EA, E>(
					getEntityAdapter(),
					(OneOfManyFilterParameter) filterParameter);
		} else if (filterParameter instanceof RegexFilterParameter) {
			sqlParameterAdapter = new SQLRegexFilterParameter<EA, E>(
					getEntityAdapter(), (RegexFilterParameter) filterParameter);
		} else if (filterParameter instanceof LikeFilterParameter) {
			sqlParameterAdapter = new SQLLikeFilterParameter<EA, E>(
					getEntityAdapter(), (LikeFilterParameter) filterParameter);
		} else {
			logger.warn("FilterParameters with type "
					+ filterParameter.getClass().getSimpleName()
					+ " are not supported");
		}

		return sqlParameterAdapter;
	}

	/**
	 * @param sqlBaseQuery
	 * @param useDefaultSorter
	 * @return a SQL query
	 */
	public String getSQLQuery(String sqlBaseQuery, boolean useDefaultSorter) {

		String sqlFilterText = getFilterSQLText();
		String sqlSorterText = getSorterSQLText(useDefaultSorter);

		String sqlQuery = sqlBaseQuery;

		if (sqlFilterText != null) {
			sqlQuery += " WHERE " + sqlFilterText;
		}
		if (sqlSorterText != null) {
			sqlQuery += " ORDER BY " + sqlSorterText;
		}

		return sqlQuery;
	}

	/**
	 * @param connection
	 * @param filter
	 * @param tableName
	 * @return the number of entities that match the specified {@link Filter} .
	 * 
	 * @throws SQLException
	 */
	public int getEntityCount(Connection connection, Filter filter,
			String tableName) throws SQLException {

		String query = getSQLQuery("SELECT COUNT(*) FROM " + tableName, false);

		logger.trace("getEntityCount(" + filter + ") SQL Query: " + query);

		Statement statement = null;
		try {

			statement = connection.createStatement();

			ResultSet resultSet = statement.executeQuery(query);

			resultSet.first();
			int count = resultSet.getInt(1);

			statement.close();

			logger.trace("getEntityCount(" + filter + ") => " + count);

			return count;

		} catch (SQLException e) {
			logger.debug("SQLException counting entities - " + e.getMessage());
			throw e;
		} finally {

			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e1) {
				logger.warn("SQLException closing statement - "
						+ e1.getMessage(), e1);
			}
		}
	}

	/**
	 * Returns a {@link List} of entities that match the current content
	 * adapter.
	 * 
	 * @param connection
	 *            the SQL connection to use.
	 * @param table
	 *            the name of the SQL table to query.
	 * 
	 * @return a {@link List} of entities that match the current
	 *         {@link ContentAdapter}.
	 * 
	 * @throws SQLException
	 */
	public List<E> getEntities(Connection connection, String table)
			throws SQLException {

		Statement statement = null;
		try {

			String query = getSQLQuery("SELECT * FROM " + table, true);

			logger.trace("getEntities(" + getContentAdapter() + ") SQL Query: "
					+ query);

			statement = connection.createStatement();

			ResultSet resultSet = statement.executeQuery(query);

			List<E> entities = getResultSetEntities(connection, resultSet);

			logger.debug("getEntities(" + getContentAdapter() + ") => "
					+ entities.size());

			return entities;

		} catch (SQLException e) {
			throw e;
		} finally {
			if (statement != null) {
				statement.close();
			}
			// connection.close();
		}

	}

	protected String getFilterSQLText() {
		String sqlText = null;

		List<String> conditions = new ArrayList<String>();

		if (hasFilter()) {
			for (FilterParameter filterParameter : getFilter().getParameters()) {
				if (filterParameter != null
						&& getEntityAdapter().hasAttribute(
								filterParameter.getName())) {

					SQLFilterParameterAdapter<EA, E> sqlParamAdapter = getFilterParameterAdapter(filterParameter);

					if (sqlParamAdapter != null) {
						String condition = sqlParamAdapter.getSQLCondition();
						if (!StringUtils.isBlank(condition)) {
							conditions.add(condition);
						}
					}
				}
			}
		}

		if (conditions.size() > 0) {

			sqlText = conditions.get(0);

			for (int i = 1; i < conditions.size(); i++) {
				sqlText += " AND " + conditions.get(i);
			}
		}

		return sqlText;
	}

	protected String getSorterSQLText(boolean useDefaultSorter) {
		String sqlText = null;

		List<String> sortFields = new ArrayList<String>();
		if (hasSorter()) {

			for (SortParameter sortParameter : getSorter().getParameters()) {

				if (getEntityAdapter().hasAttribute(sortParameter.getName())) {

					String sortField = getEntityAdapter()
							.getSQLColumnNameForAttribute(
									sortParameter.getName());

					if (sortParameter.isDescending()) {
						sortField += " DESC ";
					} else {
						sortField += " ASC ";
					}

					sortFields.add(sortField);
				}
			}

		}

		if (sortFields.size() > 0) {
			sqlText = sortFields.get(0);
			for (int i = 1; i < sortFields.size(); i++) {
				sqlText += ", " + sortFields.get(i);
			}
		} else if (useDefaultSorter) {
			sqlText = getEntityAdapter().getDefaultSortColumnName() + " DESC ";
		}

		return sqlText;
	}

	private List<E> getResultSetEntities(Connection connection,
			ResultSet resultSet) throws SQLException {
		List<E> entities = new ArrayList<E>();

		boolean hasValueFilters = hasValueFilters();

		if (hasSublist()) {

			// returning a sublist of the results

			int rowCount = 0;
			// Check if the first index exists
			if (resultSet.absolute(getSublist().getFirstElementIndex() + 1)) {

				do {

					if (hasValueFilters) {

						if (filterValue(resultSet)) {
							// adding entity to results, because it passed by
							// the value filters
							entities.add(getEntityAdapter().getEntity(
									connection, resultSet));
							rowCount++;
						} else {
							// value didn't pass the value filters
						}

					} else {

						// adding entity to results
						entities.add(getEntityAdapter().getEntity(connection,
								resultSet));
						rowCount++;
					}

					// add values until we reach the maximum of elements in the
					// sublist
				} while (resultSet.next()
						&& rowCount < getSublist().getMaximumElementCount());

			} else {
				logger
						.debug("firstIndex is out of range. returning results so far");
				// throw new IndexOutOfBoundsException();
			}

		} else {

			// returning all the entities

			while (resultSet.next()) {

				if (hasValueFilters) {

					// filtering value
					if (filterValue(resultSet)) {

						// adding entity to results, because it passed by the
						// value filters
						entities.add(getEntityAdapter().getEntity(connection,
								resultSet));
					} else {
						// value didn't pass the value filters
					}

				} else {

					// adding entity to results
					entities.add(getEntityAdapter().getEntity(connection,
							resultSet));
				}

			}

		}

		return entities;
	}

}
