package org.roda.wui.api.v2.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.DownloadUtils;
import org.roda.core.common.HandlebarsUtility;
import org.roda.core.common.Messages;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.RodaUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.iterables.CloseableIterables;
import org.roda.core.common.tools.ZipEntryInfo;
import org.roda.core.common.validation.ValidationUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.utils.URNUtils;
import org.roda.core.data.v2.ConsumesOutputStream;
import org.roda.core.data.v2.DefaultConsumesOutputStream;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.core.data.v2.generics.UpdatePermissionsRequest;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.ConfiguredDescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.ConfiguredDescriptiveMetadataList;
import org.roda.core.data.v2.ip.metadata.CreateDescriptiveMetadataRequest;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfo;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfos;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataRequestXML;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataVersions;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.ResourceVersion;
import org.roda.core.data.v2.ip.metadata.SelectedType;
import org.roda.core.data.v2.ip.metadata.SupportedMetadataValue;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.data.v2.validation.ValidationReport;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.model.ModelService;
import org.roda.core.model.TransactionalModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.plugins.base.maintenance.ChangeTypePlugin;
import org.roda.core.plugins.base.maintenance.DeleteRODAObjectPlugin;
import org.roda.core.plugins.base.maintenance.MovePlugin;
import org.roda.core.plugins.base.maintenance.UpdatePermissionsPlugin;
import org.roda.core.plugins.base.preservation.AppraisalPlugin;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryConsumesOutputStream;
import org.roda.core.storage.BinaryVersion;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StringContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.common.HTMLUtils;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.server.ServerTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.ElementSelectors;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Service
public class AIPService {
  private static final Logger LOGGER = LoggerFactory.getLogger(AIPService.class);
  private static final String HTML_EXT = ".html";
  private static final String XML_EXT = ".xml";

  public StreamResponse downloadAIPDescriptiveMetadata(String aipId, String metadataId, String versionId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    ModelService modelService = RodaCoreFactory.getModelService();
    Optional<LiteRODAObject> liteMetadata = LiteRODAObjectFactory.get(DescriptiveMetadata.class, aipId, metadataId);
    if (liteMetadata.isEmpty()) {
      throw new RequestNotValidException("Could not retrieve metadata " + metadataId + " lite for AIP " + aipId);
    }
    Binary descriptiveMetadataBinary;
    if (versionId == null) {
      descriptiveMetadataBinary = modelService.getBinary(liteMetadata.get());
    } else {
      BinaryVersion binaryVersion = modelService.getBinaryVersion(liteMetadata.get(), versionId, new ArrayList<>());
      descriptiveMetadataBinary = binaryVersion.getBinary();
    }

    return new StreamResponse(
      new BinaryConsumesOutputStream(descriptiveMetadataBinary, RodaConstants.MEDIA_TYPE_APPLICATION_XML));
  }

  public StreamResponse retrieveAIPDescriptiveMetadata(String aipId, String metadataId, String versionId,
    String localeString)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    ModelService model = RodaCoreFactory.getModelService();
    Binary descriptiveMetadataBinary;
    if (versionId != null) {
      Optional<LiteRODAObject> liteDM = LiteRODAObjectFactory.get(DescriptiveMetadata.class, aipId, metadataId);
      if (liteDM.isEmpty()) {
        throw new RequestNotValidException("Could not get LITE from AIP " + aipId + " with metadata id" + metadataId);
      }
      BinaryVersion binaryVersion = model.getBinaryVersion(liteDM.get(), versionId, new ArrayList<>());
      descriptiveMetadataBinary = binaryVersion.getBinary();
    } else {
      descriptiveMetadataBinary = model.retrieveDescriptiveMetadataBinary(aipId, metadataId);
    }

    String filename = descriptiveMetadataBinary.getStoragePath().getName() + HTML_EXT;
    DescriptiveMetadata descriptiveMetadata = model.retrieveDescriptiveMetadata(aipId, metadataId);
    String htmlDescriptive = HTMLUtils.descriptiveMetadataToHtml(descriptiveMetadataBinary,
      descriptiveMetadata.getType(), descriptiveMetadata.getVersion(), ServerTools.parseLocale(localeString));

    ConsumesOutputStream stream = new DefaultConsumesOutputStream(filename, RodaConstants.MEDIA_TYPE_APPLICATION_XML,
      out -> {
        PrintStream printStream = new PrintStream(out);
        printStream.print(htmlDescriptive);
        printStream.close();
      });

