/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

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
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RiskIncidencePlugin<T extends Serializable> extends AbstractPlugin<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(RiskIncidencePlugin.class);
  private static String riskId = null;

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

    if (parameters.containsKey("riskId")) {
      riskId = parameters.get("riskId");
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<T> list)
    throws PluginException {
    if (!list.isEmpty()) {
      if (list.get(0) instanceof AIP) {
        return executeOnAIP(index, model, storage, (List<AIP>) list);
      } else if (list.get(0) instanceof Representation) {
        return executeOnRepresentation(index, model, storage, (List<Representation>) list);
      } else if (list.get(0) instanceof File) {
        return executeOnFile(index, model, storage, (List<File>) list);
      }
    }

    return new Report();
  }

  public Report executeOnAIP(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {
    LOGGER.debug("Creating risk incidences");
    Report pluginReport = PluginHelper.createPluginReport(this);

    for (AIP aip : list) {
      try {
        model.addRiskIncidence(riskId, aip.getId(), null, null, null, "RiskIncidence");
      } catch (GenericException e) {
        pluginReport.setPluginState(PluginState.FAILURE).setPluginDetails("Risk incidence was not added");
      }
    }

    LOGGER.debug("Done creating risk incidences");
    return pluginReport;
  }

  public Report executeOnRepresentation(IndexService index, ModelService model, StorageService storage,
    List<Representation> list) throws PluginException {
    LOGGER.debug("Creating risk incidences");
    Report pluginReport = PluginHelper.createPluginReport(this);

    for (Representation representation : list) {
      try {
        model.addRiskIncidence(riskId, representation.getAipId(), representation.getId(), null, null, "RiskIncidence");
      } catch (GenericException e) {
        pluginReport.setPluginState(PluginState.FAILURE).setPluginDetails("Risk incidence was not added");
      }
    }

    LOGGER.debug("Done creating risk incidences");
    return pluginReport;
  }

  public Report executeOnFile(IndexService index, ModelService model, StorageService storage, List<File> list)
    throws PluginException {
    LOGGER.debug("Creating risk incidences");
    Report pluginReport = PluginHelper.createPluginReport(this);

    for (File file : list) {
      try {
        model.addRiskIncidence(riskId, file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(),
          "RiskIncidence");
      } catch (GenericException e) {
        pluginReport.setPluginState(PluginState.FAILURE).setPluginDetails("Risk incidence was not added");
      }
    }

    LOGGER.debug("Done creating risk incidences");
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
