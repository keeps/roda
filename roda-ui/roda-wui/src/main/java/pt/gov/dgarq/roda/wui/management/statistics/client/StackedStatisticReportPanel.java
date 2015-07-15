/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.statistics.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.roda.index.filter.FilterParameter;
import org.roda.index.filter.RegexFilterParameter;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;
import com.rednels.ofcgwt.client.ChartWidget;
import com.rednels.ofcgwt.client.model.ChartData;
import com.rednels.ofcgwt.client.model.axis.Label.Rotation;
import com.rednels.ofcgwt.client.model.axis.XAxis;
import com.rednels.ofcgwt.client.model.axis.YAxis;
import com.rednels.ofcgwt.client.model.elements.AreaChart;
import com.rednels.ofcgwt.client.model.elements.AreaChart.AreaStyle;
import com.rednels.ofcgwt.client.model.elements.LineChart.Dot;

import pt.gov.dgarq.roda.core.data.StatisticData;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;

/**
 * @author Luis Faria
 * 
 */
public class StackedStatisticReportPanel extends StatisticReportPanel {

	private ClientLogger logger = new ClientLogger(getClass().getName());

	// Stack colours
	private Map<String, String> type2colour = new HashMap<String, String>();
	private int stackColorIndex = 0;

	private Legends legends;

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
	public StackedStatisticReportPanel(String title, String type, ValueDimension dimension, Segmentation segmentation,
			StatisticFunction... functions) {
		this(title, type, dimension, segmentation, new ArrayList<StatisticFunction>(Arrays.asList(functions)));

	}

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
	public StackedStatisticReportPanel(String title, String type, ValueDimension dimension, Segmentation segmentation,
			List<StatisticFunction> functions) {
		super(title, type, dimension, segmentation, functions);

		legends = new Legends(false);
		getChartLayout().add(legends, DockPanel.SOUTH);
	}

	@Override
	protected FilterParameter getTypeFilterParameter() {
		return new RegexFilterParameter("type", getType());
	}

	@Override
	public void update() {
		loading.show();
		StatisticsService.Util.getInstance().getStatisticStackedList(getContentAdapter(), getFunctions(),
				getSegmentation(), getInitialDate(), getFinalDate(), new AsyncCallback<List<List<StatisticData>>>() {

					public void onFailure(Throwable caught) {
						logger.error("Could not get statistics list", caught);
						loading.hide();
					}

					public void onSuccess(List<List<StatisticData>> statistics) {
						update(statistics);
						loading.hide();
					}
				});
	}

	protected void update(List<List<StatisticData>> statistics) {
		// update chart
		updateStackedChart("", statistics, getColors(), getSegmentation(), getChartWidget());

		// update legends
		legends.clear();
		for (Entry<String, String> entry : type2colour.entrySet()) {
			String label = getTypeTranslated(entry.getKey());
			String color = entry.getValue();
			legends.addLegend(color, label);
		}
	}

	protected void updateStackedChart(String title, List<List<StatisticData>> infoList, List<String> colours,
			Segmentation segmentation, ChartWidget historyChart) {

		// Create chart data
		ChartData cd1 = new ChartData(title, CHART_TITLE_STYLE);

		Set<String> xLabels = new TreeSet<String>();

		Integer xMax = Integer.MIN_VALUE;
		Integer yMax = Integer.MIN_VALUE;

		for (List<StatisticData> infos : infoList) {
			xMax = Math.max(xMax, infos.size());
			List<Dot> dots = new ArrayList<Dot>();

			for (StatisticData info : infos) {
				String date = formatDate(info.getTimestamp(), segmentation);
				xLabels.add(date);
				double value = Double.parseDouble(info.getValue());
				Dot dot;
				if (value == DEFAULT_VALUE) {
					dot = new Dot(0, BACKGROUND);
				} else {
					dot = new Dot(value);
				}
				dots.add(dot);

				yMax = Math.max(yMax, (int) value);
			}

			String typeLabel = getTypeTranslated(infos.get(0).getType());
			String typeColor = getTypeColour(infos.get(0).getType(), colours);

			// Create chart
			AreaChart areaChart = new AreaChart(AreaStyle.LINE);
			areaChart.setTooltip(typeLabel + ": #val#");
			areaChart.addDots(dots);
			cd1.addElements(areaChart);

			areaChart.setColour(typeColor);
			areaChart.setFillColour(typeColor);
			areaChart.setFillAlpha(0.05f);
			areaChart.setWidth(2);
			areaChart.setDotSize(4);
			areaChart.setHaloSize(1);
		}

		XAxis xa = new XAxis();
		xa.setLabels(new ArrayList<String>(xLabels));
		xa.setMax(xMax);
		cd1.setXAxis(xa);
		YAxis ya = new YAxis();
		int ySteps = (yMax / Y_STEPS) + 1;
		ya.setSteps(ySteps);
		ya.setMax(yMax + ySteps);
		cd1.setYAxis(ya);

		// Set chart style
		cd1.setBackgroundColour(BACKGROUND);
		ya.setColour(FOREGROUND);
		ya.setGridColour(HALFGROUND);
		xa.setColour(FOREGROUND);
		xa.setGridColour(HALFGROUND);

		if (Segmentation.MONTH.compareTo(segmentation) <= 0) {
			xa.getLabels().setRotation(Rotation.VERTICAL);
		}

		// Insert data
		historyChart.setJsonData(cd1.toString());
	}

	protected String getTypeColour(String type, List<String> colours) {
		String ret;
		if (type2colour.containsKey(type)) {
			ret = type2colour.get(type);
		} else {
			ret = colours.get(stackColorIndex);
			stackColorIndex = (stackColorIndex + 1) % colours.size();
			type2colour.put(type, ret);
		}
		return ret;
	}

}
