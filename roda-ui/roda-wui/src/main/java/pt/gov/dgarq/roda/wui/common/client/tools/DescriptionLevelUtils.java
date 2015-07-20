package pt.gov.dgarq.roda.wui.common.client.tools;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;

import config.i18n.client.CommonConstants;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevelInfo;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.main.client.DescriptionLevelInfoPack;
import pt.gov.dgarq.roda.wui.main.client.DescriptionLevelServiceAsync;

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
	 */
	public static Image getElementLevelIconImage(String level) {
		Image ret;
		final DescriptionLevelInfo levelInfo = DescriptionLevelUtils.getDescriptionLevel(level);
		if (levelInfo != null) {
			ret = new Image(
					GWT.getModuleBaseURL() + "description_levels/" + levelInfo.getCategory().getCategory() + ".png");
			ret.setAltText(levelInfo.getLabel(constants.locale()));
		} else {
			ret = new Image(GWT.getModuleBaseURL() + "description_levels/default.png");
			ret.setAltText("default");
		}

		return ret;
	}

}
