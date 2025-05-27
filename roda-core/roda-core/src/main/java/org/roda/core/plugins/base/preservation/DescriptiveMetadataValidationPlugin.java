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

import org.apache.commons.lang3.StringUtils;
import org.roda.core.common.validation.ValidationUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.validation.ValidationReport;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.RODAObjectProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DescriptiveMetadataValidationPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DescriptiveMetadataValidationPlugin.class);
  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_VALIDATE_DESCRIPTIVE_METADATA, PluginParameter
      .getBuilder(RodaConstants.PLUGIN_PARAMS_VALIDATE_DESCRIPTIVE_METADATA, "Validate descriptive metadata",
        PluginParameterType.BOOLEAN)
      .withDefaultValue("true")
      .withDescription(
        "If true, the action will check if the descriptive metadata is valid according to the schemas installed in the repository.")
      .build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DESCRIPTIVE_METADATA_TYPE, PluginParameter
      .getBuilder(RodaConstants.PLUGIN_PARAMS_DESCRIPTIVE_METADATA_TYPE, "Descriptive metadata format",
        PluginParameterType.STRING)
      .isMandatory(false)
      .withDescription(
        "Descriptive metadata format to be used as fallback if the information package does not specify the metadata format or if the action is set to FORCE.")
      .build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DESCRIPTIVE_METADATA_VERSION, PluginParameter
      .getBuilder(RodaConstants.PLUGIN_PARAMS_DESCRIPTIVE_METADATA_VERSION, "Descriptive metadata version",
        PluginParameterType.STRING)
      .isMandatory(false)
      .withDescription(
        "Descriptive metadata version to be used as fallback if the information package does not specify the metadata version or if the action is set to FORCE.")
      .build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DESCRIPTIVE_METADATA_FORCE_TYPE, PluginParameter
      .getBuilder(RodaConstants.PLUGIN_PARAMS_DESCRIPTIVE_METADATA_FORCE_TYPE, "Force metadata format and version",
        PluginParameterType.BOOLEAN)
      .withDefaultValue("false")
      .withDescription(
        "If true, bypass the metadata format and version set in the information package and use the metadata format and version passed as parameters (see above).")
      .build());
  }

  private boolean validateDescriptiveMetadata;
  private boolean forceDescriptiveMetadataType;
  private String metadataType;
  private String metadataVersion;

  private List<Pair<String, String>> schemasInfo;

  public static String getStaticName() {
    return "Metadata validation";
  }

  public static String getStaticDescription() {
    return "Checks if the descriptive metadata included in the Information Package is present, and if it is valid according to the "
      + "XML Schemas installed in the repository. A validation report is generated indicating which Information Packages have valid and invalid metadata.";
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
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_VALIDATE_DESCRIPTIVE_METADATA));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DESCRIPTIVE_METADATA_TYPE));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DESCRIPTIVE_METADATA_VERSION));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DESCRIPTIVE_METADATA_FORCE_TYPE));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_VALIDATE_DESCRIPTIVE_METADATA)) {
      validateDescriptiveMetadata = Boolean
        .parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_VALIDATE_DESCRIPTIVE_METADATA));
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DESCRIPTIVE_METADATA_FORCE_TYPE)) {
      forceDescriptiveMetadataType = Boolean
        .parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_DESCRIPTIVE_METADATA_FORCE_TYPE));
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DESCRIPTIVE_METADATA_VERSION)) {
      metadataVersion = parameters.get(RodaConstants.PLUGIN_PARAMS_DESCRIPTIVE_METADATA_VERSION);
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DESCRIPTIVE_METADATA_TYPE)) {
      metadataType = parameters.get(RodaConstants.PLUGIN_PARAMS_DESCRIPTIVE_METADATA_TYPE);
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return PluginHelper.processObjects(this, new RODAObjectProcessingLogic<AIP>() {
      @Override
      public void process(IndexService index, ModelService model, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<AIP> plugin, AIP object) {
        processAIP(index, model, report, jobPluginInfo, cachedJob, object);
      }
    }, index, model, liteList);
  }

  private void processAIP(IndexService index, ModelService model, Report pluginReport, JobPluginInfo jobPluginInfo,
    Job cachedJob, AIP aip) {

    Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.INGEST_PROCESSING);
    PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
    PluginState state = PluginState.SUCCESS;

    try {
      LOGGER.debug("Validating AIP {}", aip.getId());
      Pair<ValidationReport, List<Pair<String, String>>> reportAndSchemaInfo = ValidationUtils.isAIPMetadataValid(
        forceDescriptiveMetadataType, validateDescriptiveMetadata, metadataType, metadataVersion, model, aip,
        cachedJob.getUsername());
      schemasInfo = reportAndSchemaInfo.getSecond();

      if (reportAndSchemaInfo.getFirst().isValid()) {
        reportItem.setPluginState(state);
      } else {
        state = PluginState.FAILURE;
        reportItem.setPluginState(state).setHtmlPluginDetails(true)
          .setPluginDetails(reportAndSchemaInfo.getFirst().toHtml(false, false));
      }
    } catch (RODAException mse) {
      state = PluginState.FAILURE;
      LOGGER.error("Error processing AIP {}: {}", aip.getId(), mse.getMessage(), mse);
    }

    try {
      boolean notify = true;
      createEvent(aip, model, index, reportItem.getPluginState(), notify, cachedJob);
      jobPluginInfo.incrementObjectsProcessed(state);

      pluginReport.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
    } catch (PluginException | RuntimeException e) {
      LOGGER.error("Error updating job report", e);
    }
  }

  private void createEvent(AIP aip, ModelService model, IndexService index, PluginState state, boolean notify,
    Job cachedJob) throws PluginException {
    try {
      PluginHelper.createPluginEvent(this, aip.getId(), model, index, state, "", notify, cachedJob);
    } catch (RODAException e) {
      throw new PluginException(e.getMessage(), e);
    }
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new DescriptiveMetadataValidationPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.WELLFORMEDNESS_CHECK;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Checked whether the descriptive metadata is included in the SIP and if this metadata is valid according to the established policy.";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return addSchemaToBuilder("Descriptive metadata is well formed and complete.");
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return addSchemaToBuilder(
      "Descriptive metadata was not well formed or failed to meet the established ingest policy.");
  }

  private String addSchemaToBuilder(String eventMessage) {
    if (!schemasInfo.isEmpty()) {
      StringBuilder builder = new StringBuilder(eventMessage);
      builder.append("\nSchemas used on validation: ");

      Pair<String, String> firstSchema = schemasInfo.get(0);
      builder.append(firstSchema.getFirst());

      if (StringUtils.isNotBlank(firstSchema.getSecond())) {
        builder.append(" (").append(schemasInfo.get(0).getSecond()).append(")");
      }

      for (int i = 1; i < schemasInfo.size(); i++) {
        Pair<String, String> schema = schemasInfo.get(i);
        builder.append(", ").append(schema.getFirst());

        if (StringUtils.isNotBlank(schema.getSecond())) {
          builder.append(" (").append(schema.getSecond()).append(")");
        }
      }

      return builder.toString();
    }

    return eventMessage;
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_VALIDATION, RodaConstants.PLUGIN_CATEGORY_CHARACTERIZATION);
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Arrays.asList(AIP.class);
  }
}
