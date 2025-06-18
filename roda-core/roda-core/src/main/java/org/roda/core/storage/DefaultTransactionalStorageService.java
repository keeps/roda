package org.roda.core.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.entity.transaction.OperationState;
import org.roda.core.entity.transaction.OperationType;
import org.roda.core.entity.transaction.TransactionLog;
import org.roda.core.entity.transaction.TransactionalStoragePathOperationLog;
import org.roda.core.transaction.ConsolidatedOperation;
import org.roda.core.transaction.RODATransactionException;
import org.roda.core.transaction.TransactionLogConsolidator;
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
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.READ);
    try {
      boolean ret = getEffectiveStorageService(storagePath).exists(storagePath);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException e) {
      boolean ret = false;
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
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
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.CREATE);
    try {
      Container ret = stagingStorageService.createContainer(storagePath);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AlreadyExistsException | GenericException | AuthorizationDeniedException | RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Container getContainer(StoragePath storagePath)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.READ);
    try {
      Container ret = getStorageService(storagePath).getContainer(storagePath);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void deleteContainer(StoragePath storagePath)
    throws NotFoundException, GenericException, AuthorizationDeniedException {
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.DELETE);
    // TODO: What to do in this case?
    try {
      stagingStorageService.deleteContainer(storagePath);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (NotFoundException | GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public CloseableIterable<Resource> listResourcesUnderContainer(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.READ);
    try {
      CloseableIterable<Resource> ret = StorageServiceUtils
        .listTransactionalResourcesUnderContainer(stagingStorageService, mainStorageService, storagePath, recursive);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Long countResourcesUnderContainer(StoragePath storagePath, boolean recursive)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.READ);
    try {
      Long ret = getStorageService(storagePath).countResourcesUnderContainer(storagePath, recursive);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Directory createDirectory(StoragePath storagePath)
    throws AlreadyExistsException, GenericException, AuthorizationDeniedException {
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.CREATE);
    try {
      Directory ret = stagingStorageService.createDirectory(storagePath);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AlreadyExistsException | GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Directory createRandomDirectory(StoragePath parentStoragePath) throws RequestNotValidException,
    GenericException, NotFoundException, AlreadyExistsException, AuthorizationDeniedException {
    TransactionalStoragePathOperationLog operationLog = registerOperation(parentStoragePath, OperationType.CREATE);
    try {
      Directory ret = stagingStorageService.createRandomDirectory(parentStoragePath);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AlreadyExistsException | GenericException | NotFoundException | RequestNotValidException
      | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Directory getDirectory(StoragePath storagePath)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.READ);
    try {
      Directory ret = getStorageService(storagePath).getDirectory(storagePath);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public boolean hasDirectory(StoragePath storagePath) {
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.READ);
    boolean ret = getStorageService(storagePath).hasDirectory(storagePath);
    updateOperationState(operationLog, OperationState.SUCCESS);
    return ret;
  }

  @Override
  public CloseableIterable<Resource> listResourcesUnderDirectory(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.READ);
    try {
      CloseableIterable<Resource> ret = StorageServiceUtils
        .listTransactionalResourcesUnderDirectory(stagingStorageService, mainStorageService, storagePath, recursive);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public CloseableIterable<Resource> listResourcesUnderFile(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.READ);
    try {
      CloseableIterable<Resource> ret = getStorageService(storagePath).listResourcesUnderFile(storagePath, recursive);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Long countResourcesUnderDirectory(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.READ);
    try {
      Long ret = getStorageService(storagePath).countResourcesUnderDirectory(storagePath, recursive);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Binary createBinary(StoragePath storagePath, ContentPayload payload, boolean asReference)
    throws GenericException, AlreadyExistsException, RequestNotValidException, AuthorizationDeniedException,
    NotFoundException {
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.CREATE);
    try {
      Binary ret = stagingStorageService.createBinary(storagePath, payload, asReference);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (AlreadyExistsException | GenericException | RequestNotValidException | AuthorizationDeniedException
      | NotFoundException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Binary createRandomBinary(StoragePath parentStoragePath, ContentPayload payload, boolean asReference)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    TransactionalStoragePathOperationLog operationLog = registerOperation(parentStoragePath, OperationType.CREATE);
    try {
      Binary ret = stagingStorageService.createRandomBinary(parentStoragePath, payload, asReference);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public Binary getBinary(StoragePath storagePath)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.READ);
    try {
      Binary ret = getEffectiveStorageService(storagePath).getBinary(storagePath);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public boolean hasBinary(StoragePath storagePath) {
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.READ);
    boolean ret = getStorageService(storagePath).hasBinary(storagePath);
    updateOperationState(operationLog, OperationState.SUCCESS);
    return ret;
  }

  @Override
  public Binary updateBinaryContent(StoragePath storagePath, ContentPayload payload, boolean asReference,
    boolean createIfNotExists)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    if (mainStorageService.exists(storagePath) || stagingStorageService.exists(storagePath)) {
      TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.UPDATE);
      try {
        Binary ret = stagingStorageService.updateBinaryContent(storagePath, payload, asReference, true);
        updateOperationState(operationLog, OperationState.SUCCESS);
        return ret;
      } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
        updateOperationState(operationLog, OperationState.FAILURE);
        throw e;
      }
    } else if (createIfNotExists) {
      TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.CREATE);
      try {
        Binary ret = stagingStorageService.updateBinaryContent(storagePath, payload, asReference, true);
        updateOperationState(operationLog, OperationState.SUCCESS);
        return ret;
      } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
        updateOperationState(operationLog, OperationState.FAILURE);
        throw e;
      }
    }
    throw new NotFoundException("Storage path does not exist: " + storagePath);
  }

  @Override
  public void deleteResource(StoragePath storagePath)
    throws NotFoundException, GenericException, AuthorizationDeniedException {
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.DELETE);
    try {
      stagingStorageService.deleteResource(storagePath);
      updateOperationState(operationLog, OperationState.SUCCESS);
    } catch (NotFoundException | GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }

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
    List<TransactionalStoragePathOperationLog> operationLogs = registerOperationForCopy(fromService, fromStoragePath,
      toStoragePath, OperationType.CREATE);
    try {
      stagingStorageService.copy(fromService, fromStoragePath, toStoragePath);
      for (TransactionalStoragePathOperationLog operationLog : operationLogs) {
        updateOperationState(operationLog, OperationState.SUCCESS);
      }
    } catch (AlreadyExistsException | GenericException | RequestNotValidException | NotFoundException
      | AuthorizationDeniedException e) {
      for (TransactionalStoragePathOperationLog operationLog : operationLogs) {
        updateOperationState(operationLog, OperationState.FAILURE);
      }
      throw e;
    }
  }

  @Override
  public void copy(StorageService fromService, StoragePath fromStoragePath, Path toPath, String resource,
    boolean replaceExisting) throws AlreadyExistsException, GenericException, AuthorizationDeniedException {
    List<TransactionalStoragePathOperationLog> operationLogs = registerOperationForCopy(fromService, fromStoragePath,
      toPath, OperationType.CREATE);
    try {
      stagingStorageService.copy(fromService, fromStoragePath, toPath, resource, replaceExisting);
      for (TransactionalStoragePathOperationLog operationLog : operationLogs) {
        updateOperationState(operationLog, OperationState.SUCCESS);
      }
    } catch (AlreadyExistsException | GenericException | AuthorizationDeniedException e) {
      for (TransactionalStoragePathOperationLog operationLog : operationLogs) {
        updateOperationState(operationLog, OperationState.FAILURE);
      }
      throw e;
    }

  }

  @Override
  public void move(StorageService fromService, StoragePath fromStoragePath, StoragePath toStoragePath)
    throws AlreadyExistsException, GenericException, RequestNotValidException, NotFoundException,
    AuthorizationDeniedException {
    List<TransactionalStoragePathOperationLog> operationLogs = registerOperationForCopy(fromService, fromStoragePath,
      toStoragePath, OperationType.CREATE);
    operationLogs.addAll(registerOperationForCopy(fromService, fromStoragePath, fromStoragePath, OperationType.DELETE));
    try {
      stagingStorageService.copy(fromService, fromStoragePath, toStoragePath);
      for (TransactionalStoragePathOperationLog operationLog : operationLogs) {
        updateOperationState(operationLog, OperationState.SUCCESS);
      }
    } catch (AlreadyExistsException | GenericException | RequestNotValidException | NotFoundException
      | AuthorizationDeniedException e) {
      for (TransactionalStoragePathOperationLog operationLog : operationLogs) {
        updateOperationState(operationLog, OperationState.FAILURE);
      }
      throw e;
    }

  }

  @Override
  public DirectResourceAccess getDirectAccess(StoragePath storagePath) {
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.READ);
    try {
      if (storagePath.isFromAContainer()) {
        updateOperationState(operationLog, OperationState.FAILURE);
        throw new IllegalArgumentException("Cannot get direct access to a container: " + storagePath);
      }

      // check if transaction has any changes below the storagePath, if not use
      // mainStorageService.getDirectAccess
      if (!transactionLogService.hasModificationsUnderStoragePath(transaction.getId(), storagePath.toString())) {
        DirectResourceAccess ret = mainStorageService.getDirectAccess(storagePath);
        updateOperationState(operationLog, OperationState.SUCCESS);
        return ret;
      }
      // Prepare the staging storage area to be used
      copyMissingResourcesToStagingStorage(storagePath);
      DirectResourceAccess ret = stagingStorageService.getDirectAccess(storagePath);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RODATransactionException | RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw new RuntimeException(e);
    }
  }

  @Override
  public CloseableIterable<BinaryVersion> listBinaryVersions(StoragePath storagePath)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.READ);
    try {
      CloseableIterable<BinaryVersion> ret = getStorageService(storagePath).listBinaryVersions(storagePath);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public BinaryVersion getBinaryVersion(StoragePath storagePath, String version)
    throws RequestNotValidException, NotFoundException, GenericException {
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.READ, version);
    try {
      BinaryVersion ret = getStorageService(storagePath).getBinaryVersion(storagePath, version);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (RequestNotValidException | NotFoundException | GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public BinaryVersion createBinaryVersion(StoragePath storagePath, Map<String, String> properties)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    // Need to copy the resource from main storage to staging storage for internal
    // check
    copyToStagingStorageService(mainStorageService, storagePath);
    BinaryVersion binaryVersion = stagingStorageService.createBinaryVersion(storagePath, properties);
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.CREATE,
      binaryVersion.getId());
    updateOperationState(operationLog, OperationState.SUCCESS);
    return binaryVersion;
  }

  @Override
  public void revertBinaryVersion(StoragePath storagePath, String version)
    throws NotFoundException, RequestNotValidException, GenericException, AuthorizationDeniedException {
    List<TransactionalStoragePathOperationLog> operationLogs = new ArrayList<>(
      List.of(registerOperation(storagePath, OperationType.UPDATE)));
    operationLogs.add(registerOperation(storagePath, OperationType.UPDATE, version));
    copyToStagingStorageService(mainStorageService, storagePath);
    try {
      importBinaryVersion(mainStorageService, storagePath, version);
      try {
        stagingStorageService.revertBinaryVersion(storagePath, version);
        for (TransactionalStoragePathOperationLog operationLog : operationLogs) {
          updateOperationState(operationLog, OperationState.SUCCESS);
        }
      } catch (NotFoundException | RequestNotValidException | GenericException | AuthorizationDeniedException e) {
        for (TransactionalStoragePathOperationLog operationLog : operationLogs) {
          updateOperationState(operationLog, OperationState.FAILURE);
        }
        throw e;
      }
    } catch (AlreadyExistsException e) {
      throw new GenericException("Failed to import binary version", e);
    }
  }

  @Override
  public void deleteBinaryVersion(StoragePath storagePath, String version)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    // TODO: NOT WORKING
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.DELETE, version);
    copyToStagingStorageService(mainStorageService, storagePath);
    try {
      importBinaryVersion(mainStorageService, storagePath, version);
      try {
        stagingStorageService.deleteBinaryVersion(storagePath, version);
        updateOperationState(operationLog, OperationState.SUCCESS);
      } catch (NotFoundException | RequestNotValidException | GenericException | AuthorizationDeniedException e) {
        updateOperationState(operationLog, OperationState.FAILURE);
        throw e;
      }
    } catch (AlreadyExistsException e) {
      throw new GenericException("Failed to import binary version", e);
    }
  }

  @Override
  public String getStoragePathAsString(StoragePath storagePath, boolean skipStoragePathContainer,
    StoragePath anotherStoragePath, boolean skipAnotherStoragePathContainer) {
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.READ);
    String ret = getStorageService(storagePath).getStoragePathAsString(storagePath, skipStoragePathContainer,
      anotherStoragePath, skipAnotherStoragePathContainer);
    updateOperationState(operationLog, OperationState.SUCCESS);
    return ret;
  }

  @Override
  public String getStoragePathAsString(StoragePath storagePath, boolean skipContainer) {
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.READ);
    String ret = getStorageService(storagePath).getStoragePathAsString(storagePath, skipContainer);
    updateOperationState(operationLog, OperationState.SUCCESS);
    return ret;
  }

  @Override
  public List<StoragePath> getShallowFiles(StoragePath storagePath) throws NotFoundException, GenericException {
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.READ);
    List<StoragePath> ret = getStorageService(storagePath).getShallowFiles(storagePath);
    updateOperationState(operationLog, OperationState.SUCCESS);
    return ret;
  }

  @Override
  public Date getCreationDate(StoragePath storagePath) throws GenericException {
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.READ);
    try {
      Date ret = getStorageService(storagePath).getCreationDate(storagePath);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (GenericException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
  }

  @Override
  public void importBinaryVersion(StorageService fromService, StoragePath storagePath, String version)
    throws AlreadyExistsException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    stagingStorageService.importBinaryVersion(fromService, storagePath, version);
  }

  @Override
  public void commit() throws RODATransactionException {
    try {
      Map<StoragePath, List<ConsolidatedOperation>> consolidatedOperations = TransactionLogConsolidator
        .consolidateLogs(transactionLogService.getStoragePathsOperations(transaction.getId()));
      for (Map.Entry<StoragePath, List<ConsolidatedOperation>> consolidatedOperation : consolidatedOperations
        .entrySet()) {
        StoragePath storagePath = consolidatedOperation.getKey();
        List<ConsolidatedOperation> operations = consolidatedOperation.getValue();

        for (ConsolidatedOperation operation : operations) {
          OperationType operationType = operation.operationType();
          String version = operation.version();

          if (operationType == OperationType.DELETE) {
            handleDeleteOperation(storagePath, version);
          } else if (operationType == OperationType.UPDATE) {
            handleUpdateOperation(storagePath, version);
          } else if (operationType == OperationType.CREATE) {
            handleCreateOperation(storagePath, version);
          } else if (operationType == OperationType.READ) {
            // TODO: READ
            LOGGER.debug("Skipping read operation for storage path: {}", storagePath);
          }
        }

      }
    } catch (RequestNotValidException e) {
      throw new RODATransactionException("Failed to consolidate transaction logs", e);
    }
  }

  private void handleDeleteOperation(StoragePath storagePath, String version) throws RODATransactionException {
    try {
      if (version != null) {
        mainStorageService.deleteBinaryVersion(storagePath, version);
      } else {
        mainStorageService.deleteResource(storagePath);
      }
    } catch (NotFoundException | AuthorizationDeniedException | GenericException | RequestNotValidException e) {
      throw new RODATransactionException("Failed to delete storage path at " + storagePath, e);
    }
  }

  private void handleUpdateOperation(StoragePath storagePath, String version) throws RODATransactionException {
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

  private void handleCreateOperation(StoragePath storagePath, String version) throws RODATransactionException {
    try {
      if (version != null) {
        LOGGER.info("Creating binary version from staging to main storage service: {}", storagePath);
        mainStorageService.importBinaryVersion(stagingStorageService, storagePath, version);
      } else {
        LOGGER.info("Moving resource from staging to main storage service: {}", storagePath);
        Class<? extends Entity> rootEntity = stagingStorageService.getEntity(storagePath);
        // TODO: This is necessary to avoid recursive copies, we should handle it better
        // in StorageServiceUtils
        if (Container.class.isAssignableFrom(rootEntity)) {
          mainStorageService.createContainer(storagePath);
        } else if (Directory.class.isAssignableFrom(rootEntity)) {
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

  private List<TransactionalStoragePathOperationLog> registerOperationForCopy(StorageService fromService,
    StoragePath fromStoragePath, Path toPath, OperationType operation)
    throws AuthorizationDeniedException, GenericException {
    try {
      List<String> pathParts = new ArrayList<>();
      for (Path part : toPath) {
        pathParts.add(part.toString());
      }
      return registerOperationForCopy(fromService, fromStoragePath, DefaultStoragePath.parse(pathParts), operation);
    } catch (RequestNotValidException e) {
      throw new GenericException("Failed to register operation for copy: " + toPath, e);
    }

  }

  private List<TransactionalStoragePathOperationLog> registerOperationForCopy(StorageService fromService,
    StoragePath fromStoragePath, StoragePath toStoragePath, OperationType operation)
    throws AuthorizationDeniedException, GenericException {
    List<TransactionalStoragePathOperationLog> ret = new ArrayList<>(
      List.of(registerOperation(toStoragePath, operation)));
    try (CloseableIterable<Resource> listResourcesUnderDirectory = fromService
      .listResourcesUnderDirectory(fromStoragePath, true)) {
      if (listResourcesUnderDirectory == null) {
        return ret;
      }

      String fromStoragePathAsString = getStoragePathAsString(fromStoragePath, false);
      String toStoragePathAsString = getStoragePathAsString(toStoragePath, false);

      // register operation for each resource under the storage path
      for (Resource resource : listResourcesUnderDirectory) {
        StoragePath resourceStoragePath = resource.getStoragePath();
        String destinationStoragePathAsString = getStoragePathAsString(resourceStoragePath, false)
          .replace(fromStoragePathAsString, toStoragePathAsString);
        Arrays.asList(Paths.get(destinationStoragePathAsString));
        ret.add(registerOperation(destinationStoragePathAsString, operation, null));
      }
    } catch (NotFoundException | RequestNotValidException | IOException e) {
      throw new GenericException("Failed to register operation for copy: " + toStoragePath, e);
    }
    return ret;
  }

  private TransactionalStoragePathOperationLog registerOperation(StoragePath storagePath, OperationType operation) {
    return registerOperation(storagePath, operation, null);
  }

  private TransactionalStoragePathOperationLog registerOperation(StoragePath storagePath, OperationType operation,
    String version) {
    if (storagePath.getName().equals(RodaConstants.STORAGE_DIRECTORY_AGENTS)) {
      return null;
    }

    String storagePathAsString = stagingStorageService.getStoragePathAsString(storagePath, false);
    return registerOperation(storagePathAsString, operation, version);
  }

  private TransactionalStoragePathOperationLog registerOperation(String storagePathAsString, OperationType operation,
    String version) {
    LOGGER.debug("Registering operation for storage path: {}", storagePathAsString);
    try {
      return transactionLogService.registerStoragePathOperation(transaction.getId(), storagePathAsString, operation,
        version);
    } catch (RODATransactionException e) {
      throw new IllegalArgumentException("Cannot register operation for storagePath: " + storagePathAsString, e);
    }
  }

  public void updateOperationState(TransactionalStoragePathOperationLog operationLog, OperationState state) {
    try {
      if (operationLog != null) {
        transactionLogService.updateStoragePathOperationState(operationLog.getId(), state);
      }
    } catch (RODATransactionException e) {
      throw new IllegalArgumentException("Cannot update operation state: " + operationLog.getId(), e);
    }
  }

  private StorageService getStorageService(StoragePath storagePath) {
    if (storagePath.isFromAContainer()) {
      return mainStorageService;
    }
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
      // TODO gbarros: remove second condition on rebase
      if (storagePathOperation.isPresent()
        && storagePathOperation.get().getOperationState().equals(OperationState.SUCCESS)) {
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
