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
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.log.LogEntry.LOG_ENTRY_STATE;
import org.roda.core.data.v2.notifications.Notification.NOTIFICATION_STATE;
import org.roda.core.data.v2.risks.Risk.SEVERITY_LEVEL;
import org.roda.core.data.v2.risks.RiskIncidence.INCIDENCE_STATUS;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.safehtml.shared.SafeHtml;

/**
 * @author Luis Faria
 * 
 */
public interface ClientMessages extends Messages {

  String moveIllegalOperation(String message);

  String moveNoSuchObject(String message);

  String of();

  /******* TITLES ******************************************/

  String genericTitle();

  String processTitle();

  String reportTitle();

  String ingestAppraisalTitle();

  String riskHistoryTitle();

  String riskRegisterTitle();

  String riskIncidenceRegisterTitle();

  String editIncidenceTitle();

  String editIncidencesTitle();

  String showRiskIncidenceTitle();

  String newFormatTitle();

  String createFormatTitle();

  String editFormatTitle();

  String showFormatTitle();

  String formatsTitle();

  String newRiskTitle();

  String createRiskTitle();

  String editRiskTitle();

  String showRiskTitle();

  String risksTitle();

  String riskIncidencesTitle();

  String newDescriptiveMetadataTitle();

  String historyDescriptiveMetadataTitle();

  String catalogueTitle();

  String catalogueItemTitle();

  String catalogueRepresentationTitle();

  String catalogueFolderTitle();

  String catalogueFileTitle();

  String preservationActionsTitle();

  String ingestProcessTitle();

  String loginTitle();

  String searchTitle();

  String activityLogTitle();

  String showLogEntryTitle();

  String notificationsTitle();

  String notificationTitle();

  String usersAndGroupsTitle();

  String usersAndGroupsSearchPlaceHolder();

  String logsTitle();

  String changeTypeTitle();

  String changeTypePlaceHolder();

  String createFolderTitle();

  String createFolderPlaceholder();

  String outcomeDetailTitle();

  String outcomeDetailPlaceholder();

  String outcomeDetailFailed();

  /******* SIDEBAR ******************************************/

  String sidebarFilterDates();

  String sidebarFilterFromDatePlaceHolder();

  String sidebarFilterToDatePlaceHolder();

  String sidebarFoldersFilesTitle();

  String sidebarProcessTitle();

  String sidebarIngestTitle();

  String sidebarActionsTitle();

  String sidebarPreservationTitle();

  String sidebarFormatsTitle();

  String sidebarRisksTitle();

  String sidebarRiskIncidencesTitle();

  String sidebarAppraisalTitle();

  String sidebarJobReportStatusTitle();

  String sidebarJobReportLastActionTitle();

  SafeHtml metadataParseError(int line, int column, String message);

  SafeHtml notFoundErrorTitle();

  SafeHtml notFoundErrorMessage(String id);

  SafeHtml genericErrorTitle();

  SafeHtml genericErrorMessage(String message);

  SafeHtml descriptiveMetadataTranformToHTMLError();

  SafeHtml preservationMetadataTranformToHTMLError();

  String notFoundError();

  String allCollectionsTitle();

  String errorLoadingDescriptiveMetadata(String message);

  String errorLoadingPreservationMetadata(String message);

  String downloadRepresentationInfo(@PluralCount int numberOfFiles, String readableFileSize);

  SafeHtml downloadTitleOriginal(String repType);

  SafeHtml downloadTitleDefault(String repType);

  String titleDatesEmpty();

  String titleDatesNoFinal(Date dateInitial);

  String titleDatesNoInitial(Date dateFinal);

  String titleDates(Date dateInitial, Date dateFinal);

  String simpleDatesEmpty();

  String simpleDatesNoFinal(Date dateInitial);

  String simpleDatesNoInitial(Date dateFinal);

  String simpleDates(Date dateInitial, Date dateFinal);

  String featureNotYetImplemented();

  String itemWasAccepted();

  String itemWasRejected();

  String rejectMessage();

  String rejectQuestion();

  String rejectSIPQuestion();

  String renameSIPPlaceholder();

  String renameSIPFailed();

  String renameSIPSuccessful();

  String moveSIPFailed();

  String moveSIPSuccessful();

  String changeTypeSuccessful();

  String allItemsWereAccepted();

  String jobNotFound();

  String updateIsCurrentlyRunning();

  String updatedFilesUnderFolder();

  String updateIsBeginning();

  String riskRefreshDone();

  String processCreated();

  String otherItem();

  String metadataFileCreated();

  String metadataFileSaved();

  String metadataFileRemoved();

  String versionReverted();

  String versionDeleted();

  String selectUserOrGroupToAdd();

  String permissionsChanged();

  String couldNotFindPreservationEvent();

  String intellectualEntity();

  String inspectIntellectualEntity();

  String inspectRepresentation();

  String inspectFile();

  String allPlugins();

  String allIntellectualEntities();

  String allRepresentations();

  String allFiles();

  String allRisks();

  String allRiskIncidences();

  String allFormats();

  String allNotifications();

  String allLogEntries();

  String allJobs();

  String allJobReports();

