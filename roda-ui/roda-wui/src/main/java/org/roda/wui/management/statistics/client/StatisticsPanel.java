/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.management.statistics.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.MissingResourceException;

import org.roda.core.data.StatisticData;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.FilterParameter;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.LoadingPopup;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Composite;

import config.i18n.client.StatisticsConstants;

/**
 * @author Luis Faria
 * 
 */
public abstract class StatisticsPanel extends Composite {

  protected static final int Y_STEPS = 10;
  protected static final List<String> COLORS = Arrays.asList(new String[] {"#88AFBE", "#D7BC00", "#7e8a57", "#ff0000",
    "#00ff00", "#0000ff", "#ff9900", "#ff00ff", "#79C2FF"});
  protected static final String BACKGROUND = "#e6e7e9";
  protected static final String HALFGROUND = "#58595b";
  protected static final String FOREGROUND = "#000000";

  protected static final String CHART_ACTUAL_WIDTH = "250px";
  protected static final String CHART_HISTORY_WIDTH = "600px";
  protected static final String CHART_HEIGHT = "250px";

  protected static final String CHART_TITLE_STYLE = "font-size: 14px;"
    + " font-family: Verdana,Lucida,Helvetica,Arial,sans-serif;"
    + " text-align: center; color: #58595b; margin-bottom: 5px";

  public static final double DEFAULT_VALUE = -1;
  protected static final int MAX_VALUE_COUNT = 30;

  private static int baseColorIndex = 0;

  private static int getNextBaseColorIndex() {
    int index = baseColorIndex;
    baseColorIndex = (baseColorIndex + 1) % COLORS.size();
    return index;
  }

  /**
   * The value dimension or unit
   * 
   * @author Luis Faria
   * 
   */
  public enum ValueDimension {
    /**
     * No dimension or unit
     */
    ADIMENSIONAL, /**
     * Time in milliseconds
     */
    MILLISECONDS
  }

  private ClientLogger logger = new ClientLogger(getClass().getName());

  protected StatisticsConstants constants = (StatisticsConstants) GWT.create(StatisticsConstants.class);

  // variables
  private final String title;
  private final String type;
  private final ValueDimension dimension;
  private final List<StatisticFunction> functions;
  private Segmentation segmentation;
  private final int colorIndex;
  private final List<String> colors;

  private Date initialDate;
  private Date finalDate;

