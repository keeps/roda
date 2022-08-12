/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.synchronization.packages;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.*;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
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
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.PluginHelper;
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
  protected Path workingDirPath;
  protected String workingDir;
  protected long totalCount;

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    try {
      if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_BUNDLE_WORKING_PATH)) {
        workingDir = parameters.get(RodaConstants.PLUGIN_PARAMS_BUNDLE_WORKING_PATH);
        workingDirPath = Paths.get(workingDir);
      }
      if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_BUNDLE_FROM_DATE)) {
        fromDate = JsonUtils.getObjectFromJson(parameters.get(RodaConstants.PLUGIN_PARAMS_BUNDLE_FROM_DATE),
          Date.class);
      }
      if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_BUNDLE_TO_DATE)) {
        toDate = JsonUtils.getObjectFromJson(parameters.get(RodaConstants.PLUGIN_PARAMS_BUNDLE_TO_DATE), Date.class);
      }
    } catch (GenericException e) {
      throw new InvalidParameterException(e);
    }
  }

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
      List<IterableIndexResult> indexResultList = retrieveList(index);

      for (IterableIndexResult indexResult : indexResultList) {
        addTotalCount(indexResult.getTotalCount());
        createPackage(index, model, indexResult);
      }

      state = PluginState.SUCCESS;
      outcomeText = "Package created with " + totalCount + " item(s) of entity " + getEntity();
      jobPluginInfo.incrementObjectsProcessedWithSuccess();
    } catch (GenericException | AuthorizationDeniedException | RequestNotValidException | NotFoundException
      | AlreadyExistsException e) {
      LOGGER.error("Error on create package for entity " + getEntity(), e);
      state = PluginState.FAILURE;
      jobPluginInfo.incrementObjectsProcessedWithFailure();
      outcomeText = "Error on create package for entity " + getEntity();
    }

    reportItem.setPluginState(state).setPluginDetails(outcomeText);
    report.addReport(reportItem);
    PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
  }

  // AipPackagePlugin needs to override this method to avoid counting preservation
  // events.
  public void addTotalCount(long totalCount) {
    this.totalCount += totalCount;
  }

  protected abstract String getEntity();

  protected abstract Class<T> getEntityClass();

  protected abstract List<IterableIndexResult> retrieveList(IndexService index)
    throws RequestNotValidException, GenericException;

  protected abstract void createPackage(IndexService index, ModelService model, IterableIndexResult objectList)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException,
    AlreadyExistsException;

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return null;
  }

  @Override
  public void shutdown() {

  }
}
