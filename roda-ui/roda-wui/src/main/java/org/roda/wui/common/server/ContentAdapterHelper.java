/**
 * 
 */
package org.roda.wui.common.server;

import java.util.List;
import java.util.Locale;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.FilterParameter;
import org.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import org.roda.core.data.adapter.filter.RangeFilterParameter;
import org.roda.core.data.adapter.filter.RegexFilterParameter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sort.SortParameter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;

import config.i18n.server.ContentAdapterHelperMessages;

/**
 * @author Luis Faria
 * 
 */
public class ContentAdapterHelper {

  /**
   * Translate the content adapter sub list
   * 
   * @param subList
   *          the sub list
   * @param total
   *          the total number of elements (filtered)
   * @param locale
   *          the target locale
   * @return
   */
  public static String translateSubList(Sublist subList, int total, Locale locale) {
    ContentAdapterHelperMessages messages = new ContentAdapterHelperMessages(locale);
    int start = subList.getFirstElementIndex();
    int limit = subList.getMaximumElementCount();
    String ret;
    if (start == 0) {
      if (limit >= total) {
        ret = String.format(messages.getString("subList.all"), total);
      } else {
        ret = String.format(messages.getString("subList.first"), limit, total);
      }
    } else {
      int end = (start + limit >= total) ? total : start + limit;
      ret = String.format(messages.getString("subList.interval"), start, end, total);

    }
    return ret;
  }

  /**
   * Translate the content adapter sorter
   * 
   * @param sorter
   *          the Sorter
   * @param content
   *          the report content source to translate field names
   * @param locale
   *          the target locale
   * @return
   */
  public static String translateSorter(Sorter sorter, ReportContentSource<Object> content, Locale locale) {
    ContentAdapterHelperMessages messages = new ContentAdapterHelperMessages(locale);
    String ret;
    SortParameter[] parameters = sorter.getParameters();
    if (parameters.length > 0) {
      ret = messages.getString("sorted.list.start");
      boolean first = true;
      for (SortParameter parameter : parameters) {
        if (first) {
          first = false;
        } else {
          ret += messages.getString("sorted.list.inc");
        }
        if (parameter.isDescending()) {
          ret += String.format(messages.getString("sorted.list.descending"),
            content.getFieldNameTranslation(parameter.getName()));
        } else {
          ret += String.format(messages.getString("sorted.list.ascending"),
            content.getFieldNameTranslation(parameter.getName()));
        }
      }

    } else {
      ret = messages.getString("sorted.not");
    }

    return ret;
  }

  /**
   * Translate the content adapter filter
   * 
   * @param filter
   *          the filter
   * @param content
   *          the report content source to translate field names and values
   * @param locale
   *          the target locale
   * @return
   */
  public static String translateFilter(Filter filter, ReportContentSource<Object> content, Locale locale) {
    ContentAdapterHelperMessages messages = new ContentAdapterHelperMessages(locale);
    String ret;
    List<FilterParameter> parameters = filter.getParameters();

    if (parameters != null && parameters.size() > 0) {
      ret = "";
      boolean first = true;
      for (FilterParameter parameter : parameters) {
        if (first) {
          first = false;
        } else {
          ret += messages.getString("filter.inc");
        }
        ret += translateFilterParameter(parameter, content, messages);
      }
    } else {
      ret = messages.getString("filter.not");
    }

    return ret;
  }

  private static String translateFilterParameter(FilterParameter parameter, ReportContentSource<Object> content,
    ContentAdapterHelperMessages messages) {
    String ret;
    if (parameter instanceof SimpleFilterParameter) {
      SimpleFilterParameter simple = (SimpleFilterParameter) parameter;
      ret = String.format(messages.getString("filter.simple"), content.getFieldNameTranslation(simple.getName()),
        content.getFieldValueTranslation(simple.getValue()));
    } else if (parameter instanceof OneOfManyFilterParameter) {
      OneOfManyFilterParameter oneOfMany = (OneOfManyFilterParameter) parameter;
      String list = "";
      boolean first = true;
      for (String value : oneOfMany.getValues()) {
        if (first) {
          first = false;
        } else {
          list += messages.getString("filter.oneOfMany.inc");
        }
        list += content.getFieldValueTranslation(value);
      }
      ret = String.format(messages.getString("filter.oneOfMany"), content.getFieldNameTranslation(oneOfMany.getName()),
        list);

    } else if (parameter instanceof RangeFilterParameter) {
      RangeFilterParameter range = (RangeFilterParameter) parameter;
      ret = String.format(messages.getString("filter.range"), content.getFieldNameTranslation(range.getName()),
        range.getFromValue(), range.getToValue());

    } else if (parameter instanceof RegexFilterParameter) {
      RegexFilterParameter regex = (RegexFilterParameter) parameter;
      ret = String.format(messages.getString("filter.regex"), content.getFieldNameTranslation(regex.getName()),
        regex.getRegex());

    } else {
      ret = "!!!NO TRANSLATION FOR FILTER TYPE " + parameter.getClass().getSimpleName() + "!!!";
    }

    return ret;
  }
}