  protected final LoadingPopup loading;

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
  public StatisticsPanel(String title, String type, ValueDimension dimension, Segmentation segmentation,
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
   *          statistic functions, filler function will be added to the end of
   *          the list
   * @param segmentation
   */
  public StatisticsPanel(String title, String type, ValueDimension dimension, Segmentation segmentation,
    List<StatisticFunction> functions) {
    this.title = title;
    this.type = type;
    this.dimension = dimension;
    this.functions = new ArrayList<StatisticFunction>(functions);
    this.segmentation = segmentation;
    this.colorIndex = getNextBaseColorIndex();
    loading = new LoadingPopup(this);

    finalDate = new Date();
    initialDate = getPreferredInitialDate(segmentation, finalDate);

    logger.debug("Adding filler function");
    // show skipped dates as columns with value -1
    this.functions.add(new StatisticFunction(StatisticFunction.FunctionType.FILLER, "" + DEFAULT_VALUE));

    // set maximum number of results
    this.functions.add(new StatisticFunction(StatisticFunction.FunctionType.SAMPLING, "" + MAX_VALUE_COUNT));

    logger.debug("Setting color list");
    // Set new color list
    colors = new ArrayList<String>();
    int colorIndex = getColorIndex();
    // shift colors order
    for (int i = colorIndex; i < COLORS.size(); i++) {
      colors.add(COLORS.get(i));
    }
    for (int i = 0; i < colorIndex; i++) {
      colors.add(COLORS.get(i));
    }
  }

  /**
   * Updates all values from server
   */
  public abstract void update();

  /**
   * Get statistic panel title
   * 
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Get statistic type or regexp of types
   * 
   * @return the type or regexp of types
   */
  public String getType() {
    return type;
  }

  /**
   * Get the dimension (or unit) of the value
   * 
   * @return the dimension
   */
  public ValueDimension getDimension() {
    return dimension;
  }

  /**
   * Get statistic functions
   * 
   * @return a list of functions
   */
  public List<StatisticFunction> getFunctions() {
    return functions;
  }

  /**
   * Get current segmentation
   * 
   * @return {@link Segmentation}
   */
  public Segmentation getSegmentation() {
    return segmentation;
  }

  /**
   * Set segmentation
   * 
   * @param segmentation
   */
  public void setSegmentation(Segmentation segmentation) {
    this.segmentation = segmentation;
  }

  /**
   * Get initial date from where to show the statistics
   * 
   * @return the initial {@link Date}
   */
  public Date getInitialDate() {
    return initialDate;
  }

  /**
   * Set the initial date and update
   * 
   * @param initialDate
   */
  public void setInitialDate(Date initialDate) {
    this.initialDate = initialDate;
  }

  /**
   * Get final date up to where show the statistics
   * 
   * @return the final {@link Date}
   */
  public Date getFinalDate() {
    return finalDate;
  }

  /**
   * Set the final date and update
   * 
   * @param finalDate
   */
  public void setFinalDate(Date finalDate) {
    this.finalDate = finalDate;
  }

  /**
   * Get color index where to start picking colors
   * 
   * @return the index on {@link #COLORS}
   */
  protected int getColorIndex() {
    return colorIndex;
  }

  protected abstract FilterParameter getTypeFilterParameter();

//  protected ContentAdapter getContentAdapter() {
//    Filter filter = new Filter();
//    filter.add(getTypeFilterParameter());
//
//    ContentAdapter adapter = new ContentAdapter();
//    adapter.setFilter(filter);
//    return adapter;
//  }

  private static final DateTimeFormat YEAR_FORMAT = DateTimeFormat.getFormat("yyyy");
  private static final DateTimeFormat MONTH_FORMAT = DateTimeFormat.getFormat("yyyy-MM");
  private static final DateTimeFormat DAY_FORMAT = DateTimeFormat.getFormat("yyyy-MM-dd");

  protected String formatDate(Date date, Segmentation segmentation) {
    String dateFormat;

    if (segmentation.equals(Segmentation.YEAR)) {
      dateFormat = YEAR_FORMAT.format(date);
    } else if (segmentation.equals(Segmentation.MONTH)) {
      dateFormat = MONTH_FORMAT.format(date);
    } else if (segmentation.equals(Segmentation.DAY)) {
      dateFormat = DAY_FORMAT.format(date);
    } else {
      dateFormat = Tools.formatDate(date);
    }
    return dateFormat;
  }

  protected Date getPreferredInitialDate(Segmentation segmentation, Date finalDate) {
    Date initialDate;

    if (segmentation.equals(Segmentation.DAY)) {
      initialDate = new Date(finalDate.getTime() - 30 * segmentation2Millis(Segmentation.DAY));
    } else if (segmentation.equals(Segmentation.MONTH)) {
      initialDate = new Date(finalDate.getTime() - 12 * segmentation2Millis(Segmentation.MONTH));
    } else {
      initialDate = new Date(finalDate.getTime() - 10 * segmentation2Millis(Segmentation.YEAR));
    }

    return initialDate;
  }

  protected long segmentation2Millis(Segmentation segmentation) {
    long millis = 0;
    if (segmentation.equals(Segmentation.DAY)) {
      millis = 24 * 60 * 60 * 1000;
    } else if (segmentation.equals(Segmentation.MONTH)) {
      millis = 30 * 24 * 60 * 60 * 1000;
    } else if (segmentation.equals(Segmentation.YEAR)) {
      millis = 31536000000l;
    }

    return millis;
  }

  /**
   * Get the last valid statistics
   * 
   * @return the last valid statistics or null if none exists
   */
  protected StatisticData getLastValidStatistics(List<StatisticData> statistics) {
    StatisticData ret = null;
    if (statistics != null) {
      for (int i = statistics.size() - 1; i >= 0; i--) {
        StatisticData data = statistics.get(i);
        double value = Double.valueOf(data.getValue());
        if (value != DEFAULT_VALUE) {
          ret = data;
          break;
        }
      }
    }

    return ret;
  }

  /**
   * Get the last valid statistics
   * 
   * @return the last valid statistics or null if none exists
   */
  protected List<StatisticData> getLastValidStackedStatistics(List<List<StatisticData>> statistics) {
    List<StatisticData> ret = new ArrayList<StatisticData>();

    for (List<StatisticData> infos : statistics) {
      StatisticData lastData = infos.get(infos.size() - 1);
      ret.add(lastData);
    }

    return ret;
  }

  protected List<String> getColors() {
    return colors;
  }

  protected String getTypeTranslated(String type) {
    String ret;
    if (type.contains("*")) {
      ret = constants.statistic_type_others();
    } else {
      try {
        ret = constants.getString(type.replace('.', '_'));
      } catch (MissingResourceException e) {
        int dotIndex = type.lastIndexOf('.');
        if (dotIndex >= 0) {
          ret = type.substring(dotIndex + 1);
        } else {
          ret = type;
        }
      }
    }
    return ret;
  }
}
