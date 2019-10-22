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
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.orchestrate.JobsHelper;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleanUnfinishedJobsPlugin extends AbstractPlugin<Job> {
  private static final Logger LOGGER = LoggerFactory.getLogger(CleanUnfinishedJobsPlugin.class);

  @Override
  public String getName() {
    return "Clean unfinished jobs";
  }

  @Override
  public String getDescription() {
    return "Cleans unfinished jobs (and the data they have created)";
  }

  @Override
  public String getVersionImpl() {
    return "1.0.0";
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    try {
      // make sure the index is up to date
      index.commit(IndexedAIP.class);
    } catch (GenericException | AuthorizationDeniedException e) {
      // do nothing
    }
    return null;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return PluginHelper.processObjects(this, new RODAObjectsProcessingLogic<Job>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<Job> plugin, List<Job> objects) {
        cleanUnfinishedJobs(model, index, objects, plugin, report, jobPluginInfo);
      }
    }, index, model, storage, liteList);
  }

  private void cleanUnfinishedJobs(ModelService model, IndexService index, List<Job> unfinishedJobsList,
    Plugin<Job> plugin, Report pluginReport, JobPluginInfo jobPluginInfo) {
    Report reportItem = null;

    List<String> jobsToBeDeletedFromIndex = new ArrayList<>();
    for (Job job : unfinishedJobsList) {
      reportItem = PluginHelper.initPluginReportItem(plugin, job.getId(), Job.class);
      PluginHelper.updatePartialJobReport(plugin, model, reportItem, false, job);

      try {
        // cleanup job related objects (aips, sips, etc.)
        JobsHelper.cleanJobObjects(job, model, index);

        // only after deleting all the objects, delete the job
        model.createOrUpdateJob(JobsHelper.updateJobInTheStateStartedOrCreated(job));

        jobPluginInfo.incrementObjectsProcessedWithSuccess();
        reportItem.setPluginState(PluginState.SUCCESS);
      } catch (NotFoundException e) {
        jobsToBeDeletedFromIndex.add(job.getId());
      } catch (RequestNotValidException | GenericException | AuthorizationDeniedException e) {
        LOGGER.error("Unable to get/update Job", e);
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        reportItem.setPluginState(PluginState.FAILURE);
      } finally {
        try {
          PluginHelper.updateJobInformationAsync(plugin, jobPluginInfo);
        } catch (JobException e) {
          // do nothing
        }
        pluginReport.addReport(reportItem);
        PluginHelper.updatePartialJobReport(plugin, model, reportItem, true, job);
      }
    }

    if (!jobsToBeDeletedFromIndex.isEmpty()) {
      index.deleteSilently(Job.class, jobsToBeDeletedFromIndex);
    }
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return null;
  }

  @Override
  public String getPreservationEventDescription() {
    return null;
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return null;
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return null;
  }

  @Override
  public PluginType getType() {
    return PluginType.INTERNAL;
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public Plugin<Job> cloneMe() {
    return new CleanUnfinishedJobsPlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public List<Class<Job>> getObjectClasses() {
    List<Class<? extends IsRODAObject>> list = new ArrayList<>();
    list.add(Job.class);
    return (List) list;
  }

  @Override
  public void shutdown() {
    // do nothing
  }

}
