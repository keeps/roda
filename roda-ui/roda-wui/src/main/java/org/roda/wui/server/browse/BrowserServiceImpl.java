/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.server.browse;

import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.RodaUtils;
import org.roda.core.common.SelectedItemsUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.LockingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.accessKey.AccessKey;
import org.roda.core.data.v2.accessKey.AccessKeys;
import org.roda.core.data.v2.properties.ConversionProfile;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.FacetFieldResult;
import org.roda.core.data.v2.index.facet.FacetValue;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.central.DistributedInstances;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StringContentPayload;
import org.roda.wui.api.controllers.ApplicationAuth;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.controllers.RODAInstance;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataEditBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataVersionsBundle;
import org.roda.wui.client.browse.bundle.SupportedMetadataTypeBundle;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.server.ServerTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;

import it.burning.cron.CronExpressionDescriptor;

/**
 * Browser Service Implementation
 *
 * @author Luis Faria
 *
 */
public class BrowserServiceImpl extends RemoteServiceServlet implements BrowserService {

  static final String FONDLIST_PAGESIZE = "10";
  @Serial
  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = LoggerFactory.getLogger(BrowserServiceImpl.class);

  /**
   * Create a new BrowserService Implementation instance
   *
   */
  public BrowserServiceImpl() {
    // do nothing
  }

  @Override
  protected void doUnexpectedFailure(Throwable e) {
    LOGGER.error("Unexpected failure", e);
    super.doUnexpectedFailure(e);
  }

