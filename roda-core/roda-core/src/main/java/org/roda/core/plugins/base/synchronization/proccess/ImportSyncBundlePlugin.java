package org.roda.core.plugins.base.synchronization.proccess;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.roda.core.common.SyncUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.synchronization.bundle.v2.BundleManifest;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.base.synchronization.ImportUtils;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.internal.synchronization.BundleManifestCreator;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ImportSyncBundlePlugin extends AbstractPlugin<Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ImportSyncBundlePlugin.class);

  private String bundlePath = null;
  private String workingDir = null;
  private String instanceIdentifier = null;
  private int syncErrors = 0;

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_BUNDLE_WORKING_PATH)) {
      workingDir = parameters.get(RodaConstants.PLUGIN_PARAMS_BUNDLE_WORKING_PATH);
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_BUNDLE_PATH)) {
      bundlePath = parameters.get(RodaConstants.PLUGIN_PARAMS_BUNDLE_PATH);
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER)) {
      instanceIdentifier = parameters.get(RodaConstants.PLUGIN_PARAMS_INSTANCE_IDENTIFIER);
    }
    getParameterValues().put(RodaConstants.PLUGIN_PARAMS_REPORTING_CLASS, getClass().getName());
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public String getName() {
    return "Import sync bundle";
  }

  @Override
  public String getDescription() {
    return "Send the sync bundle to the central instance";
  }

  @Override
  public RodaConstants.PreservationEventType getPreservationEventType() {
    return RodaConstants.PreservationEventType.NONE;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Send the sync bundle to the central instance";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Sync bundle sent successfully";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Sync bundle not sent successfully";
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
    return new ImportSyncBundlePlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public void init() throws PluginException {

  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public List<Class<Void>> getObjectClasses() {
    return Arrays.asList(Void.class);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    return PluginHelper.processVoids(this, new RODAProcessingLogic<Void>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<Void> plugin) throws PluginException {
        importSyncBundle(model, index, storage, report, cachedJob, jobPluginInfo);
      }
    }, index, model, storage);
  }

  private void importSyncBundle(final ModelService model, final IndexService index, final StorageService storage,
    final Report report, final Job cachedJob, final JobPluginInfo jobPluginInfo) {
    if (Files.exists(Paths.get(bundlePath))) {
      try {
        DistributedInstance distributedInstance = model.retrieveDistributedInstance(instanceIdentifier);
        distributedInstance.cleanEntitySummaryList();

        SyncUtils.extract(Paths.get(workingDir), Paths.get(bundlePath));
        BundleManifestCreator bundleManifestCreator = new BundleManifestCreator(Paths.get(workingDir));
        BundleManifest manifestFile = bundleManifestCreator.parse();

        // Import entities
        ImportUtils.importStorage(model, index, storage, Paths.get(workingDir), true);
        // importStorage(model, index, storage, cachedJob, Paths.get(workingDir),
        // jobPluginInfo, report);
        ImportUtils.importAttachments(Paths.get(workingDir), manifestFile);

        // Delete entities
        ImportUtils.deleteBundleEntities(model, index, cachedJob, this, jobPluginInfo, distributedInstance,
          Paths.get(workingDir), manifestFile.getValidationEntityList(), report);

        // Validate entities
        ImportUtils.validateEntitiesBundle(index, Paths.get(workingDir), manifestFile.getValidationEntityList(),
          distributedInstance, syncErrors);

        distributedInstance.setLastSynchronizationDate(manifestFile.getToDate());

        ImportUtils.updateEntityCounter(manifestFile, distributedInstance);

        ImportUtils.createLastSyncFile(Paths.get(workingDir), distributedInstance, cachedJob.getId(),
          manifestFile.getId());

        model.updateDistributedInstance(distributedInstance, cachedJob.getUsername());
        report.setPluginState(PluginState.SUCCESS).setPluginDetails("Bundle imported successfully");
      } catch (IOException e) {
        LOGGER.error("Error extracting bundle to {}", workingDir, e);
        report.setPluginState(PluginState.FAILURE).setPluginDetails("Error extracting bundle to " + workingDir);
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      } catch (RODAException e) {
        LOGGER.error("Error creating temporary StorageService on {}", workingDir, e);
        report.setPluginState(PluginState.FAILURE)
          .setPluginDetails("Error creating temporary StorageService on " + workingDir);
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      }
    } else {
      report.setPluginState(PluginState.FAILURE).setPluginDetails("Cannot find bundle on path " + bundlePath);
      jobPluginInfo.incrementObjectsProcessedWithFailure();
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> list) throws PluginException {
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return new Report();
  }
}
