/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.search.advanced.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.CommonConstants;
import config.i18n.client.DisseminationConstants;
import pt.gov.dgarq.roda.core.data.SearchParameter;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevelInfo;
import pt.gov.dgarq.roda.core.data.search.DefaultSearchParameter;
import pt.gov.dgarq.roda.core.data.search.EadcSearchFields;
import pt.gov.dgarq.roda.wui.common.client.tools.DescriptionLevelUtils;

/**
 * @author Luis Faria
 * 
 */
public class ElementLevelChooser extends DockPanel {

	private static DisseminationConstants constants = (DisseminationConstants) GWT.create(DisseminationConstants.class);

	private static CommonConstants commonConstants = (CommonConstants) GWT.create(CommonConstants.class);

	private final VerticalPanel optionLayout;

	private final RadioButton allLevelsOption;

	private final RadioButton chooseLevelsOption;

	private final Grid centralLayout;

	private final List<CheckBox> checkBoxes;

	private final List<Label> labels;

	private final List<Image> images;

	/**
	 * Create new element level chooser
	 */
	public ElementLevelChooser() {

		int levelsPerRow = 4;
		int rows = DescriptionLevelUtils.DESCRIPTION_LEVELS.size() / levelsPerRow;
		if ((DescriptionLevelUtils.DESCRIPTION_LEVELS.size() % levelsPerRow) != 0) {
			rows = rows + 1;
		}
		int columns = levelsPerRow * 3;

		optionLayout = new VerticalPanel();
		centralLayout = new Grid(rows, columns);

		this.add(optionLayout, WEST);
		this.add(centralLayout, CENTER);

		allLevelsOption = new RadioButton("level-option", constants.allLevels());
		chooseLevelsOption = new RadioButton("level-option", constants.chooseLevels());

		optionLayout.add(allLevelsOption);
		optionLayout.add(chooseLevelsOption);

		checkBoxes = new ArrayList<CheckBox>();
		labels = new ArrayList<Label>();
		images = new ArrayList<Image>();

		for (DescriptionLevel level : DescriptionLevelUtils.DESCRIPTION_LEVELS) {
			DescriptionLevelInfo levelInfo = DescriptionLevelUtils.getDescriptionLevel(level.getLevel());
			checkBoxes.add(new CheckBox());
			images.add(DescriptionLevelUtils.getElementLevelIconImage(level.getLevel()));
			labels.add(new Label(levelInfo.getLabel(commonConstants.locale())));
		}

		int row = 0, column = 0;
		for (int i = 0; i < DescriptionLevelUtils.DESCRIPTION_LEVELS.size(); i++) {
			centralLayout.setWidget(row, column, checkBoxes.get(i));
			centralLayout.setWidget(row, column + 1, images.get(i));
			centralLayout.setWidget(row, column + 2, labels.get(i));
			column = column + 3;
			if (((i + 1) % levelsPerRow) == 0) {
				row = row + 1;
				column = 0;
			}
		}

		for (int i = 0; i < DescriptionLevelUtils.DESCRIPTION_LEVELS.size(); i++) {
			addListeners(checkBoxes.get(i), images.get(i), labels.get(i));
		}

		ClickListener updateListener = new ClickListener() {

			public void onClick(Widget sender) {
				onOptionClick();
			}

		};

		allLevelsOption.addClickListener(updateListener);
		chooseLevelsOption.addClickListener(updateListener);

		allLevelsOption.setChecked(true);

		onOptionClick();

		this.addStyleName("wui-elementLevelChooser");
		allLevelsOption.addStyleName("level-all");
		chooseLevelsOption.addStyleName("level-choose");
		for (Label label : labels) {
			label.addStyleName("level-label");
		}

		optionLayout.addStyleName("level-option-layout");
		centralLayout.addStyleName("level-layout");

	}

	protected void onOptionClick() {
		for (CheckBox checkBox : checkBoxes) {
			checkBox.setChecked(allLevelsOption.isChecked());
		}
	}

	protected void onElementClick() {
		boolean areAllBoxesChecked = true;
		for (CheckBox checkBox : checkBoxes) {
			areAllBoxesChecked = areAllBoxesChecked && checkBox.isChecked();
		}
		if (areAllBoxesChecked) {
			allLevelsOption.setChecked(true);
		} else {
			chooseLevelsOption.setChecked(true);
		}
	}

	protected void addListeners(final CheckBox checkBox, final Image icon, final Label label) {
		ClickListener widgetsClickListener = new ClickListener() {
			public void onClick(Widget sender) {
				if (checkBox.isEnabled()) {
					checkBox.setChecked(!checkBox.isChecked());
					onElementClick();
				}

			}
		};

		icon.addClickListener(widgetsClickListener);
		label.addClickListener(widgetsClickListener);

		checkBox.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				onElementClick();

			}

		});

	}

	/**
	 * Get the list of the selected element levels
	 * 
	 * @return A List of the element levels as defined at
	 *         SimpleDescriptionObject
	 */
	public List<DescriptionLevel> getSelected() {
		List<DescriptionLevel> selected = new ArrayList<DescriptionLevel>();
		if (allLevelsOption.isChecked()) {
			for (DescriptionLevel level : DescriptionLevelUtils.DESCRIPTION_LEVELS) {
				selected.add(level);
			}
		} else {
			for (int i = 0; i < DescriptionLevelUtils.DESCRIPTION_LEVELS.size(); i++) {
				if (checkBoxes.get(i).isChecked()) {
					selected.add(DescriptionLevelUtils.DESCRIPTION_LEVELS.get(i));
				}
			}
		}

		return selected;
	}

	/**
	 * Get search parameters
	 * 
	 * @return an array with the search parameters
	 */
	public SearchParameter[] getSearchParameters() {
		SearchParameter[] parameters;

		if (allLevelsOption.isChecked()) {
			parameters = new SearchParameter[] {};
		} else {
			String keyword = "";
			for (int i = 0; i < DescriptionLevelUtils.DESCRIPTION_LEVELS.size(); i++) {
				if (checkBoxes.get(i).isChecked()) {
					keyword += DescriptionLevelUtils.DESCRIPTION_LEVELS.get(i) + " ";
				}
			}

			SearchParameter parameter = new DefaultSearchParameter(new String[] { EadcSearchFields.LEVEL }, keyword,
					DefaultSearchParameter.MATCH_AT_LEAST_ONE_WORD);
			parameters = new SearchParameter[] { parameter };
		}
		return parameters;
	}
}
