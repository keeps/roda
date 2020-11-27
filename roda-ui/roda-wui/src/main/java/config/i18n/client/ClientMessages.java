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
import org.roda.core.data.v2.ip.disposal.DisposalConfirmationState;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.notifications.NotificationState;
import org.roda.core.data.v2.risks.IncidenceStatus;
import org.roda.core.data.v2.risks.SeverityLevel;
import org.roda.wui.client.common.actions.model.ActionableObject;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.safehtml.shared.SafeHtml;

/**
 * @author Luis Faria
 * 
 */
public interface ClientMessages extends Messages {

  String moveNoSuchObject(String message);

  String and();

  String of();

  String ofOver();

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

  String createRepresentationInformationTitle();

  String editRepresentationInformationTitle();

  String showRepresentationInformationTitle();

  String representationInformationTitle();

  String newRiskTitle();

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

  String catalogueDIPTitle();

  String preservationActionsTitle();

  String ingestProcessTitle();

  String internalProcessTitle();

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

  String changeStatusTitle();

  String changeStatusSuccessful();

  String statusLabel(@Select String status);

  String otherStatusLabel();

  String otherStatusPlaceholder();

  String createFolderTitle();

  String createFolderPlaceholder();

  String createFolderAlreadyExistsTitle();

  String createFolderAlreadyExistsMessage();

  String outcomeDetailTitle();

  String outcomeDetailPlaceholder();

  String chooseEntityTitle();

  /******* SIDEBAR ******************************************/

  String sidebarFilterDates();

  String sidebarFilterFromDatePlaceHolder();

  String sidebarFilterToDatePlaceHolder();

  String sidebarFoldersFilesTitle();

  String sidebarProcessTitle();

  String sidebarIngestTitle();

  String sidebarActionsTitle();

  String sidebarDisposalBinTitle();

  String sidebarDisposalScheduleTitle();

  String sidebarDisposalConfirmationReportTitle();

  String sidebarDisposalConfirmationTitle();

  String sidebarRepresentationInformationTitle();

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

  SafeHtml descriptiveMetadataTransformToHTMLError();

  SafeHtml preservationEventDetailsTransformToHTMLError();

  String notFoundError();

  String allCollectionsTitle();

  String errorLoadingDescriptiveMetadata(String message);

  String errorLoadingPreservationEventDetails(String message);

  String titleDatesEmpty();

  String titleDatesNoFinal(String dateInitial);

  String titleDatesNoInitial(String dateFinal);

  String titleDates(String dateInitial, String dateFinal);

  String simpleDatesEmpty();

  String simpleDatesNoFinal(String dateInitial);

  String simpleDatesNoInitial(String dateFinal);

  String simpleDates(String dateInitial, String dateFinal);

  String itemWasAccepted();

  String itemWasRejected();

  String rejectMessage();

  String rejectQuestion();

  String renameSIPFailed();

  String renameSIPSuccessful();

  String moveSIPFailed();

  String movingAIP();

  String changeTypeSuccessful();

  String jobNotFound();

  String updateIsCurrentlyRunning();

  String updatedFilesUnderFolder();

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

  String permissionsWillBeChanged();

  String couldNotFindPreservationEvent();

  String couldNotFindPreservationAgent();

  String intellectualEntity();

  String inspectIntellectualEntity();

  String inspectRepresentation();

  String inspectFile();

  String inspectPreservationAgent();

  String allIntellectualEntities();

  String allRepresentations();

  String allFiles();

  String allOfAObject(@Select String objectClass);

  String someOfAObject(@Select String objectClass);

  String oneOfAObject(@Select String objectClass);

  String selected(@PluralCount int selectedCount, String entity);

  String inspectTransferredResource();

  String identifierNotFound();

  String originalRepresentation();

  String alternativeRepresentation();

  String representationStatus();

  String executingTaskMessage();

  String noItemsToDisplay(String itemLabel);

  String noItemsToDisplayPreFilters(String itemLabel);

  String noItemsToDisplayButFacetsActive(String itemLabel);

  String resetFacetsLink();

  String disableFacets();

  String browserOfflineError();

