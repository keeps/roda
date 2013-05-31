/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.search.advanced.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.gov.dgarq.roda.core.data.search.EadcSearchFields;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.DisseminationConstants;

/**
 * @author Luis Faria
 * 
 */
public class DescriptiveFieldChooser extends FlexTable {

	private static DisseminationConstants constants = (DisseminationConstants) GWT
			.create(DisseminationConstants.class);

	/**
	 * Panel that allows toggle of searching in a specific field
	 */
	public class FieldOption extends HorizontalPanel {
		private final String fieldName;

		private final CheckBox checkBox;

		private final String description;

		private final Label descriptionLabel;

		/**
		 * Create a new field option panel
		 * 
		 * @param fieldName
		 *            the name of the field
		 * @param description
		 *            the descrition of the field
		 */
		public FieldOption(String fieldName, String description) {
			this.fieldName = fieldName;
			this.checkBox = new CheckBox();
			this.description = description;
			this.descriptionLabel = new Label(description);

			add(checkBox);
			add(descriptionLabel);

			descriptionLabel.addClickListener(new ClickListener() {

				public void onClick(Widget sender) {
					checkBox.setChecked(!checkBox.isChecked());
				}

			});

			this.setVerticalAlignment(ALIGN_MIDDLE);

			this.addStyleName("fieldOption");
			checkBox.addStyleName("fieldOption-checkbox");
			descriptionLabel.addStyleName("fieldOption-label");
		}

		/**
		 * Is field checked to be search on
		 * 
		 * @return true if checked
		 */
		public boolean isChecked() {
			return checkBox.isChecked();
		}

		/**
		 * Get the field name
		 * 
		 * @return the field name
		 */
		public String getFieldName() {
			return fieldName;
		}

		/**
		 * Get the field description
		 * 
		 * @return the field description
		 */
		public String getDescription() {
			return description;
		}

	}

	private final FieldOption completeReference;

	private final FieldOption title;

	private final FieldOption origination;
	
	private final FieldOption acqInfoNum;

	private final FieldOption materialspec;

	private final FieldOption physDesc;

	private final FieldOption physDescDimensions;

	private final FieldOption physDescPhysFacet;

	private final FieldOption physDescExtent;

	private final FieldOption preferCite;

	private final FieldOption bioghist;

	private final FieldOption custodhist;

	private final FieldOption acqinfo;

	private final FieldOption scopeContent;

	private final FieldOption arrangement;

	private final FieldOption appraisal;

	private final FieldOption accruals;

	private final FieldOption physTech;

	private final FieldOption accessRestrict;

	private final FieldOption useRestrict;

	private final FieldOption relatedMaterials;

	private final FieldOption otherFindAid;

	private final FieldOption bibliography;

	/**
	 * Create a new description fields chooser panel
	 */
	public DescriptiveFieldChooser() {

		completeReference = new FieldOption(
				EadcSearchFields.COMPLETE_REFERENCE, constants
						.completeReference());
		title = new FieldOption(EadcSearchFields.UNITTITLE, constants.title());
		origination = new FieldOption(EadcSearchFields.ORIGINATION, constants
				.origination());
		acqInfoNum = new FieldOption(EadcSearchFields.ACQINFO_NUM, constants
				.acqInfoNum());
		
		materialspec = new FieldOption(EadcSearchFields.MATERIALSPEC, constants
				.materialspec());
		physDesc = new FieldOption(EadcSearchFields.PHYSDESC, constants
				.physDesc());
		physDescDimensions = new FieldOption(
				EadcSearchFields.PHYSDESC_DIMENSIONS, constants
						.physDescDimensions());
		physDescPhysFacet = new FieldOption(
				EadcSearchFields.PHYSDESC_PHYSFACET, constants
						.physDescPhysFacet());
		physDescExtent = new FieldOption(EadcSearchFields.PHYSDESC_EXTENT,
				constants.physDescExtent());
		preferCite = new FieldOption(EadcSearchFields.PREFERCITE, constants
				.preferCite());

		bioghist = new FieldOption(EadcSearchFields.BIOGHIST, constants
				.bioghist());
		custodhist = new FieldOption(EadcSearchFields.CUSTODHIST, constants
				.custodhist());
		acqinfo = new FieldOption(EadcSearchFields.ACQINFO, constants.acqinfo());

		scopeContent = new FieldOption(EadcSearchFields.SCOPECONTENT, constants
				.scopeContent());
		arrangement = new FieldOption(EadcSearchFields.ARRANGEMENT, constants
				.arrangement());
		appraisal = new FieldOption(EadcSearchFields.APPRAISAL, constants
				.appraisal());
		accruals = new FieldOption(EadcSearchFields.ACCRUALS, constants
				.accruals());

		physTech = new FieldOption(EadcSearchFields.PHYSDESC, constants
				.physTech());
		accessRestrict = new FieldOption(EadcSearchFields.ACCESSRESTRICT,
				constants.accessRestrict());
		useRestrict = new FieldOption(EadcSearchFields.USERESTRICT, constants
				.useRestrict());

		relatedMaterials = new FieldOption(EadcSearchFields.RELATEDMATERIAL,
				constants.relatedMaterialsGroupLabel());

		otherFindAid = new FieldOption(EadcSearchFields.OTHERFINDAID, constants
				.otherFindAid());

		bibliography = new FieldOption(EadcSearchFields.BIBLIOGRAPHY, constants
				.bibliography());

		this.setWidget(0, 0, completeReference);
		this.setWidget(1, 0, title);
		this.setWidget(2, 0, origination);
		this.setWidget(3, 0, acqInfoNum);
		this.setWidget(4, 0, materialspec);
		this.setWidget(5, 0, physDesc);
		this.setWidget(6, 0, physDescDimensions);
		this.setWidget(7, 0, physDescPhysFacet);
		this.setWidget(8, 0, physDescExtent);
		
		this.setWidget(0, 1, preferCite);
		this.setWidget(1, 1, bioghist);
		this.setWidget(2, 1, custodhist);
		this.setWidget(3, 1, acqinfo);

		this.setWidget(5, 1, scopeContent);
		this.setWidget(6, 1, arrangement);
		this.setWidget(7, 1, appraisal);
		this.setWidget(8, 1, accruals);

		this.setWidget(0, 2, physTech);
		this.setWidget(1, 2, accessRestrict);
		this.setWidget(2, 2, useRestrict);

		this.setWidget(4, 2, otherFindAid);
		this.setWidget(5, 2, relatedMaterials);

		this.setWidget(7, 2, bibliography);

		this.addStyleName("wui-descriptiveFieldChooser");

	}

