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
import java.util.Map;
import java.util.stream.Collectors;

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
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.index.SelectedItemsFilter;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.ORCHESTRATOR_METHOD;
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
import org.roda.wui.client.browse.DescriptiveMetadataVersionsBundle;
import org.roda.wui.client.browse.PreservationEventViewBundle;
import org.roda.wui.client.browse.SupportedMetadataTypeBundle;
import org.roda.wui.client.browse.Viewers;
import org.roda.wui.client.ingest.process.CreateIngestJobBundle;
import org.roda.wui.client.ingest.process.JobBundle;
import org.roda.wui.client.search.SearchField;
import org.roda.wui.common.I18nUtility;
import org.roda.wui.common.server.ServerTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private static final Logger LOGGER = LoggerFactory.getLogger(BrowserServiceImpl.class);

  private static String COOKIES_ACTIVE_PROPERTY = "ui.cookies.active";
  private static String REGISTER_ACTIVE_PROPERTY = "ui.register.active";

  private static String GANALYTICS_ACCOUNT_CODE = null;
  private static String GRECAPTCHA_ACCOUNT_CODE = null;

  private static String GANALYTICS_CODE_PROPERTY = "ui.google.analytics.code";
  private static String GRECAPTCHA_CODE_PROPERTY = "ui.google.recaptcha.code";

  /**
   * Create a new BrowserService Implementation instance
   *
   */
  public BrowserServiceImpl() {

  }

  @Override
  public boolean isCookiesMessageActive() {
    return RodaCoreFactory.getRodaConfiguration().getBoolean(COOKIES_ACTIVE_PROPERTY, false);
  }

  @Override
  public boolean isRegisterActive() {
    return RodaCoreFactory.getRodaConfiguration().getBoolean(REGISTER_ACTIVE_PROPERTY, false);
  }

  @Override
  public String getGoogleAnalyticsAccount() {
    if (GANALYTICS_ACCOUNT_CODE == null) {
      GANALYTICS_ACCOUNT_CODE = RodaCoreFactory.getRodaConfiguration().getString(GANALYTICS_CODE_PROPERTY, "");
      LOGGER.debug("Google Analytics Account Code: " + GANALYTICS_ACCOUNT_CODE);
    }
    return GANALYTICS_ACCOUNT_CODE;
  }

  @Override
  public String getGoogleReCAPTCHAAccount() {
    if (GRECAPTCHA_ACCOUNT_CODE == null) {
      GRECAPTCHA_ACCOUNT_CODE = RodaCoreFactory.getRodaConfiguration().getString(GRECAPTCHA_CODE_PROPERTY, "");
      LOGGER.debug("Google ReCAPTCHA Account Code: " + GRECAPTCHA_ACCOUNT_CODE);
    }
    return GRECAPTCHA_ACCOUNT_CODE;
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

  @SuppressWarnings("unchecked")
  private <T extends IsIndexed> Class<T> parseClass(String classNameToReturn) throws GenericException {
    Class<T> classToReturn;
    try {
      classToReturn = (Class<T>) Class.forName(classNameToReturn);
    } catch (ClassNotFoundException e) {
      throw new GenericException("Could not find class " + classNameToReturn);
    }
    return classToReturn;
  }

  @Override
  public <T extends IsIndexed> IndexResult<T> find(String classNameToReturn, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets, String localeString)
      throws GenericException, AuthorizationDeniedException, RequestNotValidException {
    try {
      RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
      Class<T> classToReturn = parseClass(classNameToReturn);
      IndexResult<T> result = Browser.find(user, classToReturn, filter, sorter, sublist, facets);
      return I18nUtility.translate(result, classToReturn, localeString);
    } catch (Throwable e) {
      LOGGER.error("Unexpected error in find", e);
      throw e;
    }
  }

  @Override
  public <T extends IsIndexed> Long count(String classNameToReturn, Filter filter)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    Class<T> classToReturn = parseClass(classNameToReturn);
    return Browser.count(user, classToReturn, filter);
  }

  @Override
  public <T extends IsIndexed> T retrieve(String classNameToReturn, String id)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    Class<T> classToReturn = parseClass(classNameToReturn);
    return Browser.retrieve(user, classToReturn, id);
  }

  @Override
  public <T extends IsIndexed> List<String> suggest(String classNameToReturn, String field, String query)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    Class<T> classToReturn = parseClass(classNameToReturn);
    return Browser.suggest(user, classToReturn, field, query);
  }

  public List<IndexedAIP> getAncestors(IndexedAIP aip)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Browser.getAncestors(user, aip);
  }

  @Override
  public List<SearchField> getSearchFields(String localeString) throws GenericException {
    List<SearchField> searchFields = new ArrayList<SearchField>();
    String fieldsNamesString = RodaCoreFactory.getRodaConfigurationAsString("ui", "search", "fields");
    if (fieldsNamesString != null) {
      Messages messages = RodaCoreFactory.getI18NMessages(new Locale(localeString));
      String[] fields = fieldsNamesString.split(",");
      for (String field : fields) {
        SearchField searchField = new SearchField();

        String fieldsNames = RodaCoreFactory.getRodaConfigurationAsString("ui", "search", "fields", field, "fields");
        String fieldType = RodaCoreFactory.getRodaConfigurationAsString("ui", "search", "fields", field, "type");
        String fieldLabelI18N = RodaCoreFactory.getRodaConfigurationAsString("ui", "search", "fields", field, "i18n");
        boolean fieldFixed = Boolean
          .valueOf(RodaCoreFactory.getRodaConfigurationAsString("ui", "search", "fields", field, "fixed"));

        if (fieldsNames != null && fieldType != null && fieldLabelI18N != null) {
          List<String> fieldsNamesList = Arrays.asList(fieldsNames.split(","));

          searchField.setId(field);
          searchField.setSearchFields(fieldsNamesList);
          searchField.setType(fieldType);
          searchField.setLabel(messages.getTranslation(fieldLabelI18N));
          searchField.setFixed(fieldFixed);

          searchFields.add(searchField);
        }
      }
    }
    return searchFields;
  }

  @Override
  public AIP moveInHierarchy(String aipId, String parentId) throws AuthorizationDeniedException, GenericException,
    NotFoundException, RequestNotValidException, AlreadyExistsException, ValidationException {
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
  public String removeAIP(SelectedItems<IndexedAIP> aips)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Browser.removeAIP(user, aips);
  }

  @Override
  public void createDescriptiveMetadataFile(String aipId, DescriptiveMetadataEditBundle bundle)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException,
    AlreadyExistsException, ValidationException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());

    String metadataId = bundle.getId();
    String descriptiveMetadataType = bundle.getType();
    String descriptiveMetadataVersion = bundle.getVersion();
    ContentPayload payload = new StringContentPayload(bundle.getXml());

    Browser.createDescriptiveMetadataFile(user, aipId, metadataId, descriptiveMetadataType, descriptiveMetadataVersion,
      payload);
  }

  @Override
  public void updateDescriptiveMetadataFile(String aipId, DescriptiveMetadataEditBundle bundle)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException,
    ValidationException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    String metadataId = bundle.getId();
    String metadataType = bundle.getType();
    String metadataVersion = bundle.getVersion();
    ContentPayload payload = new StringContentPayload(bundle.getXml());

    Browser.updateDescriptiveMetadataFile(user, aipId, metadataId, metadataType, metadataVersion, payload);

  }

  public void removeDescriptiveMetadataFile(String itemId, String descriptiveMetadataId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    Browser.removeDescriptiveMetadataFile(user, itemId, descriptiveMetadataId);
  }

  public String createTransferredResourcesFolder(String parent, String folderName)
    throws AuthorizationDeniedException, GenericException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Browser.createTransferredResourcesFolder(user, parent, folderName, false);
  }

  @Override
  public void removeTransferredResources(SelectedItems<TransferredResource> selected)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    Browser.removeTransferredResources(user, selected);
  }



  @Override
  public boolean isTransferFullyInitialized() throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Browser.isTransferFullyInitialized(user);
  }

  @Override
  public JobBundle retrieveJobBundle(String jobId)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    Job job = Browser.retrieve(user, Job.class, jobId);
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
  public Job createIngestProcess(String jobName, SelectedItems<TransferredResource> selected, String plugin,
    Map<String, String> parameters)
      throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {

    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());

    Job job = new Job();
    job.setName(jobName);

    List<String> objectIds = Browser.consolidate(user, TransferredResource.class, selected);
    job.setObjectIds(objectIds);
    job.setOrchestratorMethod(ORCHESTRATOR_METHOD.ON_TRANSFERRED_RESOURCES);

    job.setPlugin(plugin);
    job.setPluginParameters(parameters);

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
  public PreservationEventViewBundle retrievePreservationEventViewBundle(String eventId)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    return Browser.retrievePreservationEventViewBundle(user, eventId);
  }

  @Override
  public DescriptiveMetadataVersionsBundle getDescriptiveMetadataVersionsBundle(String aipId,
    String descriptiveMetadataId, String localeString)
      throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    Locale locale = ServerTools.parseLocale(localeString);
    return Browser.getDescriptiveMetadataVersionsBundle(user, aipId, descriptiveMetadataId, locale);
  }

  @Override
  public void revertDescriptiveMetadataVersion(String aipId, String descriptiveMetadataId, String versionId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    Browser.revertDescriptiveMetadataVersion(user, aipId, descriptiveMetadataId, versionId);
  }

  @Override
  public void removeDescriptiveMetadataVersion(String aipId, String descriptiveMetadataId, String versionId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    Browser.removeDescriptiveMetadataVersion(user, aipId, descriptiveMetadataId, versionId);
  }

  @Override
  public void updateAIPPermssions(String aipId, Permissions permissions)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest(), RodaCoreFactory.getIndexService());
    Browser.updateAIPPermissions(user, aipId, permissions);
  }
}
