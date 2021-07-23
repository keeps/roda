package org.roda.core.plugins.plugins.internal.synchronization.bundle;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.synchronization.bundle.BundleState;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class CreateSyncBundlePlugin extends AbstractPlugin<Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateSyncBundlePlugin.class);

  private LocalInstance localInstance;
  private BundleState bundleState;

  private Date finalDate = null;

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
    return "Create bundle over RODA entities";
  }

  @Override
  public String getDescription() {
    return "Create compressed entity bundles to be synchronized with the central instance";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> list) throws PluginException {
    final List<Class<? extends IsRODAObject>> classes = PluginHelper.getSynchronizedObjectClasses();
    return PluginHelper.processVoids(this, new RODAProcessingLogic<Void>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<Void> plugin) {
        createBundle(index, model, report, jobPluginInfo, cachedJob, classes);
      }
    }, index, model, storage, classes.size());
  }

  private void createBundle(IndexService index, ModelService model, Report pluginReport, JobPluginInfo jobPluginInfo,
    Job job, List<Class<? extends IsRODAObject>> classes) {

    finalDate = new Date();
    for (Class<? extends IsRODAObject> bundleClass : classes) {
      Report reportItem = createBundleForClass(index, model, bundleClass, jobPluginInfo, finalDate);
      if (reportItem != null) {
        pluginReport.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
      }
    }

    pluginReport.setPluginState(PluginState.SUCCESS);
  }

  private Report createBundleForClass(IndexService index, ModelService model, Class<? extends IsRODAObject> bundleClass,
    JobPluginInfo jobPluginInfo, Date finalDate) {
    Report report = null;
    if (model.hasObjects(bundleClass)) {
      String jobId = IdUtils.createUUID();
      String jobName = "Create bundle of RODA entity (" + bundleClass.getSimpleName() + ")";
      report = PluginHelper.initPluginReportItem(this, jobId, Job.class);

      try {
        String username = PluginHelper.getJobUsername(this, model);
        Job job = initBundleJob(index, bundleClass, jobId, jobName, username, finalDate);
        PluginHelper.createAndExecuteJob(job);
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
        report.setPluginState(PluginState.SUCCESS).setPluginDetails(jobName + " ran successfully");
      } catch (RODAException e) {
        LOGGER.error("Error creating job to create bundle of {}", bundleClass.getSimpleName(), e);
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        report.setPluginState(PluginState.FAILURE).setPluginDetails(jobName + " did not run successfully");
      }
    } else {
      jobPluginInfo.incrementObjectsProcessedWithSuccess();
    }
    return report;
  }

  private Job initBundleJob(IndexService index, Class<? extends IsRODAObject> bundleClass, String jobId, String jobName,
    String username, Date finalDate) throws NotFoundException {
    Job job = new Job();
    job.setId(jobId);
    job.setName(jobName);

    job.setPluginType(PluginType.INTERNAL);
    job.setUsername(username);

    job.setPlugin(PluginHelper.getBundlePluginName(bundleClass));
    job.setSourceObjects(
      SyncBundleHelper.getSelectItems(bundleClass, bundleState.getFromDate(), bundleState.getFromDate()));

    return job;
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    try {
      localInstance = RodaCoreFactory.getLocalInstance();
      bundleState = SyncBundleHelper.createBundleStateFile(localInstance);
    } catch (GenericException e) {
      throw new PluginException("Error while creating entity bundle state", e);
    }
    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return new Report();
  }

  @Override
  public RodaConstants.PreservationEventType getPreservationEventType() {
    return RodaConstants.PreservationEventType.NONE;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Create compressed entity bundles to be synchronized with the central instance";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Bundle was created successfully";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Bundle was not created successfully";
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
  public Plugin<Void> cloneMe() {
    return new CreateSyncBundlePlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public List<Class<Void>> getObjectClasses() {
    return Arrays.asList(Void.class);
  }
}
