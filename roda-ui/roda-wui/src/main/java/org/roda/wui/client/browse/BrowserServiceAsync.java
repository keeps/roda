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

import java.util.List;
import java.util.Map;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.v2.agents.Agent;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.risks.Risk;
import org.roda.wui.client.ingest.process.CreateIngestJobBundle;
import org.roda.wui.client.ingest.process.JobBundle;
import org.roda.wui.client.planning.MitigationPropertiesBundle;
import org.roda.wui.client.planning.RiskMitigationBundle;
import org.roda.wui.client.planning.RiskVersionsBundle;
import org.roda.wui.client.search.SearchField;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Luis Faria
 * 
 */
public interface BrowserServiceAsync {

  void getItemBundle(String aipId, String localeString, AsyncCallback<BrowseItemBundle> callback);

  void getDescriptiveMetadataEditBundle(String aipId, String descId,
    AsyncCallback<DescriptiveMetadataEditBundle> callback);

  void getSearchFields(String locale, AsyncCallback<List<SearchField>> callback);

  void moveInHierarchy(String aipId, String parentId, AsyncCallback<AIP> callback);

  void createAIP(String parentId, String type, AsyncCallback<String> callback);

  void removeAIP(SelectedItems aips, AsyncCallback<String> callback);

  void updateDescriptiveMetadataFile(String aipId, DescriptiveMetadataEditBundle bundle, AsyncCallback<Void> callback);

  void removeDescriptiveMetadataFile(String itemId, String descriptiveMetadataId, AsyncCallback<Void> callback);

  void createDescriptiveMetadataFile(String aipId, DescriptiveMetadataEditBundle newBundle,
    AsyncCallback<Void> asyncCallback);

  void createTransferredResourcesFolder(String parent, String folderName, AsyncCallback<String> callback);

  void removeTransferredResources(SelectedItems selected, AsyncCallback<Void> callback);

  void transferScanIsUpdating(AsyncCallback<Boolean> callback);

  void transferScanRequestUpdate(String transferredResourceUUID, AsyncCallback<Void> callback);

  void createJob(Job job, AsyncCallback<Job> callback);

  void getPluginsInfo(List<PluginType> type, AsyncCallback<List<PluginInfo>> callback);

  void getCreateIngestProcessBundle(AsyncCallback<CreateIngestJobBundle> callback);

  void retrieveJobBundle(String jobId, AsyncCallback<JobBundle> callback);

  void getViewersProperties(AsyncCallback<Viewers> callback);

  void getSupportedMetadata(String locale, AsyncCallback<List<SupportedMetadataTypeBundle>> callback);

  void isCookiesMessageActive(AsyncCallback<Boolean> callback);

  void isRegisterActive(AsyncCallback<Boolean> callback);

  void getGoogleAnalyticsAccount(AsyncCallback<String> callback);

  void getGoogleReCAPTCHAAccount(AsyncCallback<String> callback);

  void retrievePreservationEventViewBundle(String eventId, AsyncCallback<PreservationEventViewBundle> asyncCallback);

  void getDescriptiveMetadataVersionsBundle(String aipId, String descriptiveMetadataId, String localeString,
    AsyncCallback<DescriptiveMetadataVersionsBundle> callback);

  void revertDescriptiveMetadataVersion(String aipId, String descriptiveMetadataId, String versionId,
    AsyncCallback<Void> callback);

  void removeDescriptiveMetadataVersion(String aipId, String descriptiveMetadataId, String versionId,
    AsyncCallback<Void> callback);

  <T extends IsIndexed> void find(String classNameToReturn, Filter filter, Sorter sorter, Sublist sublist,
    Facets facets, String localeString, AsyncCallback<IndexResult<T>> callback);

  void delete(String classNameToReturn, SelectedItems ids, AsyncCallback<Void> callback);

  void count(String classNameToReturn, Filter filter, AsyncCallback<Long> callback);

  <T extends IsIndexed> void retrieve(String classNameToReturn, String id, AsyncCallback<T> callback);

  void suggest(String classNameToReturn, String field, String query, AsyncCallback<List<String>> callback);

  void updateAIPPermissions(String aipId, Permissions permissions, boolean recursive, AsyncCallback<Void> callback);

  void addRisk(Risk risk, AsyncCallback<Risk> asyncCallback);

  void modifyRisk(Risk risk, String message, AsyncCallback<Void> asyncCallback);

  void addAgent(Agent agent, AsyncCallback<Agent> asyncCallback);

  void modifyAgent(Agent agent, AsyncCallback<Void> asyncCallback);

  void addFormat(Format format, AsyncCallback<Format> asyncCallback);

  void modifyFormat(Format format, AsyncCallback<Void> asyncCallback);

  void retrieveFormats(String agentId, AsyncCallback<List<Format>> asyncCallback);

  void retrieveRequiredAgents(String agentId, AsyncCallback<List<Agent>> asyncCallback);

  void revertRiskVersion(String riskId, String versionId, String message, AsyncCallback<Void> callback);

  void removeRiskVersion(String riskId, String versionId, AsyncCallback<Void> callback);

  void retrieveRiskVersions(String riskId, AsyncCallback<RiskVersionsBundle> callback);

  void hasRiskVersions(String id, AsyncCallback<Boolean> asyncCallback);

  void retrieveRiskVersion(String riskId, String selectedVersion, AsyncCallback<Risk> asyncCallback);

  void retrieveShowMitigationTerms(int preMitigationProbability, int preMitigationImpact, int posMitigationProbability,
    int posMitigationImpact, AsyncCallback<RiskMitigationBundle> asyncCallback);

  void retrieveMitigationSeverityLimits(AsyncCallback<List<String>> asyncCallback);

  void retrieveAllMitigationProperties(AsyncCallback<MitigationPropertiesBundle> asyncCallback);

  void deleteRisk(SelectedItems selected, AsyncCallback<Void> asyncCallback);

  void deleteAgent(SelectedItems selected, AsyncCallback<Void> asyncCallback);

  void deleteFormat(SelectedItems selected, AsyncCallback<Void> asyncCallback);

  void createProcess(String jobName, SelectedItems selected, String id, Map<String, String> value,
    AsyncCallback<Job> asyncCallback);

  void getObjectRiskSize(String aipId, AsyncCallback<Integer> asyncCallback);

  void getRiskOnAIP(String aipId, AsyncCallback<List<String>> asyncCallback);
}
