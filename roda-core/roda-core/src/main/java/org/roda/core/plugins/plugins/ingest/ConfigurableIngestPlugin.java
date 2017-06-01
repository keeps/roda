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
import org.roda.core.plugins.PluginManager;
import org.roda.core.plugins.plugins.antivirus.AntivirusPlugin;
import org.roda.core.plugins.plugins.base.DescriptiveMetadataValidationPlugin;
import org.roda.core.plugins.plugins.base.ReplicationPlugin;
import org.roda.core.plugins.plugins.characterization.PremisSkeletonPlugin;
import org.roda.core.plugins.plugins.characterization.SiegfriedPlugin;

public class ConfigurableIngestPlugin extends DefaultIngestPlugin {

  private Map<String, PluginParameter> pluginParameters = new HashMap<>();
  private List<String> deactivatedPlugins = new ArrayList<>();

  @Override
  public String getName() {
    return "Default ingest workflow";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public String getDescription() {
    return "Performs all the tasks needed to ingest a SIP into the repository and therefore creating an AIP.";
  }

  @Override
  public List<PluginParameter> getParameters() {
    loadMap();

    ArrayList<PluginParameter> pluginParameters = new ArrayList<>();
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_PARENT_ID));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_VIRUS_CHECK));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_DESCRIPTIVE_METADATA_VALIDATION));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_CREATE_PREMIS_SKELETON));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_FILE_FORMAT_IDENTIFICATION));

    if (!deactivatedPlugins.contains(RodaConstants.PLUGIN_CLASS_VERAPDF)) {
      pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_VERAPDF_CHECK));
    }

    if (!deactivatedPlugins.contains(RodaConstants.PLUGIN_CLASS_TIKA_FULLTEXT)) {
      pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_FEATURE_EXTRACTION));
      pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_FULL_TEXT_EXTRACTION));
    }

    if (!deactivatedPlugins.contains(RodaConstants.PLUGIN_CLASS_DIGITAL_SIGNATURE)) {
      pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_DIGITAL_SIGNATURE_VALIDATION));
    }

    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_PRODUCER_AUTHORIZATION_CHECK));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_AUTO_ACCEPT));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_REPLICATION));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_EMAIL_NOTIFICATION));
    return pluginParameters;
  }

  private void loadMap() {
    if (pluginParameters.isEmpty()) {
      deactivatedPlugins = new ArrayList<>();

      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS,
        new PluginParameter(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS,
          "Format of the Submission Information Packages", PluginParameterType.PLUGIN_SIP_TO_AIP,
          "org.roda.core.plugins.plugins.ingest.EARKSIPToAIPPlugin", true, false,
          "Select the format of the Submission Information Packages to be ingested in this ingest process."));

      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID,
        new PluginParameter(RodaConstants.PLUGIN_PARAMS_PARENT_ID, "Parent node", PluginParameterType.AIP_ID, "", false,
          false, "Use the provided parent node if the SIPs does not provide one."));

      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID,
        new PluginParameter(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID, "Force parent node",
          PluginParameterType.BOOLEAN, "false", false, false,
          "Force the use of the selected parent node even if the SIPs provide information about the desired parent."));

      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_VIRUS_CHECK,
        new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_VIRUS_CHECK, AntivirusPlugin.getStaticName(),
          PluginParameterType.BOOLEAN, "true", true, false, AntivirusPlugin.getStaticDescription()));

      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_DESCRIPTIVE_METADATA_VALIDATION,
        new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_DESCRIPTIVE_METADATA_VALIDATION,
          DescriptiveMetadataValidationPlugin.getStaticName(), PluginParameterType.BOOLEAN, "true", true, true,
          DescriptiveMetadataValidationPlugin.getStaticDescription()));

      PluginManager pluginManager = RodaCoreFactory.getPluginManager();
      Plugin<?> plugin = pluginManager.getPlugin(RodaConstants.PLUGIN_CLASS_VERAPDF);
      if (plugin != null) {
        pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_VERAPDF_CHECK,
          new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_VERAPDF_CHECK, plugin.getName(),
            PluginParameterType.BOOLEAN, "false", true, false, plugin.getDescription()));
      } else {
        deactivatedPlugins.add(RodaConstants.PLUGIN_CLASS_VERAPDF);
      }

      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CREATE_PREMIS_SKELETON,
        new PluginParameter(RodaConstants.PLUGIN_PARAMS_CREATE_PREMIS_SKELETON, PremisSkeletonPlugin.getStaticName(),
          PluginParameterType.BOOLEAN, "true", true, true, PremisSkeletonPlugin.getStaticDescription()));

      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_PRODUCER_AUTHORIZATION_CHECK,
        new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_PRODUCER_AUTHORIZATION_CHECK,
          VerifyUserAuthorizationPlugin.getStaticName(), PluginParameterType.BOOLEAN, "true", true, true,
          VerifyUserAuthorizationPlugin.getStaticDescription()));

      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_FILE_FORMAT_IDENTIFICATION,
        new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_FILE_FORMAT_IDENTIFICATION, SiegfriedPlugin.getStaticName(),
          PluginParameterType.BOOLEAN, "true", true, false, SiegfriedPlugin.getStaticDescription()));

      plugin = pluginManager.getPlugin(RodaConstants.PLUGIN_CLASS_TIKA_FULLTEXT);
      if (plugin != null) {
        pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_FEATURE_EXTRACTION,
          new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_FEATURE_EXTRACTION, "Feature extraction",
            PluginParameterType.BOOLEAN, "false", true, false, "Extraction of technical metadata using Apache Tika"));

        pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_FULL_TEXT_EXTRACTION,
          new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_FULL_TEXT_EXTRACTION, "Full-text extraction",
            PluginParameterType.BOOLEAN, "false", true, false, "Extraction of full-text using Apache Tika"));
      } else {
        deactivatedPlugins.add(RodaConstants.PLUGIN_CLASS_TIKA_FULLTEXT);
      }

      plugin = pluginManager.getPlugin(RodaConstants.PLUGIN_CLASS_DIGITAL_SIGNATURE);
      if (plugin != null) {
        pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_DIGITAL_SIGNATURE_VALIDATION,
          new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_DIGITAL_SIGNATURE_VALIDATION, plugin.getName(),
            PluginParameterType.BOOLEAN, "false", true, false, plugin.getDescription()));
      } else {
        deactivatedPlugins.add(RodaConstants.PLUGIN_CLASS_DIGITAL_SIGNATURE);
      }

      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_AUTO_ACCEPT,
        new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_AUTO_ACCEPT, AutoAcceptSIPPlugin.getStaticName(),
          PluginParameterType.BOOLEAN, "true", true, false, AutoAcceptSIPPlugin.getStaticDescription()));

      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_REPLICATION,
        new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_REPLICATION, ReplicationPlugin.getStaticName(),
          PluginParameterType.BOOLEAN, "false", true, false, ReplicationPlugin.getStaticDescription()));

      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_EMAIL_NOTIFICATION,
        new PluginParameter(RodaConstants.PLUGIN_PARAMS_EMAIL_NOTIFICATION, "Ingest finished email notification",
          PluginParameterType.STRING, "", false, false,
          "Send a notification after finishing the ingest process to one or more e-mail addresses (comma separated)"));

      pluginParameters.put(RodaConstants.NOTIFICATION_HTTP_ENDPOINT,
        new PluginParameter(RodaConstants.NOTIFICATION_HTTP_ENDPOINT, "Ingest finished HTTP notification",
          PluginParameterType.STRING, RodaCoreFactory.getRodaConfigurationAsString("ingest.configurable.http_endpoint"),
          false, false, "Send a notification after finishing the ingest process to a specific HTTP endpoint"));
    }
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    loadMap();
    setTotalSteps();
    super.setParameterValues(parameters);
  }

  @Override
  public Plugin<TransferredResource> cloneMe() {
    return new ConfigurableIngestPlugin();
  }

  @Override
  public PluginParameter getPluginParameter(String pluginParameterId) {
    loadMap();
    if (pluginParameters.get(pluginParameterId) != null) {
      return pluginParameters.get(pluginParameterId);
    } else {
      return new PluginParameter();
    }
  }

  @Override
  public void setTotalSteps() {
    this.totalSteps = 11 - deactivatedPlugins.size();
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
