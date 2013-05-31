package pt.gov.dgarq.roda.services.client;

import java.net.URL;
import java.util.Arrays;
import java.util.Date;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.data.StatisticData;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.RangeFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.RegexFilterParameter;
import pt.gov.dgarq.roda.core.stubs.Statistics;
import pt.gov.dgarq.roda.core.stubs.StatisticsMonitor;

/**
 * Test class for {@link Statistics} and {@link StatisticsMonitor} service.
 * 
 * @author Rui Castro
 */
public class StatisticsTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			RODAClient rodaClient = null;

			if (args.length == 1) {

				// http://localhost:8180/
				String hostUrl = args[0];

				rodaClient = new RODAClient(new URL(hostUrl));

			} else if (args.length >= 3) {

				// http://localhost:8180/ user pass
				String hostUrl = args[0];
				String username = args[1];
				String password = args[2];

				rodaClient = new RODAClient(new URL(hostUrl), username,
						password);
			} else {
				System.err.println(LoggerTest.class.getSimpleName()
						+ " protocol://hostname:port/ [username password]");
				System.exit(1);
			}

			Statistics statisticsService = rodaClient.getStatisticsService();
			StatisticsMonitor statisticsMonitorService = rodaClient
					.getStatisticsMonitorService();

			StatisticData[] statisticDataToInsert = new StatisticData[] { new StatisticData(
					new Date(), "fake.logs.action.disseminator\\..*", "666") };

			/*
			 * Insert statistics
			 */
			System.out.println("\n***********************************");
			System.out.println("Insert 1 statistic value ");
			System.out.println("***********************************");

			System.out.println("Adding "
					+ Arrays.toString(statisticDataToInsert));

			statisticsService.insertStatisticDataList(statisticDataToInsert);

			/*
			 * Count statistics
			 */
			System.out.println("\n***********************************");
			System.out.println("Count all StatisticData");
			System.out.println("***********************************");

			int count = statisticsMonitorService.getStatisticDataCount(null);
			System.out.println("StatisticData count without filter: " + count);

			System.out.println("\n***********************************");
			System.out
					.println("Get StatisticData with values between 0 and 1000");
			System.out.println("***********************************");

			Filter filter = new Filter(
					new FilterParameter[] { new RangeFilterParameter("value",
							"0", "1000") });
			StatisticData[] statisticData = statisticsMonitorService
					.getStatisticData(new ContentAdapter(filter, null, null));

			System.out.println(Arrays.toString(statisticData));

			System.out.println("\n***********************************");
			System.out
					.println("Get StatisticData with type starting with od.bla");
			System.out.println("***********************************");

			filter = new Filter(
					new FilterParameter[] { new RegexFilterParameter("type",
							"od\\.bla.*") });
			statisticData = statisticsMonitorService
					.getStatisticData(new ContentAdapter(filter, null, null));

			System.out.println(Arrays.toString(statisticData));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
