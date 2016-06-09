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

import java.util.Date;
import java.util.List;

import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.Permissions.PermissionType;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.safehtml.shared.SafeHtml;

/**
 * @author Luis Faria
 * 
 */
public interface BrowseMessages extends Messages {

  @DefaultMessage("Unable to move the element because the levels of description are not appropriate. Details: {0}")
  public String moveIllegalOperation(String message);

  @DefaultMessage("Unable to move the element because it or the destination were not found in the repository. Details: {0}")
  public String moveNoSuchObject(String message);

  /******* SIDEBAR ******************************************/

  @DefaultMessage("Dates")
  String sidebarFilterDates();

  @DefaultMessage("From date")
  String sidebarFilterFromDatePlaceHolder();

  @DefaultMessage("To date")
  String sidebarFilterToDatePlaceHolder();

  @DefaultMessage("Folders and files")
  String sidebarFoldersFilesTitle();

  @DefaultMessage("Process")
  String sidebarProcessTitle();

  @DefaultMessage("Ingest")
  String sidebarIngestTitle();

  @DefaultMessage("Actions")
  String sidebarActionsTitle();

  @DefaultMessage("Preservation")
  String sidebarPreservationTitle();

  @DefaultMessage("Agents")
  String sidebarAgentsTitle();

  @DefaultMessage("Formats")
  String sidebarFormatsTitle();

  @DefaultMessage("Risks")
  String sidebarRisksTitle();

  @DefaultMessage("Error in line {0}, column {1}: {2}")
  SafeHtml metadataParseError(int line, int column, String message);

  @DefaultMessage("Error")
  SafeHtml notFoundErrorTitle();

  @DefaultMessage("Item with id {0} could not be found.")
  SafeHtml notFoundErrorMessage(String id);

  @DefaultMessage("Error")
  SafeHtml genericErrorTitle();

  @DefaultMessage("An unexpected error occurred when retrieving item. <pre><code>{0}</code></pre>")
  SafeHtml genericErrorMessage(String message);

  @DefaultMessage("Error transforming descriptive metadata into HTML")
  SafeHtml descriptiveMetadataTranformToHTMLError();

  @DefaultMessage("Error transforming preservation metadata into HTML")
  SafeHtml preservationMetadataTranformToHTMLError();

  @DefaultMessage("All collections")
  String allCollectionsTitle();

  @DefaultMessage("Error loading descriptive metadata: {0}")
  String errorLoadingDescriptiveMetadata(String message);

  @DefaultMessage("Error loading preservation metadata: {0}")
  String errorLoadingPreservationMetadata(String message);

  @DefaultMessage("{0,number} files, {1}")
  @AlternateMessage({"one", "One file, {1}"})
  String downloadRepresentationInfo(@PluralCount int numberOfFiles, String readableFileSize);

  @DefaultMessage("Original ({0})")
  SafeHtml downloadTitleOriginal(String repType);

  @DefaultMessage("Alternative ({0})")
  SafeHtml downloadTitleDefault(String repType);

  @DefaultMessage("")
  String titleDatesEmpty();

  @DefaultMessage("From {0,localdatetime,predef:DATE_LONG}")
  String titleDatesNoFinal(Date dateInitial);

  @DefaultMessage("Up to {0,localdatetime,predef:DATE_LONG}")
  String titleDatesNoInitial(Date dateFinal);

  @DefaultMessage("From {0,localdatetime,predef:DATE_LONG} to {1,localdatetime,predef:DATE_LONG}")
  String titleDates(Date dateInitial, Date dateFinal);

  @DefaultMessage("")
  String simpleDatesEmpty();

  @DefaultMessage("{0,date,yyyy-MM-dd}")
  String simpleDatesNoFinal(Date dateInitial);

  @DefaultMessage("To {0,date,yyyy-MM-dd}")
  String simpleDatesNoInitial(Date dateFinal);

  @DefaultMessage("{0,date,yyyy-MM-dd} to {1,date,yyyy-MM-dd}")
  String simpleDates(Date dateInitial, Date dateFinal);

