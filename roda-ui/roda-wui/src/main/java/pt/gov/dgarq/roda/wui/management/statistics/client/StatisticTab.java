/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.statistics.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.gov.dgarq.roda.wui.management.statistics.client.StatisticsPanel.ValueDimension;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;

import config.i18n.client.StatisticsConstants;

/**
 * @author Luis Faria
 * 
 */
public abstract class StatisticTab extends Composite {

  protected static final StatisticFunction AGGREGATION_LAST = new StatisticFunction(
    StatisticFunction.FunctionType.AGGREGATION_LAST);

  protected static final StatisticFunction DELTA = new StatisticFunction(StatisticFunction.FunctionType.DELTA);

  protected StatisticsConstants constants = (StatisticsConstants) GWT.create(StatisticsConstants.class);

  private boolean initialized;
  protected Segmentation segmentation;
  private SegmentationPicker selectSegmentation;

  public StatisticTab() {
    initialized = false;
  }

  protected StatisticMiniPanel createStatisticPanel(String title, String description, String statisticType,
    boolean stacked, StatisticFunction... functions) {
    return createStatisticPanel(title, description, statisticType, stacked, false, functions);
  }

  protected StatisticMiniPanel createStatisticPanel(String title, String description, String statisticType,
    boolean stacked, boolean deltaInReport, StatisticFunction... functions) {
    return createStatisticPanel(title, description, statisticType, ValueDimension.ADIMENSIONAL, stacked, deltaInReport,
      functions);

  }

  protected StatisticMiniPanel createStatisticPanel(String title, String description, String statisticType,
    ValueDimension dimension, boolean stacked, boolean deltaInReport, StatisticFunction... functions) {
    List<StatisticFunction> functionList = Arrays.asList(functions);
    List<StatisticFunction> reportFunctions = new ArrayList<StatisticFunction>(functionList);
    if (deltaInReport && !reportFunctions.contains(DELTA)) {
      reportFunctions.add(DELTA);
    }
    return createStatisticPanel(title, description, statisticType, dimension, stacked, functionList, reportFunctions);
  }

  protected StatisticMiniPanel createStatisticPanel(String title, String description, String statisticType,
    ValueDimension dimension, boolean stacked, List<StatisticFunction> functions,
    List<StatisticFunction> reportFunctions) {
    StatisticMiniPanel statistic;
    if (stacked) {
      statistic = new StackedStatisticMiniPanel(title, description, statisticType, dimension, segmentation, functions,
        reportFunctions);
    } else {
      statistic = new SimpleStatisticMiniPanel(title, description, statisticType, dimension, segmentation, functions,
        reportFunctions);
    }
    return statistic;
  }

  protected boolean init() {
    boolean ret = false;
    if (!initialized) {
      initialized = true;
      selectSegmentation = new SegmentationPicker();
      segmentation = selectSegmentation.getSegmentation();
      ret = true;
    }
    return ret;

  }

  protected void setCellsVerticalAlignment(FlexTable table, VerticalAlignmentConstant vc) {
    for (int i = 0; i < table.getRowCount(); i++) {
      for (int j = 0; j < table.getCellCount(i); j++) {
        table.getCellFormatter().setVerticalAlignment(i, j, vc);
      }
    }
  }

  public abstract String getTabText();

}
