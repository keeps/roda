package org.roda.core.plugins.plugins.internal.synchronization.bundle;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

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
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
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

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class CreateAipPackagePlugin extends CreateRodaEntityPackagePlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateAipPackagePlugin.class);

  @Override
  public String getName() {
    return "Create AIP Bundle";
  }

  @Override
  public String getVersionImpl() {
    return "1.0.0";
  }

  @Override
  protected String getEntity() {
    return "aip";
  }

  @Override
  protected void createBundle(IndexService index, ModelService model, Report pluginReport, JobPluginInfo jobPluginInfo,
    Job job) {
    pluginReport.setPluginState(PluginState.SUCCESS);

    SelectedItems<?> sourceObjects = job.getSourceObjects();

    if (sourceObjects instanceof SelectedItemsFilter) {
      Filter filter = ((SelectedItemsFilter) sourceObjects).getFilter();
      try {
        int counter = index.count(IndexedAIP.class, filter).intValue();

        jobPluginInfo.setSourceObjectsCount(counter);

        PackageState packageState = SyncBundleHelper.getPackageState(getEntity());
        packageState.setCount(counter);
        SyncBundleHelper.updatePackageState(getEntity(), packageState);

        IterableIndexResult<IndexedAIP> aips = index.findAll(IndexedAIP.class, filter,
          Arrays.asList(RodaConstants.INDEX_UUID));
        for (IndexedAIP aip : aips) {
          Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), IndexedAIP.class);
          AIP retrieveAIP = null;
          try {
            retrieveAIP = model.retrieveAIP(aip.getId());
            createAIPBundle(model, retrieveAIP);
            jobPluginInfo.incrementObjectsProcessedWithSuccess();
          } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
            LOGGER.error("Error on create bundle for aip {}", aip.getId());
            jobPluginInfo.incrementObjectsProcessedWithFailure();
            reportItem.addPluginDetails("Failed to create bundle for " + aip.getClass() + " " + aip.getId() + "\n");
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

  public void createAIPBundle(ModelService model, AIP aip) throws RequestNotValidException, NotFoundException,
    AuthorizationDeniedException, GenericException, AlreadyExistsException {

    StorageService storage = model.getStorage();
    StoragePath aipStoragePath = ModelUtils.getAIPStoragePath(aip.getId());
    Path destinationPath = getDestinationPath().resolve(RodaConstants.STORAGE_CONTAINER_AIP).resolve(aip.getId());

    Path documentationPath = destinationPath.resolve(RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION);
    Path metadataPath = destinationPath.resolve(RodaConstants.STORAGE_DIRECTORY_METADATA);
    Path schemasPath = destinationPath.resolve(RodaConstants.STORAGE_DIRECTORY_SCHEMAS);
    Path submissionsPath = destinationPath.resolve(RodaConstants.STORAGE_DIRECTORY_SUBMISSION);
    Path aipMetadataPath = destinationPath.resolve(RodaConstants.STORAGE_AIP_METADATA_FILENAME);

    storage.copy(storage, aipStoragePath, documentationPath, RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION);
    storage.copy(storage, aipStoragePath, metadataPath, RodaConstants.STORAGE_DIRECTORY_METADATA);
    storage.copy(storage, aipStoragePath, schemasPath, RodaConstants.STORAGE_DIRECTORY_SCHEMAS);
    storage.copy(storage, aipStoragePath, submissionsPath, RodaConstants.STORAGE_DIRECTORY_SUBMISSION);
    storage.copy(storage, aipStoragePath, aipMetadataPath, RodaConstants.STORAGE_AIP_METADATA_FILENAME);

    for (Representation representation : aip.getRepresentations()) {
      Path repMetadataPath = Paths.get(RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS, representation.getId(),
        RodaConstants.STORAGE_DIRECTORY_METADATA);
      Path representationsPath = destinationPath.resolve(repMetadataPath);
      storage.copy(storage, aipStoragePath, representationsPath, repMetadataPath.toString());
    }
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new CreateAipPackagePlugin();
  }
}
