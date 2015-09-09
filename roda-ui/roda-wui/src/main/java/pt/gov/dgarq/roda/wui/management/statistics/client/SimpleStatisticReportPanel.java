/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.statistics.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;

import com.google.gwt.user.client.rpc.AsyncCallback;
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
public class SimpleStatisticReportPanel extends StatisticReportPanel {

  private ClientLogger logger = new ClientLogger(getClass().getName());

  /**
   * Create a new statistic result panel
   * 
   * @param title
   *          statistic panel title
   * @param type
   *          statistic type or regexp of types
   * @param dimension
   *          the dimension (or unit) of the value
   * @param functions
   *          statistic functions
   * @param segmentation
   */
  public SimpleStatisticReportPanel(String title, String type, ValueDimension dimension, Segmentation segmentation,
    StatisticFunction... functions) {
    this(title, type, dimension, segmentation, new ArrayList<StatisticFunction>(Arrays.asList(functions)));

  }

  /**
   * Create a new statistic result panel
   * 
   * @param title
   *          statistic panel title
   * @param type
   *          statistic type or regexp of types
   * @param dimension
   *          the dimension (or unit) of the value
   * @param functions
   *          statistic functions
   * @param segmentation
   */
  public SimpleStatisticReportPanel(String title, String type, ValueDimension dimension, Segmentation segmentation,
    List<StatisticFunction> functions) {
    super(title, type, dimension, segmentation, functions);

  }

  @Override
  protected FilterParameter getTypeFilterParameter() {
    return new SimpleFilterParameter("type", getType());
  }

  @Override
  public void update() {
    loading.show();
    StatisticsService.Util.getInstance().getStatisticList(getContentAdapter(), getFunctions(), getSegmentation(),
      getInitialDate(), getFinalDate(), new AsyncCallback<List<StatisticData>>() {

        public void onFailure(Throwable caught) {
          logger.error("Could not get statistics list", caught);
          loading.hide();
        }

        public void onSuccess(List<StatisticData> statistics) {
          logger.debug("got statistics, updating chart...");
          update(statistics);
          logger.debug("update finished");
          loading.hide();
        }

      });

  }

  protected void update(List<StatisticData> statistics) {
    updateChart("", statistics, getSegmentation(), getChartWidget());
  }

  protected void updateChart(String title, List<StatisticData> infos, Segmentation segmentation,
    ChartWidget historyChart) {

    // Create chart data
    ChartData cd1 = new ChartData(title, CHART_TITLE_STYLE);

    List<String> xLabels = new ArrayList<String>();
    List<Dot> dots = new ArrayList<Dot>();
    Integer xMax = infos.size();
    Integer yMax = Integer.MIN_VALUE;
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

    XAxis xa = new XAxis();
    xa.setLabels(xLabels);
    xa.setMax(xMax);
    cd1.setXAxis(xa);
    YAxis ya = new YAxis();
    int ySteps = (yMax / Y_STEPS) + 1;
    ya.setSteps(ySteps);
    ya.setMax(yMax + ySteps);
    cd1.setYAxis(ya);

    // Create chart
    AreaChart areaChart = new AreaChart(AreaStyle.LINE);
    areaChart.setTooltip("#val#");
    areaChart.addDots(dots);
    cd1.addElements(areaChart);

    // Set chart style
    cd1.setBackgroundColour(BACKGROUND);
    ya.setColour(FOREGROUND);
    ya.setGridColour(HALFGROUND);
    xa.setColour(FOREGROUND);
    xa.setGridColour(HALFGROUND);

    if (Segmentation.MONTH.compareTo(segmentation) <= 0) {
      xa.getLabels().setRotation(Rotation.VERTICAL);
    }

    areaChart.setColour(COLORS.get(getColorIndex()));
    areaChart.setFillColour(COLORS.get(getColorIndex()));
    areaChart.setFillAlpha(0.7f);
    areaChart.setWidth(2);
    areaChart.setDotSize(4);
    areaChart.setHaloSize(1);

    // Insert data
    historyChart.setJsonData(cd1.toString());

  }

}
