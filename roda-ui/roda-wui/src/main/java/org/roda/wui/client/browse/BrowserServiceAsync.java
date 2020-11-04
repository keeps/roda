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
package org.roda.wui.client.browse;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.roda.core.data.v2.ip.disposal.DisposalHoldAssociation;
import org.roda.core.data.v2.ip.disposal.DisposalHolds;
import org.roda.core.data.v2.ip.disposal.DisposalRule;
import org.roda.core.data.v2.ip.disposal.DisposalRules;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.DisposalSchedules;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
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

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Luis Faria
 * 
 */
public interface BrowserServiceAsync {

  void retrieveBrowseAIPBundle(String aipId, String localeString, List<String> aipFieldsToReturn,
    AsyncCallback<BrowseAIPBundle> callback);

  void retrieveDescriptiveMetadataEditBundle(String aipId, String representationId, String descId, String type,
    String version, String localeString, AsyncCallback<DescriptiveMetadataEditBundle> callback);

  void retrieveDescriptiveMetadataEditBundle(String aipId, String representationId, String descId, String localeString,
    AsyncCallback<DescriptiveMetadataEditBundle> callback);

  void moveAIPInHierarchy(SelectedItems<IndexedAIP> selected, String parentId, String details,
    AsyncCallback<Job> callback);

  void createAIP(String parentId, String type, AsyncCallback<String> callback);

  void createRepresentation(String aipId, String details, AsyncCallback<String> callback);

  void deleteAIP(SelectedItems<IndexedAIP> aips, String details, AsyncCallback<Job> callback);

  void deleteRepresentation(SelectedItems<IndexedRepresentation> representations, String details,
    AsyncCallback<Job> callback);

  void deleteFile(SelectedItems<IndexedFile> representations, String details, AsyncCallback<Job> callback);

  void updateDescriptiveMetadataFile(String aipId, String representationId, DescriptiveMetadataEditBundle bundle,
    AsyncCallback<Void> callback);

  void deleteDescriptiveMetadataFile(String aipId, String representationId, String descriptiveMetadataId,
    AsyncCallback<Void> callback);

  void createDescriptiveMetadataFile(String aipId, String representationId, DescriptiveMetadataEditBundle newBundle,
    AsyncCallback<Void> asyncCallback);

  void retrieveDescriptiveMetadataPreview(SupportedMetadataTypeBundle bundle, AsyncCallback<String> async);

  void createTransferredResourcesFolder(String parent, String folderName, boolean commit,
    AsyncCallback<String> callback);

  void deleteTransferredResources(SelectedItems<TransferredResource> selected, AsyncCallback<Void> callback);

  void transferScanRequestUpdate(String transferredResourceUUID, AsyncCallback<Void> callback);

  void createJob(Job job, AsyncCallback<Job> callback);

  void stopJob(String jobId, AsyncCallback<Void> callback);

  void retrievePluginsInfo(List<PluginType> type, AsyncCallback<List<PluginInfo>> callback);

  void retrieveReindexPluginObjectClasses(AsyncCallback<Set<Pair<String, String>>> asyncCallback);

  void retrieveCreateIngestProcessBundle(AsyncCallback<CreateIngestJobBundle> callback);

  void retrieveJobBundle(String jobId, List<String> fieldsToReturn, AsyncCallback<JobBundle> callback);

  void retrieveViewersProperties(AsyncCallback<Viewers> callback);

  void retrieveSupportedMetadata(String aipId, String representationUUID, String locale,
    AsyncCallback<List<SupportedMetadataTypeBundle>> callback);

  void retrievePreservationEventViewBundle(String eventId, AsyncCallback<PreservationEventViewBundle> asyncCallback);

  void retrieveDescriptiveMetadataVersionsBundle(String aipId, String representationId, String descriptiveMetadataId,
    String localeString, AsyncCallback<DescriptiveMetadataVersionsBundle> callback);

  void revertDescriptiveMetadataVersion(String aipId, String representationId, String descriptiveMetadataId,
    String versionId, AsyncCallback<Void> callback);

  void deleteDescriptiveMetadataVersion(String aipId, String representationId, String descriptiveMetadataId,
    String versionId, AsyncCallback<Void> callback);

  <T extends IsIndexed> void find(String classNameToReturn, Filter filter, Sorter sorter, Sublist sublist,
    Facets facets, String localeString, boolean justActive, List<String> fieldsToReturn,
    AsyncCallback<IndexResult<T>> callback);

  <T extends IsIndexed> void delete(String classNameToReturn, SelectedItems<T> ids, AsyncCallback<Void> callback);

  void count(String classNameToReturn, Filter filter, boolean justActive, AsyncCallback<Long> callback);

  <T extends IsIndexed> void retrieve(String classNameToReturn, String id, List<String> fieldsToReturn,
    AsyncCallback<T> callback);

  <T extends IsIndexed> void retrieve(String classNameToReturn, SelectedItems<T> selectedItems,
    List<String> fieldsToReturn, AsyncCallback<List<T>> asyncCallback);

