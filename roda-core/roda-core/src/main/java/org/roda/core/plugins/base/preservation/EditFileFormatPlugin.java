/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.preservation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.risks.IncidenceStatus;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.base.characterization.SiegfriedPluginUtils;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        .withDescription("The MIME type to set for all selected files.").build());
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
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INCIDENCES,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_CLEAR_INCIDENCES, "Clear Siegfried risk incidences",
          PluginParameter.PluginParameterType.BOOLEAN)
        .withDescription(
          "Have this plugin clear risk incidences caused by Siegfried file format identification warnings.")
        .build());
  }

  private String mimetype;
  private String format;
  private String formatVersion;
  private String pronom;
  private boolean clearIncidences;

  public static String getStaticName() {
    return "Edit File Format";
  }

  public static String getStaticDescription() {
    return "Overwrites the selected files' extension, MIME type, format and PRONOM identifier with provided values.";
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
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_CLEAR_INCIDENCES));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    mimetype = parameters.get(RodaConstants.PLUGIN_PARAMS_MIMETYPE);
    format = parameters.get(RodaConstants.PLUGIN_PARAMS_FORMAT);
    formatVersion = parameters.get(RodaConstants.PLUGIN_PARAMS_FORMAT_VERSION);
    pronom = parameters.get(RodaConstants.PLUGIN_PARAMS_PRONOM);
    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_CLEAR_INCIDENCES)) {
      clearIncidences = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_CLEAR_INCIDENCES));
    } else {
      clearIncidences = false;
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, List<LiteOptionalWithCause> liteList)
    throws PluginException {
    return PluginHelper.processObjects(this, new RODAObjectsProcessingLogic<File>() {
      @Override
      public void process(IndexService index, ModelService model, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<File> plugin, List<File> objects) {
        processFiles(index, model, report, jobPluginInfo, cachedJob, objects);
      }
    }, index, model, liteList);
  }

  private void processFiles(IndexService index, ModelService model, Report report, JobPluginInfo jobPluginInfo,
    Job cachedJob, List<File> files) {
    Report parametersReport = validateParameters();

    for (File file : files) {
      Report reportItem = PluginHelper.initPluginReportItem(this, IdUtils.getFileId(file), File.class);
      PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
      if (parametersReport.getPluginState().equals(PluginState.SUCCESS)) {
        if (file.isDirectory()) {
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          reportItem.setPluginState(PluginState.SKIPPED).setPluginDetails("Skipping folder");
        } else {
          List<LinkingIdentifier> sources = new ArrayList<>();
          try {
            sources.add(setFileFormatMetadata(model, index, file, cachedJob.getId(), cachedJob.getUsername()));
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
      } else {
        reportItem.setPluginState(PluginState.FAILURE);
        reportItem.setPluginDetails(parametersReport.getPluginDetails());
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      }

      report.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
    }
  }

  private Report validateParameters() {
    Report reportItem = PluginHelper.initPluginReportItem(this, null, File.class);
    reportItem.setPluginState(PluginState.SUCCESS);
    if (StringUtils.isNotBlank(mimetype) && !mimetype.matches(RodaConstants.REGEX_MIME)) {
      reportItem.setPluginState(PluginState.FAILURE).addPluginDetails(
        "Invalid MIME type (expecting a string that conforms to \"" + RodaConstants.REGEX_MIME + "\".");
    }
    if (StringUtils.isNotBlank(pronom) && !pronom.matches(RodaConstants.REGEX_PUID)) {
      reportItem.setPluginState(PluginState.FAILURE).addPluginDetails(
        "Invalid PRONOM identifier (expecting a string that conforms to \"" + RodaConstants.REGEX_PUID + "\".");
    }
    return reportItem;
  }

  private LinkingIdentifier setFileFormatMetadata(ModelService model, IndexService index, File file, String jobId,
    String username) throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException,
    PluginException {
    String updatedFormat = format;
    String updatedFormatVersion = formatVersion;
    String updatedPronomIdentifier = pronom;
    String updatedMimetype = mimetype;

    List<String> notes = mitigatePreviousIncidencesAndCreateNotes(model, index, file.getAipId(),
      file.getRepresentationId(), file.getId(), file.getPath(), username);
    PremisV3Utils.updateFormatPreservationMetadata(model, file.getAipId(), file.getRepresentationId(), file.getPath(),
      file.getId(), updatedFormat, updatedFormatVersion, updatedPronomIdentifier, updatedMimetype, notes, username,
      true);

    LinkingIdentifier source = PluginHelper.getLinkingIdentifier(file.getAipId(), file.getRepresentationId(),
      file.getPath(), file.getId(),
      RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE);
    model.notifyFileUpdated(file);
    return source;
  }

  private List<String> mitigatePreviousIncidencesAndCreateNotes(ModelService model, IndexService index, String aipId,
    String representationId, String fileId, List<String> filePath, String username)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    List<String> notes = new ArrayList<>();
    notes.add(RodaConstants.PRESERVATION_FORMAT_NOTE_MANUAL);
    List<RiskIncidence> siegfriedRiskIncidences = SiegfriedPluginUtils.getPreviousSiegfriedIncidences(model, index,
      fileId);
    if (!siegfriedRiskIncidences.isEmpty()) {
      if (clearIncidences) {
        for (RiskIncidence incidence : siegfriedRiskIncidences) {
          incidence.setStatus(IncidenceStatus.MITIGATED);
          model.updateRiskIncidence(incidence, true);
        }
      } else {
        for (String note : PremisV3Utils.getFormatNotes(model, aipId, representationId, filePath, fileId, username)) {
          if (note.contains(RodaConstants.PRESERVATION_FORMAT_NOTE_SIEGFRIED_WARNING)) {
            notes.add(note);
          }
        }
      }
    }
    return notes;
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
    return true;
  }

  @Override
  public List<Class<File>> getObjectClasses() {
    return Arrays.asList(File.class);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model) throws PluginException {
    // do nothing
    return null;
  }
}
