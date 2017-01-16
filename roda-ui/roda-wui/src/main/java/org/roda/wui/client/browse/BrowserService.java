/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
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
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;
import org.roda.wui.client.browse.bundle.BrowseFileBundle;
import org.roda.wui.client.browse.bundle.BrowseRepresentationBundle;
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

  /**
   * Utilities
   *
   */
  public static class Util {

    /**
     * Get singleton instance
     *
     * @return the instance
     */
    public static BrowserServiceAsync getInstance() {

      BrowserServiceAsync instance = (BrowserServiceAsync) GWT.create(BrowserService.class);
      ServiceDefTarget target = (ServiceDefTarget) instance;
      target.setServiceEntryPoint(GWT.getModuleBaseURL() + SERVICE_URI);
      return instance;
    }
  }

  BrowseAIPBundle retrieveBrowseAIPBundle(String aipId, String localeString)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  BrowseRepresentationBundle retrieveBrowseRepresentationBundle(String aipId, String representationId,
    String localeString)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  BrowseFileBundle retrieveBrowseFileBundle(String historyAipId, String historyRepresentationId,
    List<String> historyFilePath, String historyFileId, String localeName)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  DescriptiveMetadataEditBundle retrieveDescriptiveMetadataEditBundle(String aipId, String representationId,
    String descId, String type, String version, String localeString)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException;

  DescriptiveMetadataEditBundle retrieveDescriptiveMetadataEditBundle(String aipId, String representationId,
    String descId, String localeString)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException;

  List<SearchField> retrieveSearchFields(String className, String locale) throws GenericException;

  IndexedAIP moveAIPInHierarchy(SelectedItems<IndexedAIP> selected, String parentId, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException,
    AlreadyExistsException, ValidationException;

  String createAIP(String parentId, String type) throws AuthorizationDeniedException, GenericException,
    NotFoundException, RequestNotValidException, AlreadyExistsException;

  String deleteAIP(SelectedItems<IndexedAIP> aips, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  void deleteRepresentation(SelectedItems<IndexedRepresentation> representations, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  void deleteFile(SelectedItems<IndexedFile> files, String details)
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

  String createTransferredResourcesFolder(String parent, String folderName)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException;

  void deleteTransferredResources(SelectedItems<TransferredResource> selected)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  void transferScanRequestUpdate(String transferredResourceUUID)
    throws IsStillUpdatingException, AuthorizationDeniedException;

  Job createJob(Job job) throws AuthorizationDeniedException, NotFoundException, RequestNotValidException,
    GenericException, JobAlreadyStartedException;

  void stopJob(String jobId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  List<PluginInfo> retrievePluginsInfo(List<PluginType> type);

  Set<Pair<String, String>> retrieveReindexPluginObjectClasses();

  CreateIngestJobBundle retrieveCreateIngestProcessBundle();

  JobBundle retrieveJobBundle(String jobId) throws AuthorizationDeniedException, GenericException, NotFoundException;

  Viewers retrieveViewersProperties() throws GenericException;

  List<SupportedMetadataTypeBundle> retrieveSupportedMetadata(String aipId, String representationUUID, String locale)
    throws AuthorizationDeniedException, GenericException, NotFoundException;

  boolean isCookiesMessageActive();

  /**
   * Get Google Analytics account id
   */
  String retrieveGoogleAnalyticsAccount();

  /**
   * Get Google reCAPTCHA account id
   */
  String retrieveGoogleReCAPTCHAAccount();

  PreservationEventViewBundle retrievePreservationEventViewBundle(String eventId)
    throws AuthorizationDeniedException, GenericException, NotFoundException;

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
    Facets facets, String localeString, boolean justActive)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException;

  <T extends IsIndexed> Long count(String classNameToReturn, Filter filter)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException;

  <T extends IsIndexed> T retrieve(String classNameToReturn, String id)
    throws AuthorizationDeniedException, GenericException, NotFoundException;

  <T extends IsIndexed> List<T> retrieve(String classNameToReturn, SelectedItems<T> selectedItems)
    throws GenericException, AuthorizationDeniedException, NotFoundException, RequestNotValidException;

  <T extends IsIndexed> void delete(String classNameToReturn, SelectedItems<T> ids)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  <T extends IsIndexed> List<String> suggest(String classNameToReturn, String field, String query, boolean allowPartial)
    throws AuthorizationDeniedException, GenericException, NotFoundException;

  void updateAIPPermissions(List<IndexedAIP> aips, Permissions permissions, String details, boolean recursive)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException;

  void updateRisk(Risk risk)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  void updateFormat(Format format)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  Risk createRisk(Risk risk)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  Format createFormat(Format format)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  void revertRiskVersion(String riskId, String versionId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException, IOException;

  boolean hasRiskVersions(String id)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException;

  void deleteRiskVersion(String riskId, String versionId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException, IOException;

  RiskVersionsBundle retrieveRiskVersions(String riskId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException, IOException;

  Risk retrieveRiskVersion(String riskId, String selectedVersion)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException, IOException;

  RiskMitigationBundle retrieveShowMitigationTerms(int preMitigationProbability, int preMitigationImpact,
    int posMitigationProbability, int posMitigationImpact) throws AuthorizationDeniedException;

  List<String> retrieveMitigationSeverityLimits() throws AuthorizationDeniedException;

  MitigationPropertiesBundle retrieveAllMitigationProperties() throws AuthorizationDeniedException;

  void deleteRisk(SelectedItems<IndexedRisk> selected) throws AuthorizationDeniedException, GenericException,
    RequestNotValidException, NotFoundException, InvalidParameterException, JobAlreadyStartedException;

  void deleteFormat(SelectedItems<Format> selected)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException;

  <T extends IsIndexed> Job createProcess(String jobName, SelectedItems<T> selected, String id,
    Map<String, String> value, String selectedClass) throws AuthorizationDeniedException, RequestNotValidException,
    NotFoundException, GenericException, JobAlreadyStartedException;

  void updateRiskCounters()
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException;

  void appraisal(SelectedItems<IndexedAIP> selected, boolean accept, String rejectReason, String localeString)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException;

  IndexedRepresentation retrieveRepresentationById(String representationId)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException;

  IndexedFile retrieveFileById(String fileId)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException;

  String renameTransferredResource(String transferredResourceId, String newName)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException,
    IsStillUpdatingException, NotFoundException;

  String moveTransferredResource(SelectedItems<TransferredResource> selected, TransferredResource transferredResource)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, AlreadyExistsException,
    IsStillUpdatingException, NotFoundException;

  List<TransferredResource> retrieveSelectedTransferredResource(SelectedItems<TransferredResource> selected)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException;

  void deleteFile(String fileUUID, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException;

  void updateRiskIncidence(RiskIncidence incidence) throws AuthorizationDeniedException, GenericException;

  void deleteRiskIncidences(SelectedItems<RiskIncidence> selected)
    throws JobAlreadyStartedException, AuthorizationDeniedException, GenericException, RequestNotValidException,
    NotFoundException, InvalidParameterException;

  void updateMultipleIncidences(SelectedItems<RiskIncidence> selected, String status, String severity, Date mitigatedOn,
    String mitigatedBy, String mitigatedDescription)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException;

  String createRepresentation(String aipId, String details) throws AuthorizationDeniedException, GenericException,
    NotFoundException, RequestNotValidException, AlreadyExistsException;

  IndexedFile renameFolder(String folderUUID, String newName, String details) throws AuthorizationDeniedException,
    GenericException, RequestNotValidException, AlreadyExistsException, NotFoundException;

  void moveFiles(String aipId, String representationId, SelectedItems<IndexedFile> selectedFiles, IndexedFile toFolder,
    String details) throws AuthorizationDeniedException, GenericException, RequestNotValidException,
    AlreadyExistsException, NotFoundException;

  IndexedFile createFolder(String aipId, String representationId, String folderUUID, String newName, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, AlreadyExistsException,
    NotFoundException;

  void createFormatIdentificationJob(SelectedItems selected) throws GenericException, AuthorizationDeniedException,
    JobAlreadyStartedException, RequestNotValidException, NotFoundException;

  void changeRepresentationType(SelectedItemsList<IndexedRepresentation> selectedRepresentation, String newType,
    String details) throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException;

  DipBundle getDipBundle(String dipUUID, String dipFileUUID)
    throws RequestNotValidException, AuthorizationDeniedException, GenericException, NotFoundException;

  void deleteDIP(String dipId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  <T extends IsIndexed> T retrieveFromModel(String classNameToReturn, String id)
    throws AuthorizationDeniedException, GenericException, NotFoundException;

}
