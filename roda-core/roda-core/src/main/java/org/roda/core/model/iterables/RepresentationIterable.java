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

public class RepresentationIterable implements CloseableIterable<Resource> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RepresentationIterable.class);

  private final Iterator<Resource> aipIterator;
  private final CloseableIterable<Resource> aipResources;
  private final StorageService storage;

  public RepresentationIterable(StorageService storage, Iterator<Resource> aipIterator,
    CloseableIterable<Resource> aipResources) {
    this.aipIterator = aipIterator;
    this.aipResources = aipResources;
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
      Resource nextResource = null;

      @Override
      public boolean hasNext() {
        if (repResources == null) {
          try {
            while (aipIterator.hasNext()) {
              StoragePath repPath = DefaultStoragePath.parse(aipIterator.next().getStoragePath(),
                RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS);

              if (storage.hasDirectory(repPath)) {
                repResources = storage.listResourcesUnderDirectory(repPath, false);
                repIterator = repResources.iterator();

                if (repIterator.hasNext()) {
                  nextResource = repIterator.next();
                  break;
                } else {
                  IOUtils.closeQuietly(repResources);
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

        if (repIterator.hasNext()) {
          nextResource = repIterator.next();
        } else {
          while (aipIterator.hasNext()) {
            try {
              StoragePath repPath = DefaultStoragePath.parse(aipIterator.next().getStoragePath(),
                RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS);

              if (storage.hasDirectory(repPath)) {
                repResources = storage.listResourcesUnderDirectory(repPath, false);
                repIterator = repResources.iterator();

                if (repIterator.hasNext()) {
                  nextResource = repIterator.next();
                  break;
                } else {
                  IOUtils.closeQuietly(repResources);
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