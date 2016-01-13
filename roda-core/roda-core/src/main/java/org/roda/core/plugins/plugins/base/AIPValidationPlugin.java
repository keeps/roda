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
import java.util.Map;

import org.roda.core.common.ValidationUtils;
import org.roda.core.data.Attribute;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.core.data.ReportItem;
import org.roda.core.data.common.InvalidParameterException;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.EventPreservationObject;
import org.roda.core.data.v2.JobReport.PluginState;
import org.roda.core.data.v2.PluginType;
import org.roda.core.index.IndexService;
import org.roda.core.metadata.v2.premis.PremisMetadataException;
import org.roda.core.model.AIP;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FIXME rename this to SIPValidationPlugin
public class AIPValidationPlugin implements Plugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AIPValidationPlugin.class);

  private Map<String, String> parameters;

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
    return new ArrayList<PluginParameter>();
  }

  @Override
  public Map<String, String> getParameterValues() {
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    this.parameters = parameters;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {
    List<String> validAIP = new ArrayList<String>();
    List<String> invalidAIP = new ArrayList<String>();

    Report report = PluginHelper.createPluginReport(this);
    PluginState state;

    for (AIP aip : list) {
      ReportItem reportItem = PluginHelper.createPluginReportItem(this, "SIP syntax check", aip.getId(), null);

      try {
        LOGGER.debug("Validating AIP {}", aip.getId());
        boolean descriptiveValid = ValidationUtils.isAIPDescriptiveMetadataValid(model, aip.getId(), true);
        boolean preservationValid = ValidationUtils.isAIPPreservationMetadataValid(model, aip.getId(), true);
        if (descriptiveValid && preservationValid) {
          validAIP.add(aip.getId());
          LOGGER.debug("Done with validating AIP {}: valid!", aip.getId());

          state = PluginState.SUCCESS;
          reportItem.addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()));
        } else {
          invalidAIP.add(aip.getId());
          LOGGER.debug("Done with validating AIP {}: invalid!", aip.getId());

          state = PluginState.FAILURE;
          reportItem.addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()));
        }

        createEvent(aip, model, descriptiveValid, preservationValid);

      } catch (RODAException e) {
        LOGGER.error("Error processing AIP " + aip.getId(), e);
        state = PluginState.FAILURE;
        reportItem = PluginHelper.setPluginReportItemInfo(reportItem, aip.getId(),
          new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()),
          new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, e.getMessage()));
      }

      report.addItem(reportItem);

      PluginHelper.updateJobReport(model, index, this, reportItem, state, PluginHelper.getJobId(parameters),
        aip.getId());
    }
    return report;
  }

  // TODO EVENT MUST BE "AIP EVENT" INSTEAD OF "REPRESENTATION EVENT"
  // TODO AGENT ID...
  private void createEvent(AIP aip, ModelService model, boolean descriptiveValid, boolean preservationValid)
    throws PluginException {
    try {
      boolean success = descriptiveValid && preservationValid;

      for (String representationID : aip.getRepresentationIds()) {
        PluginHelper.createPluginEvent(aip.getId(), representationID, model,
          EventPreservationObject.PRESERVATION_EVENT_TYPE_FORMAT_VALIDATION, "The AIP format was validated.",
          EventPreservationObject.PRESERVATION_EVENT_AGENT_ROLE_INGEST_TASK, "AGENT ID",
          Arrays.asList(representationID), success ? PluginState.SUCCESS : PluginState.FAILURE, "Report", "");
      }
    } catch (PremisMetadataException | IOException | RODAException e) {
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
