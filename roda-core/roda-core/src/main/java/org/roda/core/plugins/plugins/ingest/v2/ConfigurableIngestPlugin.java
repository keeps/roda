/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.v2;

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
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.plugins.plugins.antivirus.AntivirusPlugin;
import org.roda.core.plugins.plugins.base.DescriptiveMetadataValidationPlugin;
import org.roda.core.plugins.plugins.characterization.PremisSkeletonPlugin;
import org.roda.core.plugins.plugins.characterization.SiegfriedPlugin;
import org.roda.core.plugins.plugins.ingest.AutoAcceptSIPPlugin;
import org.roda.core.plugins.plugins.ingest.EARKSIPToAIPPlugin;
import org.roda.core.plugins.plugins.ingest.VerifyUserAuthorizationPlugin;
import org.roda.core.plugins.plugins.ingest.v2.steps.AutoAcceptIngestStep;
import org.roda.core.plugins.plugins.ingest.v2.steps.IngestStep;
import org.roda.core.plugins.plugins.notifications.EmailIngestNotification;
import org.roda.core.plugins.plugins.notifications.HttpGenericNotification;
import org.roda.core.plugins.plugins.notifications.JobNotification;

public class ConfigurableIngestPlugin extends DefaultIngestPlugin {
  private static List<IngestStep> steps = new ArrayList<>();

  static {
    // 2) virus check
    steps.add(new IngestStep(AntivirusPlugin.class.getName(), RodaConstants.PLUGIN_PARAMS_DO_VIRUS_CHECK, true, false,
      true, true));
    // 3) descriptive metadata validation
    steps.add(new IngestStep(DescriptiveMetadataValidationPlugin.class.getName(),
      RodaConstants.PLUGIN_PARAMS_DO_DESCRIPTIVE_METADATA_VALIDATION, true, true, true, true));
    // 4) create file fixity information
    steps.add(new IngestStep(PremisSkeletonPlugin.class.getName(), RodaConstants.PLUGIN_PARAMS_CREATE_PREMIS_SKELETON,
      true, true, true, true));
    // 5) format identification (using Siegfried)
    steps.add(new IngestStep(SiegfriedPlugin.class.getName(), RodaConstants.PLUGIN_PARAMS_DO_FILE_FORMAT_IDENTIFICATION,
      true, false, true, false));
    // 6) Format validation - PDF/A format validator (using VeraPDF)
    Map<String, String> params = new HashMap<>();
    params.put("profile", "1b");
    steps.add(new IngestStep(DefaultIngestPlugin.PLUGIN_CLASS_VERAPDF,
      DefaultIngestPlugin.PLUGIN_PARAMS_DO_VERAPDF_CHECK, false, false, true, false, params));
    // 7) feature & full-text extraction (using Apache Tika)
    params = new HashMap<>();
    params.put(RodaConstants.PLUGIN_PARAMS_DO_FEATURE_EXTRACTION, "true");
    params.put(RodaConstants.PLUGIN_PARAMS_DO_FULLTEXT_EXTRACTION, "true");
    steps.add(new IngestStep(DefaultIngestPlugin.PLUGIN_CLASS_TIKA_FULLTEXT,
      DefaultIngestPlugin.PLUGIN_PARAMS_DO_FEATURE_AND_FULL_TEXT_EXTRACTION, false, false, true, false, params));
    // 8) validation of digital signature
    steps.add(new IngestStep(DefaultIngestPlugin.PLUGIN_CLASS_DIGITAL_SIGNATURE,
      DefaultIngestPlugin.PLUGIN_PARAMS_DO_DIGITAL_SIGNATURE_VALIDATION, false, false, true, false));
    // 9) verify producer authorization
    steps.add(new IngestStep(VerifyUserAuthorizationPlugin.class.getName(),
      RodaConstants.PLUGIN_PARAMS_DO_PRODUCER_AUTHORIZATION_CHECK, true, true, true, true));
    // 10) Auto accept
    steps.add(new AutoAcceptIngestStep(AutoAcceptSIPPlugin.class.getName(), RodaConstants.PLUGIN_PARAMS_DO_AUTO_ACCEPT,
      true, true, true, true));
  }

  private Map<String, PluginParameter> pluginParameters = new HashMap<>();
  private List<String> deactivatedPlugins = new ArrayList<>();

  @Override
  public String getName() {
    return "Default ingest workflow";
  }

