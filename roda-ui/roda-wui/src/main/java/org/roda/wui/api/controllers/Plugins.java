package org.roda.wui.api.controllers;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.Messages;
import org.roda.core.common.RodaUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.common.ConversionProfile;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.plugins.PluginHelper;
import org.roda.wui.common.server.ServerTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class Plugins {

  private static final Logger LOGGER = LoggerFactory.getLogger(JobsHelper.class);

  Plugins(){
    //do nothing
  }

  public static List<PluginInfo> retrievePluginsInfo(List<PluginType> types) {
    // TODO check permissions
    return RodaCoreFactory.getPluginManager().getPluginsInfo(types);
  }

  public static Set<Pair<String, String>> retrieveReindexPluginObjectClasses(){
    // TODO check permissions
    Set<Pair<String, String>> classNames = new HashSet<>();
    List<Class<? extends IsRODAObject>> classes = PluginHelper.getReindexObjectClasses();
    classes.remove(Void.class);

    for (Class<? extends IsRODAObject> c : classes) {
      Pair<String, String> names = Pair.of(c.getSimpleName(), c.getName());
      classNames.add(names);
    }

    return classNames;
  }

  public static Set<Pair<String, String>> retrieveDropdownPluginItems(String parameterId, String localeString) {
    Set<Pair<String, String>> items = new HashSet<>();
    List<String> dropdownItems = RodaUtils
      .copyList(RodaCoreFactory.getRodaConfiguration().getList("core.plugins.dropdown." + parameterId + "[]"));
    Locale locale = ServerTools.parseLocale(localeString);
    Messages messages = RodaCoreFactory.getI18NMessages(locale);

    for (String item : dropdownItems) {
      String i18nProperty = RodaCoreFactory.getRodaConfiguration()
        .getString("core.plugins.dropdown." + parameterId + "[]." + item + ".i18n");
      items.add(Pair.of(messages.getTranslation(i18nProperty, item), item));
    }

    return items;
  }

  public static Set<ConversionProfile> retrieveConversionProfilePluginItems(String pluginId, String repOrDip,
                                                                     String localeString) {
    Set<ConversionProfile> items = new HashSet<>();

    String pluginName = RodaCoreFactory.getRodaConfiguration().getString("core.plugins.conversion.profile." + pluginId);

    List<String> dropdownItems = RodaUtils.copyList(
      RodaCoreFactory.getRodaConfiguration().getList("core.plugins.conversion.profile." + pluginName + ".profiles[]"));
    Locale locale = ServerTools.parseLocale(localeString);

    ResourceBundle pluginMessages = RodaCoreFactory.getPluginMessages(pluginId, locale);

    for (String item : dropdownItems) {
      ConversionProfile conversionProfile = retrieveConversionProfileItem(item, pluginName, pluginMessages);
      if (repOrDip.equals(RodaConstants.PLUGIN_PARAMS_CONVERSION_REPRESENTATION)
        && conversionProfile.canBeUsedForRepresentation()) {
        items.add(conversionProfile);
      }

      if (repOrDip.equals(RodaConstants.PLUGIN_PARAMS_CONVERSION_DISSEMINATION)
        && conversionProfile.canBeUsedForDissemination()) {
        items.add(conversionProfile);
      }
    }

    return items;
  }

  private static ConversionProfile retrieveConversionProfileItem(String item, String pluginName,
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
}
