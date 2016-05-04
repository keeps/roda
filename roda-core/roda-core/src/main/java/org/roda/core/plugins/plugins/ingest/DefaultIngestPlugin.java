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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
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
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.IngestJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.plugins.plugins.antivirus.AntivirusPlugin;
import org.roda.core.plugins.plugins.base.DescriptiveMetadataValidationPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.PremisSkeletonPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.SiegfriedPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.TikaFullTextPlugin;
import org.roda.core.plugins.plugins.ingest.validation.DigitalSignaturePlugin;
import org.roda.core.plugins.plugins.ingest.validation.VeraPDFPlugin;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * https://docs.google.com/spreadsheets/d/
 * 1Ncu0My6tf19umSClIA6iXeYlJ4_FP6MygRwFCe0EzyM
 * 
 * FIXME 20160323 hsilva: after each task (i.e. plugin), the AIP should be
 * obtained again from model (as it might have changed)
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 */
public abstract class DefaultIngestPlugin extends AbstractPlugin<TransferredResource> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultIngestPlugin.class);

  public static String START_SUCCESS = "The ingest process has started.";
  public static String START_FAILURE = "The ingest process has started.";
  public static String START_PARTIAL = "The ingest process has started.";
  public static String START_DESCRIPTION = "The ingest process has started.";
  public static PreservationEventType START_TYPE = PreservationEventType.INGEST_START;

  public static String END_SUCCESS = "The ingest process has successfully ended.";
  public static String END_FAILURE = "Failed to conclude the ingest process.";
  public static String END_PARTIAL = "The ingest process ended, however, some of the SIPs could not be successfully ingested.";
  public static String END_DESCRIPTION = "The ingest process has ended.";
  public static PreservationEventType END_TYPE = PreservationEventType.INGEST_END;

  protected int totalSteps = 10;
  protected boolean failIfRelatedAIPFailed = true;

  private String successMessage;
  private String failureMessage;
  private PreservationEventType eventType;
  private String eventDescription;

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    totalSteps = calculateEfectiveTotalSteps();
    getParameterValues().put(RodaConstants.PLUGIN_PARAMS_TOTAL_STEPS, getTotalSteps() + "");
  }

  public abstract PluginParameter getPluginParameter(String pluginParameterId);

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report beforeBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  private class Reports {
    // transferredResourceId > map<aipId, report>
    private Map<String, Map<String, Report>> reports = new HashMap<>();
    // transferredResourceId > list<aipId>
    private Map<String, List<String>> transferredResourceToAipIds = new HashMap<>();
    // aipId > transferredResourceId
    private Map<String, String> aipIdToTransferredResourceId = new HashMap<>();

    public Map<String, Map<String, Report>> getReports() {
      return reports;
    }

    public List<String> getAipIds() {
      return transferredResourceToAipIds.values().stream().flatMap(l -> l.stream()).collect(Collectors.toList());
    }

    public List<String> getAipIds(String transferredResource) {
      return transferredResourceToAipIds.get(transferredResource);
    }

    public Map<String, String> getAipIdToTransferredResourceId() {
      return aipIdToTransferredResourceId;
    }

    public void addReport(String sourceObjectId, String outcomeObjectId, Report report) {
      if (StringUtils.isNotBlank(sourceObjectId) && StringUtils.isNotBlank(outcomeObjectId)) {
        aipIdToTransferredResourceId.put(outcomeObjectId, sourceObjectId);
        if (transferredResourceToAipIds.get(sourceObjectId) != null) {
          transferredResourceToAipIds.get(sourceObjectId).add(outcomeObjectId);
        } else {
          List<String> aipIds = new ArrayList<>();
          aipIds.add(outcomeObjectId);
          transferredResourceToAipIds.put(sourceObjectId, aipIds);
        }
      }
      if (reports.get(sourceObjectId) != null) {
        reports.get(sourceObjectId).put(outcomeObjectId, report);
      } else {
        Map<String, Report> innerReports = new HashMap<>();
        innerReports.put(outcomeObjectId, report);
        reports.put(sourceObjectId, innerReports);
      }
    }

    public void addReport(String itemId, Report report) {
      reports.get(aipIdToTransferredResourceId.get(itemId)).get(itemId).addReport(report);
    }

    public void remove(String transferredResourceId) {
      reports.remove(transferredResourceId);
      transferredResourceToAipIds.remove(transferredResourceId);
    }

  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<TransferredResource> resources) throws PluginException {
    Date startDate = new Date();
    Reports reports = new Reports();
    Report report = PluginHelper.createPluginReport(this);
    Report pluginReport;

    IngestJobPluginInfo jobPluginInfo = new IngestJobPluginInfo();
    jobPluginInfo.setTotalSteps(getTotalSteps());
    jobPluginInfo.setObjectsCount(resources.size());
    jobPluginInfo.setObjectsBeingProcessed(resources.size());
    PluginHelper.updateJobInformation(this, jobPluginInfo);

    // 0) process "parent id" and "force parent id" info. (because we might need
    // to fallback to default values)
    String parentId = PluginHelper.getStringFromParameters(this,
      getPluginParameter(RodaConstants.PLUGIN_PARAMS_PARENT_ID));
    boolean forceParentId = PluginHelper.getBooleanFromParameters(this,
      getPluginParameter(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID));
    getParameterValues().put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, parentId);
    getParameterValues().put(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID, forceParentId ? "true" : "false");

    // 1) unpacking & wellformedness check (transform TransferredResource into
    // an AIP)
    pluginReport = transformTransferredResourceIntoAnAIP(index, model, storage, resources);
    reports = mergeReports(reports, pluginReport);
    List<AIP> aips = getAIPsFromReports(model, resources, jobPluginInfo, reports);
    PluginHelper.updateJobInformation(this, jobPluginInfo.incrementStepsCompletedByOne());

    // this event can only be created after AIPs exist and that's why it is
    // performed here, after transformTransferredResourceIntoAnAIP
    createIngestStartedEvent(model, index, reports, startDate);

    // 2) virus check
    if (PluginHelper.verifyIfStepShouldBePerformed(this,
      getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_VIRUS_CHECK))) {
      pluginReport = doVirusCheck(index, model, storage, aips);
      reports = mergeReports(reports, pluginReport);
      aips = recalculateAIPsList(model, resources, aips, reports, jobPluginInfo, true);
      PluginHelper.updateJobInformation(this, jobPluginInfo.incrementStepsCompletedByOne());
    }

    // 3) descriptive metadata validation
    if (PluginHelper.verifyIfStepShouldBePerformed(this,
      getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_DESCRIPTIVE_METADATA_VALIDATION))) {
      pluginReport = doDescriptiveMetadataValidation(index, model, storage, aips);
      reports = mergeReports(reports, pluginReport);
      aips = recalculateAIPsList(model, resources, aips, reports, jobPluginInfo, true);
      PluginHelper.updateJobInformation(this, jobPluginInfo.incrementStepsCompletedByOne());
    }

    // 4) create file fixity information
    if (PluginHelper.verifyIfStepShouldBePerformed(this,
      getPluginParameter(RodaConstants.PLUGIN_PARAMS_CREATE_PREMIS_SKELETON))) {
      pluginReport = createFileFixityInformation(index, model, storage, aips);
      reports = mergeReports(reports, pluginReport);
      aips = recalculateAIPsList(model, resources, aips, reports, jobPluginInfo, true);
      PluginHelper.updateJobInformation(this, jobPluginInfo.incrementStepsCompletedByOne());
    }

    // 5) format identification (using Siegfried)
    if (PluginHelper.verifyIfStepShouldBePerformed(this,
      getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_FILE_FORMAT_IDENTIFICATION))) {
      pluginReport = doFileFormatIdentification(index, model, storage, aips);
      reports = mergeReports(reports, pluginReport);
      aips = recalculateAIPsList(model, resources, aips, reports, jobPluginInfo, false);
      PluginHelper.updateJobInformation(this, jobPluginInfo.incrementStepsCompletedByOne());
    }

    // 6) Format validation - PDF/A format validator (using VeraPDF)
    if (PluginHelper.verifyIfStepShouldBePerformed(this,
      getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_VERAPDF_CHECK))) {
      Map<String, String> params = new HashMap<String, String>();
      params.put("profile", "1b");
      params.put("hasFeatures", "False");
      params.put("maxKbytes", "20000");
      pluginReport = doVeraPDFCheck(index, model, storage, aips, params);
      reports = mergeReports(reports, pluginReport);
      aips = recalculateAIPsList(model, resources, aips, reports, jobPluginInfo, true);
      PluginHelper.updateJobInformation(this, jobPluginInfo.incrementStepsCompletedByOne());
    }

    // 7) feature extraction (using Apache Tika)
    // 8) full-text extraction (using Apache Tika)
    if (PluginHelper.verifyIfStepShouldBePerformed(this,
      getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_FEATURE_EXTRACTION))
      || PluginHelper.verifyIfStepShouldBePerformed(this,
        getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_FULL_TEXT_EXTRACTION))) {
      Map<String, String> params = new HashMap<String, String>();
      params.put(RodaConstants.PLUGIN_PARAMS_DO_FEATURE_EXTRACTION, PluginHelper.verifyIfStepShouldBePerformed(this,
        getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_FEATURE_EXTRACTION)) ? "true" : "false");
      params.put(RodaConstants.PLUGIN_PARAMS_DO_FULLTEXT_EXTRACTION, PluginHelper.verifyIfStepShouldBePerformed(this,
        getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_FULL_TEXT_EXTRACTION)) ? "true" : "false");
      pluginReport = doFeatureAndFullTextExtraction(index, model, storage, aips, params);
      reports = mergeReports(reports, pluginReport);
      aips = recalculateAIPsList(model, resources, aips, reports, jobPluginInfo, false);
      PluginHelper.updateJobInformation(this, jobPluginInfo.incrementStepsCompletedByOne());
    }

    // 9) validation of digital signature
    if (PluginHelper.verifyIfStepShouldBePerformed(this,
      getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_DIGITAL_SIGNATURE_VALIDATION))) {
      pluginReport = doDigitalSignatureValidation(index, model, storage, aips);
      reports = mergeReports(reports, pluginReport);
      aips = recalculateAIPsList(model, resources, aips, reports, jobPluginInfo, false);
      PluginHelper.updateJobInformation(this, jobPluginInfo.incrementStepsCompletedByOne());
    }

    // 10) verify producer authorization
    if (PluginHelper.verifyIfStepShouldBePerformed(this,
      getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_PRODUCER_AUTHORIZATION_CHECK))) {
      pluginReport = verifyProducerAuthorization(index, model, storage, aips);
      reports = mergeReports(reports, pluginReport);
      aips = recalculateAIPsList(model, resources, aips, reports, jobPluginInfo, true);
      PluginHelper.updateJobInformation(this, jobPluginInfo.incrementStepsCompletedByOne());
    }

    // 11) Auto accept
    if (PluginHelper.verifyIfStepShouldBePerformed(this,
      getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_AUTO_ACCEPT))) {
      pluginReport = doAutoAccept(index, model, storage, aips);
      reports = mergeReports(reports, pluginReport);
      aips = recalculateAIPsList(model, resources, aips, reports, jobPluginInfo, true);
      jobPluginInfo.setObjectsProcessedWithSuccess(resources.size() - jobPluginInfo.getObjectsProcessedWithFailure());
      PluginHelper.updateJobInformation(this, jobPluginInfo.incrementStepsCompletedByOne());
    }

    // delete SIP from transfer
    // FIXME 20160429 hsilva: actually this will happen in two very distinct
    // moments and should not be done by a plugin 1) when auto accepting is on
    // 2) when doing manual acceptance

    createIngestEndedEvent(model, index, aips);

    return report;
  }

  @Override
  public Report afterBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

    String emails = PluginHelper.getStringFromParameters(this,
      getPluginParameter(RodaConstants.PLUGIN_PARAMS_EMAIL_NOTIFICATION));
    if (!"".equals(emails)) {
      List<String> emailList = new ArrayList<String>(Arrays.asList(emails.split("\\s*,\\s*")));
      try {
        Notification notification = new Notification();
        notification.setSubject("New ingest process was completed");
        notification.setFromUser("Ingest Process");
        notification.setRecipientUsers(emailList);
        Map<String, Object> scopes = new HashMap<String, Object>();
        model.createNotification(notification, RodaConstants.INGEST_EMAIL_TEMPLATE, scopes);
      } catch (GenericException e) {
        LOGGER.error("Error while creating new message", e);
      }
    }

    return null;
  }

  /**
   * Recalculates (if failures must be noticed) and updates AIP objects (by
   * obtaining them from model)
   */
  private List<AIP> recalculateAIPsList(ModelService model, List<TransferredResource> resources, List<AIP> aips,
    Reports reports, IngestJobPluginInfo jobPluginInfo, boolean removeAIPProcessingFailed) {
    List<AIP> newAips = new ArrayList<>();
    List<AIP> transferredResourceAips;
    boolean oneTransferredResourceAipFailed;
    for (Entry<String, Map<String, Report>> transferredResourceReportsEntry : reports.getReports().entrySet()) {
      String transferredResourceId = transferredResourceReportsEntry.getKey();
      transferredResourceAips = new ArrayList<>();
      oneTransferredResourceAipFailed = false;

      for (String aipId : reports.getAipIds(transferredResourceId)) {
        Report aipReport = transferredResourceReportsEntry.getValue().get(aipId);
        if (removeAIPProcessingFailed && aipReport.getPluginState() == PluginState.FAILURE) {
          LOGGER.trace("Removing AIP {} from the list", aipReport.getOutcomeObjectId());
          oneTransferredResourceAipFailed = true;
        } else {
          try {
            transferredResourceAips.add(model.retrieveAIP(aipId));
          } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
            LOGGER.error("Error while retrieving AIP", e);
          }
        }
      }

      if (failIfRelatedAIPFailed && oneTransferredResourceAipFailed) {
        LOGGER.trace(
          "Will not process AIPs from transferred resource '{}' any longer because at least one of them failed",
          transferredResourceId);
        reports.remove(transferredResourceId);
        // FIXME 20160502 hsilva: more should be done because reports in some of
        // the AIPs state that it went ok, so all reports of this transferred
        // resource should be updated to state the failure
      } else {
        newAips.addAll(transferredResourceAips);
      }
    }

    int sourceObjectsBeingProcessed = reports.getReports().keySet().size();
    jobPluginInfo.setObjectsProcessedWithFailure(resources.size() - sourceObjectsBeingProcessed);
    jobPluginInfo.setObjectsBeingProcessed(sourceObjectsBeingProcessed);

    return newAips;
  }

  private int calculateEfectiveTotalSteps() {
    List<String> parameterIdsToIgnore = Arrays.asList(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID,
      RodaConstants.PLUGIN_PARAMS_DO_FEATURE_EXTRACTION, RodaConstants.PLUGIN_PARAMS_DO_FULL_TEXT_EXTRACTION);
    int effectiveTotalSteps = getTotalSteps();
    boolean tikaParameters = false, dontDoFeatureExtraction = false, dontDoFulltext = false;

    for (PluginParameter pluginParameter : getParameters()) {
      if (pluginParameter.getType() == PluginParameterType.BOOLEAN
        && !parameterIdsToIgnore.contains(pluginParameter.getId())
        && !PluginHelper.verifyIfStepShouldBePerformed(this, pluginParameter)) {
        effectiveTotalSteps--;
      }

      if (pluginParameter.getId().equals(RodaConstants.PLUGIN_PARAMS_DO_FEATURE_EXTRACTION)) {
        tikaParameters = true;
        if (!PluginHelper.verifyIfStepShouldBePerformed(this, pluginParameter)) {
          dontDoFeatureExtraction = true;
        }
      }
      if (pluginParameter.getId().equals(RodaConstants.PLUGIN_PARAMS_DO_FULL_TEXT_EXTRACTION)) {
        tikaParameters = true;
        if (!PluginHelper.verifyIfStepShouldBePerformed(this, pluginParameter)) {
          dontDoFulltext = true;
        }
      }
    }

    if (tikaParameters && (dontDoFeatureExtraction && dontDoFulltext)) {
      effectiveTotalSteps--;
    }
    return effectiveTotalSteps;
  }

  private void createIngestStartedEvent(ModelService model, IndexService index, Reports reports, Date startDate) {
    Map<String, String> aipIdToTransferredResourceId = reports.getAipIdToTransferredResourceId();
    setPreservationEventType(START_TYPE);
    setPreservationSuccessMessage(START_SUCCESS);
    setPreservationFailureMessage(START_FAILURE);
    setPreservationEventDescription(START_DESCRIPTION);
    for (Map.Entry<String, String> entry : aipIdToTransferredResourceId.entrySet()) {
      try {
        AIP aip = model.retrieveAIP(entry.getKey());
        TransferredResource tr = index.retrieve(TransferredResource.class, entry.getValue());
        boolean notify = true;
        PluginHelper.createPluginEvent(this, aip.getId(), model, index, tr, PluginState.SUCCESS, "", notify, startDate);
      } catch (NotFoundException | RequestNotValidException | GenericException | AuthorizationDeniedException
        | ValidationException | AlreadyExistsException e) {
        LOGGER.warn("Error creating ingest start event", e);
      }
    }
  }

  private void createIngestEndedEvent(ModelService model, IndexService index, List<AIP> aips) {
    setPreservationEventType(END_TYPE);
    setPreservationSuccessMessage(END_SUCCESS);
    setPreservationFailureMessage(END_FAILURE);
    setPreservationEventDescription(END_DESCRIPTION);
    for (AIP aip : aips) {
      try {
        boolean notify = true;

        // FIXME create event based on report (outcome & outcomeDetails)
        PluginHelper.createPluginEvent(this, aip.getId(), model, index, PluginState.SUCCESS, "", notify);
      } catch (NotFoundException | RequestNotValidException | GenericException | AuthorizationDeniedException
        | ValidationException | AlreadyExistsException e) {
        LOGGER.warn("Error creating ingest end event", e);
      }
    }
  }

  private List<AIP> getAIPsFromReports(ModelService model, List<TransferredResource> resources,
    IngestJobPluginInfo jobPluginInfo, Reports reports) {
    List<AIP> aips = new ArrayList<>();
    List<String> aipIds = reports.getAipIds();

    LOGGER.debug("Getting AIPs: {}", aipIds);

    for (String aipId : aipIds) {
      try {
        aips.add(model.retrieveAIP(aipId));
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
        LOGGER.error("Error while retrieving AIP", e);
      }
    }
    LOGGER.debug("Done retrieving AIPs");

    jobPluginInfo.setObjectsBeingProcessed(reports.getReports().keySet().size());
    jobPluginInfo.setObjectsProcessedWithFailure(resources.size() - reports.getReports().keySet().size());

    return aips;

  }

  private Reports mergeReports(Reports reports, Report plugin) {
    Map<String, String> aipIdToTransferredResourceId = reports.getAipIdToTransferredResourceId();
    if (plugin != null) {
      for (Report reportItem : plugin.getReports()) {
        if (StringUtils.isNotBlank(reportItem.getSourceObjectId())) {
          Report report = new Report();
          report.addReport(reportItem);
          report.setOutcomeObjectId(reportItem.getOutcomeObjectId());
          reports.addReport(reportItem.getSourceObjectId(), reportItem.getOutcomeObjectId(), report);
        } else if (StringUtils.isNotBlank(reportItem.getOutcomeObjectId())
          && aipIdToTransferredResourceId.get(reportItem.getOutcomeObjectId()) != null) {
          reports.addReport(reportItem.getOutcomeObjectId(), reportItem);
        }
      }
    }

    return reports;
  }

  private Report transformTransferredResourceIntoAnAIP(IndexService index, ModelService model, StorageService storage,
    List<TransferredResource> transferredResources) {
    Report report = null;

    String pluginClassName = getParameterValues().getOrDefault(
      getPluginParameter(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS).getId(),
      getPluginParameter(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS).getDefaultValue());

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

  private Report createFileFixityInformation(IndexService index, ModelService model, StorageService storage,
    List<AIP> aips) {
    return executePlugin(index, model, storage, aips, PremisSkeletonPlugin.class.getName());
  }

  private Report doVirusCheck(IndexService index, ModelService model, StorageService storage, List<AIP> aips) {
    return executePlugin(index, model, storage, aips, AntivirusPlugin.class.getName());
  }

  private Report doVeraPDFCheck(IndexService index, ModelService model, StorageService storage, List<AIP> aips,
    Map<String, String> params) {
    return executePlugin(index, model, storage, aips, VeraPDFPlugin.class.getName(), params);
  }

  private Report doDescriptiveMetadataValidation(IndexService index, ModelService model, StorageService storage,
    List<AIP> aips) {
    return executePlugin(index, model, storage, aips, DescriptiveMetadataValidationPlugin.class.getName());
  }

  private Report verifyProducerAuthorization(IndexService index, ModelService model, StorageService storage,
    List<AIP> aips) {
    return executePlugin(index, model, storage, aips, VerifyProducerAuthorizationPlugin.class.getName());
  }

  private Report doFileFormatIdentification(IndexService index, ModelService model, StorageService storage,
    List<AIP> aips) {
    return executePlugin(index, model, storage, aips, SiegfriedPlugin.class.getName());
  }

  private Report doDigitalSignatureValidation(IndexService index, ModelService model, StorageService storage,
    List<AIP> aips) {
    return executePlugin(index, model, storage, aips, DigitalSignaturePlugin.class.getName());
  }

  private Report doFeatureAndFullTextExtraction(IndexService index, ModelService model, StorageService storage,
    List<AIP> aips, Map<String, String> params) {
    return executePlugin(index, model, storage, aips, TikaFullTextPlugin.class.getName(), params);
  }

  private Report doAutoAccept(IndexService index, ModelService model, StorageService storage, List<AIP> aips) {
    return executePlugin(index, model, storage, aips, AutoAcceptSIPPlugin.class.getName());
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
    } catch (Throwable e) {
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
    String sipToAipClass = getParameterValues()
      .getOrDefault(getPluginParameter(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS).getId(), "");
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

  @Override
  public PreservationEventType getPreservationEventType() {
    return eventType;
  }

  @Override
  public String getPreservationEventDescription() {
    return eventDescription;
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return successMessage;
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return failureMessage;
  }

  public void setPreservationEventType(PreservationEventType t) {
    this.eventType = t;
  }

  public void setPreservationSuccessMessage(String message) {
    this.successMessage = message;
  }

  public void setPreservationFailureMessage(String message) {
    this.failureMessage = message;
  }

  public void setPreservationEventDescription(String description) {
    this.eventDescription = description;
  }

  public int getTotalSteps() {
    return totalSteps;
  }

  public abstract void setTotalSteps();
}
