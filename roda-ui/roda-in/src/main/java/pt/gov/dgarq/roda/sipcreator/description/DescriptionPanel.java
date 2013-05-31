package pt.gov.dgarq.roda.sipcreator.description;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.sipcreator.Messages;

/**
 * @author Rui Castro
 * @author Luis Faria
 * 
 */
public class DescriptionPanel extends JPanel {
	private static final long serialVersionUID = 6083601300197096712L;

	private DescriptionObject descriptionObject = null;

	private final List<DisclosurePanel> disclosurePanels;

	private JPanel groupsPanel = null;

	private JScrollPane groupsScroll = null;

	private JCheckBox checkboxShowAllFields = null;

	private boolean readonly = false;

	private DescriptionLevelEditor levelEditor = null;

	/**
	 * Constructs a new {@link DescriptionPanel}.
	 */
	public DescriptionPanel() {
		disclosurePanels = new ArrayList<DisclosurePanel>();
		initComponents();
	}

	/**
	 * @return the descriptionObject
	 */
	public DescriptionObject getDescriptionObject() {
		return descriptionObject;
	}

	/**
	 * @param descriptionObject
	 *            the descriptionObject to set
	 */
	public void setDescriptionObject(DescriptionObject descriptionObject) {
		this.descriptionObject = descriptionObject;
		for (DisclosurePanel disclosure : disclosurePanels) {
			disclosure.setDescriptionObject(descriptionObject);
		}

	}

	private void initComponents() {
		setLayout(new BorderLayout());

		add(getShowAllFieldsCheckbox(), BorderLayout.NORTH);

		createGroups();

		for (DisclosurePanel disclosure : disclosurePanels) {
			getGroupsPanel().add(disclosure);
		}

		updateVisibleFields();
		add(getGroupsScroll(), BorderLayout.CENTER);
	}

	private JPanel getGroupsPanel() {
		if (groupsPanel == null) {
			groupsPanel = new JPanel();
			groupsPanel.setLayout(new BoxLayout(groupsPanel, BoxLayout.Y_AXIS));
			groupsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		}
		return groupsPanel;
	}

	private JScrollPane getGroupsScroll() {
		if (groupsScroll == null) {
			groupsScroll = new JScrollPane(getGroupsPanel());
		}
		return groupsScroll;
	}

	private void createGroups() {
		disclosurePanels.add(createIdentificationGroupPanel());
		disclosurePanels.add(createContextGroupPanel());
		disclosurePanels.add(createContentGroupPanel());
		disclosurePanels.add(createAccessGroupPanel());
		disclosurePanels.add(createRelatedMaterialsGroupPanel());
		disclosurePanels.add(createNotesGroupPanel());

	}

