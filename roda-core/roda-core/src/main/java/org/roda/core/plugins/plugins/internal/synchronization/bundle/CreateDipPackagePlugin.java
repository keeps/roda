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
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.synchronization.bundle.PackageState;
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
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class CreateDipPackagePlugin extends CreateRodaEntityPackagePlugin<DIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateDipPackagePlugin.class);

  @Override
  public String getName() {
    return "Create DIP Bundle";
  }

  @Override
  public String getVersionImpl() {
    return "1.0.0";
  }

  @Override
  protected String getEntity() {
    return "dip";
  }

  @Override
  protected void createBundle(IndexService index, ModelService model, Report pluginReport, JobPluginInfo jobPluginInfo,
    Job job) {
    pluginReport.setPluginState(PluginState.SUCCESS);

    SelectedItems<?> sourceObjects = job.getSourceObjects();

    if (sourceObjects instanceof SelectedItemsFilter) {
      Filter filter = ((SelectedItemsFilter) sourceObjects).getFilter();
      try {
        int counter = index.count(IndexedDIP.class, filter).intValue();

        jobPluginInfo.setSourceObjectsCount(counter);

        PackageState packageState = SyncBundleHelper.getPackageState(getLocalInstance(), getEntity());
        packageState.setClassName(DIP.class);
        packageState.setCount(counter);
        SyncBundleHelper.updatePackageState(getLocalInstance(), getEntity(), packageState);

        IterableIndexResult<IndexedDIP> dips = index.findAll(IndexedDIP.class, filter,
          Arrays.asList(RodaConstants.INDEX_UUID));
        for (IndexedDIP dip : dips) {
          Report reportItem = PluginHelper.initPluginReportItem(this, dip.getId(), IndexedDIP.class);
          DIP retrieveDIP = null;
          try {
            retrieveDIP = model.retrieveDIP(dip.getId());
            createDIPBundle(model, retrieveDIP);
            packageState.addIdList(retrieveDIP.getId());
            SyncBundleHelper.updatePackageState(getLocalInstance(), getEntity(), packageState);
            jobPluginInfo.incrementObjectsProcessedWithSuccess();
          } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
            LOGGER.error("Error on create bundle for aip {}", dip.getId());
            jobPluginInfo.incrementObjectsProcessedWithFailure();
            reportItem.addPluginDetails("Failed to create bundle for " + dip.getClass() + " " + dip.getId() + "\n");
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

  public void createDIPBundle(ModelService model, DIP dip) throws RequestNotValidException, NotFoundException,
    AuthorizationDeniedException, GenericException, AlreadyExistsException {

    StorageService storage = model.getStorage();
    StoragePath dipStoragePath = ModelUtils.getDIPStoragePath(dip.getId());
    Path destinationPath = getDestinationPath().resolve(RodaConstants.CORE_STORAGE_FOLDER)
      .resolve(RodaConstants.STORAGE_CONTAINER_DIP).resolve(dip.getId());

    Path dipFilePath = destinationPath.resolve(RodaConstants.STORAGE_DIP_METADATA_FILENAME);

    storage.copy(storage, dipStoragePath, dipFilePath, RodaConstants.STORAGE_DIP_METADATA_FILENAME);

  }

  @Override
  public Plugin<Void> cloneMe() {
    return new CreateDipPackagePlugin();
  }
}
