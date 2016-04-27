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
import java.util.Map;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
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
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.wui.client.ingest.process.CreateIngestJobBundle;
import org.roda.wui.client.ingest.process.JobBundle;
import org.roda.wui.client.planning.MitigationPropertiesBundle;
import org.roda.wui.client.planning.RiskMitigationBundle;
import org.roda.wui.client.search.SearchField;

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

  BrowseItemBundle getItemBundle(String aipId, String localeString)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  DescriptiveMetadataEditBundle getDescriptiveMetadataEditBundle(String aipId, String descId)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException;

  List<SearchField> getSearchFields(String locale) throws GenericException;

  AIP moveInHierarchy(String aipId, String parentId) throws AuthorizationDeniedException, GenericException,
    NotFoundException, RequestNotValidException, AlreadyExistsException, ValidationException;

  String createAIP(String parentId, String type) throws AuthorizationDeniedException, GenericException,
    NotFoundException, RequestNotValidException, AlreadyExistsException;

  String removeAIP(SelectedItems aips)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  void removeDescriptiveMetadataFile(String itemId, String descriptiveMetadataId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  void updateDescriptiveMetadataFile(String aipId, DescriptiveMetadataEditBundle bundle)
    throws AuthorizationDeniedException, GenericException, ValidationException, NotFoundException,
    RequestNotValidException;

  void createDescriptiveMetadataFile(String aipId, DescriptiveMetadataEditBundle newBundle)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException,
    AlreadyExistsException, ValidationException;

  String createTransferredResourcesFolder(String parent, String folderName)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException;

  void removeTransferredResources(SelectedItems selected)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  boolean transferScanIsUpdating();

  void transferScanRequestUpdate(String transferredResourceUUID) throws IsStillUpdatingException;

  Job createJob(Job job) throws AuthorizationDeniedException, NotFoundException, RequestNotValidException,
    GenericException, JobAlreadyStartedException;

  List<PluginInfo> getPluginsInfo(PluginType type);

  CreateIngestJobBundle getCreateIngestProcessBundle();

  JobBundle retrieveJobBundle(String jobId) throws AuthorizationDeniedException, GenericException, NotFoundException;

  Viewers getViewersProperties() throws GenericException;

  List<SupportedMetadataTypeBundle> getSupportedMetadata(String locale)
    throws AuthorizationDeniedException, GenericException;

  boolean isCookiesMessageActive();

  boolean isRegisterActive();

  /**
   * Get Google Analytics account id
   */
  String getGoogleAnalyticsAccount();

  /**
   * Get Google reCAPTCHA account id
   */
  String getGoogleReCAPTCHAAccount();

  PreservationEventViewBundle retrievePreservationEventViewBundle(String eventId)
    throws AuthorizationDeniedException, GenericException, NotFoundException;

  DescriptiveMetadataVersionsBundle getDescriptiveMetadataVersionsBundle(String aipId, String descriptiveMetadataId,
    String localeString)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException;

  void revertDescriptiveMetadataVersion(String aipId, String descriptiveMetadataId, String versionId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException;

  void removeDescriptiveMetadataVersion(String aipId, String descriptiveMetadataId, String versionId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException;

  <T extends IsIndexed> IndexResult<T> find(String classNameToReturn, Filter filter, Sorter sorter, Sublist sublist,
    Facets facets, String localeString) throws GenericException, AuthorizationDeniedException, RequestNotValidException;

  <T extends IsIndexed> Long count(String classNameToReturn, Filter filter)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException;

  <T extends IsIndexed> T retrieve(String classNameToReturn, String id)
    throws AuthorizationDeniedException, GenericException, NotFoundException;

  <T extends IsIndexed> void delete(String classNameToReturn, SelectedItems ids)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  <T extends IsIndexed> List<String> suggest(String classNameToReturn, String field, String query)
    throws AuthorizationDeniedException, GenericException, NotFoundException;

  Job createIngestProcess(String jobName, SelectedItems selected, String plugin, Map<String, String> parameters)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException,
    JobAlreadyStartedException;

  void updateAIPPermssions(String aipId, Permissions permissions)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException;

  void modifyRisk(Risk risk, String message)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  void modifyFormat(Format format)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  void modifyAgent(Agent agent)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  Risk addRisk(Risk risk)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  Format addFormat(Format format)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  Agent addAgent(Agent agent)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  List<Format> retrieveFormats(String agentId) throws AuthorizationDeniedException, NotFoundException, GenericException;

  List<Agent> retrieveRequiredAgents(String agentId)
    throws AuthorizationDeniedException, NotFoundException, GenericException;

  void revertRiskVersion(String riskId, String versionId, String message)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException, IOException;

  boolean hasRiskVersions(String id)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException;

  void removeRiskVersion(String riskId, String versionId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException, IOException;

  RiskVersionsBundle retrieveRiskVersions(String riskId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException, IOException;

  Risk retrieveRiskVersion(String riskId, String selectedVersion)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException, IOException;

  RiskMitigationBundle retrieveShowMitigationTerms(int preMitigationProbability, int preMitigationImpact,
    int posMitigationProbability, int posMitigationImpact) throws AuthorizationDeniedException;

  List<String> retrieveMitigationSeverityLimits() throws AuthorizationDeniedException;

  MitigationPropertiesBundle retrieveAllMitigationProperties() throws AuthorizationDeniedException;
}
