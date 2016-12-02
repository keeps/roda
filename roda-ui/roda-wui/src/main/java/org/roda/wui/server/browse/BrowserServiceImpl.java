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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.configuration.Configuration;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.IdUtils;
import org.roda.core.common.Messages;
import org.roda.core.common.RodaUtils;
import org.roda.core.common.SelectedItemsUtils;
import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
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
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StringContentPayload;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.controllers.Jobs;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.Viewers;
import org.roda.wui.client.browse.bundle.BrowseItemBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataEditBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataVersionsBundle;
import org.roda.wui.client.browse.bundle.DipBundle;
import org.roda.wui.client.browse.bundle.PreservationEventViewBundle;
import org.roda.wui.client.browse.bundle.SupportedMetadataTypeBundle;
import org.roda.wui.client.common.search.SearchField;
import org.roda.wui.client.common.utils.Tree;
import org.roda.wui.client.ingest.process.CreateIngestJobBundle;
import org.roda.wui.client.ingest.process.JobBundle;
import org.roda.wui.client.planning.MitigationPropertiesBundle;
import org.roda.wui.client.planning.RiskMitigationBundle;
import org.roda.wui.client.planning.RiskVersionsBundle;
import org.roda.wui.common.I18nUtility;
import org.roda.wui.common.server.ServerTools;
import org.roda_project.commons_ip.model.RepresentationContentType;
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
  protected void doUnexpectedFailure(Throwable e) {
    LOGGER.error("Unexpected failure", e);
    super.doUnexpectedFailure(e);
  }

  @Override
  public boolean isCookiesMessageActive() {
    return RodaCoreFactory.getRodaConfiguration().getBoolean(COOKIES_ACTIVE_PROPERTY, false);
  }

  @Override
  public String retrieveGoogleAnalyticsAccount() {
    if (GANALYTICS_ACCOUNT_CODE == null) {
      GANALYTICS_ACCOUNT_CODE = RodaCoreFactory.getRodaConfiguration().getString(GANALYTICS_CODE_PROPERTY, "");
      LOGGER.debug("Google Analytics Account Code: {}", GANALYTICS_ACCOUNT_CODE);
    }
    return GANALYTICS_ACCOUNT_CODE;
  }

  @Override
  public String retrieveGoogleReCAPTCHAAccount() {
    if (GRECAPTCHA_ACCOUNT_CODE == null) {
      GRECAPTCHA_ACCOUNT_CODE = RodaCoreFactory.getRodaConfiguration().getString(GRECAPTCHA_CODE_PROPERTY, "");
      LOGGER.debug("Google ReCAPTCHA Account Code: {}", GRECAPTCHA_ACCOUNT_CODE);
    }
    return GRECAPTCHA_ACCOUNT_CODE;
  }

  @Override
  public BrowseItemBundle retrieveItemBundle(String aipId, String localeString)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Locale locale = ServerTools.parseLocale(localeString);
    return Browser.retrieveItemBundle(user, aipId, locale);
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
    Sublist sublist, Facets facets, String localeString, boolean justActive)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException {
    try {
      User user = UserUtility.getUser(getThreadLocalRequest());
      Class<T> classToReturn = SelectedItemsUtils.parseClass(classNameToReturn);
      IndexResult<T> result = Browser.find(classToReturn, filter, sorter, sublist, facets, user, justActive);
      return I18nUtility.translate(result, classToReturn, localeString);
    } catch (RuntimeException e) {
      LOGGER.error("Unexpected error in find", e);
      throw new GenericException(e);
    } catch (GenericException e) {
      LOGGER.error("Unexpected error in find", e);
      throw e;

    }
  }

  @Override
  public <T extends IsIndexed> Long count(String classNameToReturn, Filter filter)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Class<T> classToReturn = SelectedItemsUtils.parseClass(classNameToReturn);
    return Browser.count(user, classToReturn, filter);
  }

  @Override
  public <T extends IsIndexed> T retrieve(String classNameToReturn, String id)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Class<T> classToReturn = SelectedItemsUtils.parseClass(classNameToReturn);
    return Browser.retrieve(user, classToReturn, id);
  }

  @Override
  public <T extends IsIndexed> List<T> retrieve(String classNameToReturn, SelectedItems<T> selectedItems)
    throws GenericException, AuthorizationDeniedException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Class<T> classToReturn = SelectedItemsUtils.parseClass(classNameToReturn);
    return Browser.retrieve(user, classToReturn, selectedItems);
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

  public List<IndexedAIP> retrieveAncestors(IndexedAIP aip)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.retrieveAncestors(user, aip);
  }

  @Override
  public List<SearchField> retrieveSearchFields(String className, String localeString) throws GenericException {
    List<SearchField> searchFields = new ArrayList<SearchField>();
    List<String> fields = RodaUtils.copyList(RodaCoreFactory.getRodaConfiguration()
      .getList(RodaCoreFactory.getConfigurationKey(RodaConstants.SEARCH_FIELD_PREFIX, className)));
    Locale locale = ServerTools.parseLocale(localeString);
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    for (String field : fields) {
      SearchField searchField = new SearchField();
      String fieldsNames = RodaCoreFactory.getRodaConfigurationAsString(RodaConstants.SEARCH_FIELD_PREFIX, className,
        field, RodaConstants.SEARCH_FIELD_FIELDS);
      String fieldType = RodaCoreFactory.getRodaConfigurationAsString(RodaConstants.SEARCH_FIELD_PREFIX, className,
        field, RodaConstants.SEARCH_FIELD_TYPE);
      String fieldLabelI18N = RodaCoreFactory.getRodaConfigurationAsString(RodaConstants.SEARCH_FIELD_PREFIX, className,
        field, RodaConstants.SEARCH_FIELD_I18N);
      String fieldI18NPrefix = RodaCoreFactory.getRodaConfigurationAsString(RodaConstants.SEARCH_FIELD_PREFIX,
        className, field, RodaConstants.SEARCH_FIELD_I18N_PREFIX);
      List<String> fieldsValues = RodaCoreFactory.getRodaConfigurationAsList(RodaConstants.SEARCH_FIELD_PREFIX,
        className, field, RodaConstants.SEARCH_FIELD_VALUES);
      String suggestField = RodaCoreFactory.getRodaConfigurationAsString(RodaConstants.SEARCH_FIELD_PREFIX, className,
        field, RodaConstants.SEARCH_FIELD_TYPE_SUGGEST_FIELD);

      boolean fieldFixed = Boolean.valueOf(RodaCoreFactory.getRodaConfigurationAsString(
        RodaConstants.SEARCH_FIELD_PREFIX, className, field, RodaConstants.SEARCH_FIELD_FIXED));
      boolean suggestPartial = Boolean.valueOf(RodaCoreFactory.getRodaConfigurationAsString(
        RodaConstants.SEARCH_FIELD_PREFIX, className, field, RodaConstants.SEARCH_FIELD_TYPE_SUGGEST_PARTIAL));

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

        if (fieldsValues != null) {
          Map<String, String> labels = messages.getTranslations(fieldI18NPrefix, String.class, false);
          Tree<String> terms = new Tree<String>(field, field);
          // terms.addChild("", "");
          for (String value : fieldsValues) {
            terms.addChild(labels.get(fieldI18NPrefix + "." + value), value);
          }
          searchField.setTerms(terms);
        }

        if (suggestField != null) {
          searchField.setSuggestField(suggestField);
        }
        searchField.setSuggestPartial(suggestPartial);

        searchFields.add(searchField);
      }
    }

    return searchFields;
  }

  @Override
  public IndexedAIP moveAIPInHierarchy(SelectedItems<IndexedAIP> selected, String parentId, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException,
    AlreadyExistsException, ValidationException {
    try {
      User user = UserUtility.getUser(getThreadLocalRequest());
      return Browser.moveAIPInHierarchy(user, selected, parentId, details);
    } catch (Throwable e) {
      LOGGER.error("Unexpected error", e);
      throw e;
    }
  }

  @Override
  public String createAIP(String parentId, String type) throws AuthorizationDeniedException, GenericException,
    NotFoundException, RequestNotValidException, AlreadyExistsException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.createAIP(user, parentId, type).getId();

  }

  @Override
  public String deleteAIP(SelectedItems<IndexedAIP> aips)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.deleteAIP(user, aips);
  }

  @Override
  public void deleteRepresentation(SelectedItems<IndexedRepresentation> representations, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Browser.deleteRepresentation(user, representations, details);
  }

  @Override
  public void deleteFile(SelectedItems<IndexedFile> files, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Browser.deleteFile(user, files, details);
  }

  @Override
  public void createDescriptiveMetadataFile(String aipId, String representationId, DescriptiveMetadataEditBundle bundle)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException,
    AlreadyExistsException, ValidationException {
    User user = UserUtility.getUser(getThreadLocalRequest());

    // If the bundle has values from the form, we need to update the XML by
    // applying the values of the form to the raw template
    if (bundle.getValues() != null) {
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

  public void deleteDescriptiveMetadataFile(String aipId, String representationId, String descriptiveMetadataId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Browser.deleteDescriptiveMetadataFile(user, aipId, representationId, descriptiveMetadataId);
  }

  public String createTransferredResourcesFolder(String parent, String folderName)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.createTransferredResourcesFolder(user, parent, folderName, false).getUUID();
  }

  @Override
  public void deleteTransferredResources(SelectedItems<TransferredResource> selected)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Browser.deleteTransferredResources(user, selected);
  }

  @Override
  public void transferScanRequestUpdate(String transferredResourceRelativePath)
    throws IsStillUpdatingException, AuthorizationDeniedException {
    try {
      User user = UserUtility.getUser(getThreadLocalRequest());
      Browser.updateTransferredResources(user,
        transferredResourceRelativePath != null ? Optional.of(transferredResourceRelativePath) : Optional.empty(),
        true);
    } catch (RuntimeException | GenericException e) {
      LOGGER.error("Error running transferred resources scanner");
    }
  }

  @Override
  public JobBundle retrieveJobBundle(String jobId)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Job job = Browser.retrieve(user, Job.class, jobId);

    List<PluginInfo> pluginsInfo = new ArrayList<>();

    PluginInfo basePlugin = RodaCoreFactory.getPluginManager().getPluginInfo(job.getPlugin());

    if (basePlugin != null) {
      pluginsInfo.add(basePlugin);

      for (PluginParameter parameter : basePlugin.getParameters()) {
        if (PluginParameterType.PLUGIN_SIP_TO_AIP.equals(parameter.getType())) {
          String pluginId = job.getPluginParameters().get(parameter.getId());
          if (pluginId == null) {
            pluginId = parameter.getDefaultValue();
          }
          if (pluginId != null) {
            PluginInfo refPlugin = RodaCoreFactory.getPluginManager().getPluginInfo(pluginId);
            pluginsInfo.add(refPlugin);
          }
        }
      }
    }

    // adding all AIP to AIP plugins for job report list
    List<PluginInfo> aipToAipPlugins = RodaCoreFactory.getPluginManager().getPluginsInfo(PluginType.AIP_TO_AIP);
    if (aipToAipPlugins != null) {
      pluginsInfo.addAll(aipToAipPlugins);
    }

    JobBundle bundle = new JobBundle();
    bundle.setJob(job);
    bundle.setPluginsInfo(pluginsInfo);
    return bundle;
  }

  @Override
  public Job createJob(Job job) throws RequestNotValidException, AuthorizationDeniedException, NotFoundException,
    GenericException, JobAlreadyStartedException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Jobs.createJob(user, job);
  }

  @Override
  public void stopJob(String jobId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Jobs.stopJob(user, jobId);
  }

  @Override
  public List<PluginInfo> retrievePluginsInfo(List<PluginType> types) {
    // TODO check permissions
    return RodaCoreFactory.getPluginManager().getPluginsInfo(types);
  }

  @Override
  public Set<Pair<String, String>> retrieveReindexPluginObjectClasses() {
    // TODO check permissions
    Set<Pair<String, String>> classNames = new HashSet<>();
    List<?> classes = PluginHelper.getReindexObjectClasses();
    classes.remove(Void.class);

    for (Object o : classes) {
      Class c = (Class) o;
      Pair<String, String> names = new Pair<String, String>(c.getSimpleName(), c.getName());
      classNames.add(names);
    }

    return classNames;
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

      viewers.setTextLimit(rodaConfig.getString("ui.viewers.text.limit"));
    }

    return viewers;
  }

  @Override
  public List<SupportedMetadataTypeBundle> retrieveSupportedMetadata(String aipId, String representationUUID,
    String localeString) throws AuthorizationDeniedException, GenericException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Locale locale = ServerTools.parseLocale(localeString);
    return Browser.retrieveSupportedMetadata(user, aipId, representationUUID, locale);
  }

  @Override
  public PreservationEventViewBundle retrievePreservationEventViewBundle(String eventId)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.retrievePreservationEventViewBundle(user, eventId);
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
  public void updateAIPPermissions(List<IndexedAIP> aips, Permissions permissions, String details, boolean recursive)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Browser.updateAIPPermissions(user, aips, permissions, details, recursive);
  }

  @Override
  public void updateRisk(Risk risk)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Browser.updateRisk(user, risk);
  }

  @Override
  public void updateFormat(Format format)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Browser.updateFormat(user, format);
  }

  @Override
  public Risk createRisk(Risk risk)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.createRisk(user, risk);
  }

  @Override
  public Format createFormat(Format format)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.createFormat(user, format);
  }

  @Override
  public void revertRiskVersion(String riskId, String versionId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException, IOException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Browser.revertRiskVersion(user, riskId, versionId);
  }

  @Override
  public void deleteRiskVersion(String riskId, String versionId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException, IOException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Browser.deleteRiskVersion(user, riskId, versionId);
  }

  @Override
  public RiskVersionsBundle retrieveRiskVersions(String riskId)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException, IOException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.retrieveRiskVersions(user, riskId);
  }

  @Override
  public boolean hasRiskVersions(String id)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.hasRiskVersions(user, id);
  }

  @Override
  public Risk retrieveRiskVersion(String riskId, String selectedVersion)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException, IOException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.retrieveRiskVersion(user, riskId, selectedVersion);
  }

  @Override
  public RiskMitigationBundle retrieveShowMitigationTerms(int preMitigationProbability, int preMitigationImpact,
    int posMitigationProbability, int posMitigationImpact) throws AuthorizationDeniedException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.retrieveShowMitigationTerms(user, preMitigationProbability, preMitigationImpact,
      posMitigationProbability, posMitigationImpact);
  }

  @Override
  public List<String> retrieveMitigationSeverityLimits() throws AuthorizationDeniedException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.retrieveMitigationSeverityLimits(user);
  }

  @Override
  public MitigationPropertiesBundle retrieveAllMitigationProperties() throws AuthorizationDeniedException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.retrieveAllMitigationProperties(user);
  }

  @Override
  public void deleteRisk(SelectedItems<IndexedRisk> selected) throws AuthorizationDeniedException, GenericException,
    RequestNotValidException, NotFoundException, InvalidParameterException, JobAlreadyStartedException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Browser.deleteRisk(user, selected);
  }

  @Override
  public void deleteFormat(SelectedItems<Format> selected)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Browser.deleteFormat(user, selected);
  }

  @Override
  public <T extends IsIndexed> Job createProcess(String jobName, SelectedItems<T> selected, String id,
    Map<String, String> value, String selectedClass) throws AuthorizationDeniedException, RequestNotValidException,
    NotFoundException, GenericException, JobAlreadyStartedException {

    User user = UserUtility.getUser(getThreadLocalRequest());

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
  public void updateRiskCounters()
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Browser.updateRiskCounters(user);
  }

  @Override
  public void appraisal(SelectedItems<IndexedAIP> selected, boolean accept, String rejectReason)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Browser.appraisal(user, selected, accept, rejectReason);
  }

  @Override
  public IndexedRepresentation retrieveRepresentationById(String representationId)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.retrieveRepresentationById(user, representationId);
  }

  @Override
  public IndexedFile retrieveFileById(String fileId)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.retrieveFileById(user, fileId);
  }

  @Override
  public String renameTransferredResource(String transferredResourceId, String newName)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException,
    IsStillUpdatingException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.renameTransferredResource(user, transferredResourceId, newName);
  }

  @Override
  public String renameFolder(String folderUUID, String newName, String details) throws AuthorizationDeniedException,
    GenericException, RequestNotValidException, AlreadyExistsException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.renameFolder(user, folderUUID, newName, details);
  }

  @Override
  public String moveFiles(String aipId, String representationUUID, SelectedItems<IndexedFile> selectedFiles,
    IndexedFile toFolder, String details) throws AuthorizationDeniedException, GenericException,
    RequestNotValidException, AlreadyExistsException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.moveFiles(user, aipId, representationUUID, selectedFiles, toFolder, details);
  }

  @Override
  public String createFolder(String aipId, String representationUUID, String folderUUID, String newName, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, AlreadyExistsException,
    NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.createFolder(user, aipId, representationUUID, folderUUID, newName, details);
  }

  @Override
  public String moveTransferredResource(SelectedItems selected, TransferredResource transferredResource)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, AlreadyExistsException,
    IsStillUpdatingException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.moveTransferredResource(user, selected, transferredResource);
  }

  @Override
  public List<TransferredResource> retrieveSelectedTransferredResource(SelectedItems<TransferredResource> selected)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.retrieveSelectedTransferredResource(user, selected);
  }

  @Override
  public void deleteFile(String fileUUID)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Browser.deleteFile(user, fileUUID);
  }

  @Override
  public void updateRiskIncidence(RiskIncidence incidence) throws AuthorizationDeniedException, GenericException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Browser.updateRiskIncidence(user, incidence);
  }

  @Override
  public void deleteRiskIncidences(SelectedItems<RiskIncidence> selected)
    throws JobAlreadyStartedException, AuthorizationDeniedException, GenericException, RequestNotValidException,
    NotFoundException, InvalidParameterException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Browser.deleteRiskIncidences(user, selected);
  }

  @Override
  public void updateMultipleIncidences(SelectedItems<RiskIncidence> selected, String status, String severity,
    Date mitigatedOn, String mitigatedBy, String mitigatedDescription)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Browser.updateMultipleIncidences(user, selected, status, severity, mitigatedOn, mitigatedBy, mitigatedDescription);
  }

  @Override
  public String createRepresentation(String aipId, String details) throws AuthorizationDeniedException,
    GenericException, NotFoundException, RequestNotValidException, AlreadyExistsException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    String representationId = UUID.randomUUID().toString();
    Browser.createRepresentation(user, aipId, representationId, RepresentationContentType.getMIXED().asString(),
      details);
    return IdUtils.getRepresentationId(aipId, representationId);
  }

  @Override
  public void createFormatIdentificationJob(SelectedItems selected)
    throws GenericException, AuthorizationDeniedException, JobAlreadyStartedException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Browser.createFormatIdentificationJob(user, selected);
  }

  @Override
  public void changeRepresentationType(SelectedItemsList<IndexedRepresentation> selected, String newType,
    String details) throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Browser.changeRepresentationType(user, selected, newType, details);
  }

  @Override
  public DipBundle getDipBundle(String dipUUID, String aipUUID, String representationUUID, String fileUUID)
    throws RequestNotValidException, AuthorizationDeniedException, GenericException, NotFoundException {
    DipBundle bundle = new DipBundle();
    User user = UserUtility.getUser(getThreadLocalRequest());

    if (dipUUID != null) {
      bundle.setDip(Browser.retrieve(user, IndexedDIP.class, dipUUID));
    } else {
      throw new RequestNotValidException("DIP id must be defined in request and it was null");
    }

    if (aipUUID != null) {
      bundle.setAip(Browser.retrieve(user, IndexedAIP.class, aipUUID));
    }

    if (representationUUID != null) {
      bundle.setRepresentation(Browser.retrieve(user, IndexedRepresentation.class, representationUUID));
    }

    if (fileUUID != null) {
      bundle.setFile(Browser.retrieve(user, IndexedFile.class, fileUUID));
    }

    return bundle;
  }

}
