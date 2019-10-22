/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.risks.IncidenceStatus;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.risks.SeverityLevel;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;

public class UpdateIncidencesPlugin extends AbstractPlugin<RiskIncidence> {
  private IncidenceStatus status;
  private SeverityLevel severity;
  private Date mitigatedOn;
  private String mitigatedBy;
  private String mitigatedDescription;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_STATUS,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_STATUS, "Risk incidence status",
        PluginParameterType.STRING, "UNMITIGATED", false, false, "Risk incidence status."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_SEVERITY,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_SEVERITY, "Risk incidence severity",
        PluginParameterType.STRING, "MODERATE", false, false, "Risk incidence severity."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_MITIGATED_ON,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_MITIGATED_ON, "Risk incidence mitigated on",
        PluginParameterType.STRING, "", false, false, "Risk incidence mitigated on."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_MITIGATED_BY,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_MITIGATED_BY, "Risk incidence mitigated by",
        PluginParameterType.STRING, "MODERATE", false, false, "Risk incidence mitigated by."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_MITIGATED_DESCRIPTION,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_MITIGATED_DESCRIPTION,
        "Risk incidence mitigated description", PluginParameterType.STRING, "MODERATE", false, false,
        "Risk incidence mitigated description."));
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
    return "Update AIP permissions recursively";
  }

  @Override
  public String getDescription() {
    return "Update AIP permissions recursively copying from parent or using serializable permission object";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_STATUS));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_SEVERITY));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_MITIGATED_ON));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_MITIGATED_BY));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_MITIGATED_DESCRIPTION));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_STATUS)) {
      status = IncidenceStatus.valueOf(parameters.get(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_STATUS));
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_SEVERITY)) {
      severity = SeverityLevel.valueOf(parameters.get(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_SEVERITY));
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_MITIGATED_ON)) {
      try {
        mitigatedOn = JsonUtils
          .getObjectFromJson(parameters.get(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_MITIGATED_ON), Date.class);
      } catch (GenericException e) {
        mitigatedOn = null;
      }
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_MITIGATED_BY)) {
      mitigatedBy = parameters.get(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_MITIGATED_BY);
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_MITIGATED_DESCRIPTION)) {
      mitigatedDescription = parameters.get(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_MITIGATED_DESCRIPTION);
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return PluginHelper.processObjects(this, new RODAObjectsProcessingLogic<RiskIncidence>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<RiskIncidence> plugin, List<RiskIncidence> objects) {
        processRiskIncidence(model, report, jobPluginInfo, cachedJob, objects);
      }
    }, index, model, storage, liteList);
  }

  private void processRiskIncidence(ModelService model, Report report, JobPluginInfo jobPluginInfo, Job job,
    List<RiskIncidence> incidences) {
    for (RiskIncidence incidence : incidences) {
      Report reportItem = PluginHelper.initPluginReportItem(this, incidence.getId(), RiskIncidence.class);
      PluginHelper.updatePartialJobReport(this, model, reportItem, false, job);

      try {
        incidence.setStatus(status);
        incidence.setSeverity(severity);
        incidence.setMitigatedBy(mitigatedBy);
        incidence.setMitigatedDescription(mitigatedDescription);

        if (mitigatedOn != null) {
          incidence.setMitigatedOn(mitigatedOn);
        }

        model.updateRiskIncidence(incidence, false);

        jobPluginInfo.incrementObjectsProcessedWithSuccess();
        reportItem.setPluginState(PluginState.SUCCESS);
      } catch (AuthorizationDeniedException | GenericException e) {
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        reportItem.setPluginState(PluginState.FAILURE)
          .setPluginDetails("Failed to update risk incidence " + incidence.getId());
      } finally {
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
      }
    }
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return new Report();
  }

  @Override
  public Plugin<RiskIncidence> cloneMe() {
    return new UpdateIncidencesPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.INTERNAL;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.UPDATE;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Update multiple incidences";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Multiple incidences update was successful";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Multiple incidences update failed";
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public List<Class<RiskIncidence>> getObjectClasses() {
    return Arrays.asList(RiskIncidence.class);
  }
}
