/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import java.util.List;

import org.roda.core.common.validation.ValidationException;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.RepresentationPreservationObject;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobReport;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.wui.client.ingest.process.CreateIngestJobBundle;
import org.roda.wui.client.ingest.process.JobBundle;
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

  /**
   * Get simple descriptive metadata count
   *
   * @param filter
   *
   * @return the number of simple descriptive metadata that fit the filter
   * @throws RequestNotValidException
   * @throws GenericException
   * @throws AuthorizationDeniedException
   */
  Long countDescriptiveMetadata(Filter filter)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException;

  /**
   * Get imple descriptive metadata
   *
   *
   * @return
   * @throws RequestNotValidException
   * @throws AuthorizationDeniedException
   * @throws GenericException
   */
  IndexResult<IndexedAIP> findDescriptiveMetadata(Filter filter, Sorter sorter, Sublist sublist, Facets facets,
    String locale) throws GenericException, AuthorizationDeniedException, RequestNotValidException;

  BrowseItemBundle getItemBundle(String aipId, String localeString)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  DescriptiveMetadataEditBundle getDescriptiveMetadataEditBundle(String aipId, String descId)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException;

  /**
   * Get simple description object
   *
   * @param pid
   *          the object id
   * @return {@link SimpleDescriptionObject}
   * @throws NotFoundException
   * @throws GenericException
   * @throws AuthorizationDeniedException
   */
  IndexedAIP getIndexedAIP(String pid) throws AuthorizationDeniedException, GenericException, NotFoundException;

  Long countDescriptiveMetadataBinaries(String aipId)
    throws AuthorizationDeniedException, NotFoundException, RequestNotValidException, GenericException;

  List<SearchField> getSearchFields(String locale) throws GenericException;

  IndexedAIP moveInHierarchy(String aipId, String parentId) throws AuthorizationDeniedException, GenericException,
    NotFoundException, RequestNotValidException, AlreadyExistsException, ValidationException;

  String createAIP(String parentId) throws AuthorizationDeniedException, GenericException, NotFoundException,
    RequestNotValidException, AlreadyExistsException;

  String removeAIP(String aipId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  void removeDescriptiveMetadataFile(String itemId, String descriptiveMetadataId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException;

  void updateDescriptiveMetadataFile(String aipId, DescriptiveMetadataEditBundle bundle)
    throws AuthorizationDeniedException, GenericException, ValidationException, NotFoundException,
    RequestNotValidException;

  void createDescriptiveMetadataFile(String aipId, DescriptiveMetadataEditBundle newBundle)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException,
    AlreadyExistsException, ValidationException;

  IndexResult<TransferredResource> findTransferredResources(Filter filter, Sorter sorter, Sublist sublist,
    Facets facets) throws AuthorizationDeniedException, GenericException, RequestNotValidException;

  TransferredResource retrieveTransferredResource(String transferredResourceId)
    throws AuthorizationDeniedException, GenericException, NotFoundException;

  String createTransferredResourcesFolder(String parent, String folderName)
    throws AuthorizationDeniedException, GenericException;

  void removeTransferredResources(List<String> ids)
    throws AuthorizationDeniedException, GenericException, NotFoundException;

  boolean isTransferFullyInitialized() throws AuthorizationDeniedException, GenericException, NotFoundException;

  IndexResult<IndexedFile> getRepresentationFiles(Filter filter, Sorter sorter, Sublist sublist, Facets facets,
    String localeString) throws AuthorizationDeniedException, GenericException, RequestNotValidException;

  IndexResult<Job> findJobs(Filter filter, Sorter sorter, Sublist sublist, Facets facets, String localeString)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException;

  Job retrieveJob(String jobId) throws AuthorizationDeniedException, GenericException, NotFoundException;

  Job createJob(Job job)
    throws AuthorizationDeniedException, NotFoundException, RequestNotValidException, GenericException;

  List<PluginInfo> getPluginsInfo(PluginType type);

  CreateIngestJobBundle getCreateIngestProcessBundle();

  JobBundle retrieveJobBundle(String jobId) throws AuthorizationDeniedException, GenericException, NotFoundException;

  IndexResult<JobReport> findJobReports(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws GenericException, RequestNotValidException;

  Viewers getViewersProperties() throws GenericException;

  List<SupportedMetadataTypeBundle> getSupportedMetadata(String locale)
    throws AuthorizationDeniedException, GenericException;

  JobReport retrieveJobReport(String jobReportId) throws NotFoundException, GenericException;

  IndexResult<IndexedPreservationEvent> findIndexedPreservationEvent(Filter filter, Sorter sorter, Sublist sublist,
    Facets facets) throws AuthorizationDeniedException, GenericException, RequestNotValidException;

  IndexedPreservationEvent retrieveIndexedPreservationEvent(String indexedPreservationEventId)
    throws AuthorizationDeniedException, GenericException, NotFoundException;

}
