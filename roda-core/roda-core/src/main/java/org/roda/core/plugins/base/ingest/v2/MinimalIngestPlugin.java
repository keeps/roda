/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.ingest.v2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.base.characterization.PremisSkeletonPlugin;
import org.roda.core.plugins.base.ingest.AutoAcceptSIPPlugin;
import org.roda.core.plugins.base.ingest.EARKSIP2ToAIPPlugin;
import org.roda.core.plugins.base.ingest.VerifyUserAuthorizationPlugin;
import org.roda.core.plugins.base.ingest.v2.steps.AutoAcceptIngestStep;
import org.roda.core.plugins.base.ingest.v2.steps.IngestStep;
import org.roda.core.plugins.base.notifications.EmailIngestNotification;
import org.roda.core.plugins.base.notifications.HttpGenericNotification;
import org.roda.core.plugins.base.notifications.JobNotification;
import org.roda.core.plugins.base.preservation.DescriptiveMetadataValidationPlugin;

public class MinimalIngestPlugin extends DefaultIngestPlugin {
  private static List<IngestStep> steps = new ArrayList<>();
  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS, "Format of the Submission Information Packages",
          PluginParameterType.PLUGIN_SIP_TO_AIP)
        .withDefaultValue(EARKSIP2ToAIPPlugin.class.getName()).withDescription(
          "Select the format of the Submission Information Packages to be ingested in this ingest process.")
        .build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID,
      PluginParameter.getBuilder(RodaConstants.PLUGIN_PARAMS_PARENT_ID, "Parent node", PluginParameterType.AIP_ID)
        .isMandatory(false).withDescription("Use the provided parent node if the SIPs does not provide one.").build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID, "Force parent node", PluginParameterType.BOOLEAN)
        .withDefaultValue("false").isMandatory(false)
        .withDescription(
          "Force the use of the selected parent node even if the SIPs provide information about the desired parent.")
        .build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_DESCRIPTIVE_METADATA_VALIDATION,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_DO_DESCRIPTIVE_METADATA_VALIDATION,
          DescriptiveMetadataValidationPlugin.getStaticName(), PluginParameterType.BOOLEAN)
        .withDefaultValue("true").isReadOnly(true)
        .withDescription(DescriptiveMetadataValidationPlugin.getStaticDescription()).build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CREATE_PREMIS_SKELETON,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_CREATE_PREMIS_SKELETON, PremisSkeletonPlugin.getStaticName(),
          PluginParameterType.BOOLEAN)
        .withDefaultValue("true").isReadOnly(true).withDescription(PremisSkeletonPlugin.getStaticDescription())
        .build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_PRODUCER_AUTHORIZATION_CHECK,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_DO_PRODUCER_AUTHORIZATION_CHECK,
          VerifyUserAuthorizationPlugin.getStaticName(), PluginParameterType.BOOLEAN)
        .withDefaultValue("true").isReadOnly(true).withDescription(VerifyUserAuthorizationPlugin.getStaticDescription())
        .build());

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_AUTO_ACCEPT,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_DO_AUTO_ACCEPT, AutoAcceptSIPPlugin.getStaticName(),
          PluginParameterType.BOOLEAN)
        .withDefaultValue("true").isReadOnly(true).withDescription(AutoAcceptSIPPlugin.getStaticDescription()).build());
    pluginParameters.put(RodaConstants.NOTIFICATION_HTTP_ENDPOINT,
      PluginParameter
        .getBuilder(RodaConstants.NOTIFICATION_HTTP_ENDPOINT, "Ingest finished HTTP notification",
          PluginParameterType.STRING)
        .withDefaultValue(RodaCoreFactory.getRodaConfigurationAsString("ingest.configurable.http_endpoint"))
        .isMandatory(false)
        .withDescription("Send a notification after finishing the ingest process to a specific HTTP endpoint").build());

    // 2) descriptive metadata validation
    steps.add(new IngestStep(DescriptiveMetadataValidationPlugin.class.getName(),
      RodaConstants.PLUGIN_PARAMS_DO_DESCRIPTIVE_METADATA_VALIDATION, true, true, true, true));
    // 3) create file fixity information
    steps.add(new IngestStep(PremisSkeletonPlugin.class.getName(), RodaConstants.PLUGIN_PARAMS_CREATE_PREMIS_SKELETON,
      true, true, true, true));
    // 4) verify producer authorization
    steps.add(new IngestStep(VerifyUserAuthorizationPlugin.class.getName(),
      RodaConstants.PLUGIN_PARAMS_DO_PRODUCER_AUTHORIZATION_CHECK, true, true, true, true));
    // 5) Auto accept
    steps.add(new AutoAcceptIngestStep(AutoAcceptSIPPlugin.class.getName(), RodaConstants.PLUGIN_PARAMS_DO_AUTO_ACCEPT,
      true, true, true, true));
  }

  @Override
  public String getName() {
    return "Minimal ingest workflow";
  }

  @Override
  public String getVersionImpl() {
    return "2.0";
  }

  @Override
  public String getDescription() {
    return "Performs the minimum set of tasks needed to ingest a SIP into the repository and therefore creating an AIP.";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS));
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_PARENT_ID));
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID));
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_DESCRIPTIVE_METADATA_VALIDATION));
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_CREATE_PREMIS_SKELETON));
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_PRODUCER_AUTHORIZATION_CHECK));
    parameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_AUTO_ACCEPT));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    setTotalSteps();
    super.setParameterValues(parameters);
  }

  @Override
  public Plugin<TransferredResource> cloneMe() {
    return new MinimalIngestPlugin();
  }

  @Override
  public PluginParameter getPluginParameter(String pluginParameterId) {
    if (pluginParameters.get(pluginParameterId) != null) {
      return pluginParameters.get(pluginParameterId);
    } else {
      // just to not return null
      return new PluginParameter();
    }
  }

  @Override
  public void setTotalSteps() {
    this.totalSteps = steps.size() + 1;
  }

  @Override
  public List<IngestStep> getIngestSteps() {
    return steps;
  }

  @Override
  public List<JobNotification> getNotifications() {
    List<JobNotification> notifications = new ArrayList<>();

    boolean whenFailed = PluginHelper.getBooleanFromParameters(this,
      getPluginParameter(RodaConstants.PLUGIN_PARAMS_NOTIFICATION_WHEN_FAILED));

    notifications.add(new EmailIngestNotification(
      PluginHelper.getStringFromParameters(this, getPluginParameter(RodaConstants.PLUGIN_PARAMS_EMAIL_NOTIFICATION)),
      whenFailed));
    notifications.add(new HttpGenericNotification(
      PluginHelper.getStringFromParameters(this, getPluginParameter(RodaConstants.NOTIFICATION_HTTP_ENDPOINT)),
      whenFailed));
    return notifications;
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_INGEST);
  }

  @Override
  public List<Class<TransferredResource>> getObjectClasses() {
    return Arrays.asList(TransferredResource.class);
  }

  @Override
  public Optional<? extends AfterExecute> getAfterExecute() {
    return Optional.empty();
  }

}
