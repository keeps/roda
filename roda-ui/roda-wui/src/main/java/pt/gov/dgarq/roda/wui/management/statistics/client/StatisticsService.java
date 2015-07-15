/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.statistics.client;

import java.util.Date;
import java.util.List;

import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import org.roda.legacy.old.adapter.ContentAdapter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.data.StatisticData;
import pt.gov.dgarq.roda.wui.common.client.PrintReportException;

/**
 * @author Luis Faria
 * 
 */
@RemoteServiceRelativePath("StatisticsService")
public interface StatisticsService extends RemoteService {

	/**
	 * Get Utilities
	 * 
	 * @author Luis Faria
	 * 
	 */
	public static class Util {

		/**
		 * Get singleton
		 * 
		 * @return the singleton
		 */
		public static StatisticsServiceAsync getInstance() {

			return GWT.create(StatisticsService.class);
		}
	}

	/**
	 * Get statistic data count
	 * 
	 * @param filter
	 * @return the number of entries
	 * @throws RODAException
	 */
	public int getStatisticCount(Filter filter) throws RODAException;

	/**
	 * Get statistic data
	 * 
	 * @param adapter
	 * @return a list of statistic entries
	 * @throws RODAException
	 */
	public List<StatisticData> getStatisticList(ContentAdapter adapter) throws RODAException;

	/**
	 * Get statistic data processed by statistic functions
	 * 
	 * @param adapter
	 * @param functions
	 * @param segmentation
	 * @param initialDate
	 * @param finalDate
	 * @return the result statistic data
	 * @throws RODAException
	 */
	public List<StatisticData> getStatisticList(ContentAdapter adapter, List<StatisticFunction> functions,
			Segmentation segmentation, Date initialDate, Date finalDate) throws RODAException;

	/**
	 * Get statistic stacked list. When using adapter queries that return a set
	 * on statistic types, this method should be called.
	 * 
	 * @param adapter
	 * @param functions
	 *            function are processed horizontally, on the list of each
	 *            statistic type
	 * @param steps
	 * @param segmentation
	 * @param finalDate
	 * @return A list of stacks, one for each type.
	 * @throws RODAException
	 */
	public List<List<StatisticData>> getStatisticStackedList(ContentAdapter adapter, List<StatisticFunction> functions,
			Segmentation segmentation, Date initialDate, Date finalDate) throws RODAException;

	public void setStatisticListReportInfo(ContentAdapter adapter, String localeString) throws PrintReportException;

}
