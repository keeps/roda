/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.client.tools;

import java.util.List;

import org.roda.core.data.descriptionLevels.DescriptionLevel;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.client.main.DescriptionLevelInfoPack;
import org.roda.wui.client.main.DescriptionLevelServiceAsync;
import org.roda.wui.common.client.ClientLogger;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;

public class DescriptionLevelUtils {

  private static final String TOP_ICON = "<span class='roda-logo'></span>";

  private static ClientLogger logger = new ClientLogger(DescriptionLevelUtils.class.getName());

  private DescriptionLevelUtils() {
    super();
  }

  public static List<DescriptionLevel> DESCRIPTION_LEVELS;

  public static String GHOST_CLASS;

  public static String DEFAULT_CLASS;

  public static void load(final AsyncCallback<Void> callback) {
    DescriptionLevelServiceAsync.INSTANCE.getAllDescriptionLevels(new AsyncCallback<DescriptionLevelInfoPack>() {

      @Override
      public void onFailure(Throwable caught) {
        logger.error("Error getting all the description levels!", caught);
        callback.onFailure(caught);
      }

      @Override
      public void onSuccess(DescriptionLevelInfoPack result) {
        DESCRIPTION_LEVELS = result.getDescriptionLevels();
        GHOST_CLASS = result.getGhostClass();
        DEFAULT_CLASS = result.getDefaultClass();
        callback.onSuccess(null);
      }
    });
  }

  public static DescriptionLevel getDescriptionLevel(String level) {
    DescriptionLevel ret = null;
    if (DESCRIPTION_LEVELS == null) {
      logger.error("Requiring a description level while their are not yet loaded");
      return null;
    }

    for (DescriptionLevel descriptionLevel : DESCRIPTION_LEVELS) {
      if (descriptionLevel.getLevel().equals(level)) {
        ret = descriptionLevel;
        break;
      }
    }
    return ret;
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

  public static SafeHtml getElementLevelIconSafeHtml(String level, boolean showText) {
    SafeHtml ret = null;
    if (level.equals("ghost")) {
      StringBuilder b = new StringBuilder();
      b.append("<i class='").append(GHOST_CLASS).append("' aria-hidden=\"true\"></i>");
      ret = SafeHtmlUtils.fromSafeConstant(b.toString());
    } else {
      DescriptionLevel levelInfo = DescriptionLevelUtils.getDescriptionLevel(level);
      if (levelInfo == null) {
        StringBuilder b = new StringBuilder();
        b.append("<i class='").append(DEFAULT_CLASS).append("' aria-hidden=\"true\"></i>");
        appendLevel(b, showText, level);
        ret = SafeHtmlUtils.fromSafeConstant(b.toString());
      } else {
        StringBuilder b = new StringBuilder();
        b.append("<i class='").append(levelInfo.getIconClass() + "'");
        if (levelInfo != null) {
          String label = levelInfo.getLabel(LocaleInfo.getCurrentLocale().getLocaleName());
          if (StringUtils.isNotBlank(label)) {
            b.append(" alt='").append(levelInfo.getLabel(LocaleInfo.getCurrentLocale().getLocaleName())).append("'");
          }
        }
        b.append("'>");
        b.append("</i>");
        appendLevel(b, showText, level);
        ret = SafeHtmlUtils.fromSafeConstant(b.toString());
      }
    }
    return ret;
  }

  private static void appendLevel(StringBuilder b, boolean showText, String level) {
    if (showText && level != null && level.length() > 0) {
      b.append("&nbsp;");
      b.append(level);
    }
  }
}