	/**
	 * Get selected field options
	 * 
	 * @return a list with selected field options
	 */
	public List<FieldOption> getSelected() {
		List<FieldOption> selectedFieldSet = new ArrayList<FieldOption>();

		if (completeReference.isChecked()) {
			selectedFieldSet.add(completeReference);
		}

		if (title.isChecked()) {
			selectedFieldSet.add(title);
		}

		if (origination.isChecked()) {
			selectedFieldSet.add(origination);
		}
		
		if (acqInfoNum.isChecked()) {
			selectedFieldSet.add(acqInfoNum);
		}

		if (materialspec.isChecked()) {
			selectedFieldSet.add(materialspec);
		}

		if (physDesc.isChecked()) {
			selectedFieldSet.add(physDesc);
		}

		if (physDescDimensions.isChecked()) {
			selectedFieldSet.add(physDescDimensions);
		}

		if (physDescPhysFacet.isChecked()) {
			selectedFieldSet.add(physDescPhysFacet);
		}

		if (physDescExtent.isChecked()) {
			selectedFieldSet.add(physDescExtent);
		}

		if (preferCite.isChecked()) {
			selectedFieldSet.add(preferCite);
		}

		if (bioghist.isChecked()) {
			selectedFieldSet.add(bioghist);
		}

		if (custodhist.isChecked()) {
			selectedFieldSet.add(custodhist);
		}

		if (acqinfo.isChecked()) {
			selectedFieldSet.add(acqinfo);
		}

		if (scopeContent.isChecked()) {
			selectedFieldSet.add(scopeContent);
		}

		if (arrangement.isChecked()) {
			selectedFieldSet.add(arrangement);
		}

		if (appraisal.isChecked()) {
			selectedFieldSet.add(appraisal);
		}

		if (accruals.isChecked()) {
			selectedFieldSet.add(accruals);
		}

		if (physTech.isChecked()) {
			selectedFieldSet.add(physTech);
		}

		if (accessRestrict.isChecked()) {
			selectedFieldSet.add(accessRestrict);
		}

		if (useRestrict.isChecked()) {
			selectedFieldSet.add(useRestrict);
		}

		if (otherFindAid.isChecked()) {
			selectedFieldSet.add(otherFindAid);
		}

		if (relatedMaterials.isChecked()) {
			selectedFieldSet.add(relatedMaterials);
		}

		if (bibliography.isChecked()) {
			selectedFieldSet.add(bibliography);
		}
		return selectedFieldSet;
	}

	/**
	 * Get the selected fields
	 * 
	 * @return a list with selected field names
	 */
	public String[] getSelectedFieldNames() {
		List<String> selectedFieldNames = new ArrayList<String>();
		for (FieldOption fieldOption : getSelected()) {
			selectedFieldNames.add(fieldOption.getFieldName());
		}
		return (String[]) selectedFieldNames.toArray(new String[] {});

	}

	/**
	 * Get all possible fields
	 * 
	 * @return a list with all possible field options
	 */
	public List<FieldOption> getAllFields() {
		return Arrays.asList(new FieldOption[] { completeReference, title,
				origination, acqInfoNum, materialspec, physDesc, physDescDimensions,
				physDescPhysFacet, physDescExtent, preferCite, bioghist,
				custodhist, acqinfo, scopeContent, arrangement, appraisal,
				accruals, physTech, accessRestrict, useRestrict, otherFindAid,
				relatedMaterials, bibliography });
	}

	/**
	 * Get the names of all possible fields
	 * 
	 * @return an array with all possible field names
	 */
	public String[] getAllFieldNames() {
		List<FieldOption> allFields = getAllFields();
		String[] ret = new String[allFields.size()];
		int index = 0;
		for (FieldOption fieldOption : allFields) {
			ret[index++] = fieldOption.getFieldName();
		}
		return ret;
	}
}
