package pt.gov.dgarq.roda.core.statistics;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.adapter.sql.SQLContentAdapterEngine;
import pt.gov.dgarq.roda.core.common.StatisticsException;
import pt.gov.dgarq.roda.core.data.StatisticData;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.db.DatabaseUtility;

/**
 * @author Rui Castro
 */
public class StatisticsDatabaseUtility extends
		DatabaseUtility<StatisticDataAdapter, StatisticData> {

	static final private Logger logger = Logger
			.getLogger(StatisticsDatabaseUtility.class);

	/**
	 * @param jdbcDriver
	 * @param jdbcURL
	 * @param jdbcUsername
	 * @param jdbcPassword
	 * 
	 * @throws StatisticsException
	 */
	public StatisticsDatabaseUtility(String jdbcDriver, String jdbcURL,
			String jdbcUsername, String jdbcPassword)
			throws StatisticsException {
		super();

		try {

			setJdbcDriver(jdbcDriver);
			setJdbcParameters(jdbcURL, jdbcUsername, jdbcPassword);

		} catch (SQLException e) {
			throw new StatisticsException("SQLException initializing "
					+ getClass().getSimpleName() + " - " + e.getMessage(), e);
		}
	}

	/**
	 * Adds a new {@link StatisticData}.
	 * 
	 * @param statisticData
	 * 
	 * @throws StatisticsException
	 */
	public void insertStatisticData(StatisticData statisticData)
			throws StatisticsException {

		Connection connection = null;
		try {

			connection = getConnection();
			connection.setAutoCommit(true);

			addStatisticData(connection, statisticData);

			connection.close();

		} catch (SQLException e) {
			logger.debug("An error occured. " + "The SQLException message is: "
					+ e.getMessage(), e);
			throw new StatisticsException("An error occured. "
					+ "The SQLException message is: " + e.getMessage(), e);
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
	 * Adds a new {@link StatisticData}.
	 * 
	 * @param statisticData
	 * 
	 * @throws StatisticsException
	 */
	public void insertStatisticData(List<StatisticData> statisticData)
			throws StatisticsException {

		Connection connection = null;
		try {

			connection = getConnection();
			connection.setAutoCommit(false);

			for (StatisticData statisticDataItem : statisticData) {
				addStatisticData(connection, statisticDataItem);
			}

			connection.commit();
			connection.close();

		} catch (SQLException e) {
			logger.debug("An error occured. " + "The SQLException message is: "
					+ e.getMessage(), e);
			throw new StatisticsException("An error occured. "
					+ "The SQLException message is: " + e.getMessage(), e);
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
	 * @param filter
	 * @return the number of statistics data that match the specified
	 *         {@link Filter} .
	 * @throws StatisticsException
	 */
	public int getStatisticDataCount(Filter filter) throws StatisticsException {

		SQLContentAdapterEngine<StatisticDataAdapter, StatisticData> adapterEngine = new SQLContentAdapterEngine<StatisticDataAdapter, StatisticData>(
				new StatisticDataAdapter(), new ContentAdapter(filter, null,
						null));
		try {

			Connection connection = getConnection();

			int entitiesCount = adapterEngine.getEntityCount(connection,
					filter, "StatisticData");

			connection.close();

			return entitiesCount;

		} catch (SQLException e) {

			logger.debug("SQLException counting StatisticData - "
					+ e.getMessage(), e);

			throw new StatisticsException(
					"SQLException counting StatisticData - " + e.getMessage(),
					e);
		}

	}

	/**
	 * @param contentAdapter
	 * 
	 * @return a {@link List} of {@link StatisticData} that match the specified
	 *         {@link ContentAdapter}.
	 * 
	 * @throws StatisticsException
	 */
	public List<StatisticData> getStatisticData(ContentAdapter contentAdapter)
			throws StatisticsException {

		SQLContentAdapterEngine<StatisticDataAdapter, StatisticData> adapterEngine = new SQLContentAdapterEngine<StatisticDataAdapter, StatisticData>(
				new StatisticDataAdapter(), contentAdapter);

		try {
			Connection connection = getConnection();

			List<StatisticData> statistics = adapterEngine.getEntities(
					connection, "StatisticData");

			connection.close();

			return statistics;

		} catch (SQLException e) {

			logger.debug("SQLException getting StatisticData - "
					+ e.getMessage(), e);

			throw new StatisticsException(
					"SQLException getting StatisticData - " + e.getMessage(), e);
		}

	}

	/**
	 * Adds a new {@link StatisticData}.
	 * 
	 * @param statisticData
	 * 
	 * @throws SQLException
	 */
	private void addStatisticData(Connection connection,
			StatisticData statisticData) throws SQLException {

		StatisticDataAdapter dataAdapter = new StatisticDataAdapter();

		Statement statement = connection.createStatement();

		String query = String.format(
				"INSERT INTO StatisticData (datetime, type, value) "
						+ "VALUES (%1$s, %2$s, %3$s)", dataAdapter
						.getSQLValueForAttribute("timestamp", statisticData
								.getTimestamp()), dataAdapter
						.getSQLValueForAttribute("type", statisticData
								.getType()), dataAdapter
						.getSQLValueForAttribute("value", statisticData
								.getValue()));

		logger.trace("addStatisticData(" + statisticData + ") SQL Query: "
				+ query);

		@SuppressWarnings("unused")
		int affectedRows = statement.executeUpdate(query);

		// connection.commit();

		logger.trace("Added " + statisticData);

		if (statement != null) {
			statement.close();
		}

	}

}
