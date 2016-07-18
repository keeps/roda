/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.common.validation.ValidationUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.validation.ValidationReport;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DescriptiveMetadataValidationPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DescriptiveMetadataValidationPlugin.class);

  public static final PluginParameter PARAMETER_VALIDATE_DESCRIPTIVE_METADATA = new PluginParameter(
    "parameter.validate_descriptive_metadata", "Validate descriptive metadata", PluginParameterType.BOOLEAN, "true",
    true, false, "If true, the action will check if the the descriptive metadata is valid according to the schemas installed in the repository.");

  public static final PluginParameter PARAMETER_METADATA_TYPE = new PluginParameter("parameter.metadata_type",
    "Descriptive metadata type", PluginParameterType.METADATA_TYPE, null, false, false,
    "Descriptive metadata type to be used as fallback or if metadata type is forced.");

  public static final PluginParameter PARAMETER_METADATA_VERSION = new PluginParameter("parameter.metadata_version",
    "Descriptive metadata version", PluginParameterType.STRING, null, false, false,
    "Descriptive metadata version to be used as fallback or if metadata type is forced.");

  public static final PluginParameter PARAMETER_FORCE_DESCRIPTIVE_METADATA_TYPE = new PluginParameter(
    "parameter.force_type", "Force metadata type in all", PluginParameterType.BOOLEAN, "false", true, false,
    "If true, bypass current metadata type with metadata type passed as parameter. If false, if metadata type passed as parameter is defined use as fallback, else no fallback");

  public static final PluginParameter PARAMETER_VALIDATE_PREMIS = new PluginParameter("parameter.validate_premis",
    "Validate Premis", PluginParameterType.BOOLEAN, "true", true, false, "Validate Premis");

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  public static String getStaticName() {
    return "Descriptive metadata validation";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "Checks if the descriptive metadata is included in the SIP and if it is valid according to the schemas installed in the repository.";
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
    ArrayList<PluginParameter> pluginParameters = new ArrayList<PluginParameter>();
    pluginParameters.add(PARAMETER_VALIDATE_DESCRIPTIVE_METADATA);
    pluginParameters.add(PARAMETER_METADATA_TYPE);
    pluginParameters.add(PARAMETER_METADATA_VERSION);
    pluginParameters.add(PARAMETER_FORCE_DESCRIPTIVE_METADATA_TYPE);
    pluginParameters.add(PARAMETER_VALIDATE_PREMIS);
    return pluginParameters;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {

    boolean validateDescriptiveMetadata = PluginHelper.getBooleanFromParameters(this,
      PARAMETER_VALIDATE_DESCRIPTIVE_METADATA);
    boolean validatePremis = PluginHelper.getBooleanFromParameters(this, PARAMETER_VALIDATE_PREMIS);
    boolean forceDescriptiveMetadataType = PluginHelper.getBooleanFromParameters(this,
      PARAMETER_FORCE_DESCRIPTIVE_METADATA_TYPE);
    String metadataType = PluginHelper.getStringFromParameters(this, PARAMETER_METADATA_TYPE);
    String metadataVersion = PluginHelper.getStringFromParameters(this, PARAMETER_METADATA_VERSION);

    Report pluginReport = PluginHelper.initPluginReport(this);
    List<ValidationReport> reports = new ArrayList<ValidationReport>();

    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, list.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      try {
        for (AIP aip : list) {
          Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class,
            AIPState.INGEST_PROCESSING);
          PluginHelper.updatePartialJobReport(this, model, index, reportItem, false);
          PluginState state = PluginState.SUCCESS;

          try {
            LOGGER.debug("Validating AIP {}", aip.getId());
            ValidationReport report = ValidationUtils.isAIPMetadataValid(forceDescriptiveMetadataType,
              validateDescriptiveMetadata, metadataType, metadataVersion, validatePremis, model, aip.getId());
            reports.add(report);
            if (report.isValid()) {
              reportItem.setPluginState(state);
            } else {
              state = PluginState.FAILURE;
              reportItem.setPluginState(state).setHtmlPluginDetails(true).setPluginDetails(report.toHtml(false, false));
            }
          } catch (RODAException mse) {
            state = PluginState.FAILURE;
            LOGGER.error("Error processing AIP " + aip.getId() + ": " + mse.getMessage(), mse);
          }

          try {
            boolean notify = true;
            createEvent(aip, model, index, reportItem.getPluginState(), notify);
            jobPluginInfo.incrementObjectsProcessed(state);

            pluginReport.addReport(reportItem);
            PluginHelper.updatePartialJobReport(this, model, index, reportItem, true);
          } catch (Throwable e) {
            LOGGER.error("Error updating job report", e);
          }
        }
      } catch (ClassCastException e) {
        LOGGER.error("Trying to execute an AIP-only plugin with other objects");
        jobPluginInfo.incrementObjectsProcessedWithFailure(list.size());
      }

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (JobException e) {
      throw new PluginException("A job exception has occurred", e);
    }

    return pluginReport;
  }

  private void createEvent(AIP aip, ModelService model, IndexService index, PluginState state, boolean notify)
    throws PluginException {
    try {
      PluginHelper.createPluginEvent(this, aip.getId(), model, index, state, "", notify);
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
    return "Descriptive metadata is well formed and complete.";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Descriptive metadata was not well formed or failed to meet the established ingest policy.";
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

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_VALIDATION);
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Arrays.asList(AIP.class);
  }
}