  String allTransferredResources();

  String allOfAObject(@Select String objectClass);

  String selectItems();

  String noInputObjects();

  String inspectTransferredResource();

  String identifierNotFound();

  String originalRepresentation();

  String alternativeRepresentation();

  String representationStatus();

  String executingTaskMessage();

  String noItemsToDisplay();

  String browserOfflineError();

  String cannotReachServerError();

  /****** INGEST TRANSFER **********/

  String ingestTransferTitle();

  String ingestTransferList();

  String ingestTransferSearchPlaceHolder();

  String ingestTransferItemInfo(Date creationDate, String readableFileSize);

  String ingestTransferRemoveFolderConfirmDialogTitle();

  String ingestTransferRemoveFolderConfirmDialogMessage(String folderName);

  String ingestTransferRemoveAllSelectedConfirmDialogMessage();

  String ingestTransferRemoveSelectedConfirmDialogMessage(Long size);

  String ingestTransferLastScanned(Date lastScanDate);

  String ingestTransferNotFoundDialogTitle();

  String ingestTransferNotFoundDialogMessage();

  String ingestTransferNotFoundDialogButton();

  String ingestTransferCreateFolderTitle();

  String ingestTransferCreateFolderMessage();

  String ingestAllTransferredPackages();

  String ingestJobList();

  /***** CREATE PROCESS JOB *********/

  String createJobTitle();

  String createJobName();

  String createJobSelectObject();

  String createJobSelectedAllObject();

  String createJobSelectedSIP();

  String createJobSelectedAIP();

  String createJobSelectedRepresentation();

  String createJobSelectedFile();

  String createJobIngestWorkflow();

  String createJobCreateTitle();

  String createJobCancelTitle();

  String createJobWorkflow();

  String createJobCategorySelect();

  SafeHtml createJobCategoryWorkflow(List<String> categoryLabels);

  String allCategoryItem();

  String emptyAssociativeObjectLabel();

  /****** INGEST TRANSFER UPLOAD *********/

  String ingestTransferUploadTitle();

  String ingestTransferUploadDropHere();

  String ingestTransferUploadBrowseFiles();

  String ingestTransferUploadFinishButton();

  /******** DIALOG GENERIC **************/

  String dialogCancel();

  String dialogOk();

  String dialogNo();

  String dialogYes();

  String dialogSorry();

  String dialogDone();

  String dialogSuccess();

  String dialogFailure();

  String dialogRefresh();

  /*** Generic Buttons ***/

  String backButton();

  String cancelButton();

  String revertButton();

  String removeButton();

  String refreshButton();

  String newButton();

  String createButton();

  String editButton();

  String saveButton();

  String addButton();

  String confirmButton();

  String applyAllButton();

  String stopButton();

  String viewButton();

  String listButton();

  String downloadButton();

  String uploadFilesButton();

  String renameButton();

  String moveButton();

  String createFolderButton();

  String removeWholeFolderButton();

  String removeSelectedItemsButton();

  String ingestWholeFolderButton();

  String ingestSelectedItemsButton();

  String changeTypeButton();

  /*** Identify formats ***/

  String identifyFormatsButton();

  String identifyingFormatsTitle();

  String identifyingFormatsDescription();

  /*** View representation ***/

  String viewRepresentationNextFileButton();

  String viewRepresentationPreviousFileButton();

  String viewRepresentationDownloadFileButton();

  String viewRepresentationRemoveFileButton();

  String viewRepresentationInfoFileButton();

  String viewRepresentationEmptyPreview();

  String viewRepresentationErrorPreview();

  String viewRepresentationTooLargeErrorPreview();

  String viewRepresentationNotSupportedPreview();

  String viewRepresentationSearchPlaceHolder();

  String viewRepresentationInfoTitle();
  
  String viewRepresentationFileDisseminationTitle();

  String viewRepresentationInfoFilename();

  String viewRepresentationInfoSize();

  String viewRepresentationInfoMimetype();

  String viewRepresentationInfoFormat();

  String viewRepresentationInfoFormatVersion();

  String viewRepresentationInfoPronom();

  String viewRepresentationInfoCreatedDate();

  String viewRepresentationInfoCreatingApplicationName();

  String viewRepresentationInfoCreatingApplicationVersion();

  String viewRepresentationInfoDateCreatedByApplication();

  String viewRepresentationInfoHash();

  String viewRepresentationInfoStoragePath();

  String viewRepresentationRemoveFileTitle();

  String viewRepresentationRemoveFileMessage();

  /************* Process new ****************/

  String processNewDefaultName(Date date);

  String pluginLabel(String name, String version);

  String pluginAipIdButton();

  String processNewMissingMandatoryInfoDialogTitle();

  String processNewMissingMandatoryInfoDialogMessage(List<String> missingPluginNames);

  String showPluginCategories(@Select String category);

  /************* Ingest process show ****************/

  String showJobStatusCreated();

  String showJobStatusStarted();

  String showJobStatusCompleted();

  String showJobStatusFailedDuringCreation();

  String showJobStatusFailedToComplete();

  String showJobStatusStopping();

  String showJobStatusStopped();

