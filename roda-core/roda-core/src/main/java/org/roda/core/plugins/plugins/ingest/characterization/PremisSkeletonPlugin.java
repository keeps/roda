/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.characterization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlException;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.Attribute;
import org.roda.core.data.v2.jobs.JobReport.PluginState;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.ReportItem;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PremisSkeletonPlugin implements Plugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(PremisSkeletonPlugin.class);

  private Map<String, String> parameters;
  private boolean createsPluginEvent = true;

  @Override
  public void init() throws PluginException {
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "PREMIS basic information";
  }

  @Override
  public String getDescription() {
    return "Create basic PREMIS information";
  }

  @Override
  public String getAgentType(){
    return RodaConstants.PRESERVATION_AGENT_TYPE_SOFTWARE;
  }
  
  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    return new ArrayList<>();
  }

  @Override
  public Map<String, String> getParameterValues() {
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    this.parameters = parameters;

    // updates the flag responsible to allow plugin event creation
    if (parameters.containsKey("createsPluginEvent")) {
      createsPluginEvent = Boolean.parseBoolean(parameters.get("createsPluginEvent"));
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {
    Report report = PluginHelper.createPluginReport(this);
    PluginState state;

    try {
      for (AIP aip : list) {
        LOGGER.debug("Processing AIP " + aip.getId());
        ReportItem reportItem = PluginHelper.createPluginReportItem(this, "Creating base PREMIS", aip.getId(), null);

        try {
          for (Representation representation : aip.getRepresentations()) {

            LOGGER.debug("createPremisForRepresentation  " + representation.getId());
            LOGGER.debug("Processing representation " + representation.getId() + " from AIP " + aip.getId());
            boolean inotify = false;
            PremisSkeletonPluginUtils.createPremisSkeletonOnRepresentation(model, storage, aip, representation.getId(),
              inotify);
          }
          model.notifyAIPUpdated(aip.getId());

          state = PluginState.SUCCESS;
          reportItem = PluginHelper.setPluginReportItemInfo(reportItem, aip.getId(), new Attribute(
            RodaConstants.REPORT_ATTR_OUTCOME, state.toString()));

        } catch (RODAException | XmlException e) {
          LOGGER.error("Error processing AIP " + aip.getId(), e);

          state = PluginState.FAILURE;
          reportItem = PluginHelper.setPluginReportItemInfo(reportItem, aip.getId(), new Attribute(
            RodaConstants.REPORT_ATTR_OUTCOME, state.toString()), new Attribute(
            RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, e.getMessage()));
        }

        report.addItem(reportItem);

        if (createsPluginEvent) {
          PluginHelper.updateJobReport(model, index, this, reportItem, state, PluginHelper.getJobId(parameters),
            aip.getId());
        }
      }
    } catch (IOException ioe) {
      LOGGER.error("Error executing FastCharacterizationAction: " + ioe.getMessage(), ioe);
    }
    return report;
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
    return new PremisSkeletonPlugin();
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
