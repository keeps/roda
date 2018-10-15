/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.OrFiltersParameters;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;

public class SearchFilters {

  private SearchFilters() {
    // do nothing
  }

  public static Filter allFilter() {
    return new Filter(new BasicSearchFilterParameter(searchField(), RodaConstants.INDEX_WILDCARD));
  }

  public static String searchField() {
    return RodaConstants.INDEX_SEARCH;
  }

  public static Filter createIncrementalFilterFromTokens(final List<String> historyTokens, Filter baseFilter) {
    // historyTokens like TYPE/key/value/key/value or key/value/key/value

    Filter resultingFilter = baseFilter == null ? new Filter(Filter.ALL) : new Filter(baseFilter);

    List<String> parts = new ArrayList<>(historyTokens);
    if (!parts.isEmpty()) {
      String operator = RodaConstants.OPERATOR_AND;
      if (parts.size() % 2 == 1) {
        operator = parts.remove(0);
      }

      if (parts.size() % 2 == 0 && operator.equals(RodaConstants.OPERATOR_AND)
        || operator.equals(RodaConstants.OPERATOR_OR)) {
        List<FilterParameter> filterParameterList = new ArrayList<>();

        for (int i = 0; i < parts.size() - 1; i += 2) {
          String key = parts.get(i);
          String value = parts.get(i + 1);
          filterParameterList.add(new SimpleFilterParameter(key, value));
        }

        if (!filterParameterList.isEmpty()) {
          if (RodaConstants.OPERATOR_AND.equals(operator)) {
            resultingFilter.add(filterParameterList);
          } else {
            resultingFilter.add(new OrFiltersParameters(filterParameterList));
          }
        }
      }
    }

    return resultingFilter;
  }

  public static Filter createFilterFromHistoryTokens(final List<String> historyTokens) {
    return createIncrementalFilterFromTokens(historyTokens, null);
  }

  public static boolean shouldBeIncremental(final Filter filter) {
    return !filter.getParameters().isEmpty() && !SearchFilters.allFilter().equals(filter);
  }

  public static String classesToHistoryTokens(List<Class> classes) {
    StringBuilder result = new StringBuilder();
    for (Class aClass : classes) {
      result.append("@").append(aClass.getSimpleName());
    }
    return result.length() == 0 ? "@Void" : result.toString();
  }

  public static String classesToHistoryTokens(Class... classes) {
    return classesToHistoryTokens(Arrays.asList(classes));
  }
}
