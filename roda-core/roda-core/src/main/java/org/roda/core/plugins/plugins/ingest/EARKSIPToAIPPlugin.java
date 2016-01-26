/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.TransferredResource;
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
import org.roda_project.commons_ip.model.SIP;
import org.roda_project.commons_ip.parse.impl.eark.EARKParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EARKSIPToAIPPlugin implements Plugin<TransferredResource> {
  private static final Logger LOGGER = LoggerFactory.getLogger(EARKSIPToAIPPlugin.class);

  private Map<String, String> parameters;

  @Override
  public void init() throws PluginException {
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "E-ARK";
  }

  @Override
  public String getDescription() {
    return "E-ARK SIP in zip file";
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
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<TransferredResource> list)
    throws PluginException {
    Report report = PluginHelper.createPluginReport(this);
    PluginState state;

    String jobDefinedParentId = PluginHelper.getParentIdFromParameters(parameters);
    boolean jobDefinedForceParentId = PluginHelper.getForceParentIdFromParameters(parameters);

    for (TransferredResource transferredResource : list) {
      Path earkSIPPath = Paths.get(transferredResource.getFullPath());

      ReportItem reportItem = PluginHelper.createPluginReportItem(transferredResource, this);

      try {
        LOGGER.debug("Converting " + earkSIPPath + " to AIP");
        EARKParser migrator = new EARKParser();
        SIP sip = migrator.parse(earkSIPPath);

        String parentId = PluginHelper.getParentId(sip.getParentID(), jobDefinedParentId, jobDefinedForceParentId);

        AIP aipCreated = EARKSIPToAIPPluginUtils.earkSIPToAIP(sip, earkSIPPath, model, storage, parentId);

        state = PluginState.SUCCESS;
        reportItem = PluginHelper.setPluginReportItemInfo(reportItem, aipCreated.getId(),
          new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()));

        if (sip.getParentID() != null && aipCreated.getParentId() == null) {
          LOGGER.error("PARENT NOT FOUND!");
          reportItem = reportItem
            .addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, "Parent not found"));
        }

        LOGGER.debug("Done with converting " + earkSIPPath + " to AIP " + aipCreated.getId());
      } catch (Throwable e) {
        state = PluginState.FAILURE;
        reportItem = PluginHelper.setPluginReportItemInfo(reportItem, null,
          new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()),
          new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, e.getMessage()));

        LOGGER.error("Error converting " + earkSIPPath + " to AIP", e);
      }
      report.addItem(reportItem);
      PluginHelper.createJobReport(model, this, reportItem, state, PluginHelper.getJobId(parameters));
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
  public Plugin<TransferredResource> cloneMe() {
    return new EARKSIPToAIPPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.SIP_TO_AIP;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

}
