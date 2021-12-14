package org.roda.core.plugins.plugins.internal.synchronization.packages;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.SyncUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.*;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.synchronization.bundle.BundleState;
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
import org.roda.core.plugins.plugins.internal.synchronization.SyncBundleHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public abstract class RodaEntityPackagesPlugin<T extends IsRODAObject> extends AbstractPlugin<Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RodaEntityPackagesPlugin.class);
  protected Date fromDate;
  protected Date toDate;
  protected Path bundlePath;
  protected BundleState bundleState;
  private LocalInstance localInstance;

  @Override
  public abstract String getVersionImpl();

  @Override
  public abstract String getName();

  @Override
  public String getDescription() {
    return "RodaEntityPackagesPlugin";
  }

  @Override
  public RodaConstants.PreservationEventType getPreservationEventType() {
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
  public abstract Plugin<Void> cloneMe();

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public void init() throws PluginException {

  }

  @Override
  public List<Class<Void>> getObjectClasses() {
    return Arrays.asList(Void.class);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    return null;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> list) throws PluginException {
    return PluginHelper.processVoids(this, new RODAProcessingLogic<Void>() {

      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<Void> plugin) throws PluginException {
        processEntity(index, model, report, jobPluginInfo, cachedJob);
      }
    }, index, model, storage);
  }

  protected void processEntity(IndexService index, ModelService model, Report report, JobPluginInfo jobPluginInfo,
    Job cachedJob) {
    PluginState state = PluginState.SUCCESS;
    String outcomeText = "There are no updates for the entity " + getEntity();
    Report reportItem = PluginHelper.initPluginReportItem(this, cachedJob.getId(), Job.class);
    PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);

    try {
      localInstance = RodaCoreFactory.getLocalInstance();
      bundleState = SyncUtils.getOutcomeBundleState(localInstance.getId());
      SyncUtils.createEntityPackageState(localInstance.getId(), getEntity());
      fromDate = bundleState.getFromDate();
      toDate = bundleState.getToDate();
      bundlePath = Paths.get(bundleState.getDestinationPath());
      List<String> packageList = retrieveList(index);
      if (!packageList.isEmpty()) {
        createPackage(index, model, packageList);
        updateEntityPackageState(getEntityClass(), packageList);
        state = PluginState.SUCCESS;
        outcomeText = "Package created with " + packageList.size() + " item(s) of entity " + getEntity();
      }
      jobPluginInfo.incrementObjectsProcessedWithSuccess();
    } catch (GenericException | IOException | AuthorizationDeniedException | RequestNotValidException
      | NotFoundException | AlreadyExistsException e) {
      LOGGER.error("Error on create package for entity " + getEntity(), e);
      state = PluginState.FAILURE;
      jobPluginInfo.incrementObjectsProcessedWithFailure();
      outcomeText = "Error on create package for entity " + getEntity();
    }

    reportItem.setPluginState(state).setPluginDetails(outcomeText);
    report.addReport(reportItem);
    PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
  }

  protected abstract String getEntity();

  protected abstract Class<T> getEntityClass();

  protected abstract List<String> retrieveList(IndexService index) throws RequestNotValidException, GenericException;

  protected abstract void createPackage(IndexService index, ModelService model, List<String> list)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException,
    AlreadyExistsException, IOException;

  protected void updateEntityPackageState(Class<T> entityClass, List<String> idList)
    throws NotFoundException, GenericException, IOException {
    PackageState entityPackageState = SyncUtils.getOutcomeEntityPackageState(localInstance.getId(), getEntity());
    entityPackageState.setClassName(entityClass);
    entityPackageState.setStatus(PackageState.Status.CREATED);
    entityPackageState.setIdList(idList);
    entityPackageState.setCount(idList.size());
    SyncUtils.updateEntityPackageState(localInstance.getId(), getEntity(), entityPackageState);
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return null;
  }

  @Override
  public void shutdown() {

  }
}
