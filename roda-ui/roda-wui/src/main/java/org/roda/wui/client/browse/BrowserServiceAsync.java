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

import org.roda.core.data.v2.accessKey.AccessKey;
import org.roda.core.data.v2.accessKey.AccessKeys;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.central.DistributedInstances;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Luis Faria
 *
 */
public interface BrowserServiceAsync {

  void retrieveBrowseAIPBundle(String aipId, String localeString, List<String> aipFieldsToReturn,
    AsyncCallback<BrowseAIPBundle> callback);

  void createDistributedInstance(DistributedInstance distributedInstance, AsyncCallback<DistributedInstance> async);

  void listDistributedInstances(AsyncCallback<DistributedInstances> async);

  void retrieveDistributedInstance(String distributedInstanceId, AsyncCallback<DistributedInstance> async);

  void updateDistributedInstance(DistributedInstance distributedInstance, AsyncCallback<DistributedInstance> async);

  void deleteDistributedInstance(String distributedInstanceId, AsyncCallback<Void> async);

  void createLocalInstance(LocalInstance localInstance, AsyncCallback async);

  void retrieveLocalInstance(AsyncCallback async);

  void deleteLocalInstanceConfiguration(AsyncCallback async);

  void updateLocalInstanceConfiguration(LocalInstance localInstance, AsyncCallback async);

  void testLocalInstanceConfiguration(LocalInstance localInstance, AsyncCallback<List<String>> async);

  void subscribeLocalInstance(LocalInstance localInstance, AsyncCallback<LocalInstance> async);

  void synchronizeBundle(LocalInstance localInstance, AsyncCallback<Job> async);

  void removeLocalConfiguration(LocalInstance localInstance, AsyncCallback<Job> async);

  void getCrontabValue(String localeName, AsyncCallback<String> async);
}