  SafeHtml showJobProgressCompletionPercentage(int completionPercentage);

  SafeHtml showJobProgressTotalCount(int objectsCount);

  SafeHtml showJobProgressSuccessfulCount(int objectsProcessedWithSuccess);

  SafeHtml showJobProgressFailedCount(int objectsProcessedWithFailure);

  SafeHtml showJobProgressProcessingCount(int objectsBeingProcessed);

  SafeHtml showJobProgressWaitingCount(int objectsWaitingToBeProcessed);

  String showJobReportProgress(Integer completionPercentage, Integer stepsCompleted, Integer totalSteps);

  String showSIPExtended();

  String showAIPExtended();

  String showRepresentationExtended();

  String showFileExtended();

  String showTransferredResourceExtended();

  String showJobSourceObjects();

  String jobStopConfirmDialogTitle();

  String jobStopConfirmDialogMessage();

  /************* Generic remove toast and dialog ****************/

  String removeSuccessTitle();

  String removeAllSuccessMessage();

  String removeSuccessMessage(Long size);

  String removeConfirmDialogTitle();

  String removeAllConfirmDialogMessage();

  String removeSelectedConfirmDialogMessage(Long size);

  String removeAllSelectedConfirmDialogMessage();

  /************* Browse ****************/
  String browseLoading();

  String itemId();

  String sipId();

  String processId();

  String itemIdMin(String itemId);

  String sipIdMin(String sipId);

  /************* Search ****************/
  String searchListBoxItems();

  String searchListBoxRepresentations();

  String searchListBoxFiles();

  String searchPlaceHolder();

  String searchResults();

  String searchFieldDatePlaceHolder();

  String searchFieldDateFromPlaceHolder();

  String searchFieldDateToPlaceHolder();

  String searchFieldNumericPlaceHolder();

  String searchFieldNumericFromPlaceHolder();

  String searchFieldNumericToPlaceHolder();

  String addSearchField();

  String searchButton();

  /*************
   * AIP, Representations, File & Transferred Resources
   *************/

  String aipsTitle();

  String aipGenericTitle();

  String aipDates();

  String aipHasRepresentations();

  String folderId();

  String folderFiles();

  String fileId();

  String fileName();

  String filePath();

  String fileFormat();

  String fileSize();

  String fileLength();

  String transferredResourceName();

  String transferredResourcePath();

  String transferredResourceSize();

  String transferredResourceDateCreated();

  String numberOfFiles(long count);

  String representationId();

  String representationOriginal();

  String representationType();

  String representationSize();

  String representationFiles();

  String representationDocumentation();

  String representationSchemas();

  String dipCreatedDate();

  String dipLastModified();

  /************* Preservation Event List ****************/

  String preservationEventListHeaderDate();

  String preservationEventListHeaderAgent();

  String preservationEventListHeaderType();

  String preservationEventListHeaderDetail();

  String preservationEventListHeaderSourceObject();

  String preservationEventListHeaderOutcomeObject();

  String preservationEventListHeaderOutcome();

  /*** Preservation events ***/

  String preservationEventsDownloadButton();

  String preservationEventsBackButton();

  String preservationEventsTitle();

  String preservationEventTitle();

  String preservationEventId();

  String preservationEventDatetime();

  String preservationEventOutcome();

  String preservationEventType();

  String preservationEventDetail();

  String preservationEventAgentsHeader();

  String preservationEventAgentRole();

  String preservationEventSourceObjectsHeader();

  String preservationEventOutcomeObjectsHeader();

  String preservationEventObjectId();

  String preservationEventObjectRole();

  String preservationEventOutcomeDetailHeader();

  String preservationEventOutcomeDetailNote();

  String preservationEventOutcomeDetailExtension();

  String descriptiveMetadataHistoryLabel(String versionKey, Date createdDate);

  /************* Select AIP Dialog ****************/

  String selectAipCancelButton();

  String selectAipEmptyParentButton();

  String selectAipSelectButton();

  String selectAipSearchResults();

  String selectAipSearchPlaceHolder();

  String selectTransferredResourcesSearchResults();

  String renameTransferredResourcesDialogTitle();

  String selectRepresentationSearchResults();

  String selectFileSearchResults();

  /************* Rename Item ****************/

  String renameItemTitle();

  String renamePlaceholder();

  String renameFailed();

  String renameSuccessful();

  /************* Move Item ****************/

  String moveItemTitle();

  String moveItemFailed();

  /************* Select Parent ****************/

  String selectParentTitle();

  /*************
   * Lists
   * 
   * @param count
   ****************/

  String listSelectAllMessage(int count);

  /************* User log ****************/

  String userLogSearchPlaceHolder();

  /************* Notification messages ****************/

  String messageSearchPlaceHolder();

  String showMessageAcknowledged();

  String showMessageNotAcknowledged();

  /************* Humanize ****************/

  String durationDHMSShortDays(int days, int hours, int minutes, int seconds);

  String durationDHMSShortHours(int hours, int minutes, int seconds);

  String durationDHMSShortMinutes(int minutes, int seconds);

  String durationDHMSShortSeconds(int seconds);

