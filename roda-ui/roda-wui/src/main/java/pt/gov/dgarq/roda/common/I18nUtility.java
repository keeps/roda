package pt.gov.dgarq.roda.common;

import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;

import pt.gov.dgarq.roda.core.data.v2.FacetFieldResult;
import pt.gov.dgarq.roda.core.data.v2.FacetValue;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;

public class I18nUtility {
  
  public static <T extends Serializable> IndexResult<T> translate(Class<T> responseClass, IndexResult<T> input,
    Locale locale) {
    IndexResult<T> output = input;
    if (output != null && output.getFacetResults() != null && output.getFacetResults().size() > 0) {
      for (FacetFieldResult ffr : output.getFacetResults()) {
        if (ffr != null && ffr.getValues() != null && ffr.getValues().size() > 0) {
          for (FacetValue fv : ffr.getValues()) {
            fv.setLabel(getTranslation("index.results.facets.", fv.getValue(), locale));
          }
        }
      }
    }
    return output;
  }

  private static String getTranslation(String keyNamespace, String key, Locale locale) {
    String bundleName = "config.i18n.server.Facets";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(bundleName, locale);
    String bundleKey = keyNamespace + key;
    String ret;
    if (StringUtils.isNotBlank(resourceBundle.getString(bundleKey))) {
      ret = resourceBundle.getString(bundleKey);
    } else {
      ret = key;
    }
    return ret;
  }

}
