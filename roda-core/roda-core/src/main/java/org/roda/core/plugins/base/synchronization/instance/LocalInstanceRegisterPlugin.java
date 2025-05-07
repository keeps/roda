/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.synchronization.instance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.base.multiple.NoObjectsMultipleStepPlugin;
import org.roda.core.plugins.base.multiple.Step;
import org.roda.core.storage.utils.RODAInstanceUtils;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class LocalInstanceRegisterPlugin extends NoObjectsMultipleStepPlugin {
  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  private static List<Step> steps = new ArrayList<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_AIP_PLUGIN,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_AIP_PLUGIN,
          InstanceIdentifierAIPPlugin.getStaticName(), PluginParameter.PluginParameterType.BOOLEAN)
        .withDefaultValue("true").isReadOnly(true).withDescription(InstanceIdentifierAIPPlugin.getStaticDescription())
        .build());
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_AIP_EVENT_PLUGIN,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_AIP_EVENT_PLUGIN,
          InstanceIdentifierAIPEventPlugin.getStaticName(), PluginParameter.PluginParameterType.BOOLEAN)
        .withDefaultValue("true").isReadOnly(true)
        .withDescription(InstanceIdentifierAIPEventPlugin.getStaticDescription()).build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_DIP_PLUGIN,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_DIP_PLUGIN,
          InstanceIdentifierDIPPlugin.getStaticName(), PluginParameter.PluginParameterType.BOOLEAN)
        .withDefaultValue("true").isReadOnly(true).withDescription(InstanceIdentifierDIPPlugin.getStaticDescription())
        .build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_REPRESENTATION_INFORMATION_PLUGIN,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_REPRESENTATION_INFORMATION_PLUGIN,
          InstanceIdentifierRepresentationInformationPlugin.getStaticName(),
          PluginParameter.PluginParameterType.BOOLEAN)
        .withDefaultValue("true").isReadOnly(true)
        .withDescription(InstanceIdentifierRepresentationInformationPlugin.getStaticDescription()).build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_NOTIFICATION_PLUGIN,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_NOTIFICATION_PLUGIN,
          InstanceIdentifierNotificationPlugin.getStaticName(), PluginParameter.PluginParameterType.BOOLEAN)
        .withDefaultValue("true").isReadOnly(true)
        .withDescription(InstanceIdentifierNotificationPlugin.getStaticDescription()).build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_RISK_PLUGIN,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_RISK_PLUGIN,
          InstanceIdentifierRiskPlugin.getStaticName(), PluginParameter.PluginParameterType.BOOLEAN)
        .withDefaultValue("true").isReadOnly(true).withDescription(InstanceIdentifierRiskPlugin.getStaticDescription())
        .build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_RISK_INCIDENCE_PLUGIN,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_RISK_INCIDENCE_PLUGIN,
          InstanceIdentifierRiskIncidencePlugin.getStaticName(), PluginParameter.PluginParameterType.BOOLEAN)
        .withDefaultValue("true").isReadOnly(true)
        .withDescription(InstanceIdentifierRiskIncidencePlugin.getStaticDescription()).build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_JOB_PLUGIN,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_JOB_PLUGIN,
          InstanceIdentifierJobPlugin.getStaticName(), PluginParameter.PluginParameterType.BOOLEAN)
        .withDefaultValue("true").isReadOnly(true).withDescription(InstanceIdentifierJobPlugin.getStaticDescription())
        .build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_REPOSITORY_EVENT_PLUGIN,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_REPOSITORY_EVENT_PLUGIN,
          InstanceIdentifierRepositoryEventPlugin.getStaticName(), PluginParameter.PluginParameterType.BOOLEAN)
        .withDefaultValue("true").isReadOnly(true)
        .withDescription(InstanceIdentifierRepositoryEventPlugin.getStaticDescription()).build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_PRESERVATION_AGENT_PLUGIN,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_PRESERVATION_AGENT_PLUGIN,
          InstanceIdentifierPreservationAgentPlugin.getStaticName(), PluginParameter.PluginParameterType.BOOLEAN)
        .withDefaultValue("true").isReadOnly(true)
        .withDescription(InstanceIdentifierPreservationAgentPlugin.getStaticDescription()).build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_REGISTER_PLUGIN,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_DO_REGISTER_PLUGIN, RegisterPlugin.getStaticName(),
          PluginParameter.PluginParameterType.BOOLEAN)
        .withDefaultValue("false").withDescription(RegisterPlugin.getStaticDescription()).build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER, "Instance Identifier",
          PluginParameter.PluginParameterType.STRING)
        .withDefaultValue(RODAInstanceUtils.retrieveLocalInstanceIdentifierToPlugin()).isReadOnly(true)
        .withDescription("Identifier from the RODA local instance").build());

    steps.add(new Step(InstanceIdentifierAIPPlugin.class.getName(),
      RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_AIP_PLUGIN, true, true));
    steps.add(new Step(InstanceIdentifierAIPEventPlugin.class.getName(),
      RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_AIP_EVENT_PLUGIN, true, true));
    steps.add(new Step(InstanceIdentifierDIPPlugin.class.getName(),
      RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_DIP_PLUGIN, true, true));
    steps.add(new Step(InstanceIdentifierRepresentationInformationPlugin.class.getName(),
      RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_REPRESENTATION_INFORMATION_PLUGIN, true, true));
    steps.add(new Step(InstanceIdentifierNotificationPlugin.class.getName(),
      RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_NOTIFICATION_PLUGIN, true, true));
    steps.add(new Step(InstanceIdentifierRiskPlugin.class.getName(),
      RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_RISK_PLUGIN, true, true));
    steps.add(new Step(InstanceIdentifierRiskIncidencePlugin.class.getName(),
      RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_RISK_INCIDENCE_PLUGIN, true, true));
    steps.add(new Step(InstanceIdentifierJobPlugin.class.getName(),
      RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_JOB_PLUGIN, true, true));
    steps.add(new Step(InstanceIdentifierRepositoryEventPlugin.class.getName(),
      RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_REPOSITORY_EVENT_PLUGIN, true, true));
    steps.add(new Step(InstanceIdentifierPreservationAgentPlugin.class.getName(),
      RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_PRESERVATION_AGENT_PLUGIN, true, true));
    steps.add(new Step(RegisterPlugin.class.getName(), RodaConstants.PLUGIN_PARAMS_DO_REGISTER_PLUGIN, true, true));
  }

  @Override
  public List<PluginParameter> getParameters() {
    final ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_AIP_PLUGIN));
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_AIP_EVENT_PLUGIN));
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_DIP_PLUGIN));
    parameters
      .add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_REPRESENTATION_INFORMATION_PLUGIN));
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_NOTIFICATION_PLUGIN));
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_RISK_PLUGIN));
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_RISK_INCIDENCE_PLUGIN));
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_JOB_PLUGIN));
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_REPOSITORY_EVENT_PLUGIN));
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_PRESERVATION_AGENT_PLUGIN));
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_REGISTER_PLUGIN));
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER));
    return parameters;
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public String getName() {
    return "RODA Object instance identifier";
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
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_MANAGEMENT);
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new LocalInstanceRegisterPlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public List<Class<Void>> getObjectClasses() {
    return Arrays.asList(Void.class);
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model) throws PluginException {
    return null;
  }

  @Override
  public void setTotalSteps() {
    this.totalSteps = steps.size();
  }

  @Override
  public List<Step> getPluginSteps() {
    return steps;
  }

  @Override
  public PluginParameter getPluginParameter(String pluginParameterId) {
    if (pluginParameters.get(pluginParameterId) != null) {
      return pluginParameters.get(pluginParameterId);
    } else {
      return new PluginParameter();
    }
  }

  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    setTotalSteps();
    setSourceObjectsCount(1);
    super.setParameterValues(parameters);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model)
    throws PluginException {
    return new Report();
  }
}