  String durationDHMSLongDays(int days, int hours, int minutes, int seconds);

  String durationDHMSLongHours(int hours, int minutes, int seconds);

  String durationDHMSLongMinutes(int minutes, int seconds);

  String durationDHMSLongSeconds(int seconds);

  String durationDHMSShortMillis(int millis);

  String objectPermission(@Select PermissionType permissionType);

  String objectPermissionDescription(@Select PermissionType permissionType);

  /************* Ingest appraisal ****************/

  String ingestAppraisalAcceptButton();

  String ingestAppraisalRejectButton();

  String aipState(@Select AIPState state);

  /************* Search pre-filters ****************/

  SafeHtml searchPreFilterSimpleFilterParameter(String searchPreFilterName, String searchPreFilterValue);

  SafeHtml searchPreFilterBasicSearchFilterParameter(String searchPreFilterName, String searchPreFilterValue);

  SafeHtml searchPreFilterNotSimpleFilterParameter(String searchPreFilterName, String searchPreFilterValue);

  SafeHtml searchPreFilterEmptyKeyFilterParameter(String searchPreFilterName);

  String searchPreFilterName(@Select String name);

  String searchPreFilterValue(@Select String name);

  /************* Risk register ****************/

  String showRiskAIPTitle(String aipId);

  String riskRegisterCategory();

  String riskRegisterSeverity();

  String riskRegisterOwner();

  String riskRegisterProcessButton();

  String riskRegisterSearchPlaceHolder();

  String riskIncidenceRegisterSearchPlaceHolder();

  String showObjectsRiskCounter(@PluralCount @Optional int counter);

  String createRiskFailure(String message);

  String editRiskNotFound(String name);

  String editRiskFailure(String message);

  String riskRemoveFolderConfirmDialogTitle();

  String riskRemoveSelectedConfirmDialogMessage(Long size);

  String riskRemoveFolderConfirmDialogCancel();

  String riskRemoveFolderConfirmDialogOk();

  String riskRemoveSuccessTitle();

  String riskRemoveSuccessMessage(Long size);

  String riskHistoryLabel(String versionKey, Date createdDate);

  String severityLevel(@Select SEVERITY_LEVEL level);

  String getRisksDialogName();

  String riskHistoryButton();

  String editIncidenceNotFound(String incidence);

  String editIncidenceFailure(String errorMessage);

  /************* Format register ****************/

  String getFormatsDialogName();

  String formatRegisterTitle();

  String formatRegisterProcessButton();

  String formatRegisterSearchPlaceHolder();

  String createFormatFailure(String message);

  String editFormatNotFound(String name);

  String editFormatFailure(String message);

  String formatRemoveFolderConfirmDialogTitle();

  String formatRemoveSelectedConfirmDialogMessage(Long size);

  String formatRemoveFolderConfirmDialogCancel();

  String formatRemoveFolderConfirmDialogOk();

  String formatRemoveSuccessTitle();

  String formatRemoveSuccessMessage(Long size);

  String formatListItems(String item);

  String formatIsOpenFormat();

  String formatIsNotOpenFormat();

  /************* Common Messages ****************/

  String logParameter(String name, String value);

  // Content Panel
  String pageNotFound(String error);

  String windowTitle(String history);

  /************* User management Messages ****************/

  // Create
  String createUserFailure(String reason);

  String createGroupFailure(String reason);

  String createUserAlreadyExists(String username);

  String createUserEmailAlreadyExists(String email);

  String createGroupAlreadyExists(String groupname);

  // Edit
  String editUserFailure(String username, String reason);

  String editGroupFailure(String groupname, String reason);

  String editUserNotFound(String username);

  String editUserEmailAlreadyExists(String email);

  String editGroupNotFound(String groupname);

  // Remove
  String removeUserConfirm(String username);

  String removeUserFailure(String username, String reason);

  String removeUserNotPossible(String name);

  String removeGroupConfirm(String groupname);

  String removeGroupFailure(String groupname, String reason);

  // User/Group Alphabet Sorted List
  String userCount(int count);

  String groupCount(int count);

  // Action Report Window
  String actionResportTitle(String username);

  // User log
  String userLogEntriesTotal(int total);

  // Member management

  String userStatus();

  String userType();

  String addUserButton();

  String addGroupButton();

  String userIdentifier();

  String userFullName();

  /*** Show job and report ****/

  String jobName();

  String jobCreator();

  String jobStartDate();

  String jobEndDate();

  String jobDuration();

  String jobStatus();

  String jobProgress();

  String jobPlugin();

  String jobProcessed();

  String jobProcessedSearchPlaceHolder();

  String jobList();

  String reportJob();

  String reportDateCreated();

  String reportDateUpdated();

  String reportDuration();

  String reportStatus();

  String reportProgress();

  String reportRunTasks();

  String jobTotalCountMessage();

  String jobSuccessCountMessage();

  String jobFailureCountMessage();

  String jobProcessingCountMessage();

  String jobWaitingCountMessage();

  String showSIPNotExtended();

  String showAIPNotExtended();

  String reportLastUpdatedAt();

  String reportLastRunTask();

  String reportOutcome();

