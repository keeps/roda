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
import java.util.Set;

import org.roda.core.data.v2.accessKey.AccessKey;
import org.roda.core.data.v2.accessKey.AccessKeys;
import org.roda.core.data.v2.common.ConversionProfile;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.central.DistributedInstances;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;
import org.roda.wui.client.browse.bundle.BrowseDipBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataEditBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataVersionsBundle;
import org.roda.wui.client.browse.bundle.SupportedMetadataTypeBundle;

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

  void deleteAIP(SelectedItems<IndexedAIP> aips, String details, AsyncCallback<Job> callback);

  void updateDescriptiveMetadataFile(String aipId, String representationId, DescriptiveMetadataEditBundle bundle,
    AsyncCallback<Void> callback);

  void deleteDescriptiveMetadataFile(String aipId, String representationId, String descriptiveMetadataId,
    AsyncCallback<Void> callback);

  void createDescriptiveMetadataFile(String aipId, String representationId, DescriptiveMetadataEditBundle newBundle,
    AsyncCallback<Void> asyncCallback);

  void retrieveDescriptiveMetadataPreview(SupportedMetadataTypeBundle bundle, AsyncCallback<String> async);

  void retrieveDropdownPluginItems(String parameterId, String localeString,
    AsyncCallback<Set<Pair<String, String>>> asyncCallback);

  void retrieveConversionProfilePluginItems(String pluginId, String repOrDip, String localeString,
    AsyncCallback<Set<ConversionProfile>> asyncCallback);

  void retrieveSupportedMetadata(String aipId, String representationUUID, String locale,
    AsyncCallback<List<SupportedMetadataTypeBundle>> callback);

  void retrieveDescriptiveMetadataVersionsBundle(String aipId, String representationId, String descriptiveMetadataId,
    String localeString, AsyncCallback<DescriptiveMetadataVersionsBundle> callback);

  void revertDescriptiveMetadataVersion(String aipId, String representationId, String descriptiveMetadataId,
    String versionId, AsyncCallback<Void> callback);

  void deleteDescriptiveMetadataVersion(String aipId, String representationId, String descriptiveMetadataId,
    String versionId, AsyncCallback<Void> callback);

  <T extends IsIndexed> void find(String classNameToReturn, Filter filter, Sorter sorter, Sublist sublist,
    Facets facets, String localeString, boolean justActive, List<String> fieldsToReturn,
    AsyncCallback<IndexResult<T>> callback);

  <T extends IsIndexed> void retrieve(String classNameToReturn, String id, List<String> fieldsToReturn,
    AsyncCallback<T> callback);

  <T extends IsIndexed> void retrieve(String classNameToReturn, SelectedItems<T> selectedItems,
    List<String> fieldsToReturn, AsyncCallback<List<T>> asyncCallback);

  void updateAIPPermissions(SelectedItems<IndexedAIP> aips, Permissions permissions, String details, boolean recursive,
    AsyncCallback<Job> callback);

  void updateDIPPermissions(SelectedItems<IndexedDIP> dips, Permissions permissions, String details,
    AsyncCallback<Job> callback);

  void appraisal(SelectedItems<IndexedAIP> selected, boolean accept, String rejectReason, AsyncCallback<Job> callback);

  void changeAIPType(SelectedItems<IndexedAIP> selectedAIP, String newType, String details,
    AsyncCallback<Job> loadingAsyncCallback);

  void retrieveDipBundle(String dipUUID, String dipFileUUID, String localeString,
    AsyncCallback<BrowseDipBundle> callback);

  void deleteDIPs(SelectedItems<IndexedDIP> dips, String details, AsyncCallback<Job> async);

  void hasDocumentation(String aipId, AsyncCallback<Boolean> asyncCallback);

  void hasSubmissions(String aipId, AsyncCallback<Boolean> asyncCallback);

  void showDIPEmbedded(AsyncCallback<Boolean> asyncCallback);

  void acknowledgeNotification(String notificationId, String ackToken, AsyncCallback<Notification> asyncCallback);

  void getExportLimit(AsyncCallback<Integer> asyncCallback);

  void retrieveAIPTypeOptions(String locale, AsyncCallback<Pair<Boolean, List<String>>> asyncCallback);

  void createDistributedInstance(DistributedInstance distributedInstance, AsyncCallback<DistributedInstance> async);

  void listDistributedInstances(AsyncCallback<DistributedInstances> async);

  void retrieveDistributedInstance(String distributedInstanceId, AsyncCallback<DistributedInstance> async);

  void updateDistributedInstance(DistributedInstance distributedInstance, AsyncCallback<DistributedInstance> async);

  void deleteDistributedInstance(String distributedInstanceId, AsyncCallback<Void> async);

  void createAccessKey(AccessKey accessKey, AsyncCallback<AccessKey> async);

  void listAccessKey(AsyncCallback<AccessKeys> async);

  void retrieveAccessKey(String accessKeyId, AsyncCallback<AccessKey> async);

  void deleteAccessKey(String accessKeyId, AsyncCallback<Void> async);

  void updateAccessKey(AccessKey accessKey, AsyncCallback<AccessKey> async);

  void listAccessKeyByUser(String userId, AsyncCallback<AccessKeys> async);

  void deactivateUserAccessKeys(String userId, AsyncCallback<Void> async);

  void deleteUserAccessKeys(String userId, AsyncCallback<Void> async);

  void regenerateAccessKey(AccessKey accessKey, AsyncCallback<AccessKey> async);

  void revokeAccessKey(AccessKey accessKey, AsyncCallback<AccessKey> async);

  void createLocalInstance(LocalInstance localInstance, AsyncCallback async);

  void retrieveLocalInstance(AsyncCallback async);

  void deleteLocalInstanceConfiguration(AsyncCallback async);

  void updateLocalInstanceConfiguration(LocalInstance localInstance, AsyncCallback async);

  void testLocalInstanceConfiguration(LocalInstance localInstance, AsyncCallback<List<String>> async);

  void subscribeLocalInstance(LocalInstance localInstance, AsyncCallback<LocalInstance> async);

  void synchronizeBundle(LocalInstance localInstance, AsyncCallback<Job> async);

  void removeLocalConfiguration(LocalInstance localInstance, AsyncCallback<Job> async);

  void getCrontabValue(String localeName, AsyncCallback<String> async);

  void requestAIPLock(String aipId, AsyncCallback<Boolean> async);

  void releaseAIPLock(String aipId, AsyncCallback<Void> async);
}
