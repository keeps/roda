/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.management.statistics.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.roda.core.data.StatisticData;
import org.roda.core.data.adapter.filter.FilterParameter;
import org.roda.core.data.adapter.filter.RegexFilterParameter;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.images.CommonImageBundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.rednels.ofcgwt.client.ChartWidget;
import com.rednels.ofcgwt.client.IChartListener;
import com.rednels.ofcgwt.client.model.ChartData;
import com.rednels.ofcgwt.client.model.elements.PieChart;

/**
 * Small sized panel for a simple statistics
 * 
 * @author Luis Faria
 * 
 */
public class StackedStatisticMiniPanel extends StatisticMiniPanel {

  private static CommonImageBundle commonImageBundle = (CommonImageBundle) GWT.create(CommonImageBundle.class);

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private final VerticalPanel layout;
  private final DockPanel header;
  private final Label title;
  private final Image report;
  private final HorizontalPanel centerLayout;
  private final Label description;
  private final ChartWidget value;
  private final Legends legends;

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
  public StackedStatisticMiniPanel(String title, String description, String type, ValueDimension dimension,
    Segmentation segmentation, List<StatisticFunction> functions, List<StatisticFunction> reportFunctions) {
    super(title, description, type, dimension, segmentation, functions, reportFunctions);

    this.layout = new VerticalPanel();
    this.header = new DockPanel();
    this.title = new Label(title);
    centerLayout = new HorizontalPanel();
    this.description = new Label(description);
    this.value = new ChartWidget();
    this.value.setFlashUrl(GWT.getModuleBaseURL() + "open-flash-chart.swf");
    value.setSize("200px", "100px");
    this.legends = new Legends();
    this.report = commonImageBundle.chart().createImage();

    this.header.add(this.title, DockPanel.CENTER);
    this.header.add(this.report, DockPanel.EAST);

    this.centerLayout.add(this.description);
    this.centerLayout.add(value);
    this.centerLayout.add(legends);

    this.layout.add(this.header);
    this.layout.add(this.centerLayout);

    report.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        showReport();
      }

    });

    initWidget(layout);
    value.addChartListeners(new IChartListener() {

      public void handleChartReadyEvent() {
        initPieChart(value);
        update();
      }

      public void imageSavedEvent() {
        // nothing to do
      }

    });

    this.layout.addStyleName("wui-statistic-mini-stacked");
    this.header.addStyleName("mini-stacked-header");
    this.title.addStyleName("mini-stacked-title");
    this.description.addStyleName("mini-stacked-description");
    this.value.addStyleName("mini-stacked-value");
    this.report.addStyleName("mini-stacked-report");

    header.setCellWidth(this.title, "100%");
    header.setCellVerticalAlignment(this.title, HasAlignment.ALIGN_MIDDLE);
    header.setCellVerticalAlignment(this.report, HasAlignment.ALIGN_MIDDLE);

    centerLayout.setCellVerticalAlignment(legends, HasAlignment.ALIGN_MIDDLE);
  }

  @Override
  protected FilterParameter getTypeFilterParameter() {
    return new RegexFilterParameter("type", getType());
  }

  @Override
  public void update() {
    loading.show();
//    StatisticsService.Util.getInstance().getStatisticStackedList(getContentAdapter(), getFunctions(),
//      getSegmentation(), getInitialDate(), getFinalDate(), new AsyncCallback<List<List<StatisticData>>>() {
//
//        public void onFailure(Throwable caught) {
//          logger.error("Could not get statistics list", caught);
//          loading.hide();
//        }
//
//        public void onSuccess(List<List<StatisticData>> statistics) {
//          update(statistics);
//          loading.hide();
//        }
//
//      });
  }

  protected void update(List<List<StatisticData>> statistics) {
    // update value
    List<StatisticData> lastValidData = getLastValidStackedStatistics(statistics);
    updatePieChart(lastValidData, new ArrayList<String>(getColors()), value);

  }

  protected void updatePieChart(List<StatisticData> data, List<String> colors, ChartWidget actualChart) {
    Map<String, Number> pieData = new LinkedHashMap<String, Number>();

    if (data != null) {
      int index = 0;
      legends.clear();
      for (StatisticData info : data) {
        String label = getTypeTranslated(info.getType());
        double value = Double.valueOf(info.getValue());
        if (value == DEFAULT_VALUE) {
          value = 0;
        }
        pieData.put(label, value);

        String legendLabel = label + " (" + (value % 1 == 0 ? ((long) value) + "" : value + "") + ")";
        legends.addLegend(colors.get(index++ % colors.size()), legendLabel);

      }
      // String title = formatDate(data.get(0).getTimestamp(),
      // getSegmentation());
      updatePieChart("", pieData, colors, actualChart);
    } else {
      updatePieChart(constants.noDataAvailable(), pieData, colors, actualChart);
    }

  }

  protected void initPieChart(ChartWidget actualChart) {
    // Create chart data
    ChartData cd = new ChartData();
    PieChart pie = new PieChart();
    pie.setGradientFill(true);
    cd.addElements(pie);

    // Set style
    cd.setBackgroundColour(BACKGROUND);

    // Insert data
    actualChart.setJsonData(cd.toString());
  }

  protected void updatePieChart(String title, Map<String, Number> data, List<String> colours, ChartWidget actualChart) {

    // Create chart data
    ChartData cd = new ChartData(title, CHART_TITLE_STYLE);

    PieChart pie = new PieChart();
    pie.setAlpha(0.7f);
    pie.setNoLabels(true);
    pie.setTooltip(constants.chartPieTooltip());
    pie.setAnimate(false);
    pie.setGradientFill(true);

    // Generate slices
    for (Entry<String, Number> entry : data.entrySet()) {
      pie.addSlices(new PieChart.Slice(entry.getValue(), entry.getKey()));
    }
    cd.addElements(pie);

    // Set style
    cd.setBackgroundColour(BACKGROUND);
    pie.setColours(colours);

    // Insert data
    actualChart.setJsonData(cd.toString());

  }

  @Override
  public StatisticReportPanel getReportPanel() {
    return new StackedStatisticReportPanel(getTitle(), getType(), getDimension(), getSegmentation(),
      getReportFunctions());
  }

}
