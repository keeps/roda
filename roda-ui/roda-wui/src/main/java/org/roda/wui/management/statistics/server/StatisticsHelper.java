/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.management.statistics.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.roda.core.data.StatisticData;
import org.roda.wui.management.statistics.client.Segmentation;
import org.roda.wui.management.statistics.client.StatisticFunction;
import org.roda.wui.management.statistics.client.StatisticsPanel;

/**
 * Statistics Helper
 * 
 * @author Luis Faria
 * 
 */
public class StatisticsHelper {

  private static final Logger logger = Logger.getLogger(StatisticsHelper.class);

  /**
   * Apply statistic functions
   * 
   * @param entries
   * @param functions
   * @param segmentation
   * @param finalDate
   * @return the result entry list
   */
  public List<StatisticData> applyFunctions(List<StatisticData> entries, List<StatisticFunction> functions,
    Segmentation segmentation, Date initialDate, Date finalDate) {
    List<StatisticData> ret = entries;
    if (entries != null && entries.size() > 0) {
      for (StatisticFunction function : functions) {

        if (function.getFunction().equals(StatisticFunction.FunctionType.AGGREGATION_ADD)) {
          ret = aggregate(ret, function.getArguments(), AggregateFunction.ADD, segmentation);

        } else if (function.getFunction().equals(StatisticFunction.FunctionType.AGGREGATION_LAST)) {
          ret = aggregate(ret, function.getArguments(), AggregateFunction.LAST, segmentation);

        } else if (function.getFunction().equals(StatisticFunction.FunctionType.AGGREGATION_AVERAGE)) {
          ret = aggregate(ret, function.getArguments(), AggregateFunction.AVERAGE, segmentation);

        } else if (function.getFunction().equals(StatisticFunction.FunctionType.DELTA)) {
          ret = delta(ret, function.getArguments());

        } else if (function.getFunction().equals(StatisticFunction.FunctionType.FILLER)) {
          ret = filler(ret, function.getArguments(), segmentation, initialDate, finalDate);

        } else if (function.getFunction().equals(StatisticFunction.FunctionType.SAMPLING)) {
          ret = sampling(ret, function.getArguments());

        } else {
          throw new UnsupportedOperationException("Statistic function type not supported: " + function.getFunction());
        }
      }
    }
    return ret;
  }

  /**
   * Group entries by date
   * 
   * @param entries
   * @param segmentation
   * @return entries grouped by type
   */
  public List<List<StatisticData>> groupByDate(List<StatisticData> entries, Segmentation segmentation) {
    List<List<StatisticData>> ret = new ArrayList<List<StatisticData>>();
    List<StatisticData> bag = new ArrayList<StatisticData>(entries);
    SegmentationComparator comparator = getComparator(segmentation);
    while (!bag.isEmpty()) {
      // Get a pivot
      StatisticData pivot = bag.remove(0);
      // Create a aggregation list
      List<StatisticData> group = new ArrayList<StatisticData>();
      group.add(pivot);
      // Add items to aggregation list
      for (StatisticData entry : bag) {
        if (comparator.equals(pivot.getTimestamp(), entry.getTimestamp())) {
          group.add(entry);
        }
      }

      // Remove aggregation list from bag
      bag.removeAll(group);

      // Add group to result list
      ret.add(group);

    }
    return ret;
  }

  /**
   * Group entries by type
   * 
   * @param entries
   * @return entries grouped by type
   */
  public List<List<StatisticData>> groupByType(List<StatisticData> entries) {
    List<List<StatisticData>> ret = new ArrayList<List<StatisticData>>();
    List<StatisticData> bag = new ArrayList<StatisticData>(entries);
    while (!bag.isEmpty()) {
      // Get a pivot
      StatisticData pivot = bag.remove(0);
      // Create a aggregation list
      List<StatisticData> group = new ArrayList<StatisticData>();
      group.add(pivot);
      // Add items to aggregation list
      for (StatisticData entry : bag) {
        if (pivot.getType().equals(entry.getType())) {
          group.add(entry);
        }
      }

      // Remove aggregation list from bag
      bag.removeAll(group);

      // Add group to result list
      ret.add(group);

    }
    return ret;
  }

