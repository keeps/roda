/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.StoragePath;

/**
 * Storage Service related and independent utility class
 *
 * @author Hélder Silva <hsilva@keep.pt>
 * @author Luis Faria <lfaria@keep.pt>
 */
public final class StorageServiceUtils {

  /**
   * Private empty constructor
   */
  private StorageServiceUtils() {

  }

  /**
   * Move resources from a given storage service/storage path to another storage
   * service/storage path
   *
   * @param fromService
   *          source storage service
   * @param fromStoragePath
   *          source storage path
   * @param toService
   *          destination storage service
   * @param toStoragePath
   *          destination storage path
   * @param rootEntity
   *          class of the root entity
   * @throws AlreadyExistsException
   * @throws NotFoundException
   * @throws RequestNotValidException
   * @throws GenericException
   * @throws AuthorizationDeniedException
   */
  public static void moveBetweenStorageServices(StorageService fromService, StoragePath fromStoragePath,
    StorageService toService, StoragePath toStoragePath, Class<? extends Entity> rootEntity) throws GenericException,
    RequestNotValidException, NotFoundException, AlreadyExistsException, AuthorizationDeniedException {
    copyOrMoveBetweenStorageServices(fromService, fromStoragePath, toService, toStoragePath, rootEntity, false, false);
  }

  /**
   * Synchronize resources from a given storage service/storage path to another
   *
   * @param fromService
   *          source storage service
   * @param fromStoragePath
   *          source storage path
   * @param toService
   *          destination storage service
   * @param toStoragePath
   *          destination storage path
   * @param rootEntity
   *          class of the root entity
   * @throws GenericException
   * @throws RequestNotValidException
   * @throws NotFoundException
   * @throws AlreadyExistsException
   * @throws AuthorizationDeniedException
   */
  public static void syncBetweenStorageServices(StorageService fromService, StoragePath fromStoragePath,
    StorageService toService, StoragePath toStoragePath, Class<? extends Entity> rootEntity) throws GenericException,
    RequestNotValidException, NotFoundException, AlreadyExistsException, AuthorizationDeniedException {
    copyOrMoveBetweenStorageServices(fromService, fromStoragePath, toService, toStoragePath, rootEntity, false, true);
  }

  /**
   * Copy resources from a given "storage service/storage path" to another
   * "storage service/storage path"
   *
   * @param fromService
   *          source storage service
   * @param fromStoragePath
   *          source storage path
   * @param toService
   *          destination storage service
   * @param toStoragePath
   *          destination storage path
   * @param rootEntity
   *          class of the root entity
   * @throws AlreadyExistsException
   * @throws NotFoundException
   * @throws RequestNotValidException
   * @throws GenericException
   * @throws AuthorizationDeniedException
   */
  public static void copyBetweenStorageServices(StorageService fromService, StoragePath fromStoragePath,
    StorageService toService, StoragePath toStoragePath, Class<? extends Entity> rootEntity) throws GenericException,
    RequestNotValidException, NotFoundException, AlreadyExistsException, AuthorizationDeniedException {
    copyOrMoveBetweenStorageServices(fromService, fromStoragePath, toService, toStoragePath, rootEntity, true, false);
  }

  private static void copyOrMoveBetweenStorageServices(StorageService fromService, StoragePath fromStoragePath,
    StorageService toService, StoragePath toStoragePath, Class<? extends Entity> rootEntity, boolean copy, boolean sync)
    throws GenericException, RequestNotValidException, NotFoundException, AlreadyExistsException,
    AuthorizationDeniedException {
    if (Container.class.isAssignableFrom(rootEntity)) {
      toService.createContainer(toStoragePath);
      boolean recursive = false;
      CloseableIterable<Resource> childResourcesIterator = fromService.listResourcesUnderContainer(fromStoragePath,
        recursive);
      iterateAndCopyOrMoveResources(fromService, fromStoragePath, toService, toStoragePath, childResourcesIterator,
        copy);
      if (!copy && !sync) {
        fromService.deleteContainer(fromStoragePath);
      }
    } else if (Directory.class.isAssignableFrom(rootEntity)) {
      toService.createDirectory(toStoragePath);
      boolean recursive = false;
      CloseableIterable<Resource> childResourcesIterator = fromService.listResourcesUnderDirectory(fromStoragePath,
        recursive);
      iterateAndCopyOrMoveResources(fromService, fromStoragePath, toService, toStoragePath, childResourcesIterator,
        copy);
      if (!copy && !sync) {
        fromService.deleteResource(fromStoragePath);
      }
    } else {
      Binary binary = fromService.getBinary(fromStoragePath);
      boolean asReference = false;

      if (sync) {
        toService.updateBinaryContent(toStoragePath, binary.getContent(), asReference, true);
      } else {
        toService.createBinary(toStoragePath, binary.getContent(), asReference);
      }

      if (!copy && !sync) {
        fromService.deleteResource(fromStoragePath);
      }
    }
  }

