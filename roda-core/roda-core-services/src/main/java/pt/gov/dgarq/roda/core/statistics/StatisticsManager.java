package pt.gov.dgarq.roda.core.statistics;

import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.RodaWebApplication;
import pt.gov.dgarq.roda.core.common.StatisticsException;
import pt.gov.dgarq.roda.core.data.StatisticData;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.services.Statistics;

/**
 * This class implements the RODA Statistics Manager.
 * 
 * @author Rui Castro
 */
public class StatisticsManager {

	static final private Logger logger = Logger
			.getLogger(StatisticsManager.class);

	static private StatisticsManager defaultStatisticsManager = null;

	/**
	 * Returns the default {@link StatisticsManager}. If it doesn't exist, a new
	 * {@link StatisticsManager} it will be created and returned.
	 * 
	 * @return a {@link StatisticsManager}.
	 * 
	 * @throws StatisticsException
	 *             if the {@link StatisticsManager} couldn't be created.
	 */
	public static StatisticsManager getDefaultStatisticsManager()
			throws StatisticsException {
		if (defaultStatisticsManager == null) {
			defaultStatisticsManager = new StatisticsManager();
		}
		return defaultStatisticsManager;
	}

	private StatisticsDatabaseUtility databaseUtility = null;

	/**
	 * Creates a new {@link StatisticsManager}.
	 * 
	 * @throws StatisticsException
	 */
	private StatisticsManager() throws StatisticsException {

		try {

			Configuration configuration = RodaWebApplication.getConfiguration(
					getClass(), "statistics.properties");

			String jdbcDriver = configuration.getString("jdbcDriver");
			String jdbcURL = configuration.getString("jdbcURL");
			String jdbcUsername = configuration.getString("jdbcUsername");
			String jdbcPassword = configuration.getString("jdbcPassword");

			if (databaseUtility == null) {
				databaseUtility = new StatisticsDatabaseUtility(jdbcDriver,
						jdbcURL, jdbcUsername, jdbcPassword);
			}

		} catch (ConfigurationException e) {
			logger.error(
					"Error reading configuration file - " + e.getMessage(), e);
			throw new StatisticsException("Error reading configuration file - "
					+ e.getMessage(), e);
		} catch (StatisticsException e) {
			logger.error("Error creating StatisticsDatabaseUtility - "
					+ e.getMessage(), e);
			throw new StatisticsException(
					"Error creating StatisticsDatabaseUtility - "
							+ e.getMessage(), e);
		}

		logger.info(getClass().getSimpleName() + " initialised OK");
	}

	/**
	 * Inserts a new {@link Statistics} into the database.
	 * 
	 * @param statisticData
	 *            the {@link StatisticData} to insert.
	 * 
	 * @throws StatisticsException
	 *             if a database exception occurred.
	 */
	public void insertStatisticData(StatisticData statisticData)
			throws StatisticsException {
		this.databaseUtility.insertStatisticData(statisticData);
	}

	/**
	 * Inserts a {@link List} of {@link StatisticData} into the database.
	 * 
	 * @param statisticData
	 *            the {@link StatisticData} to insert.
	 * 
	 * @throws StatisticsException
	 *             if a database exception occurred.
	 */
	public void insertStatisticData(List<StatisticData> statisticData)
			throws StatisticsException {
		this.databaseUtility.insertStatisticData(statisticData);
	}

	/**
	 * Returns the number of {@link StatisticData} that respect the specified
	 * filters.
	 * 
	 * @param contentAdapterFilter
	 * 
	 * @return the number of {@link Statistics}s.
	 * 
	 * @throws StatisticsException
	 */
	public int getStatisticDataCount(Filter contentAdapterFilter)
			throws StatisticsException {
		return this.databaseUtility.getStatisticDataCount(contentAdapterFilter);
	}

	/**
	 * * Returns a list of {@link StatisticData}s matching the
	 * {@link ContentAdapter} specified.
	 * 
	 * @param contentAdapter
	 * 
	 * @return a {@link List} of {@link StatisticData}.
	 * 
	 * @throws StatisticsException
	 */
	public List<StatisticData> getStatisticData(ContentAdapter contentAdapter)
			throws StatisticsException {
		return this.databaseUtility.getStatisticData(contentAdapter);
	}

}
