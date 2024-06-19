/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import java.io.IOException;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.accessKey.AccessKey;
import org.roda.core.data.v2.accessKey.AccessKeys;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.central.DistributedInstances;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;

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

  BrowseAIPBundle retrieveBrowseAIPBundle(String aipId, String localeString, List<String> aipFieldsToReturn)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  DistributedInstance createDistributedInstance(DistributedInstance distributedInstance)
    throws AuthorizationDeniedException, AlreadyExistsException, NotFoundException, GenericException,
    RequestNotValidException, IOException, IllegalOperationException;

  DistributedInstances listDistributedInstances()
    throws AuthorizationDeniedException, IOException, GenericException, RequestNotValidException;

  DistributedInstance retrieveDistributedInstance(String distributedInstancesId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  void deleteDistributedInstance(String distributedInstancesId)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException;

  DistributedInstance updateDistributedInstance(DistributedInstance distributedInstance)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException;

  void createLocalInstance(LocalInstance localInstance) throws AuthorizationDeniedException, GenericException;

  LocalInstance retrieveLocalInstance() throws AuthorizationDeniedException, GenericException;

  void deleteLocalInstanceConfiguration() throws AuthorizationDeniedException, GenericException;

  void updateLocalInstanceConfiguration(LocalInstance localInstance)
    throws AuthorizationDeniedException, GenericException;

  List<String> testLocalInstanceConfiguration(LocalInstance localInstance)
    throws AuthorizationDeniedException, GenericException, AuthenticationDeniedException;

  LocalInstance subscribeLocalInstance(LocalInstance localInstance) throws AuthorizationDeniedException,
    GenericException, AuthenticationDeniedException, RequestNotValidException, NotFoundException;

  Job synchronizeBundle(LocalInstance localInstance)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  void removeLocalConfiguration(LocalInstance localInstance)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException;

  String getCrontabValue(String locale);

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
}
