/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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

public class RepresentationDescriptiveMetadataIterable implements CloseableIterable<Resource> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RepresentationDescriptiveMetadataIterable.class);

  private final CloseableIterable<Resource> aipResourcesSub;
  private final Iterator<Resource> aipIteratorSub;
  private final StorageService storage;

  public RepresentationDescriptiveMetadataIterable(StorageService storage, CloseableIterable<Resource> aipResourcesSub,
    Iterator<Resource> aipIteratorSub) {
    this.aipResourcesSub = aipResourcesSub;
    this.aipIteratorSub = aipIteratorSub;
    this.storage = storage;
  }

  @Override
  public void close() throws IOException {
    aipResourcesSub.close();
  }

  @Override
  public Iterator<Resource> iterator() {

    return new Iterator<Resource>() {
      CloseableIterable<Resource> repResources = null;
      Iterator<Resource> repIterator = null;
      CloseableIterable<Resource> metadataResources = null;
      Iterator<Resource> metadataIterator = null;
      Resource nextResource = null;

      @Override
      public boolean hasNext() {
        if (repResources == null) {
          try {
            while (aipIteratorSub.hasNext()) {
              StoragePath repPath = DefaultStoragePath.parse(aipIteratorSub.next().getStoragePath(),
                RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS);

              if (storage.hasDirectory(repPath)) {
                repResources = storage.listResourcesUnderDirectory(repPath, false);
                repIterator = repResources.iterator();

                while (repIterator.hasNext()) {
                  StoragePath metadataPath = DefaultStoragePath.parse(repIterator.next().getStoragePath(),
                    RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_DESCRIPTIVE);

                  if (storage.hasDirectory(metadataPath)) {
                    metadataResources = storage.listResourcesUnderDirectory(metadataPath, true);
                    metadataIterator = metadataResources.iterator();
                    if (metadataIterator.hasNext()) {
                      nextResource = metadataIterator.next();
                      break;
                    } else {
                      IOUtils.closeQuietly(metadataResources);
                    }
                  }
                }

                if (nextResource == null) {
                  IOUtils.closeQuietly(repResources);
                } else {
                  break;
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
          while (repIterator.hasNext()) {
            try {
              StoragePath metadataPath = DefaultStoragePath.parse(repIterator.next().getStoragePath(),
                RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_DESCRIPTIVE);

              if (storage.hasDirectory(metadataPath)) {
                IOUtils.closeQuietly(metadataResources);
                metadataResources = storage.listResourcesUnderDirectory(metadataPath, true);
                metadataIterator = metadataResources.iterator();
                if (metadataIterator.hasNext()) {
                  break;
                } else {
                  IOUtils.closeQuietly(metadataResources);
                }
              }
            } catch (RODAException e) {
              LOGGER.error("Could not list resources under representation data directory", e);
            }
          }

          if (metadataIterator.hasNext()) {
            nextResource = metadataIterator.next();
          } else {
            outerloop: while (aipIteratorSub.hasNext()) {
              try {
                StoragePath repPath = DefaultStoragePath.parse(aipIteratorSub.next().getStoragePath(),
                  RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS);

                if (storage.hasDirectory(repPath)) {
                  IOUtils.closeQuietly(repResources);
                  repResources = storage.listResourcesUnderDirectory(repPath, false);
                  repIterator = repResources.iterator();
                  while (repIterator.hasNext()) {
                    StoragePath metadataPath = DefaultStoragePath.parse(repIterator.next().getStoragePath(),
                      RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_DESCRIPTIVE);

                    if (storage.hasDirectory(metadataPath)) {
                      IOUtils.closeQuietly(metadataResources);
                      metadataResources = storage.listResourcesUnderDirectory(metadataPath, true);
                      metadataIterator = metadataResources.iterator();
                      if (metadataIterator.hasNext()) {
                        break outerloop;
                      } else {
                        IOUtils.closeQuietly(metadataResources);
                      }
                    }
                  }
                }
              } catch (RODAException e) {
                LOGGER.error("Could not list resources under AIP", e);
              }
            }

            if (metadataIterator.hasNext()) {
              nextResource = metadataIterator.next();
            }
          }
        }

        return ret;
      }
    };
  }
}