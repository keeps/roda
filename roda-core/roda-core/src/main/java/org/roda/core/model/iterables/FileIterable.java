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

public class FileIterable implements CloseableIterable<Resource> {
  private static final Logger LOGGER = LoggerFactory.getLogger(FileIterable.class);

  private final CloseableIterable<Resource> aipResources;
  private final Iterator<Resource> aipIterator;
  private final StorageService storage;

  public FileIterable(StorageService storage, CloseableIterable<Resource> aipResources,
    Iterator<Resource> aipIterator) {
    this.aipResources = aipResources;
    this.aipIterator = aipIterator;
    this.storage = storage;
  }

  @Override
  public void close() throws IOException {
    aipResources.close();
  }

  @Override
  public Iterator<Resource> iterator() {

    return new Iterator<Resource>() {
      CloseableIterable<Resource> repResources = null;
      Iterator<Resource> repIterator = null;
      CloseableIterable<Resource> fileResources = null;
      Iterator<Resource> fileIterator = null;
      Resource nextResource = null;

      @Override
      public boolean hasNext() {
        if (repResources == null) {
          while (aipIterator.hasNext()) {
            try {
              StoragePath repPath = DefaultStoragePath.parse(aipIterator.next().getStoragePath(),
                RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS);

              if (storage.hasDirectory(repPath)) {
                repResources = storage.listResourcesUnderDirectory(repPath, false);
                repIterator = repResources.iterator();

                while (repIterator.hasNext()) {
                  StoragePath filePath = DefaultStoragePath.parse(repIterator.next().getStoragePath(),
                    RodaConstants.STORAGE_DIRECTORY_DATA);

                  if (storage.hasDirectory(filePath)) {
                    fileResources = storage.listResourcesUnderDirectory(filePath, true);
                    fileIterator = fileResources.iterator();
                    if (fileIterator.hasNext()) {
                      nextResource = fileIterator.next();
                      break;
                    } else {
                      IOUtils.closeQuietly(fileResources);
                    }
                  }
                }

                if (nextResource == null) {
                  IOUtils.closeQuietly(repResources);
                } else {
                  break;
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
          while (repIterator.hasNext()) {
            try {
              StoragePath filePath = DefaultStoragePath.parse(repIterator.next().getStoragePath(),
                RodaConstants.STORAGE_DIRECTORY_DATA);

              if (storage.hasDirectory(filePath)) {
                IOUtils.closeQuietly(fileResources);
                fileResources = storage.listResourcesUnderDirectory(filePath, true);
                fileIterator = fileResources.iterator();
                if (fileIterator.hasNext()) {
                  break;
                } else {
                  IOUtils.closeQuietly(fileResources);
                }
              }
            } catch (RODAException e) {
              LOGGER.error("Could not list resources under representation data directory", e);
            }
          }

          if (fileIterator.hasNext()) {
            nextResource = fileIterator.next();
          } else {
            outerloop: while (aipIterator.hasNext()) {
              try {
                StoragePath repPath = DefaultStoragePath.parse(aipIterator.next().getStoragePath(),
                  RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS);

                if (storage.hasDirectory(repPath)) {
                  IOUtils.closeQuietly(repResources);
                  repResources = storage.listResourcesUnderDirectory(repPath, false);
                  repIterator = repResources.iterator();
                  while (repIterator.hasNext()) {
                    StoragePath filePath = DefaultStoragePath.parse(repIterator.next().getStoragePath(),
                      RodaConstants.STORAGE_DIRECTORY_DATA);

                    if (storage.hasDirectory(filePath)) {
                      IOUtils.closeQuietly(fileResources);
                      fileResources = storage.listResourcesUnderDirectory(filePath, true);
                      fileIterator = fileResources.iterator();
                      if (fileIterator.hasNext()) {
                        break outerloop;
                      } else {
                        IOUtils.closeQuietly(fileResources);
                      }
                    }
                  }
                }
              } catch (RODAException e) {
                LOGGER.error("Could not list resources under AIP", e);
              }
            }

            if (fileIterator.hasNext()) {
              nextResource = fileIterator.next();
            }
          }
        }

        return ret;
      }
    };
  }
}
