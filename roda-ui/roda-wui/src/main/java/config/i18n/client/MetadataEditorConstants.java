/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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

  // Physical Description Genre Form
  @DefaultStringValue("ID")
  public String physDescGenreformID();

  @DefaultStringValue("Source")
  public String physDescGenreformSource();

  @DefaultStringValue("Name")
  public String physDescGenreformName();

  @DefaultStringValue("Description")
  public String physDescGenreformDescription();

  // Material Specs
  @DefaultStringValue("Material specification")
  public String materialSpecsAdd();

  @DefaultStringValue("Label...")
  public String materialSpecsLabel();

  @DefaultStringValue("Value...")
  public String materialSpecsValue();

  // Control Accesses
  @DefaultStringValue("Security category")
  public String controlAccessesAdd();

  @DefaultStringValue("Security category")
  public String controlAccessesTitle();

  @DefaultStringValue("Source")
  public String controlAccessesSource();

  @DefaultStringValue("ID")
  public String controlAccessesID();

  @DefaultStringValue("Name")
  public String controlAccessesName();

  @DefaultStringValue("Description")
  public String controlAccessesDescription();

  @DefaultStringValue("Level")
  public String controlAccessesLevel();

  @DefaultStringValue("Reason")
  public String controlAccessesReason();

  // Related Materials
  @DefaultStringValue("Related material")
  public String relatedMaterialsAdd();

  @DefaultStringValue("Related material")
  public String relatedMaterialsTitle();

  @DefaultStringValue("Description")
  public String relatedMaterialsDescription();

  // Archref
  @DefaultStringValue("Identifier")
  public String archrefAdd();

  @DefaultStringValue("Identifiers")
  public String archrefTitle();

  @DefaultStringValue("Value...")
  public String archrefValue();

  @DefaultStringValue("Other binding info")
  public String archrefNote();

  // Process Info
  @DefaultStringValue("Process info")
  public String processInfoAdd();

  @DefaultStringValue("Reason")
  public String processInfoReason();

  @DefaultStringValue("Input")
  public String processInfoInput();

  @DefaultStringValue("Output")
  public String processInfoOutput();

  @DefaultStringValue("Reason for output")
  public String processInfoReasonForOutput();

  // Acquisition infos
  @DefaultStringValue("Acquisition info")
  public String acqInfosAdd();

  @DefaultStringValue("Acquisition info")
  public String acqInfosTitle();

  @DefaultStringValue("Text")
  public String acqInfosText();

  @DefaultStringValue("Date")
  public String acqInfosDate();

  @DefaultStringValue("Num")
  public String acqInfosNum();

  @DefaultStringValue("Corpname")
  public String acqInfosCorpname();

  // Notes
  @DefaultStringValue("Note")
  public String notesAdd();

  // Keywords
  @DefaultStringValue("Keyword")
  public String keywordsAdd();
}