  String reportSource();

  String reportList();

  String reportAgent();

  String reportStartDatetime();

  String reportEndDatetime();

  String reportOutcomeDetails();

  /*** Appraisal ***/

  String ingestAppraisalDescriptionLevels();

  String ingestAppraisalRepresentations();

  String ingestAppraisalFormats();

  String ingestAppraisalPronomIds();

  String ingestAppraisalMimetypes();

  /*** Risks ***/

  String riskIdentifier();

  String riskName();

  String riskDescription();

  String riskIdentifiedOn();

  String riskIdentifiedBy();

  String riskCategory();

  String riskNotes();

  String riskPreMitigation();

  String riskPreMitigationProbability();

  String riskPreMitigationImpact();

  String riskPreMitigationSeverity();

  String riskPreMitigationNotes();

  String riskMitigation();

  String riskMitigationStrategy();

  String riskMitigationOwnerType();

  String riskMitigationOwner();

  String riskMitigationRelatedEventIdentifierType();

  String riskMitigationRelatedEventIdentifierValue();

  String riskPostMitigation();

  String riskPostMitigationProbability();

  String riskPostMitigationImpact();

  String riskPostMitigationSeverity();

  String riskPostMitigationNotes();

  String riskIncidences();

  String riskIncidenceObjectId();

  String riskIncidenceObjectType();

  String riskIncidenceDetectedOn();

  String riskIncidenceDetectedBy();

  String riskMitigationProbability(@Select String probability);

  String riskMitigationImpact(@Select String impact);

  String riskIncidenceIdentifier();

  String riskIncidenceDescription();

  String riskIncidenceRisk();

  String riskIncidenceStatus();

  String riskIncidenceSeverity();

  String riskIncidenceMitigatedOn();

  String riskIncidenceMitigatedBy();

  String riskIncidenceMitigatedDescription();

  String riskIncidenceStatusValue(@Select INCIDENCE_STATUS status);

  /*** Formats ***/

  String formatIdentifier();

  String formatName();

  String formatDefinition();

  String formatCategory();

  String formatLatestVersion();

  String formatPopularity();

  String formatDeveloper();

  String formatInitialRelease();

  String formatStandard();

  String formatOpen();

  String formatWebsite();

  String formatProvenanceInformation();

  String formatExtensions();

  String formatMimetypes();

  String formatMimetype();

  String formatPronoms();

  String formatPronom();

  String formatUtis();

  /****** Descriptive Metadata ****/

  String metadataType();

  String metadataFilename();

  String metadataContent();

  /*** Browse ****/

  String sublevels();

  String appraisalTitle();

  String appraisalAccept();

  String appraisalReject();

  String representationsTitle();

  String filesTitle();

  String transferredResourcesTitle();

  String archivalPackageTitle();

  String newArchivalPackage();

  String moveArchivalPackage();

  String archivalPackagePermissions();

  String archivalPackagePermissionsTitle();

  String removeArchivalPackage();

  String addRepresentation();

  String preservationTitle();

  String newProcessPreservation();

  String preservationEvents();

  String preservationRisks();

  String preservationLogs();

  String downloadTitle();

  String downloadArchivalPackage();

  String downloadSubmission();

  String downloadDocumentation();

  String downloadSchemas();

  String addPermission();

  String permissionAssignedGroups();

  String permissionAssignedUsers();

  String permissionAssignedGroupsEmpty();

  String permissionAssignedUsersEmpty();

  String listOfItems();

  String unknownAncestorError();

  String selectAnItemTitle();

  String selectAnItemToRemoveDescription();

  String selectAnItemToMoveDescription();

  String searchContext();

  String searchAIP();

  /***** Representation ****/

  String representation();

  String representationListOfFiles();

  String representationRemoveTitle();

  String representationRemoveMessage();

  /***** File ****/

  String fileRemoveTitle();

  String fileRemoveMessage();

  String folderRemoveMessage();

  String filesRemoveTitle();

  String selectedFileRemoveMessage();

  /***** Job processes ****/

  String processJobTypes();

  String processStatus();

  String processCreators();

  /*** Login ***/

  String loginUsername();

  String loginPassword();

  String loginRecoverPassword();

  String fillUsernameAndPasswordMessage();

  String wrongUsernameAndPasswordMessage();

  String emailUnverifiedMessage();

  String inactiveUserMessage();

  String systemCurrentlyUnavailableMessage();

  String loginResendEmail();

  String loginResendEmailSuccessDialogTitle();

  String loginResendEmailSuccessDialogMessage();

  String loginResendEmailSuccessDialogButton();

  String loginResendEmailFailureDialogTitle();

  String loginResendEmailFailureDialogMessage();

  String loginResendEmailFailureDialogButton();

  String loginResendEmailVerificationFailure();

  /*** More Search ***/

  String searchDescriptionLevels();

  String searchRepresentations();

  String searchFormats();

  String searchPronomIds();

  String searchMimetypes();

  String searchDuplicateWarningMessage(String field);

  /**** Activity log ****/

  String logComponents();

  String logMethods();

  String logUsers();

  String logEntryIdentifier();

  String logEntryComponent();