  private static void iterateAndCopyOrMoveResources(StorageService fromService, StoragePath fromStoragePath,
    StorageService toService, StoragePath toStoragePath, CloseableIterable<Resource> childResourcesIterable,
    boolean copy) throws RequestNotValidException, AlreadyExistsException, GenericException, NotFoundException,
    AuthorizationDeniedException {

    Iterator<Resource> iterator = childResourcesIterable.iterator();
    while (iterator.hasNext()) {
      Resource child = iterator.next();
      if (copy) {
        toService.copy(fromService, child.getStoragePath(),
          extractToStoragePathChild(fromStoragePath, child.getStoragePath(), toStoragePath));
      } else {
        toService.move(fromService, child.getStoragePath(),
          extractToStoragePathChild(fromStoragePath, child.getStoragePath(), toStoragePath));
      }
    }
    IOUtils.closeQuietly(childResourcesIterable);
  }

  private static StoragePath extractToStoragePathChild(StoragePath fromStoragePath, StoragePath fromStoragePathChild,
    StoragePath toStoragePath) throws RequestNotValidException {

    List<String> fromChildRelativePath = fromStoragePathChild.asList().subList(fromStoragePath.asList().size(),
      fromStoragePathChild.asList().size());
    List<String> toChildAbsolutePath = new ArrayList<>(toStoragePath.asList());

    toChildAbsolutePath.addAll(fromChildRelativePath);

    return DefaultStoragePath.parse(toChildAbsolutePath);
  }

  public static CloseableIterable<Resource> listTransactionalResourcesUnderContainer(
    StorageService stagingStorageService, StorageService mainStorageService, StoragePath storagePath, boolean recursive)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    CloseableIterable<Resource> stagingResources = stagingStorageService.listResourcesUnderContainer(storagePath,
      recursive);
    CloseableIterable<Resource> mainResources = mainStorageService.listResourcesUnderContainer(storagePath, recursive);
    return listTransactionalResourcesUnder(stagingResources, mainResources);
  }

  public static CloseableIterable<Resource> listTransactionalResourcesUnderDirectory(
          StorageService stagingStorageService, StorageService mainStorageService, StoragePath storagePath, boolean recursive)
          throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    CloseableIterable<Resource> stagingResources = stagingStorageService.listResourcesUnderDirectory(storagePath,
            recursive);
    CloseableIterable<Resource> mainResources = mainStorageService.listResourcesUnderDirectory(storagePath, recursive);
    return listTransactionalResourcesUnder(stagingResources, mainResources);
  }

  private static CloseableIterable<Resource> listTransactionalResourcesUnder(
    CloseableIterable<Resource> stagingResources, CloseableIterable<Resource> mainResources) {

    return new CloseableIterable<Resource>() {
      @Override
      public void close() throws IOException {
        stagingResources.close();
        mainResources.close();
      }

      @Override
      public Iterator<Resource> iterator() {
        return new Iterator<Resource>() {
          private final Iterator<Resource> stagingIterator = stagingResources.iterator();
          private final Iterator<Resource> mainIterator = mainResources.iterator();
          private final Set<StoragePath> seenPaths = new HashSet<>();
          private Resource nextItem = null;

          {
            advance();
          }

          private void advance() {
            nextItem = null;

            // Consume staging first
            while (stagingIterator.hasNext()) {
              Resource res = stagingIterator.next();
              StoragePath path = res.getStoragePath();
              if (seenPaths.add(path)) {
                nextItem = res;
                return;
              }
            }

            // Then consume main lazily, skipping seen paths
            while (mainIterator.hasNext()) {
              Resource res = mainIterator.next();
              StoragePath path = res.getStoragePath();
              if (seenPaths.contains(path)) {
                continue;
              }
              seenPaths.add(path);
              nextItem = res;
              return;
            }
          }

          @Override
          public boolean hasNext() {
            return nextItem != null;
          }

          @Override
          public Resource next() {
            if (nextItem == null) {
              throw new NoSuchElementException();
            }
            Resource current = nextItem;
            advance();
            return current;
          }
        };
      }
    };
  }
}
