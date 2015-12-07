/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.Attribute;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.PluginParameter.PluginParameterType;
import org.roda.core.data.Report;
import org.roda.core.data.ReportItem;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.InvalidParameterException;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.Job;
import org.roda.core.data.v2.Job.JOB_STATE;
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
import org.w3c.util.DateParser;

public class DefaultIngestPlugin implements Plugin<TransferredResource> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultIngestPlugin.class);

  public static final PluginParameter PARAMETER_SIP_TO_AIP_CLASS = new PluginParameter("parameter.sip_to_aip_class",
    "SIP to AIP transformer", PluginParameterType.PLUGIN_SIP_TO_AIP, "", true, false,
    "Canonical name of the class that will be used to transform the SIP into an AIP.");
  public static final PluginParameter PARAMETER_DO_VIRUS_CHECK = new PluginParameter("parameter.do_virus_check",
    "Virus check", PluginParameterType.BOOLEAN, "false", true, false, "Verifies if an SIP is free of virus.");
  public static final PluginParameter PARAMETER_DO_SIP_SYNTAX_CHECK = new PluginParameter(
    "parameter.do_sip_syntax_check", "SIP syntax check", PluginParameterType.BOOLEAN, "true", true, true,
    "Check SIP coherence. Verifies the validity and completeness of a SIP.");
  public static final PluginParameter PARAMETER_DO_PRODUCER_AUTHORIZATION_CHECK = new PluginParameter(
    "parameter.do_producer_authorization_check", "Producer authorization check", PluginParameterType.BOOLEAN, "true",
    true, true,
    "Check SIP producer authorization. Verifies that the producer of the SIP has permissions to ingest to the specified Fonds.");
  public static final PluginParameter PARAMETER_DO_AUTO_ACCEPT = new PluginParameter("parameter.do_auto_accept",
    "Auto accept SIP", PluginParameterType.BOOLEAN, "false", true, false, "Automatically accept SIPs.");

  private Map<String, String> parameters;
  private int totalSteps = 4;
  private int currentCompletionPercentage = 0;
  private int completionPercentageStep = 100 / totalSteps;

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
    return "Default Ingest Plugin";
  }

  @Override
  public String getVersion() {
    return "1.0";
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
    pluginParameters.add(PARAMETER_DO_SIP_SYNTAX_CHECK);
    pluginParameters.add(PARAMETER_DO_PRODUCER_AUTHORIZATION_CHECK);
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
    completionPercentageStep = calculateCompletionPercentageStep();
  }

  private int calculateCompletionPercentageStep() {
    int effectiveTotalSteps = totalSteps;
    for (PluginParameter pluginParameter : getParameters()) {
      if (pluginParameter.getType() == PluginParameterType.BOOLEAN && !verifyIfStepShouldBePerformed(pluginParameter)) {
        effectiveTotalSteps--;
      }
    }
    return 100 / effectiveTotalSteps;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<TransferredResource> resources) throws PluginException {
    Report report = new Report();
    report.setType(Report.TYPE_PLUGIN_REPORT);
    report.setTitle("Report of plugin " + getName());
    report.addAttribute(new Attribute("Agent name", getName()))
      .addAttribute(new Attribute("Agent version", getVersion()))
      .addAttribute(new Attribute("Start datetime", DateParser.getIsoDate(new Date())));
    Report pluginReport;
    
    // FIXME yet to be used
    Map<String, Report> reports = new HashMap<>();

    updateJobStatus(index, model, 0);

    // 1) transform TransferredResource into an AIP
    pluginReport = transformTransferredResourceIntoAnAIP(index, model, storage, resources);
    report = mergeReports(report, pluginReport);

    // 1.1) obtain list of AIPs that were successfully transformed from
    // transferred resources
    List<AIP> aips = getAIPsFromReport(index, report);

    updateJobStatus(index, model);

    // 2) do virus check
    if (verifyIfStepShouldBePerformed(PARAMETER_DO_VIRUS_CHECK)) {
      pluginReport = doVirusCheck(index, model, storage, aips);
      report = mergeReports(report, pluginReport);
      updateJobStatus(index, model);
    }

    // 3) verify if AIP is well formed
    pluginReport = verifyIfAipIsWellFormed(index, model, storage, aips);
    report = mergeReports(report, pluginReport);

    updateJobStatus(index, model);

    // 4) verify if the user has permissions to ingest SIPS into the specified
    // fonds
    pluginReport = verifyProducerAuthorization();
    report = mergeReports(report, pluginReport);

    // 5) do file format normalization
    pluginReport = doFileFormatNormalization(index, model, storage, aips);
    report = mergeReports(report, pluginReport);

    // 6) generate dissemination copy
    pluginReport = generateDisseminationCopy(index, model, storage, aips);
    report = mergeReports(report, pluginReport);

    // 7) do auto accept
    if (verifyIfStepShouldBePerformed(PARAMETER_DO_AUTO_ACCEPT)) {
      pluginReport = doAutoAccept(index, model, storage, aips);
      report = mergeReports(report, pluginReport);
    }

    updateJobStatus(index, model, 100);

    return report;
  }

  private void updateJobStatus(IndexService index, ModelService model) {
    currentCompletionPercentage = currentCompletionPercentage + completionPercentageStep;
    updateJobStatus(index, model, currentCompletionPercentage);
  }

  private void updateJobStatus(IndexService index, ModelService model, int newCompletionPercentage) {
    try {
      LOGGER.debug("New job completionPercentage: " + newCompletionPercentage);
      Job job = PluginUtils.getJobFromIndex(index, parameters);
      job.setCompletionPercentage(newCompletionPercentage);
      if (newCompletionPercentage == 0) {
        job.setState(JOB_STATE.STARTED);
      } else if (newCompletionPercentage == 100) {
        job.setState(JOB_STATE.COMPLETED);
        job.setEndDate(new Date());
      }
      model.updateJob(job);
    } catch (IndexServiceException | NotFoundException e) {
      LOGGER.error("Unable to get Job from index", e);
    }
  }

  private List<AIP> getAIPsFromReport(IndexService index, Report report) {
    List<AIP> aips = new ArrayList<>();
    List<String> aipIds = getAIPsIdsFromReport(report);

    int maxAips = 200;
    IndexResult<AIP> aipsFromIndex;
    try {
      aipsFromIndex = index.find(AIP.class, new Filter(new OneOfManyFilterParameter(RodaConstants.AIP_ID, aipIds)),
        null, new Sublist(0, maxAips));
      aips = aipsFromIndex.getResults();
    } catch (IndexServiceException e) {
      LOGGER.error("Error retrieving AIPs", e);
    }

    return aips;

  }

  private List<String> getAIPsIdsFromReport(Report report) {
    List<String> aipIds = new ArrayList<>();
    for (ReportItem reportItem : report.getItems()) {
      if (StringUtils.isNotBlank(reportItem.getItemId())) {
        aipIds.add(reportItem.getItemId());
      }
    }
    return aipIds;
  }

  private boolean verifyIfStepShouldBePerformed(PluginParameter pluginParameter) {
    String paramValue = parameters.getOrDefault(pluginParameter.getId(), pluginParameter.getDefaultValue());
    return Boolean.parseBoolean(paramValue);
  }

  private Report mergeReports(Report actual, Report plugin) {
    if (plugin != null) {
      for (ReportItem reportItem : plugin.getItems()) {
        actual.addItem(reportItem);
      }
    }

    return actual;
  }

  private Report transformTransferredResourceIntoAnAIP(IndexService index, ModelService model, StorageService storage,
    List<TransferredResource> transferredResources) {
    Report report = null;

    String pluginClassName = parameters.getOrDefault(PARAMETER_SIP_TO_AIP_CLASS.getId(),
      PARAMETER_SIP_TO_AIP_CLASS.getDefaultValue());

    Plugin<TransferredResource> plugin = (Plugin<TransferredResource>) RodaCoreFactory.getPluginManager()
      .getPlugin(pluginClassName);
    try {
      plugin.setParameterValues(getParameterValues());
      report = plugin.execute(index, model, storage, transferredResources);
    } catch (PluginException | InvalidParameterException e) {
      // FIXME handle failure
      LOGGER.error("Error executing plug-in", e);
    }

    return report;
  }

  private Report doVirusCheck(IndexService index, ModelService model, StorageService storage, List<AIP> aips) {
    return executePlugin(index, model, storage, aips, AntivirusPlugin.class.getName());
  }

  private Report verifyIfAipIsWellFormed(IndexService index, ModelService model, StorageService storage,
    List<AIP> aips) {
    return executePlugin(index, model, storage, aips, AIPValidationPlugin.class.getName());
  }

  private Report verifyProducerAuthorization() {
    return null;
  }

  private Report doFileFormatNormalization(IndexService index, ModelService model, StorageService storage,
    List<AIP> aips) {
    return null;
  }

  private Report generateDisseminationCopy(IndexService index, ModelService model, StorageService storage,
    List<AIP> aips) {
    return null;
  }

  private Report doAutoAccept(IndexService index, ModelService model, StorageService storage, List<AIP> aips) {
    return executePlugin(index, model, storage, aips, AutoAcceptSIP.class.getName());
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
    return new DefaultIngestPlugin();
  }

  private Report executePlugin(IndexService index, ModelService model, StorageService storage, List<AIP> aips,
    String pluginClassName) {
    Report report = null;
    Plugin<AIP> plugin = (Plugin<AIP>) RodaCoreFactory.getPluginManager().getPlugin(pluginClassName);

    try {
      plugin.setParameterValues(getParameterValues());
      report = plugin.execute(index, model, storage, aips);
    } catch (PluginException | InvalidParameterException e) {
      // FIXME handle failure
      LOGGER.error("Error executing plug-in", e);
    }

    return report;
  }

  @Override
  public PluginType getType() {
    return PluginType.INGEST;
  }

  @Override
  public boolean areParameterValuesValid() {
    boolean areValid = true;
    String sipToAipClass = parameters.getOrDefault(PARAMETER_SIP_TO_AIP_CLASS.getId(), "");
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
