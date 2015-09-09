package pt.gov.dgarq.roda.wui.management.statistics.client;

import java.util.List;

import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

import pt.gov.dgarq.roda.core.data.StatisticData;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.images.CommonImageBundle;
import pt.gov.dgarq.roda.wui.common.client.tools.Tools;

/**
 * Small sized panel for a simple statistics
 * 
 * @author Luis Faria
 * 
 */
public class SimpleStatisticMiniPanel extends StatisticMiniPanel {

  private static CommonImageBundle commonImageBundle = (CommonImageBundle) GWT.create(CommonImageBundle.class);

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private final DockPanel layout;
  private final HorizontalPanel header;
  private final Label title;
  private final Image report;
  private final HorizontalPanel centerLayout;
  private final Label description;
  private final Label value;

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
  public SimpleStatisticMiniPanel(String title, String description, String type, ValueDimension dimension,
    Segmentation segmentation, List<StatisticFunction> functions, List<StatisticFunction> reportFunctions) {
    super(title, description, type, dimension, segmentation, functions, reportFunctions);

    this.layout = new DockPanel();
    this.header = new HorizontalPanel();
    this.title = new Label(title);
    this.report = commonImageBundle.chart().createImage();
    this.centerLayout = new HorizontalPanel();
    this.description = new Label(description);
    this.value = new Label();

    header.add(this.title);
    header.add(report);

    centerLayout.add(this.description);
    centerLayout.add(value);

    this.layout.add(header, DockPanel.NORTH);
    this.layout.add(centerLayout, DockPanel.CENTER);

    report.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        showReport();
      }

    });

    initWidget(layout);
    update();

    this.layout.addStyleName("wui-statistic-mini-simple");
    this.header.addStyleName("mini-simple-header");
    this.title.addStyleName("mini-simple-title");
    this.report.addStyleName("mini-simple-report");
    this.centerLayout.addStyleName("mini-simple-center");
    this.description.addStyleName("mini-simple-description");
    this.value.addStyleName("mini-simple-value");

    this.header.setCellHorizontalAlignment(report, HasAlignment.ALIGN_RIGHT);
    this.centerLayout.setCellWidth(value, "100%");
    this.centerLayout.setCellHorizontalAlignment(value, HasAlignment.ALIGN_CENTER);

  }

  @Override
  protected FilterParameter getTypeFilterParameter() {
    return new SimpleFilterParameter("type", getType());
  }

  @Override
  public void update() {
    StatisticsService.Util.getInstance().getStatisticList(getContentAdapter(), getFunctions(), getSegmentation(),
      getInitialDate(), getFinalDate(), new AsyncCallback<List<StatisticData>>() {

        public void onFailure(Throwable caught) {
          logger.error("Could not get statistics list", caught);

        }

        public void onSuccess(List<StatisticData> statistics) {
          update(statistics);
        }

      });
  }

  protected void update(List<StatisticData> statistics) {
    StatisticData lastValidData = getLastValidStatistics(statistics);
    String actualValue;

    if (lastValidData != null) {
      if (getDimension().equals(ValueDimension.MILLISECONDS)) {
        long value = Long.parseLong(lastValidData.getValue());

        actualValue = Tools.formatValueMilliseconds(value, false);

        if (actualValue.length() == 0) {
          actualValue = "0";
        }

      } else {
        double value = Double.parseDouble(lastValidData.getValue());
        if (Math.round(value) == value) {
          // if value is an integer, make toString as an integer
          actualValue = "" + ((long) value);
        } else {
          actualValue = "" + value;
        }
      }
    } else {
      // TODO i18n
      actualValue = "-";
    }

    // update value
    value.setText(actualValue);

  }

  @Override
  public StatisticReportPanel getReportPanel() {
    return new SimpleStatisticReportPanel(getTitle(), getType(), getDimension(), getSegmentation(),
      getReportFunctions());
  }

}
