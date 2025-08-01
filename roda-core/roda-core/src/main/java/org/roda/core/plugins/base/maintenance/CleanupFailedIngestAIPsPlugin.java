/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.maintenance;

import org.apache.commons.lang.StringUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.IndexedJob;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CleanupFailedIngestAIPsPlugin extends AbstractPlugin<Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(CleanupFailedIngestAIPsPlugin.class);

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
    return "Remove AIP(s) whose ingest failed";
  }

  @Override
  public String getDescription() {
    return "Permanently removes AIP(s) from the repository whose ingest failed (state="
      + AIPState.INGEST_PROCESSING.toString()
      + "). Data, metadata and event history will be deleted permanently. WARNING: This operation cannot be undone. Use with extreme caution.";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Report execute(IndexService index, ModelService model,
    List<LiteOptionalWithCause> objects) throws PluginException {
    return PluginHelper.processVoids(this, new RODAProcessingLogic<Void>() {
      @Override
      public void process(IndexService index, ModelService model, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<Void> plugin) {
        try {
          processAIPs(index, model, report, cachedJob, jobPluginInfo);
        } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
          LOGGER.error("Could not update Job information");
        }
      }
    }, index, model);
  }

  private void processAIPs(IndexService index, ModelService model, Report report, Job job, JobPluginInfo jobPluginInfo)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    // jobs that are running
    List<String> activeJobsIds = findActiveJobs(index);
    // remove self
    activeJobsIds.remove(job.getId());

    // find & delete aips
    try (IterableIndexResult<IndexedAIP> aipsToDelete = findAipsToDelete(index, activeJobsIds)) {
      for (IndexedAIP indexedAIP : aipsToDelete) {
        String error = "";
        try {
          LOGGER.debug("Removing unwanted AIP {}", indexedAIP.getId());
          model.deleteAIP(indexedAIP.getId());
        } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
          error = e.getMessage();
        }

        Report reportItem = PluginHelper.initPluginReportItem(this, indexedAIP.getId(), AIP.class,
          AIPState.INGEST_PROCESSING);
        if (StringUtils.isNotBlank(error)) {
          reportItem.setPluginState(PluginState.FAILURE)
            .setPluginDetails("Removal of AIP " + indexedAIP.getId() + " did not end successfully: " + error);
          jobPluginInfo.incrementObjectsProcessedWithFailure();
        } else {
          reportItem.setPluginState(PluginState.SUCCESS)
            .setPluginDetails("Removal of AIP " + indexedAIP.getId() + " ended successfully");
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
        }
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
      }

      jobPluginInfo.setSourceObjectsCount((int) aipsToDelete.getTotalCount());
    } catch (IOException e) {
      LOGGER.error("Error getting AIPs to delete", e);
    }
  }

  private List<String> findActiveJobs(IndexService index) {
    Filter activeJobsViaStateFilter = new Filter();
    for (Job.JOB_STATE jobState : Job.JOB_STATE.values()) {
      if (Job.isFinalState(jobState)) {
        activeJobsViaStateFilter.add(new NotSimpleFilterParameter(RodaConstants.JOB_STATE, jobState.toString()));
      }
    }

    List<String> activeJobsIds = new ArrayList<>();
    try (IterableIndexResult<IndexedJob> result = index.findAll(IndexedJob.class, activeJobsViaStateFilter,
      Arrays.asList(RodaConstants.INDEX_UUID))) {
      result.forEach(e -> activeJobsIds.add(e.getId()));
    } catch (IOException | GenericException | RequestNotValidException e) {
      LOGGER.error("Error getting jobs when finding active ones", e);
    }

    return activeJobsIds;
  }

  private IterableIndexResult<IndexedAIP> findAipsToDelete(IndexService index, List<String> activeJobsIds)
    throws GenericException, RequestNotValidException {
    Filter aipsFilter = new Filter();
    // all aips whose job id is not one of the active job ids
    activeJobsIds.forEach(e -> aipsFilter.add(new NotSimpleFilterParameter(RodaConstants.INGEST_JOB_ID, e)));
    // aip state is INGEST_PROCESSING
    aipsFilter.add(new SimpleFilterParameter(RodaConstants.AIP_STATE, AIPState.INGEST_PROCESSING.toString()));
    return index.findAll(IndexedAIP.class, aipsFilter, false, Arrays.asList(RodaConstants.INDEX_UUID));
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model) throws PluginException {
    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model) throws PluginException {
    return new Report();
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new CleanupFailedIngestAIPsPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.MISC;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.DELETION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Removes AIP from the system";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "The AIPs were successfully removed";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "The AIPs were not removed";
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_MANAGEMENT);
  }

  @Override
  public List<Class<Void>> getObjectClasses() {
    return Arrays.asList(Void.class);
  }
}
