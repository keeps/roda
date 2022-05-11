package org.roda.core.plugins.plugins.internal.synchronization.packages;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.filter.DateIntervalFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class JobPackagePlugin extends RodaEntityPackagesPlugin<Job> {
  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  public static String getStaticName() {
    return "JobPackagePlugin";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "";
  }

  @Override
  public String getDescription() {
    return getStaticDescription();
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new JobPackagePlugin();
  }

  @Override
  protected String getEntity() {
    return "job";
  }

  @Override
  protected Class<Job> getEntityClass() {
    return Job.class;
  }

  @Override
  protected List<IterableIndexResult> retrieveList(IndexService index)
    throws RequestNotValidException, GenericException {
    Filter filter = new Filter();
    filter.add(new NotSimpleFilterParameter(RodaConstants.JOB_PLUGIN_TYPE, PluginType.INTERNAL.toString()));
    filter.add(new NotSimpleFilterParameter(RodaConstants.JOB_STATE, Job.JOB_STATE.CREATED.name()));
    filter.add(new NotSimpleFilterParameter(RodaConstants.JOB_STATE, Job.JOB_STATE.PENDING_APPROVAL.name()));
    if (fromDate != null) {
      filter.add(
        new DateIntervalFilterParameter(RodaConstants.JOB_START_DATE, RodaConstants.JOB_END_DATE, fromDate, toDate));
    }
    return Arrays.asList(index.findAll(Job.class, filter, Collections.singletonList(RodaConstants.INDEX_UUID)));
  }

  @Override
  protected void createPackage(IndexService index, ModelService model, IterableIndexResult objectList)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException,
    AlreadyExistsException {
    for (Object object : objectList) {
      if (object instanceof Job) {
        Job job = model.retrieveJob(((Job) object).getId());
        createJobBundle(model, job);
      }
    }
  }

  public void createJobBundle(ModelService model, Job jobToBundle) throws RequestNotValidException, NotFoundException,
    AuthorizationDeniedException, GenericException, AlreadyExistsException {

    StorageService storage = model.getStorage();
    StoragePath jobContainerPath = ModelUtils.getJobContainerPath();
    String jobFile = jobToBundle.getId() + RodaConstants.JOB_FILE_EXTENSION;

    Path destinationPath = workingDirPath.resolve(RodaConstants.CORE_STORAGE_FOLDER)
      .resolve(RodaConstants.STORAGE_CONTAINER_JOB);

    Path jobPath = destinationPath.resolve(jobFile);

    storage.copy(storage, jobContainerPath, jobPath, jobFile);

    // Job Report
    StoragePath jobReportsPath = ModelUtils.getJobReportContainerPath();
    if (storage.exists(jobReportsPath)) {
      Path jobReportDestinationPath = workingDirPath.resolve(RodaConstants.CORE_STORAGE_FOLDER)
        .resolve(RodaConstants.STORAGE_CONTAINER_JOB_REPORT).resolve(jobToBundle.getId());

      storage.copy(storage, jobReportsPath, jobReportDestinationPath, jobToBundle.getId());
    }

    // Job Attachments
    addAttachmentToBundle(jobToBundle);

  }

  private void addAttachmentToBundle(Job job) throws AlreadyExistsException, GenericException {
    Path jobAttachmentDirectoryPath = RodaCoreFactory.getJobAttachmentsDirectoryPath().resolve(job.getId());
    if (FSUtils.exists(jobAttachmentDirectoryPath)) {
      Path jobAttachmentDestinationPath = workingDirPath.resolve(RodaConstants.CORE_JOB_ATTACHMENTS_FOLDER)
        .resolve(job.getId());
      FSUtils.copy(jobAttachmentDirectoryPath, jobAttachmentDestinationPath, true);
    }
  }
}