  /****** INGEST TRANSFER **********/

  @DefaultMessage("Ingest transfer")
  String ingestTransferTitle();

  @DefaultMessage("Transferred resources list")
  String ingestTransferList();

  @DefaultMessage("Search transferred resources...")
  String ingestTransferSearchPlaceHolder();

  @DefaultMessage("Created at {0,localdatetime,predef:DATE_TIME_MEDIUM}, with {1}")
  String ingestTransferItemInfo(Date creationDate, String readableFileSize);

  @DefaultMessage("Confirm remove folder")
  String ingestTransferRemoveFolderConfirmDialogTitle();

  @DefaultMessage("Are you sure you want to remove the whole folder \"{0}\"?")
  String ingestTransferRemoveFolderConfirmDialogMessage(String folderName);

  @DefaultMessage("Are you sure you want to remove the the selected {0} files and folders?")
  String ingestTransferRemoveSelectedConfirmDialogMessage(Long size);

  @DefaultMessage("No")
  String ingestTransferRemoveFolderConfirmDialogCancel();

  @DefaultMessage("Yes")
  String ingestTransferRemoveFolderConfirmDialogOk();

  @DefaultMessage("Removed items")
  String ingestTransferRemoveSuccessTitle();

  @DefaultMessage("Successfully removed {0} items")
  String ingestTransferRemoveSuccessMessage(Long size);

  @DefaultMessage("Last checked at {0,localdatetime,predef:DATE_TIME_MEDIUM}")
  String ingestTransferLastScanned(Date lastScanDate);

  @DefaultMessage("Upload")
  String ingestTransferButtonUploadFiles();

  @DefaultMessage("Refresh")
  String ingestTransferButtonRefresh();

  @DefaultMessage("Download")
  String ingestTransferButtonDownload();

  @DefaultMessage("New folder")
  String ingestTransferButtonCreateFolder();

  @DefaultMessage("Remove")
  String ingestTransferButtonRemoveWholeFolder();

  @DefaultMessage("Remove")
  String ingestTransferButtonRemoveSelectedItems();

  @DefaultMessage("Process")
  String ingestTransferButtonIngestWholeFolder();

  @DefaultMessage("Process")
  String ingestTransferButtonIngestSelectedItems();

  @DefaultMessage("Resource not found")
  String ingestTransferNotFoundDialogTitle();

  @DefaultMessage("The resource was not found")
  String ingestTransferNotFoundDialogMessage();

  @DefaultMessage("Continue")
  String ingestTransferNotFoundDialogButton();

  @DefaultMessage("New folder name")
  String ingestTransferCreateFolderTitle();

  @DefaultMessage("Please select a name for your new folder")
  String ingestTransferCreateFolderMessage();

  /***** CREATE PROCESS JOB *********/

  @DefaultMessage("New process")
  String createJobTitle();

  @DefaultMessage("Name")
  String createJobName();

  @DefaultMessage("Run on:")
  String createJobSelectObject();

  @DefaultMessage("Run on all objects")
  String createJobSelectedAllObject();

  @DefaultMessage("Selected submission information packages (SIP)")
  String createJobSelectedSIP();

  @DefaultMessage("Selected archival information packages (AIP)")
  String createJobSelectedAIP();

  @DefaultMessage("Selected representations")
  String createJobSelectedRepresentation();

  @DefaultMessage("Selected files")
  String createJobSelectedFile();

  @DefaultMessage("Ingest workflow")
  String createJobIngestWorkflow();

  @DefaultMessage("Create")
  String createJobCreateTitle();

  @DefaultMessage("Cancel")
  String createJobCancelTitle();

  @DefaultMessage("Workflow")
  String createJobWorkflow();

  @DefaultMessage("Category")
  String createJobCategoryWorkflow();

  @DefaultMessage("All")
  String allCategoryItem();

  @DefaultMessage("Empty")
  String emptyAssociativeObjectLabel();

