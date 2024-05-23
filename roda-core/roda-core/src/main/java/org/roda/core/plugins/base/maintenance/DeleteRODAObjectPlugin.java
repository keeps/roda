/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.maintenance;

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
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.RODAObjectProcessingLogic;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteRODAObjectPlugin<T extends IsRODAObject> extends AbstractPlugin<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteRODAObjectPlugin.class);
  private String details = null;
  private boolean dontCheckRelatives;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS,
      PluginParameter.getBuilder(RodaConstants.PLUGIN_PARAMS_DETAILS, "Event details", PluginParameterType.STRING)
        .isMandatory(false).withDescription("Details that will be used when creating event").build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DONT_CHECK_RELATIVES,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_DONT_CHECK_RELATIVES, "Don't check relatives",
          PluginParameterType.BOOLEAN)
        .withDefaultValue("false").isMandatory(false).withDescription("If relatives shouldn't be checked for deletion")
        .build());
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
    return "Delete RODA entities";
  }

  @Override
  public String getDescription() {
    return "Delete any removable type of RODA entities";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DETAILS));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DONT_CHECK_RELATIVES));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DETAILS)) {
      details = parameters.get(RodaConstants.PLUGIN_PARAMS_DETAILS);
    }

    dontCheckRelatives = PluginHelper.getBooleanFromParameters(this,
      pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DONT_CHECK_RELATIVES));
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {

    return PluginHelper.processObjects(this,
      (RODAObjectProcessingLogic<T>) (index1, model1, storage1, report, cachedJob, jobPluginInfo, plugin,
        object) -> DeleteRodaObjectPluginUtils.process(index1, model1, report, cachedJob, jobPluginInfo, plugin, object,
          details, dontCheckRelatives),
      index, model, storage, liteList);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    try {
      Job job = PluginHelper.getJob(this, index);
      index.commit((Class<? extends IsIndexed>) Class.forName(job.getSourceObjects().getSelectedClass()));
    } catch (NotFoundException | GenericException | ClassNotFoundException | RequestNotValidException
      | AuthorizationDeniedException e) {
      LOGGER.error("Could not commit after delete operation", e);
    }

    return new Report();
  }

  @Override
  public Plugin<T> cloneMe() {
    return new DeleteRODAObjectPlugin<>();
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
    return PreservationEventType.DELETION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Deletes RODA entities";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "RODA entities were successfully removed";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "RODA entities were not successfully removed";
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
    list.add(File.class);
    list.add(Risk.class);
    list.add(RepresentationInformation.class);
    list.add(RiskIncidence.class);
    list.add(DIP.class);
    list.add(DIPFile.class);
    list.add(TransferredResource.class);
    return (List) list;
  }
}
