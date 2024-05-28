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

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringEscapeUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.Messages;
import org.roda.core.common.RodaUtils;
import org.roda.core.common.SelectedItemsUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.LockingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.accessKey.AccessKey;
import org.roda.core.data.v2.accessKey.AccessKeys;
import org.roda.core.data.v2.common.ConversionProfile;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.FacetFieldResult;
import org.roda.core.data.v2.index.facet.FacetValue;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobMixIn;
import org.roda.core.data.v2.jobs.JobParallelism;
import org.roda.core.data.v2.jobs.JobPriority;
import org.roda.core.data.v2.jobs.JobUserDetails;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.central.DistributedInstances;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StringContentPayload;
import org.roda.core.util.IdUtils;
import org.roda.wui.api.controllers.ApplicationAuth;
import org.roda.wui.api.controllers.Browser;
import org.roda.wui.api.controllers.Jobs;
import org.roda.wui.api.controllers.RODAInstance;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.Viewers;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;
import org.roda.wui.client.browse.bundle.BrowseDipBundle;
import org.roda.wui.client.browse.bundle.BrowseFileBundle;
import org.roda.wui.client.browse.bundle.BrowseRepresentationBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataEditBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataVersionsBundle;
import org.roda.wui.client.browse.bundle.PreservationEventViewBundle;
import org.roda.wui.client.browse.bundle.RepresentationInformationFilterBundle;
import org.roda.wui.client.browse.bundle.SupportedMetadataTypeBundle;
import org.roda.wui.client.ingest.process.CreateIngestJobBundle;
import org.roda.wui.client.ingest.process.JobBundle;
import org.roda.wui.common.I18nUtility;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.server.ServerTools;
import org.roda.wui.servlets.ContextListener;
import org.roda_project.commons_ip.model.RepresentationContentType;
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
    try {
      User user = UserUtility.getUser(getThreadLocalRequest());
      Class<T> classToReturn = SelectedItemsUtils.parseClass(classNameToReturn);
      IndexResult<T> result = Browser.find(classToReturn, filter, sorter, sublist, facets, user, justActive,
        fieldsToReturn);
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
    return Browser.createAIP(user, parentId, type).getId();

  }

  @Override
  public Job deleteAIP(SelectedItems<IndexedAIP> aips, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.deleteAIP(user, aips, details);
  }

  @Override
  public Job deleteRepresentation(SelectedItems<IndexedRepresentation> representations, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.deleteRepresentation(user, representations, details);
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
  public JobBundle retrieveJobBundle(String jobId, List<String> fieldsToReturn) throws RODAException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Job job = Browser.retrieve(user, Job.class, jobId, fieldsToReturn);

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
            if (refPlugin != null) {
              pluginsInfo.add(refPlugin);
            } else {
              LOGGER.warn("Could not find plugin: " + pluginId);
            }
          }
        }
      }
    }

    // FIXME nvieira 20170208 it could possibly, in the future, be necessary to
    // add more plugin types adding all AIP to AIP plugins for job report list
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
  public List<PluginInfo> retrievePluginsInfo(List<PluginType> types) {
    // TODO check permissions
    return RodaCoreFactory.getPluginManager().getPluginsInfo(types);
  }

  @Override
  public Set<Pair<String, String>> retrieveReindexPluginObjectClasses() {
    // TODO check permissions
    Set<Pair<String, String>> classNames = new HashSet<>();
    List<Class<? extends IsRODAObject>> classes = PluginHelper.getReindexObjectClasses();
    classes.remove(Void.class);

    for (Class<? extends IsRODAObject> c : classes) {
      Pair<String, String> names = Pair.of(c.getSimpleName(), c.getName());
      classNames.add(names);
    }

    return classNames;
  }

  @Override
  public Set<Pair<String, String>> retrieveDropdownPluginItems(String parameterId, String localeString) {
    Set<Pair<String, String>> items = new HashSet<>();
    List<String> dropdownItems = RodaUtils
      .copyList(RodaCoreFactory.getRodaConfiguration().getList("core.plugins.dropdown." + parameterId + "[]"));
    Locale locale = ServerTools.parseLocale(localeString);
    Messages messages = RodaCoreFactory.getI18NMessages(locale);

    for (String item : dropdownItems) {
      String i18nProperty = RodaCoreFactory.getRodaConfiguration()
        .getString("core.plugins.dropdown." + parameterId + "[]." + item + ".i18n");
      items.add(Pair.of(messages.getTranslation(i18nProperty, item), item));
    }

    return items;
  }

  @Override
  public Set<ConversionProfile> retrieveConversionProfilePluginItems(String pluginId, String repOrDip,
    String localeString) {
    Set<ConversionProfile> items = new HashSet<>();

    String pluginName = RodaCoreFactory.getRodaConfiguration().getString("core.plugins.conversion.profile." + pluginId);

    List<String> dropdownItems = RodaUtils.copyList(
      RodaCoreFactory.getRodaConfiguration().getList("core.plugins.conversion.profile." + pluginName + ".profiles[]"));
    Locale locale = ServerTools.parseLocale(localeString);

    ResourceBundle pluginMessages = RodaCoreFactory.getPluginMessages(pluginId, locale);

    for (String item : dropdownItems) {
      ConversionProfile conversionProfile = retrieveConversionProfileItem(item, pluginName, pluginMessages);
      if (repOrDip.equals(RodaConstants.PLUGIN_PARAMS_CONVERSION_REPRESENTATION)
        && conversionProfile.canBeUsedForRepresentation()) {
        items.add(conversionProfile);
      }

      if (repOrDip.equals(RodaConstants.PLUGIN_PARAMS_CONVERSION_DISSEMINATION)
        && conversionProfile.canBeUsedForDissemination()) {
        items.add(conversionProfile);
      }
    }

    return items;
  }

  private ConversionProfile retrieveConversionProfileItem(String item, String pluginName,
    ResourceBundle resourceBundle) {
    ConversionProfile conversionProfile = new ConversionProfile();
    Map<String, String> optionsValues = new HashMap<>();

    String i18nKey = RodaCoreFactory.getRodaConfiguration()
      .getString("core.plugins.conversion.profile." + pluginName + ".profiles.i18nPrefix");

    String title;
    String description;

    try {
      title = resourceBundle.getString(i18nKey + "." + item + ".title");
    } catch (MissingResourceException e) {
      title = i18nKey + "." + item + ".title";
    }

    try {
      description = resourceBundle.getString(i18nKey + "." + item + ".description");
    } catch (MissingResourceException e) {
      description = i18nKey + "." + item + ".description";
    }

    conversionProfile.setTitle(title);
    conversionProfile.setDescription(description);
    conversionProfile.setProfile(item);

    conversionProfile.setCanBeUsedForDissemination(RodaCoreFactory.getRodaConfiguration()
      .getBoolean("core.plugins.conversion.profile." + pluginName + "." + item + ".canBeUsedForDissemination", false));
    conversionProfile.setCanBeUsedForRepresentation(RodaCoreFactory.getRodaConfiguration()
      .getBoolean("core.plugins.conversion.profile." + pluginName + "." + item + ".canBeUsedForRepresentation", false));

    String[] options = RodaCoreFactory.getRodaConfiguration()
      .getStringArray("core.plugins.conversion.profile." + pluginName + "." + item + ".options[]");
    for (String option : options) {
      String optionValue = RodaCoreFactory.getRodaConfiguration()
        .getString("core.plugins.conversion.profile." + pluginName + "." + item + "." + option);
      optionsValues.put(option, optionValue);
    }
    conversionProfile.setOptions(optionsValues);

    return conversionProfile;
  }

  @Override
  public CreateIngestJobBundle retrieveCreateIngestProcessBundle() {
    // TODO check permissions
    CreateIngestJobBundle bundle = new CreateIngestJobBundle();
    bundle.setIngestPlugins(RodaCoreFactory.getPluginManager().getPluginsInfo(PluginType.INGEST));
    bundle.setSipToAipPlugins(RodaCoreFactory.getPluginManager().getPluginsInfo(PluginType.SIP_TO_AIP));
    return bundle;
  }

  @Override
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

      viewers.setTextLimit(rodaConfig.getString("ui.viewers.text.limit", ""));
      viewers.setOptions(rodaConfig.getString("ui.viewers." + type + ".options", ""));
    }

    return viewers;
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

  @Override
  public Job updateDIPPermissions(SelectedItems<IndexedDIP> dips, Permissions permissions, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.updateDIPPermissions(user, dips, permissions, details);
  }

  @Override
  public <T extends IsIndexed> Job createProcess(String jobName, JobPriority priority, JobParallelism parallelism,
    SelectedItems<T> selected, String id, Map<String, String> value, String selectedClass)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException,
    JobAlreadyStartedException {
    SelectedItems<T> selectedItems = selected;
    User user = UserUtility.getUser(getThreadLocalRequest());

    if (selectedItems instanceof SelectedItemsList) {
      SelectedItemsList<T> items = (SelectedItemsList<T>) selectedItems;

      if (items.getIds().isEmpty()) {
        selectedItems = getAllItemsByClass(selectedClass);
      }
    }

    Job job = new Job();
    job.setName(jobName);
    job.setSourceObjects(selectedItems);
    job.setPlugin(id);
    job.setPluginParameters(value);
    job.setUsername(user.getName());
    job.setPriority(priority);
    job.setParallelism(parallelism);

    return Jobs.createJob(user, job, true);
  }

  @Override
  public <T extends IsIndexed> Job createProcess(String jobName, SelectedItems<T> selected, String id,
    Map<String, String> value, String selectedClass) throws AuthorizationDeniedException, RequestNotValidException,
    NotFoundException, GenericException, JobAlreadyStartedException {
    return createProcess(jobName, JobPriority.MEDIUM, JobParallelism.NORMAL, selected, id, value, selectedClass);
  }

  @Override
  public <T extends IsIndexed> String createProcessJson(String jobName, JobPriority priority,
    JobParallelism parallelism, SelectedItems<T> selected, String id, Map<String, String> value, String selectedClass)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException,
    JobAlreadyStartedException {
    SelectedItems<T> selectedItems = selected;
    User user = UserUtility.getUser(getThreadLocalRequest());

    if (selectedItems instanceof SelectedItemsList) {
      SelectedItemsList<T> items = (SelectedItemsList<T>) selectedItems;

      if (items.getIds().isEmpty()) {
        selectedItems = getAllItemsByClass(selectedClass);
      }
    }

    JobUserDetails jobUserDetails = new JobUserDetails();
    jobUserDetails.setUsername(user.getName());
    jobUserDetails.setEmail(user.getEmail());
    jobUserDetails.setFullname(user.getFullName());
    jobUserDetails.setRole(RodaConstants.PreservationAgentRole.IMPLEMENTER.toString());

    Job job = new Job();
    job.setId(IdUtils.createUUID());
    job.setName(jobName);
    job.setSourceObjects(selectedItems);
    job.setPlugin(id);
    job.setPluginParameters(value);
    job.setUsername(user.getName());
    job.setPriority(priority);
    job.setParallelism(parallelism);
    job.getJobUsersDetails().add(jobUserDetails);

    String command = RodaCoreFactory.getRodaConfiguration().getString("ui.createJob.curl");
    if (command != null) {
      command = command.replace("{{jsonObject}}",
        StringEscapeUtils.escapeJava(JsonUtils.getJsonFromObject(job, JobMixIn.class)));

      command = command.replace("{{RODA_CONTEXT_PATH}}",
        StringEscapeUtils.escapeJava(ContextListener.getServletContext().getContextPath()));
      return command;
    } else {
      return "";
    }
  }

  @Override
  public <T extends IsIndexed> String createProcessJson(String jobName, SelectedItems<T> selected, String id,
    Map<String, String> value, String selectedClass) throws AuthorizationDeniedException, RequestNotValidException,
    NotFoundException, GenericException, JobAlreadyStartedException {
    return createProcessJson(jobName, JobPriority.MEDIUM, JobParallelism.NORMAL, selected, id, value, selectedClass);
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
  public Job deleteRiskIncidences(SelectedItems<RiskIncidence> selected, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.deleteRiskIncidences(user, selected, details);
  }

  @Override
  public String createRepresentation(String aipId, String details) throws AuthorizationDeniedException,
    GenericException, NotFoundException, RequestNotValidException, AlreadyExistsException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    String representationId = IdUtils.createUUID();
    Browser.createRepresentation(user, aipId, representationId, RepresentationContentType.getMIXED().asString(),
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
    return Browser.changeRepresentationType(user, selected, newType, details);
  }

  @Override
  public Job changeAIPType(SelectedItems<IndexedAIP> selected, String newType, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.changeAIPType(user, selected, newType, details);
  }

  @Override
  public void changeRepresentationStates(IndexedRepresentation representation, List<String> newStates, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    Browser.changeRepresentationStates(user, representation, newStates, details);
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
    return Browser.deleteDIPs(user, dips, details);
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
    return RodaCoreFactory.getRodaConfiguration().getBoolean("ui.dip.externalURL.showEmbedded", false);
  }

  @Override
  public Notification acknowledgeNotification(String notificationId, String ackToken)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Browser.acknowledgeNotification(user, notificationId, ackToken);
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
      try {
        Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.AIP_TYPE));
        IndexResult<IndexedAIP> result = find(IndexedAIP.class.getName(), Filter.ALL, Sorter.NONE, Sublist.NONE, facets,
          locale, false, new ArrayList<>());

        List<FacetFieldResult> facetResults = result.getFacetResults();
        for (FacetValue facetValue : facetResults.get(0).getValues()) {
          types.add(facetValue.getValue());
        }
      } catch (GenericException | AuthorizationDeniedException | RequestNotValidException e) {
        LOGGER.error("Could not execute find request on AIPs", e);
      }
    }

    return Pair.of(isControlled, types);
  }

  @Override
  public Pair<Boolean, List<String>> retrieveRepresentationTypeOptions(String locale) {
    List<String> types = new ArrayList<>();
    boolean isControlled = RodaCoreFactory.getRodaConfiguration()
      .getBoolean("core.representation_type.controlled_vocabulary", false);

    if (isControlled) {
      types = RodaCoreFactory.getRodaConfigurationAsList("core.representation_type.value");
    } else {
      try {
        Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.REPRESENTATION_TYPE));
        IndexResult<IndexedRepresentation> result = find(IndexedRepresentation.class.getName(), Filter.ALL, Sorter.NONE,
          Sublist.NONE, facets, locale, false, new ArrayList<>());

        List<FacetFieldResult> facetResults = result.getFacetResults();
        for (FacetValue facetValue : facetResults.get(0).getValues()) {
          types.add(facetValue.getValue());
        }

        Boolean flag = false;

        for (String word : types) {
          if (word.equals("MIXED")) {
            flag = true;
            break;
          }
        }

        if (!flag)
          types.add("MIXED");
      } catch (GenericException | AuthorizationDeniedException | RequestNotValidException e) {
        LOGGER.error("Could not execute find request on representations", e);
      }
    }

    return Pair.of(isControlled, types);
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

  public List<Report> retrieveJobReportItems(String jobId, String jobReportId)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    User user = UserUtility.getUser(getThreadLocalRequest());
    return Jobs.retrieveJobReportItems(user, jobId, jobReportId);
  }
}
