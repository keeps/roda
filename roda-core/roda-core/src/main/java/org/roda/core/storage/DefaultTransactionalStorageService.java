package org.roda.core.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.roda.core.entity.transaction.OperationState;
import org.roda.core.entity.transaction.OperationType;
import org.roda.core.entity.transaction.TransactionLog;
import org.roda.core.entity.transaction.TransactionStoragePathConsolidatedOperation;
import org.roda.core.entity.transaction.TransactionalStoragePathOperationLog;
import org.roda.core.transaction.ConsolidatedOperation;
import org.roda.core.transaction.RODATransactionException;
import org.roda.core.transaction.StoragePathVersion;
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
  private boolean isInitialized = false;

  public DefaultTransactionalStorageService(StorageService mainStorageService, StorageService stagingStorageService,
    TransactionLog transaction, TransactionLogService transactionLogService) {
    this.mainStorageService = mainStorageService;
    this.stagingStorageService = stagingStorageService;
    this.transaction = transaction;
    this.transactionLogService = transactionLogService;
  }

  public void setInitialized(boolean initialized) {
    isInitialized = initialized;
  }

  @Override
  public StorageService getStagingStorageService() {
    return stagingStorageService;
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
    } catch (NotFoundException e) {
      // ignored
    } catch (GenericException | AuthorizationDeniedException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    }
    updateOperationState(operationLog, OperationState.SUCCESS);
  }

  @Override
  public CloseableIterable<Resource> listResourcesUnderContainer(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.READ);
    try {
      HashSet<StoragePath> deletedStoragePaths = new HashSet<>();
      List<TransactionalStoragePathOperationLog> deletedStoragePathOperations = transactionLogService
        .getStoragePathsOperations(transaction.getId(), OperationType.DELETE);
      for (TransactionalStoragePathOperationLog deletedStoragePathOperation : deletedStoragePathOperations) {
        deletedStoragePaths.add(DefaultStoragePath.parse(deletedStoragePathOperation.getStoragePath()));
      }
      CloseableIterable<Resource> ret = StorageServiceUtils.listTransactionalResourcesUnderContainer(
        stagingStorageService, mainStorageService, storagePath, deletedStoragePaths, recursive);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    } catch (RODATransactionException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw new GenericException(
        "[transactionId:" + transaction.getId() + "] Failed to retrieve storage paths operations from database", e);
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
      HashSet<StoragePath> deletedStoragePaths = new HashSet<>();
      List<TransactionalStoragePathOperationLog> deletedStoragePathOperations = transactionLogService
        .getStoragePathsOperations(transaction.getId(), OperationType.DELETE);
      for (TransactionalStoragePathOperationLog deletedStoragePathOperation : deletedStoragePathOperations) {
        deletedStoragePaths.add(DefaultStoragePath.parse(deletedStoragePathOperation.getStoragePath()));
      }
      CloseableIterable<Resource> ret = StorageServiceUtils.listTransactionalResourcesUnderDirectory(
        stagingStorageService, mainStorageService, storagePath, deletedStoragePaths, recursive);
      updateOperationState(operationLog, OperationState.SUCCESS);
      return ret;
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw e;
    } catch (RODATransactionException e) {
      updateOperationState(operationLog, OperationState.FAILURE);
      throw new GenericException(
        "[transactionId:" + transaction.getId() + "] Failed to retrieve storage paths operations from database", e);
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
    TransactionalStoragePathOperationLog operationLog;
    // if storage path is agent we need to register a create or update operation
    if (storagePath.getDirectoryPath() != null && !storagePath.getDirectoryPath().isEmpty()
      && storagePath.getDirectoryPath().getFirst().equals(RodaConstants.STORAGE_DIRECTORY_AGENTS)) {
      operationLog = registerOperation(storagePath, OperationType.CREATE_OR_UPDATE);
    } else {
      operationLog = registerOperation(storagePath, OperationType.CREATE);
    }
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
    boolean createIfNotExists, boolean snapshotCurrentVersion)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    if (mainStorageService.exists(storagePath) || stagingStorageService.exists(storagePath)) {
      TransactionalStoragePathOperationLog updateBinaryLog = registerOperation(storagePath, OperationType.UPDATE);
      if (!stagingStorageService.exists(storagePath)) {
        try {
          stagingStorageService.copy(mainStorageService, storagePath, storagePath);
        } catch (AlreadyExistsException e) {
          LOGGER.warn("Failed to copy binary to staging storage before update", e);
        }
      }
      try {
        Binary ret = stagingStorageService.updateBinaryContent(storagePath, payload, asReference, true,
          snapshotCurrentVersion);
        updateOperationState(updateBinaryLog, OperationState.SUCCESS, ret.getPreviousVersionId(), null);
        return ret;
      } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
        updateOperationState(updateBinaryLog, OperationState.FAILURE);
        throw e;
      }
    } else if (createIfNotExists) {
      TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.CREATE);
      try {
        Binary ret = stagingStorageService.updateBinaryContent(storagePath, payload, asReference, true, false);
        updateOperationState(operationLog, OperationState.SUCCESS);
        return ret;
      } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
        updateOperationState(operationLog, OperationState.FAILURE);
        throw e;
      }
    }
    throw new NotFoundException(
      "[transactionId:" + transaction.getId() + "] Storage path does not exist: " + storagePath);
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
        throw new IllegalArgumentException(
          "[transactionId:" + transaction.getId() + "] Cannot get direct access to a container: " + storagePath);
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
      // TODO: Handle this exception properly
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
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.READ, null,
      version);
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
  public Binary revertBinaryVersion(StoragePath storagePath, String version)
    throws NotFoundException, RequestNotValidException, GenericException, AuthorizationDeniedException {
    TransactionalStoragePathOperationLog updateBinaryOperation = registerOperation(storagePath, OperationType.UPDATE);
    copyToStagingStorageService(mainStorageService, storagePath);
    try {
      importBinaryVersion(mainStorageService, storagePath, version);
      try {
        Binary ret = stagingStorageService.revertBinaryVersion(storagePath, version);
        if (ret.getPreviousVersionId() != null) {
          updateOperationState(updateBinaryOperation, OperationState.SUCCESS, ret.getPreviousVersionId(), null);
        }
        updateOperationState(updateBinaryOperation, OperationState.SUCCESS);
        return ret;
      } catch (NotFoundException | RequestNotValidException | GenericException | AuthorizationDeniedException e) {
        updateOperationState(updateBinaryOperation, OperationState.FAILURE);
        throw e;
      }
    } catch (AlreadyExistsException e) {
      throw new GenericException("[transactionId:" + transaction.getId() + "] Failed to import binary version", e);
    }
  }

  @Override
  public void deleteBinaryVersion(StoragePath storagePath, String version)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    // TODO: NOT WORKING
    TransactionalStoragePathOperationLog operationLog = registerOperation(storagePath, OperationType.DELETE, null,
      version);
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
      throw new GenericException("[transactionId:" + transaction.getId() + "] Failed to import binary version", e);
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

  private Map<StoragePathVersion, List<TransactionStoragePathConsolidatedOperation>> consolidateAndRegisterLogs()
    throws RequestNotValidException, RODATransactionException {
    Map<StoragePathVersion, List<ConsolidatedOperation>> consolidatedOperations = TransactionLogConsolidator
      .consolidateLogs(transactionLogService.getStoragePathsOperations(transaction.getId()));
    Map<StoragePathVersion, List<TransactionStoragePathConsolidatedOperation>> databaseOperationsMap = new HashMap<>();
    for (Map.Entry<StoragePathVersion, List<ConsolidatedOperation>> consolidatedOperation : consolidatedOperations
      .entrySet()) {
      StoragePath storagePath = consolidatedOperation.getKey().storagePath();
      String version = consolidatedOperation.getKey().version();
      List<ConsolidatedOperation> operations = consolidatedOperation.getValue();
      List<TransactionStoragePathConsolidatedOperation> databaseOperations = transactionLogService
        .registerConsolidatedStoragePathOperations(transaction, getStoragePathAsString(storagePath, false), version,
          operations);
      databaseOperationsMap.put(consolidatedOperation.getKey(), databaseOperations);
    }
    return databaseOperationsMap;
  }

  @Override
  public void commit() throws RODATransactionException {
    try {
      consolidateAndRegisterLogs();
    } catch (RequestNotValidException e) {
      throw new RODATransactionException(
        "[transactionId:" + transaction.getId() + "] Failed to consolidate transaction logs", e);
    }

    List<TransactionStoragePathConsolidatedOperation> databaseOperations = transactionLogService
      .getConsolidatedStoragePathOperations(transaction);

    for (TransactionStoragePathConsolidatedOperation operation : databaseOperations) {
      StoragePath storagePath;
      try {
        storagePath = DefaultStoragePath
          .parse(StreamSupport.stream(Paths.get(operation.getStoragePath()).spliterator(), false).map(Path::toString)
            .collect(Collectors.toList()));
      } catch (RequestNotValidException e) {
        transactionLogService.updateConsolidatedStoragePathOperationState(operation, OperationState.FAILURE);
        throw new RODATransactionException(
          "[transactionId:" + transaction.getId() + "] Failed to parse storage path: " + operation.getStoragePath(), e);
      }
      String version = operation.getVersion();
      String previousVersion = operation.getPreviousVersion();
      try {
        transactionLogService.updateConsolidatedStoragePathOperationState(operation, OperationState.RUNNING);
        OperationType operationType = operation.getOperationType();
        String previousVersionId = null;
        if (operationType == OperationType.DELETE) {
          handleDeleteOperation(storagePath, version);
        } else if (operationType == OperationType.UPDATE) {
          previousVersionId = handleUpdateOperation(storagePath, previousVersion, version);
        } else if (operationType == OperationType.CREATE) {
          handleCreateOperation(storagePath, version);
        } else if (operationType == OperationType.CREATE_OR_UPDATE) {
          handleCreateUpdateOperation(storagePath, version);
        } else if (operationType == OperationType.READ) {
          LOGGER.debug("[transactionId:{}] Skipping read operation for storage path: {}", transaction.getId(),
            storagePath);
        }
        transactionLogService.updateConsolidatedStoragePathOperationState(operation, OperationState.SUCCESS,
          previousVersionId);
      } catch (RODATransactionException e) {
        transactionLogService.updateConsolidatedStoragePathOperationState(operation, OperationState.FAILURE);
        throw e;
      }
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
      throw new RODATransactionException(
        "[transactionId:" + transaction.getId() + "] Failed to delete storage path at " + storagePath, e);
    }
  }

  private String handleUpdateOperation(StoragePath storagePath, String previousVersion, String version)
    throws RODATransactionException {
    try {
      if (version != null) {
        LOGGER.info("[transactionId:{}] Importing binary version from staging to main storage service: {}",
          transaction.getId(), storagePath);
        mainStorageService.importBinaryVersion(stagingStorageService, storagePath, version);
        return null;
      } else {
        LOGGER.info("[transactionId:{}] Updating resource from staging to main storage service: {}",
          transaction.getId(), storagePath);
        if (previousVersion != null) {
          Binary stagingBinary = stagingStorageService.getBinary(storagePath);
          Binary mainStorageBinary = mainStorageService.updateBinaryContent(storagePath, stagingBinary.getContent(),
            false, false, true);
          return mainStorageBinary.getPreviousVersionId();
        }

        StorageServiceUtils.syncBetweenStorageServices(stagingStorageService, storagePath, mainStorageService,
          storagePath, getEntity(storagePath));
        return null;
      }
    } catch (GenericException | RequestNotValidException | NotFoundException | AlreadyExistsException
      | AuthorizationDeniedException e) {
      throw new RODATransactionException("[transactionId:" + transaction.getId()
        + "] Failed to update storage path from staging to main storage service: " + storagePath, e);
    }
  }

  private void handleCreateOperation(StoragePath storagePath, String version) throws RODATransactionException {
    try {
      if (version != null) {
        LOGGER.info("[transactionId:{}] Creating binary version from staging to main storage service: {}",
          transaction.getId(), storagePath);
        mainStorageService.importBinaryVersion(stagingStorageService, storagePath, version);
      } else {
        LOGGER.info("[transactionId:{}] Moving resource from staging to main storage service: {}", transaction.getId(),
          storagePath);
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
      throw new RODATransactionException("[transactionId:" + transaction.getId()
        + "] Failed to copy storage path from staging to main storage service: " + storagePath, e);
    }
  }

  private void handleCreateUpdateOperation(StoragePath storagePath, String version) throws RODATransactionException {
    try {
      if (version != null) {
        LOGGER.info("[transactionId:{}] Creating or updating binary version from staging to main storage service: {}",
          transaction.getId(), storagePath);
        mainStorageService.importBinaryVersion(stagingStorageService, storagePath, version);
      } else {
        LOGGER.info("[transactionId:{}] Creating or updating resource from staging to main storage service: {}",
          transaction.getId(), storagePath);
        Class<? extends Entity> rootEntity = stagingStorageService.getEntity(storagePath);
        // TODO: This is necessary to avoid recursive copies, we should handle it better
        // in StorageServiceUtils
        if (Container.class.isAssignableFrom(rootEntity)) {
          mainStorageService.createContainer(storagePath);
        } else if (Directory.class.isAssignableFrom(rootEntity)) {
          mainStorageService.createDirectory(storagePath);
        } else {
          StorageServiceUtils.syncBetweenStorageServices(stagingStorageService, storagePath, mainStorageService,
            storagePath, getEntity(storagePath));
        }
      }
    } catch (GenericException | RequestNotValidException | NotFoundException | AlreadyExistsException
      | AuthorizationDeniedException e) {
      throw new RODATransactionException("[transactionId:" + transaction.getId()
        + "] Failed to update storage path from staging to main storage service: " + storagePath, e);
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
      throw new RODATransactionException(
        "[transactionId:" + transaction.getId() + "] Failed to copy resources from main storage to staging storage", e);
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
    LOGGER.warn("[transactionId:{}] Rolling back transaction", transaction.getId());
    List<TransactionStoragePathConsolidatedOperation> successfulOperations = transactionLogService
      .getSuccessfulConsolidatedStoragePathOperations(transaction);
    try {
      for (TransactionStoragePathConsolidatedOperation operation : successfulOperations) {
        transactionLogService.updateConsolidatedStoragePathOperationState(operation, OperationState.ROLLING_BACK);
        if (operation.getOperationType() == OperationType.DELETE) {
          // rollbackDeleteOperation(operation);
        } else if (operation.getOperationType() == OperationType.UPDATE) {
          rollbackUpdateOperation(operation);
        } else if (operation.getOperationType() == OperationType.CREATE) {
          rollbackCreateOperation(operation);
        } else if (operation.getOperationType() == OperationType.READ) {
          // do nothing for read operations
        }
        transactionLogService.updateConsolidatedStoragePathOperationState(operation, OperationState.ROLLED_BACK);
      }
    } catch (RODATransactionException e) {
      for (TransactionStoragePathConsolidatedOperation operation : successfulOperations) {
        transactionLogService.updateConsolidatedStoragePathOperationState(operation, OperationState.ROLL_BACK_FAILURE);
      }
      throw new RODATransactionException(
        "[transactionId:" + transaction.getId() + "] Failed to rollback this transaction", e);
    }
  }

  public void rollbackCreateOperation(TransactionStoragePathConsolidatedOperation operation)
    throws RODATransactionException {
    try {
      StoragePath storagePath = DefaultStoragePath
        .parse(StreamSupport.stream(Paths.get(operation.getStoragePath()).spliterator(), false).map(Path::toString)
          .collect(Collectors.toList()));
      if (operation.getPreviousVersion() != null) {
        mainStorageService.deleteBinaryVersion(storagePath, operation.getVersion());
      } else {
        mainStorageService.deleteResource(storagePath);
      }
    } catch (NotFoundException | AuthorizationDeniedException | GenericException | RequestNotValidException e) {
      throw new RODATransactionException("[transactionId:" + transaction.getId()
        + "] Failed to roll back create operation path at " + operation.getStoragePath(), e);
    }
  }

  public void rollbackUpdateOperation(TransactionStoragePathConsolidatedOperation operation)
    throws RODATransactionException {
    if (operation.getPreviousVersion() == null) {
      throw new RODATransactionException(
        "[transactionId:" + transaction.getId() + "] Can't roll back update operation of unversioned storage path");
    }
    if (operation.getVersion() != null) {
      throw new RODATransactionException(
        "[transactionId:" + transaction.getId() + "] Can't roll back an update to binary's version history file");
    }
    try {
      StoragePath storagePath = DefaultStoragePath
        .parse(StreamSupport.stream(Paths.get(operation.getStoragePath()).spliterator(), false).map(Path::toString)
          .collect(Collectors.toList()));
      mainStorageService.revertBinaryVersion(storagePath, operation.getPreviousVersion());
    } catch (NotFoundException | RequestNotValidException | GenericException | AuthorizationDeniedException e) {
      throw new RODATransactionException("[transactionId:" + transaction.getId()
        + "] Failed to roll back update operation path at " + operation.getStoragePath(), e);
    }
  }

  public void rollbackDeleteOperation(TransactionStoragePathConsolidatedOperation operation)
    throws RODATransactionException {
    throw new RODATransactionException(
      "[transactionId:" + transaction.getId() + "] Delete operation roll back not supported");
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
      throw new GenericException(
        "[transactionId:" + transaction.getId() + "] Failed to register operation for copy: " + toPath, e);
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
        ret.add(registerOperation(destinationStoragePathAsString, operation, null, null));
      }
    } catch (NotFoundException | RequestNotValidException | IOException e) {
      throw new GenericException(
        "[transactionId:" + transaction.getId() + "] Failed to register operation for copy: " + toStoragePath, e);
    }
    return ret;
  }

  private TransactionalStoragePathOperationLog registerOperation(StoragePath storagePath, OperationType operation) {
    return registerOperation(storagePath, operation, null, null);
  }

  private TransactionalStoragePathOperationLog registerOperation(StoragePath storagePath, OperationType operation,
    String previousVersion, String version) {
    if (!isInitialized) {
      return null;
    }

    if (storagePath.getName().equals(RodaConstants.STORAGE_DIRECTORY_AGENTS)) {
      return null;
    }

    String storagePathAsString = stagingStorageService.getStoragePathAsString(storagePath, false);
    return registerOperation(storagePathAsString, operation, previousVersion, version);
  }

  private TransactionalStoragePathOperationLog registerOperation(String storagePathAsString, OperationType operation,
    String previousVersion, String version) {
    try {
      LOGGER.debug("[transactionId:{}] Registering operation for storage path: {} with operation: {}",
        transaction.getId(), storagePathAsString, operation);
      return transactionLogService.registerStoragePathOperation(transaction.getId(), storagePathAsString, operation,
        previousVersion, version);
    } catch (RODATransactionException e) {
      throw new IllegalArgumentException(
        "[transactionId:" + transaction.getId() + "] Cannot register operation for storagePath: " + storagePathAsString,
        e);
    }
  }

  public void updateOperationState(TransactionalStoragePathOperationLog operationLog, OperationState state) {
    try {
      if (operationLog != null) {
        transactionLogService.updateStoragePathOperationState(operationLog.getId(), state);
      }
    } catch (RODATransactionException e) {
      throw new IllegalArgumentException(
        "[transactionId:" + transaction.getId() + "] Cannot update operation state: " + operationLog.getId(), e);
    }
  }

  public void updateOperationState(TransactionalStoragePathOperationLog operationLog, OperationState state,
    String previousVersion, String version) {
    try {
      if (operationLog != null) {
        transactionLogService.updateStoragePathOperationState(operationLog.getId(), state, previousVersion, version);
      }
    } catch (RODATransactionException e) {
      throw new IllegalArgumentException(
        "[transactionId:" + transaction.getId() + "] Cannot update operation state: " + operationLog.getId(), e);
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
    if (stagingStorageService.exists(storagePath)) {
      LOGGER.debug("[transactionId:{}] Using staging storage service for storage path: {}", transaction.getId(),
        storagePathAsString);
      return stagingStorageService;
    }
    try {
      TransactionalStoragePathOperationLog storagePathOperation = transactionLogService
        .getAnyDeletedStoragePathOperation(transaction.getId(), storagePathAsString);
      if (storagePathOperation == null) {
        LOGGER.debug("[transactionId:{}] Using main storage service for storage path: {}", transaction.getId(),
          storagePathAsString);
        return mainStorageService;
      }
      throw new NotFoundException(
        "[transactionId:" + transaction.getId() + "] Resource was deleted in this transaction.");
    } catch (RODATransactionException e) {
      throw new GenericException("[transactionId:" + transaction.getId()
        + "] Failed to get effective storage service for storage path: " + storagePath, e);
    }
  }
}
