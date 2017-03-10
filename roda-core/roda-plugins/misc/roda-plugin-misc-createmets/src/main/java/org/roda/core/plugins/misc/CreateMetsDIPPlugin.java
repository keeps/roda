/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.misc;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPLink;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StringContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda_project.commons_ip.model.ParseException;
import org.roda_project.commons_ip.model.impl.eark.EARKAIP;
import org.roda_project.commons_ip.utils.IPEnums.IPType;
import org.roda_project.commons_ip.utils.IPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Plugin that generates E-ARK DIP manifest files (METS.xml) from the exiting
 * AIP information in the storage layer.
 */
public class CreateMetsDIPPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateMetsDIPPlugin.class);
  private static final String VERSION = "1.0";

  private boolean includeSelectedDescriptiveMetadata = false;
  private String selectedDescriptiveMetadata = "";
  private boolean includeAllPreservationMetadata = false;
  private boolean includeSelectedOtherMetadata = false;
  private String selectedOtherMetadata = "";
  private boolean includeSelectedRepresentations = false;
  private String selectedRepresentations = "";

  private boolean includeSchemas = true;
  private boolean includeDocumentation = true;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_INCLUDE_SELECTED_DESCRIPTIVE_METADATA,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_INCLUDE_SELECTED_DESCRIPTIVE_METADATA,
        "Include only selected descriptive metadata", PluginParameterType.BOOLEAN, "false", false, false,
        "Include only selected descriptive metadata on the following parameter."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_SELECTED_DESCRIPTIVE_METADATA,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_SELECTED_DESCRIPTIVE_METADATA,
        "Selected descriptive metadata (type_version)", PluginParameterType.STRING, "ead_3, dc_2002, ead_2002", true,
        false, "The selected descriptive metadata to filter with types and versions."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_INCLUDE_ALL_PRESERVATION_METADATA,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_INCLUDE_ALL_PRESERVATION_METADATA,
        "Include all preservation metadata", PluginParameterType.BOOLEAN, "true", false, false,
        "Include all preservation metadata inside the AIP."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_INCLUDE_SELECTED_OTHER_METADATA,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_INCLUDE_SELECTED_OTHER_METADATA,
        "Include only selected other metadata", PluginParameterType.BOOLEAN, "false", false, false,
        "Include only selected other metadata on the following parameter."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_SELECTED_OTHER_METADATA,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_SELECTED_OTHER_METADATA, "Selected other metadata (type)",
        PluginParameterType.STRING, "Siegfried, Tika", true, false,
        "The selected other metadata to filter with types."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_INCLUDE_SELECTED_REPRESENTATIONS,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_INCLUDE_SELECTED_REPRESENTATIONS,
        "Include only selected representations", PluginParameterType.BOOLEAN, "false", false, false,
        "Include only selected representations on the following parameter."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_SELECTED_REPRESENTATIONS,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_SELECTED_REPRESENTATIONS, "Selected representations (type)",
        PluginParameterType.STRING, "pdfa, MIXED", true, false, "The selected representations to filter with types."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_INCLUDE_SCHEMAS,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_INCLUDE_SCHEMAS, "Include schemas folder",
        PluginParameterType.BOOLEAN, "true", false, false,
        "Include schemas folder contained on AIP and representations."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_INCLUDE_DOCUMENTATION,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_INCLUDE_DOCUMENTATION, "Include documentation folder",
        PluginParameterType.BOOLEAN, "true", false, false,
        "Include documentation folder contained on AIP and representations."));
  }

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Create E-ARK DIP manifest files (METS.xml)";
  }

  @Override
  public String getDescription() {
    return "Plugin that generates E-ARK DIP manifest files (\"METS.xml\") from "
      + "existing AIP information in the storage layer. This plugin only works with filesystem as the storage service.";
  }

  @Override
  public String getVersionImpl() {
    return VERSION;
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new CreateMetsDIPPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.MISC;
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<PluginParameter>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_INCLUDE_SELECTED_DESCRIPTIVE_METADATA));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_SELECTED_DESCRIPTIVE_METADATA));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_INCLUDE_ALL_PRESERVATION_METADATA));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_INCLUDE_SELECTED_OTHER_METADATA));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_SELECTED_OTHER_METADATA));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_INCLUDE_SELECTED_REPRESENTATIONS));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_SELECTED_REPRESENTATIONS));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_INCLUDE_SCHEMAS));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_INCLUDE_DOCUMENTATION));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_INCLUDE_SELECTED_DESCRIPTIVE_METADATA)) {
      includeSelectedDescriptiveMetadata = Boolean
        .parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_INCLUDE_SELECTED_DESCRIPTIVE_METADATA));
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_SELECTED_DESCRIPTIVE_METADATA)) {
      selectedDescriptiveMetadata = parameters.get(RodaConstants.PLUGIN_PARAMS_SELECTED_DESCRIPTIVE_METADATA);
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_INCLUDE_ALL_PRESERVATION_METADATA)) {
      includeAllPreservationMetadata = Boolean
        .parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_INCLUDE_ALL_PRESERVATION_METADATA));
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_INCLUDE_SELECTED_OTHER_METADATA)) {
      includeSelectedOtherMetadata = Boolean
        .parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_INCLUDE_SELECTED_OTHER_METADATA));
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_SELECTED_OTHER_METADATA)) {
      selectedOtherMetadata = parameters.get(RodaConstants.PLUGIN_PARAMS_SELECTED_OTHER_METADATA);
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_INCLUDE_SELECTED_REPRESENTATIONS)) {
      includeSelectedRepresentations = Boolean
        .parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_INCLUDE_SELECTED_REPRESENTATIONS));
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_SELECTED_REPRESENTATIONS)) {
      selectedRepresentations = parameters.get(RodaConstants.PLUGIN_PARAMS_SELECTED_REPRESENTATIONS);
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_INCLUDE_SCHEMAS)) {
      includeSchemas = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_INCLUDE_SCHEMAS));
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_INCLUDE_DOCUMENTATION)) {
      includeDocumentation = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_INCLUDE_DOCUMENTATION));
    }
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.RISK_MANAGEMENT;
  }

  @Override
  public String getPreservationEventDescription() {
    return "TODO: Preservation event description";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Success";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Failure";
  }

  @Override
  public Report beforeAllExecute(final IndexService index, final ModelService model, final StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(final IndexService index, final ModelService model, final StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public List<String> getCategories() {
    return Collections.singletonList(RodaConstants.PLUGIN_CATEGORY_DISSEMINATION);
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Collections.singletonList(AIP.class);
  }

  @Override
  public Report execute(final IndexService index, final ModelService model, final StorageService storage,
    final List<LiteOptionalWithCause> liteList) throws PluginException {
    return PluginHelper.processObjects(this, new RODAObjectProcessingLogic<AIP>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        SimpleJobPluginInfo jobPluginInfo, Plugin<AIP> plugin, AIP object) {
        executeOnAip(object, index, model, storage, jobPluginInfo, report, cachedJob);
      }
    }, index, model, storage, liteList);
  }

  /**
   * Execute on a single {@link AIP}.
   * 
   * @param aip
   *          the {@link AIP}.
   * @param index
   *          the {@link IndexService}.
   * @param model
   *          the {@link ModelService}.
   * @param storage
   *          the {@link StorageService}.
   * @param jobPluginInfo
   *          the {@link JobPluginInfo}
   * @param report
   *          the {@link Report}.
   * @param job
   */
  private void executeOnAip(final AIP aip, final IndexService index, final ModelService model,
    final StorageService storage, final JobPluginInfo jobPluginInfo, final Report report, final Job job) {
    LOGGER.debug("Processing AIP {}", aip.getId());

    final Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.ACTIVE);
    PluginHelper.updatePartialJobReport(this, model, index, reportItem, false, job);

    // FIXME 20170118 nvieira condition should be removed when build is not
    // depending on FS
    if (storage instanceof FileStorageService) {
      try {
        AIPLink aipLink = new AIPLink(aip.getId());
        List<AIPLink> links = new ArrayList<AIPLink>();
        links.add(aipLink);

        DIP dip = new DIP();
        dip.setAipIds(links);
        dip.setPermissions(aip.getPermissions());
        dip.setTitle("EARK-DIP");
        dip.setDescription("EARK-DIP generated and filtered based on an AIP");
        dip.setType(RodaConstants.DIP_TYPE_CONVERSION);
        dip = model.createDIP(dip, true);

        StoragePath aipPath = ModelUtils.getAIPStoragePath(aip.getId());
        StoragePath dipDataPath = ModelUtils.getDIPDataStoragePath(dip.getId());
        StoragePath aipOnDIPPath = DefaultStoragePath.parse(dipDataPath, aip.getId());
        storage.createDirectory(aipOnDIPPath);

        // filter AIP structure using plugin parameters
        copyAndFilterAIP(storage, aip, aipPath, aipOnDIPPath);

        // create mets files
        Path fsPath = FSUtils.getEntityPath(RodaCoreFactory.getStoragePath(), aipOnDIPPath);
        EARKAIP earkAIPonDIP = new EARKAIP(RodaFolderAIP.parse(fsPath));
        earkAIPonDIP.setType(IPType.DIP);
        earkAIPonDIP.build(fsPath.getParent(), true);

        // delete aip.json
        storage.deleteResource(DefaultStoragePath.parse(aipOnDIPPath, RodaConstants.STORAGE_AIP_METADATA_FILENAME));

        model.updateDIP(dip);
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
        reportItem.setPluginState(PluginState.SUCCESS);

      } catch (RODAException | ParseException | IPException | InterruptedException e) {
        String message = String.format("Error creating manifest files for AIP %s. Cause: %s.", aip.getId(),
          e.getMessage());
        LOGGER.debug(message, e);
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(message);
      }
    } else {
      jobPluginInfo.incrementObjectsProcessedWithFailure();
      reportItem.setPluginState(PluginState.FAILURE).setPluginDetails("Storage service type used is not supported");
    }

    report.addReport(reportItem);
    PluginHelper.updatePartialJobReport(this, model, index, reportItem, true, job);
  }

  private void copyAndFilterAIP(StorageService storage, AIP aip, StoragePath aipPath, StoragePath aipOnDIPPath)
    throws RequestNotValidException, AlreadyExistsException, GenericException, NotFoundException,
    AuthorizationDeniedException {
    LOGGER.info("Copying AIP to a new DIP");

    copyAIPBaseFiles(storage, aipPath, aipOnDIPPath);

    List<String> representationIds = copyRepresentationsAndItsMetadata(storage, aip, aipPath, aipOnDIPPath);
    List<String> descriptiveMetadataIds = copyAIPDescriptiveMetadata(storage, aip, aipPath, aipOnDIPPath);
    copyPreservationMetadata(storage, aipPath, aipOnDIPPath);
    copyOtherMetadata(storage, aipPath, aipOnDIPPath);

    copyAndUpdateAIPJson(storage, aip, aipOnDIPPath, representationIds, descriptiveMetadataIds);
  }

  private void copyAIPBaseFiles(StorageService storage, StoragePath aipPath, StoragePath aipOnDIPPath)
    throws RequestNotValidException, AlreadyExistsException, GenericException, NotFoundException,
    AuthorizationDeniedException {

    StoragePath schemasPath = DefaultStoragePath.parse(aipPath, RodaConstants.STORAGE_DIRECTORY_SCHEMAS);
    if (includeSchemas && storage.hasDirectory(schemasPath)) {
      storage.copy(storage, schemasPath,
        DefaultStoragePath.parse(aipOnDIPPath, RodaConstants.STORAGE_DIRECTORY_SCHEMAS));
    }

    StoragePath documentationPath = DefaultStoragePath.parse(aipPath, RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION);
    if (includeDocumentation && storage.hasDirectory(documentationPath)) {
      storage.copy(storage, documentationPath,
        DefaultStoragePath.parse(aipOnDIPPath, RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION));
    }
  }

  private List<String> copyRepresentationsAndItsMetadata(StorageService storage, AIP aip, StoragePath aipPath,
    StoragePath aipOnDIPPath) throws RequestNotValidException, AlreadyExistsException, GenericException,
    NotFoundException, AuthorizationDeniedException {
    List<String> representationTypes = Arrays.asList(selectedRepresentations.toLowerCase().split(",\\s*"));
    List<String> representationIds = new ArrayList<String>();

    for (Representation representation : aip.getRepresentations()) {
      String representationId = representation.getId();
      LOGGER.info("Testing if representation '{}' with type '{}' should be copied", representationId,
        representation.getType());

      if (!includeSelectedRepresentations
        || (includeSelectedRepresentations && representationTypes.contains(representation.getType().toLowerCase()))) {
        representationIds.add(representationId);
        StoragePath representationPath = DefaultStoragePath.parse(aipPath,
          RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS, representationId);

        if (storage.hasDirectory(representationPath)) {
          StoragePath representationDIPPath = DefaultStoragePath.parse(aipOnDIPPath,
            RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS, representationId);

          StoragePath representationDataPath = DefaultStoragePath.parse(representationPath,
            RodaConstants.STORAGE_DIRECTORY_DATA);
          if (storage.hasDirectory(representationDataPath)) {
            storage.copy(storage, representationDataPath,
              DefaultStoragePath.parse(representationDIPPath, RodaConstants.STORAGE_DIRECTORY_DATA));
          }

          StoragePath representationSchemasPath = DefaultStoragePath.parse(representationPath,
            RodaConstants.STORAGE_DIRECTORY_SCHEMAS);
          if (includeSchemas && storage.hasDirectory(representationSchemasPath)) {
            storage.copy(storage, representationSchemasPath,
              DefaultStoragePath.parse(representationDIPPath, RodaConstants.STORAGE_DIRECTORY_SCHEMAS));
          }

          StoragePath representationDocPath = DefaultStoragePath.parse(representationPath,
            RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION);
          if (includeDocumentation && storage.hasDirectory(representationDocPath)) {
            storage.copy(storage, representationDocPath,
              DefaultStoragePath.parse(representationDIPPath, RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION));
          }

          for (DescriptiveMetadata dm : representation.getDescriptiveMetadata()) {
            copyDescriptiveMetadata(storage, dm, representationPath, representationDIPPath);
          }

          copyPreservationMetadata(storage, representationPath, representationDIPPath);
          copyOtherMetadata(storage, representationPath, representationDIPPath);
        }
      }
    }

    return representationIds;
  }

  private List<String> copyAIPDescriptiveMetadata(StorageService storage, AIP aip, StoragePath aipPath,
    StoragePath aipOnDIPPath) throws RequestNotValidException, AlreadyExistsException, GenericException,
    NotFoundException, AuthorizationDeniedException {
    List<String> descriptiveMetadataIds = new ArrayList<String>();

    for (DescriptiveMetadata dm : aip.getDescriptiveMetadata()) {
      String dmId = copyDescriptiveMetadata(storage, dm, aipPath, aipOnDIPPath);
      if (StringUtils.isNotBlank(dmId)) {
        descriptiveMetadataIds.add(dmId);
      }
    }

    return descriptiveMetadataIds;
  }

  private String copyDescriptiveMetadata(StorageService storage, DescriptiveMetadata dm, StoragePath sourcePath,
    StoragePath targetPath) throws RequestNotValidException, AlreadyExistsException, GenericException,
    NotFoundException, AuthorizationDeniedException {
    List<String> metadataTypes = Arrays.asList(selectedDescriptiveMetadata.toLowerCase().split(",\\s*"));
    String versionType = dm.getType() + RodaConstants.METADATA_VERSION_SEPARATOR + dm.getVersion();
    String dmId = dm.getId();

    if (!includeSelectedDescriptiveMetadata || metadataTypes.contains(versionType.toLowerCase())) {
      StoragePath oldMetadataPath = DefaultStoragePath.parse(sourcePath, RodaConstants.STORAGE_DIRECTORY_METADATA,
        RodaConstants.STORAGE_DIRECTORY_DESCRIPTIVE, dmId);

      if (storage.hasBinary(oldMetadataPath)) {
        StoragePath newMetadataPath = DefaultStoragePath.parse(targetPath, RodaConstants.STORAGE_DIRECTORY_METADATA,
          RodaConstants.STORAGE_DIRECTORY_DESCRIPTIVE, dmId);

        storage.copy(storage, oldMetadataPath, newMetadataPath);
        return dmId;
      }
    }

    return null;
  }

  private void copyPreservationMetadata(StorageService storage, StoragePath sourcePath, StoragePath targetPath)
    throws RequestNotValidException, AlreadyExistsException, GenericException, NotFoundException,
    AuthorizationDeniedException {
    if (includeAllPreservationMetadata) {
      StoragePath oldMetadataPath = DefaultStoragePath.parse(sourcePath, RodaConstants.STORAGE_DIRECTORY_METADATA,
        RodaConstants.STORAGE_DIRECTORY_PRESERVATION);

      if (storage.hasDirectory(oldMetadataPath)) {
        StoragePath newMetadataPath = DefaultStoragePath.parse(targetPath, RodaConstants.STORAGE_DIRECTORY_METADATA,
          RodaConstants.STORAGE_DIRECTORY_PRESERVATION);

        storage.copy(storage, oldMetadataPath, newMetadataPath);
      }
    }
  }

  private void copyOtherMetadata(StorageService storage, StoragePath sourcePath, StoragePath targetPath) {
    List<String> metadataTypes = Arrays.asList(selectedOtherMetadata.split(",\\s*"));

    if (includeSelectedOtherMetadata) {
      for (String type : metadataTypes) {
        try {
          StoragePath oldMetadataPath = DefaultStoragePath.parse(sourcePath, RodaConstants.STORAGE_DIRECTORY_METADATA,
            RodaConstants.STORAGE_DIRECTORY_OTHER, type);

          if (storage.hasDirectory(oldMetadataPath)) {
            StoragePath newMetadataPath = DefaultStoragePath.parse(targetPath, RodaConstants.STORAGE_DIRECTORY_METADATA,
              RodaConstants.STORAGE_DIRECTORY_OTHER, type);

            storage.copy(storage, oldMetadataPath, newMetadataPath);
          }
        } catch (RequestNotValidException | AlreadyExistsException | GenericException | NotFoundException
          | AuthorizationDeniedException e) {
          LOGGER.error("Error copying other metadata type '{}' when creating EARK-DIP", type, e);
        }
      }
    } else {
      try {
        StoragePath oldMetadataPath = DefaultStoragePath.parse(sourcePath, RodaConstants.STORAGE_DIRECTORY_METADATA,
          RodaConstants.STORAGE_DIRECTORY_OTHER);

        if (storage.hasDirectory(oldMetadataPath)) {
          StoragePath newMetadataPath = DefaultStoragePath.parse(targetPath, RodaConstants.STORAGE_DIRECTORY_METADATA,
            RodaConstants.STORAGE_DIRECTORY_OTHER);

          storage.copy(storage, oldMetadataPath, newMetadataPath);
        }
      } catch (RequestNotValidException | AlreadyExistsException | GenericException | NotFoundException
        | AuthorizationDeniedException e) {
        LOGGER.error("Error copying other metadata '{}' when creating EARK-DIP", e);
      }
    }
  }

  private void copyAndUpdateAIPJson(StorageService storage, AIP aip, StoragePath aipOnDIPPath,
    List<String> representationIds, List<String> descriptiveMetadataIds) throws GenericException,
    RequestNotValidException, AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    String json = JsonUtils.getJsonFromObject(aip);
    JsonNode parseJson = JsonUtils.parseJson(json);

    JsonNode representationList = parseJson.get(RodaConstants.AIP_REPRESENTATIONS);
    for (Iterator<JsonNode> representationIt = representationList.elements(); representationIt.hasNext();) {
      JsonNode representation = (JsonNode) representationIt.next();
      if (!representationIds.contains(representation.get("id").asText())) {
        representationIt.remove();
      }
    }

    JsonNode metadataList = parseJson.get(RodaConstants.AIP_DESCRIPTIVE_METADATA);
    for (Iterator<JsonNode> descriptiveMetadataIt = metadataList.elements(); descriptiveMetadataIt.hasNext();) {
      JsonNode descriptiveMetadata = (JsonNode) descriptiveMetadataIt.next();
      if (!representationIds.contains(descriptiveMetadata.get("id").asText())) {
        descriptiveMetadataIt.remove();
      }
    }

    String updatedJson = parseJson.toString();
    StringContentPayload content = new StringContentPayload(updatedJson);
    StoragePath aipJson = DefaultStoragePath.parse(aipOnDIPPath, RodaConstants.STORAGE_AIP_METADATA_FILENAME);
    storage.createBinary(aipJson, content, false);
  }

}
