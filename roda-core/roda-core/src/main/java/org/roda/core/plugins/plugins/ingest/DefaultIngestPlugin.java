/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.ReportItem;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.plugins.plugins.antivirus.AntivirusPlugin;
import org.roda.core.plugins.plugins.base.AIPValidationPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.PremisSkeletonPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.SiegfriedPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.TikaFullTextPlugin;
import org.roda.core.plugins.plugins.ingest.migration.converters.PdfToPdfaPlugin;
import org.roda.core.plugins.plugins.ingest.validation.VeraPDFPlugin;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultIngestPlugin implements Plugin<TransferredResource> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultIngestPlugin.class);

  public static final PluginParameter PARAMETER_SIP_TO_AIP_CLASS = new PluginParameter("parameter.sip_to_aip_class",
    "SIP format", PluginParameterType.PLUGIN_SIP_TO_AIP, "", true, false,
    "Known format of SIP to be ingest into the repository.");
  public static final PluginParameter PARAMETER_PARENT_ID = new PluginParameter(RodaConstants.PLUGIN_PARAMS_PARENT_ID,
    "Parent Description Object ID", PluginParameterType.STRING, "", false, false,
    "Use the provided parent Description Object ID if the SIPs does not provide one.");
  public static final PluginParameter PARAMETER_FORCE_PARENT_ID = new PluginParameter(
    RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID, "Force parent Description Object ID", PluginParameterType.BOOLEAN,
    "false", false, false, "Use the provided parent Description Object ID even if the SIPs provide one.");
  public static final PluginParameter PARAMETER_DO_VIRUS_CHECK = new PluginParameter("parameter.do_virus_check",
    "Virus check", PluginParameterType.BOOLEAN, "true", true, false, "Verifies if an SIP is free of virus.");

  public static final PluginParameter PARAMETER_DO_PDFTOPDFA_CONVERSION = new PluginParameter(
    "parameter.do_pdftopdfa_conversion", "Convert PDF to valid PDF/A", PluginParameterType.BOOLEAN, "false", true,
    false, "Converts PDF files into veraPDF valid PDF/A files.");
  public static final PluginParameter PARAMETER_DO_VERAPDF_CHECK = new PluginParameter("parameter.do_verapdf_check",
    "VeraPDF check", PluginParameterType.BOOLEAN, "false", true, false,
    "Verifies if PDF files sent are veraPDF valid PDF/A files.");

  public static final PluginParameter PARAMETER_CREATE_PREMIS_SKELETON = new PluginParameter(
    "parameter.create.premis.skeleton", "Create basic PREMIS information", PluginParameterType.BOOLEAN, "true", true,
    true, "Create basic PREMIS information (e.g. PREMIS object for each representation file, etc.).");
  public static final PluginParameter PARAMETER_DO_SIP_SYNTAX_CHECK = new PluginParameter(
    "parameter.do_sip_syntax_check", "SIP syntax check", PluginParameterType.BOOLEAN, "true", true, true,
    "Check SIP coherence. Verifies the validity and completeness of a SIP.");
  public static final PluginParameter PARAMETER_DO_PRODUCER_AUTHORIZATION_CHECK = new PluginParameter(
    "parameter.do_producer_authorization_check", "Producer authorization check", PluginParameterType.BOOLEAN, "true",
    true, true,
    "Check SIP producer authorization. Verifies that the producer of the SIP has permissions to ingest to the specified Fonds.");
  public static final PluginParameter PARAMETER_DO_FILE_FORMAT_IDENTIFICATION = new PluginParameter(
    "parameter.do_file_format_identification", "File format identification", PluginParameterType.BOOLEAN, "true", true,
    false, "Does file format identification.");
  public static final PluginParameter PARAMETER_DO_METADATA_AND_FULL_TEXT_EXTRACTION = new PluginParameter(
    "parameter.do_metadata_and_full_text_extraction", "File metadata and full text extraction",
    PluginParameterType.BOOLEAN, "true", true, false, "Extracts file metadata and full text.");
  public static final PluginParameter PARAMETER_DO_AUTO_ACCEPT = new PluginParameter("parameter.do_auto_accept",
    "Auto accept SIP", PluginParameterType.BOOLEAN, "true", true, false, "Automatically accept SIPs.");

  private Map<String, String> parameters;
  private int totalSteps = 8;
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
    return "Performs all the tasks needed to ingest an SIP into the repository and therefore creating an AIP";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> pluginParameters = new ArrayList<PluginParameter>();
    pluginParameters.add(PARAMETER_SIP_TO_AIP_CLASS);
    pluginParameters.add(PARAMETER_PARENT_ID);
    pluginParameters.add(PARAMETER_FORCE_PARENT_ID);
    pluginParameters.add(PARAMETER_DO_VIRUS_CHECK);
    pluginParameters.add(PARAMETER_CREATE_PREMIS_SKELETON);
    pluginParameters.add(PARAMETER_DO_SIP_SYNTAX_CHECK);
    pluginParameters.add(PARAMETER_DO_PRODUCER_AUTHORIZATION_CHECK);
    pluginParameters.add(PARAMETER_DO_FILE_FORMAT_IDENTIFICATION);
    pluginParameters.add(PARAMETER_DO_METADATA_AND_FULL_TEXT_EXTRACTION);
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

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<TransferredResource> resources) throws PluginException {
    Report report = PluginHelper.createPluginReport(this);
    Report pluginReport;

    // FIXME
    Map<String, Report> reports = new HashMap<>();
    aipIdToObjectId = new HashMap<>();

    PluginHelper.updateJobStatus(index, model, 0, parameters);

    // 0) process "parent id" and "force parent id" info. (because we might need
    // to fallback to default values)
    String parentId = PluginHelper.getStringFromParameters(parameters, PARAMETER_PARENT_ID);
    boolean forceParentId = PluginHelper.getBooleanFromParameters(parameters, PARAMETER_FORCE_PARENT_ID);
    parameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, parentId);
    parameters.put(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID, forceParentId ? "true" : "false");

    // 1) transform TransferredResource into an AIP
    // 1.1) obtain list of AIPs that were successfully transformed from
    // transferred resources
    pluginReport = transformTransferredResourceIntoAnAIP(index, model, storage, resources);
    reports = mergeReports(reports, pluginReport);
    List<AIP> aips = getAIPsFromReports(index, model, storage, reports);
    currentCompletionPercentage = PluginHelper.updateJobStatus(index, model, currentCompletionPercentage,
      completionPercentageStep, parameters);

    // 2) do virus check
    if (PluginHelper.verifyIfStepShouldBePerformed(parameters, PARAMETER_DO_VIRUS_CHECK)) {
      pluginReport = doVirusCheck(index, model, storage, aips);
      reports = mergeReports(reports, pluginReport);
      currentCompletionPercentage = PluginHelper.updateJobStatus(index, model, currentCompletionPercentage,
        completionPercentageStep, parameters);
    }

    // 2.1) do pdftopdfa conversion
    if (PluginHelper.verifyIfStepShouldBePerformed(parameters, PARAMETER_DO_PDFTOPDFA_CONVERSION)) {
      Map<String, String> params = new HashMap<String, String>();
      params.put("maxKbytes", "20000");
      params.put("hasPartialSuccessOnOutcome", "True");
      pluginReport = doPDFtoPDFAConversion(index, model, storage, aips, params);
      reports = mergeReports(reports, pluginReport);
      currentCompletionPercentage = PluginHelper.updateJobStatus(index, model, currentCompletionPercentage,
        completionPercentageStep, parameters);
    }

    // 2.2) do verapdf check
    if (PluginHelper.verifyIfStepShouldBePerformed(parameters, PARAMETER_DO_VERAPDF_CHECK)) {
      Map<String, String> params = new HashMap<String, String>();
      params.put("profile", "1b");
      params.put("hasFeatures", "False");
      params.put("maxKbytes", "20000");
      params.put("hasPartialSuccessOnOutcome", "True");
      pluginReport = doVeraPDFCheck(index, model, storage, aips, params);
      reports = mergeReports(reports, pluginReport);
      currentCompletionPercentage = PluginHelper.updateJobStatus(index, model, currentCompletionPercentage,
        completionPercentageStep, parameters);
    }

    // 3) create premis skeleton
    pluginReport = createPremisSkeleton(index, model, storage, aips);
    reports = mergeReports(reports, pluginReport);
    currentCompletionPercentage = PluginHelper.updateJobStatus(index, model, currentCompletionPercentage,
      completionPercentageStep, parameters);

    // 4) verify if AIP is well formed
    pluginReport = verifyIfAipIsWellFormed(index, model, storage, aips);
    reports = mergeReports(reports, pluginReport);
    currentCompletionPercentage = PluginHelper.updateJobStatus(index, model, currentCompletionPercentage,
      completionPercentageStep, parameters);

    // 5) verify if the user has permissions to ingest SIPS into the specified
    // fonds
    pluginReport = verifyProducerAuthorization(index, model, storage, aips);
    reports = mergeReports(reports, pluginReport);
    currentCompletionPercentage = PluginHelper.updateJobStatus(index, model, currentCompletionPercentage,
      completionPercentageStep, parameters);

    // 6) do file format identification (sieg)
    if (PluginHelper.verifyIfStepShouldBePerformed(parameters, PARAMETER_DO_FILE_FORMAT_IDENTIFICATION)) {
      pluginReport = doFileFormatIdentification(index, model, storage, aips);
      reports = mergeReports(reports, pluginReport);
      currentCompletionPercentage = PluginHelper.updateJobStatus(index, model, currentCompletionPercentage,
        completionPercentageStep, parameters);
    }

    // 7) do metadata and full text extraction (tika)
    if (PluginHelper.verifyIfStepShouldBePerformed(parameters, PARAMETER_DO_METADATA_AND_FULL_TEXT_EXTRACTION)) {
      pluginReport = doMetadataAndFullTextExtraction(index, model, storage, aips);
      reports = mergeReports(reports, pluginReport);
      currentCompletionPercentage = PluginHelper.updateJobStatus(index, model, currentCompletionPercentage,
        completionPercentageStep, parameters);
    }

    // 8) do auto accept
    if (PluginHelper.verifyIfStepShouldBePerformed(parameters, PARAMETER_DO_AUTO_ACCEPT)) {
      pluginReport = doAutoAccept(index, model, storage, aips);
      reports = mergeReports(reports, pluginReport);
    }

    PluginHelper.updateJobStatus(index, model, 100, parameters);

    return report;
  }

  private int calculateCompletionPercentageStep() {
    int effectiveTotalSteps = totalSteps;
    for (PluginParameter pluginParameter : getParameters()) {
      if (pluginParameter.getType() == PluginParameterType.BOOLEAN
        && !PluginHelper.verifyIfStepShouldBePerformed(parameters, pluginParameter)) {
        effectiveTotalSteps--;
      }
    }
    return 100 / effectiveTotalSteps;
  }

  private List<AIP> getAIPsFromReports(IndexService index, ModelService model, StorageService storage,
    Map<String, Report> reports) {
    List<AIP> aips = new ArrayList<>();
    List<String> aipIds = getAIPsIdsFromReport(reports);

    LOGGER.debug("Getting AIPs: {}", aipIds);

    for (String aipId : aipIds) {
      try {
        aips.add(model.retrieveAIP(aipId));
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
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

  private Map<String, Report> mergeReports(Map<String, Report> reports, Report plugin) {
    if (plugin != null) {
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

  private Report createPremisSkeleton(IndexService index, ModelService model, StorageService storage, List<AIP> aips) {
    return executePlugin(index, model, storage, aips, PremisSkeletonPlugin.class.getName());
  }

  private Report doVirusCheck(IndexService index, ModelService model, StorageService storage, List<AIP> aips) {
    return executePlugin(index, model, storage, aips, AntivirusPlugin.class.getName());
  }

  private Report doPDFtoPDFAConversion(IndexService index, ModelService model, StorageService storage, List<AIP> aips,
    Map<String, String> params) {
    return executePlugin(index, model, storage, aips, PdfToPdfaPlugin.class.getName(), params);
  }

  private Report doVeraPDFCheck(IndexService index, ModelService model, StorageService storage, List<AIP> aips,
    Map<String, String> params) {
    return executePlugin(index, model, storage, aips, VeraPDFPlugin.class.getName(), params);
  }

  private Report verifyIfAipIsWellFormed(IndexService index, ModelService model, StorageService storage,
    List<AIP> aips) {
    return executePlugin(index, model, storage, aips, AIPValidationPlugin.class.getName());
  }

  private Report verifyProducerAuthorization(IndexService index, ModelService model, StorageService storage,
    List<AIP> aips) {
    return executePlugin(index, model, storage, aips, VerifyProducerAuthorizationPlugin.class.getName());
  }

  private Report doFileFormatIdentification(IndexService index, ModelService model, StorageService storage,
    List<AIP> aips) {
    return executePlugin(index, model, storage, aips, SiegfriedPlugin.class.getName());
  }

  private Report doMetadataAndFullTextExtraction(IndexService index, ModelService model, StorageService storage,
    List<AIP> aips) {
    return executePlugin(index, model, storage, aips, TikaFullTextPlugin.class.getName());
  }

  private Report doAutoAccept(IndexService index, ModelService model, StorageService storage, List<AIP> aips) {
    return executePlugin(index, model, storage, aips, AutoAcceptSIPPlugin.class.getName());
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
    return executePlugin(index, model, storage, aips, pluginClassName, null);
  }

  private Report executePlugin(IndexService index, ModelService model, StorageService storage, List<AIP> aips,
    String pluginClassName, Map<String, String> params) {
    Report report = null;
    Plugin<AIP> plugin = (Plugin<AIP>) RodaCoreFactory.getPluginManager().getPlugin(pluginClassName);
    Map<String, String> mergedParams = new HashMap<String, String>(getParameterValues());
    if (params != null) {
      mergedParams.putAll(params);
    }

    try {
      plugin.setParameterValues(mergedParams);
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
