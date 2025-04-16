package org.roda.core.storage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.entity.transaction.TransactionLog;
import org.roda.core.entity.transaction.TransactionalStoragePath;
import org.roda.core.transaction.RODATransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.transaction.TransactionalException;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DefaultTransactionalStorageService implements TransactionalStorageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTransactionalStorageService.class);

  private StorageService stagingStorageService;
  private StorageService mainStorageService;
  private TransactionLog transaction;
  private RODATransactionManager RODATransactionManager;

  public DefaultTransactionalStorageService(StorageService mainStorageService, StorageService stagingStorageService,
    TransactionLog transaction, RODATransactionManager RODATransactionManager) {
    this.mainStorageService = mainStorageService;
    this.stagingStorageService = stagingStorageService;
    this.transaction = transaction;
    this.RODATransactionManager = RODATransactionManager;
  }

  @Override
  public boolean exists(StoragePath storagePath) {
    registerOperation(storagePath, TransactionalStoragePath.OperationType.READ);
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
    registerOperation(storagePath, TransactionalStoragePath.OperationType.CREATE);
    return stagingStorageService.createContainer(storagePath);
  }

  @Override
  public Container getContainer(StoragePath storagePath)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    registerOperation(storagePath, TransactionalStoragePath.OperationType.READ);
    return getStorageService(storagePath).getContainer(storagePath);
  }

  @Override
  public void deleteContainer(StoragePath storagePath)
    throws NotFoundException, GenericException, AuthorizationDeniedException {
    registerOperation(storagePath, TransactionalStoragePath.OperationType.DELETE);
    // TODO: What to do in this case?
    stagingStorageService.deleteContainer(storagePath);
  }

  @Override
  public CloseableIterable<Resource> listResourcesUnderContainer(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    registerOperation(storagePath, TransactionalStoragePath.OperationType.READ);
    return getStorageService(storagePath).listResourcesUnderContainer(storagePath, recursive);
  }

  @Override
  public Long countResourcesUnderContainer(StoragePath storagePath, boolean recursive)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    registerOperation(storagePath, TransactionalStoragePath.OperationType.READ);
    return getStorageService(storagePath).countResourcesUnderContainer(storagePath, recursive);
  }

  @Override
  public Directory createDirectory(StoragePath storagePath)
    throws AlreadyExistsException, GenericException, AuthorizationDeniedException {
    registerOperation(storagePath, TransactionalStoragePath.OperationType.CREATE);
    return stagingStorageService.createDirectory(storagePath);
  }

  @Override
  public Directory createRandomDirectory(StoragePath parentStoragePath) throws RequestNotValidException,
    GenericException, NotFoundException, AlreadyExistsException, AuthorizationDeniedException {
    registerOperation(parentStoragePath, TransactionalStoragePath.OperationType.CREATE);
    return stagingStorageService.createRandomDirectory(parentStoragePath);
  }

  @Override
  public Directory getDirectory(StoragePath storagePath)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    registerOperation(storagePath, TransactionalStoragePath.OperationType.READ);
    return getStorageService(storagePath).getDirectory(storagePath);
  }

  @Override
  public boolean hasDirectory(StoragePath storagePath) {
    registerOperation(storagePath, TransactionalStoragePath.OperationType.READ);
    return getStorageService(storagePath).hasDirectory(storagePath);
  }

  @Override
  public CloseableIterable<Resource> listResourcesUnderDirectory(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    registerOperation(storagePath, TransactionalStoragePath.OperationType.READ);
    return getStorageService(storagePath).listResourcesUnderDirectory(storagePath, recursive);
  }

  @Override
  public CloseableIterable<Resource> listResourcesUnderFile(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    registerOperation(storagePath, TransactionalStoragePath.OperationType.READ);
    return getStorageService(storagePath).listResourcesUnderFile(storagePath, recursive);
  }

  @Override
  public Long countResourcesUnderDirectory(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    registerOperation(storagePath, TransactionalStoragePath.OperationType.READ);
    return getStorageService(storagePath).countResourcesUnderDirectory(storagePath, recursive);
  }

  @Override
  public Binary createBinary(StoragePath storagePath, ContentPayload payload, boolean asReference)
    throws GenericException, AlreadyExistsException, RequestNotValidException, AuthorizationDeniedException,
    NotFoundException {
    registerOperation(storagePath, TransactionalStoragePath.OperationType.CREATE);
    return stagingStorageService.createBinary(storagePath, payload, asReference);
  }

  @Override
  public Binary createRandomBinary(StoragePath parentStoragePath, ContentPayload payload, boolean asReference)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    registerOperation(parentStoragePath, TransactionalStoragePath.OperationType.CREATE);
    return stagingStorageService.createRandomBinary(parentStoragePath, payload, asReference);
  }

  @Override
  public Binary getBinary(StoragePath storagePath)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    registerOperation(storagePath, TransactionalStoragePath.OperationType.READ);
    return getStorageService(storagePath).getBinary(storagePath);
  }

  @Override
  public boolean hasBinary(StoragePath storagePath) {
    registerOperation(storagePath, TransactionalStoragePath.OperationType.READ);
    return getStorageService(storagePath).hasBinary(storagePath);
  }

  @Override
  public Binary updateBinaryContent(StoragePath storagePath, ContentPayload payload, boolean asReference,
    boolean createIfNotExists)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    registerOperation(storagePath, TransactionalStoragePath.OperationType.UPDATE);
    return stagingStorageService.updateBinaryContent(storagePath, payload, asReference, createIfNotExists);
  }

  @Override
  public void deleteResource(StoragePath storagePath)
    throws NotFoundException, GenericException, AuthorizationDeniedException {
    registerOperation(storagePath, TransactionalStoragePath.OperationType.DELETE);
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
    registerOperation(storagePath, TransactionalStoragePath.OperationType.READ);
    return getStorageService(storagePath).listBinaryVersions(storagePath);
  }

  @Override
  public BinaryVersion getBinaryVersion(StoragePath storagePath, String version)
    throws RequestNotValidException, NotFoundException, GenericException {
    registerOperation(storagePath, TransactionalStoragePath.OperationType.READ);
    return getStorageService(storagePath).getBinaryVersion(storagePath, version);
  }

  @Override
  public BinaryVersion createBinaryVersion(StoragePath storagePath, Map<String, String> properties)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    registerOperation(storagePath, TransactionalStoragePath.OperationType.CREATE);
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
    registerOperation(storagePath, TransactionalStoragePath.OperationType.READ);
    return getStorageService(storagePath).getStoragePathAsString(storagePath, skipStoragePathContainer,
      anotherStoragePath, skipAnotherStoragePathContainer);
  }

  @Override
  public String getStoragePathAsString(StoragePath storagePath, boolean skipContainer) {
    registerOperation(storagePath, TransactionalStoragePath.OperationType.READ);
    return getStorageService(storagePath).getStoragePathAsString(storagePath, skipContainer);
  }

  @Override
  public List<StoragePath> getShallowFiles(StoragePath storagePath) throws NotFoundException, GenericException {
    registerOperation(storagePath, TransactionalStoragePath.OperationType.READ);
    return getStorageService(storagePath).getShallowFiles(storagePath);
  }

  @Override
  public void commit() {
    try {
      for (TransactionalStoragePath storagePathLog : RODATransactionManager.getStoragePathsOperations(transaction)) {

        Path path = Paths.get(storagePathLog.getStoragePath());
        List<String> parts = StreamSupport.stream(path.spliterator(), false).map(Path::toString)
          .collect(Collectors.toList());
        DefaultStoragePath storagePath = DefaultStoragePath.parse(parts);

        TransactionalStoragePath.OperationType operationType = storagePathLog.getOperationType();

        if (operationType == TransactionalStoragePath.OperationType.DELETE) {
          if (mainStorageService.exists(storagePath)) {
            try {
              mainStorageService.deleteResource(storagePath);
            } catch (NotFoundException | AuthorizationDeniedException e) {
              throw new TransactionalException("Failed to delete storage path at " + path, e);
            } finally {
              removeOperation(storagePathLog);
            }
          }
        } else {
          if (!stagingStorageService.exists(storagePath)) {
            removeOperation(storagePathLog);
            continue;
          }
          try {
            if (mainStorageService.exists(storagePath)) {
              mainStorageService.deleteResource(storagePath);
            }
            LOGGER.info("Moving resource from staging to main storage service: {}", storagePath);
            StorageServiceUtils.copyBetweenStorageServices(stagingStorageService, storagePath, mainStorageService,
              storagePath, getEntity(storagePath));
          } catch (GenericException | RequestNotValidException | NotFoundException | AlreadyExistsException
            | AuthorizationDeniedException e) {
            // TODO: Handle this exceptions
          } finally {
            removeOperation(storagePathLog);
          }
        }

      }
    } catch (RequestNotValidException | GenericException e) {
      throw new RuntimeException(e);
    }
  }

  private void copyFromMainStorageService(StoragePath storagePath) {
    registerOperation(storagePath, TransactionalStoragePath.OperationType.READ);
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
    RODATransactionManager.releaseLock(transaction);
  }

  private void registerOperation(StoragePath storagePath, TransactionalStoragePath.OperationType operation) {
    if (!storagePath.isFromAContainer()) {
      LOGGER.info("Acquiring lock for storage path: {}", storagePath);
      try {
        RODATransactionManager.registerOperation(transaction, storagePath, operation);
      } catch (InterruptedException | GenericException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void removeOperation(TransactionalStoragePath storagePathLog) {
    LOGGER.info("Releasing lock for storage path: {}", storagePathLog.getStoragePath());
    try {
      RODATransactionManager.removeOperation(transaction, storagePathLog);
    } catch (GenericException e) {
      throw new RuntimeException(e);
    }
  }

  private StorageService getStorageService(StoragePath storagePath) {
    if (stagingStorageService.exists(storagePath)) {
      return stagingStorageService;
    } else {
      return mainStorageService;
    }
  }

}
