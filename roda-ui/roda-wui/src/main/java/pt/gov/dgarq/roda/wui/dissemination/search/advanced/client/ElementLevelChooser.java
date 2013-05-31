/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.search.advanced.client;

import java.util.ArrayList;
import java.util.List;

import pt.gov.dgarq.roda.core.data.SearchParameter;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.core.data.search.DefaultSearchParameter;
import pt.gov.dgarq.roda.core.data.search.EadcSearchFields;
import pt.gov.dgarq.roda.wui.dissemination.client.images.ElementIconBundle;

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

import config.i18n.client.DisseminationConstants;

/**
 * @author Luis Faria
 * 
 */
public class ElementLevelChooser extends DockPanel {

	private static DisseminationConstants constants = (DisseminationConstants) GWT
			.create(DisseminationConstants.class);

	private static ElementIconBundle icons = (ElementIconBundle) GWT
			.create(ElementIconBundle.class);

	private final VerticalPanel optionLayout;

	private final RadioButton allLevelsOption;

	private final RadioButton chooseLevelsOption;

	private final Grid centralLayout;

	private final CheckBox fondsCheck;

	private final CheckBox subfondsCheck;

	private final CheckBox classCheck;

	private final CheckBox subclassCheck;

	private final CheckBox seriesCheck;

	private final CheckBox subseriesCheck;

	private final CheckBox fileCheck;

	private final CheckBox itemCheck;

	private final Label fondsLabel;

	private final Label subfondsLabel;

	private final Label classLabel;

	private final Label subclassLabel;

	private final Label seriesLabel;

	private final Label subseriesLabel;

	private final Label fileLabel;

	private final Label itemLabel;

	private final Image fondsIcon;

	private final Image subfondsIcon;

	private final Image classIcon;

	private final Image subclassIcon;

	private final Image seriesIcon;

	private final Image subseriesIcon;

	private final Image fileIcon;

	private final Image itemIcon;

	/**
	 * Create new element level chooser
	 */
	public ElementLevelChooser() {

		optionLayout = new VerticalPanel();
		centralLayout = new Grid(2, 14);

		this.add(optionLayout, WEST);
		this.add(centralLayout, CENTER);

		allLevelsOption = new RadioButton("level-option", constants.allLevels());
		chooseLevelsOption = new RadioButton("level-option", constants
				.chooseLevels());

		optionLayout.add(allLevelsOption);
		optionLayout.add(chooseLevelsOption);

		fondsCheck = new CheckBox();
		subfondsCheck = new CheckBox();
		classCheck = new CheckBox();
		subclassCheck = new CheckBox();
		seriesCheck = new CheckBox();
		subseriesCheck = new CheckBox();
		fileCheck = new CheckBox();
		itemCheck = new CheckBox();

		fondsLabel = new Label(constants.fonds());
		subfondsLabel = new Label(constants.subfonds());
		classLabel = new Label(constants.class_());
		subclassLabel = new Label(constants.subclass());
		seriesLabel = new Label(constants.series());
		subseriesLabel = new Label(constants.subseries());
		fileLabel = new Label(constants.file());
		itemLabel = new Label(constants.item());

		fondsIcon = icons.fonds().createImage();
		subfondsIcon = icons.subfonds().createImage();
		classIcon = icons.class_().createImage();
		subclassIcon = icons.subclass().createImage();
		seriesIcon = icons.series().createImage();
		subseriesIcon = icons.subseries().createImage();
		fileIcon = icons.file().createImage();
		itemIcon = icons.item().createImage();

		centralLayout.setWidget(0, 0, fondsCheck);
		centralLayout.setWidget(0, 1, fondsIcon);
		centralLayout.setWidget(0, 2, fondsLabel);

		centralLayout.setWidget(1, 0, subfondsCheck);
		centralLayout.setWidget(1, 1, subfondsIcon);
		centralLayout.setWidget(1, 2, subfondsLabel);

		centralLayout.setWidget(0, 3, classCheck);
		centralLayout.setWidget(0, 4, classIcon);
		centralLayout.setWidget(0, 5, classLabel);

		centralLayout.setWidget(1, 3, subclassCheck);
		centralLayout.setWidget(1, 4, subclassIcon);
		centralLayout.setWidget(1, 5, subclassLabel);

		centralLayout.setWidget(0, 6, seriesCheck);
		centralLayout.setWidget(0, 7, seriesIcon);
		centralLayout.setWidget(0, 8, seriesLabel);

		centralLayout.setWidget(1, 6, subseriesCheck);
		centralLayout.setWidget(1, 7, subseriesIcon);
		centralLayout.setWidget(1, 8, subseriesLabel);

		centralLayout.setWidget(0, 9, fileCheck);
		centralLayout.setWidget(0, 10, fileIcon);
		centralLayout.setWidget(0, 11, fileLabel);

		centralLayout.setWidget(1, 9, itemCheck);
		centralLayout.setWidget(1, 10, itemIcon);
		centralLayout.setWidget(1, 11, itemLabel);

		addListeners(fondsCheck, fondsIcon, fondsLabel);
		addListeners(subfondsCheck, subfondsIcon, subfondsLabel);
		addListeners(classCheck, classIcon, classLabel);
		addListeners(subclassCheck, subclassIcon, subclassLabel);
		addListeners(seriesCheck, seriesIcon, seriesLabel);
		addListeners(subseriesCheck, subseriesIcon, subseriesLabel);
		addListeners(fileCheck, fileIcon, fileLabel);
		addListeners(itemCheck, itemIcon, itemLabel);

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
		fondsLabel.addStyleName("level-label");
		subfondsLabel.addStyleName("level-label");
		classLabel.addStyleName("level-label");
		subclassLabel.addStyleName("level-label");
		seriesLabel.addStyleName("level-label");
		subseriesLabel.addStyleName("level-label");
		fileLabel.addStyleName("level-label");
		itemLabel.addStyleName("level-label");

		optionLayout.addStyleName("level-option-layout");
		centralLayout.addStyleName("level-layout");

	}

