package org.roda.core.plugins.plugins.internal.synchronization.proccess;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
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
import org.roda.core.plugins.plugins.internal.synchronization.BundleManifestCreator;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class BuildSyncManifestPlugin extends AbstractPlugin<Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(BuildSyncManifestPlugin.class);
  private Path workingDir;
  private String bundleName;
  private Date fromDate;
  private Date toDate;

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    try {
      if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_BUNDLE_WORKING_PATH)) {
        workingDir = Paths.get(parameters.get(RodaConstants.PLUGIN_PARAMS_BUNDLE_WORKING_PATH));
      }
      if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_BUNDLE_NAME)) {
        bundleName = parameters.get(RodaConstants.PLUGIN_PARAMS_BUNDLE_NAME);
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
  public String getVersionImpl() {
    return "1.0";
  }

  public static String getStaticName() {
    return "BuildSyncManifestPlugin";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "Creates manifest sync bundle based on packages";
  }

  @Override
  public String getDescription() {
    return getStaticDescription();
  }

  @Override
  public RodaConstants.PreservationEventType getPreservationEventType() {
    return RodaConstants.PreservationEventType.NONE;
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
  public Plugin<Void> cloneMe() {
    return new BuildSyncManifestPlugin();
  }

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
        processBundle(index, model, storage, report, jobPluginInfo, cachedJob);
      }
    }, index, model, storage);
  }

  private void processBundle(IndexService index, ModelService model, StorageService storage, Report report,
    JobPluginInfo jobPluginInfo, Job cachedJob) throws PluginException {
    Report reportItem = PluginHelper.initPluginReportItem(this, cachedJob.getId(), Job.class);
    PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
    PluginState pluginState = PluginState.SKIPPED;
    String pluginDetails = "";

    try {
      BundleManifestCreator manifestCreator = new BundleManifestCreator(RodaConstants.DistributedModeType.LOCAL,
        workingDir, fromDate, toDate);
      manifestCreator.create();
      pluginState = PluginState.SUCCESS;
      jobPluginInfo.incrementObjectsProcessed(pluginState);

    } catch (GenericException e) {
      jobPluginInfo.incrementObjectsProcessedWithFailure();
      pluginDetails = "Unable to get Local instance configuration: " + e.getMessage();
      pluginState = PluginState.FAILURE;
    } catch (NotFoundException | IOException e) {
      jobPluginInfo.incrementObjectsProcessedWithFailure();
      pluginDetails = "Unable to create bundle manifest: " + e.getMessage();
      pluginState = PluginState.FAILURE;
    }

    reportItem.setPluginState(pluginState).setPluginDetails(pluginDetails);
    report.addReport(reportItem);
    PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return null;
  }

  @Override
  public void shutdown() {

  }
}
