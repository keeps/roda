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
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.browse.bundle.BrowseItemBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataEditBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataVersionsBundle;
import org.roda.wui.client.browse.bundle.DipBundle;
import org.roda.wui.client.browse.bundle.PreservationEventViewBundle;
import org.roda.wui.client.browse.bundle.SupportedMetadataTypeBundle;
import org.roda.wui.client.common.search.SearchField;
import org.roda.wui.client.ingest.process.CreateIngestJobBundle;
import org.roda.wui.client.ingest.process.JobBundle;
import org.roda.wui.client.planning.MitigationPropertiesBundle;
import org.roda.wui.client.planning.RiskMitigationBundle;
import org.roda.wui.client.planning.RiskVersionsBundle;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Luis Faria
 * 
 */
public interface BrowserServiceAsync {

  void retrieveItemBundle(String aipId, String localeString, AsyncCallback<BrowseItemBundle> callback);

  void retrieveDescriptiveMetadataEditBundle(String aipId, String representationId, String descId, String type,
    String version, String localeString, AsyncCallback<DescriptiveMetadataEditBundle> callback);

  void retrieveDescriptiveMetadataEditBundle(String aipId, String representationId, String descId, String localeString,
    AsyncCallback<DescriptiveMetadataEditBundle> callback);

  void retrieveSearchFields(String className, String locale, AsyncCallback<List<SearchField>> callback);

  void moveAIPInHierarchy(SelectedItems<IndexedAIP> selected, String parentId, String details,
    AsyncCallback<IndexedAIP> callback);

  void createAIP(String parentId, String type, AsyncCallback<String> callback);

  void createRepresentation(String aipId, String details, AsyncCallback<String> callback);

  void deleteAIP(SelectedItems<IndexedAIP> aips, AsyncCallback<String> callback);

  void deleteRepresentation(SelectedItems<IndexedRepresentation> representations, String details,
    AsyncCallback<Void> callback);

  void deleteFile(SelectedItems<IndexedFile> representations, String details, AsyncCallback<Void> callback);

  void updateDescriptiveMetadataFile(String aipId, String representationId, DescriptiveMetadataEditBundle bundle,
    AsyncCallback<Void> callback);

  void deleteDescriptiveMetadataFile(String aipId, String representationId, String descriptiveMetadataId,
    AsyncCallback<Void> callback);

  void createDescriptiveMetadataFile(String aipId, String representationId, DescriptiveMetadataEditBundle newBundle,
    AsyncCallback<Void> asyncCallback);

  void retrieveDescriptiveMetadataPreview(SupportedMetadataTypeBundle bundle, AsyncCallback<String> async);

  void createTransferredResourcesFolder(String parent, String folderName, AsyncCallback<String> callback);

  void deleteTransferredResources(SelectedItems<TransferredResource> selected, AsyncCallback<Void> callback);

  void transferScanRequestUpdate(String transferredResourceUUID, AsyncCallback<Void> callback);

  void createJob(Job job, AsyncCallback<Job> callback);

  void stopJob(String jobId, AsyncCallback<Void> callback);

  void retrievePluginsInfo(List<PluginType> type, AsyncCallback<List<PluginInfo>> callback);

  void retrieveReindexPluginObjectClasses(AsyncCallback<Set<Pair<String, String>>> asyncCallback);

  void retrieveCreateIngestProcessBundle(AsyncCallback<CreateIngestJobBundle> callback);

  void retrieveJobBundle(String jobId, AsyncCallback<JobBundle> callback);

  void retrieveViewersProperties(AsyncCallback<Viewers> callback);

  void retrieveSupportedMetadata(String aipId, String representationUUID, String locale,
    AsyncCallback<List<SupportedMetadataTypeBundle>> callback);

  void isCookiesMessageActive(AsyncCallback<Boolean> callback);

  void retrieveGoogleAnalyticsAccount(AsyncCallback<String> callback);

  void retrieveGoogleReCAPTCHAAccount(AsyncCallback<String> callback);

  void retrievePreservationEventViewBundle(String eventId, AsyncCallback<PreservationEventViewBundle> asyncCallback);

  void retrieveDescriptiveMetadataVersionsBundle(String aipId, String representationId, String descriptiveMetadataId,
    String localeString, AsyncCallback<DescriptiveMetadataVersionsBundle> callback);

  void revertDescriptiveMetadataVersion(String aipId, String representationId, String descriptiveMetadataId,
    String versionId, AsyncCallback<Void> callback);

  void deleteDescriptiveMetadataVersion(String aipId, String representationId, String descriptiveMetadataId,
    String versionId, AsyncCallback<Void> callback);

  <T extends IsIndexed> void find(String classNameToReturn, Filter filter, Sorter sorter, Sublist sublist,
    Facets facets, String localeString, boolean justActive, AsyncCallback<IndexResult<T>> callback);

