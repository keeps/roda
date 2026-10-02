/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.maintenance;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChangeTypePlugin<T extends IsRODAObject> extends AbstractPlugin<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ChangeTypePlugin.class);
  private static final String EVENT_DESCRIPTION = "The process of updating an object of the repository";

  private String newType = null;
  private String details = null;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_NEW_TYPE,
      PluginParameter.getBuilder(RodaConstants.PLUGIN_PARAMS_NEW_TYPE, "plugin.changeTypePlugin.parameter.newType.name", PluginParameterType.STRING)
        .isMandatory(false).withDescription("plugin.changeTypePlugin.parameter.newType.description").build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS,
      PluginParameter.getBuilder(RodaConstants.PLUGIN_PARAMS_DETAILS, "plugin.generic.parameter.eventDetails.name", PluginParameterType.STRING)
        .isMandatory(false).withDescription("plugin.generic.parameter.eventDetails.description").build());
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
    return "plugin.changeTypePlugin.name";
  }

  @Override
  public String getDescription() {
    return "plugin.changeTypePlugin.description";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_NEW_TYPE));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DETAILS));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_NEW_TYPE)) {
      newType = parameters.get(RodaConstants.PLUGIN_PARAMS_NEW_TYPE);
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DETAILS)) {
      details = parameters.get(RodaConstants.PLUGIN_PARAMS_DETAILS);
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model,
    List<LiteOptionalWithCause> liteList) throws PluginException {

    return PluginHelper.processObjects(this,
            (RODAObjectsProcessingLogic<T>) (index1, model1, report, cachedJob, jobPluginInfo, plugin, objects) -> {
        if (objects.get(0) instanceof AIP) {
          processAIP(model1, report, jobPluginInfo, cachedJob, (List<AIP>) objects);
        } else if (objects.get(0) instanceof Representation) {
          processRepresentation(model1, report, jobPluginInfo, cachedJob, (List<Representation>) objects);
        }
            }, index, model, liteList);
  }

  private void processAIP(ModelService model, Report report, JobPluginInfo jobPluginInfo, Job job, List<AIP> aips) {
    for (AIP aip : aips) {
      LOGGER.debug("Processing AIP {}", aip.getId());
      Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class);
      PluginHelper.updatePartialJobReport(this, model, reportItem, false, job);
      PluginState state = PluginState.SUCCESS;

      try {
        model.changeAIPType(aip.getId(), newType, job.getUsername());
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
        state = PluginState.FAILURE;
        LOGGER.error("Error processing AIP {}: {}", aip.getId(), e.getMessage(), e);
      } finally {
        jobPluginInfo.incrementObjectsProcessed(state);
        reportItem.setPluginState(state);

        String outcomeText = PluginHelper.getPreservationEventText("plugin.changeTypePlugin.event.aipTypeChanged",
          "The AIP '{0}' changed its type from '{1}' to '{2}'.", aip.getId(), aip.getType(), newType);

        model.createUpdateAIPEvent(aip.getId(), null, null, null,
          PluginHelper.getPreservationEventType(ChangeTypePlugin.class, PreservationEventType.UPDATE),
          PluginHelper.getPreservationEventText("preservationEvent.common.updateObject", EVENT_DESCRIPTION), state,
          outcomeText, details, job.getUsername(), true, null);
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
      }
    }
  }

  private void processRepresentation(ModelService model, Report report, JobPluginInfo jobPluginInfo, Job job,
    List<Representation> representations) {
    for (Representation representation : representations) {
      LOGGER.debug("Processing representation {}", representation.getId());
      Report reportItem = PluginHelper.initPluginReportItem(this, representation.getId(), Representation.class);
      PluginHelper.updatePartialJobReport(this, model, reportItem, false, job);
      PluginState state = PluginState.SUCCESS;

      try {
        model.changeRepresentationType(representation.getAipId(), representation.getId(), newType, job.getUsername());
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
        state = PluginState.FAILURE;
        LOGGER.error("Error processing Representation {}: {}", representation.getId(), e.getMessage(), e);
      } finally {
        jobPluginInfo.incrementObjectsProcessed(state);
        reportItem.setPluginState(state);

        String outcomeText = PluginHelper.getPreservationEventText(
          "plugin.changeTypePlugin.event.representationTypeChanged",
          "The representation '{0} of AIP {1}' changed its type from '{2}' to '{3}'.", representation.getId(),
          representation.getAipId(), representation.getType(), newType);

        model.createUpdateAIPEvent(representation.getAipId(), representation.getId(), null, null,
          PluginHelper.getPreservationEventType(ChangeTypePlugin.class, PreservationEventType.UPDATE),
          PluginHelper.getPreservationEventText("preservationEvent.common.updateObject", EVENT_DESCRIPTION), state,
          outcomeText, details, job.getUsername(), true, null);
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
      }
    }
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model)
    throws PluginException {
    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model) throws PluginException {
    return new Report();
  }

  @Override
  public Plugin<T> cloneMe() {
    return new ChangeTypePlugin<>();
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
    return PluginHelper.getPreservationEventText("plugin.changeTypePlugin.event.description", "Updates object type");
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return PluginHelper.getPreservationEventText("plugin.changeTypePlugin.event.success",
      "The object type were successfully updated");
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return PluginHelper.getPreservationEventText("plugin.changeTypePlugin.event.failure",
      "The object type were not successfully updated");
  }

  @Override
  public List<String> getCategories() {
    return Collections.singletonList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public List<Class<T>> getObjectClasses() {
    List<Class<? extends IsRODAObject>> list = new ArrayList<>();
    list.add(AIP.class);
    list.add(Representation.class);
    return (List) list;
  }
}
