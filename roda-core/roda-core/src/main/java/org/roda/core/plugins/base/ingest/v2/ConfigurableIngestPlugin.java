/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.ingest.v2;

import java.util.ArrayList;
import java.util.Collections;
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
import org.roda.core.plugins.PluginManager;
import org.roda.core.plugins.base.antivirus.AntivirusPlugin;
import org.roda.core.plugins.base.characterization.PremisSkeletonPlugin;
import org.roda.core.plugins.base.characterization.SiegfriedPlugin;
import org.roda.core.plugins.base.disposal.rules.ApplyDisposalRulesPlugin;
import org.roda.core.plugins.base.ingest.AutoAcceptSIPPlugin;
import org.roda.core.plugins.base.ingest.EARKSIP2ToAIPPlugin;
import org.roda.core.plugins.base.ingest.VerifyUserAuthorizationPlugin;
import org.roda.core.plugins.base.ingest.v2.steps.AutoAcceptIngestStep;
import org.roda.core.plugins.base.ingest.v2.steps.IngestStep;
import org.roda.core.plugins.base.notifications.EmailIngestNotification;
import org.roda.core.plugins.base.notifications.HttpGenericNotification;
import org.roda.core.plugins.base.notifications.JobNotification;
import org.roda.core.plugins.base.preservation.DescriptiveMetadataValidationPlugin;

public class ConfigurableIngestPlugin extends DefaultIngestPlugin {
  public static final String FALSE = "false";
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
    // 10) apply a disposal schedule via disposal rules
    steps.add(new IngestStep(ApplyDisposalRulesPlugin.class.getName(),
      RodaConstants.PLUGIN_PARAMS_DO_APPLY_DISPOSAL_RULES, true, false, true, false));
    // 11) Auto accept
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

