package org.roda.core.plugins.plugins.internal.synchronization.bundle;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.synchronization.bundle.PackageState;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

public class CreateJobPackagePlugin extends CreateRodaEntityPackagePlugin<Job> {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateJobPackagePlugin.class);

  @Override
  public String getName() {
    return "Create Job Bundle";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  protected String getEntity() {
    return "job";
  }

  @Override
  protected void createBundle(IndexService index, ModelService model, Report pluginReport, JobPluginInfo jobPluginInfo,
    Job job) {
    pluginReport.setPluginState(PluginState.SUCCESS);

    SelectedItems<?> sourceObjects = job.getSourceObjects();

    if (sourceObjects instanceof SelectedItemsFilter) {
      Filter filter = ((SelectedItemsFilter) sourceObjects).getFilter();
      try {
        int counter = index.count(Job.class, filter).intValue();

        jobPluginInfo.setSourceObjectsCount(counter);

        PackageState packageState = SyncBundleHelper.getPackageState(getLocalInstance(), getEntity());
        packageState.setClassName(Job.class);
        packageState.setCount(counter);
        SyncBundleHelper.updatePackageState(getLocalInstance(), getEntity(), packageState);

        IterableIndexResult<Job> jobs = index.findAll(Job.class, filter, Arrays.asList(RodaConstants.INDEX_UUID));
        for (Job jobToBundle : jobs) {
          Report reportItem = PluginHelper.initPluginReportItem(this, jobToBundle.getId(), Job.class);
          Job retrieveJob = null;
          try {
            retrieveJob = model.retrieveJob(jobToBundle.getId());
            createJobBundle(model, retrieveJob);
            packageState.addIdList(retrieveJob.getId());
            SyncBundleHelper.updatePackageState(getLocalInstance(), getEntity(), packageState);
            jobPluginInfo.incrementObjectsProcessedWithSuccess();
          } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
            LOGGER.error("Error on create bundle for job {}", jobToBundle.getId());
            jobPluginInfo.incrementObjectsProcessedWithFailure();
            reportItem.addPluginDetails(
              "Failed to create bundle for " + jobToBundle.getClass() + " " + jobToBundle.getId() + "\n");
            reportItem.addPluginDetails(e.getMessage());
            pluginReport.addReport(reportItem.setPluginState(PluginState.FAILURE));
            PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
          }
        }
      } catch (RODAException e) {
        LOGGER.error("Error on retrieve indexes of a RODA entity", e);
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      }
    }
  }

  public void createJobBundle(ModelService model, Job jobToBundle) throws RequestNotValidException, NotFoundException,
    AuthorizationDeniedException, GenericException, AlreadyExistsException {

    StorageService storage = model.getStorage();
    StoragePath jobContainerPath = ModelUtils.getJobContainerPath();
    String jobFile = jobToBundle.getId() + RodaConstants.JOB_FILE_EXTENSION;

    Path destinationPath = getDestinationPath().resolve(RodaConstants.CORE_STORAGE_FOLDER)
      .resolve(RodaConstants.STORAGE_CONTAINER_JOB);

    Path jobPath = destinationPath.resolve(jobFile);

    storage.copy(storage, jobContainerPath, jobPath, jobFile);

    // Job Report
    StoragePath jobReportsPath = ModelUtils.getJobReportContainerPath();
    if (storage.exists(jobReportsPath)) {
      Path jobReportDestinationPath = getDestinationPath().resolve(RodaConstants.CORE_STORAGE_FOLDER)
        .resolve(RodaConstants.STORAGE_CONTAINER_JOB_REPORT).resolve(jobToBundle.getId());

      storage.copy(storage, jobReportsPath, jobReportDestinationPath, jobToBundle.getId());
    }

  }

  @Override
  public Plugin<Void> cloneMe() {
    return new CreateJobPackagePlugin();
  }
}
