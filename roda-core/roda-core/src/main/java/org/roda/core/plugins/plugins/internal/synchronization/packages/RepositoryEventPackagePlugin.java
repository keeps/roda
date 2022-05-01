package org.roda.core.plugins.plugins.internal.synchronization.packages;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
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
  protected List<IterableIndexResult> retrieveList(IndexService index)
    throws RequestNotValidException, GenericException {
    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_OBJECT_CLASS,
      IndexedPreservationEvent.PreservationMetadataEventClass.REPOSITORY.toString()));
    if (fromDate != null) {
      filter.add(new DateIntervalFilterParameter(RodaConstants.PRESERVATION_EVENT_DATETIME,
        RodaConstants.PRESERVATION_EVENT_DATETIME, fromDate, toDate));
    }
    return Arrays
      .asList(index.findAll(IndexedPreservationEvent.class, filter, Arrays.asList(RodaConstants.INDEX_UUID)));
  }

  @Override
  protected void createPackage(IndexService index, ModelService model, IterableIndexResult objectList)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException,
    AlreadyExistsException {
    for (Object object : objectList) {
      if (object instanceof IndexedPreservationEvent) {
        PreservationMetadata preservationMetadata = model.retrievePreservationMetadata(
          ((IndexedPreservationEvent) object).getId(), PreservationMetadata.PreservationMetadataType.EVENT);
        createEventBundle(model, preservationMetadata);
      }
    }
  }

  private void createEventBundle(ModelService model, PreservationMetadata event)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, AlreadyExistsException {
    StorageService storage = model.getStorage();
    StoragePath eventStoragePath = ModelUtils.getPreservationRepositoryEventStoragePath();
    String eventFile = event.getId() + RodaConstants.PREMIS_SUFFIX;

    Path destinationPath = workingDirPath.resolve(RodaConstants.CORE_STORAGE_FOLDER)
      .resolve(RodaConstants.STORAGE_CONTAINER_PRESERVATION).resolve(RodaConstants.STORAGE_DIRECTORY_EVENTS);

    Path eventPath = destinationPath.resolve(eventFile);

    storage.copy(storage, eventStoragePath, eventPath, eventFile);
  }
}