  String logEntryMethod();

  String logEntryAddress();

  String logEntryState();

  String logEntryDatetime();

  String logEntryDatetimeExtended();

  String logEntryDuration();

  String logEntryRelatedObject();

  String logEntryUsername();

  String logEntryUser();

  String logEntryParameters();

  String logEntryStateValue(@Select LOG_ENTRY_STATE state);

  /*** Notifications ***/

  String notificationRecipients();

  String notificationAcknowledged();

  String notificationAck();

  String notificationIdentifier();

  String notificationSubject();

  String notificationBody();

  String notificationSentOn();

  String notificationFrom();

  String notificationFromUser();

  String notificationIsAcknowledged();

  String notificationAcknowledgedUsers();

  String notificationNotAcknowledgedUsers();

  String notificationState();

  /*** Browse Constants **/

  /** Advanced Search Constants **/

  String january();

  String february();

  String march();

  String april();

  String may();

  String june();

  String july();

  String august();

  String september();

  String october();

  String november();

  String december();

  /** Common Constants **/

  // Alphabet Sorted List

  String AlphabetSortedListAll();

  // User Info Panel

  String userInfoDetails();

  String userInfoFullname();

  String userInfoBusinessCategory();

  String userInfoOrganization();

  String userInfoEmail();

  String userInfoTelephoneNumber();

  String userInfoFax();

  String userInfoPostalAddress();

  String userInfoPostalCode();

  String userInfoLocality();

  String userInfoCountry();

  // Logger

  String alertErrorTitle();

  // Report Window

  String reportWindowClose();

  String reportWindowPrintPDF();

  String reportWindowPrintCSV();

  // ID Type

  String simpleID();

  String fullID();

  // Redaction Type

  String input();

  String output();

  String focusPanelTitle();

  /** Main constants **/

  // Content titles

  String title(@Select String title);

  // Login Panel

  String loginLogin();

  String loginRegister();

  String loginProfile();

  String loginLogout();

  // Login Dialog

  String loginDialogTitle();

  String loginDialogLogin();

  String loginDialogCancel();

  // Home

  String homeTitle();

  // Content Panel

  String authorizationDeniedAlert();

  String authorizationDeniedAlertMessageMissingRoles(List<String> missingRoles);

  String authorizationDeniedAlertMessageException(String message);

  String casForwardWarning();

  // Cookies

  String cookiesMessage();

  String cookiesDismisse();

  String cookiesLearnMore();

  /** Metadata editor constants **/

  // Move element

  String moveChooseDestinationTitle();

  String moveChooseDestinationChoose();

  String moveChooseDestinationCancel();

  String moveSuccessful();

  // Edit Producers Panel

  String editProducersTitle();

  String editProducersAddUser();

  String editProducersAddGroup();

  String editProducersDelete();

  String editProducersRodaIn();

  // Edit Object Permissions Panel

  String objectPermissionsApplyRecursivelly();

  String objectPermissionsAddUser();

  String objectPermissionsAddGroup();

  String objectPermissionsSave();

  // Meta permissions

  String permission_object_NoAccess();

  String permission_object_ReadOnly();

  String permission_object_ReadAndEditMetadata();

  String permission_object_FullControl();

  /** User management constants **/

  // Create/Edit User/Group
  String dataTabTitle();

  String permissionsTabTitle();

  // Control Panel
  String list();

  String users();

  String groups();

  String search();

  String userActions();

  String report();

  String createUser();

  String editUser();

  String removeUser();

  String setActive();

  String setInactive();

  String groupActions();

  String createGroup();

  String editGroup();

  String removeGroup();

  String actions();

  // Select User/Group
  String selectNoUser();

  String selectNoGroup();

  String selectNoActiveUser();

  String selectNoInactiveUser();

  String selectNoUserOrGroup();

  // Create User
  String createUserTitle();

  String createUserCancel();

  String createUserCreate();

  // Edit User
  String editUserTitle();

  String editUserCancel();

  String editUserRemove();

  String editUserActivate();

  String editUserDeactivate();

  String editUserApply();

  // User Data Panel
  String username();

  String password();

  String passwordNote();

  String userDataChangePassword();

  String fullname();

  String jobFunction();

  String getJobFunctions(@Select int index);

  String idTypeAndNumber();

  String id_type(@Select String item);

  String idDateAndLocality();

  String nationality();

  String address();

  String postalCodeAndLocality();

  String country();

  String nif();

  String email();

  String phonenumber();

  String fax();

  String extra();

  String userDataNote();

  String allGroups();

  String memberGroups();

  String userGroups();

  String userPermissions();

  // Create Group
  String createGroupTitle();

  String createGroupCreate();

  String createGroupCancel();

  // Edit Group
  String editGroupTitle();

  String editGroupApply();

  String editGroupRemove();

  String editGroupCancel();

  // Group data panel
  String groupName();

  String groupFullname();

  String groupUsers();

  String groupDataNote();

  String groupPermissions();

  // Preferences
  String preferencesUserDataTitle();

  String preferencesSubmit();

  String preferencesCancel();

  String preferencesEmailAlreadyExists();

  String preferencesSubmitSuccess();