  @Override
  public String getVersionImpl() {
    return "2.0";
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

    if (!deactivatedPlugins.contains(DefaultIngestPlugin.PLUGIN_CLASS_VERAPDF)) {
      pluginParameters.add(getPluginParameter(DefaultIngestPlugin.PLUGIN_PARAMS_DO_VERAPDF_CHECK));
    }

    if (!deactivatedPlugins.contains(DefaultIngestPlugin.PLUGIN_CLASS_TIKA_FULLTEXT)) {
      pluginParameters.add(getPluginParameter(DefaultIngestPlugin.PLUGIN_PARAMS_DO_FEATURE_AND_FULL_TEXT_EXTRACTION));
    }

    if (!deactivatedPlugins.contains(DefaultIngestPlugin.PLUGIN_CLASS_DIGITAL_SIGNATURE)) {
      pluginParameters.add(getPluginParameter(DefaultIngestPlugin.PLUGIN_PARAMS_DO_DIGITAL_SIGNATURE_VALIDATION));
    }

    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_PRODUCER_AUTHORIZATION_CHECK));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_AUTO_ACCEPT));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_EMAIL_NOTIFICATION));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_NOTIFICATION_WHEN_FAILED));
    return pluginParameters;
  }

  private void loadMap() {
    if (pluginParameters.isEmpty()) {
      deactivatedPlugins = new ArrayList<>();

      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS,
        new PluginParameter(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS,
          "Format of the Submission Information Packages", PluginParameterType.PLUGIN_SIP_TO_AIP,
          EARKSIPToAIPPlugin.class.getName(), true, false,
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
      Plugin<?> plugin = pluginManager.getPlugin(DefaultIngestPlugin.PLUGIN_CLASS_VERAPDF);
      if (plugin != null) {
        pluginParameters.put(DefaultIngestPlugin.PLUGIN_PARAMS_DO_VERAPDF_CHECK,
          new PluginParameter(DefaultIngestPlugin.PLUGIN_PARAMS_DO_VERAPDF_CHECK, plugin.getName(),
            PluginParameterType.BOOLEAN, "false", true, false, plugin.getDescription()));
      } else {
        deactivatedPlugins.add(DefaultIngestPlugin.PLUGIN_CLASS_VERAPDF);
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

      plugin = pluginManager.getPlugin(DefaultIngestPlugin.PLUGIN_CLASS_TIKA_FULLTEXT);
      if (plugin != null) {
        pluginParameters.put(DefaultIngestPlugin.PLUGIN_PARAMS_DO_FEATURE_AND_FULL_TEXT_EXTRACTION,
          new PluginParameter(DefaultIngestPlugin.PLUGIN_PARAMS_DO_FEATURE_AND_FULL_TEXT_EXTRACTION,
            "Feature & full-text extraction", PluginParameterType.BOOLEAN, "false", true, false,
            "Extraction of technical metadata and full-text using Apache Tika"));
      } else {
        deactivatedPlugins.add(DefaultIngestPlugin.PLUGIN_CLASS_TIKA_FULLTEXT);
      }

      plugin = pluginManager.getPlugin(DefaultIngestPlugin.PLUGIN_CLASS_DIGITAL_SIGNATURE);
      if (plugin != null) {
        pluginParameters.put(DefaultIngestPlugin.PLUGIN_PARAMS_DO_DIGITAL_SIGNATURE_VALIDATION,
          new PluginParameter(DefaultIngestPlugin.PLUGIN_PARAMS_DO_DIGITAL_SIGNATURE_VALIDATION, plugin.getName(),
            PluginParameterType.BOOLEAN, "false", true, false, plugin.getDescription()));
      } else {
        deactivatedPlugins.add(DefaultIngestPlugin.PLUGIN_CLASS_DIGITAL_SIGNATURE);
      }

      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_AUTO_ACCEPT,
        new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_AUTO_ACCEPT, AutoAcceptSIPPlugin.getStaticName(),
          PluginParameterType.BOOLEAN, "true", true, false, AutoAcceptSIPPlugin.getStaticDescription()));

      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_NOTIFICATION_WHEN_FAILED,
        new PluginParameter(RodaConstants.PLUGIN_PARAMS_NOTIFICATION_WHEN_FAILED,
          "Ingest finished notification only when failed", PluginParameterType.BOOLEAN,
          RodaCoreFactory.getRodaConfigurationAsString("ingest.notification.when_failed"), false, false,
          "If checked, the ingest finished notification will only be sent if a fail occurs during ingestion"));

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
