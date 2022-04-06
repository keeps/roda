package org.roda.core.plugins.plugins.internal.synchronization.bundle;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
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
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
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
public class CreateRepositoryEventPackagePlugin extends CreateRodaEntityPackagePlugin<IndexedPreservationEvent> {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateRepositoryEventPackagePlugin.class);

  @Override
  public String getName() {
    return "Create Repository Preservation Event Bundle";
  }

  @Override
  public String getVersionImpl() {
    return "1.0.0";
  }

  @Override
  protected String getEntity() {
    return "preservation_event";
  }

  @Override
  protected String getEntityStoragePath() {
    return "preservation/event";
  }

  @Override
  protected void createBundle(IndexService index, ModelService model, Report pluginReport, JobPluginInfo jobPluginInfo,
    Job job) {
    pluginReport.setPluginState(PluginState.SUCCESS);

    SelectedItems<?> sourceObjects = job.getSourceObjects();

    if (sourceObjects instanceof SelectedItemsFilter) {
      Filter filter = ((SelectedItemsFilter) sourceObjects).getFilter();
      try {
        int counter = index.count(IndexedPreservationEvent.class, filter).intValue();
        jobPluginInfo.setSourceObjectsCount(counter);
        ArrayList<String> idList = new ArrayList<>();

        IterableIndexResult<IndexedPreservationEvent> events = index.findAll(IndexedPreservationEvent.class, filter,
          Arrays.asList(RodaConstants.INDEX_UUID));
        for (IndexedPreservationEvent event : events) {
          Report reportItem = PluginHelper.initPluginReportItem(this, event.getId(), IndexedPreservationEvent.class);
          PreservationMetadata retrieveEvent = null;
          try {
            retrieveEvent = model.retrievePreservationMetadata(event.getId(),
              PreservationMetadata.PreservationMetadataType.EVENT);
            createEventBundle(model, retrieveEvent);
            idList.add(retrieveEvent.getId());
            jobPluginInfo.incrementObjectsProcessedWithSuccess();
          } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
            LOGGER.error("Error on create bundle for repository event {}", retrieveEvent.getId());
            jobPluginInfo.incrementObjectsProcessedWithFailure();
            reportItem.addPluginDetails(
              "Failed to create bundle for " + retrieveEvent.getClass() + " " + retrieveEvent.getId() + "\n");
            reportItem.addPluginDetails(e.getMessage());
            pluginReport.addReport(reportItem.setPluginState(PluginState.FAILURE));
            PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
          }
        }
        updateEntityPackageState(IndexedPreservationEvent.class, idList);
      } catch (RODAException e) {
        LOGGER.error("Error on retrieve indexes of a RODA entity", e);
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      } catch (IOException e) {
        LOGGER.error("Error on update entity package state file", e);
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      }
    }
  }

  public void createEventBundle(ModelService model, PreservationMetadata event) throws RequestNotValidException,
    NotFoundException, AuthorizationDeniedException, GenericException, AlreadyExistsException {

    StorageService storage = model.getStorage();
    StoragePath eventStoragePath = ModelUtils.getPreservationRepositoryEventStoragePath();
    String eventFile = event.getId() + RodaConstants.PREMIS_SUFFIX;

    Path destinationPath = getDestinationPath().resolve(RodaConstants.CORE_STORAGE_FOLDER)
      .resolve(RodaConstants.STORAGE_CONTAINER_PRESERVATION).resolve(RodaConstants.STORAGE_DIRECTORY_EVENTS);

    Path eventPath = destinationPath.resolve(eventFile);

    storage.copy(storage, eventStoragePath, eventPath, eventFile);
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new CreateRepositoryEventPackagePlugin();
  }
}
