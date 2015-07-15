package pt.gov.dgarq.roda.wui.management.statistics.client;

import java.util.List;

import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.data.StatisticData;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.images.CommonImageBundle;
import pt.gov.dgarq.roda.wui.common.client.tools.Tools;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.ViewWindow;
import pt.gov.dgarq.roda.wui.ingest.list.client.IngestListService;
import pt.gov.dgarq.roda.wui.ingest.list.client.IngestReportWindow;
import pt.gov.dgarq.roda.wui.management.statistics.client.StatisticsPanel.ValueDimension;

/**
 * 
 * @author Luis Faria
 * 
 */
public class IngestStatistics extends StatisticTab {
	private ClientLogger logger = new ClientLogger(getClass().getName());

	private VerticalPanel layout;
	private StatisticMiniPanel sipComplete;
	private StatisticMiniPanel sipState;
	private StatisticMiniPanel sipDurationAuto;
	private StatisticMiniPanel sipDurationManual;

	/**
	 * Create new ingest statistics
	 */
	public IngestStatistics() {
		layout = new VerticalPanel();
		initWidget(layout);
	}

	@Override
	protected boolean init() {
		boolean ret = false;
		if (super.init()) {
			ret = true;
			sipComplete = createStatisticPanel(
					constants.sipCompletenessTitle(), constants
							.sipCompletenessDesc(), "sips\\.complete\\..*",
					true, AGGREGATION_LAST);
			sipState = createStatisticPanel(constants.sipStateTitle(),
					constants.sipStateDesc(), "sips\\.state\\..*", true,
					AGGREGATION_LAST);

			sipDurationAuto = createStatisticPanel(constants
					.sipDurationAutoTitle(), constants.sipDurationAutoDesc(),
					"sips.duration.auto", ValueDimension.MILLISECONDS, false,
					false, AGGREGATION_LAST);

			sipDurationManual = createStatisticPanel(constants
					.sipDurationManualTitle(), constants
					.sipDurationManualDesc(), "sips.duration.manual",
					ValueDimension.MILLISECONDS, false, false, AGGREGATION_LAST);

			Panel sipMinAutoDurationPanel = createSipLimitDurationPanel(
					constants.sipMinAutomaticProcessingTimeTitle(), constants
							.sipMinAutomaticProcessingTimeDesc(), true, false);
			Panel sipMaxAutoDurationPanel = createSipLimitDurationPanel(
					constants.sipMaxAutomaticProcessingTimeTitle(), constants
							.sipMaxAutomaticProcessingTimeDesc(), true, true);
			Panel sipMinManualDurationPanel = createSipLimitDurationPanel(
					constants.sipMinManualProcessingTimeTitle(), constants
							.sipMinManualProcessingTimeDesc(), false, false);
			Panel sipMaxManualDurationPanel = createSipLimitDurationPanel(
					constants.sipMaxManualProcessingTimeTitle(), constants
							.sipMaxManualProcessingTimeDesc(), false, true);

			layout.add(sipComplete);
			layout.add(sipState);
			layout.add(sipDurationAuto);
			layout.add(sipDurationManual);
			layout.add(sipMinAutoDurationPanel);
			layout.add(sipMaxAutoDurationPanel);
			layout.add(sipMinManualDurationPanel);
			layout.add(sipMaxManualDurationPanel);

			layout.addStyleName("wui-statistics-ingest");
		}
		return ret;

	}

	private static CommonImageBundle commonImageBundle = (CommonImageBundle) GWT
			.create(CommonImageBundle.class);

	private Panel createSipLimitDurationPanel(String title, String description,
			boolean auto, boolean max) {
		DockPanel ret = new DockPanel();
		HorizontalPanel header = new HorizontalPanel();
		Label titleLabel = new Label(title);
		final Image report = commonImageBundle.report().createImage();
		final Image info = commonImageBundle.info().createImage();

		HorizontalPanel centerLayout = new HorizontalPanel();

		Label descriptionLabel = new Label(description);
		final Label value = new Label();

		getLastDataOfType("sips.duration." + (auto ? "auto" : "manual") + "."
				+ (max ? "max" : "min"), new AsyncCallback<StatisticData>() {

			public void onFailure(Throwable caught) {
				logger.error("Error creating sip duration report", caught);
			}

			public void onSuccess(StatisticData data) {
				if (data != null) {
					value.setText(Tools.formatValueMilliseconds(Long
							.parseLong(data.getValue()), false));
				} else {
					value.setText(constants.noDataAvailable());
				}
			}
		});

		getLastDataOfType("sips.duration." + (auto ? "auto" : "manual") + "."
				+ (max ? "max" : "min") + ".id",
				new AsyncCallback<StatisticData>() {

					public void onFailure(Throwable caught) {
						logger.error("Error creating sip duration report",
								caught);
					}

					public void onSuccess(StatisticData data) {
						if (data != null) {
							final String sipId = data.getValue();
							IngestListService.Util.getInstance().getSipState(
									sipId, new AsyncCallback<SIPState>() {

										public void onFailure(Throwable caught) {
											logger
													.error(
															"Error creating sip duration report",
															caught);

										}

										public void onSuccess(
												final SIPState result) {
											report
													.addClickListener(new ClickListener() {

														public void onClick(
																Widget sender) {
															IngestReportWindow w = new IngestReportWindow(
																	result);
															w.show();

														}

													});
											info
													.addClickListener(new ClickListener() {

														public void onClick(
																Widget sender) {
															String pid = result
																	.getIngestedPID();
															if (pid != null) {
																ViewWindow viewWindow = new ViewWindow(
																		pid);
																viewWindow
																		.show();
															} else {
																Window
																		.alert(constants
																				.viewImpossibleBcSipNotIngested());
															}
														}

													});

										}

									});
						} else {
							value.setText(constants.noDataAvailable());
						}
					}

				});

		header.add(titleLabel);
		header.add(report);
		header.add(info);

		centerLayout.add(descriptionLabel);
		centerLayout.add(value);

		ret.add(header, DockPanel.NORTH);
		ret.add(centerLayout, DockPanel.CENTER);

		ret.addStyleName("wui-statistic-mini-simple");
		header.addStyleName("mini-simple-header");
		titleLabel.addStyleName("mini-simple-title");
		report.addStyleName("mini-simple-report");
		info.addStyleName("mini-simple-report");
		centerLayout.addStyleName("mini-simple-center");
		descriptionLabel.addStyleName("mini-simple-description");
		value.addStyleName("mini-simple-value");

		header.setCellWidth(titleLabel, "100%");

		centerLayout.setCellWidth(value, "100%");
		centerLayout.setCellHorizontalAlignment(value,
				HasAlignment.ALIGN_CENTER);

		return ret;
	}

	protected void getLastDataOfType(String type,
			final AsyncCallback<StatisticData> callback) {
		Filter filter = new Filter();
		filter.add(new SimpleFilterParameter("type", type));
		Sorter sorter = new Sorter();
		sorter.add(new SortParameter("datetime", true));
		Sublist subList = new Sublist(0, 1);
		ContentAdapter adapter = new ContentAdapter(filter, sorter, subList);
		StatisticsService.Util.getInstance().getStatisticList(adapter,
				new AsyncCallback<List<StatisticData>>() {

					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}

					public void onSuccess(List<StatisticData> result) {
						if (result.size() > 0) {
							callback.onSuccess(result.get(0));
						} else {
							callback.onSuccess(null);
						}

					}

				});

	}

	@Override
	public String getTabText() {
		return constants.ingestStatistics();
	}

}
