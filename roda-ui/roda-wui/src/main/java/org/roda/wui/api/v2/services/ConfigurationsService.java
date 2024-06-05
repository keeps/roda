package org.roda.wui.api.v2.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.Messages;
import org.roda.core.common.RodaUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.properties.ObjectClassFields;
import org.roda.wui.client.browse.Viewers;
import org.roda.wui.common.server.ServerTools;
import org.springframework.stereotype.Service;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Service
public class ConfigurationsService {

  private static final String UI_VIEWERS_PREFIX = "ui.viewers.";

  public Viewers getViewerConfigurations() {
    Viewers viewers = new Viewers();
    Configuration rodaConfig = RodaCoreFactory.getRodaConfiguration();
    List<String> viewersSupported = RodaUtils.copyList(rodaConfig.getList("ui.viewers"));

    for (String type : viewersSupported) {
      List<String> fieldPronoms = RodaUtils.copyList(rodaConfig.getList(UI_VIEWERS_PREFIX + type + ".pronoms"));
      List<String> fieldMimetypes = RodaUtils.copyList(rodaConfig.getList(UI_VIEWERS_PREFIX + type + ".mimetypes"));
      List<String> fieldExtensions = RodaUtils.copyList(rodaConfig.getList(UI_VIEWERS_PREFIX + type + ".extensions"));

      for (String pronom : fieldPronoms) {
        viewers.addPronom(pronom, type);
      }

      for (String mimetype : fieldMimetypes) {
        viewers.addMimetype(mimetype, type);
      }

      for (String extension : fieldExtensions) {
        viewers.addExtension(extension, type);
      }

      viewers.setTextLimit(rodaConfig.getString(UI_VIEWERS_PREFIX + "text.limit", ""));
      viewers.setOptions(rodaConfig.getString(UI_VIEWERS_PREFIX + type + ".options", ""));
    }

    return viewers;
  }

  public ObjectClassFields retrieveObjectClassFields(String localeString) {
    Locale locale = ServerTools.parseLocale(localeString);
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    ObjectClassFields objectClassFields = new ObjectClassFields();
    Iterator<String> keys = RodaCoreFactory.getRodaConfiguration().getKeys("ui.ri.rule");
    Map<String, List<String>> fieldsResult = new HashMap<>();
    Map<String, String> translationsResult = new HashMap<>();

    while (keys.hasNext()) {
      String key = keys.next();
      String[] splitKey = key.split("\\.");
      List<String> fields = RodaCoreFactory.getRodaConfigurationAsList(key);
      List<String> fieldsAndTranslations = new ArrayList<>();

      for (String field : fields) {
        String fieldName = RodaCoreFactory.getRodaConfigurationAsString(field, RodaConstants.SEARCH_FIELD_FIELDS);
        fieldsAndTranslations.add(fieldName);

        String fieldI18nKey = RodaCoreFactory.getRodaConfigurationAsString(field, RodaConstants.SEARCH_FIELD_I18N);
        if (StringUtils.isNotBlank(fieldI18nKey)) {
          String translation = messages.getTranslation(fieldI18nKey);
          translationsResult.put(splitKey[3] + ":" + fieldName, translation);
        }
      }

      fieldsResult.put(splitKey[3], fieldsAndTranslations);
    }

    objectClassFields.setObjectClassFields(fieldsResult);
    objectClassFields.setTranslations(translationsResult);
    return objectClassFields;
  }
}