  String cannotReachServerError();

  /****** INGEST TRANSFER **********/

  String ingestTransferTitle();

  String ingestTransferList();

  String ingestTransferSearchPlaceHolder();

  String ingestTransferItemInfo(String creationDate, String readableFileSize);

  String ingestTransferRemoveFolderConfirmDialogTitle();

  String ingestTransferRemoveFolderConfirmDialogMessage(String folderName);

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

  String createJobSelectedSIP();

  String createJobCreateTitle();

  String createJobObtainCommandTitle();

  String createJobCancelTitle();

  String createJobWorkflow();

  String createJobCategorySelect();

  SafeHtml createJobCategoryWorkflow(List<String> categoryLabels);

  String createJobCurlCommand();

  String copiedToClipboardTitle();

  String copiedToClipboardMessage();

  String jobCreatedRedirectTitle();

  String removeJobCreatedMessage();

  String identifyFormatsJobCreatedMessage();

  String moveJobCreatedMessage();

  String jobCreatedMessage();

  /****** INGEST TRANSFER UPLOAD *********/

  String ingestTransferUploadTitle();

  String fileUploadTitle();

  String ingestTransferUploadDropHere();

  String ingestTransferUploadBrowseFiles();

  String ingestTransferUploadFinishButton();

  String uploadDoneMessage();

  /******** DIALOG GENERIC **************/

  String dialogCancel();

  String dialogOk();

  String dialogNo();

  String dialogYes();

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

  String newRepresentationButton();

  String editButton();

  String liftButton();

  String deactivateButton();

  String nextButton();

  String saveButton();

  String addButton();

  String confirmButton();

  String applyButton();

  String overrideManualAppliedSchedulesButton();

  String applyDisposalRulesButton();

  String applyAllButton();

  String stopButton();

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

  String changeStatusButton();

  String clearButton();

  String selectAllButton();

  String closeButton();

  String selectButton();

  String inspectButton();

  String printButton();

  /*** Identify formats ***/

  String identifyFormatsButton();

  String identifyingFormatsTitle();

  String identifyingFormatsDescription();

  /*** View representation ***/

  String viewRepresentationDownloadFileButton();

  String viewRepresentationInfoFileButton();

  String viewRepresentationErrorPreview();

  String viewRepresentationTooLargeErrorPreview();

  String viewRepresentationNotSupportedPreview();

  String viewRepresentationInfoTitle();

  String viewRepresentationFileDisseminationTitle();

  String viewRepresentationInfoFilename();

  String viewRepresentationInfoSize();

  String viewRepresentationInfoExtension();

  String viewRepresentationInfoMimetype();

  String viewRepresentationInfoFormat();

  String viewRepresentationInfoPronom();

  String viewRepresentationInfoCreatingApplicationName();

  String viewRepresentationInfoCreatingApplicationVersion();

  String viewRepresentationInfoDateCreatedByApplication();

  String viewRepresentationInfoHash();

  String viewRepresentationInfoStoragePath();

  String viewRepresentationRemoveFileTitle();

  String viewRepresentationRemoveFileMessage();

  /************* Process new ****************/

  String processNewDefaultName(Date date);

  String pluginLabel(String name);

  String pluginLabelWithVersion(String name, String version);

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

  String showJobStatusToBeCleaned();

  SafeHtml showJobProgressCompletionPercentage(int completionPercentage);

  SafeHtml showJobProgressTotalCount(int objectsCount);

  SafeHtml showJobProgressSuccessfulCount(int objectsProcessedWithSuccess);

  SafeHtml showJobProgressPartialSuccessfulCount(int objectsProcessedWithSuccess);

  SafeHtml showJobProgressSkippedCount(int objectsProcessedWithSkipped);

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

  String removeSuccessMessage(Long size);

  String removeConfirmDialogTitle();

  String removeAllConfirmDialogMessageSingle(String aipTitle);

  String removeSelectedConfirmDialogMessage(Long size);

  String removingSuccessTitle();

  String removingSuccessMessage(Long size);

  String moveConfirmDialogTitle();

  String moveAllConfirmDialogMessageSingle(String aipTitle);