  /****** INGEST TRANSFER UPLOAD *********/

  @DefaultMessage("Ingest transfer upload")
  String ingestTransferUploadTitle();

  @DefaultMessage("Drop files here")
  String ingestTransferUploadDropHere();

  @DefaultMessage("Choose files...")
  String ingestTransferUploadBrowseFiles();

  @DefaultMessage("done")
  String ingestTransferUploadFinishButton();

  /******** DIALOG GENERIC **************/

  @DefaultMessage("Cancel")
  String dialogCancel();

  @DefaultMessage("OK")
  String dialogOk();

  @DefaultMessage("Yes")
  String dialogYes();

  /*** View representation ***/

  @DefaultMessage("Back")
  String backButton();

  @DefaultMessage("Next file")
  String viewRepresentationNextFileButton();

  @DefaultMessage("Previous file")
  String viewRepresentationPreviousFileButton();

  @DefaultMessage("Download")
  String viewRepresentationDownloadFileButton();

  @DefaultMessage("File info")
  String viewRepresentationInfoFileButton();

  @DefaultMessage("Please select a file from the list on left panel")
  String viewRepresentationEmptyPreview();

  @DefaultMessage("An error occurred while trying to view the file")
  String viewRepresentationErrorPreview();

  @DefaultMessage("File preview not supported")
  String viewRepresentationNotSupportedPreview();

  @DefaultMessage("Search files...")
  String viewRepresentationSearchPlaceHolder();

  @DefaultMessage("Details")
  String viewRepresentationInfoTitle();

  @DefaultMessage("Filename")
  String viewRepresentationInfoFilename();

  @DefaultMessage("Size")
  String viewRepresentationInfoSize();

  @DefaultMessage("Mimetype")
  String viewRepresentationInfoMimetype();

  @DefaultMessage("Format")
  String viewRepresentationInfoFormat();

  @DefaultMessage("PRONOM")
  String viewRepresentationInfoPronom();

  @DefaultMessage("Created date")
  String viewRepresentationInfoCreatedDate();

  @DefaultMessage("Creating application name")
  String viewRepresentationInfoCreatingApplicationName();

  @DefaultMessage("Creating application version")
  String viewRepresentationInfoCreatingApplicationVersion();

  @DefaultMessage("Date created by application")
  String viewRepresentationInfoDateCreatedByApplication();

  @DefaultMessage("Fixity")
  String viewRepresentationInfoHash();

  @DefaultMessage("Storage path")
  String viewRepresentationInfoStoragePath();

  /************* process new ****************/
  @DefaultMessage("Job {0,localdatetime,predef:DATE_TIME_SHORT}")
  String processNewDefaultName(Date date);

  @DefaultMessage("{0} ({1})")
  String pluginLabel(String name, String version);

  @DefaultMessage("Select")
  String pluginAipIdButton();

  @DefaultMessage("Missing mandatory information")
  String processNewMissingMandatoryInfoDialogTitle();

  @DefaultMessage("Please fill the following mandatory parameters: {0,list}")
  String processNewMissingMandatoryInfoDialogMessage(List<String> missingPluginNames);

  /************* Ingest process show ****************/
  @DefaultMessage("waiting")
  String showJobStatusCreated();

  @DefaultMessage("running")
  String showJobStatusStarted();

  @DefaultMessage("done")
  String showJobStatusCompleted();

  @DefaultMessage("failed to start")
  String showJobStatusFailedDuringCreation();

  @DefaultMessage("failed")
  String showJobStatusFailedToComplete();

  @DefaultMessage("{0}% done")
  SafeHtml showJobProgressCompletionPercentage(int completionPercentage);

  @DefaultMessage("{0,number} total")
  SafeHtml showJobProgressTotalCount(int objectsCount);

  @DefaultMessage("{0,number} successful")
  SafeHtml showJobProgressSuccessfulCount(int objectsProcessedWithSuccess);