    ArrayList<PluginParameter> pluginParametersList = new ArrayList<>();
    pluginParametersList.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS));
    pluginParametersList.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_PARENT_ID));
    pluginParametersList.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID));
    pluginParametersList.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_VIRUS_CHECK));
    pluginParametersList.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_DESCRIPTIVE_METADATA_VALIDATION));
    pluginParametersList.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_CREATE_PREMIS_SKELETON));
    pluginParametersList.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_FILE_FORMAT_IDENTIFICATION));

    if (!deactivatedPlugins.contains(DefaultIngestPlugin.PLUGIN_CLASS_VERAPDF)) {
      pluginParametersList.add(getPluginParameter(DefaultIngestPlugin.PLUGIN_PARAMS_DO_VERAPDF_CHECK));
    }

    if (!deactivatedPlugins.contains(DefaultIngestPlugin.PLUGIN_CLASS_TIKA_FULLTEXT)) {
      pluginParametersList
        .add(getPluginParameter(DefaultIngestPlugin.PLUGIN_PARAMS_DO_FEATURE_AND_FULL_TEXT_EXTRACTION));
    }

    if (!deactivatedPlugins.contains(DefaultIngestPlugin.PLUGIN_CLASS_DIGITAL_SIGNATURE)) {
      pluginParametersList.add(getPluginParameter(DefaultIngestPlugin.PLUGIN_PARAMS_DO_DIGITAL_SIGNATURE_VALIDATION));
    }

    pluginParametersList.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_PRODUCER_AUTHORIZATION_CHECK));
    pluginParametersList.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_APPLY_DISPOSAL_RULES));
    pluginParametersList.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_AUTO_ACCEPT));
    pluginParametersList.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_EMAIL_NOTIFICATION));
    pluginParametersList.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_NOTIFICATION_WHEN_FAILED));
    return pluginParametersList;
  }

  private void loadMap() {
    if (pluginParameters.isEmpty()) {
      deactivatedPlugins = new ArrayList<>();

      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS,
        PluginParameter
          .getBuilder(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS, "Format of the Submission Information Packages",
            PluginParameterType.PLUGIN_SIP_TO_AIP)
          .withDefaultValue(EARKSIP2ToAIPPlugin.class.getName()).withDescription(
            "Select the format of the Submission Information Packages to be ingested in this ingest process.")
          .build());

      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID,
        PluginParameter.getBuilder(RodaConstants.PLUGIN_PARAMS_PARENT_ID, "Parent node", PluginParameterType.AIP_ID)
          .isMandatory(false).withDescription("Use the provided parent node if the SIPs does not provide one.")
          .build());

      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID,
        PluginParameter
          .getBuilder(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID, "Force parent node", PluginParameterType.BOOLEAN)
          .withDefaultValue(FALSE).isMandatory(false)
          .withDescription(
            "Force the use of the selected parent node even if the SIPs provide information about the desired parent.")
          .build());

      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_VIRUS_CHECK,
        PluginParameter
          .getBuilder(RodaConstants.PLUGIN_PARAMS_DO_VIRUS_CHECK, AntivirusPlugin.getStaticName(),
            PluginParameterType.BOOLEAN)
          .withDefaultValue("true").withDescription(AntivirusPlugin.getStaticDescription()).build());

      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_DESCRIPTIVE_METADATA_VALIDATION,
        PluginParameter
          .getBuilder(RodaConstants.PLUGIN_PARAMS_DO_DESCRIPTIVE_METADATA_VALIDATION,
            DescriptiveMetadataValidationPlugin.getStaticName(), PluginParameterType.BOOLEAN)
          .withDefaultValue("true").isReadOnly(true)
          .withDescription(DescriptiveMetadataValidationPlugin.getStaticDescription()).build());

      PluginManager pluginManager = RodaCoreFactory.getPluginManager();
      Plugin<?> plugin = pluginManager.getPlugin(DefaultIngestPlugin.PLUGIN_CLASS_VERAPDF);
      if (plugin != null) {
        pluginParameters.put(DefaultIngestPlugin.PLUGIN_PARAMS_DO_VERAPDF_CHECK, PluginParameter
          .getBuilder(DefaultIngestPlugin.PLUGIN_PARAMS_DO_VERAPDF_CHECK, plugin.getName(), PluginParameterType.BOOLEAN)
          .withDefaultValue(FALSE).withDescription(plugin.getDescription()).build());
      } else {
        deactivatedPlugins.add(DefaultIngestPlugin.PLUGIN_CLASS_VERAPDF);
      }

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
          .withDefaultValue("true").isReadOnly(true)
          .withDescription(VerifyUserAuthorizationPlugin.getStaticDescription()).build());

      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_FILE_FORMAT_IDENTIFICATION,
        PluginParameter
          .getBuilder(RodaConstants.PLUGIN_PARAMS_DO_FILE_FORMAT_IDENTIFICATION, SiegfriedPlugin.getStaticName(),
            PluginParameterType.BOOLEAN)
          .withDefaultValue("true").withDescription(SiegfriedPlugin.getStaticDescription()).build());

      plugin = pluginManager.getPlugin(DefaultIngestPlugin.PLUGIN_CLASS_TIKA_FULLTEXT);
      if (plugin != null) {
        pluginParameters.put(DefaultIngestPlugin.PLUGIN_PARAMS_DO_FEATURE_AND_FULL_TEXT_EXTRACTION,
          PluginParameter
            .getBuilder(DefaultIngestPlugin.PLUGIN_PARAMS_DO_FEATURE_AND_FULL_TEXT_EXTRACTION,
              "Feature & full-text extraction", PluginParameterType.BOOLEAN)
            .withDefaultValue(FALSE).withDescription("Extraction of technical metadata and full-text using Apache Tika")
            .build());
      } else {
        deactivatedPlugins.add(DefaultIngestPlugin.PLUGIN_CLASS_TIKA_FULLTEXT);
      }

      plugin = pluginManager.getPlugin(DefaultIngestPlugin.PLUGIN_CLASS_DIGITAL_SIGNATURE);
      if (plugin != null) {
        pluginParameters.put(DefaultIngestPlugin.PLUGIN_PARAMS_DO_DIGITAL_SIGNATURE_VALIDATION,
          PluginParameter.getBuilder(DefaultIngestPlugin.PLUGIN_PARAMS_DO_DIGITAL_SIGNATURE_VALIDATION,
            plugin.getName(), PluginParameterType.BOOLEAN).withDefaultValue(FALSE)
            .withDescription(plugin.getDescription()).build());
      } else {
        deactivatedPlugins.add(DefaultIngestPlugin.PLUGIN_CLASS_DIGITAL_SIGNATURE);
      }

      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_APPLY_DISPOSAL_RULES,
        PluginParameter
          .getBuilder(RodaConstants.PLUGIN_PARAMS_DO_APPLY_DISPOSAL_RULES, ApplyDisposalRulesPlugin.getStaticName(),
            PluginParameterType.BOOLEAN)
          .withDefaultValue("true").withDescription(ApplyDisposalRulesPlugin.getStaticDescription()).build());

      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_AUTO_ACCEPT,
        PluginParameter
          .getBuilder(RodaConstants.PLUGIN_PARAMS_DO_AUTO_ACCEPT, AutoAcceptSIPPlugin.getStaticName(),
            PluginParameterType.BOOLEAN)
          .withDefaultValue("true").withDescription(AutoAcceptSIPPlugin.getStaticDescription()).build());

      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_NOTIFICATION_WHEN_FAILED,
        PluginParameter
          .getBuilder(RodaConstants.PLUGIN_PARAMS_NOTIFICATION_WHEN_FAILED,
            "Ingest finished notification only when failed", PluginParameterType.BOOLEAN)
          .withDefaultValue(RodaCoreFactory.getRodaConfigurationAsString("ingest.notification.when_failed"))
          .isMandatory(false)
          .withDescription(
            "If checked, the ingest finished notification will only be sent if a fail occurs during ingestion")
          .build());

      pluginParameters.put(RodaConstants.PLUGIN_PARAMS_EMAIL_NOTIFICATION,
        PluginParameter
          .getBuilder(RodaConstants.PLUGIN_PARAMS_EMAIL_NOTIFICATION, "Ingest finished email notification",
            PluginParameterType.STRING)
          .isMandatory(false)
          .withDescription(
            "Send a notification after finishing the ingest process to one or more e-mail addresses (comma separated)")
          .build());

      pluginParameters.put(RodaConstants.NOTIFICATION_HTTP_ENDPOINT, PluginParameter
        .getBuilder(RodaConstants.NOTIFICATION_HTTP_ENDPOINT, "Ingest finished HTTP notification",
          PluginParameterType.STRING)
        .withDefaultValue(RodaCoreFactory.getRodaConfigurationAsString("ingest.configurable.http_endpoint"))
        .isMandatory(false)
        .withDescription("Send a notification after finishing the ingest process to a specific HTTP endpoint").build());
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
    return Collections.singletonList(RodaConstants.PLUGIN_CATEGORY_INGEST);
  }

  @Override
  public List<Class<TransferredResource>> getObjectClasses() {
    return Collections.singletonList(TransferredResource.class);
  }

  @Override
  public Optional<? extends AfterExecute> getAfterExecute() {
    return Optional.empty();
  }

}