  String moveSelectedConfirmDialogMessage(Long size);

  /************* Browse ****************/
  String browseLoading();

  String itemId();

  String sipId();

  String processId();

  String updateProcessId();

  String processIdTitle();

  String updateProcessIdTitle();

  String itemIdMin(String itemId);

  String sipIdMin(String sipId);

  String aipCreated();

  String aipUpdated();

  String dateCreated(String date, String person);

  String dateUpdated(String date, String person);

  String dateCreatedOrUpdated(String date, String person);

  String dateCreatedAndUpdated(String createdDate, String createdPerson, String updatedDate, String updatedPerson);

  String aipType();

  String aipTypeItem();

  String aipLevelItem();

  /************* Search ****************/
  String searchDropdownLabels(@Select String objectClass);

  String searchListBoxItems();

  String searchListBoxRepresentations();

  String searchListBoxFiles();

  String searchListBoxJobs();

  String searchListBoxJobReports();

  String searchListBoxRisks();

  String searchListBoxIncidences();

  String searchListBoxPreservationEvents();

  String searchListBoxActionLogs();

  String searchListBoxNotifications();

  String searchListBoxTransferredResources();

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

  String aipRiskIncidences(Long amount);

  String aipEvents(Long amount);

  String aipLogs(Long amount);

  String fileId();

  String fileName();

  String filePath();

  String fileFormat();

  String fileMimetype();

  String filePronom();

  String fileSize();

  String transferredResourceName();

  String transferredResourcePath();

  String transferredResourceSize();

  String transferredResourceDateCreated();

  String numberOfFiles(long countFiles, long countFolders);

  String representationId();

  String representationOriginal();

  String representationType();

  String representationSize();

  String representationFiles();

  String objectCreatedDate();

  String objectLastModified();

  /************* Preservation Event List ****************/

  String preservationEventListHeaderDate();

  String preservationEventListHeaderType();

  String preservationEventListHeaderDetail();

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

  String preservationEventSourceObjectsHeader();

  String preservationEventOutcomeObjectsHeader();

  String preservationEventOutcomeDetailHeader();

  String preservationEventClass();

  String descriptiveMetadataHistoryLabel(String versionKey, Date createdDate);

  /************* Preservation Agent List ****************/

  String preservationAgentsTitle();

  String preservationAgentTitle();

  String preservationAgentName();

  String preservationAgentType();

  String preservationAgentVersion();

  String preservationAgentId();

  String preservationAgentNote();

  String preservationAgentExtension();

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

  String renameSuccessful();

  /************* Move Item ****************/

  String moveItemTitle();

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

  String appraisalNoItemsSelectedTitle();

  String appraisalNoItemsSelectedMessage();

  String aipState(@Select AIPState state);

  /************* Search pre-filters ****************/

  SafeHtml searchPreFilterSimpleFilterParameter(String searchPreFilterName, String searchPreFilterValue);

  SafeHtml searchPreFilterBasicSearchFilterParameter(String searchPreFilterName, String searchPreFilterValue);

  SafeHtml searchPreFilterNotSimpleFilterParameter(String searchPreFilterName, String searchPreFilterValue);

  SafeHtml searchPreFilterEmptyKeyFilterParameter(String searchPreFilterName);

  SafeHtml searchPreFilterLongRangeFilterParameter(String searchPreFilterName, long searchPreFilterFromValue,
    long searchPreFilterToValue);

  SafeHtml searchPreFilterLongRangeFilterParameterGreaterThan(String searchPreFilterName,
    long searchPreFilterFromValue);

  SafeHtml searchPreFilterLongRangeFilterParameterSmallerThan(String searchPreFilterName, long searchPreFilterToValue);

  SafeHtml searchPreFilterOneOfManyFilterParameterSingle(String searchPreFilterName, String item);

  SafeHtml searchPreFilterOneOfManyFilterParameterWithSize(String searchPreFilterName, int listSize);

  SafeHtml searchPreFilterOneOfManyFilterParameterWithList(String searchPreFilterName, List<String> list);

  String searchPreFilterName(@Select String name);

  String searchPreFilterValue(@Select String name);

