package org.roda.wui.client.disposal.schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.common.client.tools.ConfigurationManager;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalScheduleUtils {

  public static String getI18nRetentionTriggerIdentifier(String fieldName) {
    List<Pair<String, String>> elementsFromConfig = getElementsFromConfig();

    for (Pair<String, String> pair : elementsFromConfig) {
      if (pair.getFirst().equals(fieldName)) {
        return pair.getSecond();
      }
    }

    return fieldName;
  }

  public static List<Pair<String, String>> getElementsFromConfig() {
    List<Pair<String, String>> elements = new ArrayList<>();
    String classSimpleName = IndexedAIP.class.getSimpleName();
    List<String> fields = ConfigurationManager.getStringList(RodaConstants.SEARCH_FIELD_PREFIX, classSimpleName);

    for (String field : fields) {
      String fieldPrefix = RodaConstants.SEARCH_FIELD_PREFIX + '.' + classSimpleName + '.' + field;
      String fieldType = ConfigurationManager.getString(fieldPrefix, RodaConstants.SEARCH_FIELD_TYPE);
      String fieldsNames = ConfigurationManager.getString(fieldPrefix, RodaConstants.SEARCH_FIELD_FIELDS);

      if ((RodaConstants.SEARCH_FIELD_TYPE_DATE.equals(fieldType)
        || RodaConstants.SEARCH_FIELD_TYPE_DATE_INTERVAL.equals(fieldType))) {
        String fieldLabelI18N = ConfigurationManager.getString(fieldPrefix, RodaConstants.SEARCH_FIELD_I18N);
        String translation = fieldLabelI18N;
        try {
          translation = ConfigurationManager.getTranslation(fieldLabelI18N);
        } catch (MissingResourceException e) {
          // do nothing
        }

        Pair<String, String> pair = new Pair<>(parseFieldsNames(fieldsNames), translation);
        elements.add(pair);
      }

    }
    return elements;
  }

  private static String parseFieldsNames(String fieldsNames) {
    if (fieldsNames.contains(",")) {
      String[] split = fieldsNames.split(",");
      if (split.length == 2) {
        return split[1];
      }
    }

    return fieldsNames;
  }
}
