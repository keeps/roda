package org.roda.core.plugins.plugins.internal.synchronization.bundle;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.distributedInstance.LocalInstance;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobStats;
import org.roda.core.data.v2.jobs.PluginParameter;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public abstract class CreateRodaEntityPackagePlugin<T extends IsRODAObject> extends AbstractPlugin<Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateRodaEntityPackagePlugin.class);

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public abstract String getName();

  @Override
  public String getDescription() {
    return "Create compressed entity bundles to be synchronized with the central instance";
  }

  @Override
  public abstract String getVersionImpl();

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return PluginHelper.processVoids(this, new RODAProcessingLogic<Void>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<Void> plugin) {
        createBundle(index, model, report, jobPluginInfo, cachedJob);
      }
    }, index, model, storage);
  }

  protected abstract String getEntity();

  protected Path getDestinationPath() throws GenericException {
    return Paths.get(getLocalInstance().getBundlePath());
  }

  protected LocalInstance getLocalInstance() throws GenericException {
    return RodaCoreFactory.getLocalInstance();
  }

  protected void updatePackageState(PackageState.Status status) throws PluginException {
    try {
      SyncBundleHelper.updatePackageStateStatus(getLocalInstance(), getEntity(), status);
    } catch (GenericException e) {
      throw new PluginException("Error while creating entity bundle state", e);
    }
  }

  protected abstract void createBundle(IndexService index, ModelService model, Report report,
    JobPluginInfo jobPluginInfo, Job cachedJob);

  @Override
  public abstract Plugin<Void> cloneMe();

  @Override
  public PluginType getType() {
    return PluginType.INTERNAL;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public RodaConstants.PreservationEventType getPreservationEventType() {
    return RodaConstants.PreservationEventType.NONE;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Sync Bundle Roda entity";
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
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public List<Class<Void>> getObjectClasses() {
    return Arrays.asList(Void.class);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    updatePackageState(PackageState.Status.CREATED);
    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    try {
      Job job = PluginHelper.getJob(this, model);
      JobStats jobStats = job.getJobStats();
      if (jobStats.getSourceObjectsProcessedWithFailure() == 0) {
        updatePackageState(PackageState.Status.SUCCESS);
        SyncBundleHelper.executeShaSumCommand(getLocalInstance(), getEntity());
      } else {
        updatePackageState(PackageState.Status.FAILED);
      }
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      throw new PluginException("Error on retrieve job status", e);
    }
    return new Report();
  }
}
