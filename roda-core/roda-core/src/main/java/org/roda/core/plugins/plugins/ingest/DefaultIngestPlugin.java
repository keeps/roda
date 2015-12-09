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
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
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
  private int totalSteps = 5;
  private int currentCompletionPercentage = 0;
  private int completionPercentageStep = 100 / totalSteps;
  private Map<String, String> aipIdToObjectId;

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
    Report report = PluginUtils.createPluginReport(this);
    Report pluginReport;

    // FIXME
    Map<String, Report> reports = new HashMap<>();
    aipIdToObjectId = new HashMap<>();

    updateJobStatus(index, model, null, 0);

    // 1) transform TransferredResource into an AIP
    pluginReport = transformTransferredResourceIntoAnAIP(index, model, storage, resources);
    reports = mergeReports(reports, pluginReport);

    // 1.1) obtain list of AIPs that were successfully transformed from
    // transferred resources
    List<AIP> aips = getAIPsFromReports(index, reports);

    updateJobStatus(index, model, null);

    // 2) do virus check
    if (verifyIfStepShouldBePerformed(PARAMETER_DO_VIRUS_CHECK)) {
      pluginReport = doVirusCheck(index, model, storage, aips);
      reports = mergeReports(reports, pluginReport);
      updateJobStatus(index, model, null);
    }

    // 3) verify if AIP is well formed
    pluginReport = verifyIfAipIsWellFormed(index, model, storage, aips);
    reports = mergeReports(reports, pluginReport);

    updateJobStatus(index, model, null);

    // 4) verify if the user has permissions to ingest SIPS into the specified
    // fonds
    pluginReport = verifyProducerAuthorization();
    reports = mergeReports(reports, pluginReport);

    // 5) do file format normalization
    pluginReport = doFileFormatNormalization(index, model, storage, aips);
    reports = mergeReports(reports, pluginReport);

    // 6) generate dissemination copy
    pluginReport = generateDisseminationCopy(index, model, storage, aips);
    reports = mergeReports(reports, pluginReport);

    // 7) do auto accept
    if (verifyIfStepShouldBePerformed(PARAMETER_DO_AUTO_ACCEPT)) {
      pluginReport = doAutoAccept(index, model, storage, aips);
      reports = mergeReports(reports, pluginReport);
    }

    updateJobStatus(index, model, null, 100);

    return report;
  }

  private void updateJobStatus(IndexService index, ModelService model, Map<String, Report> reports) {
    currentCompletionPercentage = currentCompletionPercentage + completionPercentageStep;
    updateJobStatus(index, model, reports, currentCompletionPercentage);
  }

  private void updateJobStatus(IndexService index, ModelService model, Map<String, Report> reports,
    int newCompletionPercentage) {
    try {
      LOGGER.debug("New job completionPercentage: " + newCompletionPercentage);
      Job job = PluginUtils.getJobFromIndex(index, parameters);
      job.setCompletionPercentage(newCompletionPercentage);
      if (reports != null) {
        job.setObjectIdsToAipIds(reports);
      }
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

  private List<AIP> getAIPsFromReports(IndexService index, Map<String, Report> reports) {
    List<AIP> aips = new ArrayList<>();
    List<String> aipIds = getAIPsIdsFromReport(reports);

    LOGGER.debug("Getting AIPs: {}", aipIds);

    if (!aipIds.isEmpty()) {
      int maxAips = 200;
      IndexResult<AIP> aipsFromIndex;
      try {
        aipsFromIndex = index.find(AIP.class, new Filter(new OneOfManyFilterParameter(RodaConstants.AIP_ID, aipIds)),
          null, new Sublist(0, maxAips));
        aips = aipsFromIndex.getResults();
      } catch (IndexServiceException e) {
        LOGGER.error("Error retrieving AIPs", e);
      }
    }

    return aips;

  }

  private List<String> getAIPsIdsFromReport(Map<String, Report> reports) {
    List<String> aipIds = new ArrayList<>();
    for (Entry<String, Report> entry : reports.entrySet()) {
      if (StringUtils.isNoneBlank(entry.getValue().getItems().get(0).getItemId())) {
        aipIds.add(entry.getValue().getItems().get(0).getItemId());
      }
    }

    return aipIds;
  }

  private boolean verifyIfStepShouldBePerformed(PluginParameter pluginParameter) {
    String paramValue = parameters.getOrDefault(pluginParameter.getId(), pluginParameter.getDefaultValue());
    return Boolean.parseBoolean(paramValue);
  }

  private Map<String, Report> mergeReports(Map<String, Report> reports, Report plugin) {
    if (plugin != null) {
      // LOGGER.debug("MAP: {} PluginReport: {}", aipIdToObjectId, plugin);
      for (ReportItem reportItem : plugin.getItems()) {
        if (StringUtils.isNotBlank(reportItem.getOtherId())) {
          aipIdToObjectId.put(reportItem.getItemId(), reportItem.getOtherId());
          Report report = new Report();
          report.addItem(reportItem);
          reports.put(reportItem.getOtherId(), report);

        } else if (StringUtils.isNotBlank(reportItem.getItemId())
          && aipIdToObjectId.get(reportItem.getItemId()) != null) {
          reports.get(aipIdToObjectId.get(reportItem.getItemId())).addItem(reportItem);
        }
      }
    }

    return reports;
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
