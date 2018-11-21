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
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;

public class UpdatePermissionsPlugin<T extends IsRODAObject> extends AbstractPlugin<T> {
  private Permissions permissions;
  private String details = null;
  private String eventDescription = null;
  private boolean recursive = true;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_PERMISSIONS_JSON,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_PERMISSIONS_JSON, "Permission object in JSON",
        PluginParameterType.STRING, "", false, false, "Permission object in JSON."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, new PluginParameter(RodaConstants.PLUGIN_PARAMS_DETAILS,
      "Event details", PluginParameterType.STRING, "", false, false, "Details that will be used when creating event"));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_EVENT_DESCRIPTION,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_EVENT_DESCRIPTION, "Event description",
        PluginParameterType.STRING, "", false, false, "Description that will be used when creating event"));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_RECURSIVE,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_RECURSIVE, "Recursive mode", PluginParameterType.BOOLEAN, "true",
        true, false, "Execute in recursive mode."));
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
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_PERMISSIONS_JSON));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_EVENT_DESCRIPTION));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DETAILS));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_RECURSIVE));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    try {
      permissions = JsonUtils.getObjectFromJson(parameters.get(RodaConstants.PLUGIN_PARAMS_PERMISSIONS_JSON),
        Permissions.class);
    } catch (GenericException e) {
      throw new InvalidParameterException(e);
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DETAILS)) {
      details = parameters.get(RodaConstants.PLUGIN_PARAMS_DETAILS);
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_EVENT_DESCRIPTION)) {
      eventDescription = parameters.get(RodaConstants.PLUGIN_PARAMS_EVENT_DESCRIPTION);
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_RECURSIVE)) {
      recursive = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_RECURSIVE));
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {

    return PluginHelper.processObjects(this, new RODAObjectProcessingLogic<T>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<T> plugin, T object) {
        if (object instanceof AIP) {
          processAIP(model, index, report, jobPluginInfo, cachedJob, (AIP) object);
        } else if (object instanceof DIP) {
          processDIP(model, report, jobPluginInfo, cachedJob, (DIP) object);
        }
      }
    }, index, model, storage, liteList);
  }

  private void processAIP(ModelService model, IndexService index, Report report, JobPluginInfo jobPluginInfo, Job job,
    AIP aip) {
    PluginState state = PluginState.SUCCESS;
    Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.ACTIVE);
    String outcome = "";

    try {
      aip.setPermissions(permissions);
      model.updateAIPPermissions(aip, job.getUsername());

      if (recursive) {
        Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, aip.getId()));
        IterableIndexResult<IndexedAIP> childs = index.findAll(IndexedAIP.class, filter,
          Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_ID));

        for (IndexedAIP child : childs) {
          model.updateAIPPermissions(child.getId(), permissions, job.getUsername());
        }
      }

      outcome = "AIP " + aip.getId() + " permissions were updated and all sublevels too";
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      state = PluginState.FAILURE;
      reportItem.addPluginDetails("Could not update AIP permissions: " + e.getMessage());
      outcome = "AIP " + aip.getId() + " or some of its child permissions were not successfully updated: "
        + e.getMessage();
    } finally {
      reportItem.setPluginState(state);
      report.addReport(reportItem);
      jobPluginInfo.incrementObjectsProcessed(state);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);

      List<LinkingIdentifier> sources = Arrays
        .asList(PluginHelper.getLinkingIdentifier(aip.getId(), RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME));
      model.createEvent(aip.getId(), null, null, null, PreservationEventType.UPDATE, eventDescription, sources, null,
        state, outcome, details, job.getUsername(), true);
    }
  }

  private void processDIP(ModelService model, Report report, JobPluginInfo jobPluginInfo, Job job, DIP dip) {
    PluginState state = PluginState.SUCCESS;
    Report reportItem = PluginHelper.initPluginReportItem(this, dip.getId(), DIP.class);

    try {
      dip.setPermissions(permissions);
      model.updateDIPPermissions(dip);
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      state = PluginState.FAILURE;
      reportItem.addPluginDetails("Could not update DIP permissions: " + e.getMessage());
    } finally {
      // TODO 20170222 nvieira it should create an event associated with DIP
      reportItem.setPluginState(state);
      report.addReport(reportItem);
      jobPluginInfo.incrementObjectsProcessed(state);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
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
    return new UpdatePermissionsPlugin();
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
    return "Updates AIP or DIP permissions recursively";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "AIP or DIP permissions update was successful";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "AIP or DIP permissions update failed";
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public List<Class<T>> getObjectClasses() {
    List<Class<? extends IsRODAObject>> list = new ArrayList<>();
    list.add(AIP.class);
    list.add(DIP.class);
    return (List) list;
  }
}
