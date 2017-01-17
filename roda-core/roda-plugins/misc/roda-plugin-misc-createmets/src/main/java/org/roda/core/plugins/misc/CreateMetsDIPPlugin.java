/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.misc;

import java.io.IOException;
import java.nio.file.Files;
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
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPLink;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.Representation;
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
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StringContentPayload;
import org.roda.core.storage.fs.FSUtils;
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

  private boolean includeSubmission = true;
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

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_INCLUDE_SUBMISSION,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_INCLUDE_SUBMISSION, "Include submission folder",
        PluginParameterType.BOOLEAN, "true", false, false, "Include submission folder with SIP file."));

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
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_INCLUDE_SUBMISSION));
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

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_INCLUDE_SUBMISSION)) {
      includeSubmission = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_INCLUDE_SUBMISSION));
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
    return Collections.singletonList(RodaConstants.PLUGIN_CATEGORY_MISC);
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
      dip = model.createDIP(dip, false);

      Path aipPath = FSUtils.getEntityPath(RodaCoreFactory.getStoragePath(), ModelUtils.getAIPStoragePath(aip.getId()));
      Path dipDataPath = FSUtils.getEntityPath(RodaCoreFactory.getStoragePath(),
        ModelUtils.getDIPDataStoragePath(dip.getId()));
      Path aipOnDIPPath = Files.createDirectories(dipDataPath.resolve(aip.getId()));

      // filter AIP structure using plugin parameters
      copyAndFilterAIP(aip, aipPath, aipOnDIPPath);

      // create mets files
      EARKAIP earkAIPonDIP = new EARKAIP(RodaFolderAIP.parse(aipOnDIPPath));
      earkAIPonDIP.setType(IPType.DIP);
      earkAIPonDIP.build(aipOnDIPPath.getParent(), true);

      // delete aip.json
      FSUtils.deletePath(aipOnDIPPath.resolve(RodaConstants.STORAGE_AIP_METADATA_FILENAME));

      jobPluginInfo.incrementObjectsProcessedWithSuccess();
      reportItem.setPluginState(PluginState.SUCCESS);

    } catch (final RODAException | ParseException | IPException | InterruptedException | IOException e) {
      final String message = String.format("Error creating manifest files for AIP %s. Cause: %s.", aip.getId(),
        e.getMessage());
      LOGGER.debug(message, e);
      jobPluginInfo.incrementObjectsProcessedWithFailure();
      reportItem.setPluginState(PluginState.FAILURE);
      reportItem.setPluginDetails(message);
    }

    report.addReport(reportItem);
    PluginHelper.updatePartialJobReport(this, model, index, reportItem, true, job);
  }

  private void copyAndFilterAIP(AIP aip, Path aipPath, Path aipOnDIPPath)
    throws AlreadyExistsException, GenericException {
    LOGGER.info("Copying AIP to a new DIP");

    copyAIPBaseFiles(aipPath, aipOnDIPPath);

    List<String> representationIds = copyRepresentationsAndItsMetadata(aip, aipPath, aipOnDIPPath);
    List<String> descriptiveMetadataIds = copyAIPDescriptiveMetadata(aip, aipPath, aipOnDIPPath);
    copyPreservationMetadata(aipPath, aipOnDIPPath);
    copyOtherMetadata(aipPath, aipOnDIPPath);

    try {
      copyAndUpdateAIPJson(aip, aipOnDIPPath, representationIds, descriptiveMetadataIds);
    } catch (IOException | GenericException e) {
      LOGGER.error("Error updating aip.json file on DIP");
    }
  }

  private void copyAIPBaseFiles(Path aipPath, Path aipOnDIPPath) throws AlreadyExistsException, GenericException {
    if (includeSubmission && Files.exists(aipPath.resolve(RodaConstants.STORAGE_DIRECTORY_SUBMISSION))) {
      FSUtils.copy(aipPath.resolve(RodaConstants.STORAGE_DIRECTORY_SUBMISSION),
        aipOnDIPPath.resolve(RodaConstants.STORAGE_DIRECTORY_SUBMISSION), true);
    }

    if (includeSchemas && Files.exists(aipPath.resolve(RodaConstants.STORAGE_DIRECTORY_SCHEMAS))) {
      FSUtils.copy(aipPath.resolve(RodaConstants.STORAGE_DIRECTORY_SCHEMAS),
        aipOnDIPPath.resolve(RodaConstants.STORAGE_DIRECTORY_SCHEMAS), true);
    }

    if (includeDocumentation && Files.exists(aipPath.resolve(RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION))) {
      FSUtils.copy(aipPath.resolve(RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION),
        aipOnDIPPath.resolve(RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION), true);
    }
  }

  private List<String> copyRepresentationsAndItsMetadata(AIP aip, Path aipPath, Path aipOnDIPPath)
    throws AlreadyExistsException, GenericException {
    List<String> representationTypes = Arrays.asList(selectedRepresentations.toLowerCase().split(",\\s*"));
    List<String> representationIds = new ArrayList<String>();

    for (Representation representation : aip.getRepresentations()) {
      String representationId = representation.getId();
      LOGGER.info("Testing if representation '{}' with type '{}' should be copied", representationId,
        representation.getType());

      if (!includeSelectedRepresentations
        || (includeSelectedRepresentations && representationTypes.contains(representation.getType().toLowerCase()))) {
        representationIds.add(representationId);
        Path representationPath = aipPath.resolve(RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS)
          .resolve(representationId);

        if (Files.exists(representationPath)) {
          Path representationDIPPath = aipOnDIPPath.resolve(RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS)
            .resolve(representationId);

          if (Files.exists(representationPath.resolve(RodaConstants.STORAGE_DIRECTORY_DATA))) {
            FSUtils.copy(representationPath.resolve(RodaConstants.STORAGE_DIRECTORY_DATA),
              representationDIPPath.resolve(RodaConstants.STORAGE_DIRECTORY_DATA), true);
          }

          if (includeSchemas && Files.exists(representationPath.resolve(RodaConstants.STORAGE_DIRECTORY_SCHEMAS))) {
            FSUtils.copy(representationPath.resolve(RodaConstants.STORAGE_DIRECTORY_SCHEMAS),
              representationDIPPath.resolve(RodaConstants.STORAGE_DIRECTORY_SCHEMAS), true);
          }

          if (includeDocumentation
            && Files.exists(representationPath.resolve(RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION))) {
            FSUtils.copy(representationPath.resolve(RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION),
              representationDIPPath.resolve(RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION), true);
          }

          List<String> metadataTypes = Arrays.asList(selectedDescriptiveMetadata.toLowerCase().split(",\\s*"));
          for (DescriptiveMetadata dm : representation.getDescriptiveMetadata()) {
            copyDescriptiveMetadata(dm, metadataTypes, representationPath, representationDIPPath);
          }

          copyPreservationMetadata(representationPath, representationDIPPath);
          copyOtherMetadata(representationPath, representationDIPPath);
        }
      }
    }

    return representationIds;
  }

  private List<String> copyAIPDescriptiveMetadata(AIP aip, Path aipPath, Path aipOnDIPPath)
    throws AlreadyExistsException, GenericException {
    List<String> metadataTypes = Arrays.asList(selectedDescriptiveMetadata.toLowerCase().split(",\\s*"));
    List<String> descriptiveMetadataIds = new ArrayList<String>();

    for (DescriptiveMetadata dm : aip.getDescriptiveMetadata()) {
      String dmId = copyDescriptiveMetadata(dm, metadataTypes, aipPath, aipOnDIPPath);
      if (StringUtils.isNotBlank(dmId)) {
        descriptiveMetadataIds.add(dmId);
      }
    }

    return descriptiveMetadataIds;
  }

  private String copyDescriptiveMetadata(DescriptiveMetadata dm, List<String> metadataTypes, Path sourcePath,
    Path targetPath) throws AlreadyExistsException, GenericException {
    String versionType = dm.getType() + "_" + dm.getVersion();
    String dmId = dm.getId();

    if (!includeSelectedDescriptiveMetadata
      || (includeSelectedDescriptiveMetadata && metadataTypes.contains(versionType.toLowerCase()))) {
      Path oldMetadataPath = sourcePath.resolve(RodaConstants.STORAGE_DIRECTORY_METADATA)
        .resolve(RodaConstants.STORAGE_DIRECTORY_DESCRIPTIVE).resolve(dmId);

      if (Files.exists(oldMetadataPath)) {
        Path newMetadataPath = targetPath.resolve(RodaConstants.STORAGE_DIRECTORY_METADATA)
          .resolve(RodaConstants.STORAGE_DIRECTORY_DESCRIPTIVE).resolve(dmId);
        FSUtils.copy(oldMetadataPath, newMetadataPath, true);
        return dmId;
      }
    }

    return null;
  }

  private void copyPreservationMetadata(Path sourcePath, Path targetPath)
    throws AlreadyExistsException, GenericException {
    if (includeAllPreservationMetadata) {
      Path oldMetadataPath = sourcePath.resolve(RodaConstants.STORAGE_DIRECTORY_METADATA)
        .resolve(RodaConstants.STORAGE_DIRECTORY_PRESERVATION);
      if (Files.exists(oldMetadataPath)) {
        Path newMetadataPath = targetPath.resolve(RodaConstants.STORAGE_DIRECTORY_METADATA)
          .resolve(RodaConstants.STORAGE_DIRECTORY_PRESERVATION);
        FSUtils.copy(oldMetadataPath, newMetadataPath, true);
      }
    }
  }

  private void copyOtherMetadata(Path sourcePath, Path targetPath) {
    List<String> metadataTypes = Arrays.asList(selectedOtherMetadata.split(",\\s*"));

    if (includeSelectedOtherMetadata) {
      for (String type : metadataTypes) {
        Path oldMetadataPath = sourcePath.resolve(RodaConstants.STORAGE_DIRECTORY_METADATA)
          .resolve(RodaConstants.STORAGE_DIRECTORY_OTHER).resolve(type);

        if (Files.exists(oldMetadataPath)) {
          Path newMetadataPath = targetPath.resolve(RodaConstants.STORAGE_DIRECTORY_METADATA)
            .resolve(RodaConstants.STORAGE_DIRECTORY_OTHER).resolve(type);

          try {
            FSUtils.copy(oldMetadataPath, newMetadataPath, true);
          } catch (AlreadyExistsException | GenericException e) {
            LOGGER.error("Error copying other metadata type '{}' when creating EARK-DIP", type, e);
          }
        }
      }
    } else {
      Path oldMetadataPath = sourcePath.resolve(RodaConstants.STORAGE_DIRECTORY_METADATA)
        .resolve(RodaConstants.STORAGE_DIRECTORY_OTHER);

      if (Files.exists(oldMetadataPath)) {
        Path newMetadataPath = targetPath.resolve(RodaConstants.STORAGE_DIRECTORY_METADATA)
          .resolve(RodaConstants.STORAGE_DIRECTORY_OTHER);

        try {
          FSUtils.copy(oldMetadataPath, newMetadataPath, true);
        } catch (AlreadyExistsException | GenericException e) {
          LOGGER.error("Error copying other metadata '{}' when creating EARK-DIP", e);
        }
      }
    }
  }

  private void copyAndUpdateAIPJson(AIP aip, Path aipOnDIPPath, List<String> representationIds,
    List<String> descriptiveMetadataIds) throws GenericException, IOException {
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
    content.writeToPath(aipOnDIPPath.resolve(RodaConstants.STORAGE_AIP_METADATA_FILENAME));
  }

}
