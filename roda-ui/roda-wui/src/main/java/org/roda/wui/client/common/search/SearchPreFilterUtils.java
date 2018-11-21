/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.search;

import java.util.List;

import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.LongRangeFilterParameter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.index.filter.OrFiltersParameters;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import config.i18n.client.ClientMessages;

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
      return messages.searchPreFilterLongRangeFilterParameter(messages.searchPreFilterName(p.getName()),
        p.getFromValue(), p.getToValue());
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
    } else {
      return SafeHtmlUtils.fromString(parameter.getClass().getSimpleName());
    }

    return null;
  }
}
