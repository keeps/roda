/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.characterization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.roda.core.common.PremisUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
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

public class TikaFullTextPlugin implements Plugin<AIP> {

  public static final String FILE_SUFFIX = ".html";

  public static final String OTHER_METADATA_TYPE = "ApacheTika";

  private static final Logger LOGGER = LoggerFactory.getLogger(TikaFullTextPlugin.class);

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
    return "Full-text extraction action";
  }

  @Override
  public String getDescription() {
    return "Extracts the full-text from the representation files";
  }

  @Override
  public String getAgentType() {
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
      boolean notifyAgent = true;
      PremisUtils.createPremisAgentBinary(this, model, notifyAgent);
    } catch (AlreadyExistsException e) {
      // TODO verify agent creation (event)
    } catch (RODAException e) {
      LOGGER.error("Error create PREMIS agent for Apache Tika", e);
    }

    for (AIP aip : list) {
      ReportItem reportItem = PluginHelper.createPluginReportItem(this, "File metadata and full-text extraction",
        aip.getId(), null);

      LOGGER.debug("Processing AIP " + aip.getId());
      try {
        for (Representation representation : aip.getRepresentations()) {
          LOGGER.debug("Processing representation " + representation.getId() + " of AIP " + aip.getId());
          boolean inotify = false;
          TikaFullTextPluginUtils.runTikaFullTextOnRepresentation(index, model, storage, aip, representation, inotify);
        }
        model.notifyAIPUpdated(aip.getId());

        state = PluginState.SUCCESS;
        reportItem.addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()));
      } catch (RODAException e) {
        LOGGER.error("Error processing AIP " + aip.getId() + ": " + e.getMessage(), e);

        state = PluginState.FAILURE;
        reportItem.addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()))
          .addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS,
            "Error running Tika " + aip.getId() + ": " + e.getMessage()));
      }

      report.addItem(reportItem);
      if (createsPluginEvent) {
        PluginHelper.updateJobReport(model, index, this, reportItem, state, PluginHelper.getJobId(parameters),
          aip.getId());
      }
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
    TikaFullTextPlugin tikaPlugin = new TikaFullTextPlugin();
    try {
      tikaPlugin.init();
    } catch (PluginException e) {
      LOGGER.error("Error doing " + TikaFullTextPlugin.class.getName() + "init", e);
    }
    return tikaPlugin;
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
