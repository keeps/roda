/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.statistics.client;

import java.util.Date;
import java.util.List;

import pt.gov.dgarq.roda.core.data.StatisticData;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Luis Faria
 * 
 */
public interface StatisticsServiceAsync {

	/**
	 * Get statistic data count
	 * 
	 * @param filter
	 * @return the number of entries
	 * @throws RODAException
	 */
	public void getStatisticCount(Filter filter, AsyncCallback<Integer> callback);

	/**
	 * Get statistic data
	 * 
	 * @param adapter
	 * @return a list of statistic entries
	 * @throws RODAException
	 */
	public void getStatisticList(ContentAdapter adapter, AsyncCallback<List<StatisticData>> callback);

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
	public void getStatisticList(ContentAdapter adapter,
			List<StatisticFunction> functions, Segmentation segmentation,
			Date initialDate, Date finalDate, AsyncCallback<List<StatisticData>> callback);

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
	public void getStatisticStackedList(
			ContentAdapter adapter, List<StatisticFunction> functions,
			Segmentation segmentation, Date initialDate, Date finalDate, AsyncCallback<List<List<StatisticData>>> callback);

	public void setStatisticListReportInfo(ContentAdapter adapter,
			String localeString, AsyncCallback<Void> callback);

}