  @DefaultMessage("{0,number} failed")
  SafeHtml showJobProgressFailedCount(int objectsProcessedWithFailure);

  @DefaultMessage("{0,number} processing")
  SafeHtml showJobProgressProcessingCount(int objectsBeingProcessed);

  @DefaultMessage("{0,number} waiting")
  SafeHtml showJobProgressWaitingCount(int objectsWaitingToBeProcessed);

  @DefaultMessage("Executed {1} of {2} tasks ({0}%)")
  String showJobReportProgress(Integer completionPercentage, Integer stepsCompleted, Integer totalSteps);

  @DefaultMessage("Submission Information Package")
  String showSIPExtended();

  @DefaultMessage("Archival Information Package")
  String showAIPExtended();

  @DefaultMessage("Representation")
  String showRepresentationExtended();

  @DefaultMessage("File")
  String showFileExtended();

  /************* Browse ****************/
  @DefaultMessage("Loading...")
  String browseLoading();

  @DefaultMessage("Confirm remove items")
  String browseRemoveConfirmDialogTitle();

  @DefaultMessage("Are you sure you want the selected items, including all their nested items?")
  String browseRemoveConfirmDialogMessage();

  /************* Search ****************/
  @DefaultMessage("Intelectual entities")
  String searchListBoxItems();

  @DefaultMessage("Representations")
  String searchListBoxRepresentations();

  @DefaultMessage("Files")
  String searchListBoxFiles();

  @DefaultMessage("Search...")
  String searchPlaceHolder();

  @DefaultMessage("Search results")
  String searchResults();

  @DefaultMessage("2008-04-01")
  String searchFieldDatePlaceHolder();

  @DefaultMessage("2008-04-01")
  String searchFieldDateFromPlaceHolder();

  @DefaultMessage("2016-06-20")
  String searchFieldDateToPlaceHolder();

  @DefaultMessage("42")
  String searchFieldNumericPlaceHolder();

  @DefaultMessage("42")
  String searchFieldNumericFromPlaceHolder();

  @DefaultMessage("108")
  String searchFieldNumericToPlaceHolder();

  @DefaultMessage("Identifier")
  String searchRepresentationFieldIdentifier();

  @DefaultMessage("Size")
  String searchRepresentationFieldSize();

  @DefaultMessage("Number of files")
  String searchRepresentationFieldNumberOfFiles();

  @DefaultMessage("Original")
  String searchRepresentationFieldOriginal();

  @DefaultMessage("Filename")
  String searchFileFieldFilename();

  @DefaultMessage("Format")
  String searchFileFieldFormat();

  @DefaultMessage("Pronom")
  String searchFileFieldPronom();

  @DefaultMessage("Mimetype")
  String searchFileFieldMimetype();

  @DefaultMessage("Filesize")
  String searchFileFieldFilesize();

  @DefaultMessage("Fulltext")
  String searchFileFieldFulltext();

  /************* Preservation Event List ****************/

  @DefaultMessage("Date")
  String preservationEventListHeaderDate();

  @DefaultMessage("Agents")
  String preservationEventListHeaderAgent();

  @DefaultMessage("Type")
  String preservationEventListHeaderType();

  @DefaultMessage("Detail")
  String preservationEventListHeaderDetail();

  @DefaultMessage("Source objects")
  String preservationEventListHeaderSourceObject();

  @DefaultMessage("Outcome objects")
  String preservationEventListHeaderOutcomeObject();

  @DefaultMessage("Outcome")
  String preservationEventListHeaderOutcome();

  /*** Preservation events ***/

  @DefaultMessage("Download")
  String preservationEventsDownloadButton();

  @DefaultMessage("Back")
  String preservationEventsBackButton();

  @DefaultMessage("Preservation events")
  String preservationEventsTitle();

  @DefaultMessage("Preservation event")
  String preservationEventTitle();

  @DefaultMessage("Identifier")
  String preservationEventId();

  @DefaultMessage("Date")
  String preservationEventDatetime();

  @DefaultMessage("Outcome")
  String preservationEventOutcome();

