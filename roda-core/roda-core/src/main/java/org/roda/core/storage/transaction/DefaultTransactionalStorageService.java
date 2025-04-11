package org.roda.core.storage.transaction;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.transaction.LockService;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.model.transaction.Transaction;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryVersion;
import org.roda.core.storage.Container;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.Directory;
import org.roda.core.storage.Entity;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DefaultTransactionalStorageService implements TransactionalStorageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTransactionalStorageService.class);

  private StorageService stagingStorageService;
  private StorageService mainStorageService;
  private Transaction transaction;
  private LockService lockService;

  public DefaultTransactionalStorageService(StorageService mainStorageService, StorageService stagingStorageService,
    Transaction transaction, LockService lockService) {
    this.mainStorageService = mainStorageService;
    this.stagingStorageService = stagingStorageService;
    this.transaction = transaction;
    this.lockService = lockService;
  }

  @Override
  public boolean exists(StoragePath storagePath) {
    acquireLock(storagePath);
    return getStorageService(storagePath).exists(storagePath);
  }

  @Override
  public CloseableIterable<Container> listContainers()
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    return mainStorageService.listContainers();
  }

  @Override
  public Container createContainer(StoragePath storagePath)
    throws GenericException, AlreadyExistsException, AuthorizationDeniedException, RequestNotValidException {
    acquireLock(storagePath);
    return stagingStorageService.createContainer(storagePath);
  }

  @Override
  public Container getContainer(StoragePath storagePath)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    acquireLock(storagePath);
    return getStorageService(storagePath).getContainer(storagePath);
  }

  @Override
  public void deleteContainer(StoragePath storagePath)
    throws NotFoundException, GenericException, AuthorizationDeniedException {
    acquireLock(storagePath);
    // TODO: What to do in this case?
    stagingStorageService.deleteContainer(storagePath);
  }

  @Override
  public CloseableIterable<Resource> listResourcesUnderContainer(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    acquireLock(storagePath);
    return getStorageService(storagePath).listResourcesUnderContainer(storagePath, recursive);
  }

  @Override
  public Long countResourcesUnderContainer(StoragePath storagePath, boolean recursive)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    acquireLock(storagePath);
    return getStorageService(storagePath).countResourcesUnderContainer(storagePath, recursive);
  }

  @Override
  public Directory createDirectory(StoragePath storagePath)
    throws AlreadyExistsException, GenericException, AuthorizationDeniedException {
    acquireLock(storagePath);
    return stagingStorageService.createDirectory(storagePath);
  }

  @Override
  public Directory createRandomDirectory(StoragePath parentStoragePath) throws RequestNotValidException,
    GenericException, NotFoundException, AlreadyExistsException, AuthorizationDeniedException {
    acquireLock(parentStoragePath);
    return stagingStorageService.createRandomDirectory(parentStoragePath);
  }

  @Override
  public Directory getDirectory(StoragePath storagePath)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    acquireLock(storagePath);
    return getStorageService(storagePath).getDirectory(storagePath);
  }

  @Override
  public boolean hasDirectory(StoragePath storagePath) {
    acquireLock(storagePath);
    return getStorageService(storagePath).hasDirectory(storagePath);
  }

  @Override
  public CloseableIterable<Resource> listResourcesUnderDirectory(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    acquireLock(storagePath);
    return getStorageService(storagePath).listResourcesUnderDirectory(storagePath, recursive);
  }

  @Override
  public CloseableIterable<Resource> listResourcesUnderFile(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    acquireLock(storagePath);
    return getStorageService(storagePath).listResourcesUnderFile(storagePath, recursive);
  }

  @Override
  public Long countResourcesUnderDirectory(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    acquireLock(storagePath);
    return getStorageService(storagePath).countResourcesUnderDirectory(storagePath, recursive);
  }

  @Override
  public Binary createBinary(StoragePath storagePath, ContentPayload payload, boolean asReference)
    throws GenericException, AlreadyExistsException, RequestNotValidException, AuthorizationDeniedException,
    NotFoundException {
    acquireLock(storagePath);
    return stagingStorageService.createBinary(storagePath, payload, asReference);
  }

  @Override
  public Binary createRandomBinary(StoragePath parentStoragePath, ContentPayload payload, boolean asReference)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    acquireLock(parentStoragePath);
    return stagingStorageService.createRandomBinary(parentStoragePath, payload, asReference);
  }

  @Override
  public Binary getBinary(StoragePath storagePath)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    acquireLock(storagePath);
    return getStorageService(storagePath).getBinary(storagePath);
  }

  @Override
  public boolean hasBinary(StoragePath storagePath) {
    acquireLock(storagePath);
    return getStorageService(storagePath).hasBinary(storagePath);
  }

  @Override
  public Binary updateBinaryContent(StoragePath storagePath, ContentPayload payload, boolean asReference,
    boolean createIfNotExists)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    acquireLock(storagePath);
    return stagingStorageService.updateBinaryContent(storagePath, payload, asReference, createIfNotExists);
  }

  @Override
  public void deleteResource(StoragePath storagePath)
    throws NotFoundException, GenericException, AuthorizationDeniedException {
    acquireLock(storagePath);
    stagingStorageService.deleteResource(storagePath);
  }

  @Override
  public Class<? extends Entity> getEntity(StoragePath storagePath)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    return getStorageService(storagePath).getEntity(storagePath);
  }

  @Override
  public void copy(StorageService fromService, StoragePath fromStoragePath, StoragePath toStoragePath)
    throws AlreadyExistsException, GenericException, RequestNotValidException, NotFoundException,
    AuthorizationDeniedException {
    copyFromMainStorageService(fromStoragePath);
    stagingStorageService.copy(fromService, fromStoragePath, toStoragePath);
  }

  @Override
  public void copy(StorageService fromService, StoragePath fromStoragePath, Path toPath, String resource)
    throws AlreadyExistsException, GenericException, AuthorizationDeniedException {
    copyFromMainStorageService(fromStoragePath);
    stagingStorageService.copy(fromService, fromStoragePath, toPath, resource);
  }

  @Override
  public void move(StorageService fromService, StoragePath fromStoragePath, StoragePath toStoragePath)
    throws AlreadyExistsException, GenericException, RequestNotValidException, NotFoundException,
    AuthorizationDeniedException {
    copyFromMainStorageService(fromStoragePath);
    stagingStorageService.move(fromService, fromStoragePath, toStoragePath);
  }

  @Override
  public DirectResourceAccess getDirectAccess(StoragePath storagePath) {
    copyFromMainStorageService(storagePath);
    return stagingStorageService.getDirectAccess(storagePath);
  }

  @Override
  public CloseableIterable<BinaryVersion> listBinaryVersions(StoragePath storagePath)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    acquireLock(storagePath);
    return getStorageService(storagePath).listBinaryVersions(storagePath);
  }

  @Override
  public BinaryVersion getBinaryVersion(StoragePath storagePath, String version)
    throws RequestNotValidException, NotFoundException, GenericException {
    acquireLock(storagePath);
    return getStorageService(storagePath).getBinaryVersion(storagePath, version);
  }

  @Override
  public BinaryVersion createBinaryVersion(StoragePath storagePath, Map<String, String> properties)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    acquireLock(storagePath);
    return stagingStorageService.createBinaryVersion(storagePath, properties);
  }

  @Override
  public void revertBinaryVersion(StoragePath storagePath, String version)
    throws NotFoundException, RequestNotValidException, GenericException, AuthorizationDeniedException {
    copyFromMainStorageService(storagePath);
    stagingStorageService.revertBinaryVersion(storagePath, version);
  }

  @Override
  public void deleteBinaryVersion(StoragePath storagePath, String version)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    copyFromMainStorageService(storagePath);
    stagingStorageService.deleteBinaryVersion(storagePath, version);
  }

  @Override
  public String getStoragePathAsString(StoragePath storagePath, boolean skipStoragePathContainer,
    StoragePath anotherStoragePath, boolean skipAnotherStoragePathContainer) {
    acquireLock(storagePath);
    return getStorageService(storagePath).getStoragePathAsString(storagePath, skipStoragePathContainer,
      anotherStoragePath, skipAnotherStoragePathContainer);
  }

  @Override
  public String getStoragePathAsString(StoragePath storagePath, boolean skipContainer) {
    acquireLock(storagePath);
    return getStorageService(storagePath).getStoragePathAsString(storagePath, skipContainer);
  }

  @Override
  public List<StoragePath> getShallowFiles(StoragePath storagePath) throws NotFoundException, GenericException {
    acquireLock(storagePath);
    return getStorageService(storagePath).getShallowFiles(storagePath);
  }

  @Override
  public void commit() {
    for (StoragePath storagePath : transaction.getStoragePathList()) {
      if(!stagingStorageService.exists(storagePath)) {
        // TODO: release lock now or wait for the transaction to finish?
        releaseLock(storagePath);
        continue;
      }
      try {
        if(mainStorageService.exists(storagePath)) {
          mainStorageService.deleteResource(storagePath);
        }
        LOGGER.info("Moving resource from staging to main storage service: {}", storagePath);
        StorageServiceUtils.copyBetweenStorageServices(stagingStorageService, storagePath, mainStorageService,
          storagePath, getEntity(storagePath));
      } catch (GenericException | RequestNotValidException | NotFoundException | AlreadyExistsException
        | AuthorizationDeniedException e) {
        // TODO: Handle this exceptions
      } finally {
        // TODO: release lock now or wait for the transaction to finish?
        releaseLock(storagePath);
      }
    }
  }

  private void copyFromMainStorageService(StoragePath storagePath) {
    acquireLock(storagePath);
    try {
      StorageServiceUtils.copyBetweenStorageServices(mainStorageService, storagePath, stagingStorageService,
        storagePath, Resource.class);
    } catch (AuthorizationDeniedException | RequestNotValidException | AlreadyExistsException | NotFoundException
      | GenericException e) {
      // Do nothing
    }
  }

  @Override
  public void rollback() {

  }

  private void acquireLock(StoragePath storagePath) {
    if (!storagePath.isFromAContainer()) {
      LOGGER.info("Acquiring lock for storage path: {}", storagePath);
      // lockService.acquireLock(storagePath.toString(),
      // transaction.getTransactionId());
      if (!transaction.exist(storagePath)) {
        transaction.addStoragePath(storagePath);
      }
    }
  }

  private void releaseLock(StoragePath storagePath) {
    LOGGER.info("Releasing lock for storage path: {}", storagePath);
    // lockService.releaseLock(storagePath.toString(),
    // transaction.getTransactionId());
  }

  private StorageService getStorageService(StoragePath storagePath) {
    if (stagingStorageService.exists(storagePath)) {
      return stagingStorageService;
    } else {
      return mainStorageService;
    }
  }

}
