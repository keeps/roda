package org.roda.core.plugins.base.preservation;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.base.characterization.SiegfriedPlugin;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StringContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class EditFileFormatPlugin extends AbstractPlugin<File> {
  private static final Logger LOGGER = LoggerFactory.getLogger(EditFileFormatPlugin.class);
  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_MIMETYPE,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_MIMETYPE, "Mimetype", PluginParameter.PluginParameterType.STRING)
        .withDescription("The Mimetype to set for all selected files.").build());
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_FORMAT,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_FORMAT, "Format", PluginParameter.PluginParameterType.STRING)
        .withDescription("The full format descriptor to set for all selected files.").build());
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_FORMAT_VERSION,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_FORMAT_VERSION, "Format version",
          PluginParameter.PluginParameterType.STRING)
        .withDescription("The version for the format descriptor to set for all selected files.").build());
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_PRONOM,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_PRONOM, "PRONOM", PluginParameter.PluginParameterType.STRING)
        .withDescription("The PRONOM identifier to set for all selected files.").build());
  }

  private String mimetype;
  private String format;
  private String formatVersion;
  private String pronom;

  public static String getStaticName() {
    return "Edit File Format";
  }

  public static String getStaticDescription() {
    return "Overwrites the selected files' extension, mimetype, format and PRONOM identifier with provided values.";
  }

  @Override
  public void init() {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  @Override
  public String getDescription() {
    return getStaticDescription();
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_MIMETYPE));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_FORMAT));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_FORMAT_VERSION));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_PRONOM));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    mimetype = parameters.get(RodaConstants.PLUGIN_PARAMS_MIMETYPE);
    format = parameters.get(RodaConstants.PLUGIN_PARAMS_FORMAT);
    formatVersion = parameters.get(RodaConstants.PLUGIN_PARAMS_FORMAT_VERSION);
    pronom = parameters.get(RodaConstants.PLUGIN_PARAMS_PRONOM);
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return PluginHelper.processObjects(this, new RODAObjectsProcessingLogic<File>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<File> plugin, List<File> objects) {
        processFiles(index, model, storage, report, jobPluginInfo, cachedJob, objects);
      }
    }, index, model, storage, liteList);
  }

  private void processFiles(IndexService index, ModelService model, StorageService storage, Report report,
    JobPluginInfo jobPluginInfo, Job cachedJob, List<File> files) {
    JsonNode payload = createPayload();
    for (File file : files) {
      Report reportItem = PluginHelper.initPluginReportItem(this, file.getId(), File.class);
      PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
      if (file.isDirectory()) {
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        reportItem.setPluginState(PluginState.SKIPPED).setPluginDetails("Skipping folder");
      } else {
        List<LinkingIdentifier> sources = new ArrayList<>();
        try {
          sources.add(setFileFormatMetadata(model, file, cachedJob.getId(), cachedJob.getUsername(), payload));
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
          reportItem.setPluginState(PluginState.SUCCESS);
        } catch (RequestNotValidException | NotFoundException | AuthorizationDeniedException | GenericException
          | PluginException e) {
          LOGGER.error("Error setting format metadata on file {}: {}", file.getId(), e.getMessage(), e);

          jobPluginInfo.incrementObjectsProcessedWithFailure();
          reportItem.setPluginState(PluginState.FAILURE)
            .setPluginDetails("Error setting format metadata on file " + file.getId() + ": " + e.getMessage());
        }

        try {
          PluginHelper.createPluginEvent(this, file.getAipId(), file.getRepresentationId(), file.getPath(),
            file.getId(), model, index, sources, null, reportItem.getPluginState(), "", true, cachedJob);
        } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException
          | ValidationException | AlreadyExistsException e) {
          LOGGER.error("Error creating event: {}", e.getMessage(), e);
        }
      }

      report.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
    }
  }

  private JsonNode createPayload() {
    JsonMapper mapper = new JsonMapper();
    ObjectNode payloadJson = mapper.createObjectNode();
    ArrayNode matchesArray = payloadJson.putArray(RodaConstants.SIEGFRIED_PAYLOAD_MATCHES);
    ObjectNode matchesNode = matchesArray.addObject();

    matchesNode.put(RodaConstants.SIEGFRIED_PAYLOAD_MATCH_NS, RodaConstants.SIEGFRIED_PAYLOAD_MATCH_NS_PRONOM);
    if (mimetype != null) {
      matchesNode.put(RodaConstants.SIEGFRIED_PAYLOAD_MATCH_MIMETYPE, mimetype);
    }
    if (format != null) {
      matchesNode.put(RodaConstants.SIEGFRIED_PAYLOAD_MATCH_FORMAT_DESIGNATION, format);
    }
    if (formatVersion != null) {
      matchesNode.put(RodaConstants.SIEGFRIED_PAYLOAD_MATCH_FORMAT_VERSION, formatVersion);
    }
    if (pronom != null) {
      matchesNode.put(RodaConstants.SIEGFRIED_PAYLOAD_MATCH_ID, pronom);
    }

    return payloadJson;
  }

  private LinkingIdentifier setFileFormatMetadata(ModelService model, File file, String jobId, String username,
    JsonNode payloadJson)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException,
    PluginException {
    StoragePath fileDataPath = ModelUtils.getFileStoragePath(file);
    StorageService tmpStorageService = ModelUtils.resolveTemporaryResourceShallow(jobId, model.getStorage(),
      ModelUtils.getAIPStoragePath(file.getAipId()));
    LinkingIdentifier source = null;
    try (DirectResourceAccess directAccess = tmpStorageService.getDirectAccess(fileDataPath)) {
      List<String> jsonFilePath = new ArrayList<>();
      jsonFilePath.add(file.getId());

      Path fileFsPath = directAccess.getPath();
      Path fullFsPath = Paths.get(FilenameUtils.normalize(fileFsPath.toString()));
      Path relativeFsPath = fileFsPath.relativize(fullFsPath);

      for (int i = 0; i < relativeFsPath.getNameCount()
        && StringUtils.isNotBlank(relativeFsPath.getName(i).toString()); i++) {
        jsonFilePath.add(relativeFsPath.getName(i).toString());
      }

      jsonFilePath.remove(jsonFilePath.size() - 1);
      String jsonFileId = fullFsPath.getFileName().toString();

      ContentPayload payload = new StringContentPayload(payloadJson.toString());
      model.createOrUpdateOtherMetadata(file.getAipId(), file.getRepresentationId(), jsonFilePath, jsonFileId,
        SiegfriedPlugin.FILE_SUFFIX, RodaConstants.OTHER_METADATA_TYPE_SIEGFRIED, payload, username, false);

      String updatedFormat = format;
      String updatedFormatVersion = formatVersion;
      String updatedPronomIdentifier = pronom;
      String updatedMimetype = mimetype;

      PremisV3Utils.updateFormatPreservationMetadata(model, file.getAipId(), file.getRepresentationId(), jsonFilePath,
        jsonFileId, updatedFormat, updatedFormatVersion, updatedPronomIdentifier, updatedMimetype,
        Arrays.asList(RodaConstants.PRESERVATION_FORMAT_NOTE_MANUAL), username, true);

      source = PluginHelper.getLinkingIdentifier(file.getAipId(), file.getRepresentationId(), jsonFilePath, jsonFileId,
        RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE);
      model.notifyFileUpdated(file);
    } catch (IOException e) {
      throw new PluginException("Could not create direct access StorageService for file " + file.getId(), e);
    }
    return source;
  }

  @Override
  public RodaConstants.PreservationEventType getPreservationEventType() {
    return RodaConstants.PreservationEventType.UPDATE;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Changed files' format metadata";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "File formats were updated and recorded in PREMIS objects.";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Failed to update file formats in the package.";
  }

  @Override
  public PluginType getType() {
    return PluginType.MISC;
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_MANAGEMENT, RodaConstants.PLUGIN_CATEGORY_MAINTENANCE);
  }

  @Override
  public Plugin cloneMe() {
    EditFileFormatPlugin editFileFormatPlugin = new EditFileFormatPlugin();
    editFileFormatPlugin.init();
    return editFileFormatPlugin;
  }

  @Override
  public boolean areParameterValuesValid() {
    if (mimetype != null && !mimetype.matches(RodaConstants.REGEX_MIME)) {
      return false;
    }
    if (pronom != null && !pronom.matches(RodaConstants.REGEX_PUID)) {
      return false;
    }
    return true;
  }

  @Override
  public List<Class<File>> getObjectClasses() {
    return Arrays.asList(File.class);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }
}
