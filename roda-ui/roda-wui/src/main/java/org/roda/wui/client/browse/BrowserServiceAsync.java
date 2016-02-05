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

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobReport;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.wui.client.ingest.process.CreateIngestJobBundle;
import org.roda.wui.client.ingest.process.JobBundle;
import org.roda.wui.client.search.SearchField;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Luis Faria
 * 
 */
public interface BrowserServiceAsync {

  void countDescriptiveMetadata(Filter filter, AsyncCallback<Long> callback);

  void findDescriptiveMetadata(Filter filter, Sorter sorter, Sublist sublist, Facets facets, String locale,
    AsyncCallback<IndexResult<IndexedAIP>> callback);

  void getItemBundle(String aipId, String localeString, AsyncCallback<BrowseItemBundle> callback);

  void getDescriptiveMetadataEditBundle(String aipId, String descId,
    AsyncCallback<DescriptiveMetadataEditBundle> callback);

  /**
   * Get simple description object
   * 
   * @param pid
   *          the object id
   * @return {@link SimpleDescriptionObject}
   * @throws RODAException
   */
  public void getIndexedAIP(String pid, AsyncCallback<IndexedAIP> callback);

  void getSearchFields(String locale, AsyncCallback<List<SearchField>> callback);

  void moveInHierarchy(String aipId, String parentId, AsyncCallback<IndexedAIP> callback);

  void createAIP(String parentId, AsyncCallback<String> callback);

  void removeAIP(String aipId, AsyncCallback<String> callback);

  void updateDescriptiveMetadataFile(String aipId, DescriptiveMetadataEditBundle bundle, AsyncCallback<Void> callback);

  void removeDescriptiveMetadataFile(String itemId, String descriptiveMetadataId, AsyncCallback<Void> callback);

  void createDescriptiveMetadataFile(String aipId, DescriptiveMetadataEditBundle newBundle,
    AsyncCallback<Void> asyncCallback);

  void findTransferredResources(Filter filter, Sorter sorter, Sublist sublist, Facets facets,
    AsyncCallback<IndexResult<TransferredResource>> callback);

  void retrieveTransferredResource(String transferredResourceId, AsyncCallback<TransferredResource> callback);

  void createTransferredResourcesFolder(String parent, String folderName, AsyncCallback<String> callback);

  void removeTransferredResources(List<String> ids, AsyncCallback<Void> callback);

  void isTransferFullyInitialized(AsyncCallback<Boolean> callback);

  void getRepresentationFiles(Filter filter, Sorter sorter, Sublist sublist, Facets facets, String localeString,
    AsyncCallback<IndexResult<IndexedFile>> callback);

  void findJobs(Filter filter, Sorter sorter, Sublist sublist, Facets facets, String localeString,
    AsyncCallback<IndexResult<Job>> callback);

  void retrieveJob(String jobId, AsyncCallback<Job> callback);

  void createJob(Job job, AsyncCallback<Job> callback);

  void getPluginsInfo(PluginType type, AsyncCallback<List<PluginInfo>> callback);

  void getCreateIngestProcessBundle(AsyncCallback<CreateIngestJobBundle> callback);

  void retrieveJobBundle(String jobId, AsyncCallback<JobBundle> callback);

  void findJobReports(Filter filter, Sorter sorter, Sublist sublist, Facets facets,
    AsyncCallback<IndexResult<JobReport>> callback);

  void retrieveJobReport(String jobReportId, AsyncCallback<JobReport> callback);

  void getViewersProperties(AsyncCallback<Viewers> callback);

  void getSupportedMetadata(String locale, AsyncCallback<List<SupportedMetadataTypeBundle>> callback);

  void findIndexedPreservationEvent(Filter filter, Sorter sorter, Sublist sublist, Facets facets,
    AsyncCallback<IndexResult<IndexedPreservationEvent>> callback);

  void retrieveIndexedPreservationEvent(String indexedPreservationEventId,
    AsyncCallback<IndexedPreservationEvent> callback);

  void getGoogleAnalyticsAccount(AsyncCallback<String> callback);

  void getGoogleReCAPTCHAAccount(AsyncCallback<String> callback);
  
  void isRegisterActive(AsyncCallback<Boolean> callback);
}
