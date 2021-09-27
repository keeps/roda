package org.roda.core.plugins.plugins.internal.synchronization.bundle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.filter.DateIntervalFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.synchronization.bundle.BundleState;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.plugins.plugins.internal.synchronization.SynchronizationHelper;
import org.roda.core.storage.StorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class CreateSyncBundlePlugin extends AbstractPlugin<Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateSyncBundlePlugin.class);
  private BundleState bundleState;

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
        createBundle(model, report, jobPluginInfo, cachedJob, classes);
      }
    }, index, model, storage, classes.size());
  }

  private void createBundle(ModelService model, Report pluginReport, JobPluginInfo jobPluginInfo, Job job,
    List<Class<? extends IsRODAObject>> classes) {

    for (Class<? extends IsRODAObject> bundleClass : classes) {
      Report reportItem = createBundleForClass(model, bundleClass, jobPluginInfo);
      if (reportItem != null) {
        pluginReport.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
      }
    }

    pluginReport.setPluginState(PluginState.SUCCESS);
  }

  private Report createBundleForClass(ModelService model, Class<? extends IsRODAObject> bundleClass,
    JobPluginInfo jobPluginInfo) {
    String jobId = IdUtils.createUUID();
    String jobName = "Create bundle of RODA entity (" + bundleClass.getSimpleName() + ")";
    Report reportItem = PluginHelper.initPluginReportItem(this, jobId, Job.class);
    if (model.hasObjects(bundleClass)) {
      try {
        String username = PluginHelper.getJobUsername(this, model);
        Job job = initBundleJob(bundleClass, jobId, jobName, username);
        PluginHelper.createAndExecuteJob(job);
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
        reportItem.setPluginState(PluginState.SUCCESS).setPluginDetails(jobName + " ran successfully");
      } catch (RODAException e) {
        LOGGER.error("Error creating job to create bundle of {}", bundleClass.getSimpleName(), e);
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(jobName + " did not run successfully");
      }
    } else {
      reportItem.setPluginState(PluginState.SKIPPED)
        .setPluginDetails("There are no entities(" + bundleClass.getSimpleName() + ") to be synchronized");
      jobPluginInfo.incrementObjectsProcessedWithSuccess();
    }
    return reportItem;
  }

  private Job initBundleJob(Class<? extends IsRODAObject> bundleClass, String jobId, String jobName, String username)
    throws NotFoundException {
    Job job = new Job();
    job.setId(jobId);
    job.setName(jobName);

    job.setPluginType(PluginType.INTERNAL);
    job.setUsername(username);

    job.setPlugin(PluginHelper.getBundlePluginName(bundleClass));
    job.setSourceObjects(getSelectItems(bundleClass, bundleState.getFromDate(), bundleState.getToDate()));

    return job;
  }

  public SelectedItemsFilter getSelectItems(Class<?> bundleClass, Date initialDate, Date finalDate)
    throws NotFoundException {
    Filter filter = new Filter();
    if (bundleClass.equals(AIP.class)) {
      if (initialDate != null) {
        filter.add(new DateIntervalFilterParameter(RodaConstants.AIP_UPDATED_ON, RodaConstants.AIP_UPDATED_ON,
          initialDate, finalDate));
      }
      return new SelectedItemsFilter(filter, IndexedAIP.class.getName(), false);
    } else if (bundleClass.equals(DIP.class)) {
      if (initialDate != null) {
        filter.add(new DateIntervalFilterParameter(RodaConstants.DIP_LAST_MODIFIED, RodaConstants.DIP_LAST_MODIFIED,
          initialDate, finalDate));
      }
      return new SelectedItemsFilter(filter, IndexedDIP.class.getName(), false);
    } else if (bundleClass.equals(RiskIncidence.class)) {
      if (initialDate != null) {
        filter.add(new DateIntervalFilterParameter(RodaConstants.RISK_INCIDENCE_DETECTED_ON,
          RodaConstants.RISK_INCIDENCE_DETECTED_ON, initialDate, finalDate));
      }
      return new SelectedItemsFilter(filter, RiskIncidence.class.getName(), false);
    } else if (bundleClass.equals(Job.class)) {
      List<FilterParameter> parameters = new ArrayList<>();
      parameters.add(new NotSimpleFilterParameter(RodaConstants.JOB_PLUGIN_TYPE, PluginType.INTERNAL.toString()));
      if (initialDate != null) {
        parameters.add(new DateIntervalFilterParameter(RodaConstants.JOB_START_DATE, RodaConstants.JOB_END_DATE,
          initialDate, finalDate));
      }
      filter.add(parameters);
      return new SelectedItemsFilter(filter, Job.class.getName(), false);
    } else if (bundleClass.equals(IndexedPreservationEvent.class)) {
      List<FilterParameter> parameters = new ArrayList<>();
      parameters.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_OBJECT_CLASS,
        IndexedPreservationEvent.PreservationMetadataEventClass.REPOSITORY.toString()));
      if (initialDate != null) {
        parameters.add(new DateIntervalFilterParameter(RodaConstants.PRESERVATION_EVENT_DATETIME,
          RodaConstants.PRESERVATION_EVENT_DATETIME, initialDate, finalDate));
      }
      filter.add(parameters);
      return new SelectedItemsFilter(filter, IndexedPreservationEvent.class.getName(), false);
    } else if (bundleClass.equals(IndexedPreservationAgent.class)) {
      return new SelectedItemsFilter(filter, IndexedPreservationAgent.class.getName(), false);
    } else {
      throw new NotFoundException("No Bundle plugin available");
    }
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    try {
      bundleState = SynchronizationHelper.createBundleStateFile();
    } catch (RODAException | IOException e) {
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
