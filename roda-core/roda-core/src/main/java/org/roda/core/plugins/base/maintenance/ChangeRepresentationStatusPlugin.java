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
import org.roda.core.data.v2.LiteOptionalWithCause;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChangeRepresentationStatusPlugin extends AbstractPlugin<Representation> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ChangeRepresentationStatusPlugin.class);
  private static final String EVENT_DESCRIPTION = "The process of updating an object of the repository";
  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_NEW_STATUS,
      PluginParameter.getBuilder(RodaConstants.PLUGIN_PARAMS_NEW_STATUS, "New status", PluginParameterType.STRING)
        .isMandatory(false).withDescription("New type").build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS,
      PluginParameter.getBuilder(RodaConstants.PLUGIN_PARAMS_DETAILS, "Event details", PluginParameterType.STRING)
        .isMandatory(false).withDescription("Details that will be used when creating event").build());
  }

  private List<String> newStatus = new ArrayList<>();
  private String details = null;

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
    return "Change representation status";
  }

  @Override
  public String getDescription() {
    return "Changes Representation status value";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_NEW_STATUS));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DETAILS));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_NEW_STATUS)) {
      String newStatusNotTreated = parameters.get(RodaConstants.PLUGIN_PARAMS_NEW_STATUS);
      if (newStatusNotTreated != null && !newStatusNotTreated.trim().isEmpty()) {
        newStatus = new ArrayList<>();
        newStatus.addAll(Arrays.asList(newStatusNotTreated.split(",")));
      }
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DETAILS)) {
      details = parameters.get(RodaConstants.PLUGIN_PARAMS_DETAILS);
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model,
    List<LiteOptionalWithCause> liteList) throws PluginException {

    return PluginHelper.processObjects(this, (RODAObjectsProcessingLogic<Representation>) (index1, model1,
      report, cachedJob, jobPluginInfo, plugin, objects) -> {
      processRepresentation(model1, report, jobPluginInfo, cachedJob, objects);
    }, index, model, liteList);
  }

  private void processRepresentation(ModelService model, Report report, JobPluginInfo jobPluginInfo, Job job,
    List<Representation> representations) {
    for (Representation representation : representations) {
      LOGGER.debug("Processing representation {}", representation.getId());
      Report reportItem = PluginHelper.initPluginReportItem(this, representation.getId(), Representation.class);
      PluginHelper.updatePartialJobReport(this, model, reportItem, false, job);
      PluginState state = PluginState.SUCCESS;

      try {
        model.changeRepresentationStates(representation.getAipId(), representation.getId(), newStatus,
          job.getUsername());
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
        state = PluginState.FAILURE;
        LOGGER.error("Error processing Representation {}: {}", representation.getId(), e.getMessage(), e);
      } finally {
        jobPluginInfo.incrementObjectsProcessed(state);
        reportItem.setPluginState(state);

        StringBuilder outcomeText = new StringBuilder().append("The representation '").append(representation.getId())
          .append(" of AIP ").append(representation.getAipId()).append("' changed its status from '")
          .append(representation.getRepresentationStates()).append("' to '").append(newStatus).append("'.");

        model.createUpdateAIPEvent(representation.getAipId(), representation.getId(), null, null,
          PreservationEventType.UPDATE, EVENT_DESCRIPTION, state, outcomeText.toString(), details, job.getUsername(),
          true);
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
  public Plugin<Representation> cloneMe() {
    return new ChangeRepresentationStatusPlugin();
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
    return "Updates object type";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "The object type were successfully updated";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "The object type were not successfully updated";
  }

  @Override
  public List<String> getCategories() {
    return Collections.singletonList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public List<Class<Representation>> getObjectClasses() {
    return Arrays.asList(Representation.class);
  }
}