  SafeHtml searchPreFilterOr();

  SafeHtml searchPreFilterAnd();

  SafeHtml searchPreFilterWhere();

  /************* Risk register ****************/

  String riskRegisterCategory();

  String riskRegisterSeverity();

  String riskRegisterOwner();

  String riskRegisterProcessButton();

  String riskRegisterSearchPlaceHolder();

  String riskIncidenceRegisterSearchPlaceHolder();

  String editRiskNotFound(String name);

  String riskRemoveConfirmDialogTitle();

  String riskRemoveSelectedConfirmDialogMessage(Long size);

  String riskRemoveConfirmDialogCancel();

  String riskRemoveConfirmDialogOk();

  String riskRemoveSuccessTitle();

  String riskRemoveSuccessMessage(Long size);

  String riskHistoryLabel(String versionKey, Date createdDate);

  String severityLevel(@Select SeverityLevel level);

  String getRisksDialogName();

  String riskHistoryButton();

  String editIncidenceNotFound(String incidence);

  String editIncidenceFailure(String errorMessage);

  String riskIncidenceRemoveConfirmDialogTitle();

  String riskIncidenceRemoveSelectedConfirmDialogMessage(Long size);

  String riskIncidenceRemoveConfirmDialogCancel();

  String riskIncidenceRemoveConfirmDialogOk();

  String riskIncidenceRemoveSuccessTitle();

  String riskIncidenceRemoveSuccessMessage(Long size);

  /************* RepresentationInformation register ****************/

  String representationInformationRegisterTitle();

  String representationInformationRegisterProcessButton();

  String representationInformationRegisterSearchPlaceHolder();

  String editRepresentationInformationNotFound(String name);

  String representationInformationRemoveFolderConfirmDialogTitle();

  String representationInformationRemoveSelectedConfirmDialogMessage(Long size);

  String representationInformationRemoveFolderConfirmDialogCancel();

  String representationInformationRemoveFolderConfirmDialogOk();

  String representationInformationRemoveSuccessTitle();

  String representationInformationRemoveSuccessMessage(Long size);

  String representationInformationListItems(String item);

  String representationInformationAdditionalInformation();

  String representationInformationMissingFieldsTitle();

  String representationInformationMissingFields();

  String noTitleMessage();

  String currentRelationResults();

  String createNewRepresentationInformation();

  String addToExistingRepresentationInformation();

  String atLeastOneOfAbove();

  /************* Common Messages ****************/

  String logParameter(String name, String value);

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

  String userRemoveConfirmDialogTitle();

  String userRemoveConfirmDialogMessage();

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

  String jobStateDetails();

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

  String reportFailed();

  String jobTotalCountMessage();

  String jobSuccessCountMessage();

  String jobPartialSuccessCountMessage();

  String jobSkippedCountMessage();

  String jobFailureCountMessage();

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

  String riskCategories();

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

  String riskNotMitigatedIncidences();

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

  String riskIncidenceStatusValue(@Select IncidenceStatus status);

  String riskIncidenceStatusTitle();

  /*** Representation information ***/

  String representationInformationIdentifier();

  String representationInformationName();

  String representationInformationDescription();

  String representationInformationFamily();

  String representationInformationTags();

  String representationInformationSupport();

  String representationInformationSupportValue(@Select String value);

  String representationInformationRelationType();

  String representationInformationRelationObjectType(@Select String type);

  String representationInformationRelationLink();

  String representationInformationRelationTitle();

  String representationInformationAddNewRelation();

  String representationInformationEditAssociations();

  String representationInformationIntellectualEntities(@PluralCount int size, String link);

  String representationInformationRepresentations(@PluralCount int size, String link);

  String representationInformationFiles(@PluralCount int size, String link);

  /****** Descriptive Metadata ****/

  String metadataType();

  String metadataFilename();

  /*** Browse ****/

  String sublevels();

  String appraisalTitle();

  String appraisalAccept();

  String appraisalReject();

  String representationsTitle();

  String disseminationsTitle();

  String filesTitle();

  String transferredResourcesTitle();

  String newArchivalPackage();

