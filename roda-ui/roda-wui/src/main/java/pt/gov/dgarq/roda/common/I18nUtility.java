package pt.gov.dgarq.roda.common;

import java.io.Serializable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import pt.gov.dgarq.roda.core.data.v2.FacetFieldResult;
import pt.gov.dgarq.roda.core.data.v2.FacetValue;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.wui.common.server.ServerTools;

public class I18nUtility {

  public static <T extends Serializable> IndexResult<T> translate(IndexResult<T> input, String localeString) {
    Locale locale = ServerTools.parseLocale(localeString);
    return translate(input, locale);
  }

  public static <T extends Serializable> IndexResult<T> translate(IndexResult<T> input, Locale locale) {
    IndexResult<T> output = input;
    if (output != null && output.getFacetResults() != null && output.getFacetResults().size() > 0) {
      for (FacetFieldResult ffr : output.getFacetResults()) {
        if (ffr != null && ffr.getValues() != null && ffr.getValues().size() > 0) {
          String field = ffr.getField();
          for (FacetValue fv : ffr.getValues()) {
            fv.setLabel(getFacetTranslation(field, fv.getValue(), locale));
          }
        }
      }
    }
    return output;
  }

  private static String getFacetTranslation(String facetField, String facetValue, Locale locale) {
    String bundleName = "config.i18n.server.Facets";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(bundleName, locale);
    String bundleKey = facetField + "." + facetValue;
    String ret;

    try {
      ret = resourceBundle.getString(bundleKey);
    } catch (MissingResourceException e) {
      ret = facetValue;
    }

    return ret;
  }

}
