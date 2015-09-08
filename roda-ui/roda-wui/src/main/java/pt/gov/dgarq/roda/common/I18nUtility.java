package pt.gov.dgarq.roda.common;

import java.util.Locale;
import java.util.ResourceBundle;

import pt.gov.dgarq.roda.core.data.v2.FacetFieldResult;
import pt.gov.dgarq.roda.core.data.v2.FacetValue;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;

public class I18nUtility {
  public static Object translate(Object input, Locale locale) {
    Object output = null;
    if (input instanceof IndexResult<?>) {
      IndexResult<?> cast = (IndexResult<?>) input;
      if (cast != null && cast.getFacetResults() != null && cast.getFacetResults().size() > 0) {
        for (FacetFieldResult ffr : cast.getFacetResults()) {
          if (ffr != null && ffr.getValues() != null && ffr.getValues().size() > 0) {
            for (FacetValue fv : ffr.getValues()) {
              fv.setLabel(getTranslation("index.results.facets." + fv.getValue(), locale));
            }
          }
        }

      }
      output = cast;
    }
    return output;
  }

  private static String getTranslation(String key, Locale locale) {
    String bundleName = "pt.gov.dgarq.roda.common/I18N";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(bundleName, locale);
    return resourceBundle.getString(key);
  }

}
