package org.roda.core.model.iterables;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AIPPreservationMetadataIterable implements CloseableIterable<Resource> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AIPPreservationMetadataIterable.class);

  private final CloseableIterable<Resource> aipResourcesTop;
  private final Iterator<Resource> aipIteratorTop;
  private final StorageService storage;

  public AIPPreservationMetadataIterable(StorageService storage, CloseableIterable<Resource> aipResourcesTop,
    Iterator<Resource> aipIteratorTop) {
    this.aipResourcesTop = aipResourcesTop;
    this.aipIteratorTop = aipIteratorTop;
    this.storage = storage;
  }

  @Override
  public void close() throws IOException {
    aipResourcesTop.close();
  }

  @Override
  public Iterator<Resource> iterator() {

    return new Iterator<Resource>() {
      CloseableIterable<Resource> metadataResources = null;
      Iterator<Resource> metadataIterator = null;
      Resource nextResource = null;

      @Override
      public boolean hasNext() {
        if (metadataResources == null) {
          try {
            while (aipIteratorTop.hasNext()) {
              StoragePath metadataPath = DefaultStoragePath.parse(aipIteratorTop.next().getStoragePath(),
                RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_PRESERVATION);

              if (storage.hasDirectory(metadataPath)) {
                metadataResources = storage.listResourcesUnderDirectory(metadataPath, false);
                metadataIterator = metadataResources.iterator();

                if (metadataIterator.hasNext()) {
                  nextResource = metadataIterator.next();
                  break;
                } else {
                  IOUtils.closeQuietly(metadataResources);
                }
              }
            }
          } catch (RODAException e) {
            return false;
          }
        }

        return nextResource != null;
      }

      @Override
      public Resource next() {
        Resource ret = nextResource;
        nextResource = null;

        if (metadataIterator.hasNext()) {
          nextResource = metadataIterator.next();
        } else {
          while (aipIteratorTop.hasNext()) {
            try {
              StoragePath metadataPath = DefaultStoragePath.parse(aipIteratorTop.next().getStoragePath(),
                RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_PRESERVATION);

              if (storage.hasDirectory(metadataPath)) {
                metadataResources = storage.listResourcesUnderDirectory(metadataPath, false);
                metadataIterator = metadataResources.iterator();

                if (metadataIterator.hasNext()) {
                  nextResource = metadataIterator.next();
                  break;
                } else {
                  IOUtils.closeQuietly(metadataResources);
                }
              }
            } catch (RODAException e) {
              LOGGER.error("Could not list resources under AIP data directory", e);
            }
          }
        }

        return ret;
      }
    };
  }

}