  String newSublevel();

  String moveArchivalPackage();

  String archivalPackagePermissions();

  String archivalPackagePermissionsTitle();

  String disseminationPermissions();

  String removeArchivalPackage();

  String preservationTitle();

  String newProcessPreservation();

  String preservationEvents();

  String preservationRisks();

  String preservationLogs();

  String downloadDocumentation();

  String downloadNoDocumentationTitle();

  String downloadNoDocumentationDescription();

  String downloadSubmissions();

  String downloadNoSubmissionsTitle();

  String downloadNoSubmissionsDescription();

  String addPermission();

  String permissionAssignedGroups();

  String permissionAssignedUsers();

  String permissionAssignedGroupsEmpty();

  String permissionAssignedUsersEmpty();

  String listOfAIPs();

  String listOfRepresentations();

  String listOfDisseminations();

  String unknownAncestorError();

  String searchPrevious();

  String searchNext();

  String searchContext();

  String searchAIP();

  String aipPermissionDetails();

  /***** Representation ****/

  String representation();

  String representationListOfFiles();

  String representationRemoveTitle();

  String representationRemoveMessage();

  String entityTypeAddNew();

  String entityTypeNewLabel();

  /***** File ****/

  String file();

  String folder();

  String filesRemoveTitle();

  String selectedFileRemoveMessage();

  /***** Dissemination ****/
  String dissemination();

  /***** Dissemination File ****/

  String disseminationFile();

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

  String searchRepresentationType();

  String searchRepresentationOriginal();

  String searchRepresentationStates();

  String searchPronomIds();

  String searchMimetypes();

  String searchFileType();

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

  String logEntryDate();

  String logEntryDatetime();

  String logEntryDuration();

  String logEntryRelatedObject();

  String logEntryUsername();

  String logEntryUser();

  String logEntryParameters();

  String logEntryStateValue(@Select LogEntryState state);

  String logEntryInstanceId();

  /*** Notifications ***/

  String notificationRecipients();

  String notificationAcknowledged();

  String notificationAck();

  String notificationIdentifier();

  String notificationSubject();

  String notificationBody();

  String notificationSentOn();

  String notificationTo();

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

  // Logger

  String alertErrorTitle();

  // Redaction Type

  String input();

  String output();

  /** Main constants **/

  // Content titles

  String title(@Select String title);

  // Login Panel

  String loginLogin();

  String loginRegister();

  String loginProfile();

  String loginLogout();

  SafeHtml loggedIn(String username);

  String welcomePage();

  // Login Dialog

  String loginDialogTitle();

  String loginDialogLogin();

  String loginDialogCancel();

  // Home

  String homeTitle();

  // Content Panel

  String authorizationDeniedAlert();

  SafeHtml authorizationDeniedAlertMessageMissingRoles(List<String> missingRoles);

  SafeHtml authorizationDeniedAlertMessageExceptionSimple(String message);

  String casForwardWarning();

  // Cookies

  String cookiesMessage();

  String cookiesDismisse();

  String cookiesLearnMore();

  // Control Panel
  String list();

  String users();

  String groups();

  String search();

  String report();

  String createUser();

  String createGroup();

  String actions();

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

  String passwordConfirmation();

  String passwordNote();

  String userDataChangePassword();

  String fullname();

  String address();

  String country();

  String email();

  String fax();

  String extra();

  String userDataNote();

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

  // Roles Description

  String role(@Select String role);

  // Register
  String registerUserDataTitle();

  String registerSubmit();

  String registerCancel();

  String registerUserExists();

  String registerEmailAlreadyExists();

  String registerWrongCaptcha();

  String registerFailure();

  String registerSuccessDialogTitle();

  String registerSuccessDialogMessage();

  String registerSuccessDialogMessageActive();

  String registerSuccessDialogButton();

  // Verify Email
  String verifyEmailTitle();

  String verifyEmailUsername();

  String verifyEmailToken();

  String verifyEmailSubmit();

  String verifyEmailCancel();

  String verifyEmailNoSuchUser();

  String verifyEmailWrongToken();

  String verifyEmailFailure();

