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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.Messages;
import org.roda.core.common.SelectedItemsUtils;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.JobStateNotPendingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.accessKey.AccessKey;
import org.roda.core.data.v2.accessKey.AccessKeys;
import org.roda.core.data.v2.common.ConversionProfile;
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
import org.roda.core.data.v2.ip.disposal.DisposalHolds;
import org.roda.core.data.v2.ip.disposal.DisposalRule;
import org.roda.core.data.v2.ip.disposal.DisposalRules;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.DisposalSchedules;
import org.roda.core.data.v2.ip.disposal.aipMetadata.DisposalHoldAIPMetadata;
import org.roda.core.data.v2.ip.disposal.aipMetadata.DisposalTransitiveHoldAIPMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobParallelism;
import org.roda.core.data.v2.jobs.JobPriority;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.central.DistributedInstances;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.schema.SolrCollectionRegistry;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.util.IdUtils;
import org.roda.wui.api.controllers.Aips;
import org.roda.wui.api.controllers.ApplicationAuth;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.controllers.DescriptiveMetadata;
import org.roda.wui.api.controllers.Dips;
import org.roda.wui.api.controllers.Disposals;
import org.roda.wui.api.controllers.Files;
import org.roda.wui.api.controllers.JobBundles;
import org.roda.wui.api.controllers.Jobs;
import org.roda.wui.api.controllers.Plugins;
import org.roda.wui.api.controllers.RODAInstance;
import org.roda.wui.api.controllers.RepresentationInformations;
import org.roda.wui.api.controllers.RepresentationTypes;
import org.roda.wui.api.controllers.Representations;
import org.roda.wui.api.controllers.Risks;
import org.roda.wui.api.controllers.TransferredResources;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.Viewers;
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
import org.roda.wui.common.server.ServerTools;
import org.roda_project.commons_ip.model.RepresentationContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;

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
  public Map<String, List<String>> retrieveSharedProperties(String localeString) {
    return Browser.retrieveSharedProperties(localeString);
  }

  @Override
  public DisposalRule createDisposalRule(DisposalRule rule) throws AuthorizationDeniedException, AlreadyExistsException,
    NotFoundException, GenericException, RequestNotValidException, IOException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.createDisposalRule(user, rule);
  }

  @Override
  public DisposalRule retrieveDisposalRule(String disposalRuleId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.retrieveDisposalRule(user, disposalRuleId);
  }

  @Override
  public DisposalRules listDisposalRules()
    throws AuthorizationDeniedException, IOException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.listDisposalRules(user);
  }

  @Override
  public DisposalRule updateDisposalRule(DisposalRule rule)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.updateDisposalRule(user, rule);
  }

  @Override
  public void updateDisposalRules(DisposalRules rules)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Disposals.updateDisposalRules(user, rules);
  }

  @Override
  public void deleteDisposalRule(String disposalRuleId)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Disposals.deleteDisposalRule(user, disposalRuleId);
  }

  public Job applyDisposalRules(boolean applyToManuallyInclusive)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.applyDisposalRules(user, applyToManuallyInclusive);
  }

  @Override
  public DisposalSchedule createDisposalSchedule(DisposalSchedule schedule) throws AuthorizationDeniedException,
    AlreadyExistsException, NotFoundException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.createDisposalSchedule(user, schedule);
  }

  @Override
  public DisposalSchedule retrieveDisposalSchedule(String disposalScheduleId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.retrieveDisposalSchedule(user, disposalScheduleId);
  }

  @Override
  public DisposalSchedules listDisposalSchedules()
    throws AuthorizationDeniedException, IOException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.listDisposalSchedules(user);
  }

  @Override
  public DisposalSchedule updateDisposalSchedule(DisposalSchedule schedule) throws AuthorizationDeniedException,
    NotFoundException, GenericException, RequestNotValidException, IllegalOperationException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.updateDisposalSchedule(user, schedule);
  }

  @Override
  public void deleteDisposalSchedule(String disposalScheduleId) throws NotFoundException, AuthorizationDeniedException,
    IllegalOperationException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Disposals.deleteDisposalSchedule(user, disposalScheduleId);
  }

  @Override
  public DisposalHold createDisposalHold(DisposalHold hold) throws AuthorizationDeniedException, AlreadyExistsException,
    NotFoundException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.createDisposalHold(user, hold);
  }

  @Override
  public DisposalHold retrieveDisposalHold(String disposalHoldId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.retrieveDisposalHold(user, disposalHoldId);
  }

  @Override
  public DisposalHolds listDisposalHolds()
    throws AuthorizationDeniedException, IOException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.listDisposalHolds(user);
  }

  @Override
  public DisposalHold updateDisposalHold(DisposalHold hold) throws AuthorizationDeniedException, NotFoundException,
    GenericException, RequestNotValidException, IllegalOperationException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.updateDisposalHold(user, hold);
  }

  @Override
  public void deleteDisposalHold(String disposalHoldId) throws NotFoundException, AuthorizationDeniedException,
    IllegalOperationException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Disposals.deleteDisposalHold(user, disposalHoldId);
  }

  @Override
  public BrowseAIPBundle retrieveBrowseAIPBundle(String aipId, String localeString, List<String> aipFieldsToReturn)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Locale locale = ServerTools.parseLocale(localeString);
    return Browser.retrieveBrowseAipBundle(user, aipId, locale, aipFieldsToReturn);
  }

  @Override
  public BrowseRepresentationBundle retrieveBrowseRepresentationBundle(String aipId, String representationId,
    String localeString, List<String> representationFieldsToReturn)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Locale locale = ServerTools.parseLocale(localeString);
    return Browser.retrieveBrowseRepresentationBundle(user, aipId, representationId, locale,
      representationFieldsToReturn);
  }

  @Override
  public BrowseFileBundle retrieveBrowseFileBundle(String aipId, String representationId, List<String> filePath,
    String fileId, List<String> fileFieldsToReturn)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.retrieveBrowseFileBundle(user, aipId, representationId, filePath, fileId, fileFieldsToReturn);
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
  public <T extends IsIndexed> IndexResult<T> find(String classNameToReturn, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets, String localeString, boolean justActive, List<String> fieldsToReturn)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.find(classNameToReturn, filter, sorter, user, sublist, facets, localeString, justActive, fieldsToReturn);
  }

  @Override
  public <T extends IsIndexed> Long count(String classNameToReturn, Filter filter, boolean justActive)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Class<T> classToReturn = SelectedItemsUtils.parseClass(classNameToReturn);
    return Browser.count(user, classToReturn, filter, justActive);
  }

  @Override
  public <T extends IsIndexed> T retrieve(String classNameToReturn, String id, List<String> fieldsToReturn)
    throws RODAException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Class<T> classToReturn = SelectedItemsUtils.parseClass(classNameToReturn);
    return Browser.retrieve(user, classToReturn, id, fieldsToReturn);
  }

  @Override
  public <T extends IsIndexed> List<T> retrieve(String classNameToReturn, SelectedItems<T> selectedItems,
    List<String> fieldsToReturn)
    throws GenericException, AuthorizationDeniedException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Class<T> classToReturn = SelectedItemsUtils.parseClass(classNameToReturn);
    return Browser.retrieve(user, classToReturn, selectedItems, fieldsToReturn);
  }

  @Override
  public <T extends IsIndexed> void delete(String classNameToReturn, SelectedItems<T> ids)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Class<T> classToReturn = SelectedItemsUtils.parseClass(classNameToReturn);
    Browser.delete(user, classToReturn, ids);
  }

  @Override
  public <T extends IsIndexed> List<String> suggest(String classNameToReturn, String field, String query,
    boolean allowPartial) throws AuthorizationDeniedException, GenericException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Class<T> classToReturn = SelectedItemsUtils.parseClass(classNameToReturn);
    return Browser.suggest(user, classToReturn, field, query, allowPartial);
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
    return Aips.createAIP(user, parentId, type).getId();
  }

  @Override
  public Job deleteAIP(SelectedItems<IndexedAIP> aips, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Aips.deleteAIP(user, aips, details);
  }

  @Override
  public Job deleteRepresentation(SelectedItems<IndexedRepresentation> representations, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Representations.deleteRepresentation(user, representations, details);
  }

  @Override
  public Job deleteFile(SelectedItems<IndexedFile> files, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Files.deleteFile(user, files, details);
  }

  @Override
  public void createDescriptiveMetadataFile(String aipId, String representationId, DescriptiveMetadataEditBundle bundle)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException,
    AlreadyExistsException, ValidationException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    DescriptiveMetadata.createDescriptiveMetadataFile(user, aipId, representationId, bundle);
  }

  @Override
  public String retrieveDescriptiveMetadataPreview(SupportedMetadataTypeBundle bundle)
    throws AuthorizationDeniedException, GenericException, ValidationException, NotFoundException,
    RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return DescriptiveMetadata.retrieveDescriptiveMetadataPreview(user, bundle);
  }

  @Override
  public void updateDescriptiveMetadataFile(String aipId, String representationId, DescriptiveMetadataEditBundle bundle)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException,
    ValidationException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    DescriptiveMetadata.updateDescriptiveMetadataFile(user, aipId, representationId, bundle);
  }

  @Override
  public void deleteDescriptiveMetadataFile(String aipId, String representationId, String descriptiveMetadataId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    DescriptiveMetadata.deleteDescriptiveMetadataFile(user, aipId, representationId, descriptiveMetadataId);
  }

  @Override
  public String createTransferredResourcesFolder(String parent, String folderName, boolean commit)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return TransferredResources.createTransferredResourcesFolder(user, parent, folderName, commit).getUUID();
  }

  @Override
  public void deleteTransferredResources(SelectedItems<TransferredResource> selected)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    TransferredResources.deleteTransferredResources(user, selected);
  }

  @Override
  public void transferScanRequestUpdate(String transferredResourceRelativePath)
    throws IsStillUpdatingException, AuthorizationDeniedException {
    try {
      User user = UserUtility.getUser(getThreadLocalRequest());
      TransferredResources.updateTransferredResources(user,
        transferredResourceRelativePath != null ? Optional.of(transferredResourceRelativePath) : Optional.empty(),
        true);
    } catch (RuntimeException | GenericException e) {
      LOGGER.error("Error running transferred resources scanner");
    }
  }

  @Override
  public JobBundle retrieveJobBundle(String jobId, List<String> fieldsToReturn) throws RODAException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Job job = Browser.retrieve(user, Job.class, jobId, fieldsToReturn);
    return JobBundles.retrieveJobBundle(job);
  }

  @Override
  public Job createJob(Job job) throws RequestNotValidException, AuthorizationDeniedException, NotFoundException,
    GenericException, JobAlreadyStartedException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Jobs.createJob(user, job, true);
  }

  @Override
  public void stopJob(String jobId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Jobs.stopJob(user, jobId);
  }

  public void approveJob(SelectedItems<Job> jobs) throws RequestNotValidException, AuthorizationDeniedException,
    NotFoundException, GenericException, JobAlreadyStartedException, JobStateNotPendingException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Jobs.approveJob(user, jobs, true);
  }

  public void rejectJob(SelectedItems<Job> jobs, String details)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException,
    JobAlreadyStartedException, JobStateNotPendingException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Jobs.rejectJob(user, jobs, details);
  }

  @Override
  public List<PluginInfo> retrievePluginsInfo(List<PluginType> types) {
    return Plugins.retrievePluginsInfo(types);
  }

  @Override
  public Set<Pair<String, String>> retrieveReindexPluginObjectClasses() {
    return Plugins.retrieveReindexPluginObjectClasses();
  }

  @Override
  public Set<Pair<String, String>> retrieveDropdownPluginItems(String parameterId, String localeString) {
    return Plugins.retrieveDropdownPluginItems(parameterId, localeString);
  }

  @Override
  public Set<ConversionProfile> retrieveConversionProfilePluginItems(String pluginId, String repOrDip,
    String localeString) {
    return Plugins.retrieveConversionProfilePluginItems(pluginId, repOrDip, localeString);
  }

  @Override
  public CreateIngestJobBundle retrieveCreateIngestProcessBundle() {
    return JobBundles.retrieveCreateIngestProcessBundle();
  }

  @Override
  public Viewers retrieveViewersProperties() {
    return Browser.retrieveViewersProperties();
  }

  @Override
  public List<SupportedMetadataTypeBundle> retrieveSupportedMetadata(String aipId, String representationId,
    String localeString) throws RODAException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Locale locale = ServerTools.parseLocale(localeString);
    return Browser.retrieveSupportedMetadata(user, aipId, representationId, locale);
  }

  @Override
  public PreservationEventViewBundle retrievePreservationEventViewBundle(String eventId) throws RODAException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.retrievePreservationEventViewBundle(user, eventId);
  }

  @Override
  public DescriptiveMetadataVersionsBundle retrieveDescriptiveMetadataVersionsBundle(String aipId,
    String representationId, String descriptiveMetadataId, String localeString)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Locale locale = ServerTools.parseLocale(localeString);
    return DescriptiveMetadata.retrieveDescriptiveMetadataVersionsBundle(user, aipId, representationId, descriptiveMetadataId,
      locale);
  }

  @Override
  public void revertDescriptiveMetadataVersion(String aipId, String representationId, String descriptiveMetadataId,
    String versionId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    DescriptiveMetadata.revertDescriptiveMetadataVersion(user, aipId, representationId, descriptiveMetadataId, versionId);
  }

  @Override
  public void deleteDescriptiveMetadataVersion(String aipId, String representationId, String descriptiveMetadataId,
    String versionId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    DescriptiveMetadata.deleteDescriptiveMetadataVersion(user, aipId, representationId, descriptiveMetadataId, versionId);
  }

  @Override
  public Job updateAIPPermissions(SelectedItems<IndexedAIP> aips, Permissions permissions, String details,
    boolean recursive)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Aips.updateAIPPermissions(user, aips, permissions, details, recursive);
  }

  @Override
  public Job updateDIPPermissions(SelectedItems<IndexedDIP> dips, Permissions permissions, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Dips.updateDIPPermissions(user, dips, permissions, details);
  }

  @Override
  public Risk createRisk(Risk risk) throws AuthorizationDeniedException, GenericException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Risks.createRiskWithAuthor(user, risk);
  }

  @Override
  public void updateRisk(Risk risk, int incidences) throws AuthorizationDeniedException, GenericException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Risks.updateRisk(user, risk, incidences);
  }

  @Override
  public void revertRiskVersion(String riskId, String versionId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Risks.revertRiskVersion(user, riskId, versionId);
  }

  @Override
  public void deleteRiskVersion(String riskId, String versionId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Risks.deleteRiskVersion(user, riskId, versionId);
  }

  @Override
  public RiskVersionsBundle retrieveRiskVersions(String riskId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException, IOException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Risks.retrieveRiskVersions(user, riskId);
  }

  @Override
  public boolean hasRiskVersions(String id)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Risks.hasRiskVersions(user, id);
  }

  @Override
  public Risk retrieveRiskVersion(String riskId, String selectedVersion)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException, IOException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Risks.retrieveRiskVersion(user, riskId, selectedVersion);
  }

  @Override
  public RiskMitigationBundle retrieveShowMitigationTerms(int preMitigationProbability, int preMitigationImpact,
    int posMitigationProbability, int posMitigationImpact) throws AuthorizationDeniedException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Risks.retrieveShowMitigationTerms(user, preMitigationProbability, preMitigationImpact,
      posMitigationProbability, posMitigationImpact);
  }

  @Override
  public List<String> retrieveMitigationSeverityLimits() throws AuthorizationDeniedException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Risks.retrieveMitigationSeverityLimits(user);
  }

  @Override
  public MitigationPropertiesBundle retrieveAllMitigationProperties() throws AuthorizationDeniedException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Risks.retrieveAllMitigationProperties(user);
  }

  @Override
  public Job deleteRisk(SelectedItems<IndexedRisk> selected) throws AuthorizationDeniedException, GenericException,
    RequestNotValidException, NotFoundException, InvalidParameterException, JobAlreadyStartedException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Risks.deleteRisk(user, selected);
  }

  @Override
  public <T extends IsIndexed> Job createProcess(String jobName, JobPriority priority, JobParallelism parallelism,
    SelectedItems<T> selected, String id, Map<String, String> value, String selectedClass)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException,
    JobAlreadyStartedException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Jobs.createProcess(jobName, priority, parallelism, selected, id, value, selectedClass, user);
  }

  @Override
  public <T extends IsIndexed> Job createProcess(String jobName, SelectedItems<T> selected, String id,
    Map<String, String> value, String selectedClass) throws AuthorizationDeniedException, RequestNotValidException,
    NotFoundException, GenericException, JobAlreadyStartedException {
    return createProcess(jobName, JobPriority.MEDIUM, JobParallelism.NORMAL, selected, id, value, selectedClass);
  }

  @Override
  public <T extends IsIndexed> String createProcessJson(String jobName, JobPriority priority,
    JobParallelism parallelism, SelectedItems<T> selected, String id, Map<String, String> value, String selectedClass) {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Jobs.createProcessJson(jobName, priority, parallelism, selected, id, value, selectedClass, user);
  }

  @Override
  public <T extends IsIndexed> String createProcessJson(String jobName, SelectedItems<T> selected, String id,
    Map<String, String> value, String selectedClass) {
    return createProcessJson(jobName, JobPriority.MEDIUM, JobParallelism.NORMAL, selected, id, value, selectedClass);
  }

  @Override
  public void updateRiskCounters() throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Risks.updateRiskCounters(user);
  }

  @Override
  public Job appraisal(SelectedItems<IndexedAIP> selected, boolean accept, String rejectReason)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.appraisal(user, selected, accept, rejectReason);
  }

  @Override
  public String renameTransferredResource(String transferredResourceId, String newName)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException,
    IsStillUpdatingException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return TransferredResources.renameTransferredResource(user, transferredResourceId, newName);
  }

  @Override
  public IndexedFile renameFolder(String folderUUID, String newName, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, AlreadyExistsException,
    NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.renameFolder(user, folderUUID, newName, details);
  }

  @Override
  public Job moveFiles(String aipId, String representationId, SelectedItems<IndexedFile> selectedFiles,
    IndexedFile toFolder, String details) throws AuthorizationDeniedException, GenericException,
    RequestNotValidException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Files.moveFiles(user, aipId, representationId, selectedFiles, toFolder, details);
  }

  @Override
  public IndexedFile createFolder(String aipId, String representationId, String folderUUID, String newName,
    String details) throws AuthorizationDeniedException, GenericException, RequestNotValidException,
    AlreadyExistsException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.createFolder(user, aipId, representationId, folderUUID, newName, details);
  }

  @Override
  public Job moveTransferredResource(SelectedItems<TransferredResource> selected,
    TransferredResource transferredResource) throws AuthorizationDeniedException, GenericException,
    RequestNotValidException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return TransferredResources.moveTransferredResource(user, selected, transferredResource);
  }

  @Override
  public List<TransferredResource> retrieveSelectedTransferredResource(SelectedItems<TransferredResource> selected)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return TransferredResources.retrieveSelectedTransferredResource(user, selected);
  }

  @Override
  public void deleteFile(String fileUUID, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Files.deleteFile(user, fileUUID, details);
  }

  @Override
  public void updateRiskIncidence(RiskIncidence incidence) throws AuthorizationDeniedException, GenericException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Risks.updateRiskIncidenceWithCommit(user, incidence);
  }

  @Override
  public Job deleteRiskIncidences(SelectedItems<RiskIncidence> selected, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Risks.deleteRiskIncidences(user, selected, details);
  }

  @Override
  public Job updateMultipleIncidences(SelectedItems<RiskIncidence> selected, String status, String severity,
    Date mitigatedOn, String mitigatedBy, String mitigatedDescription)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Risks.updateMultipleIncidences(user, selected, status, severity, mitigatedOn, mitigatedBy,
      mitigatedDescription);
  }

  @Override
  public String createRepresentation(String aipId, String details) throws AuthorizationDeniedException,
    GenericException, NotFoundException, RequestNotValidException, AlreadyExistsException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    String representationId = IdUtils.createUUID();
    Representations.createRepresentation(user, aipId, representationId, RepresentationContentType.getMIXED().asString(),
      details);
    return representationId;
  }

  @Override
  public Job createFormatIdentificationJob(SelectedItems<?> selected)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.createFormatIdentificationJob(user, selected);
  }

  @Override
  public Job changeRepresentationType(SelectedItems<IndexedRepresentation> selected, String newType, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return RepresentationTypes.changeRepresentationType(user, selected, newType, details);
  }

  @Override
  public Job changeAIPType(SelectedItems<IndexedAIP> selected, String newType, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Aips.changeAIPType(user, selected, newType, details);
  }

  @Override
  public void changeRepresentationStates(IndexedRepresentation representation, List<String> newStates, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Representations.changeRepresentationStates(user, representation, newStates, details);
  }

  @Override
  public BrowseDipBundle retrieveDipBundle(String dipUUID, String dipFileUUID, String localeString)
    throws RequestNotValidException, AuthorizationDeniedException, GenericException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Locale locale = ServerTools.parseLocale(localeString);
    return Browser.retrieveDipBundle(user, dipUUID, dipFileUUID, locale);
  }

  @Override
  public Job deleteDIPs(SelectedItems<IndexedDIP> dips, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Dips.deleteDIPs(user, dips, details);
  }

  @Override
  public <T extends IsIndexed> T retrieveFromModel(String classNameToReturn, String id) throws RODAException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Class<? extends IsRODAObject> classToReturn = SelectedItemsUtils.parseClass(classNameToReturn);
    Class<T> indexedClassToReturn = SolrCollectionRegistry.giveRespectiveIndexClass(classToReturn);
    return Browser.retrieve(user, indexedClassToReturn, id, new ArrayList<>());
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
  public boolean showDIPEmbedded() {
    return Dips.showDIPEmbedded();
  }

  @Override
  public Notification acknowledgeNotification(String notificationId, String ackToken)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.acknowledgeNotification(user, notificationId, ackToken);
  }

  @Override
  public int getExportLimit() {
    return Browser.getExportLimit();
  }

  @Override
  public Pair<Boolean, List<String>> retrieveAIPTypeOptions(String locale) {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Aips.retrieveAIPTypeOptions(locale, user);
  }

  @Override
  public Pair<Boolean, List<String>> retrieveRepresentationTypeOptions(String locale) {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return RepresentationTypes.retrieveRepresentationTypeOptions(locale, user);
  }

  @Override
  public RepresentationInformation createRepresentationInformation(RepresentationInformation ri,
    RepresentationInformationExtraBundle extra)
    throws AuthorizationDeniedException, GenericException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return RepresentationInformations.createRepresentationInformation(user, ri, extra);
  }

  @Override
  public void updateRepresentationInformation(RepresentationInformation ri, RepresentationInformationExtraBundle extra)
    throws AuthorizationDeniedException, GenericException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    RepresentationInformations.updateRepresentationInformation(user, ri, extra);
  }

  @Override
  public Job updateRepresentationInformationListWithFilter(
    SelectedItems<RepresentationInformation> representationInformationItems, String filterToAdd)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return RepresentationInformations.updateRepresentationInformationListWithFilter(user, representationInformationItems, filterToAdd);
  }

  @Override
  public Job deleteRepresentationInformation(SelectedItems<RepresentationInformation> selected)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return RepresentationInformations.deleteRepresentationInformation(user, selected);
  }

  @Override
  public Pair<String, Integer> retrieveRepresentationInformationWithFilter(String riFilter) throws RODAException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return RepresentationInformations.retrieveRepresentationInformationWithFilter(user, riFilter);
  }

  @Override
  public RepresentationInformationFilterBundle retrieveObjectClassFields(String localeString)
    throws AuthorizationDeniedException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Locale locale = ServerTools.parseLocale(localeString);
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    return Browser.retrieveObjectClassFields(user, messages);
  }

  @Override
  public Map<String, String> retrieveRepresentationInformationFamilyOptions(String localeString) {
    return RepresentationInformations.retrieveRepresentationInformationFamilyOptions(localeString);
  }

  @Override
  public String retrieveRepresentationInformationFamilyOptions(String family, String localeString) {
    return RepresentationInformations.retrieveRepresentationInformationFamilyOptions(family, localeString);
  }

  @Override
  public RelationTypeTranslationsBundle retrieveRelationTypeTranslations(String localeString)
    throws AuthorizationDeniedException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Locale locale = ServerTools.parseLocale(localeString);
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    return Browser.retrieveRelationTypeTranslations(user, messages);
  }

  @Override
  public RepresentationInformationExtraBundle retrieveRepresentationInformationExtraBundle(
    String representationInformationId, String localeString)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Locale locale = ServerTools.parseLocale(localeString);
    return Browser.retrieveRepresentationInformationExtraBundle(user, representationInformationId, locale);
  }

  @Override
  public Job associateDisposalSchedule(SelectedItems<IndexedAIP> selectedItems, String disposalScheduleId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.associateDisposalSchedule(user, selectedItems, disposalScheduleId);
  }

  @Override
  public Job disassociateDisposalSchedule(SelectedItems<IndexedAIP> selectedItems)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.disassociateDisposalSchedule(user, selectedItems);
  }

  @Override
  public Job applyDisposalHold(SelectedItems<IndexedAIP> selectedItems, String disposalScheduleId, boolean override)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.applyDisposalHold(user, selectedItems, disposalScheduleId, override);
  }

  @Override
  public Job liftDisposalHold(SelectedItems<IndexedAIP> selectedItems, String disposalHoldId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.liftDisposalHold(user, selectedItems, disposalHoldId);
  }

  @Override
  public DisposalHold liftDisposalHold(DisposalHold disposalHold) throws AuthorizationDeniedException,
    IllegalOperationException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.liftDisposalHold(user, disposalHold);
  }

  @Override
  public Job disassociateDisposalHold(SelectedItems<IndexedAIP> selectedItems, String disposalHoldId, boolean clearAll)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.disassociateDisposalHold(user, selectedItems, disposalHoldId, clearAll);
  }

  @Override
  public Job createDisposalConfirmationReport(SelectedItems<IndexedAIP> selectedItems, String title,
    DisposalConfirmationExtraBundle metadata)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.createDisposalConfirmation(user, title, metadata, selectedItems);
  }

  @Override
  public DisposalConfirmationExtraBundle retrieveDisposalConfirmationExtraBundle() throws RODAException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.retrieveDisposalConfirmationExtraBundle(user);
  }

  @Override
  public Job deleteDisposalConfirmationReport(SelectedItems<DisposalConfirmation> selectedItems, String details)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.deleteDisposalConfirmation(user, selectedItems, details);
  }

  @Override
  public Job destroyRecordsInDisposalConfirmationReport(SelectedItemsList<DisposalConfirmation> selectedItems)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.destroyRecordsInDisposalConfirmationReport(user, selectedItems);
  }

  @Override
  public Job permanentlyDeleteRecordsInDisposalConfirmationReport(SelectedItemsList<DisposalConfirmation> selectedItems)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.permanentlyDeleteRecordsInDisposalConfirmationReport(user, selectedItems);
  }

  @Override
  public Job restoreRecordsInDisposalConfirmationReport(SelectedItemsList<DisposalConfirmation> selectedItems)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.restoreRecordsInDisposalConfirmationReport(user, selectedItems);
  }

  @Override
  public Job recoverRecordsInDisposalConfirmationReport(SelectedItemsList<DisposalConfirmation> selectedItems)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.recoverDisposalConfirmation(user, selectedItems);
  }

  @Override
  public List<DisposalHoldAIPMetadata> listDisposalHoldsAssociation(String aipId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.listDisposalHoldsAssociation(user, aipId);
  }

  @Override
  public String retrieveDisposalConfirmationReport(String confirmationId, boolean isToPrint)
    throws RODAException, IOException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.retrieveDisposalConfirmationReport(user, confirmationId, isToPrint);
  }

  @Override
  public List<DisposalTransitiveHoldAIPMetadata> listTransitiveDisposalHolds(String aipId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Disposals.listTransitiveDisposalHolds(user, aipId);
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
  public AccessKey createAccessKey(AccessKey accessKey) throws AuthorizationDeniedException, AlreadyExistsException,
    NotFoundException, GenericException, RequestNotValidException, IOException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return ApplicationAuth.createAccessKey(user, accessKey);
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
  public AccessKey updateAccessKey(AccessKey accessKey)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return ApplicationAuth.updateAccessKey(user, accessKey);
  }

  @Override
  public void deleteAccessKey(String accessKeyId)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    ApplicationAuth.deleteAccessKey(user, accessKeyId);
  }

  @Override
  public AccessKeys listAccessKeyByUser(String userId)
    throws AuthorizationDeniedException, IOException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return ApplicationAuth.listAccessKeyByUser(user, userId);
  }

  @Override
  public void deactivateUserAccessKeys(String userId)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    ApplicationAuth.deactivateUserAccessKeys(user, userId);
  }

  @Override
  public void deleteUserAccessKeys(String userId)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    ApplicationAuth.deleteUserAccessKeys(user, userId);
  }

  @Override
  public AccessKey regenerateAccessKey(AccessKey accessKey) throws AuthorizationDeniedException,
    RequestNotValidException, NotFoundException, GenericException, AuthenticationDeniedException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return ApplicationAuth.regenerateAccessKey(user, accessKey);
  }

  @Override
  public AccessKey revokeAccessKey(AccessKey accessKey)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return ApplicationAuth.revokeAccessKey(user, accessKey);
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
    return Browser.getCrontabValue(locale);
  }

  public boolean requestAIPLock(String aipId) {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Aips.requestAIPLock(aipId, user);
  }

  public void releaseAIPLock(String aipId) {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Aips.releaseAIPLock(aipId, user);
  }

  public List<Report> retrieveJobReportItems(String jobId, String jobReportId)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Jobs.retrieveJobReportItems(user, jobId, jobReportId);
  }
}
