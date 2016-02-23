/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.common.PremisUtils;
import org.roda.core.common.validation.ValidationUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.IdUtils;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.jobs.JobReport.PluginState;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.ReportItem;
import org.roda.core.data.v2.validation.ValidationReport;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FIXME rename this to SIPValidationPlugin
public class AIPValidationPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AIPValidationPlugin.class);

  public static final PluginParameter PARAMETER_VALIDATE_DESCRIPTIVE_METADATA = new PluginParameter(
    "parameter.validate_descriptive_metadata", "Validate descriptive metadata", PluginParameterType.BOOLEAN, "true",
    true, false, "If true the descriptive metadata is validated against existing schemas.");

  public static final PluginParameter PARAMETER_METADATA_TYPE = new PluginParameter("parameter.metadata_type",
    "Descriptive metadata type", PluginParameterType.METADATA_TYPE, null, false, false,
    "Descriptive metadata type to be used as fallback or if metadata type is forced.");

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

  @Override
  public String getName() {
    return "SIP syntax check";
  }

  @Override
  public String getDescription() {
    return "Check SIP coherence. Verifies the validity and completeness of a SIP.";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> pluginParameters = new ArrayList<PluginParameter>();
    pluginParameters.add(PARAMETER_VALIDATE_DESCRIPTIVE_METADATA);
    pluginParameters.add(PARAMETER_METADATA_TYPE);
    pluginParameters.add(PARAMETER_FORCE_DESCRIPTIVE_METADATA_TYPE);
    pluginParameters.add(PARAMETER_VALIDATE_PREMIS);
    return pluginParameters;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {

    boolean validateDescriptiveMetadata = Boolean.parseBoolean(getParameterValues().getOrDefault(
      PARAMETER_VALIDATE_DESCRIPTIVE_METADATA.getId(), PARAMETER_VALIDATE_DESCRIPTIVE_METADATA.getDefaultValue()));
    boolean validatePremis = Boolean.parseBoolean(getParameterValues().getOrDefault(PARAMETER_VALIDATE_PREMIS.getId(),
      PARAMETER_VALIDATE_PREMIS.getDefaultValue()));
    boolean forceDescriptiveMetadataType = Boolean.parseBoolean(getParameterValues().getOrDefault(
      PARAMETER_FORCE_DESCRIPTIVE_METADATA_TYPE.getId(), PARAMETER_FORCE_DESCRIPTIVE_METADATA_TYPE.getDefaultValue()));
    String metadataType = getParameterValues().getOrDefault(PARAMETER_METADATA_TYPE.getId(),
      PARAMETER_METADATA_TYPE.getDefaultValue());

    List<ValidationReport> reports = new ArrayList<ValidationReport>();
    for (AIP aip : list) {
      ReportItem reportItem = PluginHelper.createPluginReportItem(this, aip.getId(), null);

      try {
        LOGGER.debug("VALIDATING AIP " + aip.getId());
        ValidationReport report = ValidationUtils.isAIPMetadataValid(forceDescriptiveMetadataType,
          validateDescriptiveMetadata, metadataType, validatePremis, model, aip.getId());
        reports.add(report);
        // createEvent(aip, model, descriptiveValid, preservationValid);
      } catch (RODAException mse) {
        LOGGER.error("Error processing AIP " + aip.getId() + ": " + mse.getMessage(), mse);
      }

      try {
        PluginHelper.updateJobReport(model, index, this, reportItem, PluginState.SUCCESS, aip.getId());
      } catch (Throwable t) {

      }
    }
    return null;
  }

  // TODO EVENT MUST BE "AIP EVENT" INSTEAD OF "REPRESENTATION EVENT"
  // TODO AGENT ID...
  private void createEvent(AIP aip, ModelService model, boolean descriptiveValid, boolean preservationValid,
    IndexedPreservationAgent agent, boolean notify) throws PluginException {
    try {
      boolean success = descriptiveValid && preservationValid;

      for (Representation representation : aip.getRepresentations()) {
        boolean inotify = false;
        PluginHelper.createPluginEvent(this,aip.getId(), representation.getId(), null, null, model,
          RodaConstants.PRESERVATION_EVENT_TYPE_FORMAT_VALIDATION, "The AIP format was validated.",
          Arrays.asList(IdUtils.getLinkingIdentifierId(aip.getId(), representation.getId(), null, null)), null,
          success ? "success" : "failure", success ? "success" : "Error", "", inotify);
      }
      if (notify) {
        model.notifyAIPUpdated(aip.getId());
      }
    } catch (RODAException e) {
      throw new PluginException(e.getMessage(), e);
    }

  }

  @Override
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

    return null;
  }

  @Override
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

    return null;
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new AIPValidationPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }
}
