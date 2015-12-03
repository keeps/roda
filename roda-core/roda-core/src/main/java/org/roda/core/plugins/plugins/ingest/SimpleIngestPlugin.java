/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.core.data.common.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.PluginType;
import org.roda.core.data.v2.TransferredResource;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexServiceException;
import org.roda.core.model.AIP;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginUtils;
import org.roda.core.plugins.plugins.antivirus.AntivirusPlugin;
import org.roda.core.plugins.plugins.base.AIPValidationPlugin;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FIXME perhaps this should be renamed into "MultistepIngestPlugin"
public class SimpleIngestPlugin implements Plugin<TransferredResource> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleIngestPlugin.class);

  public static final PluginParameter PARAMETER_SIP_TO_AIP_CLASS = new PluginParameter("parameter.sip_to_aip_class",
    PluginParameter.TYPE_SIP_TO_AIP, "", true, false,
    SimpleIngestPlugin.class.getCanonicalName() + ".parameter.sip_to_aip_class");
  public static final PluginParameter PARAMETER_DO_VIRUS_CHECK = new PluginParameter("parameter.do_virus_check",
    PluginParameter.TYPE_BOOLEAN, "true", true, false,
    SimpleIngestPlugin.class.getCanonicalName() + ".parameter.do_virus_check");
  public static final PluginParameter PARAMETER_DO_AUTO_ACCEPT = new PluginParameter("parameter.do_auto_accept",
    PluginParameter.TYPE_BOOLEAN, "true", true, false,
    SimpleIngestPlugin.class.getCanonicalName() + ".parameter.do_auto_accept");

  private Map<String, String> parameters;
  private Report report;

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
    return "Simple Ingest Plugin";
  }

  @Override
  public String getVersion() {
    return "1.0.0";
  }

  @Override
  public String getDescription() {
    return "Performs all the tasks needed to ingest an SIP into an AIP";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> pluginParameters = new ArrayList<PluginParameter>();
    pluginParameters.add(PARAMETER_SIP_TO_AIP_CLASS);
    pluginParameters.add(PARAMETER_DO_VIRUS_CHECK);
    pluginParameters.add(PARAMETER_DO_AUTO_ACCEPT);
    return pluginParameters;
  }

  @Override
  public Map<String, String> getParameterValues() {
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    this.parameters = parameters;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<TransferredResource> list)
    throws PluginException {
    if (report == null) {
      report = new Report();
    }

    // 1) transform TransferredResource into an AIP
    List<AIP> aips = transformTransferredResourceIntoAnAIP(index, model, storage, list);
    // 2) do virus check
    if (verifyIfStepShouldBePerformed(PARAMETER_DO_VIRUS_CHECK, "false")) {
      doVirusCheck(index, model, storage, aips);
    }
    // 3) verify if AIP is well formed
    verifyIfAipIsWellFormed(index, model, storage, aips);
    // 4) verify if the user has permissions to ingest SIPS into the specified
    // fonds
    verifyProducerAuthorization();
    // 5) do file format normalization
    doFileFormatNormalization(index, model, storage, aips);
    // 6) generate dissemination copy
    generateDisseminationCopy(index, model, storage, aips);
    // 7) do auto accept
    if (verifyIfStepShouldBePerformed(PARAMETER_DO_AUTO_ACCEPT, "false")) {
      doAutoAccept(index, model, storage, aips);
    }

    return report;
  }

  private boolean verifyIfStepShouldBePerformed(PluginParameter pluginParameter, String defaultValue) {
    String paramValue = parameters.getOrDefault(pluginParameter.getName(), defaultValue);
    return Boolean.parseBoolean(paramValue);
  }

  private List<AIP> transformTransferredResourceIntoAnAIP(IndexService index, ModelService model,
    StorageService storage, List<TransferredResource> transferredResources) {
    List<AIP> aips = new ArrayList<AIP>();

    // String pluginClassName = BagitToAIPPlugin.class.getName();
    String pluginClassName = parameters.getOrDefault(PARAMETER_SIP_TO_AIP_CLASS.getName(), "");

    Plugin<TransferredResource> plugin = (Plugin<TransferredResource>) RodaCoreFactory.getPluginManager()
      .getPlugin(pluginClassName);
    try {
      plugin.setParameterValues(getParameterValues());
      plugin.execute(index, model, storage, transferredResources);
    } catch (PluginException | InvalidParameterException e) {
      // FIXME handle failure
      LOGGER.error("Error executing plug-in", e);
    }

    try {

      aips = PluginUtils.getJobAIPs(index, parameters);

    } catch (IndexServiceException | NotFoundException e) {
      LOGGER.error("Error getting AIPs from index", e);
    }

    return aips;
  }

  private void doVirusCheck(IndexService index, ModelService model, StorageService storage, List<AIP> aips) {
    executePlugin(index, model, storage, aips, AntivirusPlugin.class.getName());
  }

  private void verifyIfAipIsWellFormed(IndexService index, ModelService model, StorageService storage, List<AIP> aips) {
    executePlugin(index, model, storage, aips, AIPValidationPlugin.class.getName());
  }

  private void verifyProducerAuthorization() {

  }

  private void doFileFormatIdentification(IndexService index, ModelService model, StorageService storage,
    List<AIP> aips) {

  }

  private void doDeepCharacterization(IndexService index, ModelService model, StorageService storage, List<AIP> aips) {

  }

  private void doFileFormatValidation(IndexService index, ModelService model, StorageService storage, List<AIP> aips) {

  }

  private void doFileFormatNormalization(IndexService index, ModelService model, StorageService storage,
    List<AIP> aips) {

  }

  private void generateDisseminationCopy(IndexService index, ModelService model, StorageService storage,
    List<AIP> aips) {

  }

  private void doAutoAccept(IndexService index, ModelService model, StorageService storage, List<AIP> aips) {
    executePlugin(index, model, storage, aips, AutoAcceptSIP.class.getName());
  }

  @Override
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Plugin<TransferredResource> cloneMe() {
    return new SimpleIngestPlugin();
  }

  private void executePlugin(IndexService index, ModelService model, StorageService storage, List<AIP> aips,
    String pluginClassName) {
    Plugin<AIP> plugin = (Plugin<AIP>) RodaCoreFactory.getPluginManager().getPlugin(pluginClassName);
    try {
      plugin.setParameterValues(getParameterValues());
      plugin.execute(index, model, storage, aips);
    } catch (PluginException | InvalidParameterException e) {
      // FIXME handle failure
      LOGGER.error("Error executing plug-in", e);
    }
  }

  @Override
  public PluginType getType() {
    return PluginType.INGEST;
  }

  @Override
  public boolean areParameterValuesValid() {
    boolean areValid = true;
    String sipToAipClass = parameters.getOrDefault(PARAMETER_SIP_TO_AIP_CLASS.getName(), "");
    if (StringUtils.isNotBlank(sipToAipClass)) {
      Plugin<?> plugin = RodaCoreFactory.getPluginManager().getPlugin(sipToAipClass);
      if (plugin == null || plugin.getType() != PluginType.SIP_TO_AIP) {
        areValid = areValid && false;
      }
    } else {
      areValid = areValid && false;
    }

    return areValid;
  }
}