  @DefaultMessage("Type")
  String preservationEventType();

  @DefaultMessage("Detail")
  String preservationEventDetail();

  @DefaultMessage("Agents")
  String preservationEventAgentsHeader();

  @DefaultMessage("Role")
  String preservationEventAgentRole();

  @DefaultMessage("Source objects")
  String preservationEventSourceObjectsHeader();

  @DefaultMessage("Outcome objects")
  String preservationEventOutcomeObjectsHeader();

  @DefaultMessage("Identifier")
  String preservationEventObjectId();

  @DefaultMessage("Role")
  String preservationEventObjectRole();

  @DefaultMessage("Outcome detail")
  String preservationEventOutcomeDetailHeader();

  @DefaultMessage("Note")
  String preservationEventOutcomeDetailNote();

  @DefaultMessage("Extension")
  String preservationEventOutcomeDetailExtension();

  @DefaultMessage("{0} at {1,localdatetime,predef:DATE_TIME_MEDIUM}")
  String descriptiveMetadataHistoryLabel(String versionKey, Date createdDate);

  /************* Select AIP Dialog ****************/

  @DefaultMessage("Cancel")
  String selectAipCancelButton();

  @DefaultMessage("Move to root")
  String selectAipEmptyParentButton();

  @DefaultMessage("Select")
  String selectAipSelectButton();

  @DefaultMessage("Search results")
  String selectAipSearchResults();

  @DefaultMessage("Search records...")
  String selectAipSearchPlaceHolder();

  /************* Move Item ****************/

  @DefaultMessage("Move item")
  String moveItemTitle();

  @DefaultMessage("Error moving item!")
  String moveItemFailed();

  /************* Select Parent ****************/

  @DefaultMessage("Select parent")
  String selectParentTitle();

  /*************
   * Lists
   * 
   * @param count
   ****************/

  @DefaultMessage("Select all {0} items")
  String listSelectAllMessage(int count);

  /************* User log ****************/

  @DefaultMessage("Search log...")
  String userLogSearchPlaceHolder();

  /************* Notification messages ****************/

  @DefaultMessage("Search message...")
  String messageSearchPlaceHolder();

  @DefaultMessage("yes")
  String showMessageAcknowledged();

  @DefaultMessage("no")
  String showMessageNotAcknowledged();

  /************* Humanize ****************/

  @DefaultMessage("{0} days")
  @AlternateMessage({"one", "1 day"})
  String durationDHMSDay(@PluralCount int days);

  @DefaultMessage("{0} hours")
  @AlternateMessage({"one", "1 hour"})
  String durationDHMSHour(@PluralCount int hours);

  @DefaultMessage("{0} minutes")
  @AlternateMessage({"one", "1 minute"})
  String durationDHMSMinutes(@PluralCount int minutes);

  @DefaultMessage("{0} seconds")
  @AlternateMessage({"one", "1 seconds"})
  String durationDHMSSeconds(@PluralCount int seconds);

  @DefaultMessage("less than a second")
  String durationDHMSLessThanASecond();

  @DefaultMessage(", ")
  String durationDHMSSeparator();

  @DefaultMessage(" and ")
  String durationDHMSSecondSeparator();

  @DefaultMessage("{0}d {1,number,##}h {2,number,##}m {3,number,##}s")
  String durationDHMSShortDays(int days, int hours, int minutes, int seconds);

  @DefaultMessage("{0,number,##}h {1,number,##}m {2,number,##}s")
  String durationDHMSShortHours(int hours, int minutes, int seconds);

  @DefaultMessage("{0,number,##}m {1,number,##}s")
  String durationDHMSShortMinutes(int minutes, int seconds);

  @DefaultMessage("{0,number,##}s")
  String durationDHMSShortSeconds(int seconds);

  @DefaultMessage("Unknown")
  @AlternateMessage({"CREATE", "Create", "DELETE", "Delete", "GRANT", "Grant", "READ", "Read", "UPDATE", "Update"})
  String objectPermission(@Select PermissionType permissionType);