  void suggest(String classNameToReturn, String field, String query, boolean allowPartial,
    AsyncCallback<List<String>> callback);

  void updateAIPPermissions(SelectedItems<IndexedAIP> aips, Permissions permissions, String details, boolean recursive,
    AsyncCallback<Job> callback);

  void updateDIPPermissions(SelectedItems<IndexedDIP> dips, Permissions permissions, String details,
    AsyncCallback<Job> callback);

  void createRisk(Risk risk, AsyncCallback<Risk> asyncCallback);

  void updateRisk(Risk risk, int incidences, AsyncCallback<Void> asyncCallback);

  void revertRiskVersion(String riskId, String versionId, AsyncCallback<Void> callback);

  void deleteRiskVersion(String riskId, String versionId, AsyncCallback<Void> callback);

  void retrieveRiskVersions(String riskId, AsyncCallback<RiskVersionsBundle> callback);

  void hasRiskVersions(String id, AsyncCallback<Boolean> asyncCallback);

  void retrieveRiskVersion(String riskId, String selectedVersion, AsyncCallback<Risk> asyncCallback);

  void retrieveShowMitigationTerms(int preMitigationProbability, int preMitigationImpact, int posMitigationProbability,
    int posMitigationImpact, AsyncCallback<RiskMitigationBundle> asyncCallback);

  void retrieveMitigationSeverityLimits(AsyncCallback<List<String>> asyncCallback);

  void retrieveAllMitigationProperties(AsyncCallback<MitigationPropertiesBundle> asyncCallback);

  void deleteRisk(SelectedItems<IndexedRisk> selected, AsyncCallback<Job> asyncCallback);

  void deleteRiskIncidences(SelectedItems<RiskIncidence> selected, String details, AsyncCallback<Job> asyncCallback);

  void createProcess(String jobName, SelectedItems<?> selected, String id, Map<String, String> value,
    String selectedClass, AsyncCallback<Job> asyncCallback);

  void createProcessJson(String jobName, SelectedItems<?> selected, String id, Map<String, String> value,
    String selectedClass, AsyncCallback<String> asyncCallback);

  void updateRiskCounters(AsyncCallback<Void> asyncCallback);

  void appraisal(SelectedItems<IndexedAIP> selected, boolean accept, String rejectReason, AsyncCallback<Job> callback);

  void renameTransferredResource(String transferredResourceId, String newName, AsyncCallback<String> asyncCallback);

  void moveTransferredResource(SelectedItems<TransferredResource> selected, TransferredResource transferredResource,
    AsyncCallback<Job> asyncCallback);

  void retrieveSelectedTransferredResource(SelectedItems<TransferredResource> selected,
    AsyncCallback<List<TransferredResource>> asyncCallback);

  void deleteFile(String fileUUID, String details, AsyncCallback<Void> callback);

  void updateRiskIncidence(RiskIncidence incidence, AsyncCallback<Void> asyncCallback);

  void updateMultipleIncidences(SelectedItems<RiskIncidence> selected, String status, String severity, Date mitigatedOn,
    String mitigatedBy, String mitigatedDescription, AsyncCallback<Job> loadingAsyncCallback);

  void renameFolder(String folderUUID, String newName, String details, AsyncCallback<IndexedFile> asyncCallback);

  void moveFiles(String aipId, String representationId, SelectedItems<IndexedFile> selectedFiles, IndexedFile toFolder,
    String details, AsyncCallback<Job> asyncCallback);

  void createFolder(String aipId, String representationId, String folderUUID, String newName, String details,
    AsyncCallback<IndexedFile> asyncCallback);

  void createFormatIdentificationJob(SelectedItems<?> selected, AsyncCallback<Job> loadingAsyncCallback);

  void changeRepresentationType(SelectedItems<IndexedRepresentation> selectedRepresentation, String newType,
    String details, AsyncCallback<Job> loadingAsyncCallback);

  void changeAIPType(SelectedItems<IndexedAIP> selectedAIP, String newType, String details,
    AsyncCallback<Job> loadingAsyncCallback);

  void changeRepresentationStates(IndexedRepresentation selectedRepresentation, List<String> newStates, String details,
    AsyncCallback<Void> loadingAsyncCallback);

  void retrieveDipBundle(String dipUUID, String dipFileUUID, String localeString,
    AsyncCallback<BrowseDipBundle> callback);

  void deleteDIPs(SelectedItems<IndexedDIP> dips, String details, AsyncCallback<Job> async);

  void retrieveBrowseRepresentationBundle(String aipId, String representationId, String localeString,
    List<String> representationFieldsToReturn, AsyncCallback<BrowseRepresentationBundle> callback);

  void retrieveBrowseFileBundle(String historyAipId, String historyRepresentationId, List<String> historyFilePath,
    String historyFileId, List<String> fileFieldsToReturn, AsyncCallback<BrowseFileBundle> asyncCallback);

  <T extends IsIndexed> void retrieveFromModel(String objectClass, String objectUUID, AsyncCallback<T> asyncCallback);