	private JCheckBox getShowAllFieldsCheckbox() {
		if (this.checkboxShowAllFields == null) {
			this.checkboxShowAllFields = new JCheckBox(Messages
					.getString("DescriptionPanel.SHOW_ALL_FIELDS"), false);

			this.checkboxShowAllFields.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					updateVisibleFields();
				}

			});
		}
		return this.checkboxShowAllFields;
	}

	private DescriptionLevelEditor getLevelEditor() {
		if (levelEditor == null) {
			levelEditor = new DescriptionLevelEditor();
		}
		return levelEditor;
	}

	private DisclosurePanel createIdentificationGroupPanel() {
		DisclosurePanel panelIdentification = new DisclosurePanel(Messages
				.getString("DescriptionPanel.group.IDENTIFICATION"),
				getDescriptionObject());

		panelIdentification.addElement(DescriptionObject.ID,
				new TextFieldDescriptionEditor(), true);
		panelIdentification.addElement(DescriptionObject.TITLE,
				new TextFieldDescriptionEditor(), true);
		panelIdentification.addElement(DescriptionObject.LEVEL,
				getLevelEditor(), true);
		panelIdentification.addElement(DescriptionObject.DATE_INITIAL,
				new DatePickerDescriptionEditor(), true);
		panelIdentification.addElement(DescriptionObject.DATE_FINAL,
				new DatePickerDescriptionEditor(), true);
		panelIdentification.addElement(DescriptionObject.COUNTRYCODE,
				new TextFieldDescriptionEditor(), true);
		panelIdentification.addElement(DescriptionObject.REPOSITORYCODE,
				new TextFieldDescriptionEditor(), true);
		panelIdentification.addElement(DescriptionObject.ORIGINATION,
				new TextFieldDescriptionEditor(), true);
		panelIdentification.addElement(DescriptionObject.MATERIALSPEC,
				new TextFieldDescriptionEditor(), false);
		panelIdentification.addElement(DescriptionObject.PHYSDESC,
				new TextFieldDescriptionEditor(), false);
		panelIdentification.addElement(DescriptionObject.PHYSDESC_DATE_INITIAL,
				new DatePickerDescriptionEditor(), false);
		panelIdentification.addElement(DescriptionObject.PHYSDESC_DATE_FINAL,
				new DatePickerDescriptionEditor(), false);
		panelIdentification.addElement(DescriptionObject.PHYSDESC_DIMENSIONS,
				new TextFieldDescriptionEditor(), false);
		panelIdentification.addElement(DescriptionObject.PHYSDESC_PHYSFACET,
				new TextFieldDescriptionEditor(), false);
		panelIdentification.addElement(DescriptionObject.PHYSDESC_EXTENT,
				new TextFieldDescriptionEditor(), false);
		panelIdentification.addElement(
				DescriptionObject.LANGMATERIAL_LANGUAGES,
				new TextFieldDescriptionEditor(), false);
		panelIdentification.addElement(DescriptionObject.PREFERCITE,
				new TextFieldDescriptionEditor(), false);

		panelIdentification.update();

		return panelIdentification;

	}

	private DisclosurePanel createContextGroupPanel() {

		DisclosurePanel panelContext = new DisclosurePanel(Messages
				.getString("DescriptionPanel.group.CONTEXT"),
				getDescriptionObject());

		panelContext.addElement(DescriptionObject.BIOGHIST,
				new TextAreaDescriptionEditor(), false);
		panelContext.addElement(DescriptionObject.BIOGHIST_CHRONLIST,
				new TextAreaDescriptionEditor(), false);
		panelContext.addElement(DescriptionObject.CUSTODHIST,
				new TextAreaDescriptionEditor(), false);
		panelContext.addElement(DescriptionObject.ACQINFO,
				new TextAreaDescriptionEditor(), false);

		panelContext.update();

		return panelContext;
	}

	private DisclosurePanel createContentGroupPanel() {
		DisclosurePanel panelContent = new DisclosurePanel(Messages
				.getString("DescriptionPanel.group.CONTEXT"),
				getDescriptionObject());
		panelContent.addElement(DescriptionObject.SCOPECONTENT,
				new TextAreaDescriptionEditor(), true);
		panelContent.addElement(DescriptionObject.ARRANGEMENT,
				new TextAreaDescriptionEditor(), false);
		panelContent.addElement(DescriptionObject.APPRAISAL,
				new TextAreaDescriptionEditor(), false);
		panelContent.addElement(DescriptionObject.ACCRUALS,
				new TextAreaDescriptionEditor(), false);

		panelContent.update();
		return panelContent;
	}

	private DisclosurePanel createAccessGroupPanel() {
		DisclosurePanel panelAccess = new DisclosurePanel(Messages
				.getString("DescriptionPanel.group.ACCESS_AND_USE_CONDITIONS"),
				getDescriptionObject());

		panelAccess.addElement(DescriptionObject.PHYSTECH,
				new TextAreaDescriptionEditor(), false);
		panelAccess.addElement(DescriptionObject.ACCESSRESTRICT,
				new AccessRestrictEditor(), true);
		panelAccess.addElement(DescriptionObject.USERESTRICT,
				new TextAreaDescriptionEditor(), false);

		panelAccess.update();

		return panelAccess;
	}

	private DisclosurePanel createRelatedMaterialsGroupPanel() {

		DisclosurePanel panelRelatedMaterials = new DisclosurePanel(Messages
				.getString("DescriptionPanel.group.ASSOCIATED_MATERIALS"),
				getDescriptionObject());
		panelRelatedMaterials.addElement(DescriptionObject.RELATEDMATERIAL,
				new TextAreaDescriptionEditor(), false);
		panelRelatedMaterials.addElement(DescriptionObject.OTHERFINDAID,
				new TextAreaDescriptionEditor(), false);

		panelRelatedMaterials.update();
		return panelRelatedMaterials;
	}

	private DisclosurePanel createNotesGroupPanel() {

		DisclosurePanel panelNotes = new DisclosurePanel(Messages
				.getString("DescriptionPanel.group.NOTES"),
				getDescriptionObject());
		panelNotes.addElement(DescriptionObject.NOTE,
				new TextAreaDescriptionEditor(), false);
		panelNotes.addElement(DescriptionObject.BIBLIOGRAPHY,
				new TextAreaDescriptionEditor(), false);

		panelNotes.update();

		return panelNotes;
	}

	private void updateVisibleFields() {
		for (DisclosurePanel disclosure : disclosurePanels) {
			disclosure.setOptionalVisible(getShowAllFieldsCheckbox()
					.isSelected());
			disclosure.setReadOnly(readonly);
		}
		repaint();
		revalidate();
	}

	/**
	 * Is description panel read only, i.e. not editable
	 * 
	 * @return true if read only
	 */
	public boolean isReadonly() {
		return readonly;
	}

	/**
	 * Set description panel read only, i.e. not editable
	 * 
	 * @param readonly
	 */
	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
		updateVisibleFields();
	}

	/**
	 * @see DescriptionLevelEditor#isFileChild()
	 * @return true if is a child of an object of level File
	 */
	public boolean isFileChild() {
		return getLevelEditor().isFileChild();
	}

	/**
	 * @see DescriptionLevelEditor#setFileChild(boolean)
	 * @param fileChild
	 *            if is a child of an object of level File
	 */
	public void setFileChild(boolean fileChild) {
		getLevelEditor().setFileChild(fileChild);
	}

}
