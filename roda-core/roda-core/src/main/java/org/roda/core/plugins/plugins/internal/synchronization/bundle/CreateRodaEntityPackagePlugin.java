package org.roda.core.plugins.plugins.internal.synchronization.bundle;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobStats;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.synchronization.bundle.PackageState;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
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

  protected abstract String getEntityStoragePath();

  protected Path getDestinationPath() throws GenericException {
    return Paths.get(getLocalInstance().getBundlePath());
  }

  protected LocalInstance getLocalInstance() throws GenericException {
    return RodaCoreFactory.getLocalInstance();
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
    try {
      SynchronizationHelper.createEntityPackageState(getEntity());
    } catch (GenericException | IOException e) {
      throw new PluginException("Cannot create entity package state file", e);
    }
    return new Report();
  }

  protected void updateEntityPackageState(Class<T> entityClass, ArrayList<String> idList)
    throws NotFoundException, GenericException, IOException {
    PackageState entityPackageState = SynchronizationHelper.getEntityPackageState(getEntity());
    entityPackageState.setClassName(entityClass);
    entityPackageState.setStatus(PackageState.Status.CREATED);
    entityPackageState.setIdList(idList);
    entityPackageState.setCount(idList.size());
    SynchronizationHelper.updateEntityPackageState(getEntity(), entityPackageState);
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    try {
      Job job = PluginHelper.getJob(this, model);
      JobStats jobStats = job.getJobStats();
      PackageState entityPackageState = SynchronizationHelper.getEntityPackageState(getEntity());
      if (jobStats.getSourceObjectsProcessedWithFailure() == 0) {
        String entityTopHash = SynchronizationHelper.calculateEntityTopHash(getEntity());
        entityPackageState.setChecksum(entityTopHash);
        entityPackageState.setStatus(PackageState.Status.SUCCESS);
      } else {
        entityPackageState.setStatus(PackageState.Status.FAILED);
      }
      SynchronizationHelper.updateEntityPackageState(getEntity(), entityPackageState);
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException
      | IOException e) {
      throw new PluginException("Error on retrieve job status", e);
    }
    return new Report();
  }
}
