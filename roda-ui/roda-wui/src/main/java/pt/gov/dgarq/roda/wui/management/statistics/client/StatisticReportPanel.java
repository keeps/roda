/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.statistics.client;

import java.util.List;

import pt.gov.dgarq.roda.core.data.StatisticData;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.wui.common.client.images.CommonImageBundle;
import pt.gov.dgarq.roda.wui.common.client.tools.Tools;
import pt.gov.dgarq.roda.wui.common.client.widgets.DatePicker;
import pt.gov.dgarq.roda.wui.common.client.widgets.ElementPanel;
import pt.gov.dgarq.roda.wui.common.client.widgets.LazyVerticalList;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIButton;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIWindow;
import pt.gov.dgarq.roda.wui.common.client.widgets.LazyVerticalList.ContentSource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.rednels.ofcgwt.client.ChartWidget;
import com.rednels.ofcgwt.client.IChartListener;
import com.rednels.ofcgwt.client.model.ChartData;
import com.rednels.ofcgwt.client.model.axis.XAxis;
import com.rednels.ofcgwt.client.model.axis.YAxis;

/**
 * @author Luis Faria
 * 
 */
public abstract class StatisticReportPanel extends StatisticsPanel {

	// private ClientLogger logger = new ClientLogger(getClass().getName());
	private CommonImageBundle images = (CommonImageBundle) GWT
			.create(CommonImageBundle.class);

	private final WUIWindow window;
	private final WUIButton close;

	// Chart

	private final DockPanel chartLayout;
	private final HorizontalPanel chartHeader;
	private final Label chartSegmentationLabel;
	private final SegmentationPicker chartSegmentationPicker;
	private final Label chartDateIntervalLabel;
	private final Image changeDateInterval;

	private final ChartWidget chartWidget;
	private final LazyVerticalList<StatisticData> lazyList;

	private WUIWindow dateIntervalPickerWindow = null;
	private Grid dateIntervalPickerLayout = null;
	private Label dateIntervalLabelInitial = null;
	private Label dateIntervalLabelFinal = null;
	private DatePicker dateIntervalPickerInitial = null;
	private DatePicker dateIntervalPickerFinal = null;
	private WUIButton dateIntervalPickerApply = null;