  // Roles Description

  String role(@Select String role);

  // Action Report Window
  String actionReportClose();

  String actionReportLogTabTitle();

  // User Log
  String actionReportLogDateTime();

  String actionReportLogAction();

  String actionReportLogParameters();

  String actionReportLogUser();

  String userlog_initialDate();

  String userlog_finalDate();

  String userlog_setFilter();

  String userlog_actions();

  String userlog_allActions();

  // Register
  String registerUserDataTitle();

  String registerDisclaimer();

  String registerCaptchaTitle();

  String registerSubmit();

  String registerCancel();

  String registerUserExists();

  String registerEmailAlreadyExists();

  String registerWrongCaptcha();

  String registerFailure();

  String registerSendEmailVerificationFailure();

  String registerSuccessDialogTitle();

  String registerSuccessDialogMessage();

  String registerSuccessDialogMessageActive();

  String registerSuccessDialogButton();

  // Verify Email
  String verifyEmailTitle();

  String verifyEmailUsername();

  String verifyEmailToken();

  String verifyEmailVerify();

  String verifyEmailResend();

  String verifyEmailChange();

  String verifyEmailSubmit();

  String verifyEmailCancel();

  String verifyEmailNoSuchUser();

  String verifyEmailWrongToken();

  String verifyEmailResendSuccess();

  String verifyEmailResendFailure();

  String verifyEmailChangePrompt();

  String verifyEmailChangeFailure();

  String verifyEmailAlreadyExists();

  String verifyEmailChangeSuccess();

  String verifyEmailFailure();

  String verifyEmailSuccessDialogTitle();

  String verifyEmailSuccessDialogMessage();

  String verifyEmailSuccessDialogButton();

  // Recover Login
  String recoverLoginTitle();

  String recoverLoginUsernameOrEmail();

  String recoverLoginCaptchaTitle();

  String recoverLoginSubmit();

  String recoverLoginCancel();

  String recoverLoginCaptchaFailed();

  String recoverLoginNoSuchUser();

  String recoverLoginFailure();

  String recoverLoginSuccessDialogTitle();

  String recoverLoginSuccessDialogMessage();

  String recoverLoginSuccessDialogButton();

  // Reset Password
  String resetPasswordTitle();

  String resetPasswordUsername();

  String resetPasswordToken();

  String resetPasswordNewPassword();

  String resetPasswordRepeatPassword();

  String resetPasswordSubmit();

  String resetPasswordCancel();

  String resetPasswordInvalidToken();

  String resetPasswordNoSuchUser();

  String resetPasswordFailure();

  String resetPasswordSuccessDialogTitle();

  String resetPasswordSuccessDialogMessage();

  String resetPasswordSuccessDialogButton();

  // Select User Window
  String selectUserWindowTitle();

  String selectUserWindowSelect();

  String selectUserWindowCancel();

  // Select Group Window
  String selectGroupWindowTitle();

  String selectGroupWindowSelect();

  String selectGroupWindowCancel();

  /**** Statistics Constants ***/

  String timeSegmentationLabel();

  // segmentation
  String segmentation_YEAR();

  String segmentation_MONTH();

  String segmentation_DAY();

  // statistic type classes

  String systemStatistics();

  String repositoryStatistics();

  String userStatistics();

  String userActionsStatistics();

  String eventStatistics();

  String ingestStatistics();

  String accessStatistics();

  String actionsStatistics();

  String producersStatistics();

  String chartActual();

  String chartHistory();

  String chartPieTooltip();

  String noDataAvailable();

  String viewImpossibleBcSipNotIngested();

  String dateIntervalPickerWindowTitle();

  String dateIntervalLabelInitial();

  String dateIntervalLabelFinal();

  String dateIntervalPickerWindowApply();

  // Statistic panel titles

  String descriptiveObjectsCountTitle();

  String descriptiveObjectsCountDesc();

  String fondsCountTitle();

  String fondsCountDesc();

  String presEventCountTitle();

  String presEventCountDesc();

  String presRepCountTitle();

  String presRepCountDesc();

  String representationObjectsCountTitle();

  String representationObjectsCountDesc();

  String representationObjectTypeTitle();

  String representationObjectTypeDesc();

  String representationObjectSubTypeTitle();

  String representationObjectSubTypeDesc();

  String userCountTitle();

  String userCountDesc();

  String groupCountTitle();

  String groupCountDesc();

  String groupTop5Title();

  String groupTop5Desc();

  String logWuiLoginTitle();

  String logWuiLoginDesc();

  String logWuiPageHitsTitle();

  String logWuiPageHitsDesc();

  String logWuiErrorsTitle();

  String logWuiErrorsDesc();

  String logDescriptiveMetadataViewsTitle();

  String logDescriptiveMetadataViewsDesc();

  String logPreservationMetadataViewsTitle();

  String logPreservationMetadataViewsDesc();

  String logBasicSearchTitle();

  String logBasicSearchDesc();

  String logAdvancedSearchTitle();

  String logAdvancedSearchDesc();

  String disseminatorHitsTitle();

  String disseminatorHitsDesc();

  String disseminatorMissTitle();

