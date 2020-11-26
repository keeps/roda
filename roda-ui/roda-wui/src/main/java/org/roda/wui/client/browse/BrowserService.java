/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmation;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.data.v2.ip.disposal.DisposalHolds;
import org.roda.core.data.v2.ip.disposal.DisposalRule;
import org.roda.core.data.v2.ip.disposal.DisposalRules;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.DisposalSchedules;
import org.roda.core.data.v2.ip.disposal.aipMetadata.DisposalHoldAIPMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;
import org.roda.wui.client.browse.bundle.BrowseDipBundle;
import org.roda.wui.client.browse.bundle.BrowseFileBundle;
import org.roda.wui.client.browse.bundle.BrowseRepresentationBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataEditBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataVersionsBundle;
import org.roda.wui.client.browse.bundle.DisposalConfirmationExtraBundle;
import org.roda.wui.client.browse.bundle.PreservationEventViewBundle;
import org.roda.wui.client.browse.bundle.RepresentationInformationExtraBundle;
import org.roda.wui.client.browse.bundle.RepresentationInformationFilterBundle;
import org.roda.wui.client.browse.bundle.SupportedMetadataTypeBundle;
import org.roda.wui.client.ingest.process.CreateIngestJobBundle;
import org.roda.wui.client.ingest.process.JobBundle;
import org.roda.wui.client.planning.MitigationPropertiesBundle;
import org.roda.wui.client.planning.RelationTypeTranslationsBundle;
import org.roda.wui.client.planning.RiskMitigationBundle;
import org.roda.wui.client.planning.RiskVersionsBundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * @author Luis Faria <lfaria@keep.pt>
 */
public interface BrowserService extends RemoteService {

  /**
   * Service location
   */
  static final String SERVICE_URI = "browserservice";

  /**
   * Utilities
   *
   */
  public static class Util {

    private Util() {
      // do nothing
    }

    /**
     * Get singleton instance
     *
     * @return the instance
     */
    public static BrowserServiceAsync getInstance() {
      BrowserServiceAsync instance = (BrowserServiceAsync) GWT.create(BrowserService.class);
      ServiceDefTarget target = (ServiceDefTarget) instance;
      target.setServiceEntryPoint(GWT.getHostPageBaseURL() + RodaConstants.GWT_RPC_BASE_URL + SERVICE_URI);
      return instance;
    }
  }

  BrowseAIPBundle retrieveBrowseAIPBundle(String aipId, String localeString, List<String> aipFieldsToReturn)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  BrowseRepresentationBundle retrieveBrowseRepresentationBundle(String aipId, String representationId,
    String localeString, List<String> representationFieldsToReturn)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  BrowseFileBundle retrieveBrowseFileBundle(String historyAipId, String historyRepresentationId,
    List<String> historyFilePath, String historyFileId, List<String> fileFieldsToReturn)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  DescriptiveMetadataEditBundle retrieveDescriptiveMetadataEditBundle(String aipId, String representationId,
    String descId, String type, String version, String localeString)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException;

  DescriptiveMetadataEditBundle retrieveDescriptiveMetadataEditBundle(String aipId, String representationId,
    String descId, String localeString)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException;