  void hasDocumentation(String aipId, AsyncCallback<Boolean> asyncCallback);

  void hasSubmissions(String aipId, AsyncCallback<Boolean> asyncCallback);

  void showDIPEmbedded(AsyncCallback<Boolean> asyncCallback);

  void acknowledgeNotification(String notificationId, String ackToken, AsyncCallback<Notification> asyncCallback);

  void getExportLimit(AsyncCallback<Integer> asyncCallback);

  void retrieveAIPTypeOptions(String locale, AsyncCallback<Pair<Boolean, List<String>>> asyncCallback);

  void retrieveRepresentationTypeOptions(String locale, AsyncCallback<Pair<Boolean, List<String>>> asyncCallback);

  void createRepresentationInformation(RepresentationInformation ri, RepresentationInformationExtraBundle bundle,
    AsyncCallback<RepresentationInformation> asyncCallback);

  void updateRepresentationInformation(RepresentationInformation ri, RepresentationInformationExtraBundle bundle,
    AsyncCallback<Void> asyncCallback);

  void updateRepresentationInformationListWithFilter(
    SelectedItems<RepresentationInformation> representationInformationIds, String filterToAdd,
    AsyncCallback<Job> asyncCallback);

  void deleteRepresentationInformation(SelectedItems<RepresentationInformation> selected,
    AsyncCallback<Job> asyncCallback);

  void retrieveRepresentationInformationWithFilter(String riFilter, AsyncCallback<Pair<String, Integer>> asyncCallback);

  void retrieveObjectClassFields(String locale, AsyncCallback<RepresentationInformationFilterBundle> asyncCallback);

  void retrieveRepresentationInformationFamilyOptions(String localeString,
    AsyncCallback<Map<String, String>> asyncCallback);

  void retrieveRepresentationInformationFamilyOptions(String family, String localeString,
    AsyncCallback<String> asyncCallback);

  void retrieveRelationTypeTranslations(String localeString,
    AsyncCallback<RelationTypeTranslationsBundle> asyncCallback);

  void retrieveRepresentationInformationExtraBundle(String representationInformationId, String localeString,
    AsyncCallback<RepresentationInformationExtraBundle> asyncCallback);

  void retrieveSharedProperties(String localeName, AsyncCallback<Map<String, List<String>>> asyncCallback);

  void createDisposalRule(DisposalRule rule, AsyncCallback<DisposalRule> async);

  void retrieveDisposalRule(String disposalRuleId, AsyncCallback<DisposalRule> async);

  void listDisposalRules(AsyncCallback<DisposalRules> async);

  void updateDisposalRule(DisposalRule rule, AsyncCallback<DisposalRule> async);

  void deleteDisposalRule(String disposalRuleId, AsyncCallback<Void> async);

  void createDisposalSchedule(DisposalSchedule schedule, AsyncCallback<DisposalSchedule> async);

  void retrieveDisposalSchedule(String disposalScheduleId, AsyncCallback<DisposalSchedule> async);

  void listDisposalSchedules(AsyncCallback<DisposalSchedules> async);

  void updateDisposalSchedule(DisposalSchedule schedule, AsyncCallback<DisposalSchedule> async);

  void deleteDisposalSchedule(String disposalScheduleId, AsyncCallback<Void> async);

  void createDisposalHold(DisposalHold hold, AsyncCallback<DisposalHold> async);

  void retrieveDisposalHold(String disposalHoldId, AsyncCallback<DisposalHold> async);

  void listDisposalHolds(AsyncCallback<DisposalHolds> async);

  void updateDisposalHold(DisposalHold hold, AsyncCallback<DisposalHold> async);

  void deleteDisposalHold(String disposalHoldId, AsyncCallback<Void> async);

  void associateDisposalSchedule(SelectedItems<IndexedAIP> selectedItems, String disposalScheduleId,
    AsyncCallback<Job> async);

  void disassociateDisposalSchedule(SelectedItems<IndexedAIP> selectedItems, AsyncCallback<Job> async);

  void createDisposalConfirmationReport(SelectedItems<IndexedAIP> selectedItems, String title, DisposalConfirmationExtraBundle metadata,
    AsyncCallback<Job> async);

  void listDisposalHoldsAssociation(String aipId, AsyncCallback<List<DisposalHoldAssociation>> async);

  void deleteDisposalConfirmationReport(SelectedItems<DisposalConfirmation> selectedItems, String details,
                                        AsyncCallback<Job> async);

  void destroyRecordsInDisposalConfirmationReport(SelectedItemsList<DisposalConfirmation> selectedItems,
                                                  AsyncCallback<Job> async);

  void retrieveDisposalConfirmationExtraBundle(AsyncCallback<DisposalConfirmationExtraBundle> async);

  void applyDisposalHold(SelectedItems<IndexedAIP> selectedItems, String disposalHoldId, AsyncCallback<Job> async);

  void liftDisposalHold(SelectedItems<DisposalHold> selectedItems, AsyncCallback<Job> async);
}