  String disseminatorMissDesc();

  String logRegisterUserTitle();

  String logRegisterUserDesc();

  String logUserEmailConfirmationTitle();

  String logUserEmailConfirmationDesc();

  String logUserPasswordResetTitle();

  String logUserPasswordResetDesc();

  String taskCountTitle();

  String taskCountDesc();

  String taskStateTitle();

  String taskStateDesc();

  String taskInstanceCountTitle();

  String taskInstanceCountDesc();

  String taskInstanceStateTitle();

  String taskInstanceStateDesc();

  String sipCountTitle();

  String sipCountDesc();

  String sipCompletenessTitle();

  String sipCompletenessDesc();

  String sipStateTitle();

  String sipStateDesc();

  String sipDurationAutoTitle();

  String sipDurationAutoDesc();

  String sipDurationManualTitle();

  String sipDurationManualDesc();

  String sipMinAutomaticProcessingTimeTitle();

  String sipMinAutomaticProcessingTimeDesc();

  String sipMaxAutomaticProcessingTimeTitle();

  String sipMaxAutomaticProcessingTimeDesc();

  String sipMinManualProcessingTimeTitle();

  String sipMinManualProcessingTimeDesc();

  String sipMaxManualProcessingTimeTitle();

  String sipMaxManualProcessingTimeDesc();

  String producerTitle();

  String producerLastSubmissionDate();

  String producerSubmissionStateChartTitle();

  String producerSubmissionStateChartDesc();

  String createDescriptionObjectTitle();

  String createDescriptionObjectDesc();

  String modifyDescriptionObjectTitle();

  String modifyDescriptionObjectDesc();

  String removeDescriptionObjectTitle();

  String removeDescriptionObjectDesc();

  String moveDescriptionObjectTitle();

  String moveDescriptionObjectDesc();

  String addUserTitle();

  String addUserDesc();

  String modifyUserTitle();

  String modifyUserDesc();

  String removeUserTitle();

  String removeUserDesc();

  String addGroupTitle();

  String addGroupDesc();

  String modifyGroupTitle();

  String modifyGroupDesc();

  String removeGroupTitle();

  String removeGroupDesc();

  String setUserPasswordTitle();

  String setUserPasswordDesc();

  String acceptSIPTitle();

  String acceptSIPDesc();

  String addTaskTitle();

  String addTaskDesc();

  String modifyTaskTitle();

  String modifyTaskDesc();

  String removeTaskTitle();

  String removeTaskDesc();

  String systemStatisticsLink();

  // Reports
  String statisticsReportClose();

  String statisticsReportSegmentationLabel();

  String statisticsReportDateSeparatorLabel();

  String statisticsReportChart();

  String statisticsReportList();

  String statisticsReportListTotal();

  String statisticsReportListHeaderDate();

  String statisticsReportListHeaderType();

  String statisticsReportListHeaderValue();

  // Statistic types
  String statistic_type_others();

  String tasks_state_running();

  String tasks_state_suspended();

  String instances_state_running();

  String instances_state_paused();

  String instances_state_stopped();

  String sips_complete_true();

  String sips_complete_false();

  String sips_state_DROPED_FTP();

  String sips_state_DROPED_UPLOAD_SERVICE();

  String sips_state_DROPED_LOCAL();

  String sips_state_UNPACKED();

  String sips_state_VIRUS_FREE();

  String sips_state_SIP_VALID();

  String sips_state_AUTHORIZED();

  String sips_state_SIP_INGESTED();

  String sips_state_SIP_NORMALIZED();

  String sips_state_ACCEPTED();

  String sips_state_QUARANTINE();

  String object_representation_type_digitalized_work();

  String object_representation_type_email();

  String object_representation_type_structured_text();

  String object_representation_type_presentation();

  String object_representation_type_spreadsheet();

  String object_representation_type_vector_graphic();

  String object_representation_type_relational_database();

  String object_representation_type_video();

  String object_representation_type_audio();

  String object_representation_type_unknown();

  String users_state_active();

  String users_state_inactive();

  /************* Metadata creation and edition ****************/

  String confirmChangeToFormTitle();

  String confirmChangeToFormMessage();

  String editDescriptiveMetadataTitle();

  String editDescriptionMetadataWarning();

  String editDescriptiveMetadataFormLabel();

  /************* TO BE ORGANIZED *************************/

  String aipLevel();

  String pluginStateMessage(@Select PluginState state);

  String notificationStateValue(@Select NOTIFICATION_STATE state);

  String isAcknowledged(@Select String isAcknowledged);

  String preservationEventAgentIdentifier();

  String preservationEventAgentType();

  String preservationEventAgentVersion();

  String preservationEventAgentNote();

  String preservationEventAgentRoles();

  String preservationEventAgentExtension();

  String mandatoryField();

  String isAMandatoryField(String field);

  String isNotValid(String field);

  String wrongMailFormat();

  String fileAlreadyExists();

  SafeHtml tableDownloadCSV();

  String showMore();

  String showLess();

  String revertedBy(String userId);

  /* Version action */
  String versionAction(@Select String action);

  String versionActionBy(String message, String string);
}