  <T extends IsIndexed> void delete(String classNameToReturn, SelectedItems<T> ids, AsyncCallback<Void> callback);

  void count(String classNameToReturn, Filter filter, AsyncCallback<Long> callback);

  <T extends IsIndexed> void retrieve(String classNameToReturn, String id, AsyncCallback<T> callback);

  <T extends IsIndexed> void retrieve(String classNameToReturn, SelectedItems<T> selectedItems,
    AsyncCallback<List<T>> asyncCallback);

  void suggest(String classNameToReturn, String field, String query, boolean allowPartial,
    AsyncCallback<List<String>> callback);

  void updateAIPPermissions(List<IndexedAIP> aips, Permissions permissions, String details, boolean recursive,
    AsyncCallback<Void> callback);

  void createRisk(Risk risk, AsyncCallback<Risk> asyncCallback);

  void updateRisk(Risk risk, AsyncCallback<Void> asyncCallback);

  void createFormat(Format format, AsyncCallback<Format> asyncCallback);

  void updateFormat(Format format, AsyncCallback<Void> asyncCallback);

  void revertRiskVersion(String riskId, String versionId, AsyncCallback<Void> callback);

  void deleteRiskVersion(String riskId, String versionId, AsyncCallback<Void> callback);

  void retrieveRiskVersions(String riskId, AsyncCallback<RiskVersionsBundle> callback);

  void hasRiskVersions(String id, AsyncCallback<Boolean> asyncCallback);

  void retrieveRiskVersion(String riskId, String selectedVersion, AsyncCallback<Risk> asyncCallback);

  void retrieveShowMitigationTerms(int preMitigationProbability, int preMitigationImpact, int posMitigationProbability,
    int posMitigationImpact, AsyncCallback<RiskMitigationBundle> asyncCallback);

  void retrieveMitigationSeverityLimits(AsyncCallback<List<String>> asyncCallback);

  void retrieveAllMitigationProperties(AsyncCallback<MitigationPropertiesBundle> asyncCallback);

  void deleteRisk(SelectedItems<IndexedRisk> selected, AsyncCallback<Void> asyncCallback);

  void deleteRiskIncidences(SelectedItems<RiskIncidence> selected, AsyncCallback<Void> asyncCallback);

  void deleteFormat(SelectedItems<Format> selected, AsyncCallback<Void> asyncCallback);

  void createProcess(String jobName, SelectedItems<?> selected, String id, Map<String, String> value,
    String selectedClass, AsyncCallback<Job> asyncCallback);

  void updateRiskCounters(AsyncCallback<Void> asyncCallback);

  void appraisal(SelectedItems<IndexedAIP> selected, boolean accept, String rejectReason, AsyncCallback<Void> callback);

  void retrieveRepresentationById(String representationId, AsyncCallback<IndexedRepresentation> asyncCallback);

  void retrieveFileById(String fileId, AsyncCallback<IndexedFile> asyncCallback);

  void renameTransferredResource(String transferredResourceId, String newName, AsyncCallback<String> asyncCallback);

  void moveTransferredResource(SelectedItems<TransferredResource> selected, TransferredResource transferredResource,
    AsyncCallback<String> asyncCallback);

  void retrieveSelectedTransferredResource(SelectedItems<TransferredResource> selected,
    AsyncCallback<List<TransferredResource>> asyncCallback);

  void deleteFile(String fileUUID, String details, AsyncCallback<Void> callback);

  void updateRiskIncidence(RiskIncidence incidence, AsyncCallback<Void> asyncCallback);

  void updateMultipleIncidences(SelectedItems<RiskIncidence> selected, String status, String severity, Date mitigatedOn,
    String mitigatedBy, String mitigatedDescription, AsyncCallback<Void> loadingAsyncCallback);

  void renameFolder(String folderUUID, String newName, String details, AsyncCallback<String> asyncCallback);

  void moveFiles(String aipId, String representationUUID, SelectedItems<IndexedFile> selectedFiles,
    IndexedFile toFolder, String details, AsyncCallback<String> asyncCallback);

  void createFolder(String aipId, String representationUUID, String folderUUID, String newName, String details,
    AsyncCallback<String> asyncCallback);

  void createFormatIdentificationJob(SelectedItems selected, AsyncCallback<Void> loadingAsyncCallback);

  void changeRepresentationType(SelectedItemsList<IndexedRepresentation> selectedRepresentation, String newType,
    String details, AsyncCallback<Void> loadingAsyncCallback);

  void getDipBundle(String dipUUID, String aipUUID, String representationUUID, String fileUUID,
    AsyncCallback<DipBundle> callback);

  void deleteDIP(String dipId, String details, AsyncCallback<Void> async);
}
