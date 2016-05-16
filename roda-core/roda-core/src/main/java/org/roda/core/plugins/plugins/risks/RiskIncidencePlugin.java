/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.risks;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.JobException;
import org.roda.core.plugins.orchestrate.RiskJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RiskIncidencePlugin<T extends Serializable> extends AbstractPlugin<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(RiskIncidencePlugin.class);
  private static String riskIds = null;
  private static String OTHER_METADATA_TYPE = "RiskIncidence";

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
    return "Create risk incidences";
  }

  @Override
  public String getDescription() {
    return "Associate risk with AIPs, representations and files";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_RISK_ID)) {
      riskIds = parameters.get(RodaConstants.PLUGIN_PARAMS_RISK_ID);
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<T> list)
    throws PluginException {
    try {

      LOGGER.debug("Creating risk incidences");
      Report pluginReport = PluginHelper.initPluginReport(this);

      RiskJobPluginInfo jobPluginInfo = new RiskJobPluginInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      if (!list.isEmpty()) {
        if (riskIds != null) {
          String[] risks = riskIds.split(",");
          for (String riskId : risks) {
            if (list.get(0) instanceof AIP) {
              pluginReport = executeOnAIP(index, model, riskId, (List<AIP>) list, pluginReport);
            } else if (list.get(0) instanceof Representation) {
              pluginReport = executeOnRepresentation(model, riskId, (List<Representation>) list, pluginReport);
            } else if (list.get(0) instanceof File) {
              pluginReport = executeOnFile(model, riskId, (List<File>) list, pluginReport);
            }

            jobPluginInfo.putRisk(riskId, 1);
          }
        }
      }

      PluginHelper.updateJobInformation(this, jobPluginInfo);
      LOGGER.debug("Done creating risk incidences");
      return pluginReport;
    } catch (JobException e) {
      throw new PluginException("A job exception has occurred", e);
    }
  }

  public Report executeOnAIP(IndexService index, ModelService model, String riskId, List<AIP> list, Report pluginReport)
    throws PluginException {

    try {
      for (AIP aip : list) {
        model.addRiskIncidence(riskId, aip.getId(), null, null, null, OTHER_METADATA_TYPE);
      }
    } catch (GenericException e) {
      pluginReport.setPluginState(PluginState.FAILURE).setPluginDetails("Risk incidence was not added");
    }

    return pluginReport;
  }

  public Report executeOnRepresentation(ModelService model, String riskId, List<Representation> list,
    Report pluginReport) throws PluginException {

    try {
      for (Representation representation : list) {
        model.addRiskIncidence(riskId, representation.getAipId(), representation.getId(), null, null,
          OTHER_METADATA_TYPE);
      }
    } catch (GenericException e) {
      pluginReport.setPluginState(PluginState.FAILURE).setPluginDetails("Risk incidence was not added");
    }

    return pluginReport;
  }

  public Report executeOnFile(ModelService model, String riskId, List<File> list, Report pluginReport)
    throws PluginException {

    try {
      for (File file : list) {
        model.addRiskIncidence(riskId, file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(),
          OTHER_METADATA_TYPE);
      }
    } catch (GenericException e) {
      pluginReport.setPluginState(PluginState.FAILURE).setPluginDetails("Risk incidence was not added");
    }

    return pluginReport;
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report beforeBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterBlockExecute(IndexService index, ModelService model, StorageService storage)
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
  public Plugin<T> cloneMe() {
    return new RiskIncidencePlugin<T>();
  }

  @Override
  public PluginType getType() {
    return PluginType.MISC;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  // TODO FIX
  @Override
  public PreservationEventType getPreservationEventType() {
    return null;
  }

  @Override
  public String getPreservationEventDescription() {
    return "XXXXXXXXXX";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXX";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXXXX";
  }
}