  @Override
  public BrowseAIPBundle retrieveBrowseAIPBundle(String aipId, String localeString, List<String> aipFieldsToReturn)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Locale locale = ServerTools.parseLocale(localeString);
    return Browser.retrieveBrowseAipBundle(user, aipId, locale, aipFieldsToReturn);
  }

  @Override
  public DescriptiveMetadataEditBundle retrieveDescriptiveMetadataEditBundle(String aipId, String representationId,
    String descId, String type, String version, String localeString)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Locale locale = ServerTools.parseLocale(localeString);
    return Browser.retrieveDescriptiveMetadataEditBundle(user, aipId, representationId, descId, type, version, locale);
  }

  @Override
  public DescriptiveMetadataEditBundle retrieveDescriptiveMetadataEditBundle(String aipId, String representationId,
    String descId, String localeString)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Locale locale = ServerTools.parseLocale(localeString);
    return Browser.retrieveDescriptiveMetadataEditBundle(user, aipId, representationId, descId, locale);
  }

  @Override
  public Job moveAIPInHierarchy(SelectedItems<IndexedAIP> selected, String parentId, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.moveAIPInHierarchy(user, selected, parentId, details);
  }

  @Override
  public String createAIP(String parentId, String type) throws AuthorizationDeniedException, GenericException,
    NotFoundException, RequestNotValidException, AlreadyExistsException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.createAIP(user, parentId, type).getId();

  }

  @Override
  public Job deleteAIP(SelectedItems<IndexedAIP> aips, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.deleteAIP(user, aips, details);
  }

  @Override
  public void createDescriptiveMetadataFile(String aipId, String representationId, DescriptiveMetadataEditBundle bundle)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException,
    AlreadyExistsException, ValidationException {
    User user = UserUtility.getUser(getThreadLocalRequest());

    // If the bundle has values from the form, we need to update the XML by
    // applying the values of the form to the raw template
    if (bundle.getValues() != null && !bundle.getValues().isEmpty()) {
      SupportedMetadataTypeBundle smtb = new SupportedMetadataTypeBundle(bundle.getId(), bundle.getType(),
        bundle.getVersion(), bundle.getId(), bundle.getRawTemplate(), bundle.getValues());
      bundle.setXml(Browser.retrieveDescriptiveMetadataPreview(user, smtb));
    }

    String metadataId = bundle.getId();
    String descriptiveMetadataType = bundle.getType();
    String descriptiveMetadataVersion = bundle.getVersion();
    ContentPayload payload = new StringContentPayload(bundle.getXml());

    Browser.createDescriptiveMetadataFile(user, aipId, representationId, metadataId, descriptiveMetadataType,
      descriptiveMetadataVersion, payload);
  }

  @Override
  public String retrieveDescriptiveMetadataPreview(SupportedMetadataTypeBundle bundle)
    throws AuthorizationDeniedException, GenericException, ValidationException, NotFoundException,
    RequestNotValidException {

    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.retrieveDescriptiveMetadataPreview(user, bundle);
  }

  @Override
  public void updateDescriptiveMetadataFile(String aipId, String representationId, DescriptiveMetadataEditBundle bundle)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException,
    ValidationException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    String metadataId = bundle.getId();
    String metadataType = bundle.getType();
    String metadataVersion = bundle.getVersion();
    ContentPayload payload = new StringContentPayload(bundle.getXml());

    Browser.updateDescriptiveMetadataFile(user, aipId, representationId, metadataId, metadataType, metadataVersion,
      payload);

  }

  @Override
  public void deleteDescriptiveMetadataFile(String aipId, String representationId, String descriptiveMetadataId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Browser.deleteDescriptiveMetadataFile(user, aipId, representationId, descriptiveMetadataId);
  }

  @Override
  public List<SupportedMetadataTypeBundle> retrieveSupportedMetadata(String aipId, String representationId,
    String localeString) throws RODAException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Locale locale = ServerTools.parseLocale(localeString);
    return Browser.retrieveSupportedMetadata(user, aipId, representationId, locale);
  }

  @Override
  public DescriptiveMetadataVersionsBundle retrieveDescriptiveMetadataVersionsBundle(String aipId,
    String representationId, String descriptiveMetadataId, String localeString)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Locale locale = ServerTools.parseLocale(localeString);
    return Browser.retrieveDescriptiveMetadataVersionsBundle(user, aipId, representationId, descriptiveMetadataId,
      locale);
  }

  @Override
  public void revertDescriptiveMetadataVersion(String aipId, String representationId, String descriptiveMetadataId,
    String versionId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Browser.revertDescriptiveMetadataVersion(user, aipId, representationId, descriptiveMetadataId, versionId);
  }

  @Override
  public void deleteDescriptiveMetadataVersion(String aipId, String representationId, String descriptiveMetadataId,
    String versionId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Browser.deleteDescriptiveMetadataVersion(user, aipId, representationId, descriptiveMetadataId, versionId);
  }

  @Override
  public Job updateAIPPermissions(SelectedItems<IndexedAIP> aips, Permissions permissions, String details,
    boolean recursive)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.updateAIPPermissions(user, aips, permissions, details, recursive);
  }

  private <T extends IsIndexed> SelectedItems<T> getAllItemsByClass(String selectedClass) {
    if (selectedClass == null || Void.class.getName().equals(selectedClass)) {
      return new SelectedItemsNone<>();
    } else {
      return new SelectedItemsAll<>(selectedClass);
    }
  }

  @Override
  public Job appraisal(SelectedItems<IndexedAIP> selected, boolean accept, String rejectReason)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.appraisal(user, selected, accept, rejectReason);
  }

  @Override
  public Job changeAIPType(SelectedItems<IndexedAIP> selected, String newType, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.changeAIPType(user, selected, newType, details);
  }

  @Override
  public boolean hasDocumentation(String aipId)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.hasDocumentation(user, aipId);
  }

  @Override
  public boolean hasSubmissions(String aipId)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.hasSubmissions(user, aipId);
  }

  @Override
  public int getExportLimit() {
    return RodaCoreFactory.getRodaConfiguration().getInt("ui.list.export_limit",
      RodaConstants.DEFAULT_LIST_EXPORT_LIMIT);
  }

  @Override
  public Pair<Boolean, List<String>> retrieveAIPTypeOptions(String locale) {
    List<String> types = new ArrayList<>();
    boolean isControlled = RodaCoreFactory.getRodaConfiguration().getBoolean("core.aip_type.controlled_vocabulary",
      false);

    if (isControlled) {
      types = RodaCoreFactory.getRodaConfigurationAsList("core.aip_type.value");
    } else {

      Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.AIP_TYPE));
      IndexResult<IndexedAIP> result = null;

      List<FacetFieldResult> facetResults = result.getFacetResults();
      for (FacetValue facetValue : facetResults.get(0).getValues()) {
        types.add(facetValue.getValue());
      }

    }

    return Pair.of(isControlled, types);
  }

  @Override
  public DistributedInstance createDistributedInstance(DistributedInstance distributedInstance)
    throws AuthorizationDeniedException, AlreadyExistsException, NotFoundException, GenericException,
    RequestNotValidException, IllegalOperationException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return RODAInstance.createDistributedInstance(user, distributedInstance);
  }

  @Override
  public DistributedInstances listDistributedInstances()
    throws AuthorizationDeniedException, IOException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return RODAInstance.listDistributedInstances(user);
  }

  @Override
  public DistributedInstance retrieveDistributedInstance(String distributedInstanceId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return RODAInstance.retrieveDistributedInstance(user, distributedInstanceId);
  }

  @Override
  public DistributedInstance updateDistributedInstance(DistributedInstance distributedInstance)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return RODAInstance.updateDistributedInstance(user, distributedInstance);
  }

  @Override
  public void deleteDistributedInstance(String distributedInstanceId)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    RODAInstance.deleteDistributedInstance(user, distributedInstanceId);
  }

  @Override
  public AccessKeys listAccessKey()
    throws AuthorizationDeniedException, IOException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return ApplicationAuth.listAccessKey(user);
  }

  @Override
  public AccessKey retrieveAccessKey(String accessKeyId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return ApplicationAuth.retrieveAccessKey(user, accessKeyId);
  }

  @Override
  public void createLocalInstance(LocalInstance localInstance) throws AuthorizationDeniedException, GenericException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    RODAInstance.createLocalInstance(user, localInstance);
  }

  @Override
  public LocalInstance retrieveLocalInstance() throws AuthorizationDeniedException, GenericException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return RODAInstance.retrieveLocalInstance(user);
  }

  @Override
  public void deleteLocalInstanceConfiguration() throws AuthorizationDeniedException, GenericException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    RODAInstance.deleteLocalInstanceConfiguration(user);
  }

  @Override
  public void updateLocalInstanceConfiguration(LocalInstance localInstance)
    throws AuthorizationDeniedException, GenericException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    RODAInstance.updateLocalInstanceConfiguration(user, localInstance);
  }

  @Override
  public List<String> testLocalInstanceConfiguration(LocalInstance localInstance)
    throws AuthorizationDeniedException, GenericException, AuthenticationDeniedException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return RODAInstance.testLocalInstanceConfiguration(user, localInstance);
  }

  @Override
  public LocalInstance subscribeLocalInstance(LocalInstance localInstance) throws AuthorizationDeniedException,
    GenericException, AuthenticationDeniedException, RequestNotValidException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return RODAInstance.subscribeLocalInstance(user, localInstance);
  }

  @Override
  public Job synchronizeBundle(LocalInstance localInstance)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return RODAInstance.synchronizeBundle(user, localInstance);
  }

  public void removeLocalConfiguration(LocalInstance localInstance)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    RODAInstance.removeLocalConfiguration(user, localInstance);
  }

  public String getCrontabValue(String locale) {
    String syncSchedule = RodaCoreFactory.getRodaConfigurationAsString("core.synchronization.scheduleInfo");
    String description = null;
    if (StringUtils.isNotBlank(syncSchedule)) {
      CronExpressionDescriptor.setDefaultLocale(locale.split("_")[0]);
      description = CronExpressionDescriptor.getDescription(syncSchedule);
    }
    return description;
  }

  public boolean requestAIPLock(String aipId) {
    boolean lockEnabled = RodaCoreFactory.getRodaConfiguration().getBoolean("core.aip.lockToEdit", false);

    if (!lockEnabled) {
      return true;
    }

    User user = UserUtility.getUser(getThreadLocalRequest());
    try {
      PluginHelper.tryLock(Collections.singletonList(aipId), user.getUUID());
    } catch (LockingException e) {
      return false;
    }
    return true;
  }

  public void releaseAIPLock(String aipId) {
    boolean lockEnabled = RodaCoreFactory.getRodaConfiguration().getBoolean("core.aip.lockToEdit", false);

    if (lockEnabled) {
      User user = UserUtility.getUser(getThreadLocalRequest());
      PluginHelper.releaseObjectLock(aipId, user.getUUID());
    }
  }
}
