/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.ip.ShallowFile;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.storage.protocol.ProtocolManager;
import org.roda.core.storage.protocol.ProtocolManagerFactory;

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
    copyOrMoveBetweenStorageServices(fromService, fromStoragePath, toService, toStoragePath, rootEntity, false);
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
    copyOrMoveBetweenStorageServices(fromService, fromStoragePath, toService, toStoragePath, rootEntity, true);
  }

  private static void copyOrMoveBetweenStorageServices(StorageService fromService, StoragePath fromStoragePath,
    StorageService toService, StoragePath toStoragePath, Class<? extends Entity> rootEntity, boolean copy)
    throws GenericException, RequestNotValidException, NotFoundException, AlreadyExistsException,
    AuthorizationDeniedException {
    if (Container.class.isAssignableFrom(rootEntity)) {
      toService.createContainer(toStoragePath);
      boolean recursive = false;
      CloseableIterable<Resource> childResourcesIterator = fromService.listResourcesUnderContainer(fromStoragePath,
        recursive);
      iterateAndCopyOrMoveResources(fromService, fromStoragePath, toService, toStoragePath, childResourcesIterator,
        copy);
      if (!copy) {
        fromService.deleteContainer(fromStoragePath);
      }
    } else if (Directory.class.isAssignableFrom(rootEntity)) {
      toService.createDirectory(toStoragePath);
      boolean recursive = false;
      CloseableIterable<Resource> childResourcesIterator = fromService.listResourcesUnderDirectory(fromStoragePath,
        recursive);
      iterateAndCopyOrMoveResources(fromService, fromStoragePath, toService, toStoragePath, childResourcesIterator,
        copy);
      if (!copy) {
        fromService.deleteResource(fromStoragePath);
      }
    } else {
      Binary binary = fromService.getBinary(fromStoragePath);
      boolean asReference = false;
      toService.createBinary(toStoragePath, binary.getContent(), asReference);
      if (!copy) {
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

}
