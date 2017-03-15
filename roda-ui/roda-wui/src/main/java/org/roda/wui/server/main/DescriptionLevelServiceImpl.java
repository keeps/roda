/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.server.main;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.Messages;
import org.roda.core.common.RodaUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.wui.client.main.DescriptionLevelConfiguration;
import org.roda.wui.client.main.DescriptionLevelService;
import org.roda.wui.common.server.ServerTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class DescriptionLevelServiceImpl extends RemoteServiceServlet implements DescriptionLevelService {

  private static final long serialVersionUID = 1133363430147430537L;

  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(DescriptionLevelServiceImpl.class);

  @Override
  public DescriptionLevelConfiguration getDescriptionLevelConfiguration(String localeString) {
    DescriptionLevelConfiguration dlc = new DescriptionLevelConfiguration();
    dlc.setClassificationPlanLevels(
      RodaUtils.copyList(RodaCoreFactory.getRodaConfiguration().getList(RodaConstants.LEVELS_CLASSIFICATION_PLAN)));
    dlc.setDefaultClass(RodaCoreFactory.getRodaConfiguration().getString(RodaConstants.LEVELS_ICONS_DEFAULT));
    dlc.setGhostClass(RodaCoreFactory.getRodaConfiguration().getString(RodaConstants.LEVELS_ICONS_GHOST));
    Map<String, String> icons = new HashMap<>();
    Iterator<String> iconKeys = RodaCoreFactory.getRodaConfiguration().getKeys(RodaConstants.LEVELS_ICONS_PREFIX);
    if (iconKeys != null) {
      while (iconKeys.hasNext()) {
        String iconKey = iconKeys.next();
        String level = iconKey.replace(RodaConstants.LEVELS_ICONS_PREFIX + ".", "");
        icons.put(level, RodaCoreFactory.getRodaConfiguration().getString(iconKey));
      }
    }
    dlc.setLevelIcons(icons);
    dlc.setRepresentationClass(
      RodaCoreFactory.getRodaConfiguration().getString(RodaConstants.LEVELS_ICONS_REPRESENTATION));
    dlc.setRepresentationFileClass(
      RodaCoreFactory.getRodaConfiguration().getString(RodaConstants.LEVELS_ICONS_REPRESENTATION_FILE));
    dlc.setRepresentationFolderClass(
      RodaCoreFactory.getRodaConfiguration().getString(RodaConstants.LEVELS_ICONS_REPRESENTATION_FOLDER));

    Map<String, String> representationTypesIcons = new HashMap<>();
    Iterator<String> representationTypesIconKeys = RodaCoreFactory.getRodaConfiguration()
      .getKeys(RodaConstants.LEVELS_ICONS_REPRESENTATION_TYPES_PREFIX);
    if (representationTypesIconKeys != null) {
      while (representationTypesIconKeys.hasNext()) {
        String iconKey = representationTypesIconKeys.next();
        String level = iconKey.replace(RodaConstants.LEVELS_ICONS_REPRESENTATION_TYPES_PREFIX + ".", "");
        representationTypesIcons.put(level, RodaCoreFactory.getRodaConfiguration().getString(iconKey));
      }
    }
    dlc.setRepresentationTypesIcons(representationTypesIcons);

    Messages messages = RodaCoreFactory.getI18NMessages(ServerTools.parseLocale(localeString));
    Map<String, String> translations = messages.getTranslations(RodaConstants.LEVEL_I18N_PREFIX, String.class, false);
    Map<String, String> levelsLabels = new HashMap<>();
    for (Map.Entry<String, String> entry : translations.entrySet()) {
      levelsLabels.put(entry.getKey().replace(RodaConstants.LEVEL_I18N_PREFIX + ".", ""), entry.getValue());
    }
    dlc.setTranslations(levelsLabels);
    return dlc;
  }

}
