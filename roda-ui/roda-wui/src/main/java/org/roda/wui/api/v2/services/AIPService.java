package org.roda.wui.api.v2.services;

import static org.roda.wui.api.controllers.BrowserHelper.listDescriptiveMetadataVersions;
import static org.roda.wui.api.v2.utils.CommonServicesUtils.createAndExecuteInternalJob;

import java.io.IOException;
import java.io.InputStream;
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
import java.util.stream.Collectors;

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
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.CreateDescriptiveMetadataRequest;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfo;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfos;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataRequestForm;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataVersionsResponse;
import org.roda.core.data.v2.ip.metadata.InstanceState;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.ResourceVersion;
import org.roda.core.data.v2.ip.metadata.SupportedMetadata;
import org.roda.core.data.v2.ip.metadata.SupportedMetadataValue;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.data.v2.validation.ValidationReport;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.plugins.base.maintenance.ChangeTypePlugin;
import org.roda.core.plugins.base.maintenance.DeleteRODAObjectPlugin;
import org.roda.core.plugins.base.maintenance.MovePlugin;
import org.roda.core.plugins.base.preservation.AppraisalPlugin;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryVersion;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Directory;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.utils.RODAInstanceUtils;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.client.browse.bundle.SupportedMetadataTypeBundle;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.server.ServerTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

  public List<IndexedAIP> getAncestors(IndexedAIP indexedAIP, User user) throws GenericException {
    return RodaCoreFactory.getIndexService().retrieveAncestors(indexedAIP, user, new ArrayList<>());
  }

  private static final String HTML_EXT = ".html";
  private IndexService indexService;

  @Autowired
  public void setIndexService(IndexService service) {
    this.indexService = service;
  }

  public Job deleteAIP(User user, SelectedItems<IndexedAIP> selected, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, details);
    return createAndExecuteInternalJob("Delete AIP", selected, DeleteRODAObjectPlugin.class, user, pluginParameters,
      "Could not execute AIP delete action");
  }

  public List<IndexedAIP> retrieveAncestors(IndexedAIP aip, User user, List<String> fieldsToReturn)
    throws GenericException {
    return RodaCoreFactory.getIndexService().retrieveAncestors(aip, user, fieldsToReturn);
  }

  public List<String> getRepresentationInformation(User user) {
    if (UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_FIND_REPRESENTATION_INFORMATION)) {
      return RodaCoreFactory.getRodaConfigurationAsList("ui.ri.rule.AIP").stream()
        .map(r -> RodaCoreFactory.getRodaConfigurationAsString(r, RodaConstants.SEARCH_FIELD_FIELDS))
        .collect(Collectors.toList());
    } else {
      return Collections.emptyList();
    }
  }

  public InstanceState getInstanceInformation(IndexedAIP aip)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException {
    InstanceState instanceState = new InstanceState();

    if (RODAInstanceUtils.isConfiguredAsDistributedMode()
      && RodaCoreFactory.getDistributedModeType().equals(RodaConstants.DistributedModeType.CENTRAL)) {
      instanceState.setLocalToInstance(aip.getInstanceId().equals(RODAInstanceUtils.getLocalInstanceIdentifier()));
      retrieveDistributedInstanceName(aip.getInstanceId(), instanceState.isLocalToInstance())
        .ifPresent(instanceState::setInstanceName);
    }

    return instanceState;
  }

  public DescriptiveMetadataInfos retrieveDescriptiveMetadataList(User user, String aipId, final Locale locale)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    if (UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_LIST_AIP_DESCRIPTIVE_METADATA)) {

      return retrieveDescriptiveMetadataInfos(aipId, null, locale);

    }
    return null;
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
      StoragePath storagePath;
      if (representationId != null) {
        storagePath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, representationId,
          descriptiveMetadata.getId());
      } else {
        storagePath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, descriptiveMetadata.getId());
      }

      metadataInfo.setHasHistory(!CloseableIterables.isEmpty(model.getStorage().listBinaryVersions(storagePath)));
    } catch (RODAException | RuntimeException e) {
      metadataInfo.setHasHistory(false);
    }

    return metadataInfo;
  }

  public AIP createAIP(User user, String parentAipId, String type, Permissions permissions) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException, NotFoundException, AlreadyExistsException {
    ModelService model = RodaCoreFactory.getModelService();
    return model.createAIP(parentAipId, type, permissions, user.getName());
  }

  public boolean hasDocumentation(String aipId)
    throws RequestNotValidException, AuthorizationDeniedException, GenericException {
    StoragePath aipPath = ModelUtils.getAIPStoragePath(aipId);
    StoragePath documentationPath = DefaultStoragePath.parse(aipPath, RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION);
    try {
      Long counter = RodaCoreFactory.getStorageService().countResourcesUnderContainer(documentationPath, false);
      return counter > 0;
    } catch (NotFoundException e) {
      throw new RESTException(e);
    }
  }

  public boolean hasSubmissions(String aipId)
    throws RequestNotValidException, AuthorizationDeniedException, GenericException {
    StoragePath aipPath = ModelUtils.getAIPStoragePath(aipId);
    StoragePath documentationPath = DefaultStoragePath.parse(aipPath, RodaConstants.STORAGE_DIRECTORY_SUBMISSION);
    try {
      Long counter = RodaCoreFactory.getStorageService().countResourcesUnderContainer(documentationPath, false);
      return counter > 0;
    } catch (NotFoundException e) {
      throw new RESTException(e);
    }
  }

  public Job changeAIPType(User user, SelectedItems<IndexedAIP> selected, String newType, String details)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_NEW_TYPE, newType);
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, details);
    return createAndExecuteInternalJob("Change AIP type", selected, ChangeTypePlugin.class, user, pluginParameters,
      "Could not change AIP type");
  }

  public Job appraisal(User user, SelectedItems<IndexedAIP> selected, boolean accept, String rejectReason)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_ACCEPT, Boolean.toString(accept));
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_REJECT_REASON, rejectReason);
    return createAndExecuteInternalJob("AIP appraisal", selected, AppraisalPlugin.class, user, pluginParameters,
      "Could not execute appraisal action on AIP");
  }

  public <T extends IsIndexed> T retrieve(RequestContext rc, Class<T> returnClass, String id,
    List<String> fieldsToReturn) {
    return indexService.retrieve(rc, returnClass, id, fieldsToReturn);
  }

  public Job moveAIPInHierarchy(User user, SelectedItems<IndexedAIP> selected, String parentId, String details)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_ID, parentId);
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, details);
    return createAndExecuteInternalJob("Move AIP in hierarchy", selected, MovePlugin.class, user, pluginParameters,
      "Could not execute move job");
  }

  public StreamResponse retrieveAIP(String aipId)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    StoragePath storagePath = ModelUtils.getAIPStoragePath(aipId);
    Directory directory = RodaCoreFactory.getStorageService().getDirectory(storagePath);
    return ApiUtils.download(directory, aipId);
  }

  public StreamResponse retrieveAIPPart(String aipId, String part)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {

    if (RodaConstants.STORAGE_DIRECTORY_SUBMISSION.equals(part)) {
      Directory directory = RodaCoreFactory.getModelService().getSubmissionDirectory(aipId);
      return ApiUtils.download(directory, part);
    } else if (RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION.equals(part)) {
      Directory directory = RodaCoreFactory.getModelService().getDocumentationDirectory(aipId);
      return ApiUtils.download(directory, part);
    } else if (RodaConstants.STORAGE_DIRECTORY_SCHEMAS.equals(part)) {
      Directory directory = RodaCoreFactory.getModelService().getSchemasDirectory(aipId);
      return ApiUtils.download(directory, part);
    } else {
      throw new GenericException("Unsupported part: " + part);
    }
  }

  public String retrieveDescriptiveMetadataPreview(String id, Set<MetadataValue> values) throws GenericException {
    String rawTemplate = "";

    String result;
    try (InputStream templateStream = RodaCoreFactory.getConfigurationFileAsStream(
      RodaConstants.METADATA_TEMPLATE_FOLDER + "/" + id + RodaConstants.METADATA_TEMPLATE_EXTENSION)) {

      if (templateStream != null) {
        Map<String, String> data = new HashMap<>();
        rawTemplate = IOUtils.toString(templateStream, RodaConstants.DEFAULT_ENCODING);
        // assume that values set is not empty
        if (values != null) {
          values.forEach(metadataValue -> {
            String val = metadataValue.get("value");
            if (val != null) {
              val = val.replaceAll("\\s", "");
              if (!"".equals(val)) {
                data.put(metadataValue.get("name"), metadataValue.get("value"));
              }
            }
          });
        }

        result = HandlebarsUtility.executeHandlebars(rawTemplate, data);
      } else {
        result = rawTemplate;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  public DescriptiveMetadata createDescriptiveMetadataFile(String aipId, String representationId,
    String descriptiveMetadataId, String descriptiveMetadataType, String descriptiveMetadataVersion,
    ContentPayload descriptiveMetadataPayload, String createdBy) throws GenericException, ValidationException,
    AuthorizationDeniedException, RequestNotValidException, AlreadyExistsException, NotFoundException {

    ValidationReport report = ValidationUtils.validateDescriptiveBinary(descriptiveMetadataPayload,
      descriptiveMetadataType, descriptiveMetadataVersion, false);

    if (!report.isValid()) {
      throw new ValidationException(report);
    }

    return RodaCoreFactory.getModelService().createDescriptiveMetadata(aipId, representationId, descriptiveMetadataId,
      descriptiveMetadataPayload, descriptiveMetadataType, descriptiveMetadataVersion, createdBy);
  }

  public List<SupportedMetadata> retrieveSupportedMetadataTypes(Locale locale) {
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    List<String> types = RodaUtils
      .copyList(RodaCoreFactory.getRodaConfiguration().getList(RodaConstants.UI_BROWSER_METADATA_DESCRIPTIVE_TYPES));

    List<SupportedMetadata> supportedMetadata = new ArrayList<>();

    if (types != null) {
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

        supportedMetadata.add(new SupportedMetadata(label, id));
      }
    }

    return supportedMetadata;
  }

  public SupportedMetadataValue retrieveSupportedMetadata(User user, IndexedAIP aip,
    IndexedRepresentation representation, String metadataType, Locale locale) throws GenericException {
    Messages messages = RodaCoreFactory.getI18NMessages(locale);

    String template = "";
    Set<MetadataValue> values = new HashSet<>();
    try (InputStream templateStream = RodaCoreFactory.getConfigurationFileAsStream(
      RodaConstants.METADATA_TEMPLATE_FOLDER + "/" + metadataType + RodaConstants.METADATA_TEMPLATE_EXTENSION)) {

      if (templateStream != null) {
        template = IOUtils.toString(templateStream, RodaConstants.DEFAULT_ENCODING);
        values = ServerTools.transform(template);
        for (MetadataValue mv : values) {
          String generator = mv.get("auto-generate");
          if (generator != null && generator.length() > 0) {
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

          String labels = mv.get("label");
          String labelI18N = mv.get("labeli18n");
          if (labels != null && labelI18N != null) {
            Map<String, String> labelsMaps = JsonUtils.getMapFromJson(labels);
            try {
              labelsMaps.put(locale.toString(), RodaCoreFactory.getI18NMessages(locale).getTranslation(labelI18N));
            } catch (MissingResourceException e) {
              LOGGER.debug("Missing resource: {}", labelI18N);
            }
            labels = JsonUtils.getJsonFromObject(labelsMaps);
            mv.set("label", labels);
          }

          String i18nPrefix = mv.get("optionsLabelI18nKeyPrefix");
          if (i18nPrefix != null) {
            Map<String, String> terms = messages.getTranslations(i18nPrefix, String.class, false);
            if (terms.size() > 0) {
              try {
                String options = mv.get("options");
                List<String> optionsList = JsonUtils.getListFromJson(options, String.class);

                if (optionsList != null) {
                  Map<String, Map<String, String>> i18nMap = new HashMap<>();
                  for (int i = 0; i < optionsList.size(); i++) {
                    String value = optionsList.get(i);
                    String translation = terms.get(i18nPrefix + "." + value);
                    if (translation == null) {
                      translation = value;
                    }
                    Map<String, String> term = new HashMap<>();
                    term.put(locale.toString(), translation);
                    i18nMap.put(value, term);
                  }
                  mv.set("optionsLabels", JsonUtils.getJsonFromObject(i18nMap));
                }
              } catch (MissingResourceException e) {
                LOGGER.error(e.getMessage(), e);
              }
            }
          }
        }
      }
    } catch (IOException e) {
      LOGGER.error("Error getting the template from the stream", e);
    }

    return new SupportedMetadataValue(template, values);

  }

  // Get preservation metadata
  public StreamResponse retrievePreservationMetadata(String aipId)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {

    try (CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationFiles = RodaCoreFactory
      .getModelService().listPreservationMetadata(aipId, true)) {
      StorageService storage = RodaCoreFactory.getStorageService();
      List<ZipEntryInfo> zipEntries = new ArrayList<>();
      Map<String, ZipEntryInfo> agents = new HashMap<>();

      for (OptionalWithCause<PreservationMetadata> oPreservationFile : preservationFiles) {
        if (oPreservationFile.isPresent()) {
          PreservationMetadata preservationFile = oPreservationFile.get();
          StoragePath storagePath = ModelUtils.getPreservationMetadataStoragePath(preservationFile);
          Binary binary = storage.getBinary(storagePath);

          ZipEntryInfo info = new ZipEntryInfo(storage.getStoragePathAsString(storagePath, true), binary.getContent());
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
                    Binary agentBinary = storage.getBinary(agentPath);
                    info = new ZipEntryInfo(
                      storage.getStoragePathAsString(DefaultStoragePath.parse(preservationFile.getAipId()), false,
                        agentPath, true),
                      agentBinary.getContent());
                    agents.put(agentID, info);
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

      if (agents.size() > 0) {
        for (Map.Entry<String, ZipEntryInfo> entry : agents.entrySet()) {
          zipEntries.add(entry.getValue());
        }
      }

      return DownloadUtils.createZipStreamResponse(zipEntries, aipId);
    } catch (IOException e) {
      throw new GenericException(e);
    }

  }

  public CreateDescriptiveMetadataRequest retrieveSpecificDescriptiveMetadata(User user, IndexedAIP aip,
    IndexedRepresentation representation, String descriptiveMetadataId, Locale locale)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    String representationId = representation != null ? representation.getId() : null;

    DescriptiveMetadata metadata = RodaCoreFactory.getModelService().retrieveDescriptiveMetadata(aip.getId(),
      representationId, descriptiveMetadataId);
    return retrieveSpecificDescriptiveMetadata(user, aip, representation, descriptiveMetadataId, metadata.getType(),
      metadata.getVersion(), locale);
  }

  public CreateDescriptiveMetadataRequest retrieveSpecificDescriptiveMetadata(User user, IndexedAIP aip,
    IndexedRepresentation representation, String descriptiveMetadataId, String type, String version, Locale locale)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    CreateDescriptiveMetadataRequest ret;
    InputStream inputStream = null;
    try {
      String representationId = representation != null ? representation.getId() : null;
      Binary binary = RodaCoreFactory.getModelService().retrieveDescriptiveMetadataBinary(aip.getId(), representationId,
        descriptiveMetadataId);
      inputStream = binary.getContent().createInputStream();
      String xml = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

      // Get the supported metadata type with the same type and version
      // We need this to try to get the values for the form
      SupportedMetadataTypeBundle metadataTypeBundle = null;
      SupportedMetadataValue smv = retrieveSupportedMetadata(user, aip, representation, type, locale);

      boolean similar = false;
      // Get the values using XPath
      Set<MetadataValue> values = null;
      String template = null;

      if (smv != null) {
        template = smv.getTemplate();
        if (smv.getValue() != null) {
          values = smv.getValue();
          for (MetadataValue mv : values) {
            // clear the auto-generated values
            // mv.set("value", null);
            String xpathRaw = mv.get("xpath");
            if (xpathRaw != null && xpathRaw.length() > 0) {
              String[] xpaths = xpathRaw.split("##%##");
              String value;
              List<String> allValues = new ArrayList<>();
              for (String xpath : xpaths) {
                allValues.addAll(ServerTools.applyXpath(xml, xpath));
              }
              // if any of the values is different, concatenate all values in a
              // string, otherwise return the value
              boolean allEqual = allValues.stream().allMatch(s -> s.trim().equals(allValues.get(0).trim()));
              if (allEqual && !allValues.isEmpty()) {
                value = allValues.get(0);
              } else {
                value = String.join(" / ", allValues);
              }
              mv.set("value", value.trim());
            }
          }

          // Identity check. Test if the original XML is equal to the result of
          // applying the extracted values to the template
          String templateWithValues = retrieveDescriptiveMetadataPreview(descriptiveMetadataId, values);

          if (StringUtils.isNotBlank(templateWithValues)) {
            Diff diff = DiffBuilder.compare(xml).withTest(templateWithValues).ignoreComments().ignoreWhitespace()
              .checkForIdentical().checkForSimilar()
              .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndText))
              .withNodeFilter(node -> !node.getNodeName().equals("schemaLocation")).build();

            similar = !diff.hasDifferences();
          }
        }
      }

      ret = new DescriptiveMetadataRequestForm(descriptiveMetadataId, "", type, version, xml, similar,
        aip.getPermissions(), values);
    } catch (IOException e) {
      throw new GenericException("Error getting descriptive metadata edit: " + e.getMessage());
    } finally {
      IOUtils.closeQuietly(inputStream);
    }

    return ret;
  }

  public void deleteDescriptiveMetadataFile(String aipId, String representationId, String descriptiveMetadataId,
    String deletedBy)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    RodaCoreFactory.getModelService().deleteDescriptiveMetadata(aipId, representationId, descriptiveMetadataId,
      deletedBy);
  }

  public DescriptiveMetadata updateDescriptiveMetadataFile(String aipId, String representationId,
    String descriptiveMetadataId, String descriptiveMetadataType, String descriptiveMetadataVersion,
    ContentPayload descriptiveMetadataPayload, Map<String, String> properties, String updatedBy)
    throws GenericException, AuthorizationDeniedException, ValidationException, RequestNotValidException,
    NotFoundException {

    ValidationReport report = ValidationUtils.validateDescriptiveBinary(descriptiveMetadataPayload,
      descriptiveMetadataType, descriptiveMetadataVersion, false);

    if (!report.isValid()) {
      throw new ValidationException(report);
    }

    return RodaCoreFactory.getModelService().updateDescriptiveMetadata(aipId, representationId, descriptiveMetadataId,
      descriptiveMetadataPayload, descriptiveMetadataType, descriptiveMetadataVersion, properties, updatedBy);

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

  public DescriptiveMetadataVersionsResponse retrieveDescriptiveMetadataVersionsResponse(IndexedAIP aip,
    String representationId, String metadataId, String localeString)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {

    Locale locale = ServerTools.parseLocale(localeString);

    DescriptiveMetadataVersionsResponse response = new DescriptiveMetadataVersionsResponse();

    DescriptiveMetadata descriptiveMetadata = RodaCoreFactory.getModelService().retrieveDescriptiveMetadata(aip.getId(),
      representationId, metadataId);

    DescriptiveMetadataInfo descriptiveMetadataInfo = retrieveDescriptiveMetadataInfo(aip.getId(), representationId,
      descriptiveMetadata, locale);

    response.setDescriptiveMetadata(descriptiveMetadataInfo);

    List<ResourceVersion> versionResponses = new ArrayList<>();

    try (CloseableIterable<BinaryVersion> it = listDescriptiveMetadataVersions(aip.getId(), representationId,
      metadataId)) {
      for (BinaryVersion v : it) {
        versionResponses.add(new ResourceVersion(v.getId(), v.getCreatedDate(), v.getProperties()));
      }
    } catch (IOException e) {
      throw new GenericException(e);
    }

    response.setVersions(versionResponses);

    response.setPermissions(aip.getPermissions());
    return response;
  }

  public void deleteDescriptiveMetadataVersion(String aipId, String representationId, String descriptiveMetadataId,
    String versionId)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    StoragePath storagePath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, representationId,
      descriptiveMetadataId);
    RodaCoreFactory.getStorageService().deleteBinaryVersion(storagePath, versionId);
  }

}
