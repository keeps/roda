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
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddRepresentationInformationFilterPlugin extends AbstractPlugin<RepresentationInformation> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AddRepresentationInformationFilterPlugin.class);
  private String filterToAdd;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_INFORMATION_FILTER,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_INFORMATION_FILTER, "New filter",
        PluginParameterType.STRING, "", true, false, "Representation information new filter"));
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
    return "Add representation information filter";
  }

  @Override
  public String getDescription() {
    return "Add filter to a representation information list";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_INFORMATION_FILTER));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_INFORMATION_FILTER)) {
      filterToAdd = parameters.get(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_INFORMATION_FILTER);
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return PluginHelper.processObjects(this, new RODAObjectProcessingLogic<RepresentationInformation>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<RepresentationInformation> plugin, RepresentationInformation object) {
        processRepresentationInformation(model, report, jobPluginInfo, cachedJob, object);
      }
    }, index, model, storage, liteList);
  }

  private void processRepresentationInformation(ModelService model, Report report, JobPluginInfo jobPluginInfo, Job job,
    RepresentationInformation representationInformation) {
    Report reportItem = PluginHelper.initPluginReportItem(this, representationInformation.getId(),
      RepresentationInformation.class);
    PluginHelper.updatePartialJobReport(this, model, reportItem, false, job);

    try {
      if (!representationInformation.getFilters().contains(filterToAdd)) {
        representationInformation.getFilters().add(filterToAdd);
      }
      model.updateRepresentationInformation(representationInformation, job.getUsername(), true);

      jobPluginInfo.incrementObjectsProcessedWithSuccess();
      reportItem.setPluginState(PluginState.SUCCESS);
    } catch (GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Could not update filter for representation information id: {}", representationInformation.getId(),
        e);
      jobPluginInfo.incrementObjectsProcessedWithFailure();
      reportItem.setPluginState(PluginState.FAILURE)
        .setPluginDetails("Failed to update representation information " + representationInformation.getId());
    } finally {
      report.addReport(reportItem);
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
  public Plugin<RepresentationInformation> cloneMe() {
    return new AddRepresentationInformationFilterPlugin();
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
    return "Add representation information filter";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Representation information filter was added successfully";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Representation information filter failed to add";
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public List<Class<RepresentationInformation>> getObjectClasses() {
    return Arrays.asList(RepresentationInformation.class);
  }
}
