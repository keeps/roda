/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.search;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import org.roda.core.data.v2.index.filter.AllFilterParameter;
import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.DateIntervalFilterParameter;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.LongRangeFilterParameter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.index.filter.OrFiltersParameters;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;

import config.i18n.client.ClientMessages;
import org.roda.wui.common.client.tools.Humanize;

public class SearchPreFilterUtils {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static SafeHtml getFilterHTML(Filter filter, String classToFilter) {
    List<FilterParameter> parameterValues = filter.getParameters();
    SafeHtmlBuilder preFilterTranslations = new SafeHtmlBuilder();
    preFilterTranslations.append(SafeHtmlUtils.fromSafeConstant(messages.allOfAObject(classToFilter)))
      .append(messages.searchPreFilterWhere()).append(SafeHtmlUtils.fromSafeConstant(":<ul><li>"));

    for (int i = 0; i < parameterValues.size(); i++) {
      preFilterTranslations.append(getFilterParameterHTML(parameterValues.get(i)));

      preFilterTranslations.append(SafeHtmlUtils.fromSafeConstant("</li>"));
      if (i != parameterValues.size() - 1) {
        preFilterTranslations.append(SafeHtmlUtils.fromSafeConstant("<li>"));
      }
    }

    preFilterTranslations.append(SafeHtmlUtils.fromSafeConstant("</ul>"));
    return preFilterTranslations.toSafeHtml();
  }

  public static SafeHtml getFilterParameterHTML(FilterParameter parameter) {
    if (parameter instanceof SimpleFilterParameter) {
      SimpleFilterParameter p = (SimpleFilterParameter) parameter;
      return messages.searchPreFilterSimpleFilterParameter(messages.searchPreFilterName(p.getName()),
        messages.searchPreFilterValue(p.getValue()));
    } else if (parameter instanceof BasicSearchFilterParameter) {
      BasicSearchFilterParameter p = (BasicSearchFilterParameter) parameter;
      // TODO put '*' in some constant, see Search
      if (!"*".equals(p.getValue())) {
        return messages.searchPreFilterBasicSearchFilterParameter(messages.searchPreFilterName(p.getName()),
          messages.searchPreFilterValue(p.getValue()));
      }
    } else if (parameter instanceof NotSimpleFilterParameter) {
      NotSimpleFilterParameter p = (NotSimpleFilterParameter) parameter;
      return messages.searchPreFilterNotSimpleFilterParameter(messages.searchPreFilterName(p.getName()),
        messages.searchPreFilterValue(p.getValue()));
    } else if (parameter instanceof EmptyKeyFilterParameter) {
      EmptyKeyFilterParameter p = (EmptyKeyFilterParameter) parameter;
      return messages.searchPreFilterEmptyKeyFilterParameter(messages.searchPreFilterName(p.getName()));
    } else if (parameter instanceof LongRangeFilterParameter) {
      LongRangeFilterParameter p = (LongRangeFilterParameter) parameter;
      if (p.getFromValue() != null && p.getToValue() != null) {
        return messages.searchPreFilterLongRangeFilterParameter(messages.searchPreFilterName(p.getName()),
          p.getFromValue(), p.getToValue());
      } else if (p.getFromValue() != null) {
        return messages.searchPreFilterLongRangeFilterParameterGreaterThan(messages.searchPreFilterName(p.getName()),
          p.getFromValue());
      } else if (p.getToValue() != null) {
        return messages.searchPreFilterLongRangeFilterParameterSmallerThan(messages.searchPreFilterName(p.getName()),
          p.getToValue());
      } else {
        return null;
      }

    } else if (parameter instanceof OneOfManyFilterParameter) {
      OneOfManyFilterParameter p = (OneOfManyFilterParameter) parameter;
      int listSize = p.getValues().size();

      if (listSize == 1) {
        return messages.searchPreFilterOneOfManyFilterParameterSingle(messages.searchPreFilterName(p.getName()),
          p.getValues().get(0));
      } else if (listSize < 5) {
        return messages.searchPreFilterOneOfManyFilterParameterWithList(messages.searchPreFilterName(p.getName()),
          p.getValues());
      } else {
        return messages.searchPreFilterOneOfManyFilterParameterWithSize(messages.searchPreFilterName(p.getName()),
          p.getValues().size());
      }
    } else if (parameter instanceof OrFiltersParameters) {
      List<FilterParameter> parameterValues = ((OrFiltersParameters) parameter).getValues();
      SafeHtmlBuilder preFilterTranslations = new SafeHtmlBuilder();

      for (int i = 0; i < parameterValues.size(); i++) {
        preFilterTranslations.append(getFilterParameterHTML(parameterValues.get(i)));

        if (i != parameterValues.size() - 1) {
          preFilterTranslations.append(messages.searchPreFilterOr());
        }
      }

      return preFilterTranslations.toSafeHtml();
    } else if (parameter instanceof DateIntervalFilterParameter) {
      DateIntervalFilterParameter p = (DateIntervalFilterParameter) parameter;
      if (p.getFromValue() != null && p.getToValue() != null) {
        String fromDateHumanized = Humanize.formatDate(p.getFromValue());
        String toDateHumanized = Humanize.formatDate(p.getToValue());
        return messages.searchPreFilterDateIntervalFilterParameter(messages.searchPreFilterName(p.getFromName()),
          fromDateHumanized, toDateHumanized);
      } else if (p.getToValue() == null) {
        String fromDateHumanized = Humanize.formatDate(p.getFromValue());
        return messages.searchPreFilterDateIntervalFilterParameterFrom(messages.searchPreFilterName(p.getFromName()),
          fromDateHumanized);
      } else {
        String toDateHumanized = Humanize.formatDate(p.getToValue());
        return messages.searchPreFilterDateIntervalFilterParameterTo(messages.searchPreFilterName(p.getFromName()),
          toDateHumanized);
      }
    } else {
      return SafeHtmlUtils.fromString(parameter.getClass().getSimpleName());
    }

    return null;
  }

