/**
 * 
 */
package config.i18n.client;

import com.google.gwt.i18n.client.ConstantsWithLookup;

/**
 * @author Luis Faria
 * 
 */
public interface MetadataEditorConstants extends ConstantsWithLookup {

	// Chron List Editor
	@DefaultStringValue("OCCURRENCE")
	public String newChronItem();

	@DefaultStringValue("Begin Date")
	public String chronitemInitialDate();

	@DefaultStringValue("End Date")
	public String chronitemFinalDate();

	// Arrangement table editor
	@DefaultStringValue("LINE")
	public String addRow();

	@DefaultStringValue("COLUMN")
	public String addColumn();

	@DefaultStringValue("All information in this column will be lost, are you sure you want to remove?")
	public String removeColumnConfirmation();

	// Move element
	@DefaultStringValue("Choose the destination")
	public String moveChooseDestinationTitle();

	@DefaultStringValue("MOVE")
	public String moveChooseDestinationChoose();

	@DefaultStringValue("CANCEL")
	public String moveChooseDestinationCancel();

	@DefaultStringValue("The element has been successfully moved")
	public String moveSuccessful();

	// Edit Producers Panel
	@DefaultStringValue("Users or groups:")
	public String editProducersTitle();

	@DefaultStringValue("User")
	public String editProducersAddUser();

	@DefaultStringValue("Group")
	public String editProducersAddGroup();

	@DefaultStringValue("Producer")
	public String editProducersDelete();

	@DefaultStringValue("RODA-in")
	public String editProducersRodaIn();

	// Edit Object Permissions Panel
	@DefaultStringValue("Apply recursively to all sub-levels")
	public String objectPermissionsApplyRecursivelly();

	@DefaultStringValue("User")
	public String objectPermissionsAddUser();

	@DefaultStringValue("Group")
	public String objectPermissionsAddGroup();

	@DefaultStringValue("Save")
	public String objectPermissionsSave();

	// Meta permissions
	@DefaultStringValue("No Access")
	public String permission_object_NoAccess();

	@DefaultStringValue("Access to metadata and disseminations")
	public String permission_object_ReadOnly();

	@DefaultStringValue("Access and metadata edition")
	public String permission_object_ReadAndEditMetadata();

	@DefaultStringValue("Full Control")
	public String permission_object_FullControl();

}