    return new StreamResponse(stream);
  }

  public StreamResponse downloadRepresentationDescriptiveMetadata(String aipId, String representationId,
    String metadataId, String versionId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    ModelService modelService = RodaCoreFactory.getModelService();
    Optional<LiteRODAObject> liteMetadata = LiteRODAObjectFactory.get(DescriptiveMetadata.class, aipId,
      representationId, metadataId);
    if (liteMetadata.isEmpty()) {
      throw new RequestNotValidException("Could not retrieve metadata " + metadataId + " lite for AIP " + aipId
        + ", representation " + representationId);
    }
    Binary descriptiveMetadataBinary;
    if (versionId == null) {
      descriptiveMetadataBinary = modelService.getBinary(liteMetadata.get());
    } else {
      BinaryVersion binaryVersion = modelService.getBinaryVersion(liteMetadata.get(), versionId, new ArrayList<>());
      descriptiveMetadataBinary = binaryVersion.getBinary();
    }

    return new StreamResponse(
      new BinaryConsumesOutputStream(descriptiveMetadataBinary, RodaConstants.MEDIA_TYPE_APPLICATION_XML));
  }

  public StreamResponse retrieveRepresentationDescriptiveMetadata(String aipId, String representationId,
    String metadataId, String versionId, String localeString)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    ModelService model = RodaCoreFactory.getModelService();
    Binary descriptiveMetadataBinary;
    if (versionId != null) {
      Optional<LiteRODAObject> liteDM = LiteRODAObjectFactory.get(DescriptiveMetadata.class, aipId, representationId,
        metadataId);
      if (liteDM.isEmpty()) {
        throw new RequestNotValidException("Could not get LITE from representation " + representationId + " of AIP "
          + aipId + " with metadata id" + metadataId);
      }
      BinaryVersion binaryVersion = model.getBinaryVersion(liteDM.get(), versionId, new ArrayList<>());
      descriptiveMetadataBinary = binaryVersion.getBinary();
    } else {
      descriptiveMetadataBinary = model.retrieveDescriptiveMetadataBinary(aipId, representationId, metadataId);
    }

    String filename = descriptiveMetadataBinary.getStoragePath().getName() + HTML_EXT;
    DescriptiveMetadata descriptiveMetadata = model.retrieveDescriptiveMetadata(aipId, representationId, metadataId);
    String htmlDescriptive = HTMLUtils.descriptiveMetadataToHtml(descriptiveMetadataBinary,
      descriptiveMetadata.getType(), descriptiveMetadata.getVersion(), ServerTools.parseLocale(localeString));

    ConsumesOutputStream stream = new DefaultConsumesOutputStream(filename, RodaConstants.MEDIA_TYPE_APPLICATION_XML,
      out -> {
        PrintStream printStream = new PrintStream(out);
        printStream.print(htmlDescriptive);
        printStream.close();
      });

    return new StreamResponse(stream);
  }

  public List<IndexedAIP> getAncestors(IndexedAIP indexedAIP, User user) throws GenericException {
    return RodaCoreFactory.getIndexService().retrieveAncestors(indexedAIP, user, new ArrayList<>());
  }

  public Job deleteAIP(User user, SelectedItems<IndexedAIP> selected, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, details);
    return CommonServicesUtils.createAndExecuteInternalJob("Delete AIP", selected, DeleteRODAObjectPlugin.class, user,
      pluginParameters, "Could not execute AIP delete action");
  }

  public List<IndexedAIP> retrieveAncestors(IndexedAIP aip, User user, List<String> fieldsToReturn)
    throws GenericException {
    return RodaCoreFactory.getIndexService().retrieveAncestors(aip, user, fieldsToReturn);
  }

  public List<String> getRepresentationInformation(User user) {
    if (UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_FIND_REPRESENTATION_INFORMATION)) {
      return RodaCoreFactory.getRodaConfigurationAsList("ui.ri.rule.AIP").stream()
        .map(r -> RodaCoreFactory.getRodaConfigurationAsString(r, RodaConstants.SEARCH_FIELD_FIELDS)).toList();
    } else {
      return Collections.emptyList();
    }
  }

  public DescriptiveMetadataInfos retrieveDescriptiveMetadataList(User user, String aipId, final Locale locale)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    if (UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_LIST_AIP_DESCRIPTIVE_METADATA)) {
      return retrieveDescriptiveMetadataInfos(aipId, null, locale);

    }
    return new DescriptiveMetadataInfos();
  }

  private DescriptiveMetadataInfos retrieveDescriptiveMetadataInfos(String aipId, String representationId,
    final Locale locale)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    DescriptiveMetadataInfos result = new DescriptiveMetadataInfos();
    ModelService model = RodaCoreFactory.getModelService();
    List<DescriptiveMetadata> listDescriptiveMetadataBinaries;
    if (representationId != null) {
      listDescriptiveMetadataBinaries = model.retrieveRepresentation(aipId, representationId).getDescriptiveMetadata();
    } else {
      listDescriptiveMetadataBinaries = model.retrieveAIP(aipId).getDescriptiveMetadata();
    }
    List<DescriptiveMetadataInfo> descriptiveMetadataList = new ArrayList<>();

    // Can be null when the AIP is a ghost
    if (listDescriptiveMetadataBinaries != null) {
      List<DescriptiveMetadata> orderedMetadata = orderDescriptiveMetadata(listDescriptiveMetadataBinaries);

      for (DescriptiveMetadata descriptiveMetadata : orderedMetadata) {
        DescriptiveMetadataInfo dmis = retrieveDescriptiveMetadataInfo(aipId, representationId, descriptiveMetadata,
          locale);

        descriptiveMetadataList.add(dmis);
      }
    }

    result.setDescriptiveMetadataInfoList(descriptiveMetadataList);
    return result;
  }

  private List<DescriptiveMetadata> orderDescriptiveMetadata(List<DescriptiveMetadata> metadata) {
    List<DescriptiveMetadata> orderedMetadata = new ArrayList<>();
    boolean order = RodaCoreFactory.getProperty("ui.browser.metadata.order", false);
    List<DescriptiveMetadata> metadataCopy = new ArrayList<>(metadata);

    if (order) {
      List<String> metadataDefaultTypes = RodaCoreFactory
        .getRodaConfigurationAsList("ui.browser.metadata.descriptive.types");

      for (String metadataDefaultType : metadataDefaultTypes) {
        for (DescriptiveMetadata dm : metadata) {
          String id = StringUtils.isNotBlank(dm.getVersion()) ? dm.getType() + "_" + dm.getVersion() : dm.getType();
          if (metadataDefaultType.equals(id)) {
            orderedMetadata.add(dm);
            metadataCopy.remove(dm);
          }
        }
      }
    }

    orderedMetadata.addAll(metadataCopy);
    return orderedMetadata;
  }

  private DescriptiveMetadataInfo retrieveDescriptiveMetadataInfo(String aipId, String representationId,
    DescriptiveMetadata descriptiveMetadata, final Locale locale) {
    ModelService model = RodaCoreFactory.getModelService();
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    DescriptiveMetadataInfo metadataInfo = new DescriptiveMetadataInfo();
    metadataInfo.setId(descriptiveMetadata.getId());

    if (descriptiveMetadata.getType() != null) {
      try {
        String labelWithoutVersion = messages.getTranslation(
          RodaConstants.I18N_UI_BROWSE_METADATA_DESCRIPTIVE_TYPE_PREFIX + descriptiveMetadata.getType().toLowerCase(),
          descriptiveMetadata.getId());

        if (descriptiveMetadata.getVersion() != null) {
          String labelWithVersion = messages.getTranslation(
            RodaConstants.I18N_UI_BROWSE_METADATA_DESCRIPTIVE_TYPE_PREFIX + descriptiveMetadata.getType().toLowerCase()
              + RodaConstants.METADATA_VERSION_SEPARATOR + descriptiveMetadata.getVersion().toLowerCase(),
            labelWithoutVersion);
          metadataInfo.setLabel(labelWithVersion);
        } else {
          metadataInfo.setLabel(labelWithoutVersion);
        }
      } catch (MissingResourceException e) {
        metadataInfo.setLabel(descriptiveMetadata.getId());
      }
    }

    try {
      metadataInfo.setHasHistory(!CloseableIterables.isEmpty(model.listBinaryVersions(descriptiveMetadata)));
    } catch (RODAException | RuntimeException e) {
      metadataInfo.setHasHistory(false);
    }

    return metadataInfo;
  }

  public AIP createAIP(RequestContext requestContext, String parentAipId, String type, Permissions permissions) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException, NotFoundException, AlreadyExistsException {
    User user = requestContext.getUser();
    TransactionalModelService model = requestContext.getTransactionalContext().transactionalModelService();
    return model.createAIP(parentAipId, type, permissions, user.getName());
  }

  public boolean hasDocumentation(String aipId)
    throws RequestNotValidException, AuthorizationDeniedException, GenericException, NotFoundException {
    try {
      Long counter = RodaCoreFactory.getModelService().countDocumentationFiles(aipId, null);
      return counter > 0;
    } catch (NotFoundException e) {
      return false;
    }
  }

  public boolean hasSubmissions(String aipId)
    throws RequestNotValidException, AuthorizationDeniedException, GenericException {
    try {
      Long counter = RodaCoreFactory.getModelService().countSubmissionFiles(aipId);
      return counter > 0;
    } catch (NotFoundException e) {
      return false;
    }
  }

  public Job changeAIPType(User user, SelectedItems<IndexedAIP> selected, String newType, String details)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_NEW_TYPE, newType);
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, details);
    return CommonServicesUtils.createAndExecuteInternalJob("Change AIP type", selected, ChangeTypePlugin.class, user,
      pluginParameters, "Could not change AIP type");
  }

  public Job appraisal(User user, SelectedItems<IndexedAIP> selected, boolean accept, String rejectReason)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_ACCEPT, Boolean.toString(accept));
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_REJECT_REASON, rejectReason);
    return CommonServicesUtils.createAndExecuteInternalJob("AIP appraisal", selected, AppraisalPlugin.class, user,
      pluginParameters, "Could not execute appraisal action on AIP");
  }

  public Job moveAIPInHierarchy(User user, SelectedItems<IndexedAIP> selected, String parentId, String details)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_ID, parentId);
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, details);
    return CommonServicesUtils.createAndExecuteInternalJob("Move AIP in hierarchy", selected, MovePlugin.class, user,
      pluginParameters, "Could not execute move job");
  }

  public StreamResponse retrieveAIP(String aipId)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    Optional<LiteRODAObject> liteAIP = LiteRODAObjectFactory.get(AIP.class, aipId);
    if (liteAIP.isEmpty()) {
      throw new RequestNotValidException("Could not retrieve AIP " + aipId + " lite");
    }
    return ApiUtils.download(liteAIP.get());
  }

  public DescriptiveMetadata revertDescriptiveMetadataVersion(User user, String aipId, String representationId,
    String descriptiveMetadataId, String versionId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {

    Map<String, String> properties = new HashMap<>();
    properties.put(RodaConstants.VERSION_USER, user.getId());
    properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.REVERTED.toString());

    RodaCoreFactory.getModelService().revertDescriptiveMetadataVersion(aipId, representationId, descriptiveMetadataId,
      versionId, properties);

    return RodaCoreFactory.getModelService().retrieveDescriptiveMetadata(aipId, representationId,
      descriptiveMetadataId);
  }

  public DescriptiveMetadata revertDescriptiveMetadataVersion(User user, String aipId, String descriptiveMetadataId,
    String versionId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {

    Map<String, String> properties = new HashMap<>();
    properties.put(RodaConstants.VERSION_USER, user.getId());
    properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.REVERTED.toString());

    RodaCoreFactory.getModelService().revertDescriptiveMetadataVersion(aipId, descriptiveMetadataId, versionId,
      properties);

    return RodaCoreFactory.getModelService().retrieveDescriptiveMetadata(aipId, descriptiveMetadataId);
  }

  public StreamResponse retrieveAIPPart(String aipId, String part)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    Optional<LiteRODAObject> liteAIP = LiteRODAObjectFactory.get(AIP.class, aipId);
    if (liteAIP.isEmpty()) {
      throw new RequestNotValidException("Could not retrieve AIP " + aipId + " lite");
    }

    if (part.equals(RodaConstants.STORAGE_DIRECTORY_SUBMISSION)
      || part.equals(RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION)
      || part.equals(RodaConstants.STORAGE_DIRECTORY_SCHEMAS)) {
      return ApiUtils.download(liteAIP.get(), part);
    }

    throw new GenericException("Unsupported part: " + part);
  }

  public String retrieveDescriptiveMetadataPreview(String aipId, String representationId, String descriptiveMetadataId,
    Set<MetadataValue> values)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    String rawTemplate;

    String result = "";
    try (
      InputStream templateStream = RodaCoreFactory.getConfigurationFileAsStream(RodaConstants.METADATA_TEMPLATE_FOLDER
        + "/" + descriptiveMetadataId + RodaConstants.METADATA_TEMPLATE_EXTENSION)) {

      if (templateStream != null) {
        Map<String, String> data = new HashMap<>();
        rawTemplate = IOUtils.toString(templateStream, StandardCharsets.UTF_8);
        // assume that values set is not empty
        if (values != null) {
          values.forEach(metadataValue -> {
            String val = metadataValue.get("value");
            if (val != null) {
              val = val.replaceAll("\\s", "");
              if (!val.isEmpty()) {
                data.put(metadataValue.get("name"), metadataValue.get("value"));
              }
            }
          });
        }

        result = HandlebarsUtility.executeHandlebars(rawTemplate, data);
      } else {
        ModelService model = RodaCoreFactory.getModelService();
        Optional<LiteRODAObject> descriptiveMetadataLite = LiteRODAObjectFactory.get(DescriptiveMetadata.class, aipId,
          representationId, descriptiveMetadataId);
        if (descriptiveMetadataLite.isEmpty()) {
          throw new RequestNotValidException("Could not get LITE from representation " + representationId + " of AIP "
            + aipId + " with metadata id" + descriptiveMetadataId);
        }
        Binary binary = model.retrieveDescriptiveMetadataBinary(aipId, representationId,
          descriptiveMetadataId + XML_EXT);
        InputStream inputStream = binary.getContent().createInputStream();
        result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
      }
    } catch (IOException e) {
      throw new GenericException(e);
    }
    return result;
  }

  public DescriptiveMetadata createDescriptiveMetadataFile(String aipId, String representationId,
    String descriptiveMetadataId, String descriptiveMetadataType, String descriptiveMetadataVersion,
    ContentPayload descriptiveMetadataPayload, String createdBy, ModelService model) throws GenericException, ValidationException,
    AuthorizationDeniedException, RequestNotValidException, AlreadyExistsException, NotFoundException {

    ValidationReport report = ValidationUtils.validateDescriptiveBinary(descriptiveMetadataPayload,
      descriptiveMetadataType, descriptiveMetadataVersion, false);

    if (!report.isValid()) {
      throw new ValidationException(report);
    }

    return model.createDescriptiveMetadata(aipId, representationId, descriptiveMetadataId,
      descriptiveMetadataPayload, descriptiveMetadataType, descriptiveMetadataVersion, createdBy);
  }

  public ConfiguredDescriptiveMetadataList retrieveSupportedMetadataTypes(Locale locale) {
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    List<String> types = RodaUtils
      .copyList(RodaCoreFactory.getRodaConfiguration().getList(RodaConstants.UI_BROWSER_METADATA_DESCRIPTIVE_TYPES));

    ConfiguredDescriptiveMetadataList supportedMetadata = new ConfiguredDescriptiveMetadataList();

    for (String id : types) {
      String type = id;
      String version = null;
      if (id.contains(RodaConstants.METADATA_VERSION_SEPARATOR)) {
        version = id.substring(id.lastIndexOf(RodaConstants.METADATA_VERSION_SEPARATOR) + 1);
        type = id.substring(0, id.lastIndexOf(RodaConstants.METADATA_VERSION_SEPARATOR));
      }
      String key = RodaConstants.I18N_UI_BROWSE_METADATA_DESCRIPTIVE_TYPE_PREFIX + type;
      if (version != null) {
        key += RodaConstants.METADATA_VERSION_SEPARATOR + version.toLowerCase();
      }
      String label = messages.getTranslation(key, type);

      supportedMetadata.addObject(new ConfiguredDescriptiveMetadata(label, id));
    }

    return supportedMetadata;
  }

  public SupportedMetadataValue retrieveSupportedMetadata(User user, IndexedAIP aip,
    IndexedRepresentation representation, String descriptiveMetadataId, Locale locale)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    Messages messages = RodaCoreFactory.getI18NMessages(locale);

    String template;
    Set<MetadataValue> values = new HashSet<>();
    try (
      InputStream templateStream = RodaCoreFactory.getConfigurationFileAsStream(RodaConstants.METADATA_TEMPLATE_FOLDER
        + "/" + descriptiveMetadataId + RodaConstants.METADATA_TEMPLATE_EXTENSION)) {

      if (templateStream == null) {
        return new SupportedMetadataValue(values);
      }

      if (checkIfDescriptiveMetadataExists(aip, representation, descriptiveMetadataId + XML_EXT)) {
        template = IOUtils.toString(templateStream, StandardCharsets.UTF_8);
        String representationId = representation != null ? representation.getId() : null;
        Binary binary = RodaCoreFactory.getModelService().retrieveDescriptiveMetadataBinary(aip.getId(),
          representationId, descriptiveMetadataId + XML_EXT);
        String xml = IOUtils.toString(binary.getContent().createInputStream(), StandardCharsets.UTF_8);

        Set<MetadataValue> result = getMetadataValuesFromDescriptiveMetadataFile(template, xml);

        return new SupportedMetadataValue(result);
      } else {
        template = IOUtils.toString(templateStream, StandardCharsets.UTF_8);
        Set<MetadataValue> result = getDefaultDescriptiveMetadataValues(template, aip, representation, user, locale,
          messages);
        return new SupportedMetadataValue(result);
      }
    } catch (IOException e) {
      LOGGER.error("Error getting the template from the stream", e);
    }

    return new SupportedMetadataValue(values);
  }

  private boolean checkIfDescriptiveMetadataExists(IndexedAIP aip, IndexedRepresentation representation,
    String descriptiveMetadataId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    if (representation != null) {
      Representation modelRepresentation = RodaCoreFactory.getModelService().retrieveRepresentation(aip.getId(),
        representation.getId());
      if (!modelRepresentation.getDescriptiveMetadata().isEmpty()) {
        return modelRepresentation.getDescriptiveMetadata().stream()
          .anyMatch(dm -> dm.getId().equals(descriptiveMetadataId));
      }
    } else {
      AIP aipModel = RodaCoreFactory.getModelService().retrieveAIP(aip.getId());
      if (!aipModel.getDescriptiveMetadata().isEmpty()) {
        return aipModel.getDescriptiveMetadata().stream().anyMatch(dm -> dm.getId().equals(descriptiveMetadataId));
      }
    }
    return false;
  }

  private Set<MetadataValue> getMetadataValuesFromDescriptiveMetadataFile(String template, String xml) {
    Set<MetadataValue> values = ServerTools.transform(template);
    Set<MetadataValue> result = new TreeSet<>();

    for (MetadataValue mv : values) {
      String xpathRaw = mv.get("xpath");
      if (xpathRaw != null && !xpathRaw.isEmpty()) {
        String[] xpaths = xpathRaw.split("##%##");
        String value;
        List<String> allValues = new ArrayList<>();
        for (String xpath : xpaths) {
          allValues.addAll(ServerTools.applyXpath(xml, xpath));
        }
        // if any of the values is different, concatenate all values in a
        // string, otherwise return the value
        boolean allEqual = allValues.stream().allMatch(s -> s.trim().equals(allValues.getFirst().trim()));
        if (allEqual && !allValues.isEmpty()) {
          value = allValues.getFirst();
        } else {
          value = String.join(" / ", allValues);
        }
        mv.set("value", value.trim());
      }
      // getMetadataValueLabels(mv, locale, messages);
      // getMetadataValueI18nPrefix(mv, locale, messages);
      result.add(mv);
    }

    return result;
  }

  // Get preservation metadata
  public StreamResponse retrievePreservationMetadata(String aipId)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {

    try (CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationFiles = RodaCoreFactory
      .getModelService().listPreservationMetadata(aipId, true)) {
      ModelService model = RodaCoreFactory.getModelService();
      List<ZipEntryInfo> zipEntries = new ArrayList<>();
      Map<String, ZipEntryInfo> agents = new HashMap<>();

      for (OptionalWithCause<PreservationMetadata> oPreservationFile : preservationFiles) {
        if (oPreservationFile.isPresent()) {
          PreservationMetadata preservationFile = oPreservationFile.get();
          Binary binary = model.getBinary(preservationFile);

          ZipEntryInfo info = new ZipEntryInfo(FSUtils.getStoragePathAsString(binary.getStoragePath(), true),
            binary.getContent());
          zipEntries.add(info);

          if (preservationFile.getType() == PreservationMetadata.PreservationMetadataType.EVENT) {
            try {
              List<LinkingIdentifier> agentIDS = PremisV3Utils.extractAgentsFromEvent(binary);
              if (!agentIDS.isEmpty()) {
                for (LinkingIdentifier li : agentIDS) {
                  String agentID = li.getValue();
                  if (!agents.containsKey(agentID)) {
                    StoragePath agentPath = ModelUtils.getPreservationMetadataStoragePath(agentID,
                      PreservationMetadata.PreservationMetadataType.AGENT);
                    String pmURN = URNUtils
                      .createRodaPreservationURN(PreservationMetadata.PreservationMetadataType.AGENT, agentID);
                    Optional<LiteRODAObject> pmLite = LiteRODAObjectFactory.get(PreservationMetadata.class, pmURN);
                    if (pmLite.isPresent()) {
                      Binary agentBinary = model.getBinary(pmLite.get());

                      info = new ZipEntryInfo(
                        FSUtils.getStoragePathAsString(DefaultStoragePath.parse(preservationFile.getAipId()), false,
                          agentPath, true),
                        agentBinary.getContent());
                      agents.put(agentID, info);
                    } else {
                      LOGGER.error("Could not get LITE object for agent {}", agentID);
                    }
                  }
                }
              }
            } catch (ValidationException | GenericException e) {
              // do nothing
            }
          }
        } else {
          LOGGER.error("Cannot get AIP preservation metadata", oPreservationFile.getCause());
        }
      }

      if (!agents.isEmpty()) {
        for (Map.Entry<String, ZipEntryInfo> entry : agents.entrySet()) {
          zipEntries.add(entry.getValue());
        }
      }

      return DownloadUtils.createZipStreamResponse(zipEntries, aipId);
    } catch (IOException e) {
      throw new GenericException(e);
    }
  }

  public Job updateAIPPermissions(User user, UpdatePermissionsRequest request)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    final String eventDescription = "The process of updating an object of the repository.";

    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_PERMISSIONS_JSON,
      JsonUtils.getJsonFromObject(request.getPermissions()));
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_RECURSIVE, Boolean.toString(request.isRecursive()));
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, request.getDetails());
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_EVENT_DESCRIPTION, eventDescription);

    return CommonServicesUtils.createAndExecuteInternalJob("Update AIP permissions recursively",
      CommonServicesUtils.convertSelectedItems(request.getItemsToUpdate(), IndexedAIP.class),
      UpdatePermissionsPlugin.class, user, pluginParameters, "Could not execute AIP permissions recursively action");
  }

  public void deleteDescriptiveMetadataFile(String aipId, String representationId, String descriptiveMetadataId,
    String deletedBy)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    RodaCoreFactory.getModelService().deleteDescriptiveMetadata(aipId, representationId, descriptiveMetadataId,
      deletedBy);
  }

  public DescriptiveMetadata updateDescriptiveMetadataFile(User user, String aipId,
    CreateDescriptiveMetadataRequest request) throws GenericException, AuthorizationDeniedException,
    ValidationException, RequestNotValidException, NotFoundException {
    return updateDescriptiveMetadataFile(user, aipId, null, request);
  }

  public DescriptiveMetadata updateDescriptiveMetadataFile(User user, String aipId, String representationId,
    CreateDescriptiveMetadataRequest request) throws GenericException, AuthorizationDeniedException,
    ValidationException, RequestNotValidException, NotFoundException {
    Map<String, String> properties = new HashMap<>();
    properties.put(RodaConstants.VERSION_USER, user.getId());
    properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.UPDATED.toString());

    String filename = request.getFilename();
    String descriptiveMetadataType = request.getType();
    String descriptiveMetadataVersion = request.getVersion();
    StringContentPayload descriptiveMetadataPayload = new StringContentPayload(
      ((DescriptiveMetadataRequestXML) request).getXml());

    ValidationReport report = ValidationUtils.validateDescriptiveBinary(descriptiveMetadataPayload,
      descriptiveMetadataType, descriptiveMetadataVersion, false);

    if (!report.isValid()) {
      throw new ValidationException(report);
    }

    return RodaCoreFactory.getModelService().updateDescriptiveMetadata(aipId, representationId, filename,
      descriptiveMetadataPayload, descriptiveMetadataType, descriptiveMetadataVersion, properties, user.getId());
  }

  public Optional<String> retrieveDistributedInstanceName(String instanceId, boolean isLocalInstance) {
    try {
      ModelService model = RodaCoreFactory.getModelService();
      RodaConstants.DistributedModeType distributedModeType = RodaCoreFactory.getDistributedModeType();

      if (RodaConstants.DistributedModeType.CENTRAL.equals(distributedModeType)) {
        if (isLocalInstance) {
          return Optional.of(RodaCoreFactory.getProperty(RodaConstants.CENTRAL_INSTANCE_NAME_PROPERTY,
            RodaConstants.DEFAULT_CENTRAL_INSTANCE_NAME));
        } else {
          DistributedInstance distributedInstance = model.retrieveDistributedInstance(instanceId);
          return Optional.of(distributedInstance.getName());
        }
      }
    } catch (GenericException | AuthorizationDeniedException | RequestNotValidException | NotFoundException e) {
      LOGGER.warn("Could not retrieve the distributed instance", e);
      return Optional.empty();
    }

    return Optional.empty();
  }

  public List<String> getConfigurationAIPRules(User user) {
    if (UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_FIND_REPRESENTATION_INFORMATION)) {
      return RodaCoreFactory.getRodaConfigurationAsList("ui.ri.rule.AIP").stream()
        .map(r -> RodaCoreFactory.getRodaConfigurationAsString(r, RodaConstants.SEARCH_FIELD_FIELDS)).toList();
    } else {
      return Collections.emptyList();
    }
  }

  public DescriptiveMetadataVersions retrieveDescriptiveMetadataVersions(IndexedAIP aip, String metadataId,
    String localeString)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    return retrieveDescriptiveMetadataVersions(aip, null, metadataId, localeString);
  }

  public DescriptiveMetadataVersions retrieveDescriptiveMetadataVersions(IndexedAIP aip, String representationId,
    String metadataId, String localeString)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    DescriptiveMetadataVersions versions = new DescriptiveMetadataVersions();
    List<ResourceVersion> versionResponses = new ArrayList<>();
    Locale locale = ServerTools.parseLocale(localeString);

    DescriptiveMetadata descriptiveMetadata = RodaCoreFactory.getModelService().retrieveDescriptiveMetadata(aip.getId(),
      representationId, metadataId);
    DescriptiveMetadataInfo descriptiveMetadataInfo = retrieveDescriptiveMetadataInfo(aip.getId(), representationId,
      descriptiveMetadata, locale);

    try (CloseableIterable<BinaryVersion> binaryVersions = listDescriptiveMetadataVersions(aip.getId(),
      representationId, metadataId)) {
      for (BinaryVersion binaryVersion : binaryVersions) {
        versionResponses.add(
          new ResourceVersion(binaryVersion.getId(), binaryVersion.getCreatedDate(), binaryVersion.getProperties()));
      }
    } catch (IOException e) {
      throw new GenericException(e);
    }

    versions.setDescriptiveMetadata(descriptiveMetadataInfo);
    versions.setVersions(versionResponses);
    versions.setPermissions(aip.getPermissions());

    return versions;
  }

  private Set<MetadataValue> getDefaultDescriptiveMetadataValues(String template, IndexedAIP aip,
    IndexedRepresentation representation, User user, Locale locale, Messages messages) throws GenericException {
    Set<MetadataValue> values = ServerTools.transform(template);
    Set<MetadataValue> result = new TreeSet<>();
    for (MetadataValue mv : values) {
      String generator = mv.get("auto-generate");
      if (generator != null && !generator.isEmpty()) {
        String value;
        if (representation != null) {
          value = ServerTools.autoGenerateRepresentationValue(representation, generator);
        } else {
          value = ServerTools.autoGenerateAIPValue(aip, user, generator);
        }

        if (value != null) {
          mv.set("value", value);
        }
      }

      CommonServicesUtils.getMetadataValueLabels(mv, locale, messages);
      CommonServicesUtils.getMetadataValueI18nPrefix(mv, locale, messages);
      result.add(mv);
    }

    return result;
  }

  private CloseableIterable<BinaryVersion> listDescriptiveMetadataVersions(String aipId, String representationId,
    String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    Optional<LiteRODAObject> liteDM = LiteRODAObjectFactory.get(DescriptiveMetadata.class, aipId, representationId,
      descriptiveMetadataId);
    if (liteDM.isEmpty()) {
      throw new RequestNotValidException("Could not get LITE from representation " + representationId + " of AIP "
        + aipId + " with metadata id" + descriptiveMetadataId);
    }
    return RodaCoreFactory.getModelService().listBinaryVersions(liteDM.get());
  }

  public void deleteDescriptiveMetadataVersion(String aipId, String descriptiveMetadataId, String versionId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    deleteDescriptiveMetadataVersion(aipId, null, descriptiveMetadataId, versionId);
  }

  public void deleteDescriptiveMetadataVersion(String aipId, String representationId, String descriptiveMetadataId,
    String versionId)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    Optional<LiteRODAObject> liteDM = LiteRODAObjectFactory.get(DescriptiveMetadata.class, aipId, representationId,
      descriptiveMetadataId);
    if (liteDM.isEmpty()) {
      throw new RequestNotValidException("Could not get LITE from representation " + representationId + " of AIP "
        + aipId + " with metadata id" + descriptiveMetadataId);
    }
    RodaCoreFactory.getModelService().deleteBinaryVersion(liteDM.get(), versionId);
  }

  public boolean isMetadataSimilar(IndexedAIP aip, String representationId, String metadataId,
    SelectedType selectedType)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    boolean isSimilar = false;

    try {
      Binary binary = RodaCoreFactory.getModelService().retrieveDescriptiveMetadataBinary(aip.getId(), representationId,
        metadataId + XML_EXT);
      InputStream inputStream = binary.getContent().createInputStream();
      String xml = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
      String selectedTypeTemplate = retrieveDescriptiveMetadataPreview(aip.getId(), representationId,
        selectedType.getMetadataId(), selectedType.getValue());

      if (selectedTypeTemplate != "") {
        Diff diff = DiffBuilder.compare(xml).withTest(selectedTypeTemplate).ignoreComments().ignoreWhitespace()
          .checkForIdentical().checkForSimilar().withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndText))
          .withNodeFilter(node -> !node.getNodeName().equals("schemaLocation")).build();

        isSimilar = !diff.hasDifferences();
      }

    } catch (IOException e) {
      throw new GenericException(e);
    }
    return isSimilar;
  }

}
