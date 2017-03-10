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

public class DIPFileIterable implements CloseableIterable<Resource> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DIPFileIterable.class);

  private final CloseableIterable<Resource> dipResources;
  private final Iterator<Resource> dipIterator;
  private final StorageService storage;

  public DIPFileIterable(StorageService storage, CloseableIterable<Resource> dipResources,
    Iterator<Resource> dipIterator) {
    this.dipResources = dipResources;
    this.dipIterator = dipIterator;
    this.storage = storage;
  }

  @Override
  public void close() throws IOException {
    dipResources.close();
  }

  @Override
  public Iterator<Resource> iterator() {

    return new Iterator<Resource>() {
      CloseableIterable<Resource> fileResources = null;
      Iterator<Resource> fileIterator = null;
      Resource nextResource = null;

      @Override
      public boolean hasNext() {
        if (fileResources == null) {
          while (dipIterator.hasNext()) {
            try {
              StoragePath dataPath = DefaultStoragePath.parse(dipIterator.next().getStoragePath(),
                RodaConstants.STORAGE_DIRECTORY_DATA);

              if (storage.hasDirectory(dataPath)) {
                fileResources = storage.listResourcesUnderDirectory(dataPath, true);
                fileIterator = fileResources.iterator();

                if (fileIterator.hasNext()) {
                  nextResource = fileIterator.next();
                  break;
                } else {
                  IOUtils.closeQuietly(fileResources);
                }
              }
            } catch (RODAException e) {
              return false;
            }
          }
        }

        return nextResource != null;
      }

      @Override
      public Resource next() {
        Resource ret = nextResource;
        nextResource = null;

        if (fileIterator.hasNext()) {
          nextResource = fileIterator.next();
        } else {
          while (dipIterator.hasNext()) {
            try {
              StoragePath dataPath = DefaultStoragePath.parse(dipIterator.next().getStoragePath(),
                RodaConstants.STORAGE_DIRECTORY_DATA);

              if (storage.hasDirectory(dataPath)) {
                fileResources = storage.listResourcesUnderDirectory(dataPath, true);
                fileIterator = fileResources.iterator();

                if (fileIterator.hasNext()) {
                  nextResource = fileIterator.next();
                  break;
                } else {
                  IOUtils.closeQuietly(fileResources);
                }
              }
            } catch (RODAException e) {
              LOGGER.error("Could not list resources under DIP data directory", e);
            }
          }
        }

        return ret;
      }
    };
  }
}