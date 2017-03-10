package org.roda.core.model.utils;

import java.util.Iterator;

import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.iterables.CloseableIterables;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.model.iterables.AIPDescriptiveMetadataIterable;
import org.roda.core.model.iterables.AIPPreservationMetadataIterable;
import org.roda.core.model.iterables.DIPFileIterable;
import org.roda.core.model.iterables.FileIterable;
import org.roda.core.model.iterables.RepresentationDescriptiveMetadataIterable;
import org.roda.core.model.iterables.RepresentationIterable;
import org.roda.core.model.iterables.RepresentationPreservationMetadataIterable;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;

public class ResourceListUtils {

  private ResourceListUtils() {
    super();
  }

  public static CloseableIterable<Resource> listFileResources(StorageService storage) throws RODAException {
    final CloseableIterable<Resource> aipResources = storage
      .listResourcesUnderContainer(ModelUtils.getContainerPath(AIP.class), false);
    final Iterator<Resource> aipIterator = aipResources.iterator();
    return new FileIterable(storage, aipResources, aipIterator);
  }

  public static CloseableIterable<Resource> listRepresentationResources(StorageService storage) throws RODAException {
    final CloseableIterable<Resource> aipResources = storage
      .listResourcesUnderContainer(ModelUtils.getContainerPath(AIP.class), false);
    final Iterator<Resource> aipIterator = aipResources.iterator();
    return new RepresentationIterable(storage, aipIterator, aipResources);
  }

  public static CloseableIterable<Resource> listDIPFileResources(StorageService storage) throws RODAException {
    final CloseableIterable<Resource> dipResources = storage
      .listResourcesUnderContainer(ModelUtils.getContainerPath(DIP.class), false);
    final Iterator<Resource> dipIterator = dipResources.iterator();
    return new DIPFileIterable(storage, dipResources, dipIterator);
  }

  public static CloseableIterable<Resource> listPreservationMetadataResources(StorageService storage)
    throws RODAException {
    final CloseableIterable<Resource> aipResourcesTop = storage
      .listResourcesUnderContainer(ModelUtils.getContainerPath(AIP.class), false);
    final Iterator<Resource> aipIteratorTop = aipResourcesTop.iterator();

    CloseableIterable<Resource> aipMetadata = new AIPPreservationMetadataIterable(storage, aipResourcesTop,
      aipIteratorTop);

    final CloseableIterable<Resource> aipResourcesSub = storage
      .listResourcesUnderContainer(ModelUtils.getContainerPath(AIP.class), false);
    final Iterator<Resource> aipIteratorSub = aipResourcesSub.iterator();

    CloseableIterable<Resource> repMetadata = new RepresentationPreservationMetadataIterable(storage, aipResourcesSub,
      aipIteratorSub);

    DefaultStoragePath agentPath = DefaultStoragePath.parse(RodaConstants.STORAGE_DIRECTORY_PRESERVATION,
      RodaConstants.STORAGE_DIRECTORY_AGENTS);
    CloseableIterable<Resource> agentMetadata = storage.listResourcesUnderDirectory(agentPath, false);

    return CloseableIterables.concat(aipMetadata, repMetadata, agentMetadata);
  }

  public static CloseableIterable<Resource> listDescriptiveMetadataResources(StorageService storage)
    throws RODAException {
    final CloseableIterable<Resource> aipResourcesTop = storage
      .listResourcesUnderContainer(ModelUtils.getContainerPath(AIP.class), false);
    final Iterator<Resource> aipIteratorTop = aipResourcesTop.iterator();

    CloseableIterable<Resource> aipMetadata = new AIPDescriptiveMetadataIterable(storage, aipResourcesTop,
      aipIteratorTop);

    final CloseableIterable<Resource> aipResourcesSub = storage
      .listResourcesUnderContainer(ModelUtils.getContainerPath(AIP.class), false);
    final Iterator<Resource> aipIteratorSub = aipResourcesSub.iterator();

    CloseableIterable<Resource> repMetadata = new RepresentationDescriptiveMetadataIterable(storage, aipResourcesSub,
      aipIteratorSub);

    return CloseableIterables.concat(aipMetadata, repMetadata);
  }

}