	protected void onOptionClick() {
		if (allLevelsOption.isChecked()) {
			fondsCheck.setChecked(true);
			subfondsCheck.setChecked(true);
			classCheck.setChecked(true);
			subclassCheck.setChecked(true);
			seriesCheck.setChecked(true);
			subseriesCheck.setChecked(true);
			fileCheck.setChecked(true);
			itemCheck.setChecked(true);
		} else {
			fondsCheck.setChecked(false);
			subfondsCheck.setChecked(false);
			classCheck.setChecked(false);
			subclassCheck.setChecked(false);
			seriesCheck.setChecked(false);
			subseriesCheck.setChecked(false);
			fileCheck.setChecked(false);
			itemCheck.setChecked(false);
		}

	}

	protected void onElementClick() {
		if (fondsCheck.isChecked() && subfondsCheck.isChecked()
				&& classCheck.isChecked() && subclassCheck.isChecked()
				&& seriesCheck.isChecked() && subseriesCheck.isChecked()
				&& fileCheck.isChecked() && itemCheck.isChecked()) {
			allLevelsOption.setChecked(true);
		} else {
			chooseLevelsOption.setChecked(true);
		}
	}

	protected void addListeners(final CheckBox checkBox, final Image icon,
			final Label label) {
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
			selected.add(DescriptionLevel.FONDS);
			selected.add(DescriptionLevel.SUBFONDS);
			selected.add(DescriptionLevel.CLASS);
			selected.add(DescriptionLevel.SUBCLASS);
			selected.add(DescriptionLevel.SERIES);
			selected.add(DescriptionLevel.SUBSERIES);
			selected.add(DescriptionLevel.FILE);
			selected.add(DescriptionLevel.ITEM);
		} else {
			if (fondsCheck.isChecked()) {
				selected.add(DescriptionLevel.FONDS);
			}
			if (subfondsCheck.isChecked()) {
				selected.add(DescriptionLevel.SUBFONDS);
			}
			if (classCheck.isChecked()) {
				selected.add(DescriptionLevel.CLASS);
			}
			if (subclassCheck.isChecked()) {
				selected.add(DescriptionLevel.SUBCLASS);
			}
			if (seriesCheck.isChecked()) {
				selected.add(DescriptionLevel.SERIES);
			}
			if (subseriesCheck.isChecked()) {
				selected.add(DescriptionLevel.SUBSERIES);
			}
			if (fileCheck.isChecked()) {
				selected.add(DescriptionLevel.FILE);
			}
			if (itemCheck.isChecked()) {
				selected.add(DescriptionLevel.ITEM);
			}

		}

		return selected;
	}

	/**
	 * Get search parameters
	 * @return an array with the search parameters 
	 */
	public SearchParameter[] getSearchParameters() {
		SearchParameter[] parameters;

		if (allLevelsOption.isChecked()) {
			parameters = new SearchParameter[] {};
		} else {
			String keyword = "";
			if (fondsCheck.isChecked()) {
				keyword += DescriptionLevel.FONDS + " ";
			}
			if (subfondsCheck.isChecked()) {
				keyword += DescriptionLevel.SUBFONDS + " ";
			}
			if (classCheck.isChecked()) {
				keyword += DescriptionLevel.CLASS + " ";
			}
			if (subclassCheck.isChecked()) {
				keyword += DescriptionLevel.SUBCLASS + " ";
			}
			if (seriesCheck.isChecked()) {
				keyword += DescriptionLevel.SERIES + " ";
			}
			if (subseriesCheck.isChecked()) {
				keyword += DescriptionLevel.SUBSERIES + " ";
			}
			if (fileCheck.isChecked()) {
				keyword += DescriptionLevel.FILE + " ";
			}
			if (itemCheck.isChecked()) {
				keyword += DescriptionLevel.ITEM + " ";
			}

			SearchParameter parameter = new DefaultSearchParameter(
					new String[] { EadcSearchFields.LEVEL }, keyword,
					DefaultSearchParameter.MATCH_AT_LEAST_ONE_WORD);
			parameters = new SearchParameter[] { parameter };
		}
		return parameters;
	}
}
