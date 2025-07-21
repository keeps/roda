/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.client.tools;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.descriptionlevels.DescriptionLevel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTMLPanel;

import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.ip.IndexedFile;

public class DescriptionLevelUtils {

  @SuppressWarnings("unused")
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final String TOP_ICON = "<span class='roda-logo'></span>";

  private DescriptionLevelUtils() {
  }

  public static DescriptionLevel getDescriptionLevel(String levelString) {
    DescriptionLevel level = new DescriptionLevel();

    // try to set a specific icon
    if (levelString != null) {
      if (ConfigurationManager.getString(RodaConstants.LEVELS_ICONS_PREFIX, levelString) != null) {
        level.setIconClass(ConfigurationManager.getString(RodaConstants.LEVELS_ICONS_PREFIX, levelString));
      } else if (levelString.equalsIgnoreCase(RodaConstants.AIP_GHOST)) {
        level.setIconClass(ConfigurationManager.getString(RodaConstants.LEVELS_ICONS_AIP_GHOST));
      } else if (levelString.equalsIgnoreCase(RodaConstants.AIP_CHILDREN)) {
        level.setIconClass(ConfigurationManager.getString(RodaConstants.LEVELS_ICONS_AIP_CHILDREN));
      } else if (levelString.equalsIgnoreCase(RodaConstants.VIEW_REPRESENTATION_REPRESENTATION)) {
        level.setIconClass(ConfigurationManager.getString(RodaConstants.LEVELS_ICONS_REPRESENTATION));
      } else if (levelString.equalsIgnoreCase(RodaConstants.VIEW_REPRESENTATION_FOLDER)) {
        level.setIconClass(ConfigurationManager.getString(RodaConstants.LEVELS_ICONS_REPRESENTATION_FOLDER));
      } else if (levelString.equalsIgnoreCase(RodaConstants.VIEW_REPRESENTATION_FILE)) {
        level.setIconClass(ConfigurationManager.getString(RodaConstants.LEVELS_ICONS_REPRESENTATION_FILE));
      } else if (levelString.equalsIgnoreCase(RodaConstants.VIEW_REPRESENTATION_FILE_REFERENCE)) {
        level.setIconClass(ConfigurationManager.getString(RodaConstants.LEVELS_ICONS_REPRESENTATION_FILE_REFERENCE));
      }
    }

    // fallback to default icon
    if (level.getIconClass() == null) {
      level.setIconClass(ConfigurationManager.getString(RodaConstants.LEVELS_ICONS_AIP_DEFAULT));
    }

    // set the label, falling back to the levelString
    String label = ConfigurationManager.getTranslation(RodaConstants.LEVEL_I18N_PREFIX, levelString);
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
    DescriptionLevel level = getDescriptionLevel(levelString);

    StringBuilder b = new StringBuilder();
    b.append("<i class='");
    b.append(level.getIconClass());
    b.append("'></i>");
    appendLevel(b, showText, level.getLabel());

    return SafeHtmlUtils.fromSafeConstant(b.toString());
  }

  public static String getElementLevelIconCssClass(String levelString) {
    return getDescriptionLevel(levelString).getIconClass();
  }

  public static String getElementLevelLabel(String levelString) {
    return getDescriptionLevel(levelString).getLabel();
  }

  private static void appendLevel(StringBuilder b, boolean showText, String level) {
    if (showText && level != null && level.length() > 0) {
      b.append("&nbsp;");
      b.append(level);
    }
  }

  public static String getRepresentationTypeIconCssClass(String representationType) {
    String icon = null;

    if (representationType != null) {
      String representationTypeKey = representationType.toLowerCase();

      // try to set a specific icon
      icon = ConfigurationManager.getString(RodaConstants.LEVELS_ICONS_REPRESENTATION_TYPES_PREFIX,
        representationTypeKey);
    }

    // fallback to default representation icon
    if (icon == null) {
      icon = ConfigurationManager.getString(RodaConstants.LEVELS_ICONS_REPRESENTATION_TYPES_PREFIX,
        RodaConstants.REPRESENTATION_TYPE_DEFAULT);
    }

    return icon;
  }

  public static SafeHtml getRepresentationTypeIcon(String representationType, boolean showText) {
    String icon = getRepresentationTypeIconCssClass(representationType);

    StringBuilder b = new StringBuilder();
    b.append("<i class='");
    if (icon != null) {
      b.append(icon);
    }

    b.append("'></i>");
    appendLevel(b, showText, representationType);
    return SafeHtmlUtils.fromSafeConstant(b.toString());
  }

  public static String getFileLevel(IndexedFile file) {
    if (file.isDirectory()) {
      return RodaConstants.VIEW_REPRESENTATION_FOLDER;
    } else if (file.isReference()) {
      return RodaConstants.VIEW_REPRESENTATION_FILE_REFERENCE;
    } else {
      return RodaConstants.VIEW_REPRESENTATION_FILE;
    }
  }
}
