/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.server.browse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

import org.apache.commons.configuration.Configuration;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.Messages;
import org.roda.core.common.RodaUtils;
import org.roda.core.common.UserUtility;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.agents.Agent;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.index.SelectedItemsAll;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.index.SelectedItemsNone;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
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
import org.roda.wui.client.common.search.SearchField;
import org.roda.wui.client.ingest.process.CreateIngestJobBundle;
import org.roda.wui.client.ingest.process.JobBundle;
import org.roda.wui.client.planning.MitigationPropertiesBundle;
import org.roda.wui.client.planning.RiskMitigationBundle;
import org.roda.wui.client.planning.RiskVersionsBundle;
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
  public String retrieveGoogleAnalyticsAccount() {
    if (GANALYTICS_ACCOUNT_CODE == null) {
      GANALYTICS_ACCOUNT_CODE = RodaCoreFactory.getRodaConfiguration().getString(GANALYTICS_CODE_PROPERTY, "");
      LOGGER.debug("Google Analytics Account Code: " + GANALYTICS_ACCOUNT_CODE);
    }
    return GANALYTICS_ACCOUNT_CODE;
  }

  @Override
  public String retrieveGoogleReCAPTCHAAccount() {
    if (GRECAPTCHA_ACCOUNT_CODE == null) {
      GRECAPTCHA_ACCOUNT_CODE = RodaCoreFactory.getRodaConfiguration().getString(GRECAPTCHA_CODE_PROPERTY, "");
      LOGGER.debug("Google ReCAPTCHA Account Code: " + GRECAPTCHA_ACCOUNT_CODE);
    }
    return GRECAPTCHA_ACCOUNT_CODE;
  }

  @Override
  public BrowseItemBundle retrieveItemBundle(String aipId, String localeString)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    Locale locale = ServerTools.parseLocale(localeString);
    return Browser.retrieveItemBundle(user, aipId, locale);
  }

  @Override
  public DescriptiveMetadataEditBundle retrieveDescriptiveMetadataEditBundle(String aipId, String descId, String type,
    String version) throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.retrieveDescriptiveMetadataEditBundle(user, aipId, descId, type, version);
  }

  @Override
  public DescriptiveMetadataEditBundle retrieveDescriptiveMetadataEditBundle(String aipId, String descId)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.retrieveDescriptiveMetadataEditBundle(user, aipId, descId);
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
    Sublist sublist, Facets facets, String localeString, boolean justActive)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException {
    try {
      RodaUser user = UserUtility.getUser(getThreadLocalRequest());
      Class<T> classToReturn = parseClass(classNameToReturn);
      IndexResult<T> result = Browser.find(classToReturn, filter, sorter, sublist, facets, user, justActive);
      return I18nUtility.translate(result, classToReturn, localeString);
    } catch (RuntimeException e) {
      LOGGER.error("Unexpected error in find", e);
      throw new GenericException(e);
    }
  }

  @Override
  public <T extends IsIndexed> Long count(String classNameToReturn, Filter filter)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    Class<T> classToReturn = parseClass(classNameToReturn);
    return Browser.count(user, classToReturn, filter);
  }

  @Override
  public <T extends IsIndexed> T retrieve(String classNameToReturn, String id)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    Class<T> classToReturn = parseClass(classNameToReturn);
    return Browser.retrieve(user, classToReturn, id);
  }

  @Override
  public <T extends IsIndexed> List<T> retrieve(String classNameToReturn, SelectedItems<T> selectedItems)
    throws GenericException, AuthorizationDeniedException, NotFoundException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    Class<T> classToReturn = parseClass(classNameToReturn);
    return Browser.retrieve(user, classToReturn, selectedItems);
  }

  @Override
  public <T extends IsIndexed> void delete(String classNameToReturn, SelectedItems<T> ids)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    Class<T> classToReturn = parseClass(classNameToReturn);
    Browser.delete(user, classToReturn, ids);
  }

  @Override
  public <T extends IsIndexed> List<String> suggest(String classNameToReturn, String field, String query)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    Class<T> classToReturn = parseClass(classNameToReturn);
    return Browser.suggest(user, classToReturn, field, query);
  }

  public List<IndexedAIP> retrieveAncestors(IndexedAIP aip)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.retrieveAncestors(user, aip);
  }

  @Override
  public List<SearchField> retrieveSearchFields(String localeString) throws GenericException {
    List<SearchField> searchFields = new ArrayList<SearchField>();
    List<String> fields = RodaUtils.copyList(RodaCoreFactory.getRodaConfiguration().getList("ui.search.fields"));

    Messages messages = RodaCoreFactory.getI18NMessages(new Locale(localeString));
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
        try {
          searchField.setLabel(messages.getTranslation(fieldLabelI18N));
        } catch (MissingResourceException e) {
          searchField.setLabel(fieldLabelI18N);
        }
        searchField.setFixed(fieldFixed);

        searchFields.add(searchField);
      }
    }

    return searchFields;
  }

  @Override
  public IndexedAIP moveAIPInHierarchy(SelectedItems<IndexedAIP> selected, String parentId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException,
    AlreadyExistsException, ValidationException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.moveAIPInHierarchy(selected, parentId, user);
  }

  @Override
  public String createAIP(String parentId, String type) throws AuthorizationDeniedException, GenericException,
    NotFoundException, RequestNotValidException, AlreadyExistsException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.createAIP(user, parentId, type).getId();

  }

  @Override
  public String deleteAIP(SelectedItems<IndexedAIP> aips)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.deleteAIP(user, aips);
  }

  @Override
  public void createDescriptiveMetadataFile(String aipId, DescriptiveMetadataEditBundle bundle)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException,
    AlreadyExistsException, ValidationException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());

    // If the bundle has values from the form, we need to update the XML by
    // applying the values of the form to the raw tempalte
    if (bundle.getValues() != null) {
      SupportedMetadataTypeBundle smtb = new SupportedMetadataTypeBundle(bundle.getType(), bundle.getVersion(),
        bundle.getId(), bundle.getRawTemplate(), bundle.getValues());
      bundle.setXml(Browser.createDescriptiveMetadataPreview(user, aipId, smtb));
    }

    String metadataId = bundle.getId();
    String descriptiveMetadataType = bundle.getType();
    String descriptiveMetadataVersion = bundle.getVersion();
    ContentPayload payload = new StringContentPayload(bundle.getXml());

    Browser.createDescriptiveMetadataFile(user, aipId, metadataId, descriptiveMetadataType, descriptiveMetadataVersion,
      payload);
  }

  @Override
  public String retrieveDescriptiveMetadataPreview(String aipId, SupportedMetadataTypeBundle bundle)
    throws AuthorizationDeniedException, GenericException, ValidationException, NotFoundException,
    RequestNotValidException {

    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.createDescriptiveMetadataPreview(user, aipId, bundle);
  }

  @Override
  public void updateDescriptiveMetadataFile(String aipId, DescriptiveMetadataEditBundle bundle)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException,
    ValidationException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    String metadataId = bundle.getId();
    String metadataType = bundle.getType();
    String metadataVersion = bundle.getVersion();
    ContentPayload payload = new StringContentPayload(bundle.getXml());

    Browser.updateDescriptiveMetadataFile(user, aipId, metadataId, metadataType, metadataVersion, payload);

  }

  public void deleteDescriptiveMetadataFile(String itemId, String descriptiveMetadataId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    Browser.deleteDescriptiveMetadataFile(user, itemId, descriptiveMetadataId);
  }

  public String createTransferredResourcesFolder(String parent, String folderName)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.createTransferredResourcesFolder(user, parent, folderName, false).getUUID();
  }

  @Override
  public void deleteTransferredResources(SelectedItems<TransferredResource> selected)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    Browser.deleteTransferredResources(user, selected);
  }

  @Override
  public boolean transferScanIsUpdating() {
    return Browser.retrieveScanUpdateStatus();
  }

  @Override
  public void transferScanRequestUpdate(String transferredResourceUUID) throws IsStillUpdatingException {
    try {
      Browser.updateAllTransferredResources(transferredResourceUUID, true);
    } catch (RuntimeException e) {
      LOGGER.error("Error running transferred resources scanner");
    }
  }

  @Override
  public JobBundle retrieveJobBundle(String jobId)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
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
  public Job createJob(Job job) throws RequestNotValidException, AuthorizationDeniedException, NotFoundException,
    GenericException, JobAlreadyStartedException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    return Jobs.createJob(user, job);
  }

  @Override
  public List<PluginInfo> retrievePluginsInfo(List<PluginType> types) {
    // TODO check permissions
    return RodaCoreFactory.getPluginManager().getPluginsInfo(types);
  }

  @Override
  public CreateIngestJobBundle retrieveCreateIngestProcessBundle() {
    // TODO check permissions
    CreateIngestJobBundle bundle = new CreateIngestJobBundle();
    bundle.setIngestPlugins(RodaCoreFactory.getPluginManager().getPluginsInfo(PluginType.INGEST));
    bundle.setSipToAipPlugins(RodaCoreFactory.getPluginManager().getPluginsInfo(PluginType.SIP_TO_AIP));
    return bundle;
  }

  public Viewers retrieveViewersProperties() {
    Viewers viewers = new Viewers();
    Configuration rodaConfig = RodaCoreFactory.getRodaConfiguration();
    List<String> viewersSupported = RodaUtils.copyList(rodaConfig.getList("ui.viewers"));

    for (String type : viewersSupported) {
      List<String> fieldPronoms = RodaUtils.copyList(rodaConfig.getList("ui.viewers." + type + ".pronoms"));
      List<String> fieldMimetypes = RodaUtils.copyList(rodaConfig.getList("ui.viewers." + type + ".mimetypes"));
      List<String> fieldExtensions = RodaUtils.copyList(rodaConfig.getList("ui.viewers." + type + ".extensions"));

      for (String pronom : fieldPronoms) {
        viewers.addPronom(pronom, type);
      }

      for (String mimetype : fieldMimetypes) {
        viewers.addMimetype(mimetype, type);
      }

      for (String extension : fieldExtensions) {
        viewers.addExtension(extension, type);
      }

    }

    return viewers;
  }

  @Override
  public List<SupportedMetadataTypeBundle> retrieveSupportedMetadata(String aipId, String localeString)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    Locale locale = ServerTools.parseLocale(localeString);
    return Browser.retrieveSupportedMetadata(user, aipId, locale);
  }

  @Override
  public PreservationEventViewBundle retrievePreservationEventViewBundle(String eventId)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.retrievePreservationEventViewBundle(user, eventId);
  }

  @Override
  public DescriptiveMetadataVersionsBundle retrieveDescriptiveMetadataVersionsBundle(String aipId,
    String descriptiveMetadataId, String localeString)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    Locale locale = ServerTools.parseLocale(localeString);
    return Browser.retrieveDescriptiveMetadataVersionsBundle(user, aipId, descriptiveMetadataId, locale);
  }

  @Override
  public void revertDescriptiveMetadataVersion(String aipId, String descriptiveMetadataId, String versionId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    Browser.revertDescriptiveMetadataVersion(user, aipId, descriptiveMetadataId, versionId);
  }

  @Override
  public void deleteDescriptiveMetadataVersion(String aipId, String descriptiveMetadataId, String versionId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    Browser.deleteDescriptiveMetadataVersion(user, aipId, descriptiveMetadataId, versionId);
  }

  @Override
  public void updateAIPPermissions(List<IndexedAIP> aips, Permissions permissions, boolean recursive)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    Browser.updateAIPPermissions(user, aips, permissions, recursive);
  }

  @Override
  public void updateRisk(Risk risk, String message)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    Browser.updateRisk(user, risk, message);
  }

  @Override
  public void updateFormat(Format format)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    Browser.updateFormat(user, format);
  }

  @Override
  public void updateAgent(Agent agent)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    Browser.updateAgent(user, agent);
  }

  @Override
  public Risk createRisk(Risk risk)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.createRisk(user, risk);
  }

  @Override
  public Format createFormat(Format format)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.createFormat(user, format);
  }

  @Override
  public Agent createAgent(Agent agent)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.createAgent(user, agent);
  }

  @Override
  public List<Format> retrieveFormats(String agentId)
    throws AuthorizationDeniedException, NotFoundException, GenericException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.retrieveFormats(user, agentId);
  }

  @Override
  public List<Agent> retrieveRequiredAgents(String agentId)
    throws AuthorizationDeniedException, NotFoundException, GenericException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.retrieveRequiredAgents(user, agentId);
  }

  @Override
  public void revertRiskVersion(String riskId, String versionId, String message)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException, IOException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    Browser.revertRiskVersion(user, riskId, versionId, message);
  }

  @Override
  public void deleteRiskVersion(String riskId, String versionId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException, IOException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    Browser.deleteRiskVersion(user, riskId, versionId);
  }

  @Override
  public RiskVersionsBundle retrieveRiskVersions(String riskId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException, IOException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.retrieveRiskVersions(user, riskId);
  }

  @Override
  public boolean hasRiskVersions(String id)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.hasRiskVersions(user, id);
  }

  @Override
  public Risk retrieveRiskVersion(String riskId, String selectedVersion)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException, IOException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.retrieveRiskVersion(user, riskId, selectedVersion);
  }

  @Override
  public RiskMitigationBundle retrieveShowMitigationTerms(int preMitigationProbability, int preMitigationImpact,
    int posMitigationProbability, int posMitigationImpact) throws AuthorizationDeniedException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.retrieveShowMitigationTerms(user, preMitigationProbability, preMitigationImpact,
      posMitigationProbability, posMitigationImpact);
  }

  @Override
  public List<String> retrieveMitigationSeverityLimits() throws AuthorizationDeniedException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.retrieveMitigationSeverityLimits(user);
  }

  @Override
  public MitigationPropertiesBundle retrieveAllMitigationProperties() throws AuthorizationDeniedException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.retrieveAllMitigationProperties(user);
  }

  @Override
  public void deleteRisk(SelectedItems<IndexedRisk> selected) throws AuthorizationDeniedException, GenericException,
    RequestNotValidException, NotFoundException, InvalidParameterException, JobAlreadyStartedException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());

    Browser.deleteRisk(user, selected);
  }

  @Override
  public void deleteAgent(SelectedItems<Agent> selected)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    Browser.deleteAgent(user, selected);
  }

  @Override
  public void deleteFormat(SelectedItems<Format> selected)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    Browser.deleteFormat(user, selected);
  }

  @Override
  public <T extends IsIndexed> Job createProcess(String jobName, SelectedItems<T> selected, String id,
    Map<String, String> value, String selectedClass) throws AuthorizationDeniedException, RequestNotValidException,
    NotFoundException, GenericException, JobAlreadyStartedException {

    RodaUser user = UserUtility.getUser(getThreadLocalRequest());

    if (selected instanceof SelectedItemsList) {
      SelectedItemsList items = (SelectedItemsList) selected;

      if (items.getIds().isEmpty()) {
        selected = getAllItemsByClass(selectedClass);
      }
    }

    Job job = new Job();
    job.setName(jobName);
    job.setSourceObjects(selected);
    job.setPlugin(id);
    job.setPluginParameters(value);

    return Jobs.createJob(user, job);
  }

  private SelectedItems getAllItemsByClass(String selectedClass) {
    if (selectedClass == null || Void.class.getName().equals(selectedClass)) {
      return new SelectedItemsNone<>();
    } else {
      return new SelectedItemsAll<>(selectedClass);
    }
  }

  @Override
  public void deleteRiskIncidences(String id, SelectedItems<RiskIncidence> incidences)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    Browser.deleteRiskIncidences(user, id, incidences);
  }

  @Override
  public void updateRiskCounters()
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    Browser.updateRiskCounters(user);
  }

  @Override
  public void appraisal(SelectedItems<IndexedAIP> selected, boolean accept, String rejectReason)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    Browser.appraisal(user, selected, accept, rejectReason);
  }

  @Override
  public IndexedRepresentation retrieveRepresentationById(String representationId)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.retrieveRepresentationById(user, representationId);
  }

  @Override
  public IndexedFile retrieveFileById(String fileId)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.retrieveFileById(user, fileId);
  }

  @Override
  public String renameTransferredResource(String transferredResourceId, String newName)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException,
    IsStillUpdatingException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.renameTransferredResource(user, transferredResourceId, newName);
  }

  @Override
  public String moveTransferredResource(SelectedItems selected, TransferredResource transferredResource)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, AlreadyExistsException,
    IsStillUpdatingException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.moveTransferredResource(user, selected, transferredResource);
  }

  @Override
  public List<TransferredResource> retrieveSelectedTransferredResource(SelectedItems<TransferredResource> selected)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.retrieveSelectedTransferredResource(user, selected);
  }

  @Override
  public void deleteFile(String fileUUID)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    Browser.deleteFile(user, fileUUID);
  }

  @Override
  public void updateRiskIncidence(RiskIncidence incidence) throws AuthorizationDeniedException, GenericException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    Browser.updateRiskIncidence(user, incidence);
  }

  @Override
  public void showLogs() throws AuthorizationDeniedException {
    RodaUser user = UserUtility.getUser(getThreadLocalRequest());
    Browser.showLogs(user);
  }

}
