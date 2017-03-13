/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.client.tools;

import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.descriptionLevels.DescriptionLevel;
import org.roda.wui.client.main.DescriptionLevelConfiguration;
import org.roda.wui.client.main.DescriptionLevelServiceAsync;
import org.roda.wui.common.client.ClientLogger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;

import config.i18n.client.ClientMessages;

public class DescriptionLevelUtils {

  @SuppressWarnings("unused")
  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  private static final String TOP_ICON = "<span class='roda-logo'></span>";

  private static ClientLogger logger = new ClientLogger(DescriptionLevelUtils.class.getName());

  private DescriptionLevelUtils() {
    super();
  }

  public static DescriptionLevelConfiguration LEVELS_CONFIGURATION;

  public static void load(final AsyncCallback<Void> callback) {
    DescriptionLevelServiceAsync.INSTANCE.getDescriptionLevelConfiguration(
      LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<DescriptionLevelConfiguration>() {

        @Override
        public void onFailure(Throwable caught) {
          logger.error("Error getting the description level configuration!", caught);
          callback.onFailure(caught);
        }

        @Override
        public void onSuccess(DescriptionLevelConfiguration result) {
          LEVELS_CONFIGURATION = result;
          callback.onSuccess(null);
        }
      });
  }

  public static DescriptionLevel getDescriptionLevel(String levelString) {
    return getDescriptionLevel(levelString, true);
  }

  public static DescriptionLevel getDescriptionLevel(String levelString, boolean showLabel) {
    if (LEVELS_CONFIGURATION == null) {
      logger.error("Requiring a description level while their are not yet loaded");
      return null;
    }

    DescriptionLevel level = new DescriptionLevel();
    if (levelString == null) {
      level.setIconClass(LEVELS_CONFIGURATION.getDefaultClass());
    } else {
      if (LEVELS_CONFIGURATION.getLevelIcons().containsKey(levelString)) {
        level.setIconClass(LEVELS_CONFIGURATION.getLevelIcons().get(levelString));
      } else if (levelString.equalsIgnoreCase(RodaConstants.AIP_GHOST)) {
        level.setIconClass(LEVELS_CONFIGURATION.getGhostClass());
      } else if ((levelString.equalsIgnoreCase(RodaConstants.VIEW_REPRESENTATION_REPRESENTATION))) {
        level.setIconClass(LEVELS_CONFIGURATION.getRepresentationClass());
      } else if ((levelString.equalsIgnoreCase(RodaConstants.VIEW_REPRESENTATION_FOLDER))) {
        level.setIconClass(LEVELS_CONFIGURATION.getRepresentationFolderClass());
      } else if ((levelString.equalsIgnoreCase(RodaConstants.VIEW_REPRESENTATION_FILE))) {
        level.setIconClass(LEVELS_CONFIGURATION.getRepresentationFileClass());
      } else {
        level.setIconClass(LEVELS_CONFIGURATION.getDefaultClass());
      }
    }

    Map<String, String> translations = LEVELS_CONFIGURATION.getTranslations();
    String label = translations.get(levelString);
    if (label == null) {
      label = levelString;
    }

    level.setLabel(label);
    return level;
  }

  public static SafeHtml getTopIconSafeHtml() {
    return SafeHtmlUtils.fromSafeConstant(TOP_ICON);
  }

  public static HTMLPanel getTopIconHTMLPanel() {
    return new HTMLPanel(SafeHtmlUtils.fromSafeConstant(TOP_ICON));
  }

  public static HTMLPanel getElementLevelIconHTMLPanel(String level) {
    return new HTMLPanel(getElementLevelIconSafeHtml(level, false));
  }

  public static SafeHtml getElementLevelIconSafeHtml(String levelString, boolean showText) {
    SafeHtml ret = null;

    DescriptionLevel level = getDescriptionLevel(levelString);
    if (level != null) {
      StringBuilder b = new StringBuilder();
      b.append("<i class='");
      b.append(level.getIconClass());
      b.append("'></i>");
      appendLevel(b, showText, level.getLabel());
      ret = SafeHtmlUtils.fromSafeConstant(b.toString());
    }

    return ret;
  }

  private static void appendLevel(StringBuilder b, boolean showText, String level) {
    if (showText && level != null && level.length() > 0) {
      b.append("&nbsp;");
      b.append(level);
    }
  }

  public static SafeHtml getRepresentationTypeIcon(String representationType, boolean showText) {
    if (LEVELS_CONFIGURATION == null) {
      logger.error("Requiring a description level while their are not yet loaded");
      return null;
    }

    String representationTypeKey = representationType.toLowerCase();
    String icon;
    if (LEVELS_CONFIGURATION.getRepresentationTypesIcons().containsKey(representationTypeKey)) {
      icon = LEVELS_CONFIGURATION.getRepresentationTypesIcons().get(representationTypeKey);
    } else if (LEVELS_CONFIGURATION.getRepresentationTypesIcons()
      .containsKey(RodaConstants.REPRESENTATION_TYPE_DEFAULT)) {
      icon = LEVELS_CONFIGURATION.getRepresentationTypesIcons().get(RodaConstants.REPRESENTATION_TYPE_DEFAULT);
    } else {
      icon = LEVELS_CONFIGURATION.getRepresentationClass();
    }

    StringBuilder b = new StringBuilder();
    b.append("<i class='");
    if (icon != null) {
      b.append(icon);
    }

    b.append("'></i>");
    appendLevel(b, showText, representationType);
    return SafeHtmlUtils.fromSafeConstant(b.toString());
  }
}
