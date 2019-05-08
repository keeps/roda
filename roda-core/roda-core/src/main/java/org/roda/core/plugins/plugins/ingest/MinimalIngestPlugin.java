/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

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
import org.roda.core.plugins.plugins.base.DescriptiveMetadataValidationPlugin;
import org.roda.core.plugins.plugins.characterization.PremisSkeletonPlugin;

public class MinimalIngestPlugin extends DefaultIngestPlugin {

  public static final int TOTAL_STEPS = 5;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS, "Format of the Submission Information Packages",
        PluginParameterType.PLUGIN_SIP_TO_AIP, EARKSIPToAIPPlugin.class.getName(), true, false,
        "Select the format of the Submission Information Packages to be ingested in this ingest process."));
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_PARENT_ID, "Parent node", PluginParameterType.AIP_ID, "", false,
        false, "Use the provided parent node if the SIPs does not provide one."));
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID, "Force parent node", PluginParameterType.BOOLEAN,
        "false", false, false,
        "Force the use of the selected parent node even if the SIPs provide information about the desired parent."));
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_DESCRIPTIVE_METADATA_VALIDATION,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_DESCRIPTIVE_METADATA_VALIDATION,
        DescriptiveMetadataValidationPlugin.getStaticName(), PluginParameterType.BOOLEAN, "true", true, true,
        DescriptiveMetadataValidationPlugin.getStaticDescription()));
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CREATE_PREMIS_SKELETON,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_CREATE_PREMIS_SKELETON, PremisSkeletonPlugin.getStaticName(),
        PluginParameterType.BOOLEAN, "true", true, true, PremisSkeletonPlugin.getStaticDescription()));
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_PRODUCER_AUTHORIZATION_CHECK,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_PRODUCER_AUTHORIZATION_CHECK,
        VerifyUserAuthorizationPlugin.getStaticName(), PluginParameterType.BOOLEAN, "true", true, true,
        VerifyUserAuthorizationPlugin.getStaticDescription()));
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_AUTO_ACCEPT,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_AUTO_ACCEPT, AutoAcceptSIPPlugin.getStaticName(),
        PluginParameterType.BOOLEAN, "true", true, true, AutoAcceptSIPPlugin.getStaticDescription()));
    pluginParameters.put(RodaConstants.NOTIFICATION_HTTP_ENDPOINT,
      new PluginParameter(RodaConstants.NOTIFICATION_HTTP_ENDPOINT, "Ingest finished HTTP notification",
        PluginParameterType.STRING, RodaCoreFactory.getRodaConfigurationAsString("ingest.configurable.http_endpoint"),
        false, false, "Send a notification after finishing the ingest process to a specific HTTP endpoint"));
  }

  @Override
  public String getName() {
    return "Minimal ingest workflow";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public String getDescription() {
    return "Performs the minimum set of tasks needed to ingest a SIP into the repository and therefore creating an AIP.";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> pluginParameters = new ArrayList<>();
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_PARENT_ID));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_DESCRIPTIVE_METADATA_VALIDATION));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_CREATE_PREMIS_SKELETON));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_PRODUCER_AUTHORIZATION_CHECK));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_AUTO_ACCEPT));
    return pluginParameters;
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
    this.totalSteps = TOTAL_STEPS;
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
