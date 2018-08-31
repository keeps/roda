/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.common.PremisV3Utils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.plugins.plugins.ingest.AutoAcceptSIPPlugin;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StorageService;
import org.roda.core.util.IdUtils;

public class AppraisalPlugin extends AbstractPlugin<AIP> {
  private boolean accept;
  private String rejectReason = null;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_ACCEPT,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_ACCEPT, "Appraisal accept or reject", PluginParameterType.BOOLEAN,
        "true", false, false, "Allows to accept or reject intellectual entities."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_REJECT_REASON,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_REJECT_REASON, "Reject reason", PluginParameterType.STRING, "",
        false, false, "Appraisal reject reason"));
  }

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
    return "Update AIP permissions recursively";
  }

  @Override
  public String getDescription() {
    return "Update AIP permissions recursively copying from parent or using serializable permission object";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_ACCEPT));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_REJECT_REASON));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_ACCEPT)) {
      accept = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_ACCEPT));
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_REJECT_REASON)) {
      rejectReason = parameters.get(RodaConstants.PLUGIN_PARAMS_REJECT_REASON);
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return PluginHelper.processObjects(this, new RODAObjectsProcessingLogic<AIP>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<AIP> plugin, List<AIP> objects) {
        processAIP(model, index, report, jobPluginInfo, cachedJob, objects);
      }
    }, index, model, storage, liteList);
  }

  private void processAIP(ModelService model, IndexService index, Report report, JobPluginInfo jobPluginInfo, Job job,
    List<AIP> aips) {
    try {
      Map<String, Pair<Integer, Integer>> jobState = new HashMap<>();
      List<String> aipsToDelete = new ArrayList<>();
      Date now = new Date();

      String userAgentId;
      try {
        PreservationMetadata pm = PremisV3Utils.createPremisUserAgentBinary(job.getUsername(), model, index, true);
        userAgentId = pm.getId();
      } catch (AlreadyExistsException e) {
        userAgentId = IdUtils.getUserAgentId(job.getUsername());
      } catch (ValidationException e) {
        throw new GenericException(e);
      }

      for (AIP aip : aips) {
        Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class);
        PluginHelper.updatePartialJobReport(this, model, reportItem, false, job);
        PluginState state = PluginState.SUCCESS;

        try {
          String aipId = aip.getId();
          String jobId = aip.getIngestJobId();
          String ingestSIPUUID = aip.getIngestSIPUUID();
          if (accept) {
            // Accept AIP
            aip.setState(AIPState.ACTIVE);
            model.updateAIPState(aip, job.getUsername());

            // create preservation event
            String id = IdUtils.createPreservationMetadataId(PreservationMetadata.PreservationMetadataType.EVENT);
            PreservationEventType type = PreservationEventType.ACCESSION;
            String preservationEventDescription = AutoAcceptSIPPlugin.DESCRIPTION;
            List<LinkingIdentifier> sources = new ArrayList<>();
            List<LinkingIdentifier> outcomes = Arrays
              .asList(PluginHelper.getLinkingIdentifier(aipId, RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME));
            PluginState outcome = PluginState.SUCCESS;
            String outcomeDetailNote = AutoAcceptSIPPlugin.SUCCESS_MESSAGE;

            try {
              ContentPayload premisEvent = PremisV3Utils.createPremisEventBinary(id, now, type.toString(),
                preservationEventDescription, sources, outcomes, outcome.name(), outcomeDetailNote, null,
                Arrays.asList(userAgentId));

              model.createPreservationMetadata(PreservationMetadata.PreservationMetadataType.EVENT, id, aipId, null,
                null, null, premisEvent, true);
            } catch (AlreadyExistsException | ValidationException e) {
              throw new GenericException(e);
            }

          } else {
            // Reject AIP
            model.deleteAIP(aipId);
            aipsToDelete.add(aipId);
          }

          // create job report
          Job ingestJob = model.retrieveJob(jobId);
          Report ingestReport = model.retrieveJobReport(jobId, ingestSIPUUID, aipId);

          Report ingestReportItem = new Report();
          ingestReportItem.setTitle("Manual appraisal");
          ingestReportItem.setPlugin("Manual appraisal");
          ingestReportItem.setPluginDetails(rejectReason);
          ingestReportItem.setPluginState(accept ? PluginState.SUCCESS : PluginState.FAILURE);
          ingestReportItem.setOutcomeObjectState(accept ? AIPState.ACTIVE : AIPState.DELETED);
          ingestReportItem.setDateCreated(now);
          ingestReport.addReport(ingestReportItem);

          model.createOrUpdateJobReport(ingestReport, ingestJob);

          // save job state
          Pair<Integer, Integer> pair = jobState.get(jobId);
          if (pair == null) {
            jobState.put(jobId, Pair.of(1, accept ? 1 : 0));
          } else {
            jobState.put(jobId, Pair.of(pair.getFirst() + 1, pair.getSecond() + (accept ? 1 : 0)));
          }

        } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException e1) {
          state = PluginState.FAILURE;
        } finally {
          jobPluginInfo.incrementObjectsProcessed(state);

          StringBuilder outcomeText = new StringBuilder().append("The AIP '").append(aip.getId()).append("' was ");
          if (accept) {
            outcomeText.append("accepted into the repository.");
          } else {
            outcomeText.append("rejected from the repository.");
          }

          model.createUpdateAIPEvent(aip.getId(), null, null, null, PreservationEventType.APPRAISAL,
            "The process of updating an non active object of the repository", state, outcomeText.toString(), null,
            job.getUsername(), true);

          reportItem.setPluginState(state).setPluginDetails(outcomeText.toString());
          report.addReport(reportItem);
          PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
        }
      }

      // update job counters
      for (Map.Entry<String, Pair<Integer, Integer>> entry : jobState.entrySet()) {
        String jobId = entry.getKey();
        int total = entry.getValue().getFirst();
        int accepted = entry.getValue().getSecond();
        int rejected = total - accepted;
        Job ingestJob = model.retrieveJob(jobId);
        if (rejected > 0) {
          // change counter to failure
          ingestJob.getJobStats().setSourceObjectsProcessedWithSuccess(
            ingestJob.getJobStats().getSourceObjectsProcessedWithSuccess() - rejected);
          ingestJob.getJobStats().setSourceObjectsProcessedWithFailure(
            ingestJob.getJobStats().getSourceObjectsProcessedWithFailure() + rejected);
        }

        // decrement manual interaction counter
        ingestJob.getJobStats().setOutcomeObjectsWithManualIntervention(
          ingestJob.getJobStats().getOutcomeObjectsWithManualIntervention() - total);

        model.createOrUpdateJob(ingestJob);
      }

      index.commit(IndexedAIP.class, Job.class, IndexedReport.class, IndexedPreservationEvent.class);

    } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException e) {
      report.setPluginState(PluginState.FAILURE).setPluginDetails("Failed to update job counters");
    }
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return new Report();
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new AppraisalPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.INTERNAL;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.APPRAISAL;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Appraisal accept or remove AIPs";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Appraisal accept or remove AIPs was successful";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Appraisal accept or remove AIPs failed";
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Arrays.asList(AIP.class);
  }
}
