package pt.gov.dgarq.roda.common;

import java.io.Serializable;
import java.util.Locale;
import java.util.MissingResourceException;

import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.data.v2.FacetFieldResult;
import pt.gov.dgarq.roda.core.data.v2.FacetValue;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.RODAMember;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.wui.common.server.ServerTools;

public class I18nUtility {

  public static <T extends Serializable> IndexResult<T> translate(IndexResult<T> input, Class<T> resultClass,
    String localeString) {
    Locale locale = ServerTools.parseLocale(localeString);
    return translate(input, resultClass, locale);
  }

  public static <T extends Serializable> IndexResult<T> translate(IndexResult<T> input, Class<T> resultClass,
    Locale locale) {
    IndexResult<T> output = input;
    if (output != null && output.getFacetResults() != null && output.getFacetResults().size() > 0) {
      for (FacetFieldResult ffr : output.getFacetResults()) {
        if (ffr != null && ffr.getValues() != null && ffr.getValues().size() > 0) {
          String field = ffr.getField();
          for (FacetValue fv : ffr.getValues()) {
            fv.setLabel(getFacetTranslation(field, fv.getValue(), locale, resultClass));
          }
        }
      }
    }
    return output;
  }

  private static <T extends Serializable> String getFacetTranslation(String facetField, String facetValue,
    Locale locale, Class<T> resultClass) {
    String bundleKey = getPrefix(resultClass) + facetField + "." + facetValue;
    String ret;

    try {
      ret = RodaCoreFactory.getI18NMessages(locale).getTranslation(bundleKey);
    } catch (MissingResourceException e) {
      ret = facetValue;
    }

    return ret;
  }

  private static <T> String getPrefix(Class<T> resultClass) {
    String prefix;
    if (resultClass.equals(SimpleDescriptionObject.class)) {
      prefix = RodaConstants.I18N_UI_SEARCH_FACETS_PREFIX;

    } else if (resultClass.equals(RODAMember.class)) {
      prefix = RodaConstants.I18N_UI_USER_ADMINISTRATION_FACETS_PREFIX;
    } else {
      // FIXME
      prefix = "";
    }
    return prefix;
  }

}
