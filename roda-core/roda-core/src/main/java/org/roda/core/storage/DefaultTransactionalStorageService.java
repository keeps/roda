package org.roda.core.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.entity.transaction.OperationType;
import org.roda.core.entity.transaction.TransactionLog;
import org.roda.core.entity.transaction.TransactionalStoragePathOperationLog;
import org.roda.core.transaction.RODATransactionException;
import org.roda.core.transaction.TransactionLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DefaultTransactionalStorageService implements TransactionalStorageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTransactionalStorageService.class);

  private StorageService stagingStorageService;
  private StorageService mainStorageService;
  private TransactionLog transaction;
  private final TransactionLogService transactionLogService;

  public DefaultTransactionalStorageService(StorageService mainStorageService, StorageService stagingStorageService,
    TransactionLog transaction, TransactionLogService transactionLogService) {
    this.mainStorageService = mainStorageService;
    this.stagingStorageService = stagingStorageService;
    this.transaction = transaction;
    this.transactionLogService = transactionLogService;
  }

  @Override
  public boolean exists(StoragePath storagePath) {
    registerOperation(storagePath, OperationType.READ);
    try {
      return getEffectiveStorageService(storagePath).exists(storagePath);
    } catch (NotFoundException e) {
      return false;
    } catch (GenericException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public CloseableIterable<Container> listContainers()
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    return mainStorageService.listContainers();
  }

  @Override
  public Container createContainer(StoragePath storagePath)
    throws GenericException, AlreadyExistsException, AuthorizationDeniedException, RequestNotValidException {
    registerOperation(storagePath, OperationType.CREATE);
    return stagingStorageService.createContainer(storagePath);
  }

  @Override
  public Container getContainer(StoragePath storagePath)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    registerOperation(storagePath, OperationType.READ);
    return getStorageService(storagePath).getContainer(storagePath);
  }

  @Override
  public void deleteContainer(StoragePath storagePath)
    throws NotFoundException, GenericException, AuthorizationDeniedException {
    registerOperation(storagePath, OperationType.DELETE);
    // TODO: What to do in this case?
    stagingStorageService.deleteContainer(storagePath);
  }

  @Override
  public CloseableIterable<Resource> listResourcesUnderContainer(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    registerOperation(storagePath, OperationType.READ);
    return StorageServiceUtils.listTransactionalResourcesUnderContainer(stagingStorageService, mainStorageService,
      storagePath, recursive);
  }

  @Override
  public Long countResourcesUnderContainer(StoragePath storagePath, boolean recursive)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    registerOperation(storagePath, OperationType.READ);
    return getStorageService(storagePath).countResourcesUnderContainer(storagePath, recursive);
  }

  @Override
  public Directory createDirectory(StoragePath storagePath)
    throws AlreadyExistsException, GenericException, AuthorizationDeniedException {
    registerOperation(storagePath, OperationType.CREATE);
    return stagingStorageService.createDirectory(storagePath);
  }

  @Override
  public Directory createRandomDirectory(StoragePath parentStoragePath) throws RequestNotValidException,
    GenericException, NotFoundException, AlreadyExistsException, AuthorizationDeniedException {
    registerOperation(parentStoragePath, OperationType.CREATE);
    return stagingStorageService.createRandomDirectory(parentStoragePath);
  }

  @Override
  public Directory getDirectory(StoragePath storagePath)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    registerOperation(storagePath, OperationType.READ);
    return getStorageService(storagePath).getDirectory(storagePath);
  }

  @Override
  public boolean hasDirectory(StoragePath storagePath) {
    registerOperation(storagePath, OperationType.READ);
    return getStorageService(storagePath).hasDirectory(storagePath);
  }

  @Override
  public CloseableIterable<Resource> listResourcesUnderDirectory(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    registerOperation(storagePath, OperationType.READ);
    return StorageServiceUtils.listTransactionalResourcesUnderDirectory(stagingStorageService, mainStorageService,
      storagePath, recursive);
  }

  @Override
  public CloseableIterable<Resource> listResourcesUnderFile(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    registerOperation(storagePath, OperationType.READ);
    return getStorageService(storagePath).listResourcesUnderFile(storagePath, recursive);
  }

  @Override
  public Long countResourcesUnderDirectory(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    registerOperation(storagePath, OperationType.READ);
    return getStorageService(storagePath).countResourcesUnderDirectory(storagePath, recursive);
  }

  @Override
  public Binary createBinary(StoragePath storagePath, ContentPayload payload, boolean asReference)
    throws GenericException, AlreadyExistsException, RequestNotValidException, AuthorizationDeniedException,
    NotFoundException {
    registerOperation(storagePath, OperationType.CREATE);
    return stagingStorageService.createBinary(storagePath, payload, asReference);
  }

  @Override
  public Binary createRandomBinary(StoragePath parentStoragePath, ContentPayload payload, boolean asReference)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    registerOperation(parentStoragePath, OperationType.CREATE);
    return stagingStorageService.createRandomBinary(parentStoragePath, payload, asReference);
  }

  @Override
  public Binary getBinary(StoragePath storagePath)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    registerOperation(storagePath, OperationType.READ);
    return getEffectiveStorageService(storagePath).getBinary(storagePath);
  }

  @Override
  public boolean hasBinary(StoragePath storagePath) {
    registerOperation(storagePath, OperationType.READ);
    return getStorageService(storagePath).hasBinary(storagePath);
  }

  @Override
  public Binary updateBinaryContent(StoragePath storagePath, ContentPayload payload, boolean asReference,
    boolean createIfNotExists)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {

    if (mainStorageService.exists(storagePath) || stagingStorageService.exists(storagePath)) {
      registerOperation(storagePath, OperationType.UPDATE);
      return stagingStorageService.updateBinaryContent(storagePath, payload, asReference, true);
    } else if (createIfNotExists) {
      registerOperation(storagePath, OperationType.CREATE);
      return stagingStorageService.updateBinaryContent(storagePath, payload, asReference, true);
    }

    throw new NotFoundException("Storage path does not exist: " + storagePath);
  }

  @Override
  public void deleteResource(StoragePath storagePath)
    throws NotFoundException, GenericException, AuthorizationDeniedException {
    registerOperation(storagePath, OperationType.DELETE);
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
    stagingStorageService.copy(fromService, fromStoragePath, toStoragePath);
    registerOperationForCopy(stagingStorageService, toStoragePath, OperationType.CREATE);
  }

  @Override
  public void copy(StorageService fromService, StoragePath fromStoragePath, Path toPath, String resource,
    boolean replaceExisting) throws AlreadyExistsException, GenericException, AuthorizationDeniedException {
    stagingStorageService.copy(fromService, fromStoragePath, toPath, resource, replaceExisting);
    registerOperationForCopy(stagingStorageService, toPath, OperationType.CREATE);
  }

  @Override
  public void move(StorageService fromService, StoragePath fromStoragePath, StoragePath toStoragePath)
    throws AlreadyExistsException, GenericException, RequestNotValidException, NotFoundException,
    AuthorizationDeniedException {
    stagingStorageService.copy(fromService, fromStoragePath, toStoragePath);
    registerOperationForCopy(stagingStorageService, toStoragePath, OperationType.CREATE);
    registerOperationForCopy(mainStorageService, fromStoragePath, OperationType.DELETE);
  }

  @Override
  public DirectResourceAccess getDirectAccess(StoragePath storagePath) {
    registerOperation(storagePath, OperationType.READ);
    try {
      if (storagePath.isFromAContainer()) {
        throw new IllegalArgumentException("Cannot get direct access to a container: " + storagePath);
      }

      // check if transaction has any changes below the storagePath, if not use
      // mainStorageService.getDirectAccess
      if (!transactionLogService.hasModificationsUnderStoragePath(transaction.getId(), storagePath.toString())) {
        return mainStorageService.getDirectAccess(storagePath);
      }
      // Prepare the staging storage area to be used
      copyMissingResourcesToStagingStorage(storagePath);
      return stagingStorageService.getDirectAccess(storagePath);
    } catch (RODATransactionException | RequestNotValidException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public CloseableIterable<BinaryVersion> listBinaryVersions(StoragePath storagePath)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    registerOperation(storagePath, OperationType.READ);
    return getStorageService(storagePath).listBinaryVersions(storagePath);
  }

  @Override
  public BinaryVersion getBinaryVersion(StoragePath storagePath, String version)
    throws RequestNotValidException, NotFoundException, GenericException {
    registerOperation(storagePath, OperationType.READ, version);
    return getStorageService(storagePath).getBinaryVersion(storagePath, version);
  }

  @Override
  public BinaryVersion createBinaryVersion(StoragePath storagePath, Map<String, String> properties)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    // Need to copy the resource from main storage to staging storage for internal
    // check
    copyToStagingStorageService(mainStorageService, storagePath);
    BinaryVersion binaryVersion = stagingStorageService.createBinaryVersion(storagePath, properties);
    registerOperation(storagePath, OperationType.CREATE, binaryVersion.getId());
    return binaryVersion;
  }

  @Override
  public void revertBinaryVersion(StoragePath storagePath, String version)
    throws NotFoundException, RequestNotValidException, GenericException, AuthorizationDeniedException {
    registerOperation(storagePath, OperationType.UPDATE);
    registerOperation(storagePath, OperationType.UPDATE, version);
    copyToStagingStorageService(mainStorageService, storagePath);
    try {
      importBinaryVersion(mainStorageService, storagePath, version);
      stagingStorageService.revertBinaryVersion(storagePath, version);
    } catch (AlreadyExistsException e) {
      throw new GenericException("Failed to import binary version", e);
    }
  }

  @Override
  public void deleteBinaryVersion(StoragePath storagePath, String version)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    // TODO: NOT WORKING
    registerOperation(storagePath, OperationType.DELETE, version);
    copyToStagingStorageService(mainStorageService, storagePath);
    try {
      importBinaryVersion(mainStorageService, storagePath, version);
      stagingStorageService.deleteBinaryVersion(storagePath, version);
    } catch (AlreadyExistsException e) {
      throw new GenericException("Failed to import binary version", e);
    }
  }

  @Override
  public String getStoragePathAsString(StoragePath storagePath, boolean skipStoragePathContainer,
    StoragePath anotherStoragePath, boolean skipAnotherStoragePathContainer) {
    registerOperation(storagePath, OperationType.READ);
    return getStorageService(storagePath).getStoragePathAsString(storagePath, skipStoragePathContainer,
      anotherStoragePath, skipAnotherStoragePathContainer);
  }

  @Override
  public String getStoragePathAsString(StoragePath storagePath, boolean skipContainer) {
    registerOperation(storagePath, OperationType.READ);
    return getStorageService(storagePath).getStoragePathAsString(storagePath, skipContainer);
  }

  @Override
  public List<StoragePath> getShallowFiles(StoragePath storagePath) throws NotFoundException, GenericException {
    registerOperation(storagePath, OperationType.READ);
    return getStorageService(storagePath).getShallowFiles(storagePath);
  }

  @Override
  public Date getCreationDate(StoragePath storagePath) throws GenericException {
    registerOperation(storagePath, OperationType.READ);
    return getStorageService(storagePath).getCreationDate(storagePath);
  }

  @Override
  public void importBinaryVersion(StorageService fromService, StoragePath storagePath, String version)
    throws AlreadyExistsException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    stagingStorageService.importBinaryVersion(fromService, storagePath, version);
  }

  @Override
  public void commit() throws RODATransactionException {
    for (TransactionalStoragePathOperationLog storagePathLog : getStoragePathsOperations(transaction)) {
      Path path = Paths.get(storagePathLog.getStoragePath());
      List<String> parts = StreamSupport.stream(path.spliterator(), false).map(Path::toString)
        .collect(Collectors.toList());
      DefaultStoragePath storagePath = null;
      try {
        storagePath = DefaultStoragePath.parse(parts);

        OperationType operationType = storagePathLog.getOperationType();
        String version = storagePathLog.getVersion();

        if (operationType == OperationType.DELETE) {
          handleDeleteOperation(storagePath, path, version);
        } else if (operationType == OperationType.UPDATE) {
          handleUpdateOperation(storagePath, version);
        } else if (operationType == OperationType.CREATE) {
          handleCreateOperation(storagePath, version);
        } else {
          // TODO: READ
          LOGGER.debug("Skipping read operation for storage path: {}", storagePathLog.getStoragePath());
        }
      } catch (RequestNotValidException e) {
        throw new RODATransactionException("Failed to parse storage path: " + path, e);
      }
    }
  }

  private void handleDeleteOperation(DefaultStoragePath storagePath, Path path, String version)
    throws RODATransactionException {
    if (mainStorageService.exists(storagePath)) {
      try {
        if (version != null) {
          mainStorageService.deleteBinaryVersion(storagePath, version);
        } else {
          mainStorageService.deleteResource(storagePath);
        }
      } catch (NotFoundException | AuthorizationDeniedException | GenericException | RequestNotValidException e) {
        throw new RODATransactionException("Failed to delete storage path at " + path, e);
      }
    }
  }

  private void handleUpdateOperation(DefaultStoragePath storagePath, String version) throws RODATransactionException {
    try {
      if (version != null) {
        LOGGER.info("Importing binary version from staging to main storage service: {}", storagePath);
        mainStorageService.importBinaryVersion(stagingStorageService, storagePath, version);
      } else {
        LOGGER.info("Updating resource from staging to main storage service: {}", storagePath);
        StorageServiceUtils.syncBetweenStorageServices(stagingStorageService, storagePath, mainStorageService,
          storagePath, getEntity(storagePath));
      }
    } catch (GenericException | RequestNotValidException | NotFoundException | AlreadyExistsException
      | AuthorizationDeniedException e) {
      throw new RODATransactionException(
        "Failed to sync storage path from staging to main storage service: " + storagePath, e);
    }
  }

  private void handleCreateOperation(DefaultStoragePath storagePath, String version) throws RODATransactionException {
    try {
      if (version != null) {
        LOGGER.info("Creating binary version from staging to main storage service: {}", storagePath);
        mainStorageService.importBinaryVersion(stagingStorageService, storagePath, version);
      } else {
        LOGGER.info("Moving resource from staging to main storage service: {}", storagePath);
        Class<? extends Entity> rootEntity = getEntity(storagePath);
        // TODO: This is necessary to avoid recursive copies, we should handle it better
        // in StorageServiceUtils
        if (Directory.class.isAssignableFrom(rootEntity)) {
          mainStorageService.createDirectory(storagePath);
        } else {
          StorageServiceUtils.copyBetweenStorageServices(stagingStorageService, storagePath, mainStorageService,
            storagePath, rootEntity);
        }
      }
    } catch (GenericException | RequestNotValidException | NotFoundException | AlreadyExistsException
      | AuthorizationDeniedException e) {
      throw new RODATransactionException("Failed to copy storage path from staging to main storage service: "
        + storagePath + ". (transactionID:" + transaction.getId() + ")", e);
    }
  }

  private void handleCopyOperation(DefaultStoragePath storagePath, String version) throws RODATransactionException {
    try {
      if (version != null) {
        LOGGER.info("Creating binary version from staging to main storage service: {}", storagePath);
        mainStorageService.importBinaryVersion(stagingStorageService, storagePath, version);
      } else {
        LOGGER.info("Moving resource from staging to main storage service: {}", storagePath);
        Class<? extends Entity> rootEntity = getEntity(storagePath);
        StorageServiceUtils.copyBetweenStorageServices(stagingStorageService, storagePath, mainStorageService,
          storagePath, rootEntity);
      }
    } catch (GenericException | RequestNotValidException | NotFoundException | AlreadyExistsException
      | AuthorizationDeniedException e) {
      throw new RODATransactionException("Failed to copy storage path from staging to main storage service: "
        + storagePath + ". (transactionID:" + transaction.getId() + ")", e);
    }
  }

  private List<TransactionalStoragePathOperationLog> getStoragePathsOperations(TransactionLog transaction)
    throws RODATransactionException {
    return transactionLogService.getStoragePathsOperations(transaction.getId());
  }

  private void copyMissingResourcesToStagingStorage(StoragePath storagePath)
    throws RequestNotValidException, RODATransactionException {
    if (!mainStorageService.exists(storagePath)) {
      return;
    }

    try (CloseableIterable<Resource> listResourcesUnderDirectory = mainStorageService
      .listResourcesUnderDirectory(storagePath, true)) {
      if (listResourcesUnderDirectory == null) {
        return;
      }

      Set<String> storagePathsOperations = transactionLogService
        .listModificationsUnderStoragePath(transaction.getId(), storagePath.toString()).stream()
        .map(TransactionalStoragePathOperationLog::getStoragePath).collect(Collectors.toSet());

      for (Resource resource : listResourcesUnderDirectory) {
        StoragePath resourceStoragePath = resource.getStoragePath();
        if (!storagePathsOperations.contains(resourceStoragePath.toString())) {
          StoragePath parsedPath = DefaultStoragePath.parse(resourceStoragePath);
          copyToStagingStorageService(mainStorageService, parsedPath);
        }
      }

    } catch (NotFoundException | GenericException | AuthorizationDeniedException | IOException e) {
      throw new RODATransactionException("Failed to copy resources from main storage to staging storage", e);
    }
  }

  private void copyToStagingStorageService(StorageService fromStorage, StoragePath storagePath) {
    try {
      StorageServiceUtils.copyBetweenStorageServices(fromStorage, storagePath, stagingStorageService, storagePath,
        fromStorage.getEntity(storagePath));
    } catch (AuthorizationDeniedException | RequestNotValidException | AlreadyExistsException | NotFoundException
      | GenericException e) {
      // Do nothing
    }
  }

  @Override
  public void rollback() throws RODATransactionException {
    LOGGER.warn("Rolling back transaction: {}", transaction.getId());
    // DO NOTHING
  }

  private void registerOperationForCopy(StorageService toService, Path toPath, OperationType operation)
    throws AuthorizationDeniedException, GenericException {
    try {
      List<String> pathParts = new ArrayList<>();
      for (Path part : toPath) {
        pathParts.add(part.toString());
      }
      registerOperationForCopy(toService, DefaultStoragePath.parse(pathParts), operation);
    } catch (RequestNotValidException e) {
      throw new GenericException("Failed to register operation for copy: " + toPath, e);
    }

  }

  private void registerOperationForCopy(StorageService toService, StoragePath toStoragePath, OperationType operation)
    throws AuthorizationDeniedException, GenericException {
    registerOperation(toStoragePath, operation);
    try (CloseableIterable<Resource> listResourcesUnderDirectory = toService.listResourcesUnderDirectory(toStoragePath,
      true)) {
      if (listResourcesUnderDirectory == null) {
        return;
      }
      // register operation for each resource under the storage path
      for (Resource resource : listResourcesUnderDirectory) {
        StoragePath resourceStoragePath = resource.getStoragePath();
        registerOperation(resourceStoragePath, operation);
      }
    } catch (NotFoundException | RequestNotValidException | IOException e) {
      throw new GenericException("Failed to register operation for copy: " + toStoragePath, e);
    }
  }

  private void registerOperation(StoragePath storagePath, OperationType operation) {
    registerOperation(storagePath, operation, null);
  }

  private void registerOperation(StoragePath storagePath, OperationType operation, String version) {
    if (storagePath.isFromAContainer()) {
      return;
    }

    if (storagePath.getName().equals(RodaConstants.STORAGE_DIRECTORY_AGENTS)) {
      return;
    }

    LOGGER.debug("Registering operation for storage path: {}", storagePath);
    try {
      String storagePathAsString = stagingStorageService.getStoragePathAsString(storagePath, false);
      transactionLogService.registerStoragePathOperation(transaction.getId(), storagePathAsString, operation, version);
    } catch (RODATransactionException e) {
      LOGGER.error("Failed to register operation for storage path: {}", storagePath, e);
    }
  }

  private StorageService getStorageService(StoragePath storagePath) {
    if (stagingStorageService.exists(storagePath)) {
      return stagingStorageService;
    } else {
      return mainStorageService;
    }
  }

  private StorageService getEffectiveStorageService(StoragePath storagePath)
    throws NotFoundException, GenericException {
    String storagePathAsString = stagingStorageService.getStoragePathAsString(storagePath, false);
    try {
      Optional<TransactionalStoragePathOperationLog> storagePathOperation = transactionLogService
        .getLastStoragePathOperation(transaction.getId(), storagePathAsString);
      if (storagePathOperation.isPresent()) {
        return switch (storagePathOperation.get().getOperationType()) {
          case DELETE -> throw new NotFoundException("Resource was deleted in this transaction.");
          case CREATE, UPDATE -> stagingStorageService;
          default ->
            throw new GenericException("Unexpected operation type: " + storagePathOperation.get().getOperationType());
        };
      }
      return mainStorageService;
    } catch (RODATransactionException e) {
      throw new GenericException("Failed to get effective storage service for storage path: " + storagePath, e);
    }
  }
}