  String verifyEmailSuccessDialogTitle();

  String verifyEmailSuccessDialogMessage();

  String verifyEmailSuccessDialogButton();

  // Recover Login
  String recoverLoginTitle();

  String recoverLoginUsernameOrEmail();

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

  // statistic type classes

  String dateIntervalLabelInitial();

  String dateIntervalLabelFinal();

  /************* Metadata creation and edition ****************/

  String confirmChangeToFormTitle();

  String confirmChangeToFormMessage();

  String editDescriptiveMetadataTitle();

  String editDescriptionMetadataWarning();

  String editDescriptiveMetadataFormLabel();

  /********************* SLIDER *************************/

  String cannotJumpToPrevious();

  String cannotJumpToNext();

  String cannotJumpToPreviousDescription();

  String cannotJumpToNextDescription();

  /************* TO BE ORGANIZED *************************/

  String mandatoryPlugin();

  String optionalPlugin();

  String aipLevel();

  SafeHtml defaultColumnHeader(@Select String field);

  String pluginStateMessage(@Select PluginState state);

  String notificationStateValue(@Select NotificationState state);

  String isAcknowledged(@Select String isAcknowledged);

  String preservationEventAgentIdentifier();

  String preservationEventAgentType();

  String preservationEventAgentVersion();

  String preservationEventAgentNote();

  String preservationEventAgentRoles();

  String preservationEventAgentExtension();

  String mandatoryField();

  String numberIsRequired();

  String isAMandatoryField(String field);

  String passwordIsTooSmall();

  String passwordDoesNotMatchConfirmation();

  String emailNotValid();

  String wrongMailFormat();

  String fileAlreadyExists();

  SafeHtml tableDownloadCSV();

  SafeHtml tableAction();

  String showMore();

  String showLess();

  String uriLinkingIdentifierTitle();

  /* Version action */
  String versionAction(@Select String action);

  String versionActionBy(String message, String string);

  String browseFileDipEmpty();

  String browseFileDipDelete();

  String browseFileDipOpenedExternalURL();

  String browseFileDipRepresentationConfirmTitle();

  String browseFileDipRepresentationConfirmMessage();

  String jobReportSource(@Select String sourceObjectClass);

  String jobReportOutcome(@Select String outcomeObjectClass);

  String sourceObjectList(long result, String listCounter);

  /* Threshold exceed warning */

  String runningInBackgroundTitle();

  String runningInBackgroundDescription();

  /* Export list messages */

  String exportListTitle();

  String exportListMessage(int limit);

  String representationInformationAssociationsTitle();

  SafeHtml representationInformationNoAssociations(String field, String value, @Select String indexObjectName);

  SafeHtml representationInformationAssociatedWith(String field, String value, @Select String indexObjectName);

  SafeHtml representationInformationAssociateWith(String field, String value, @Select String indexObjectName);

  SafeHtml representationInformationAssociatedWithDescription(String field, String value,
    @Select String indexObjectName);

  String representationInformationNameFromAssociation(String field, String value, @Select String indexObjectName);

  /******* Rich text toolbar ******************************************/

  String insertImageUrl();

  String insertLinkUrl();

  String editHTMLContent();

  String actionableEmptyHelp(@Select ActionableObject.ActionableObjectType action);

  String searchButtonAdvancedSearch();

  String infoSliderRepresentationTitle();

  String infoSliderAipInfoTitle();

  String infoSliderFileInfoTitle();

  String tableUpdateOn();

  String tableUpdating();

  String tableUpdatePause();

  String tableUpdateErrorConnection();

  String details();

  String selectAllPages();

  String selectThisPage();

  String dateRangeFieldFrom();

  String dateRangeFieldTo();

  String inputStorageSizeFrom();

  String inputStorageSizeTo();

  String inputStorageSizeList();

  String removableTextBox();

  /** Disposal **/

  String disposalPolicyTitle();

  String disposalRulesTitle();

  String orderDisposalRulesTitle();

  String disposalSchedulesTitle();

  String disposalHoldsTitle();

  String disposalDestroyedRecordsTitle();

  String disposalConfirmationsTitle();

