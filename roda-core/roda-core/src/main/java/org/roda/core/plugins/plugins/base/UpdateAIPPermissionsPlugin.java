/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.Permissions;
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
import org.roda.core.plugins.RODAObjectProcessingLogic;
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;

public class UpdateAIPPermissionsPlugin extends AbstractPlugin<AIP> {
  private Permissions permissions;
  private String permissionJson = null;
  private String aipId = null;
  private String details = null;
  private String outcomeText = null;
  private String eventDescription = null;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_PERMISSIONS_JSON,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_PERMISSIONS_JSON, "Permission object in JSON",
        PluginParameterType.STRING, "", false, false, "Permission object in JSON."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_AIP_ID,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_AIP_ID, "Parent AIP identifier", PluginParameterType.STRING, "",
        false, false, "Parent AIP identifier where to copy permissions"));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, new PluginParameter(RodaConstants.PLUGIN_PARAMS_DETAILS,
      "Event details", PluginParameterType.STRING, "", false, false, "Details that will be used when creating event"));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_OUTCOME_TEXT,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_OUTCOME_TEXT, "Event outcome text", PluginParameterType.STRING,
        "", false, false, "Outcome text that will be used when creating event"));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_EVENT_DESCRIPTION,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_EVENT_DESCRIPTION, "Event description",
        PluginParameterType.STRING, "", false, false, "Description that will be used when creating event"));
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
    ArrayList<PluginParameter> parameters = new ArrayList<PluginParameter>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_PERMISSIONS_JSON));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_AIP_ID));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_EVENT_DESCRIPTION));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DETAILS));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_OUTCOME_TEXT));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_PERMISSIONS_JSON)) {
      permissionJson = parameters.get(RodaConstants.PLUGIN_PARAMS_PERMISSIONS_JSON);
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_AIP_ID)) {
      aipId = parameters.get(RodaConstants.PLUGIN_PARAMS_AIP_ID);
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DETAILS)) {
      details = parameters.get(RodaConstants.PLUGIN_PARAMS_DETAILS);
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_OUTCOME_TEXT)) {
      outcomeText = parameters.get(RodaConstants.PLUGIN_PARAMS_OUTCOME_TEXT);
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_EVENT_DESCRIPTION)) {
      eventDescription = parameters.get(RodaConstants.PLUGIN_PARAMS_EVENT_DESCRIPTION);
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {

    return PluginHelper.processObjects(this, new RODAProcessingLogic<AIP>() {

      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        SimpleJobPluginInfo jobPluginInfo, Plugin<AIP> plugin) {
        if (StringUtils.isNotBlank(permissionJson)) {
          try {
            permissions = JsonUtils.getObjectFromJson(permissionJson, Permissions.class);
          } catch (GenericException e) {
            // do nothing
          }
        } else if (StringUtils.isNotBlank(aipId)) {
          try {
            AIP aip = model.retrieveAIP(aipId);
            permissions = new Permissions(aip.getPermissions());
          } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
            // do nothing
          }
        }
      }

    }, new RODAObjectProcessingLogic<AIP>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        SimpleJobPluginInfo jobPluginInfo, Plugin<AIP> plugin, AIP object) {
        processAIP(index, model, storage, report, jobPluginInfo, cachedJob, object);
      }
    }, index, model, storage, liteList);
  }

  private void processAIP(IndexService index, ModelService model, StorageService storage, Report report,
    SimpleJobPluginInfo jobPluginInfo, Job job, AIP aip) {
    PluginState state = PluginState.SUCCESS;
    aip.setPermissions(permissions);

    try {
      model.updateAIPPermissions(aip, job.getUsername());
    } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
      state = PluginState.FAILURE;
      Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.ACTIVE);
      reportItem.addPluginDetails("Could not update AIP permissions: " + e.getMessage())
        .setPluginState(PluginState.FAILURE);
      report.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, index, reportItem, true, job);
    }

    jobPluginInfo.incrementObjectsProcessed(state);
    model.createUpdateAIPEvent(aip.getId(), null, null, null, PreservationEventType.UPDATE, eventDescription, state,
      outcomeText, details, job.getUsername(), true);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    if (StringUtils.isNotBlank(permissionJson)) {
      try {
        permissions = JsonUtils.getObjectFromJson(permissionJson, Permissions.class);
      } catch (GenericException e) {
        throw new PluginException("Permission json is not well formed: " + e.getMessage());
      }
    } else if (StringUtils.isNotBlank(aipId)) {
      try {
        AIP aip = model.retrieveAIP(aipId);
        permissions = new Permissions(aip.getPermissions());
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
        throw new PluginException("AIP could not be retrieved: " + e.getMessage());
      }
    }

    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return new Report();
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new UpdateAIPPermissionsPlugin();
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
    return PreservationEventType.MIGRATION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Exports AIPS to a local folder";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "The AIPs were successfully exported";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "The AIPs were not exported";
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Arrays.asList(AIP.class);
  }
}
