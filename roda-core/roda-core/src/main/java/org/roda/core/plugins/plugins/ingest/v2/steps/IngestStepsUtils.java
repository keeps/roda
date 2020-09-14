package org.roda.core.plugins.plugins.ingest.v2.steps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.IngestJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IngestStepsUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(IngestStepsUtils.class);

  public static void executePlugin(IngestStepBundle bundle, IngestStep step) {
    if (!step.needsAips() || !bundle.getAips().isEmpty()) {
      Report pluginReport = IngestStepsUtils.executeStep(bundle, step);
      mergeReports(bundle.getJobPluginInfo(), pluginReport);
      if (step.needsAips()) {
        recalculateAIPsList(bundle, step);
      }
    }
  }

  public static Report executeStep(IngestStepBundle bundle, IngestStep step) {
    Plugin<AIP> plugin = RodaCoreFactory.getPluginManager().getPlugin(step.getPluginName(), AIP.class);
    plugin.setMandatory(step.isMandatory());
    Map<String, String> mergedParams = new HashMap<>(bundle.getParameterValues());
    if (step.getParameters() != null) {
      mergedParams.putAll(step.getParameters());
    }

    // set outcome_object_id > source_object_id relation
    mergedParams.put(RodaConstants.PLUGIN_PARAMS_OUTCOMEOBJECTID_TO_SOURCEOBJECTID_MAP,
      JsonUtils.getJsonFromObject(bundle.getJobPluginInfo().getAipIdToTransferredResourceIds()));

    try {
      plugin.setParameterValues(mergedParams);
      plugin.setSipInformation(bundle.getSipInformation());
      List<LiteOptionalWithCause> lites = LiteRODAObjectFactory.transformIntoLiteWithCause(bundle.getModel(),
        bundle.getAips());
      return plugin.execute(bundle.getIndex(), bundle.getModel(), bundle.getStorage(), lites);
    } catch (InvalidParameterException | PluginException | RuntimeException e) {
      LOGGER.error("Error executing plugin: {}", step.getPluginName(), e);

      Report report = PluginHelper.initPluginReport(plugin);
      for (AIP aip : bundle.getAips()) {
        Report reportItem = PluginHelper.initPluginReportItem(plugin, aip.getId(), AIP.class,
          AIPState.INGEST_PROCESSING);
        reportItem.setPluginDetails(e.getMessage());
        reportItem.setPluginState(PluginState.FAILURE);
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(plugin, bundle.getModel(), reportItem, false, bundle.getCachedJob());
      }

      report.setPluginDetails(e.getMessage());
      report.setPluginState(PluginState.FAILURE);
      return report;
    }

    // return null;
  }

  public static void mergeReports(IngestJobPluginInfo jobPluginInfo, Report pluginReport) {
    if (pluginReport != null) {
      for (Report reportItem : pluginReport.getReports()) {
        if (TransferredResource.class.getName().equals(reportItem.getSourceObjectClass())) {
          Report report = new Report(reportItem);
          report.addReport(reportItem);
          jobPluginInfo.addReport(report, false);
        } else {
          jobPluginInfo.addReport(reportItem, true);
        }
      }
    }
  }

  /**
   * Recalculates (if failures must be noticed) and updates AIP objects (by
   * obtaining them from model)
   */
  public static void recalculateAIPsList(IngestStepBundle bundle, IngestStep step) {
    bundle.getAips().clear();
    Set<String> aipsToReturn = new HashSet<>();
    Set<String> transferredResourceAips;
    List<String> transferredResourcesToRemoveFromjobPluginInfo = new ArrayList<>();
    boolean oneTransferredResourceAipFailed;
    IngestJobPluginInfo jobPluginInfo = bundle.getJobPluginInfo();

    for (Map.Entry<String, Map<String, Report>> transferredResourcejobPluginInfoEntry : jobPluginInfo
      .getReportsFromBeingProcessed().entrySet()) {
      String transferredResourceId = transferredResourcejobPluginInfoEntry.getKey();
      transferredResourceAips = new HashSet<>();
      oneTransferredResourceAipFailed = false;

      if (jobPluginInfo.getAipIds(transferredResourceId) != null) {
        for (String aipId : jobPluginInfo.getAipIds(transferredResourceId)) {
          Report aipReport = transferredResourcejobPluginInfoEntry.getValue().get(aipId);
          if (step.isMandatory() && aipReport.getPluginState() == PluginState.FAILURE) {
            LOGGER.trace("Removing AIP {} from the list", aipReport.getOutcomeObjectId());
            oneTransferredResourceAipFailed = true;
            break;
          } else {
            transferredResourceAips.add(aipId);
          }
        }

        if (oneTransferredResourceAipFailed) {
          LOGGER.info(
            "Will not process AIPs from transferred resource '{}' any longer because at least one of them failed",
            transferredResourceId);
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          jobPluginInfo.failOtherTransferredResourceAIPs(bundle.getModel(), bundle.getIndex(), transferredResourceId);
          transferredResourcesToRemoveFromjobPluginInfo.add(transferredResourceId);
        } else {
          aipsToReturn.addAll(transferredResourceAips);
        }
      }
    }

    if (step.isMandatory() && step.removesAips()) {
      for (String transferredResourceId : transferredResourcesToRemoveFromjobPluginInfo) {
        jobPluginInfo.remove(transferredResourceId);
      }
    }

    for (String aipId : aipsToReturn) {
      try {
        bundle.getAips().add(bundle.getModel().retrieveAIP(aipId));
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
        LOGGER.error("Error while retrieving AIP", e);
      }
    }
  }

  public static void updateAIPsToBeAppraised(IngestStepBundle bundle, Job cachedJob) {
    for (AIP aip : bundle.getAips()) {
      aip.setState(AIPState.UNDER_APPRAISAL);
      try {
        aip = bundle.getModel().updateAIPState(aip, cachedJob.getUsername());

        bundle.getParameterValues().put(RodaConstants.PLUGIN_PARAMS_OUTCOMEOBJECTID_TO_SOURCEOBJECTID_MAP,
          JsonUtils.getJsonFromObject(bundle.getJobPluginInfo().getAipIdToTransferredResourceIds()));

        // update main report outcomeObjectState
        PluginHelper.updateJobReportState(bundle.getIngestPlugin(), bundle.getModel(), aip.getIngestSIPUUID(),
          aip.getId(), AIPState.UNDER_APPRAISAL, cachedJob);

        // update counters of manual intervention
        bundle.getJobPluginInfo().incrementOutcomeObjectsWithManualIntervention();
      } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
        LOGGER.error("Error while updating AIP state to '{}'. Reason: {}", AIPState.UNDER_APPRAISAL, e.getMessage());
      }
    }
  }
}
