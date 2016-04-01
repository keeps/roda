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
import org.roda.core.data.descriptionLevels.DescriptionLevelInfo;
import org.roda.wui.client.main.DescriptionLevelInfoPack;
import org.roda.wui.client.main.DescriptionLevelServiceAsync;
import org.roda.wui.common.client.ClientLogger;

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

  public static List<DescriptionLevelInfo> DESCRIPTION_LEVELS_INFO;
  public static List<DescriptionLevel> DESCRIPTION_LEVELS;
  public static List<DescriptionLevel> ROOT_DESCRIPTION_LEVELS;
  public static List<DescriptionLevel> REPRESENTATION_DESCRIPTION_LEVELS;
  public static List<DescriptionLevel> ALL_BUT_REPRESENTATIONS_DESCRIPTION_LEVELS;

  public static void load(final AsyncCallback<Void> callback) {
    DescriptionLevelServiceAsync.INSTANCE.getAllDescriptionLevels(new AsyncCallback<DescriptionLevelInfoPack>() {

      @Override
      public void onFailure(Throwable caught) {
        logger.error("Error getting all the description levels!", caught);
        callback.onFailure(caught);
      }

      @Override
      public void onSuccess(DescriptionLevelInfoPack result) {
        DESCRIPTION_LEVELS_INFO = result.getDescriptionLevelsInfo();
        DESCRIPTION_LEVELS = result.getDescriptionLevels();
        ROOT_DESCRIPTION_LEVELS = result.getRootDescriptionLevels();
        REPRESENTATION_DESCRIPTION_LEVELS = result.getRepresentationDescriptionLevels();
        ALL_BUT_REPRESENTATIONS_DESCRIPTION_LEVELS = result.getAllButRepresentationDescriptionLevels();
        callback.onSuccess(null);
      }
    });
  }

  public static DescriptionLevelInfo getDescriptionLevel(String level) {
    DescriptionLevelInfo ret = null;
    if (DESCRIPTION_LEVELS_INFO == null) {
      logger.error("Requiring a description level while their are not yet loaded");
      return null;
    }

    for (DescriptionLevelInfo descriptionLevel : DESCRIPTION_LEVELS_INFO) {
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
    return new HTMLPanel(getElementLevelIconSafeHtml(level));
  }

  public static SafeHtml getElementLevelIconSafeHtml(String level) {
    return SafeHtmlUtils.fromSafeConstant("<i class='" + getElementLevelIconClasses(level) + "'></i>");
  }

  public static String getElementLevelIconClasses(String level) {
    String ret;
    final DescriptionLevelInfo levelInfo = DescriptionLevelUtils.getDescriptionLevel(level);
    if (levelInfo != null) {
      ret = "description-level description-level-" + levelInfo.getCategory().getCategory();
    } else {
      ret = "description-level";
    }
    return ret;
  }

}