  String createDisposalConfirmationTitle();

  String createDisposalConfirmationExtraInformationTitle();

  String disposalNewConfirmationAction();

  String disposalRuleTitle();

  String disposalRuleIdentifier();

  String disposalRuleDescription();

  String disposalRuleScheduleName();

  String disposalRuleType();

  String disposalRuleTypeValue(@Select String action);

  String disposalRuleOrder();

  String disposalRuleCondition();

  String disposalRuleConditionOperator();

  String addMetadataField();

  String disposalScheduleTitle();

  String disposalScheduleIdentifier();

  String disposalScheduleDescription();

  String disposalScheduleMandate();

  String disposalScheduleNotes();

  String disposalSchedulePeriod();

  String disposalScheduleActionCol();

  String disposalScheduleAction(@Select String action);

  String disposalScheduleRetentionTriggerElementId();

  String disposalScheduleRetentionTriggerCodeValue(@Select String trigger);

  String disposalScheduleRetentionPeriodInterval();

  String retentionPeriod(@PluralCount int duration, @Select String interval);

  String disposalScheduleRetentionPeriodIntervalValue(@Select String interval);

  String disposalScheduleRetentionPeriodDuration();

  String disposalScheduleNumberOfAIPs();

  String disposalScheduleStateCol();

  String disposalScheduleState(@Select String state);

  String disposalScheduleUsedInRule();

  String disposalHoldIdentifier();

  String disposalHoldTitle();

  String disposalHoldDescription();

  String disposalHoldMandate();

  String disposalHoldNotes();

  String disposalHoldNumberOfAIPs();

  String disposalHoldStateCol();

  String disposalHoldState(@Select String state);

  String newDisposalRuleTitle();

  String editDisposalRuleTitle();

  String newDisposalScheduleTitle();

  String editDisposalScheduleTitle();

  String newDisposalHoldTitle();

  String editDisposalHoldTitle();

  String createDisposalRuleFailure(String reason);

  String createDisposalRuleAlreadyExists(String title);

  String createDisposalScheduleFailure(String reason);

  String createDisposalScheduleAlreadyExists(String title);

  String createDisposalHoldFailure(String reason);

  String createDisposalHoldAlreadyExists(String title);

  String editRules();

  String applyRules();

  String showDisposalRuleTitle();

  String showDisposalHoldTitle();

  String showDisposalScheduleTitle();

  String disposalConfirmationTitle();

  String disposalConfirmationCreationDate();

  String disposalConfirmationCreationBy();

  String disposalConfirmationStatus();

  String disposalConfirmationAIPs();

  String disposalConfirmationSize();

  String disposalConfirmationState(@Select DisposalConfirmationState state);

  String applyDisposalScheduleButton();

  String deleteDisposalConfirmationReport();

  String permanentlyDeleteFromBinButton();

  String reExecuteDisposalDestroyActionButton();

  String recoverDisposalConfirmationExecutionFailedButton();

  String restoreFromBinButton();

  String newDisposalConfirmationButton();

  String createDisposalScheduleButton();

  String disassociateDisposalScheduleButton();

  String associateDisposalScheduleButton();

  String disposalScheduleSelectionDialogTitle();

  String changeDisposalScheduleActionTitle();

  String createDisposalConfirmationActionTitle();

  String dissociateDisposalScheduleDialogTitle();

  String dissociateDisposalScheduleDialogMessage(Long size);

  String createDisposalConfirmationReportDialogTitle();

  String createDisposalConfirmationReportDialogMessage(Long size);

  String associateDisposalScheduleDialogTitle();

  String associateDisposalScheduleDialogMessage(Long size);

  String applyToHierarchyDisposalDialogDescription();

  String overwriteAllDisposalDialogCheckBox();

  String overwriteAllDisposalDialogDescription();

  String disposalPolicyAIPWithoutAssociation();

  String disposalRetentionStartDateLabel();

  String disposalRetentionDueDateLabel();

  String disposalRetentionPeriodLabel();

  String disposalActionLabel();

  String holdStatusLabel();

  String addDisposalHoldButton();

  String disposalHoldAssociatedOnLabel();

