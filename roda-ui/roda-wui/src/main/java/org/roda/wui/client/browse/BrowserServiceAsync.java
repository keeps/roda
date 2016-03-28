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
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.wui.client.common.lists.SelectedItems;
import org.roda.wui.client.ingest.process.CreateIngestJobBundle;
import org.roda.wui.client.ingest.process.JobBundle;
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

  void createAIP(String parentId, AsyncCallback<String> callback);

  void removeAIP(String aipId, AsyncCallback<String> callback);

  void updateDescriptiveMetadataFile(String aipId, DescriptiveMetadataEditBundle bundle, AsyncCallback<Void> callback);

  void removeDescriptiveMetadataFile(String itemId, String descriptiveMetadataId, AsyncCallback<Void> callback);

  void createDescriptiveMetadataFile(String aipId, DescriptiveMetadataEditBundle newBundle,
    AsyncCallback<Void> asyncCallback);

  void createTransferredResourcesFolder(String parent, String folderName, AsyncCallback<String> callback);

  void removeTransferredResources(SelectedItems<TransferredResource> selected, AsyncCallback<Void> callback);

  void isTransferFullyInitialized(AsyncCallback<Boolean> callback);

  void createJob(Job job, AsyncCallback<Job> callback);

  void getPluginsInfo(PluginType type, AsyncCallback<List<PluginInfo>> callback);

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

  void count(String classNameToReturn, Filter filter, AsyncCallback<Long> callback);

  <T extends IsIndexed> void retrieve(String classNameToReturn, String id, AsyncCallback<T> callback);

  void suggest(String classNameToReturn, String field, String query, AsyncCallback<List<String>> callback);

  void createIngestProcess(String jobName, SelectedItems<TransferredResource> selected, String plugin,
    Map<String, String> parameters, AsyncCallback<Job> asyncCallback);

  void updateAIPPermssions(String aipId, Permissions permissions, AsyncCallback<Void> callback);

}
