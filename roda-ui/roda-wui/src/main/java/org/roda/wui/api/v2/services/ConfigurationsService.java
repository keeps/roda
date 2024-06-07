package org.roda.wui.api.v2.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.Messages;
import org.roda.core.common.RodaUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.properties.ConversionProfile;
import org.roda.core.data.v2.properties.ConversionProfileOutcomeType;
import org.roda.core.data.v2.properties.ConversionProfiles;
import org.roda.core.data.v2.properties.DropdownPluginParameterItem;
import org.roda.core.data.v2.properties.DropdownPluginParameterItems;
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

  public DropdownPluginParameterItems retrieveDropDownPluginParameterItems(String parameterId, String localeString) {
    DropdownPluginParameterItems items = new DropdownPluginParameterItems();

    List<String> dropdownItems = RodaUtils
      .copyList(RodaCoreFactory.getRodaConfiguration().getList("core.plugins.dropdown." + parameterId + "[]"));
    Locale locale = ServerTools.parseLocale(localeString);
    Messages messages = RodaCoreFactory.getI18NMessages(locale);

    for (String item : dropdownItems) {
      String i18nProperty = RodaCoreFactory.getRodaConfiguration()
        .getString("core.plugins.dropdown." + parameterId + "[]." + item + ".i18n");
      items.addObject(new DropdownPluginParameterItem(item, messages.getTranslation(i18nProperty, item)));
    }

    return items;
  }

  public ConversionProfiles retrieveConversionProfilePluginItems(String pluginId,
    ConversionProfileOutcomeType outcomeType, String localeString) {
    ConversionProfiles items = new ConversionProfiles();

    String pluginName = RodaCoreFactory.getRodaConfiguration().getString("core.plugins.conversion.profile." + pluginId);

    List<String> dropdownItems = RodaUtils.copyList(
      RodaCoreFactory.getRodaConfiguration().getList("core.plugins.conversion.profile." + pluginName + ".profiles[]"));
    Locale locale = ServerTools.parseLocale(localeString);

    ResourceBundle pluginMessages = RodaCoreFactory.getPluginMessages(pluginId, locale);

    for (String item : dropdownItems) {
      ConversionProfile conversionProfile = retrieveConversionProfileItem(item, pluginName, pluginMessages);
      if (outcomeType.equals(ConversionProfileOutcomeType.REPRESENTATION)
        && conversionProfile.canBeUsedForRepresentation()) {
        items.addObject(conversionProfile);
      }

      if (outcomeType.equals(ConversionProfileOutcomeType.DISSEMINATION)
        && conversionProfile.canBeUsedForDissemination()) {
        items.addObject(conversionProfile);
      }
    }

    return items;
  }

  private ConversionProfile retrieveConversionProfileItem(String item, String pluginName,
    ResourceBundle resourceBundle) {
    ConversionProfile conversionProfile = new ConversionProfile();
    Map<String, String> optionsValues = new HashMap<>();

    String i18nKey = RodaCoreFactory.getRodaConfiguration()
      .getString("core.plugins.conversion.profile." + pluginName + ".profiles.i18nPrefix");

    String title;
    String description;

    try {
      title = resourceBundle.getString(i18nKey + "." + item + ".title");
    } catch (MissingResourceException e) {
      title = i18nKey + "." + item + ".title";
    }

    try {
      description = resourceBundle.getString(i18nKey + "." + item + ".description");
    } catch (MissingResourceException e) {
      description = i18nKey + "." + item + ".description";
    }

    conversionProfile.setTitle(title);
    conversionProfile.setDescription(description);
    conversionProfile.setProfile(item);

    conversionProfile.setCanBeUsedForDissemination(RodaCoreFactory.getRodaConfiguration()
      .getBoolean("core.plugins.conversion.profile." + pluginName + "." + item + ".canBeUsedForDissemination", false));
    conversionProfile.setCanBeUsedForRepresentation(RodaCoreFactory.getRodaConfiguration()
      .getBoolean("core.plugins.conversion.profile." + pluginName + "." + item + ".canBeUsedForRepresentation", false));

    String[] options = RodaCoreFactory.getRodaConfiguration()
      .getStringArray("core.plugins.conversion.profile." + pluginName + "." + item + ".options[]");
    for (String option : options) {
      String optionValue = RodaCoreFactory.getRodaConfiguration()
        .getString("core.plugins.conversion.profile." + pluginName + "." + item + "." + option);
      optionsValues.put(option, optionValue);
    }
    conversionProfile.setOptions(optionsValues);

    return conversionProfile;
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
