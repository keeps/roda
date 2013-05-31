package pt.gov.dgarq.roda.core.services;

import java.util.Arrays;
import java.util.Date;

import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.common.StatisticsException;
import pt.gov.dgarq.roda.core.data.StatisticData;
import pt.gov.dgarq.roda.core.statistics.StatisticsManager;

/**
 * This class implements Statistics service.
 * 
 * @author Rui Castro
 */
public class Statistics extends RODAWebService {

	static final private org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(Statistics.class);

	private StatisticsManager statisticsManager = null;

	/**
	 * Constructs a new {@link Statistics}.
	 * 
	 * @throws StatisticsException
	 * @throws RODAServiceException
	 */
	public Statistics() throws StatisticsException, RODAServiceException {
		super();

		try {

			statisticsManager = StatisticsManager.getDefaultStatisticsManager();

		} catch (StatisticsException e) {
			logger.debug("Error getting default Statistics Manager - "
					+ e.getMessage(), e);
			throw new StatisticsException(
					"Error getting default Statistics Manager - "
							+ e.getMessage(), e);
		}

		logger.info(getClass().getSimpleName() + " init OK");
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

		Date start = new Date();
		this.statisticsManager.insertStatisticData(statisticData);
		long duration = new Date().getTime() - start.getTime();

		registerAction("Statistics.insertStatisticData", new String[] {
				"statisticData", "" + statisticData },
				"User %username% called method Statistics.insertStatisticData("
						+ statisticData + ")", duration);

	}

	/**
	 * Inserts a list of {@link StatisticData} into the database.
	 * 
	 * @param statisticData
	 *            the {@link StatisticData} to insert.
	 * 
	 * @throws StatisticsException
	 *             if a database exception occurred.
	 */
	public void insertStatisticDataList(StatisticData[] statisticData)
			throws StatisticsException {

		Date start = new Date();
		String statisticDataString = Arrays.toString(statisticData);
		this.statisticsManager
				.insertStatisticData(Arrays.asList(statisticData));
		long duration = new Date().getTime() - start.getTime();

		registerAction("Statistics.insertStatisticDataList", new String[] {
				"statisticData", "" + statisticDataString },
				"User %username% called method Statistics.insertStatisticDataList("
						+ statisticDataString + ")", duration);

	}

}