  @DefaultMessage("")
  @AlternateMessage({"CREATE", "Permission to submit or create a new archival package under this one", "DELETE",
    "Permission to delete this archival package or any of its sub-components", "GRANT",
    "Permission to change the permissions of this archival package", "READ",
    "Permission to seach or access this archival package", "UPDATE",
    "Permission to change this archival package or any of its sub-components"})
  String objectPermissionDescription(@Select PermissionType permissionType);

  /************* Ingest appraisal ****************/

  @DefaultMessage("Accept")
  String ingestAppraisalAcceptButton();

  @DefaultMessage("Reject")
  String ingestAppraisalRejectButton();

  @DefaultMessage("Unknown")
  @AlternateMessage({"ACTIVE", "active", "CREATED", "created", "DELETED", "deleted", "INGEST_PROCESSING", "ingesting",
    "UNDER_APPRAISAL", "under appraisal"})
  String aipState(@Select AIPState state);

  /************* Search pre-filters ****************/

  @DefaultMessage("{0}: {1}")
  SafeHtml searchPreFilterSimpleFilterParameter(String searchPreFilterName, String searchPreFilterValue);

  @DefaultMessage("{0}: {1}")
  SafeHtml searchPreFilterBasicSearchFilterParameter(String searchPreFilterName, String searchPreFilterValue);

  @DefaultMessage("NOT {0}: {1}")
  SafeHtml searchPreFilterNotSimpleFilterParameter(String searchPreFilterName, String searchPreFilterValue);

  @DefaultMessage("NO {0}")
  SafeHtml searchPreFilterEmptyKeyFilterParameter(String searchPreFilterName);

  @DefaultMessage("{0}")
  @AlternateMessage({"state", "state", "ingestJobId", "job", "parentId", "parent", "ancestors", "ancestors"})
  String searchPreFilterName(@Select String name);

  @DefaultMessage("{0}")
  @AlternateMessage({"UNDER_APPRAISAL", "under appraisal"})
  String searchPreFilterValue(@Select String name);

  /************* Risk register ****************/

  @DefaultMessage("Risks of AIP {0}")
  String showRiskAIPTitle(String aipId);

  @DefaultMessage("Risk register")
  String riskRegisterTitle();

  @DefaultMessage("Category")
  String riskRegisterCategory();

  @DefaultMessage("Severity")
  String riskRegisterSeverity();

  @DefaultMessage("Owner")
  String riskRegisterOwner();

  @DefaultMessage("New")
  String riskRegisterNewButton();

  @DefaultMessage("Edit")
  String riskRegisterEditButton();

  @DefaultMessage("Remove")
  String riskRegisterRemoveButton();

  @DefaultMessage("Start new process")
  String riskRegisterProcessButton();

  @DefaultMessage("Search risks...")
  String riskRegisterSearchPlaceHolder();

  @DefaultMessage("This object has {0,number} risks")
  @AlternateMessage({"one", "This object has 1 risk"})
  String showObjectsRiskCounter(@PluralCount int counter);

  @DefaultMessage("Risk creation failed {0}")
  String createRiskFailure(String message);

  @DefaultMessage("Risk not found {0}")
  String editRiskNotFound(String name);

  @DefaultMessage("Risk edit failed {0}")
  String editRiskFailure(String message);

  @DefaultMessage("Confirm remove risk")
  String riskRemoveFolderConfirmDialogTitle();

  @DefaultMessage("Are you sure you want to remove the the selected {0} risk(s)?")
  String riskRemoveSelectedConfirmDialogMessage(Long size);

  @DefaultMessage("No")
  String riskRemoveFolderConfirmDialogCancel();

  @DefaultMessage("Yes")
  String riskRemoveFolderConfirmDialogOk();

  @DefaultMessage("Removed risk(s)")
  String riskRemoveSuccessTitle();

  @DefaultMessage("Successfully removed {0} risk(s)")
  String riskRemoveSuccessMessage(Long size);

