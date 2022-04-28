package org.roda.core.plugins.plugins.internal.synchronization.instanceIdentifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.utils.RODAInstanceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

public class InstanceIdentifierRiskIncidencePlugin extends AbstractPlugin<RiskIncidence> {

  private static final Logger LOGGER = LoggerFactory.getLogger(InstanceIdentifierRiskIncidencePlugin.class);
  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER, "Instance Identifier",
        PluginParameter.PluginParameterType.STRING, RODAInstanceUtils.retrieveLocalInstanceIdentifierToPlugin(), true,
        true, "Identifier from the RODA local instance"));
  }

  private String instanceId;

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public String getName() {
    return "Risk Incidence instance identifier";
  }

  @Override
  public String getDescription() {
    return "Add the instance identifier on the data that exists on the storage as also on the index. "
      + "If an object already has an instance identifier it will be updated by the new one. "
      + "This task aims to help the synchronization between a RODA central instance and the RODA local instance, "
      + "since when an local object is accessed in RODA Central it should have the instance identifier in order to "
      + "inform from which source is it from.";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters != null && parameters.containsKey(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER)) {
      instanceId = parameters.get(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER);
    }
  }

  @Override
  public RodaConstants.PreservationEventType getPreservationEventType() {
    return RodaConstants.PreservationEventType.UPDATE;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Updated the instance identifier";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "The instance identifier was updated successfully";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Could not update the instance identifier";
  }

  @Override
  public PluginType getType() {
    return PluginType.INTERNAL;
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public boolean areParameterValuesValid() {
    return false;
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
  public Plugin<RiskIncidence> cloneMe() {
    return new InstanceIdentifierRiskIncidencePlugin();
  }

  @Override
  public List<Class<RiskIncidence>> getObjectClasses() {
    return Arrays.asList(RiskIncidence.class);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    return new Report();
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> list) throws PluginException {
    return PluginHelper.processObjects(this, new RODAObjectsProcessingLogic<RiskIncidence>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<RiskIncidence> plugin, List<RiskIncidence> lites) {
        try {
          modifyInstanceId(model, index, cachedJob, report, jobPluginInfo);
        } catch (RequestNotValidException | GenericException | NotFoundException e) {
          LOGGER.error("Could not modify Instance ID's in objects");
        }
      }
    }, index, model, storage, list);
  }

  private void modifyInstanceId(ModelService model, IndexService index, Job cachedJob, Report pluginReport,
    JobPluginInfo jobPluginInfo) throws RequestNotValidException, GenericException, NotFoundException {
    PluginState state = PluginState.SUCCESS;
    String details = "";

    // Get RiskIncidences from index
    IterableIndexResult<RiskIncidence> indexedRiskIncidences = retrieveList(index);

    for (RiskIncidence indexedRiskIncidence : indexedRiskIncidences) {
      Report reportItem = PluginHelper.initPluginReportItem(this, indexedRiskIncidence.getId(), RiskIncidence.class);
      try {
        model.updateRiskIncidenceInstanceId(model.retrieveRiskIncidence(indexedRiskIncidence.getId()), true);
      } catch (GenericException | AuthorizationDeniedException e) {
        details = e.getMessage() + "\n";
        state = PluginState.FAILURE;
      }
      jobPluginInfo.incrementObjectsProcessed(state);
      reportItem.setPluginState(state);
      reportItem.addPluginDetails(details);
      pluginReport.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
    }
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return new Report();
  }

  private IterableIndexResult<RiskIncidence> retrieveList(IndexService index)
    throws RequestNotValidException, GenericException {
    Filter filter = new Filter();

    IterableIndexResult<RiskIncidence> indexedRiskIncidences = index.findAll(RiskIncidence.class, filter,
      Collections.singletonList(RodaConstants.INDEX_UUID));

    return indexedRiskIncidences;
  }

}
