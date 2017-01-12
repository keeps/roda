/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.notifications.EmailNotificationProcessor;
import org.roda.core.common.notifications.HTTPNotificationProcessor;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobStats;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.IngestJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.plugins.plugins.antivirus.AntivirusPlugin;
import org.roda.core.plugins.plugins.base.DescriptiveMetadataValidationPlugin;
import org.roda.core.plugins.plugins.base.ReplicationPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.PremisSkeletonPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.SiegfriedPlugin;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CaseFormat;

/***
 * https://docs.google.com/spreadsheets/d/
 * 1Ncu0My6tf19umSClIA6iXeYlJ4_FP6MygRwFCe0EzyM
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

  protected int totalSteps = 11;

  private String successMessage;
  private String failureMessage;
  private PreservationEventType eventType;
  private String eventDescription;

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    totalSteps = calculateEfectiveTotalSteps();
    getParameterValues().put(RodaConstants.PLUGIN_PARAMS_TOTAL_STEPS, getTotalSteps() + "");
    Boolean createSubmission = RodaCoreFactory.getRodaConfiguration()
      .getBoolean("core.ingest.sip2aip.create_submission", false);
    getParameterValues().put(RodaConstants.PLUGIN_PARAMS_CREATE_SUBMISSION, createSubmission.toString());
    getParameterValues().put(RodaConstants.PLUGIN_PARAMS_REPORTING_CLASS, getClass().getName());
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
    LOGGER.debug("Doing nothing in beforeAllExecute");
    return null;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    try {
      Date startDate = new Date();
      Report report = PluginHelper.initPluginReport(this);
      Report pluginReport;

      final IngestJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, IngestJobPluginInfo.class);
      PluginHelper.updateJobInformation(this, jobPluginInfo.setTotalSteps(getTotalSteps()));

      Job job = PluginHelper.getJob(this, model);
      List<TransferredResource> resources = PluginHelper.transformLitesIntoObjects(model, index, this, report,
        jobPluginInfo, liteList, job);

      // 0) process "parent id" and "force parent id" info. (because we might
      // need to fallback to default values)
      String parentId = PluginHelper.getStringFromParameters(this,
        getPluginParameter(RodaConstants.PLUGIN_PARAMS_PARENT_ID));
      boolean forceParentId = PluginHelper.getBooleanFromParameters(this,
        getPluginParameter(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID));
      getParameterValues().put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, parentId);
      getParameterValues().put(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID, forceParentId ? "true" : "false");

      // 1) unpacking & wellformedness check (transform TransferredResource into
      // an AIP)
      pluginReport = transformTransferredResourceIntoAnAIP(index, model, storage, resources);
      mergeReports(jobPluginInfo, pluginReport);
      final List<AIP> aips = getAIPsFromReports(model, resources, jobPluginInfo);
      PluginHelper.updateJobInformation(this, jobPluginInfo.incrementStepsCompletedByOne());

      // this event can only be created after AIPs exist and that's why it is
      // performed here, after transformTransferredResourceIntoAnAIP
      createIngestStartedEvent(model, index, jobPluginInfo, startDate);

      // 2) virus check
      if (!aips.isEmpty() && PluginHelper.verifyIfStepShouldBePerformed(this,
        getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_VIRUS_CHECK))) {
        pluginReport = doVirusCheck(index, model, storage, aips);
        mergeReports(jobPluginInfo, pluginReport);
        recalculateAIPsList(model, index, this, jobPluginInfo, aips, true);
        PluginHelper.updateJobInformation(this, jobPluginInfo.incrementStepsCompletedByOne());
      }

      // 3) descriptive metadata validation
      if (!aips.isEmpty() && PluginHelper.verifyIfStepShouldBePerformed(this,
        getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_DESCRIPTIVE_METADATA_VALIDATION))) {
        pluginReport = doDescriptiveMetadataValidation(index, model, storage, aips);
        mergeReports(jobPluginInfo, pluginReport);
        recalculateAIPsList(model, index, this, jobPluginInfo, aips, true);
        PluginHelper.updateJobInformation(this, jobPluginInfo.incrementStepsCompletedByOne());
      }

      // 4) create file fixity information
      if (!aips.isEmpty() && PluginHelper.verifyIfStepShouldBePerformed(this,
        getPluginParameter(RodaConstants.PLUGIN_PARAMS_CREATE_PREMIS_SKELETON))) {
        pluginReport = createFileFixityInformation(index, model, storage, aips);
        mergeReports(jobPluginInfo, pluginReport);
        recalculateAIPsList(model, index, this, jobPluginInfo, aips, true);
        PluginHelper.updateJobInformation(this, jobPluginInfo.incrementStepsCompletedByOne());
      }

      // 5) format identification (using Siegfried)
      if (!aips.isEmpty() && PluginHelper.verifyIfStepShouldBePerformed(this,
        getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_FILE_FORMAT_IDENTIFICATION))) {
        pluginReport = doFileFormatIdentification(index, model, storage, aips);
        mergeReports(jobPluginInfo, pluginReport);
        recalculateAIPsList(model, index, this, jobPluginInfo, aips, false);
        PluginHelper.updateJobInformation(this, jobPluginInfo.incrementStepsCompletedByOne());
      }

      // 6) Format validation - PDF/A format validator (using VeraPDF)
      if (!aips.isEmpty() && PluginHelper.verifyIfStepShouldBePerformed(this,
        getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_VERAPDF_CHECK))) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("profile", "1b");
        pluginReport = doVeraPDFCheck(index, model, storage, aips, params);
        mergeReports(jobPluginInfo, pluginReport);
        recalculateAIPsList(model, index, this, jobPluginInfo, aips, false);
        PluginHelper.updateJobInformation(this, jobPluginInfo.incrementStepsCompletedByOne());
      }

      // 7.1) feature extraction (using Apache Tika)
      // 7.2) full-text extraction (using Apache Tika)
      if (!aips.isEmpty() && (PluginHelper.verifyIfStepShouldBePerformed(this,
        getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_FEATURE_EXTRACTION))
        || PluginHelper.verifyIfStepShouldBePerformed(this,
          getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_FULL_TEXT_EXTRACTION)))) {
        Map<String, String> params = new HashMap<String, String>();
        params.put(RodaConstants.PLUGIN_PARAMS_DO_FEATURE_EXTRACTION, PluginHelper.verifyIfStepShouldBePerformed(this,
          getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_FEATURE_EXTRACTION)) ? "true" : "false");
        params.put(RodaConstants.PLUGIN_PARAMS_DO_FULLTEXT_EXTRACTION, PluginHelper.verifyIfStepShouldBePerformed(this,
          getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_FULL_TEXT_EXTRACTION)) ? "true" : "false");
        pluginReport = doFeatureAndFullTextExtraction(index, model, storage, aips, params);
        mergeReports(jobPluginInfo, pluginReport);
        recalculateAIPsList(model, index, this, jobPluginInfo, aips, false);
        PluginHelper.updateJobInformation(this, jobPluginInfo.incrementStepsCompletedByOne());
      }

      // 8) validation of digital signature
      if (!aips.isEmpty() && PluginHelper.verifyIfStepShouldBePerformed(this,
        getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_DIGITAL_SIGNATURE_VALIDATION))) {
        pluginReport = doDigitalSignatureValidation(index, model, storage, aips);
        mergeReports(jobPluginInfo, pluginReport);
        recalculateAIPsList(model, index, this, jobPluginInfo, aips, false);
        PluginHelper.updateJobInformation(this, jobPluginInfo.incrementStepsCompletedByOne());
      }

      // 9) verify producer authorization
      if (!aips.isEmpty() && PluginHelper.verifyIfStepShouldBePerformed(this,
        getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_PRODUCER_AUTHORIZATION_CHECK))) {
        pluginReport = verifyProducerAuthorization(index, model, storage, aips);
        mergeReports(jobPluginInfo, pluginReport);
        recalculateAIPsList(model, index, this, jobPluginInfo, aips, true);
        PluginHelper.updateJobInformation(this, jobPluginInfo.incrementStepsCompletedByOne());
      }

      // 10) Auto accept
      if (!aips.isEmpty()) {
        if (PluginHelper.verifyIfStepShouldBePerformed(this,
          getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_AUTO_ACCEPT))) {
          pluginReport = doAutoAccept(index, model, storage, aips);
          mergeReports(jobPluginInfo, pluginReport);
          recalculateAIPsList(model, index, this, jobPluginInfo, aips, true);
          jobPluginInfo.incrementStepsCompletedByOne();
        } else {
          updateAIPsToBeAppraised(model, aips, jobPluginInfo);
        }
      }

      // X) move SIPs to PROCESSED folder??? (default: false)
      if (PluginHelper.verifyIfStepShouldBePerformed(this,
        getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_AUTO_ACCEPT))
        && RodaCoreFactory.getRodaConfiguration().getBoolean("core.ingest.processed.move_when_autoaccept", false)) {
        PluginHelper.moveSIPs(this, model, index, resources, jobPluginInfo);
      }

      createIngestEndedEvent(model, index, aips);

      // 11) Replication
      if (!aips.isEmpty() && PluginHelper.verifyIfStepShouldBePerformed(this,
        getPluginParameter(RodaConstants.PLUGIN_PARAMS_DO_REPLICATION))) {
        pluginReport = doReplication(index, model, storage, aips);
        mergeReports(jobPluginInfo, pluginReport);
        PluginHelper.updateJobInformation(this, jobPluginInfo.incrementStepsCompletedByOne());
      }

      getAfterExecute().ifPresent(e -> e.execute(jobPluginInfo, aips));

      // X) final job info update
      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      return report;
    } catch (JobException | AuthorizationDeniedException | NotFoundException | GenericException
      | RequestNotValidException e) {
      throw new PluginException("A job exception has occurred", e);
    }

  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    LOGGER.debug("Doing stuff in afterAllExecute");
    try {
      sendNotification(model);
      index.commitAIPs();
      PluginHelper.fixParents(index, model, Optional.ofNullable(PluginHelper.getJobId(this)),
        PluginHelper.getSearchScopeFromParameters(this, model));
    } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException e) {
      LOGGER.error("Could not send ingest notification");
    }

    return null;
  }

  private Report transformTransferredResourceIntoAnAIP(IndexService index, ModelService model, StorageService storage,
    List<TransferredResource> transferredResources) {
    Report report = null;

    String pluginClassName = getParameterValues().getOrDefault(
      getPluginParameter(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS).getId(),
      getPluginParameter(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS).getDefaultValue());

    Plugin<TransferredResource> plugin = RodaCoreFactory.getPluginManager().getPlugin(pluginClassName,
      TransferredResource.class);
    try {
      plugin.setParameterValues(getParameterValues());
      List<LiteOptionalWithCause> lites = LiteRODAObjectFactory.transformIntoLiteWithCause(model, transferredResources);
      report = plugin.execute(index, model, storage, lites);
    } catch (PluginException | InvalidParameterException e) {
      // FIXME handle failure
      LOGGER.error("Error executing plugin", e);
    }

    return report;
  }

  private void mergeReports(IngestJobPluginInfo jobPluginInfo, Report plugin) {
    Map<String, String> aipIdToTransferredResourceId = jobPluginInfo.getAipIdToTransferredResourceId();
    if (plugin != null) {
      for (Report reportItem : plugin.getReports()) {
        if (TransferredResource.class.getName().equals(reportItem.getSourceObjectClass())) {
          Report report = new Report(reportItem);
          report.addReport(reportItem);
          jobPluginInfo.addReport(reportItem.getSourceObjectId(), reportItem.getOutcomeObjectId(), report);
        } else if (StringUtils.isNotBlank(reportItem.getOutcomeObjectId())
          && aipIdToTransferredResourceId.get(reportItem.getOutcomeObjectId()) != null) {
          jobPluginInfo.addReport(reportItem.getOutcomeObjectId(), reportItem);
        }
      }
    }
  }

  private List<AIP> getAIPsFromReports(ModelService model, List<TransferredResource> resources,
    IngestJobPluginInfo jobPluginInfo) {
    List<AIP> aips = new ArrayList<>();
    List<String> aipIds = jobPluginInfo.getAipIds();

    LOGGER.debug("Getting AIPs: {}", aipIds);
    for (String aipId : aipIds) {
      try {
        aips.add(model.retrieveAIP(aipId));
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
        LOGGER.error("Error while retrieving AIP", e);
      }
    }
    LOGGER.debug("Done retrieving AIPs");

    jobPluginInfo.updateCounters();
    return aips;
  }

  private void sendNotification(ModelService model)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    Job job = PluginHelper.getJob(this, model);
    JobStats jobStats = job.getJobStats();

    String emails = PluginHelper.getStringFromParameters(this,
      getPluginParameter(RodaConstants.PLUGIN_PARAMS_EMAIL_NOTIFICATION));

    if (StringUtils.isNotBlank(emails)) {
      List<String> emailList = new ArrayList<String>(Arrays.asList(emails.split("\\s*,\\s*")));
      Notification notification = new Notification();
      String outcome = PluginState.SUCCESS.toString();

      if (jobStats.getSourceObjectsProcessedWithFailure() > 0) {
        outcome = PluginState.FAILURE.toString();
      }

      // FIXME 20160512 hsilva: this should be a configuration string (at least,
      // not quite sure about i18n)
      notification.setSubject("RODA ingest process finished - " + outcome);
      notification.setFromUser(this.getClass().getSimpleName());
      notification.setRecipientUsers(emailList);

      Map<String, Object> scopes = new HashMap<String, Object>();
      scopes.put("outcome", outcome);
      scopes.put("type", CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, job.getPluginType().toString()));
      scopes.put("sips", jobStats.getSourceObjectsCount());
      scopes.put("success", jobStats.getSourceObjectsProcessedWithSuccess());
      scopes.put("failed", jobStats.getSourceObjectsProcessedWithFailure());
      scopes.put("name", job.getName());
      scopes.put("creator", job.getUsername());

      SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      scopes.put("start", parser.format(job.getStartDate()));

      long duration = (new Date().getTime() - job.getStartDate().getTime()) / 1000;
      scopes.put("duration", duration + " seconds");
      model.createNotification(notification,
        new EmailNotificationProcessor(RodaConstants.INGEST_EMAIL_TEMPLATE, scopes));
    }

    String httpNotifications = RodaCoreFactory.getRodaConfiguration()
      .getString(RodaConstants.NOTIFICATION_HTTP_ENDPOINT, "");
    if (StringUtils.isNotBlank(httpNotifications)) {
      Notification notification = new Notification();
      String outcome = PluginState.SUCCESS.toString();

      if (jobStats.getSourceObjectsProcessedWithFailure() > 0) {
        outcome = PluginState.FAILURE.toString();
      }

      notification.setSubject("RODA ingest process finished - " + outcome);
      notification.setFromUser(this.getClass().getSimpleName());
      notification.setRecipientUsers(Arrays.asList(httpNotifications));
      Map<String, Object> scope = new HashMap<String, Object>();
      scope.put(HTTPNotificationProcessor.JOB_KEY, job);
      model.createNotification(notification, new HTTPNotificationProcessor(httpNotifications, scope));
    }

  }

  /**
   * Recalculates (if failures must be noticed) and updates AIP objects (by
   * obtaining them from model)
   */
  private void recalculateAIPsList(ModelService model, IndexService index, Plugin executedPlugin,
    IngestJobPluginInfo jobPluginInfo, List<AIP> aips, boolean removeAIPProcessingFailed) {
    aips.clear();
    List<AIP> transferredResourceAips;
    List<String> transferredResourcesToRemoveFromjobPluginInfo = new ArrayList<>();
    boolean oneTransferredResourceAipFailed;
    for (Entry<String, Map<String, Report>> transferredResourcejobPluginInfoEntry : jobPluginInfo
      .getReportsFromBeingProcessed().entrySet()) {
      String transferredResourceId = transferredResourcejobPluginInfoEntry.getKey();
      transferredResourceAips = new ArrayList<>();
      oneTransferredResourceAipFailed = false;

      if (jobPluginInfo.getAipIds(transferredResourceId) != null) {
        for (String aipId : jobPluginInfo.getAipIds(transferredResourceId)) {
          Report aipReport = transferredResourcejobPluginInfoEntry.getValue().get(aipId);
          if (removeAIPProcessingFailed && aipReport.getPluginState() == PluginState.FAILURE) {
            LOGGER.trace("Removing AIP {} from the list", aipReport.getOutcomeObjectId());
            oneTransferredResourceAipFailed = true;
            break;
          } else {
            try {
              transferredResourceAips.add(model.retrieveAIP(aipId));
            } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
              LOGGER.error("Error while retrieving AIP", e);
            }
          }
        }

        if (oneTransferredResourceAipFailed) {
          LOGGER.trace(
            "Will not process AIPs from transferred resource '{}' any longer because at least one of them failed",
            transferredResourceId);
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          jobPluginInfo.failOtherTransferredResourceAIPs(executedPlugin, model, index, transferredResourceId);
          transferredResourcesToRemoveFromjobPluginInfo.add(transferredResourceId);
        } else {
          aips.addAll(transferredResourceAips);
        }
      }
    }

    for (String transferredResourceId : transferredResourcesToRemoveFromjobPluginInfo) {
      jobPluginInfo.remove(transferredResourceId);
    }
  }

  private int calculateEfectiveTotalSteps() {
    List<String> parameterIdsToIgnore = Arrays.asList(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID,
      RodaConstants.PLUGIN_PARAMS_DO_FEATURE_EXTRACTION, RodaConstants.PLUGIN_PARAMS_DO_FULL_TEXT_EXTRACTION,
      RodaConstants.PLUGIN_PARAMS_DO_AUTO_ACCEPT);
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

  private void createIngestStartedEvent(ModelService model, IndexService index, IngestJobPluginInfo jobPluginInfo,
    Date startDate) {
    Map<String, String> aipIdToTransferredResourceId = jobPluginInfo.getAipIdToTransferredResourceId();
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

  private Report createFileFixityInformation(IndexService index, ModelService model, StorageService storage,
    List<AIP> aips) {
    return executePlugin(index, model, storage, aips, PremisSkeletonPlugin.class.getName());
  }

  private Report doVirusCheck(IndexService index, ModelService model, StorageService storage, List<AIP> aips) {
    return executePlugin(index, model, storage, aips, AntivirusPlugin.class.getName());
  }

  private Report doVeraPDFCheck(IndexService index, ModelService model, StorageService storage, List<AIP> aips,
    Map<String, String> params) {
    return executePlugin(index, model, storage, aips, RodaConstants.PLUGIN_CLASS_VERAPDF, params);
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
    return executePlugin(index, model, storage, aips, RodaConstants.PLUGIN_CLASS_DIGITAL_SIGNATURE);
  }

  private Report doFeatureAndFullTextExtraction(IndexService index, ModelService model, StorageService storage,
    List<AIP> aips, Map<String, String> params) {
    return executePlugin(index, model, storage, aips, RodaConstants.PLUGIN_CLASS_TIKA_FULLTEXT, params);
  }

  private Report doAutoAccept(IndexService index, ModelService model, StorageService storage, List<AIP> aips) {
    return executePlugin(index, model, storage, aips, AutoAcceptSIPPlugin.class.getName());
  }

  private Report doReplication(IndexService index, ModelService model, StorageService storage, List<AIP> aips) {
    return executePlugin(index, model, storage, aips, ReplicationPlugin.class.getName());
  }

  private Report executePlugin(IndexService index, ModelService model, StorageService storage, List<AIP> aips,
    String pluginClassName) {
    return executePlugin(index, model, storage, aips, pluginClassName, null);
  }

  private Report executePlugin(IndexService index, ModelService model, StorageService storage, List<AIP> aips,
    String pluginClassName, Map<String, String> params) {
    Report report = null;
    Plugin<AIP> plugin = RodaCoreFactory.getPluginManager().getPlugin(pluginClassName, AIP.class);
    Map<String, String> mergedParams = new HashMap<String, String>(getParameterValues());
    if (params != null) {
      mergedParams.putAll(params);
    }

    try {
      plugin.setParameterValues(mergedParams);
      List<LiteOptionalWithCause> lites = LiteRODAObjectFactory.transformIntoLiteWithCause(model, aips);
      report = plugin.execute(index, model, storage, lites);
    } catch (InvalidParameterException | PluginException | RuntimeException e) {
      LOGGER.error("Error executing plugin", e);
    }

    return report;
  }

  private void updateAIPsToBeAppraised(ModelService model, List<AIP> aips, IngestJobPluginInfo jobPluginInfo) {

    for (AIP aip : aips) {
      aip.setState(AIPState.UNDER_APPRAISAL);
      try {
        aip = model.updateAIPState(aip, PluginHelper.getJobUsername(this, model));

        // update main report outcomeObjectState
        PluginHelper.updateJobReportState(this, model, aip.getId(), AIPState.UNDER_APPRAISAL);

        // update counters of manual intervention
        jobPluginInfo.incrementOutcomeObjectsWithManualIntervention();

      } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
        LOGGER.error("Error while updating AIP state to '{}'. Reason: {}", AIPState.UNDER_APPRAISAL, e.getMessage());
      }
    }
  }

  @Override
  public PluginType getType() {
    return PluginType.INGEST;
  }

  @Override
  public boolean areParameterValuesValid() {
    boolean areValid = true;
    PluginParameter sipToAipClassPluginParameter = getPluginParameter(RodaConstants.PLUGIN_PARAMS_SIP_TO_AIP_CLASS);
    String sipToAipClass = getParameterValues().getOrDefault(sipToAipClassPluginParameter.getId(),
      sipToAipClassPluginParameter.getDefaultValue());
    if (StringUtils.isNotBlank(sipToAipClass)) {
      Plugin<TransferredResource> plugin = RodaCoreFactory.getPluginManager().getPlugin(sipToAipClass,
        TransferredResource.class);
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

  public abstract Optional<? extends AfterExecute> getAfterExecute();

  public interface AfterExecute {
    void execute(IngestJobPluginInfo jobPluginInfo, List<AIP> aips);
  }
}