  String disposalHoldAssociatedByLabel();

  String disposalHoldLiftedOnLabel();

  String disposalHoldLiftedByLabel();

  String disposalOnHoldStatusLabel();

  String disposalClearStatusLabel();

  String disposalScheduleRetentionPeriodNotValidFormat();

  String disposalScheduleListAips();

  String disposalHoldListAips();

  String deleteConfirmationReportDialogTitle();

  String deleteConfirmationReportDialogMessage();

  String deleteConfirmationReportSuccessTitle();

  String deleteConfirmationReportSuccessMessage();

  // disposal confirmation data panel
  String disposalConfirmationDataPanelTitle();

  String disposalConfirmationDataNote();

  String disposalTitle();

  String disposalHoldSelectionDialogTitle();

  String applyDisposalHoldDialogTitle();

  String applyDisposalHoldDialogMessage(@PluralCount int size);

  String applyDisposalHoldButton();

  String createDisposalHoldButton();

  String associateDisposalHoldButton();

  String clearDisposalHoldButton();

  String overrideDisposalHoldButton();

  String clearDisposalHoldDialogTitle();

  String clearDisposalHoldDialogMessage(@PluralCount int size);

  String disposalHoldAssociatedOn();

  String disposalHoldLiftedOn();

  String conditionActualParent();

  String editRulesOrder();

  String confirmChangeRulesOrder();

  String deleteDisposalRuleDialogTitle();

  String deleteDisposalRuleDialogMessage(String title);

  String disposalScheduleAssociationInformationTitle();

  String disposalScheduleActionCode(@Select String disposalAction);

  String disposalScheduleAssociationTitle();

  String disposalConfirmationAssociationInformationTitle();

  String disposalConfirmationAssociationTitle();

  String disposalHoldsAssociationInformationTitle();

  String transitiveDisposalHoldsAssociationInformationTitle();

  String disposalPolicyActionSummary(@Select String action);

  String disposalPolicyScheduleSummary(String action, String temporal);

  String disposalPolicyScheduleYearSummary(@PluralCount int duration);

  String disposalPolicyScheduleMonthSummary(@PluralCount int duration);

  String disposalPolicyScheduleDaySummary(@PluralCount int duration);

  String disposalPolicyHoldSummary();

  String disposalPolicyConfirmationSummary();

  String disposalPolicySummaryReady(String action);

  String disposalPolicyNoScheduleSummary();

  String permanentlyRetained();

  String disposalPolicyRetainPermanently();

  String disposalPolicyNone();

  String disposalPolicyDestroyedAIPSummary(String destroyedOn);

  String permanentlyDeleteConfirmDialogTitle();

  String permanentlyDeleteConfirmDialogMessage();

  String restoreDestroyedRecordsConfirmDialogTitle();

  String restoreDestroyedRecordsConfirmDialogMessage();

  String restoreDestroyedRecordsSuccessTitle();

  String restoreDestroyedRecordsSuccessMessage();

  String recoverDestroyedRecordsConfirmDialogTitle();

  String recoverDestroyedRecordsConfirmDialogMessage();

  String recoverDestroyedRecordsSuccessTitle();

  String recoverDestroyedRecordsSuccessMessage();

  String permanentlyDeleteRecordsSuccessTitle();

  String permanentlyDeleteRecordsSuccessMessage();

  String disposalScheduleAssociationTypeLabel();

  String disposalScheduleAssociationType(@Select String type);

  String applyDisposalRulesDialogTitle();

  String applyDisposalRulesDialogMessage();

  SafeHtml applyDisposalRulesDialogExplanation();

  String deleteDisposalRuleSuccessTitle();

  String deleteDisposalRuleSuccessMessage(String title);

  String updateDisposalRulesOrderSuccessTitle();

  String updateDisposalRulesOrderSuccessMessage();

  String confirmEditRuleMessage();

  String disposalPolicyRetentionPeriodCalculationError();

  String disposalConfirmationShowRecordsToDestroy();

  String disposalConfirmationShowRecordsToReview();

  String disposalConfirmationShowRecordsRetentionPeriodCalculationError();
}
