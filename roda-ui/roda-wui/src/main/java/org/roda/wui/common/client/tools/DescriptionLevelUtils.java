package org.roda.wui.common.client.tools;

import java.util.List;

import org.roda.core.data.eadc.DescriptionLevel;
import org.roda.core.data.eadc.DescriptionLevelInfo;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.main.client.DescriptionLevelInfoPack;
import org.roda.wui.main.client.DescriptionLevelServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;

import config.i18n.client.CommonConstants;

public class DescriptionLevelUtils {

  private static ClientLogger logger = new ClientLogger(DescriptionLevelUtils.class.getName());
  private static CommonConstants constants = (CommonConstants) GWT.create(CommonConstants.class);

  private DescriptionLevelUtils() {
    super();
  }

  public static List<DescriptionLevelInfo> DESCRIPTION_LEVELS_INFO;
  public static List<DescriptionLevel> DESCRIPTION_LEVELS;
  public static List<DescriptionLevel> ROOT_DESCRIPTION_LEVELS;
  public static List<DescriptionLevel> LEAF_DESCRIPTION_LEVELS;
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
        LEAF_DESCRIPTION_LEVELS = result.getLeafDescriptionLevels();
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

  /**
   * @deprecated use
   *             {@link DescriptionLevelUtils#getElementLevelIconSafeHtml(String)}
   *             instead
   * @param level
   * @return
   */
  public static String getElementLevelIconPath(String level) {
    String ret;
    final DescriptionLevelInfo levelInfo = getDescriptionLevel(level);
    if (levelInfo != null) {
      ret = GWT.getModuleBaseURL() + "description_levels/" + levelInfo.getCategory().getCategory() + ".png";

    } else {
      ret = GWT.getModuleBaseURL() + "description_levels/default.png";
    }
    return ret;
  }

  /**
   * Get description level icon
   * 
   * @param level
   * @return the icon message
   * 
   * @deprecated use
   *             {@link DescriptionLevelUtils#getElementLevelIconHTMLPanel(String)}
   *             instead
   */
  public static Image getElementLevelIconImage(String level) {
    Image ret;
    final DescriptionLevelInfo levelInfo = DescriptionLevelUtils.getDescriptionLevel(level);
    if (levelInfo != null) {
      ret = new Image(GWT.getModuleBaseURL() + "description_levels/" + levelInfo.getCategory().getCategory() + ".png");
      ret.setAltText(levelInfo.getLabel(constants.locale()));
    } else {
      ret = new Image(GWT.getModuleBaseURL() + "description_levels/default.png");
      ret.setAltText("default");
    }

    return ret;
  }

  /**
   * Get description level icon
   * 
   * @param level
   * @return the icon message
   */
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
