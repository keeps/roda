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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeTypePlugin<T extends IsRODAObject> extends AbstractPlugin<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ChangeTypePlugin.class);
  private static final String EVENT_DESCRIPTION = "The process of updating an object of the repository";

  private String newType = null;
  private String details = null;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_NEW_TYPE, new PluginParameter(RodaConstants.PLUGIN_PARAMS_NEW_TYPE,
      "New type", PluginParameterType.STRING, "", false, false, "New type"));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, new PluginParameter(RodaConstants.PLUGIN_PARAMS_DETAILS,
      "Event details", PluginParameterType.STRING, "", false, false, "Details that will be used when creating event"));
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
    return "RODA entity type change";
  }

  @Override
  public String getDescription() {
    return "Changes AIP or representation type value";
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
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {

    return PluginHelper.processObjects(this, new RODAObjectsProcessingLogic<T>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<T> plugin, List<T> objects) {
        if (objects.get(0) instanceof AIP) {
          processAIP(model, report, jobPluginInfo, cachedJob, (List<AIP>) objects);
        } else if (objects.get(0) instanceof Representation) {
          processRepresentation(model, report, jobPluginInfo, cachedJob, (List<Representation>) objects);
        }
      }
    }, index, model, storage, liteList);
  }

  private void processAIP(ModelService model, Report report, JobPluginInfo jobPluginInfo, Job job, List<AIP> aips) {
    for (AIP aip : aips) {
      Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class);
      PluginHelper.updatePartialJobReport(this, model, reportItem, false, job);
      PluginState state = PluginState.SUCCESS;

      try {
        model.changeAIPType(aip.getId(), newType, job.getUsername());
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
        state = PluginState.FAILURE;
      } finally {
        jobPluginInfo.incrementObjectsProcessed(state);
        reportItem.setPluginState(state);

        StringBuilder outcomeText = new StringBuilder().append("The AIP '").append(aip.getId())
          .append("' changed its type from '").append(aip.getType()).append("' to '").append(newType).append("'.");

        model.createUpdateAIPEvent(aip.getId(), null, null, null, PreservationEventType.UPDATE, EVENT_DESCRIPTION,
          state, outcomeText.toString(), details, job.getUsername(), true);
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
      }
    }
  }

  private void processRepresentation(ModelService model, Report report, JobPluginInfo jobPluginInfo, Job job,
    List<Representation> representations) {
    for (Representation representation : representations) {
      Report reportItem = PluginHelper.initPluginReportItem(this, representation.getId(), Representation.class);
      PluginHelper.updatePartialJobReport(this, model, reportItem, false, job);
      PluginState state = PluginState.SUCCESS;

      try {
        model.changeRepresentationType(representation.getAipId(), representation.getId(), newType, job.getUsername());
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
        state = PluginState.FAILURE;
      } finally {
        jobPluginInfo.incrementObjectsProcessed(state);
        reportItem.setPluginState(state);

        StringBuilder outcomeText = new StringBuilder().append("The representation '").append(representation.getId())
          .append(" of AIP ").append(representation.getAipId()).append("' changed its type from '")
          .append(representation.getType()).append("' to '").append(newType).append("'.");

        model.createUpdateAIPEvent(representation.getAipId(), representation.getId(), null, null,
          PreservationEventType.UPDATE, EVENT_DESCRIPTION, state, outcomeText.toString(), details, job.getUsername(),
          true);
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
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public List<Class<T>> getObjectClasses() {
    List<Class<? extends IsRODAObject>> list = new ArrayList<>();
    list.add(AIP.class);
    list.add(Representation.class);
    return (List) list;
  }
}
