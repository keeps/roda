/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage.utils;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Iterator;

import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageRecursiveListingUtils {

  private StorageRecursiveListingUtils() {
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(StorageRecursiveListingUtils.class);

  private static CloseableIterable<Resource> recursiveListing(final StorageService storage,
    final CloseableIterable<Resource> iterable) {
    final Iterator<Resource> directlyUnder = iterable.iterator();

    return new CloseableIterable<Resource>() {

      @Override
      public Iterator<Resource> iterator() {
        return new Iterator<Resource>() {

          ArrayDeque<CloseableIterable<Resource>> itStack = new ArrayDeque<>();

          @Override
          public boolean hasNext() {
            boolean hasNext;

            if (itStack.isEmpty()) {
              hasNext = directlyUnder.hasNext();
            } else {
              hasNext = false;
              // find a non-empty iterator or empty stack
              do {
                if (!itStack.peek().iterator().hasNext()) {
                  try {
                    itStack.pop().close();
                  } catch (IOException e) {
                    LOGGER.warn("Error closing file iterable, possible file leak", e);
                  }
                } else {
                  hasNext = true;
                }
              } while (!hasNext && !itStack.isEmpty());

              if (itStack.isEmpty()) {
                hasNext = directlyUnder.hasNext();
              }
            }
            return hasNext;
          }

          @Override
          public Resource next() {
            Resource resource;
            if (itStack.isEmpty()) {
              resource = directlyUnder.next();
            } else {
              resource = itStack.peek().iterator().next();
            }

            try {
              if (resource != null && resource.isDirectory()) {
                CloseableIterable<Resource> subIterable = storage.listResourcesUnderDirectory(resource.getStoragePath(),
                  false);
                itStack.push(subIterable);
              }
            } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
              LOGGER.warn("Error while listing all files", e);
            }

            return resource;
          }
        };
      }

      @Override
      public void close() throws IOException {
        iterable.close();
      }
    };
  }

  /**
   * Emulates recursive listing using non-recursive listing
   * 
   * @param storage
   * @param storagePath
   * @return
   * @throws NotFoundException
   * @throws GenericException
   * @throws RequestNotValidException
   * @throws AuthorizationDeniedException
   */
  public static CloseableIterable<Resource> listAllUnderDirectory(final StorageService storage,
    final StoragePath storagePath)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {

    final CloseableIterable<Resource> iterable = storage.listResourcesUnderDirectory(storagePath, false);
    return recursiveListing(storage, iterable);
  }

  /**
   * Emulates recursive listing using non-recursive listing
   * 
   * @param storage
   * @param storagePath
   * @return
   * @throws NotFoundException
   * @throws GenericException
   * @throws RequestNotValidException
   * @throws AuthorizationDeniedException
   */
  public static CloseableIterable<Resource> listAllUnderContainer(StorageService storage, StoragePath storagePath)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    final CloseableIterable<Resource> iterable = storage.listResourcesUnderContainer(storagePath, false);
    return recursiveListing(storage, iterable);
  }

  public static Long countAllUnderDirectory(StorageService storage, StoragePath storagePath)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    Long ret = 0L;

    try (CloseableIterable<Resource> iterable = listAllUnderDirectory(storage, storagePath)) {
      Iterator<Resource> it = iterable.iterator();
      while (it.hasNext()) {
        ret++;
      }
    } catch (IOException e) {
      throw new GenericException(e);
    }

    return ret;
  }

  public static Long countAllUnderContainer(StorageService storage, StoragePath storagePath)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    Long ret = 0L;

    try (CloseableIterable<Resource> iterable = listAllUnderContainer(storage, storagePath)) {
      Iterator<Resource> it = iterable.iterator();
      while (it.hasNext()) {
        ret++;
      }
    } catch (IOException e) {
      throw new GenericException(e);
    }

    return ret;
  }
}
