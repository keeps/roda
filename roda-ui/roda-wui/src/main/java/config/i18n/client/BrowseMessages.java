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

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.safehtml.shared.SafeHtml;

/**
 * @author Luis Faria
 * 
 */
public interface BrowseMessages extends Messages {

  /*********************************************************/
  /******************* OLD MESSAGES ***********************/
  /*********************************************************/

  // Tree
  @DefaultMessage("See {0}-{1}")
  public String previousItems(int from, int to);

  @DefaultMessage("See {0}-{1} (total {2})")
  public String nextItems(int from, int to, int total);

  // Item Popup
  @DefaultMessage("To lock wait {0} sec.")
  public String waitToLock(int sec);

  @DefaultMessage("Click here to close")
  public String close();

  // Browse
  @DefaultMessage("{0} fonds")
  public String totalFondsNumber(int count);

  @DefaultMessage("There is no such element in the repository identified by{0}.")
  public String noSuchRODAObject(String pid);

  // Edit
  @DefaultMessage("Unable to save the changes. Details: {0}")
  public String editSaveError(String message);

  @DefaultMessage("Unable to move the element because the levels of description are not appropriate. Details: {0}")
  public String moveIllegalOperation(String message);

  @DefaultMessage("Unable to move the element because it or the destination were not found in the repository. Details: {0}")
  public String moveNoSuchObject(String message);

  // Representations Panel
  @DefaultMessage("Disseminations of {0} - ''{1}''")
  public String representationsTitle(String id, String title);

  @DefaultMessage("{0} does not have associated representations")
  public String noRepresentationsTitle(String id);

  @DefaultMessage("Download representation with format {0}, {1} files, {2} bytes uncompressed")
  public String representationDownloadTooltip(String format, int numberOfFiles, long sizeOfFiles);

  // Preservation Metadata Panel
  @DefaultMessage("{0} (original)")
  public String preservationRepOriginal(String format);

  @DefaultMessage("{0} (normalized)")
  public String preservationRepNormalized(String format);

  @DefaultMessage("{0}")
  public String preservationRepAlternative(String format);

  @DefaultMessage("{0} files, {1} bytes")
  public String preservationRepTooltip(int numberOfFiles, long sizeOfFiles);

  /*********************************************************/

  /******************* NEW MESSAGES ***********************/
  /*********************************************************/

  /******* SIDEBAR ******************************************/
  @DefaultMessage("Filters")
  String sidebarFiltersTitle();

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

  @DefaultMessage("download")
  SafeHtml download();

  @DefaultMessage("PREMIS")
  String premisTitle();

  @DefaultMessage("{0,number} files, {1}")
  @AlternateMessage({"one", "One file, {1}"})
  String downloadRepresentationInfo(@PluralCount int numberOfFiles, String readableFileSize);

  @DefaultMessage("Original")
  SafeHtml downloadTitleOriginal();

  @DefaultMessage("Alternative")
  SafeHtml downloadTitleDefault();

  @DefaultMessage("")
  String titleDatesEmpty();

  @DefaultMessage("From {0,localdatetime,predef:DATE_MEDIUM}")
  String titleDatesNoFinal(Date dateInitial);

  @DefaultMessage("Up to {0,localdatetime,predef:DATE_MEDIUM}")
  String titleDatesNoInitial(Date dateFinal);

  @DefaultMessage("From {0,localdatetime,predef:DATE_MEDIUM} to {1,localdatetime,predef:DATE_MEDIUM}")
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
  String ingestTransferRemoveSelectedConfirmDialogMessage(int size);

  @DefaultMessage("No")
  String ingestTransferRemoveFolderConfirmDialogCancel();

  @DefaultMessage("Yes")
  String ingestTransferRemoveFolderConfirmDialogOk();

  @DefaultMessage("Removed items")
  String ingestTransferRemoveSuccessTitle();

  @DefaultMessage("Successfully removed {0} items")
  String ingestTransferRemoveSuccessMessage(int size);

  @DefaultMessage("Upload")
  String ingestTransferButtonUploadFiles();
  
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

  @DefaultMessage("New ingest process")
  String createJobTitle();

  @DefaultMessage("Name")
  String createJobName();

  @DefaultMessage("Selected submission information packages (SIP)")
  String createJobSelectedSIP();

  @DefaultMessage("Ingest workflow")
  String createJobIngestWorkflow();

  @DefaultMessage("Create")
  String createJobCreateTitle();

  @DefaultMessage("Cancel")
  String createJobCancelTitle();

  /****** INGEST TRANSFER UPLOAD *********/

  @DefaultMessage("Ingest transfer upload")
  String ingestTransferUploadTitle();

  @DefaultMessage("Drop files here")
  String ingestTransferUploadDropHere();

  @DefaultMessage("Choose files...")
  String ingestTransferUploadBrowseFiles();

  @DefaultMessage("Done")
  String ingestTransferUploadFinishButton();

  /******** DIALOG GENERIC **************/

  @DefaultMessage("Cancel")
  String dialogCancel();

  @DefaultMessage("OK")
  String dialogOk();

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

  /************* Ingest process new ****************/
  @DefaultMessage("Job {0,localdatetime,predef:DATE_TIME_SHORT}")
  String ingestProcessNewDefaultName(Date date);

  @DefaultMessage("{0} ({1})")
  String pluginLabel(String name, String version);

  @DefaultMessage("Missing mandatory information")
  String ingestProcessNewMissingMandatoryInfoDialogTitle();

  @DefaultMessage("Please fill the following mandatory parameters: {0,list}")
  String ingestProcessNewMissingMandatoryInfoDialogMessage(List<String> missingPluginNames);

  /************* Ingest process show ****************/
  @DefaultMessage("Waiting to start")
  String showJobStatusCreated();

  @DefaultMessage("Processing ({0}%)")
  String showJobStatusStarted(int completionPercentage);

  @DefaultMessage("Completed at {0,localdatetime,predef:DATE_TIME_MEDIUM}")
  String showJobStatusCompleted(Date endDate);

  @DefaultMessage("Failed at {0,localdatetime,predef:DATE_TIME_MEDIUM}")
  String showJobStatusFailedDuringCreation(Date endDate);

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

  /*** Preservation event ***/

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

  /************* Move item ****************/

  @DefaultMessage("Move item")
  String moveItemTitle();
  
  @DefaultMessage("Cancel")
  String moveItemCancel();
  
  @DefaultMessage("Move")
  String moveItemMove();
  
  @DefaultMessage("Search results")
  String moveItemSearchResults();
  
  @DefaultMessage("Search records...")
  String moveItemSearchPlaceHolder();
  
  @DefaultMessage("Error moving item!")
  String moveItemFailed();
}
