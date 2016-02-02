/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.server.browse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.Messages;
import org.roda.core.common.UserUtility;
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
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobReport;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StringContentPayload;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.controllers.Jobs;
import org.roda.wui.client.browse.BrowseItemBundle;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.DescriptiveMetadataEditBundle;
import org.roda.wui.client.browse.SupportedMetadataTypeBundle;
import org.roda.wui.client.browse.Viewers;
import org.roda.wui.client.ingest.process.CreateIngestJobBundle;
import org.roda.wui.client.ingest.process.JobBundle;
import org.roda.wui.client.search.SearchField;
import org.roda.wui.common.I18nUtility;
import org.roda.wui.common.server.ServerTools;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Browser Service Implementation
 *
 * @author Luis Faria
 *
 */
public class BrowserServiceImpl extends RemoteServiceServlet implements BrowserService {

  private static final long serialVersionUID = 1L;
  static final String FONDLIST_PAGESIZE = "10";
  // private static final Logger LOGGER =
  // LoggerFactory.getLogger(BrowserServiceImpl.class);

  /**
   * Create a new BrowserService Implementation instance
   *
   */
  public BrowserServiceImpl() {

  }

  @Override
  public BrowseItemBundle getItemBundle(String aipId, String localeString)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    Locale locale = ServerTools.parseLocale(localeString);
    return Browser.getItemBundle(user, aipId, locale);
  }

  @Override
  public DescriptiveMetadataEditBundle getDescriptiveMetadataEditBundle(String aipId, String descId)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Browser.getDescriptiveMetadataEditBundle(user, aipId, descId);
  }

  @Override
  public IndexResult<IndexedAIP> findDescriptiveMetadata(Filter filter, Sorter sorter, Sublist sublist, Facets facets,
    String localeString) throws GenericException, AuthorizationDeniedException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    IndexResult<IndexedAIP> result = Browser.findDescriptiveMetadata(user, filter, sorter, sublist, facets);
    return I18nUtility.translate(result, IndexedAIP.class, localeString);
  }

  public IndexResult<IndexedFile> getRepresentationFiles(Filter filter, Sorter sorter, Sublist sublist, Facets facets,
    String localeString) throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Browser.getFiles(user, filter, sorter, sublist, facets, localeString);
  }

  public Long countDescriptiveMetadata(Filter filter)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Browser.countDescriptiveMetadata(user, filter);
  }

  public IndexedAIP getIndexedAIP(String pid) throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Browser.getIndexedAip(user, pid);
  }

  public List<IndexedAIP> getAncestors(IndexedAIP aip)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Browser.getAncestors(user, aip);
  }

  // FIXME see if there is a best way to deal with "hierarchical" keys
  // FIXME deal with non-configured/badly-configured keys
  @Override
  public List<SearchField> getSearchFields(String localeString) throws GenericException {
    List<SearchField> searchFields = new ArrayList<SearchField>();
    String fieldsNamesString = RodaCoreFactory.getRodaConfigurationAsString("ui", "search", "fields");
    if (fieldsNamesString != null) {
      Messages messages = RodaCoreFactory.getI18NMessages(new Locale(localeString));
      String[] fields = fieldsNamesString.split(",");
      for (String field : fields) {
        SearchField searchField = new SearchField();
        String fieldName = RodaCoreFactory.getRodaConfigurationAsString("ui", "search", "fields", field, "field");
        String fieldType = RodaCoreFactory.getRodaConfigurationAsString("ui", "search", "fields", field, "type");
        String fieldLabelI18N = RodaCoreFactory.getRodaConfigurationAsString("ui", "search", "fields", field, "i18n");

        searchField.setField(fieldName);
        searchField.setType(fieldType);
        searchField.setLabel(messages.getTranslation(fieldLabelI18N));

        searchFields.add(searchField);
      }
    }
    return searchFields;
  }

  @Override
  public IndexedAIP moveInHierarchy(String aipId, String parentId) throws AuthorizationDeniedException,
    GenericException, NotFoundException, RequestNotValidException, AlreadyExistsException, ValidationException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Browser.moveInHierarchy(user, aipId, parentId);
  }

  @Override
  public String createAIP(String parentId) throws AuthorizationDeniedException, GenericException, NotFoundException,
    RequestNotValidException, AlreadyExistsException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Browser.createAIP(user, parentId).getId();

  }

  @Override
  public String removeAIP(String aipId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Browser.removeAIP(user, aipId);
  }

  @Override
  public void createDescriptiveMetadataFile(String aipId, DescriptiveMetadataEditBundle bundle)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException,
    AlreadyExistsException, ValidationException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());

    String metadataId = bundle.getId();
    String descriptiveMetadataType = bundle.getType();
    ContentPayload payload = new StringContentPayload(bundle.getXml());

    Browser.createDescriptiveMetadataFile(user, aipId, metadataId, descriptiveMetadataType, payload);
  }

  @Override
  public void updateDescriptiveMetadataFile(String aipId, DescriptiveMetadataEditBundle bundle)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException,
    ValidationException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    String metadataId = bundle.getId();
    String metadataType = bundle.getType();
    ContentPayload payload = new StringContentPayload(bundle.getXml());

    Browser.updateDescriptiveMetadataFile(user, aipId, metadataId, metadataType, payload);

  }

  public void removeDescriptiveMetadataFile(String itemId, String descriptiveMetadataId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    Browser.removeDescriptiveMetadataFile(user, itemId, descriptiveMetadataId);
  }

  @Override
  public IndexResult<TransferredResource> findTransferredResources(Filter filter, Sorter sorter, Sublist sublist,
    Facets facets) throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Browser.findTransferredResources(user, filter, sorter, sublist, facets);
  }

  @Override
  public TransferredResource retrieveTransferredResource(String transferredResourceId)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Browser.retrieveTransferredResource(user, transferredResourceId);
  }

  public String createTransferredResourcesFolder(String parent, String folderName)
    throws AuthorizationDeniedException, GenericException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Browser.createTransferredResourcesFolder(user, parent, folderName);
  }

  @Override
  public void removeTransferredResources(List<String> ids)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    Browser.removeTransferredResources(user, ids);
  }

  @Override
  public boolean isTransferFullyInitialized() throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Browser.isTransferFullyInitialized(user);
  }

  @Override
  public IndexResult<Job> findJobs(Filter filter, Sorter sorter, Sublist sublist, Facets facets, String localeString)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    IndexResult<Job> result = Jobs.findJobs(user, filter, sorter, sublist, facets);
    return I18nUtility.translate(result, Job.class, localeString);
  }

  @Override
  public Job retrieveJob(String jobId) throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Jobs.getJob(user, jobId);
  }

  @Override
  public JobBundle retrieveJobBundle(String jobId)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    Job job = Jobs.getJob(user, jobId);
    List<PluginInfo> pluginsInfo = new ArrayList<>();

    PluginInfo basePlugin = RodaCoreFactory.getPluginManager().getPluginInfo(job.getPlugin());
    pluginsInfo.add(basePlugin);

    for (PluginParameter parameter : basePlugin.getParameters()) {
      if (PluginParameterType.PLUGIN_SIP_TO_AIP.equals(parameter.getType())) {
        String pluginId = job.getPluginParameters().get(parameter.getId());
        if (pluginId != null) {
          PluginInfo refPlugin = RodaCoreFactory.getPluginManager().getPluginInfo(pluginId);
          pluginsInfo.add(refPlugin);
        }
      }
    }

    // adding all AIP to AIP plugins for job report list
    List<PluginInfo> aipToAipPlugins = RodaCoreFactory.getPluginManager().getPluginsInfo(PluginType.AIP_TO_AIP);
    pluginsInfo.addAll(aipToAipPlugins);

    JobBundle bundle = new JobBundle();
    bundle.setJob(job);
    bundle.setPluginsInfo(pluginsInfo);
    return bundle;
  }

  @Override
  public Job createJob(Job job)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Jobs.createJob(user, job);
  }

  @Override
  public List<PluginInfo> getPluginsInfo(PluginType type) {
    // TODO check permissions
    return RodaCoreFactory.getPluginManager().getPluginsInfo(type);
  }

  @Override
  public CreateIngestJobBundle getCreateIngestProcessBundle() {
    // TODO check permissions
    CreateIngestJobBundle bundle = new CreateIngestJobBundle();
    bundle.setIngestPlugins(RodaCoreFactory.getPluginManager().getPluginsInfo(PluginType.INGEST));
    bundle.setSipToAipPlugins(RodaCoreFactory.getPluginManager().getPluginsInfo(PluginType.SIP_TO_AIP));
    return bundle;
  }

  @Override
  public IndexResult<JobReport> findJobReports(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws GenericException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Jobs.findJobReports(user, filter, sorter, sublist, facets);
  }

  @Override
  public JobReport retrieveJobReport(String jobReportId) throws NotFoundException, GenericException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Jobs.retrieveJobReport(user, jobReportId);
  }

  public Viewers getViewersProperties() {
    Viewers viewers = new Viewers();
    String viewersString = RodaCoreFactory.getRodaConfigurationAsString("ui", "viewers");
    if (viewersString != null) {
      String[] viewersSupported = viewersString.split(",");
      for (String type : viewersSupported) {
        String fieldPronoms = RodaCoreFactory.getRodaConfigurationAsString("ui", "viewers", type, "pronoms");
        String fieldMimetypes = RodaCoreFactory.getRodaConfigurationAsString("ui", "viewers", type, "mimetypes");
        String fieldExtensions = RodaCoreFactory.getRodaConfigurationAsString("ui", "viewers", type, "extensions");

        if (fieldPronoms != null && !fieldPronoms.isEmpty()) {
          for (String pronom : Arrays.asList(fieldPronoms.split(","))) {
            viewers.addPronom(pronom, type);
          }
        }

        if (fieldMimetypes != null && !fieldMimetypes.isEmpty()) {
          for (String mimetype : Arrays.asList(fieldMimetypes.split(","))) {
            viewers.addMimetype(mimetype, type);
          }
        }

        if (fieldExtensions != null && !fieldExtensions.isEmpty()) {
          for (String extension : Arrays.asList(fieldExtensions.split(","))) {
            viewers.addExtension(extension, type);
          }
        }
      }
    }
    return viewers;
  }

  @Override
  public List<SupportedMetadataTypeBundle> getSupportedMetadata(String localeString)
    throws AuthorizationDeniedException, GenericException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    Locale locale = ServerTools.parseLocale(localeString);
    return Browser.getSupportedMetadata(user, locale);
  }

  @Override
  public IndexResult<IndexedPreservationEvent> findIndexedPreservationEvent(Filter filter, Sorter sorter,
    Sublist sublist, Facets facets) throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Browser.findIndexedPreservationEvents(user, filter, sorter, sublist, facets);
  }

  @Override
  public IndexedPreservationEvent retrieveIndexedPreservationEvent(String indexedPreservationEventId)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Browser.retrieveIndexedPreservationEvent(user, indexedPreservationEventId);
  }
}
