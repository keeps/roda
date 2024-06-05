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
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
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
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.central.DistributedInstances;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;
import org.roda.wui.client.browse.bundle.BrowseDipBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataEditBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataVersionsBundle;
import org.roda.wui.client.browse.bundle.SupportedMetadataTypeBundle;

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

  Set<Pair<String, String>> retrieveDropdownPluginItems(String parameterId, String localeString);

  Set<ConversionProfile> retrieveConversionProfilePluginItems(String pluginId, String repOrDip, String localeString);

  List<SupportedMetadataTypeBundle> retrieveSupportedMetadata(String aipId, String representationUUID, String locale)
    throws RODAException;

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

  <T extends IsIndexed> T retrieve(String classNameToReturn, String id, List<String> fieldsToReturn)
    throws RODAException;

  <T extends IsIndexed> List<T> retrieve(String classNameToReturn, SelectedItems<T> selectedItems,
    List<String> fieldsToReturn)
    throws GenericException, AuthorizationDeniedException, NotFoundException, RequestNotValidException;

  Job updateAIPPermissions(SelectedItems<IndexedAIP> aips, Permissions permissions, String details, boolean recursive)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException,
    JobAlreadyStartedException;

  Job updateDIPPermissions(SelectedItems<IndexedDIP> dips, Permissions permissions, String details)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException;

  Job appraisal(SelectedItems<IndexedAIP> selected, boolean accept, String rejectReason)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException;

  Job changeAIPType(SelectedItems<IndexedAIP> selectedAIP, String newType, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException;

  BrowseDipBundle retrieveDipBundle(String dipUUID, String dipFileUUID, String localeString)
    throws RequestNotValidException, AuthorizationDeniedException, GenericException, NotFoundException;

  Job deleteDIPs(SelectedItems<IndexedDIP> dips, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  boolean hasDocumentation(String aipId)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException;

  boolean hasSubmissions(String aipId) throws AuthorizationDeniedException, RequestNotValidException, GenericException;

  boolean showDIPEmbedded();

  Notification acknowledgeNotification(String notificationId, String ackToken)
    throws GenericException, NotFoundException, AuthorizationDeniedException;

  int getExportLimit();

  Pair<Boolean, List<String>> retrieveAIPTypeOptions(String locale);

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

  AccessKey createAccessKey(AccessKey accessKey) throws AuthorizationDeniedException, AlreadyExistsException,
    NotFoundException, GenericException, RequestNotValidException, IOException;

  AccessKeys listAccessKey()
    throws AuthorizationDeniedException, IOException, GenericException, RequestNotValidException;

  AccessKey retrieveAccessKey(String accessKeyId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  void deleteAccessKey(String accessKeyId)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException;

  AccessKey updateAccessKey(AccessKey accessKey)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException;

  AccessKeys listAccessKeyByUser(String userId)
    throws AuthorizationDeniedException, IOException, GenericException, RequestNotValidException;

  void deactivateUserAccessKeys(String userId)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException;

  void deleteUserAccessKeys(String userId)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException;

  AccessKey regenerateAccessKey(AccessKey accessKey) throws AuthorizationDeniedException, RequestNotValidException,
    NotFoundException, GenericException, AuthenticationDeniedException;

  AccessKey revokeAccessKey(AccessKey accessKey)
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

  boolean requestAIPLock(String aipId);

  void releaseAIPLock(String aipId);

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
