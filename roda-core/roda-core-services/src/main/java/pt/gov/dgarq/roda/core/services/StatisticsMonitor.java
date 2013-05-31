package pt.gov.dgarq.roda.core.services;

import java.util.Date;
import java.util.List;

import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.common.StatisticsException;
import pt.gov.dgarq.roda.core.data.StatisticData;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.statistics.StatisticsManager;

/**
 * This class implements Statistics service.
 * 
 * @author Rui Castro
 */
public class StatisticsMonitor extends RODAWebService {

	static final private org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(StatisticsMonitor.class);

	private StatisticsManager statisticsManager = null;

	/**
	 * Constructs a new {@link StatisticsMonitor}.
	 * 
	 * @throws StatisticsException
	 * @throws RODAServiceException
	 */
	public StatisticsMonitor() throws StatisticsException, RODAServiceException {
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
	 * Returns the number of {@link StatisticData} that respect the specified
	 * filter.
	 * 
	 * @param filter
	 * 
	 * @return the number of {@link StatisticData}s.
	 * 
	 * @throws StatisticsException
	 */
	public int getStatisticDataCount(Filter filter) throws StatisticsException {

		Date start = new Date();
		int count = this.statisticsManager.getStatisticDataCount(filter);
		long duration = new Date().getTime() - start.getTime();

		registerAction("StatisticsMonitor.getStatisticDataCount", new String[] {
				"filter", "" + filter },
				"User %username% called method StatisticsMonitor.getStatisticDataCount("
						+ filter + ")", duration);

		return count;
	}

	/**
	 * * Returns a list of {@link StatisticData}s matching the
	 * {@link ContentAdapter} specified.
	 * 
	 * @param contentAdapter
	 * 
	 * @return an array of {@link StatisticData}.
	 * 
	 * @throws StatisticsException
	 */
	public StatisticData[] getStatisticData(ContentAdapter contentAdapter)
			throws StatisticsException {

		Date start = new Date();
		List<StatisticData> statisticData = this.statisticsManager
				.getStatisticData(contentAdapter);
		StatisticData[] result = statisticData
				.toArray(new StatisticData[statisticData.size()]);
		long duration = new Date().getTime() - start.getTime();

		registerAction("StatisticsMonitor.getStatisticData", new String[] {
				"contentAdapter", "" + contentAdapter },
				"User %username% called method StatisticsMonitor.getStatisticData("
						+ contentAdapter + ")", duration);

		return result;
	}

}
