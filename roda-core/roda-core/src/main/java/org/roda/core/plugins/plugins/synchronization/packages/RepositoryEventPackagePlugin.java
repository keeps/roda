package org.roda.core.plugins.plugins.synchronization.packages;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.*;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.filter.DateIntervalFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.storage.StorageService;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class RepositoryEventPackagePlugin extends RodaEntityPackagesPlugin<IndexedPreservationEvent> {
  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public String getName() {
    return "RepositoryEventPackagePlugin";
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new RepositoryEventPackagePlugin();
  }

  @Override
  protected String getEntity() {
    return "preservation_event";
  }

  @Override
  protected Class<IndexedPreservationEvent> getEntityClass() {
    return IndexedPreservationEvent.class;
  }

  @Override
  protected List<String> retrieveList(IndexService index) throws RequestNotValidException, GenericException {
    ArrayList<String> eventList = new ArrayList<>();
    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_OBJECT_CLASS,
      IndexedPreservationEvent.PreservationMetadataEventClass.REPOSITORY.toString()));
    if (fromDate != null) {
      filter.add(new DateIntervalFilterParameter(RodaConstants.PRESERVATION_EVENT_DATETIME,
        RodaConstants.PRESERVATION_EVENT_DATETIME, fromDate, toDate));
    }
    IterableIndexResult<IndexedPreservationEvent> events = index.findAll(IndexedPreservationEvent.class, filter,
      Arrays.asList(RodaConstants.INDEX_UUID));

    for (IndexedPreservationEvent event : events) {
      eventList.add(event.getId());
    }
    return eventList;
  }

  @Override
  protected void createPackage(IndexService index, ModelService model, List<String> list) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException, NotFoundException, AlreadyExistsException, IOException {
    for (String eventId : list) {
      PreservationMetadata retrieveEvent = model.retrievePreservationMetadata(eventId,
        PreservationMetadata.PreservationMetadataType.EVENT);
      createEventBundle(model, retrieveEvent);
    }
  }

  public void createEventBundle(ModelService model, PreservationMetadata event) throws RequestNotValidException,
    NotFoundException, AuthorizationDeniedException, GenericException, AlreadyExistsException {

    StorageService storage = model.getStorage();
    StoragePath eventStoragePath = ModelUtils.getPreservationRepositoryEventStoragePath();
    String eventFile = event.getId() + RodaConstants.PREMIS_SUFFIX;

    Path destinationPath = bundlePath.resolve(RodaConstants.CORE_STORAGE_FOLDER)
      .resolve(RodaConstants.STORAGE_CONTAINER_PRESERVATION).resolve(RodaConstants.STORAGE_DIRECTORY_EVENTS);

    Path eventPath = destinationPath.resolve(eventFile);

    storage.copy(storage, eventStoragePath, eventPath, eventFile);
  }
}
