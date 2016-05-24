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

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.plugins.antivirus.AntivirusPlugin;
import org.roda.core.plugins.plugins.base.DescriptiveMetadataValidationPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.PremisSkeletonPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.SiegfriedPlugin;
import org.roda.core.plugins.plugins.ingest.migration.PdfToPdfaPlugin;
import org.roda.core.plugins.plugins.ingest.validation.DigitalSignaturePlugin;
import org.roda.core.plugins.plugins.ingest.validation.VeraPDFPlugin;

public class ConfigurableIngestPlugin extends DefaultIngestPlugin {

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS, "Format of the Submission Information Packages",
        PluginParameterType.PLUGIN_SIP_TO_AIP, "", true, false,
        "Select the format of the Submission Information Packages to be ingested in this ingest process."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_PARENT_ID, "Parent Object", PluginParameterType.AIP_ID, "", false,
        false, "Use the provided parent object if the SIPs does not provide one."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID, "Force parent object",
        PluginParameterType.BOOLEAN, "false", false, false,
        "Use the provided parent object even if the SIPs provide one."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_VIRUS_CHECK,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_VIRUS_CHECK, AntivirusPlugin.getStaticName(),
        PluginParameterType.BOOLEAN, "true", true, false, AntivirusPlugin.getStaticDescription()));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_DESCRIPTIVE_METADATA_VALIDATION,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_DESCRIPTIVE_METADATA_VALIDATION,
        DescriptiveMetadataValidationPlugin.getStaticName(), PluginParameterType.BOOLEAN, "true", true, true,
        DescriptiveMetadataValidationPlugin.getStaticDescription()));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_PDFTOPDFA_CONVERSION,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_PDFTOPDFA_CONVERSION, PdfToPdfaPlugin.getStaticName(),
        PluginParameterType.BOOLEAN, "false", true, false, PdfToPdfaPlugin.getStaticDescription()));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_VERAPDF_CHECK,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_VERAPDF_CHECK, VeraPDFPlugin.getStaticName(),
        PluginParameterType.BOOLEAN, "false", true, false, VeraPDFPlugin.getStaticDescription()));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_CREATE_PREMIS_SKELETON,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_CREATE_PREMIS_SKELETON, PremisSkeletonPlugin.getStaticName(),
        PluginParameterType.BOOLEAN, "true", true, true, PremisSkeletonPlugin.getStaticDescription()));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_PRODUCER_AUTHORIZATION_CHECK,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_PRODUCER_AUTHORIZATION_CHECK,
        VerifyProducerAuthorizationPlugin.getStaticName(), PluginParameterType.BOOLEAN, "true", true, true,
        VerifyProducerAuthorizationPlugin.getStaticDescription()));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_FILE_FORMAT_IDENTIFICATION,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_FILE_FORMAT_IDENTIFICATION, SiegfriedPlugin.getStaticName(),
        PluginParameterType.BOOLEAN, "true", true, false, SiegfriedPlugin.getStaticDescription()));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_FEATURE_EXTRACTION,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_FEATURE_EXTRACTION, "Feature extraction",
        PluginParameterType.BOOLEAN, "false", true, false, "Extraction of technical metadata using Apache Tika"));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_FULL_TEXT_EXTRACTION,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_FULL_TEXT_EXTRACTION, "Full-text extraction",
        PluginParameterType.BOOLEAN, "false", true, false, "Extraction of full-text using Apache Tika"));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_DIGITAL_SIGNATURE_VALIDATION,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_DIGITAL_SIGNATURE_VALIDATION,
        DigitalSignaturePlugin.getStaticName(), PluginParameterType.BOOLEAN, "false", true, false,
        DigitalSignaturePlugin.getStaticDescription()));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DO_AUTO_ACCEPT,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_DO_AUTO_ACCEPT, AutoAcceptSIPPlugin.getStaticName(),
        PluginParameterType.BOOLEAN, "true", true, false, AutoAcceptSIPPlugin.getStaticDescription()));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_EMAIL_NOTIFICATION,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_EMAIL_NOTIFICATION, "Ingest finished notification",
        PluginParameterType.STRING, "", false, false,
        "Send a notification after finishing the ingest process to one or more e-mail addresses (comma separated)"));
  }

  @Override
  public String getName() {
    return "Configurable ingest workflow";
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
    ArrayList<PluginParameter> pluginParameters = new ArrayList<PluginParameter>();
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_PARENT_ID));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_VIRUS_CHECK));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_DESCRIPTIVE_METADATA_VALIDATION));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_CREATE_PREMIS_SKELETON));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_FILE_FORMAT_IDENTIFICATION));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_VERAPDF_CHECK));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_FEATURE_EXTRACTION));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_FULL_TEXT_EXTRACTION));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_DIGITAL_SIGNATURE_VALIDATION));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_PRODUCER_AUTHORIZATION_CHECK));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_AUTO_ACCEPT));
    pluginParameters.add(getPluginParameter(RodaConstants.PLUGIN_PARAMS_EMAIL_NOTIFICATION));
    return pluginParameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    setTotalSteps();
    super.setParameterValues(parameters);
  }

  @Override
  public Plugin<TransferredResource> cloneMe() {
    return new ConfigurableIngestPlugin();
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
    this.totalSteps = 10;
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList("ingest");
  }

}
