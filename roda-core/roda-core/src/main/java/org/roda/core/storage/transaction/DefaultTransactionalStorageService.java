package org.roda.core.storage.transaction;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.transaction.InMemoryLockService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DefaultTransactionalStorageService implements TransactionalStorageService {

  private StorageService stagingStorageService;
  private StorageService mainStorageService;
  private Transaction transaction;

  private InMemoryLockService lockService;

  public DefaultTransactionalStorageService(StorageService mainStorageService, StorageService stagingStorageService,
    Transaction transaction) throws GenericException {
    this.mainStorageService = mainStorageService;
    this.stagingStorageService = stagingStorageService;
    this.transaction = transaction;
  }

  public void setLockService(InMemoryLockService lockService) {
    this.lockService = lockService;
  }

  private void acquireLock(StoragePath storagePath) {
    lockService.acquireLock(storagePath.toString(), transaction.getTransactionId());
  }


  @Override
  public boolean exists(StoragePath storagePath) {
    acquireLock(storagePath);
    return mainStorageService.exists(storagePath);
  }

  @Override
  public CloseableIterable<Container> listContainers()
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    return mainStorageService.listContainers();
  }

  @Override
  public Container createContainer(StoragePath storagePath)
    throws GenericException, AlreadyExistsException, AuthorizationDeniedException, RequestNotValidException {
    addStoragePath(storagePath);
    return stagingStorageService.createContainer(storagePath);
  }

  @Override
  public Container getContainer(StoragePath storagePath)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    acquireLock(storagePath);
    return mainStorageService.getContainer(storagePath);
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
    return mainStorageService.listResourcesUnderContainer(storagePath, recursive);
  }

  @Override
  public Long countResourcesUnderContainer(StoragePath storagePath, boolean recursive)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    acquireLock(storagePath);
    return mainStorageService.countResourcesUnderContainer(storagePath, recursive);
  }

  @Override
  public Directory createDirectory(StoragePath storagePath)
    throws AlreadyExistsException, GenericException, AuthorizationDeniedException {
    addStoragePath(storagePath);
    return stagingStorageService.createDirectory(storagePath);
  }

  @Override
  public Directory createRandomDirectory(StoragePath parentStoragePath) throws RequestNotValidException,
    GenericException, NotFoundException, AlreadyExistsException, AuthorizationDeniedException {
    addStoragePath(parentStoragePath);
    return stagingStorageService.createRandomDirectory(parentStoragePath);
  }

  @Override
  public Directory getDirectory(StoragePath storagePath)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    acquireLock(storagePath);
    return mainStorageService.getDirectory(storagePath);
  }

  @Override
  public boolean hasDirectory(StoragePath storagePath) {
    acquireLock(storagePath);
    return mainStorageService.hasDirectory(storagePath);
  }

  @Override
  public CloseableIterable<Resource> listResourcesUnderDirectory(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    acquireLock(storagePath);
    return mainStorageService.listResourcesUnderDirectory(storagePath, recursive);
  }

  @Override
  public CloseableIterable<Resource> listResourcesUnderFile(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    acquireLock(storagePath);
    return mainStorageService.listResourcesUnderFile(storagePath, recursive);
  }

  @Override
  public Long countResourcesUnderDirectory(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    acquireLock(storagePath);
    return mainStorageService.countResourcesUnderDirectory(storagePath, recursive);
  }

  @Override
  public Binary createBinary(StoragePath storagePath, ContentPayload payload, boolean asReference)
    throws GenericException, AlreadyExistsException, RequestNotValidException, AuthorizationDeniedException,
    NotFoundException {
    addStoragePath(storagePath);
    return stagingStorageService.createBinary(storagePath, payload, asReference);
  }

  @Override
  public Binary createRandomBinary(StoragePath parentStoragePath, ContentPayload payload, boolean asReference)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    addStoragePath(parentStoragePath);
    return stagingStorageService.createRandomBinary(parentStoragePath, payload, asReference);
  }

  @Override
  public Binary getBinary(StoragePath storagePath)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    acquireLock(storagePath);
    return mainStorageService.getBinary(storagePath);
  }

  @Override
  public boolean hasBinary(StoragePath storagePath) {
    acquireLock(storagePath);
    return mainStorageService.hasBinary(storagePath);
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
    acquireLock(storagePath);
    return mainStorageService.getEntity(storagePath);
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
    return mainStorageService.listBinaryVersions(storagePath);
  }

  @Override
  public BinaryVersion getBinaryVersion(StoragePath storagePath, String version)
    throws RequestNotValidException, NotFoundException, GenericException {
    acquireLock(storagePath);
    return mainStorageService.getBinaryVersion(storagePath, version);
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
    return mainStorageService.getStoragePathAsString(storagePath, skipStoragePathContainer, anotherStoragePath,
      skipAnotherStoragePathContainer);
  }

  @Override
  public String getStoragePathAsString(StoragePath storagePath, boolean skipContainer) {
    acquireLock(storagePath);
    return mainStorageService.getStoragePathAsString(storagePath, skipContainer);
  }

  @Override
  public List<StoragePath> getShallowFiles(StoragePath storagePath) throws NotFoundException, GenericException {
    acquireLock(storagePath);
    return mainStorageService.getShallowFiles(storagePath);
  }

  @Override
  public void commit() {
    for (StoragePath storagePath : transaction.getStoragePathList()) {
      try {
        StorageServiceUtils.moveBetweenStorageServices(stagingStorageService, storagePath, mainStorageService,
          storagePath, Resource.class);
      } catch (GenericException | RequestNotValidException | NotFoundException | AlreadyExistsException
        | AuthorizationDeniedException e) {
        e.printStackTrace();
      }
    }
  }

  private void copyFromMainStorageService(StoragePath storagePath) {
    acquireLock(storagePath);
    if (!transaction.exist(storagePath)) {
      try {
        StorageServiceUtils.copyBetweenStorageServices(mainStorageService, storagePath, stagingStorageService,
          storagePath, Resource.class);
        transaction.addStoragePath(storagePath);
      } catch (AuthorizationDeniedException | RequestNotValidException | AlreadyExistsException | NotFoundException
        | GenericException e) {
        // Do nothing
      }
    }
  }

  private void addStoragePath(StoragePath storagePath) {
    acquireLock(storagePath);
    if (!transaction.exist(storagePath)) {
      transaction.addStoragePath(storagePath);
    }
  }

  @Override
  public void rollback() {

  }

}