  @DefaultMessage("Risk was modified")
  String modifyRiskMessage();

  @DefaultMessage("{0} at {1,localdatetime,predef:DATE_TIME_MEDIUM}")
  String riskHistoryLabel(String versionKey, Date createdDate);

  @DefaultMessage("Low")
  String showLowSeverity();

  @DefaultMessage("Moderate")
  String showModerateSeverity();

  @DefaultMessage("High")
  String showHighSeverity();

  @DefaultMessage("Risks")
  String getRisksDialogName();

  /************* Agent register ****************/

  @DefaultMessage("Agent register")
  String agentRegisterTitle();

  @DefaultMessage("New")
  String agentRegisterNewButton();

  @DefaultMessage("Edit")
  String agentRegisterEditButton();

  @DefaultMessage("Remove")
  String agentRegisterRemoveButton();

  @DefaultMessage("Start new process")
  String agentRegisterProcessButton();

  @DefaultMessage("Search agents...")
  String agentRegisterSearchPlaceHolder();

  @DefaultMessage("Agent creation failed {0}")
  String createAgentFailure(String message);

  @DefaultMessage("Agent not found {0}")
  String editAgentNotFound(String name);

  @DefaultMessage("Agent edit failed {0}")
  String editAgentFailure(String message);

  @DefaultMessage("Confirm remove agent")
  String agentRemoveFolderConfirmDialogTitle();

  @DefaultMessage("Are you sure you want to remove the the selected {0} agent(s)?")
  String agentRemoveSelectedConfirmDialogMessage(Long size);

  @DefaultMessage("No")
  String agentRemoveFolderConfirmDialogCancel();

  @DefaultMessage("Yes")
  String agentRemoveFolderConfirmDialogOk();

  @DefaultMessage("Removed agent(s)")
  String agentRemoveSuccessTitle();

  @DefaultMessage("Successfully removed {0} agent(s)")
  String agentRemoveSuccessMessage(Long size);

  @DefaultMessage("{0}")
  String agentListItems(String item);

  @DefaultMessage("Agents")
  String getAgentsDialogName();

  /************* Format register ****************/

  @DefaultMessage("Formats")
  String getFormatsDialogName();

  @DefaultMessage("Format register")
  String formatRegisterTitle();

  @DefaultMessage("New")
  String formatRegisterNewButton();

  @DefaultMessage("Edit")
  String formatRegisterEditButton();

  @DefaultMessage("Remove")
  String formatRegisterRemoveButton();

  @DefaultMessage("Start new process")
  String formatRegisterProcessButton();

  @DefaultMessage("Search formats...")
  String formatRegisterSearchPlaceHolder();

  @DefaultMessage("Format creation failed {0}")
  String createFormatFailure(String message);

  @DefaultMessage("Format not found {0}")
  String editFormatNotFound(String name);

  @DefaultMessage("Format edit failed {0}")
  String editFormatFailure(String message);

  @DefaultMessage("Confirm remove format")
  String formatRemoveFolderConfirmDialogTitle();

  @DefaultMessage("Are you sure you want to remove the the selected {0} format(s)?")
  String formatRemoveSelectedConfirmDialogMessage(Long size);

  @DefaultMessage("No")
  String formatRemoveFolderConfirmDialogCancel();

  @DefaultMessage("Yes")
  String formatRemoveFolderConfirmDialogOk();

  @DefaultMessage("Removed format(s)")
  String formatRemoveSuccessTitle();

  @DefaultMessage("Successfully removed {0} format(s)")
  String formatRemoveSuccessMessage(Long size);

  @DefaultMessage("{0}")
  String formatListItems(String item);

  /************* Common Messages ****************/

  @DefaultMessage("{0}: {1}")
  String logParameter(String name, String value);

  // Content Panel
  @DefaultMessage("Page not found: {0}")
  public String pageNotFound(String error);

  @DefaultMessage("RODA - {0}")
  public String windowTitle(String history);
}