	/**
	 * Create a new statistic result panel
	 * 
	 * @param title
	 *            statistic panel title
	 * @param type
	 *            statistic type or regexp of types
	 * @param dimension
	 *            the dimension (or unit) of the value
	 * @param functions
	 *            statistic functions
	 * @param segmentation
	 */
	public StatisticReportPanel(String title, String type,
			ValueDimension dimension, Segmentation segmentation,
			List<StatisticFunction> functions) {
		super(title, type, dimension, segmentation, functions);

		window = new WUIWindow(title, 900, 450);

		close = new WUIButton(constants.statisticsReportClose(),
				WUIButton.Left.ROUND, WUIButton.Right.CROSS);

		window.addToBottom(close);

		close.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				hide();
			}

		});

		// Chart

		chartLayout = new DockPanel();
		chartHeader = new HorizontalPanel();
		chartSegmentationLabel = new Label(constants
				.statisticsReportSegmentationLabel()
				+ ":");
		chartSegmentationPicker = new SegmentationPicker();

		chartDateIntervalLabel = new Label();
		updateDateIntervalLabel();

		changeDateInterval = images.date_edit().createImage();

		chartHeader.add(chartSegmentationLabel);
		chartHeader.add(chartSegmentationPicker);
		chartHeader.add(chartDateIntervalLabel);
		chartHeader.add(changeDateInterval);

		chartWidget = new ChartWidget();
		chartWidget.setSize("890px", "350px");

		chartLayout.add(chartHeader, DockPanel.NORTH);
		chartLayout.add(chartWidget, DockPanel.CENTER);

		chartSegmentationPicker.addChangeListener(new ChangeListener() {

			public void onChange(Widget sender) {
				setSegmentation(chartSegmentationPicker.getSegmentation());
				update();
			}

		});

		changeDateInterval.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				getDateIntervalPickerWindow().show();
			}

		});

		chartWidget.addChartListeners(new IChartListener() {

			public void handleChartReadyEvent() {
				initChart();
				update();
			}

			public void imageSavedEvent() {
				// nothing to do

			}

		});

		// List
		Filter lazyListFilter = new Filter();
		lazyListFilter.add(getTypeFilterParameter());

		lazyList = new LazyVerticalList<StatisticData>(
				new ContentSource<StatisticData>() {

					public void getCount(Filter filter,
							AsyncCallback<Integer> callback) {
						StatisticsService.Util.getInstance().getStatisticCount(
								filter, callback);

					}

					public ElementPanel<StatisticData> getElementPanel(
							StatisticData element) {
						return new StatisticListItem(element);
					}

					public void getElements(ContentAdapter adapter,
							final AsyncCallback<StatisticData[]> callback) {
						StatisticsService.Util.getInstance().getStatisticList(
								adapter,
								new AsyncCallback<List<StatisticData>>() {

									public void onFailure(Throwable caught) {
										callback.onFailure(caught);

									}

									public void onSuccess(
											List<StatisticData> result) {
										callback
												.onSuccess(result
														.toArray(new StatisticData[] {}));

									}

								});

					}

					public String getTotalMessage(int total) {
						return total + " "
								+ constants.statisticsReportListTotal();
					}

					public void setReportInfo(ContentAdapter adapter,
							String locale, AsyncCallback<Void> callback) {
						StatisticsService.Util.getInstance()
								.setStatisticListReportInfo(adapter, locale,
										callback);

					}

				}, false, lazyListFilter);

		lazyList.getHeader().addHeader(
				constants.statisticsReportListHeaderDate(),
				"statistic-list-header-date",
				new SortParameter[] { new SortParameter("datetime", true) },
				false);
		lazyList.getHeader().addHeader(
				constants.statisticsReportListHeaderType(),
				"statistic-list-header-type",
				new SortParameter[] { new SortParameter("type", true) }, false);
		lazyList
				.getHeader()
				.addHeader(
						constants.statisticsReportListHeaderValue(),
						"statistic-list-header-value",
						new SortParameter[] { new SortParameter("value", true) },
						false);

		lazyList.getHeader().setFillerHeader(2);
		lazyList.getHeader().setSelectedHeader(0);

		lazyList.setScrollHeight("325px");

		// Add to window
		window.addTab(chartLayout, constants.statisticsReportChart());
		window.addTab(lazyList.getWidget(), constants.statisticsReportList());

		window.selectTab(0);

		// styles
		chartLayout.addStyleName("wui-statistic-report-chart");
		chartHeader.addStyleName("report-chart-header");
		chartSegmentationLabel.addStyleName("report-chart-segmentation-label");
		chartSegmentationPicker
				.addStyleName("report-chart-segmentation-picker");
		chartDateIntervalLabel.addStyleName("report-chart-date-label");
		changeDateInterval.addStyleName("report-chart-date-change");
		chartWidget.addStyleName("report-chart-widget");

		chartHeader.setCellWidth(chartDateIntervalLabel, "100%");

	}

	protected WUIWindow getDateIntervalPickerWindow() {
		if (dateIntervalPickerWindow == null) {
			dateIntervalPickerWindow = new WUIWindow(constants
					.dateIntervalPickerWindowTitle(), 400, 100);
			dateIntervalPickerLayout = new Grid(2, 2);

			dateIntervalLabelInitial = new Label(constants
					.dateIntervalLabelInitial()
					+ ":");
			dateIntervalLabelFinal = new Label(constants
					.dateIntervalLabelFinal()
					+ ":");
			dateIntervalPickerInitial = new DatePicker();
			dateIntervalPickerFinal = new DatePicker();
			dateIntervalPickerApply = new WUIButton(constants
					.dateIntervalPickerWindowApply(), WUIButton.Left.ROUND,
					WUIButton.Right.ARROW_FORWARD);

			dateIntervalPickerWindow.setWidget(dateIntervalPickerLayout);
			dateIntervalPickerWindow.addToBottom(dateIntervalPickerApply);

			dateIntervalPickerLayout.setWidget(0, 0, dateIntervalLabelInitial);
			dateIntervalPickerLayout.setWidget(0, 1, dateIntervalPickerInitial);
			dateIntervalPickerLayout.setWidget(1, 0, dateIntervalLabelFinal);
			dateIntervalPickerLayout.setWidget(1, 1, dateIntervalPickerFinal);

			dateIntervalPickerInitial.setDate(getInitialDate());
			dateIntervalPickerFinal.setDate(getFinalDate());

			dateIntervalPickerApply.addClickListener(new ClickListener() {

				public void onClick(Widget sender) {
					// date final must be set first because initial is relative
					setFinalDate(dateIntervalPickerFinal.getDate());
					setInitialDate(dateIntervalPickerInitial.getDate());
					updateDateIntervalLabel();
					update();
					dateIntervalPickerWindow.hide();
				}

			});

			dateIntervalPickerLayout.addStyleName("report-date-change");
			dateIntervalLabelInitial
					.addStyleName("report-date-change-label-initial");
			dateIntervalLabelFinal
					.addStyleName("report-date-change-label-final");
			dateIntervalPickerInitial
					.addStyleName("report-date-change-picker-initial");
			dateIntervalPickerFinal
					.addStyleName("report-date-change-picker-final");

			dateIntervalPickerLayout.getCellFormatter().setHorizontalAlignment(
					0, 0, HasAlignment.ALIGN_RIGHT);
			dateIntervalPickerLayout.getCellFormatter().setHorizontalAlignment(
					1, 0, HasAlignment.ALIGN_RIGHT);

		}
		return dateIntervalPickerWindow;
	}

	/**
	 * Show window
	 */
	public void show() {
		window.show();
	}

	/**
	 * Hide window
	 */
	public void hide() {
		window.hide();
	}

	protected final DateTimeFormat DATE_FORMAT = DateTimeFormat
			.getFormat("yyyy-MM-dd");

	protected void updateDateIntervalLabel() {
		chartDateIntervalLabel.setText(DATE_FORMAT.format(getInitialDate())
				+ " " + constants.statisticsReportDateSeparatorLabel() + " "
				+ DATE_FORMAT.format(getFinalDate()));

	}

	protected ChartWidget getChartWidget() {
		return chartWidget;
	}

	protected DockPanel getChartLayout() {
		return chartLayout;
	}

	private void initChart() {
		// Create chart data
		ChartData cd1 = new ChartData();
		XAxis xa = new XAxis();
		xa.setMax(0);
		YAxis ya = new YAxis();
		ya.setMax(0);

		cd1.setXAxis(xa);
		cd1.setYAxis(ya);

		// Set chart style
		cd1.setBackgroundColour(BACKGROUND);
		ya.setColour(FOREGROUND);
		ya.setGridColour(HALFGROUND);
		xa.setColour(FOREGROUND);
		xa.setGridColour(HALFGROUND);

		// Insert data
		getChartWidget().setJsonData(cd1.toString());

	}

	private class StatisticListItem extends ElementPanel<StatisticData> {

		private final HorizontalPanel layout;
		private final Label date;
		private final Label type;
		private final Label value;

		public StatisticListItem(StatisticData element) {
			super(element);
			layout = new HorizontalPanel();
			date = new Label();
			type = new Label();
			value = new Label();

			layout.add(date);
			layout.add(type);
			layout.add(value);

			setWidget(layout);

			update(element);

			setStylePrimaryName("statistic-list-item");
			layout.addStyleName("item-layout");
			date.addStyleName("item-date");
			type.addStyleName("item-type");
			value.addStyleName("item-value");

			layout.setCellWidth(value, "100%");
		}

		@Override
		protected void update(StatisticData element) {
			date.setText(Tools.formatDateTimeMs(element.getTimestamp()));
			type.setText(getTypeTranslated(element.getType()));
			value.setText(element.getValue());

		}

	}

}
