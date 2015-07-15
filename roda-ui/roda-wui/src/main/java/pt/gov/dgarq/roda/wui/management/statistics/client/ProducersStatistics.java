/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.statistics.client;

import java.util.List;

import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.index.sorter.SortParameter;
import org.roda.index.sorter.Sorter;
import org.roda.index.sublist.Sublist;
import org.roda.legacy.old.adapter.ContentAdapter;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import pt.gov.dgarq.roda.core.data.StatisticData;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;

/**
 * @author Luis Faria
 * 
 */
public class ProducersStatistics extends StatisticTab {

	private ClientLogger logger = new ClientLogger(getClass().getName());

	private DockPanel layout;
	private Label title;
	private ListBox producers;
	private VerticalPanel centerLayout;

	/**
	 * Create new producer statistics
	 */
	public ProducersStatistics() {
		layout = new DockPanel();
		initWidget(layout);
	}

	protected boolean init() {
		boolean ret = false;
		if (super.init()) {
			ret = true;
			title = new Label(constants.producerTitle());

			producers = new ListBox();
			producers.setMultipleSelect(false);
			producers.setVisibleItemCount(10);

			producers.addChangeListener(new ChangeListener() {

				public void onChange(Widget sender) {
					updateProducerInfo();
				}

			});

			centerLayout = new VerticalPanel();

			layout.add(title, DockPanel.NORTH);
			layout.add(producers, DockPanel.WEST);
			layout.add(centerLayout, DockPanel.CENTER);

			updateProducerList();

			layout.addStyleName("wui-statistics-producers");
			title.addStyleName("producers-title");
			producers.addStyleName("producers-list");
			centerLayout.addStyleName("producers-center");

		}

		return ret;
	}

	private void updateProducerList() {
		Filter filter = new Filter();
		filter.add(new SimpleFilterParameter("type", "producers"));
		Sorter sorter = new Sorter();
		sorter.add(new SortParameter("datetime", true));
		Sublist sublist = new Sublist(0, 1);
		ContentAdapter adapter = new ContentAdapter(filter, sorter, sublist);
		StatisticsService.Util.getInstance().getStatisticList(adapter, new AsyncCallback<List<StatisticData>>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting producer list", caught);
			}

			public void onSuccess(List<StatisticData> result) {
				if (result.size() > 0) {
					String[] producerList = result.get(0).getValue().split(" ");
					producers.clear();
					for (String producer : producerList) {
						producers.addItem(producer);
					}
					producers.setSelectedIndex(0);

				} else {
					producers.clear();
				}
				updateProducerInfo();
			}

		});
	}

	/**
	 * Get selected producer
	 * 
	 * @return the producer user name or null if none selected
	 */
	protected String getSelectedProducer() {
		String ret = null;
		int selectedIndex = producers.getSelectedIndex();
		if (selectedIndex >= 0) {
			ret = producers.getValue(selectedIndex);
		}
		return ret;
	}

	protected void updateProducerInfo() {
		updateProducerInfo(getSelectedProducer());
	}

	protected void updateProducerInfo(String selected) {
		centerLayout.clear();
		if (selected != null) {
			centerLayout.add(createLastSubmissionPanel(selected));
			centerLayout.add(createSubmissionChart(selected));

		}
	}

	private Widget createLastSubmissionPanel(String producer) {
		HorizontalPanel layout = new HorizontalPanel();
		Label title = new Label(constants.producerLastSubmissionDate() + ":");
		final Label value = new Label();

		layout.add(title);
		layout.add(value);

		Filter filter = new Filter();
		filter.add(new SimpleFilterParameter("type", "producer." + producer + ".submission.last"));
		Sorter sorter = new Sorter();
		sorter.add(new SortParameter("datetime", true));
		Sublist sublist = new Sublist(0, 1);
		ContentAdapter adapter = new ContentAdapter(filter, sorter, sublist);
		StatisticsService.Util.getInstance().getStatisticList(adapter, new AsyncCallback<List<StatisticData>>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting producer last submission", caught);

			}

			public void onSuccess(List<StatisticData> result) {
				if (result.size() > 0) {
					value.setText(result.get(0).getValue());
				}

			}

		});

		layout.addStyleName("submission-last");
		title.addStyleName("submission-last-title");
		value.addStyleName("submission-last-value");

		return layout;
	}

	private Widget createSubmissionChart(String producer) {
		return createStatisticPanel(constants.producerSubmissionStateChartTitle(),
				constants.producerSubmissionStateChartDesc(), "producer\\." + producer + "\\.submission\\.state\\..*",
				true, AGGREGATION_LAST);

	}

	/**
	 * @see pt.gov.dgarq.roda.wui.management.statistics.client.StatisticTab#getTabText()
	 */
	@Override
	public String getTabText() {
		return constants.producersStatistics();
	}

}