  private enum AggregateFunction {
    ADD, LAST, AVERAGE
  }

  private interface SegmentationComparator {
    public boolean equals(Date d1, Date d2);
  }

  private final SegmentationComparator YEAR_COMPARATOR = new SegmentationComparator() {

    public boolean equals(Date d1, Date d2) {
      Calendar c1 = Calendar.getInstance();
      c1.setTime(d1);
      Calendar c2 = Calendar.getInstance();
      c2.setTime(d2);

      return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR);
    }

  };

  private final SegmentationComparator MONTH_COMPARATOR = new SegmentationComparator() {

    public boolean equals(Date d1, Date d2) {
      Calendar c1 = Calendar.getInstance();
      c1.setTime(d1);
      Calendar c2 = Calendar.getInstance();
      c2.setTime(d2);

      return YEAR_COMPARATOR.equals(d1, d2) && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH);
    }

  };

  private final SegmentationComparator DAY_COMPARATOR = new SegmentationComparator() {

    public boolean equals(Date d1, Date d2) {
      Calendar c1 = Calendar.getInstance();
      c1.setTime(d1);
      Calendar c2 = Calendar.getInstance();
      c2.setTime(d2);

      return MONTH_COMPARATOR.equals(d1, d2) && c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH);
    }

  };

  private SegmentationComparator getComparator(Segmentation segmentation) {
    if (segmentation.equals(Segmentation.YEAR)) {
      return YEAR_COMPARATOR;

    } else if (segmentation.equals(Segmentation.MONTH)) {
      return MONTH_COMPARATOR;

    } else if (segmentation.equals(Segmentation.DAY)) {
      return DAY_COMPARATOR;
    } else {
      throw new IllegalArgumentException("No comparator for segmentation type: " + segmentation);
    }
  }

  /**
   * Get calendar field at the same level of segmentation
   * 
   * @param segmentation
   * @return the {@link Calendar} field
   */
  public int getCalendarField(Segmentation segmentation) {
    if (segmentation.equals(Segmentation.YEAR)) {
      return Calendar.YEAR;

    } else if (segmentation.equals(Segmentation.MONTH)) {
      return Calendar.MONTH;

    } else if (segmentation.equals(Segmentation.DAY)) {
      return Calendar.DAY_OF_YEAR;
    } else {
      throw new IllegalArgumentException("No comparator for segmentation type: " + segmentation);
    }
  }

  private List<StatisticData> aggregate(List<StatisticData> entries, String[] arguments,
    AggregateFunction aggregateFunc, Segmentation segmentation) {
    List<StatisticData> ret = null;

    SegmentationComparator comparator = getComparator(segmentation);
    ret = aggregate(entries, comparator, aggregateFunc);

    return ret;
  }

  private List<StatisticData> aggregate(List<StatisticData> entries, SegmentationComparator segComparator,
    AggregateFunction aggregateFunc) {
    List<StatisticData> ret = new ArrayList<StatisticData>();
    List<StatisticData> bag = new ArrayList<StatisticData>(entries);
    while (!bag.isEmpty()) {
      // Get a pivot
      StatisticData pivot = bag.remove(0);
      // Create a aggregation list
      List<StatisticData> aggregated = new ArrayList<StatisticData>();
      aggregated.add(pivot);
      // Add items to aggregation list
      for (StatisticData entry : bag) {
        if (pivot.getType().equals(entry.getType()) && segComparator.equals(pivot.getTimestamp(), entry.getTimestamp())) {
          aggregated.add(entry);
        } else {
          break;
        }
      }

      // Aggregate values
      StatisticData aggregatedEntry = aggregateValue(aggregated, aggregateFunc);

      // Remove aggregation list from bag
      bag.removeAll(aggregated);

      // Add aggregated entry to result list
      ret.add(aggregatedEntry);

    }

    return ret;
  }

  private StatisticData aggregateValue(List<StatisticData> aggregated, AggregateFunction aggregateFunc) {
    StatisticData ret = new StatisticData();
    ret.setTimestamp(aggregated.get(0).getTimestamp());
    ret.setType(aggregated.get(0).getType());
    if (aggregateFunc.equals(AggregateFunction.ADD)) {
      double total = 0;
      for (StatisticData entry : aggregated) {
        total += Double.parseDouble(entry.getValue());
      }
      ret.setValue("" + total);
    } else if (aggregateFunc.equals(AggregateFunction.AVERAGE)) {
      double total = 0;
      for (StatisticData entry : aggregated) {
        total += Double.parseDouble(entry.getValue());
      }
      ret.setValue("" + (total / aggregated.size()));
    } else if (aggregateFunc.equals(AggregateFunction.LAST)) {
      ret.setValue(aggregated.get(aggregated.size() - 1).getValue());
    } else {
      throw new IllegalArgumentException("Aggregate function not implemented: " + aggregateFunc);
    }
    return ret;
  }

  private List<StatisticData> delta(List<StatisticData> entries, String[] arguments) {
    List<StatisticData> ret;
    if (entries.size() > 1) {
      ret = new ArrayList<StatisticData>();
      for (int i = 0; i < entries.size() - 1; i++) {
        StatisticData deltaEntry = new StatisticData();
        StatisticData e1 = entries.get(i);
        StatisticData e2 = entries.get(i + 1);

        // Set parameters based on the first item
        deltaEntry.setTimestamp(e2.getTimestamp());
        deltaEntry.setType(e2.getType());
        // Calculate delta of values
        double value = Double.parseDouble(e2.getValue()) - Double.parseDouble(e1.getValue());

        // Set value
        deltaEntry.setValue("" + value);

        // Add entry to result
        ret.add(deltaEntry);
      }

    } else {
      ret = new ArrayList<StatisticData>(entries);
    }
    return ret;
  }

  private List<StatisticData> filler(List<StatisticData> entries, String[] args, Segmentation segmentation,
    Date initialDate, Date finalDate) {
    List<StatisticData> ret;
    if (args.length == 1) {
      String value = args[0];
      String type = entries.get(0).getType();
      int calendarField = getCalendarField(segmentation);
      SegmentationComparator comparator = getComparator(segmentation);
      ret = new ArrayList<StatisticData>();

      Calendar c = Calendar.getInstance();
      c.setTime(initialDate);

      Calendar cf = Calendar.getInstance();
      cf.setTime(finalDate);
      cf.add(calendarField, 1);

      int entriesIndex = 0;
      // logger.debug("init date=" + c.getTime());
      while (c.compareTo(cf) <= 0) {
        if (entries.size() <= entriesIndex) {
          ret.add(new StatisticData(c.getTime(), type, value));
        } else {
          StatisticData entry = entries.get(entriesIndex);
          if (comparator.equals(c.getTime(), entry.getTimestamp())) {
            ret.add(entry);
            entriesIndex++;
          } else {
            ret.add(new StatisticData(c.getTime(), type, value));
          }
        }
        c.add(calendarField, 1);
      }

      logger.debug("filler seg=" + segmentation + " initial" + initialDate + " final=" + finalDate + " args="
        + Arrays.asList(args) + " entries=\n" + entries + " result=\n" + ret);

    } else {
      throw new IllegalArgumentException(Arrays.toString(args));
    }
    return ret;
  }

  private List<StatisticData> sampling(List<StatisticData> entries, String[] args) {
    List<StatisticData> ret;
    if (args.length == 1) {
      ret = new ArrayList<StatisticData>();
      int size = Integer.valueOf(args[0]);
      int step = entries.size() / size;

      if (step > 1) {
        for (int i = 0; i < entries.size(); i += step) {
          StatisticData data = new StatisticData();
          data.setTimestamp(entries.get(i).getTimestamp());
          data.setType(entries.get(i).getType());
          double valueSum = 0;
          int count = 0;
          for (int j = i; j < i + step && j < entries.size(); j++) {
            Double value = Double.valueOf(entries.get(j).getValue());
            if (value != StatisticsPanel.DEFAULT_VALUE) {
              valueSum += Double.valueOf(entries.get(j).getValue());
              count++;
            }
          }

          double value = count == 0 ? -1 : valueSum / count;
          data.setValue(Double.toString(value));
          ret.add(data);
        }
      } else {
        ret.addAll(entries);
      }

    } else {
      throw new IllegalArgumentException(Arrays.toString(args));
    }
    return ret;
  }

}