  /**
   * Generates a plain text representation of the filter.
   * Replaces the HTML <ul>/<li> structure with a simple semicolon-separated string.
   */
  public static String getFilterText(Filter filter, String classToFilter) {
    if (filter == null || filter.getParameters().isEmpty()) {
      return "";
    }

    // 1. Build the prefix safely using GWT's builder to avoid the `safe: ""` bug
    SafeHtmlBuilder prefixBuilder = new SafeHtmlBuilder();
    prefixBuilder.append(SafeHtmlUtils.fromSafeConstant(messages.allOfAObject(classToFilter)))
            .append(messages.searchPreFilterWhere())
            .append(SafeHtmlUtils.fromSafeConstant(": "));

    // 2. Convert to a raw string and clean up HTML/entities
    String prefix = prefixBuilder.toSafeHtml().asString()
            .replaceAll("<[^>]+>", "")
            .replace("&nbsp;", " ")
            .trim() + " ";

    List<FilterParameter> parameterValues = filter.getParameters();
    StringBuilder plainTextBuilder = new StringBuilder();
    plainTextBuilder.append(prefix);

    for (int i = 0; i < parameterValues.size(); i++) {
      String paramText = getFilterParameterText(parameterValues.get(i));

      // Skip empty parameters so we don't end up with weird semicolons
      if (!paramText.isEmpty()) {
        if (i > 0 && plainTextBuilder.charAt(plainTextBuilder.length() - 1) != ' ') {
          plainTextBuilder.append("; ");
        }
        plainTextBuilder.append(paramText);
      }
    }

    return plainTextBuilder.toString();
  }

  /**
   * Reuses the existing HTML generation logic but strips all HTML tags
   * and unescapes basic HTML entities to return pure plain text.
   */
  public static String getFilterParameterText(FilterParameter parameter) {
    SafeHtml htmlValue = getFilterParameterHTML(parameter);

    if (htmlValue == null || htmlValue.asString().trim().isEmpty()) {
      return "";
    }

    // 1. Convert SafeHtml to a raw string
    // 2. Strip all HTML tags (like <span> and </span>)
    // 3. Replace common HTML entities back to plain text
    return htmlValue.asString()
            .replaceAll("<[^>]+>", "")  // Removes all tags
            .replace("&nbsp;", " ")     // Standardize spaces
            .replace("&lt;", "<")       // Unescape brackets if user searched for them
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .trim();
  }

  private SearchPreFilterUtils() {
    // private constructor
  }
}