  Job moveAIPInHierarchy(SelectedItems<IndexedAIP> selected, String parentId, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException,
    AlreadyExistsException, ValidationException;

  String createAIP(String parentId, String type) throws AuthorizationDeniedException, GenericException,
    NotFoundException, RequestNotValidException, AlreadyExistsException;

  Job deleteAIP(SelectedItems<IndexedAIP> aips, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  Job deleteRepresentation(SelectedItems<IndexedRepresentation> representations, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  Job deleteFile(SelectedItems<IndexedFile> files, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  void deleteDescriptiveMetadataFile(String aipId, String representationId, String descriptiveMetadataId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  void updateDescriptiveMetadataFile(String aipId, String representationId, DescriptiveMetadataEditBundle bundle)
    throws AuthorizationDeniedException, GenericException, ValidationException, NotFoundException,
    RequestNotValidException;

  void createDescriptiveMetadataFile(String aipId, String representationId, DescriptiveMetadataEditBundle newBundle)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException,
    AlreadyExistsException, ValidationException;

  String retrieveDescriptiveMetadataPreview(SupportedMetadataTypeBundle bundle) throws AuthorizationDeniedException,
    GenericException, ValidationException, NotFoundException, RequestNotValidException;

  String createTransferredResourcesFolder(String parent, String folderName, boolean commit)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException;

  void deleteTransferredResources(SelectedItems<TransferredResource> selected)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  void transferScanRequestUpdate(String transferredResourceUUID)
    throws IsStillUpdatingException, AuthorizationDeniedException;

  Job createJob(Job job) throws AuthorizationDeniedException, NotFoundException, RequestNotValidException,
    GenericException, JobAlreadyStartedException;

  void stopJob(String jobId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  List<PluginInfo> retrievePluginsInfo(List<PluginType> type);

  Set<Pair<String, String>> retrieveReindexPluginObjectClasses();

  CreateIngestJobBundle retrieveCreateIngestProcessBundle();

  JobBundle retrieveJobBundle(String jobId, List<String> fieldsToReturn) throws RODAException;

  Viewers retrieveViewersProperties() throws GenericException;

  List<SupportedMetadataTypeBundle> retrieveSupportedMetadata(String aipId, String representationUUID, String locale)
    throws RODAException;

  PreservationEventViewBundle retrievePreservationEventViewBundle(String eventId) throws RODAException;

  DescriptiveMetadataVersionsBundle retrieveDescriptiveMetadataVersionsBundle(String aipId, String representationId,
    String descriptiveMetadataId, String localeString)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException;

  void revertDescriptiveMetadataVersion(String aipId, String representationId, String descriptiveMetadataId,
    String versionId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException;

  void deleteDescriptiveMetadataVersion(String aipId, String representationId, String descriptiveMetadataId,
    String versionId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException;

  <T extends IsIndexed> IndexResult<T> find(String classNameToReturn, Filter filter, Sorter sorter, Sublist sublist,
    Facets facets, String localeString, boolean justActive, List<String> fieldsToReturn)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException;

  <T extends IsIndexed> Long count(String classNameToReturn, Filter filter, boolean justActive)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException;

  <T extends IsIndexed> T retrieve(String classNameToReturn, String id, List<String> fieldsToReturn)
    throws RODAException;

  <T extends IsIndexed> List<T> retrieve(String classNameToReturn, SelectedItems<T> selectedItems,
    List<String> fieldsToReturn)
    throws GenericException, AuthorizationDeniedException, NotFoundException, RequestNotValidException;

  <T extends IsIndexed> void delete(String classNameToReturn, SelectedItems<T> ids)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  <T extends IsIndexed> List<String> suggest(String classNameToReturn, String field, String query, boolean allowPartial)
    throws AuthorizationDeniedException, GenericException, NotFoundException;

  Job updateAIPPermissions(SelectedItems<IndexedAIP> aips, Permissions permissions, String details, boolean recursive)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException,
    JobAlreadyStartedException;

  Job updateDIPPermissions(SelectedItems<IndexedDIP> dips, Permissions permissions, String details)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException;

  Risk createRisk(Risk risk)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  void updateRisk(Risk risk, int incidences)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  void revertRiskVersion(String riskId, String versionId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException, IOException;

  boolean hasRiskVersions(String id)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException;

  void deleteRiskVersion(String riskId, String versionId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException, IOException;

  RiskVersionsBundle retrieveRiskVersions(String riskId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException, IOException;

  Risk retrieveRiskVersion(String riskId, String selectedVersion)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException, IOException;

  RiskMitigationBundle retrieveShowMitigationTerms(int preMitigationProbability, int preMitigationImpact,
    int posMitigationProbability, int posMitigationImpact) throws AuthorizationDeniedException;

  List<String> retrieveMitigationSeverityLimits() throws AuthorizationDeniedException;

  MitigationPropertiesBundle retrieveAllMitigationProperties() throws AuthorizationDeniedException;

  Job deleteRisk(SelectedItems<IndexedRisk> selected) throws AuthorizationDeniedException, GenericException,
    RequestNotValidException, NotFoundException, InvalidParameterException, JobAlreadyStartedException;

  <T extends IsIndexed> Job createProcess(String jobName, SelectedItems<T> selected, String id,
    Map<String, String> value, String selectedClass) throws AuthorizationDeniedException, RequestNotValidException,
    NotFoundException, GenericException, JobAlreadyStartedException;

  <T extends IsIndexed> String createProcessJson(String jobName, SelectedItems<T> selected, String id,
    Map<String, String> value, String selectedClass) throws AuthorizationDeniedException, RequestNotValidException,
    NotFoundException, GenericException, JobAlreadyStartedException;

  void updateRiskCounters()
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException;

  Job appraisal(SelectedItems<IndexedAIP> selected, boolean accept, String rejectReason)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException;

  String renameTransferredResource(String transferredResourceId, String newName)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException,
    IsStillUpdatingException, NotFoundException;

  Job moveTransferredResource(SelectedItems<TransferredResource> selected, TransferredResource transferredResource)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, AlreadyExistsException,
    IsStillUpdatingException, NotFoundException;

  List<TransferredResource> retrieveSelectedTransferredResource(SelectedItems<TransferredResource> selected)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException;

  void deleteFile(String fileUUID, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException;

  void updateRiskIncidence(RiskIncidence incidence) throws AuthorizationDeniedException, GenericException;

  Job deleteRiskIncidences(SelectedItems<RiskIncidence> selected, String details)
    throws JobAlreadyStartedException, AuthorizationDeniedException, GenericException, RequestNotValidException,
    NotFoundException, InvalidParameterException;

  Job updateMultipleIncidences(SelectedItems<RiskIncidence> selected, String status, String severity, Date mitigatedOn,
    String mitigatedBy, String mitigatedDescription)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException;

  String createRepresentation(String aipId, String details) throws AuthorizationDeniedException, GenericException,
    NotFoundException, RequestNotValidException, AlreadyExistsException;

  IndexedFile renameFolder(String folderUUID, String newName, String details) throws AuthorizationDeniedException,
    GenericException, RequestNotValidException, AlreadyExistsException, NotFoundException;

  Job moveFiles(String aipId, String representationId, SelectedItems<IndexedFile> selectedFiles, IndexedFile toFolder,
    String details) throws AuthorizationDeniedException, GenericException, RequestNotValidException,
    AlreadyExistsException, NotFoundException;

  IndexedFile createFolder(String aipId, String representationId, String folderUUID, String newName, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, AlreadyExistsException,
    NotFoundException;

  Job createFormatIdentificationJob(SelectedItems<?> selected) throws GenericException, AuthorizationDeniedException,
    JobAlreadyStartedException, RequestNotValidException, NotFoundException;

  Job changeRepresentationType(SelectedItems<IndexedRepresentation> selectedRepresentation, String newType,
    String details) throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException;

  Job changeAIPType(SelectedItems<IndexedAIP> selectedAIP, String newType, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException;

  void changeRepresentationStates(IndexedRepresentation selectedRepresentation, List<String> newStates, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException;

  BrowseDipBundle retrieveDipBundle(String dipUUID, String dipFileUUID, String localeString)
    throws RequestNotValidException, AuthorizationDeniedException, GenericException, NotFoundException;

  Job deleteDIPs(SelectedItems<IndexedDIP> dips, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  <T extends IsIndexed> T retrieveFromModel(String classNameToReturn, String id) throws RODAException;

  boolean hasDocumentation(String aipId)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException;

  boolean hasSubmissions(String aipId) throws AuthorizationDeniedException, RequestNotValidException, GenericException;

  boolean showDIPEmbedded();

  Notification acknowledgeNotification(String notificationId, String ackToken)
    throws GenericException, NotFoundException, AuthorizationDeniedException;

  int getExportLimit();

  Pair<Boolean, List<String>> retrieveAIPTypeOptions(String locale);

  Pair<Boolean, List<String>> retrieveRepresentationTypeOptions(String locale);

  RepresentationInformation createRepresentationInformation(RepresentationInformation ri,
    RepresentationInformationExtraBundle extra)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  void updateRepresentationInformation(RepresentationInformation ri, RepresentationInformationExtraBundle extra)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  Job updateRepresentationInformationListWithFilter(
    SelectedItems<RepresentationInformation> representationInformationIds, String filterToAdd)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  Job deleteRepresentationInformation(SelectedItems<RepresentationInformation> selected)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException;

  Pair<String, Integer> retrieveRepresentationInformationWithFilter(String riFilter) throws RODAException;

  RepresentationInformationFilterBundle retrieveObjectClassFields(String locale) throws AuthorizationDeniedException;

  Map<String, String> retrieveRepresentationInformationFamilyOptions(String localeString);

  String retrieveRepresentationInformationFamilyOptions(String family, String localeString);

  RelationTypeTranslationsBundle retrieveRelationTypeTranslations(String localeString)
    throws AuthorizationDeniedException;

  RepresentationInformationExtraBundle retrieveRepresentationInformationExtraBundle(String representationInformationId,
    String localeString)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  Map<String, List<String>> retrieveSharedProperties(String localeName);

  DisposalRule createDisposalRule(DisposalRule rule) throws AuthorizationDeniedException, AlreadyExistsException,
    NotFoundException, GenericException, RequestNotValidException, IOException;

  DisposalRule retrieveDisposalRule(String disposalRuleId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  DisposalRules listDisposalRules()
    throws AuthorizationDeniedException, IOException, GenericException, RequestNotValidException;

  DisposalRule updateDisposalRule(DisposalRule rule)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  void updateDisposalRules(DisposalRules rules)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  void deleteDisposalRule(String disposalRuleId)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException;

  Job applyDisposalRules(boolean applyToManuallyInclusive)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  DisposalSchedule createDisposalSchedule(DisposalSchedule schedule) throws AuthorizationDeniedException,
    AlreadyExistsException, NotFoundException, GenericException, RequestNotValidException;

  DisposalSchedule retrieveDisposalSchedule(String disposalScheduleId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  DisposalSchedules listDisposalSchedules()
    throws AuthorizationDeniedException, IOException, GenericException, RequestNotValidException;

  DisposalSchedule updateDisposalSchedule(DisposalSchedule schedule) throws AuthorizationDeniedException,
    NotFoundException, GenericException, RequestNotValidException, IllegalOperationException;

  void deleteDisposalSchedule(String disposalScheduleId) throws NotFoundException, AuthorizationDeniedException,
    IllegalOperationException, GenericException, RequestNotValidException;

  DisposalHold createDisposalHold(DisposalHold hold) throws AuthorizationDeniedException, AlreadyExistsException,
    NotFoundException, GenericException, RequestNotValidException;

  DisposalHold retrieveDisposalHold(String disposalHoldId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  DisposalHolds listDisposalHolds()
    throws AuthorizationDeniedException, IOException, GenericException, RequestNotValidException;

  DisposalHold updateDisposalHold(DisposalHold hold) throws AuthorizationDeniedException, NotFoundException,
    GenericException, RequestNotValidException, IllegalOperationException;

  void deleteDisposalHold(String disposalHoldId) throws NotFoundException, AuthorizationDeniedException,
    IllegalOperationException, GenericException, RequestNotValidException;

  Job associateDisposalSchedule(SelectedItems<IndexedAIP> selectedItems, String disposalScheduleId,
    Boolean applyToHierarchy, Boolean overwriteAll)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  Job disassociateDisposalSchedule(SelectedItems<IndexedAIP> selectedItems, Boolean applyToHierarchy)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  Job applyDisposalHold(SelectedItems<IndexedAIP> selectedItems, String disposalHoldId, boolean override)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  Job liftDisposalHold(SelectedItems<IndexedAIP> selectedItems, String disposalHoldId, boolean clearAll)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  Job createDisposalConfirmationReport(SelectedItems<IndexedAIP> selectedItems, String title,
    DisposalConfirmationExtraBundle metadata)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException;

  DisposalConfirmationExtraBundle retrieveDisposalConfirmationExtraBundle() throws RODAException;

  Job deleteDisposalConfirmationReport(SelectedItems<DisposalConfirmation> selectedItems, String details)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException;

  Job destroyRecordsInDisposalConfirmationReport(SelectedItemsList<DisposalConfirmation> selectedItems)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  Job permanentlyDeleteRecordsInDisposalConfirmationReport(SelectedItemsList<DisposalConfirmation> selectedItems)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  Job recoverRecordsInDisposalConfirmationReport(SelectedItemsList<DisposalConfirmation> selectedItems)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  Job restoreRecordsInDisposalConfirmationReport(SelectedItemsList<DisposalConfirmation> selectedItems) throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  List<DisposalHoldAIPMetadata> listDisposalHoldsAssociation(String aipId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  String retrieveDisposalConfirmationReport(String confirmationId) throws IOException, RODAException;

}
