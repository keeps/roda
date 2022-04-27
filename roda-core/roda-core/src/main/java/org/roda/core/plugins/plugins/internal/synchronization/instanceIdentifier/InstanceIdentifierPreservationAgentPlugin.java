package org.roda.core.plugins.plugins.internal.synchronization.instanceIdentifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.utils.RODAInstanceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

public class InstanceIdentifierPreservationAgentPlugin extends AbstractPlugin<Void> {

  private static final Logger LOGGER = LoggerFactory.getLogger(InstanceIdentifierPreservationAgentPlugin.class);
  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER, "Instance Identifier",
        PluginParameter.PluginParameterType.STRING, RODAInstanceUtils.retrieveLocalInstanceIdentifierToPlugin(), true,
        true, "Identifier from the RODA local instance"));
  }

  private String instanceId;

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
    return "Preservation Agent instance identifier";
  }

  @Override
  public String getDescription() {
    return "Add the instance identifier on the data that exists on the storage as also on the index. "
      + "If an object already has an instance identifier it will be updated by the new one. "
      + "This task aims to help the synchronization between a RODA central instance and the RODA local instance, since when an local object is accessed in RODA Central it should have the instance identifier in order to inform from which source is it from.";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
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
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> list) throws PluginException {
    return PluginHelper.processVoids(this, new RODAProcessingLogic<Void>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<Void> plugin) {
        modifyInstanceId(model, index, report, jobPluginInfo);
      }
    }, index, model, storage);
  }

  private void modifyInstanceId(ModelService model, IndexService index, Report pluginReport,
    JobPluginInfo jobPluginInfo) {
    pluginReport.setPluginState(PluginState.SUCCESS);

    try (CloseableIterable<OptionalWithCause<PreservationMetadata>> iterable = model.listPreservationAgents()) {
      int agentCounter = 0;

      for (OptionalWithCause<PreservationMetadata> opm : iterable) {
        try {
          if (opm.isPresent()) {
            PreservationMetadata pm = opm.get();
            PremisV3Utils.updatePremisUserAgentId(pm, model, index, instanceId);
            jobPluginInfo.incrementObjectsProcessedWithSuccess();
          } else {
            jobPluginInfo.incrementObjectsProcessedWithFailure();
            pluginReport.setPluginState(PluginState.FAILURE)
              .addPluginDetails("Could not update preservation agent: " + opm.getCause());
          }
        } catch (AlreadyExistsException e) {
          jobPluginInfo.incrementObjectsProcessedWithSkipped();
        } catch (NotFoundException e) {
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          pluginReport.setPluginState(PluginState.FAILURE)
            .addPluginDetails("Could not update preservation agent: " + e.getCause());
        }
        agentCounter++;
      }
      jobPluginInfo.setSourceObjectsCount(agentCounter);
    } catch (GenericException | AuthorizationDeniedException | RequestNotValidException | ValidationException
      | IOException e) {
      LOGGER.error("Error getting preservation agents to be reindexed", e);
      pluginReport.setPluginState(PluginState.FAILURE);
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
  public Plugin<Void> cloneMe() {
    return new InstanceIdentifierPreservationAgentPlugin();
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
  public RodaConstants.PreservationEventType getPreservationEventType() {
    return RodaConstants.PreservationEventType.NONE;
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
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public List<Class<Void>> getObjectClasses() {
    return Arrays.asList(Void.class);
  }
}
