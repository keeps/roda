/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.transaction;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.roda.core.entity.transaction.OperationState;
import org.roda.core.entity.transaction.OperationType;
import org.roda.core.entity.transaction.TransactionLog;
import org.roda.core.entity.transaction.TransactionStoragePathConsolidatedOperation;
import org.roda.core.entity.transaction.TransactionalModelOperationLog;
import org.roda.core.entity.transaction.TransactionalStoragePathOperationLog;
import org.roda.core.repository.transaction.TransactionLogRepository;
import org.roda.core.repository.transaction.TransactionStoragePathConsolidatedOperationsRepository;
import org.roda.core.repository.transaction.TransactionalModelOperationLogRepository;
import org.roda.core.repository.transaction.TransactionalStoragePathRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Service
@Timed
@Counted
public class TransactionLogService {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionLogService.class);

  @Autowired
  private TransactionLogRepository transactionLogRepository;
  @Autowired
  private TransactionalModelOperationLogRepository transactionalModelOperationLogRepository;
  @Autowired
  private TransactionalStoragePathRepository transactionalStoragePathRepository;
  @Autowired
  private TransactionStoragePathConsolidatedOperationsRepository transactionStoragePathConsolidatedOperationsRepository;

  /*
   * TransactionLogRepository
   */

  public List<TransactionLog> getUnfinishedTransactions() {
    return transactionLogRepository.findAllByStatusInOrderByCreatedAt(
      List.of(TransactionLog.TransactionStatus.PENDING, TransactionLog.TransactionStatus.COMMITTING));
  }

  public List<TransactionLog> getCommittedTransactions() {
    return transactionLogRepository
      .findAllByStatusInOrderByCreatedAt(List.of(TransactionLog.TransactionStatus.COMMITTED));
  }

  @Transactional
  public TransactionLog createTransactionLog(TransactionLog.TransactionRequestType requestType, UUID requestId) {
    TransactionLog transactionLog = new TransactionLog(requestType, requestId);
    transactionLogRepository.save(transactionLog);
    return transactionLog;
  }

  public TransactionLog getTransactionLog(UUID transactionId) throws RODATransactionException {
    if (transactionId == null) {
      throw new RODATransactionException("Transaction ID cannot be null");
    }
    return getTransactionLogById(transactionId, true);
  }

  private TransactionLog getTransactionLogById(UUID transactionId, boolean fetch) throws RODATransactionException {
    if (fetch) {
      return transactionLogRepository.findById(transactionId)
        .orElseThrow(() -> new RODATransactionException("Transaction not found for ID: " + transactionId));
    } else {
      TransactionLog transactionLog = new TransactionLog();
      transactionLog.setId(transactionId);
      return transactionLog;
    }
  }

  @Transactional
  public void changeStatus(UUID transactionId, TransactionLog.TransactionStatus status)
    throws RODATransactionException {
    if (transactionLogRepository.updateStatus(transactionId, status, LocalDateTime.now()) == 0) {
      throw new RODATransactionException("Transaction not found for ID: " + transactionId);
    }
  }

  @Transactional
  public void cleanUp(UUID transactionID) throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionID, true);
    if (null != transactionLog) {
      transactionLogRepository.delete(transactionLog);
    }
  }

  /*
   * transactionalModelOperationLogRepository
   */

  /**
   * Creates a model operation log without persisting it, so that several
   * operations can be saved in one database transaction with
   * {@link #saveModelOperations(List)}.
   */
  public TransactionalModelOperationLog newModelOperation(UUID transactionId, String liteObject,
    OperationType operation) throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionId, false);
    TransactionalModelOperationLog operationLog = new TransactionalModelOperationLog(liteObject, operation);
    operationLog.setTransactionLog(transactionLog);
    return operationLog;
  }

  @Transactional
  public List<TransactionalModelOperationLog> saveModelOperations(List<TransactionalModelOperationLog> operationLogs) {
    return transactionalModelOperationLogRepository.saveAll(operationLogs);
  }

  @Transactional
  public void updateModelOperationState(UUID operationId, OperationState state) throws RODATransactionException {
    if (transactionalModelOperationLogRepository.updateOperationState(List.of(operationId), state,
      LocalDateTime.now()) == 0) {
      throw new RODATransactionException("Model operation log not found for ID: " + operationId);
    }
  }

  @Transactional
  public void updateModelOperationsState(Collection<UUID> operationIds, OperationState state) {
    if (!operationIds.isEmpty()) {
      transactionalModelOperationLogRepository.updateOperationState(operationIds, state, LocalDateTime.now());
    }
  }

  public List<TransactionalModelOperationLog> getModelOperations(UUID transactionId) throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionId, false);
    return transactionalModelOperationLogRepository.findByTransactionLogOrderByUpdatedAt(transactionLog);
  }

  public TransactionalModelOperationLog getAnyDeletedModelOperation(UUID transactionId, String liteObject)
    throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionId, false);
    List<TransactionalModelOperationLog> result = transactionalModelOperationLogRepository
      .findAnyByTransactionLogAndLiteObjectAndOperationType(transactionLog, OperationState.SUCCESS, liteObject,
        OperationType.DELETE, PageRequest.of(0, 1));
    return result.isEmpty() ? null : result.getFirst();
  }

  /*
   * transactionalStoragePathRepository
   */

  /**
   * Creates a storage path operation log without persisting it. It is saved once
   * the operation ends, with its final state, by
   * {@link #saveStoragePathOperation(TransactionalStoragePathOperationLog, OperationState)}:
   * staging is discarded on rollback and only successful operations are
   * committed, so an operation still running needs no row.
   */
  public TransactionalStoragePathOperationLog newStoragePathOperation(UUID transactionId, String storagePath,
    OperationType operation, String previousVersion, String version) throws RODATransactionException {
    if (operation == OperationType.READ) {
      // TODO: add a configuration to allow logging the read operation for debugging
      // purposes
      return null;
    }
    TransactionLog transactionLog = getTransactionLogById(transactionId, false);
    TransactionalStoragePathOperationLog operationLog = new TransactionalStoragePathOperationLog(storagePath,
      operation, previousVersion, version);
    operationLog.setTransactionLog(transactionLog);
    return operationLog;
  }

  @Transactional
  public void saveStoragePathOperation(TransactionalStoragePathOperationLog operationLog, OperationState state) {
    operationLog.setOperationState(state);
    transactionalStoragePathRepository.save(operationLog);
  }

  @Transactional
  public void saveStoragePathOperation(TransactionalStoragePathOperationLog operationLog, OperationState state,
    String previousVersionID, String version) {
    operationLog.setOperationState(state);
    operationLog.setPreviousVersion(previousVersionID);
    operationLog.setVersion(version);
    transactionalStoragePathRepository.save(operationLog);
  }

  public List<TransactionalStoragePathOperationLog> getStoragePathsOperations(UUID transactionId)
    throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionId, false);
    return transactionalStoragePathRepository.findByTransactionLogOrderByUpdatedAt(transactionLog,
      OperationState.SUCCESS);
  }

  public List<TransactionalStoragePathOperationLog> getStoragePathsOperations(UUID transactionId,
    OperationType operationType) throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionId, false);
    return transactionalStoragePathRepository.findByTransactionLogAndOperationType(transactionLog, operationType,
      OperationState.SUCCESS);
  }

  public boolean hasModificationsUnderStoragePath(UUID transactionID, String storagePath)
    throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionID, false);
    List<TransactionalStoragePathOperationLog> results = transactionalStoragePathRepository
      .findModificationsUnderStoragePath(transactionLog, storagePath);
    return !results.isEmpty();
  }

  public List<TransactionalStoragePathOperationLog> listModificationsUnderStoragePath(UUID transactionID,
    String storagePath) throws RODATransactionException {
    TransactionLog transactionLog = getTransactionLogById(transactionID, false);
    return transactionalStoragePathRepository.findModificationsUnderStoragePath(transactionLog, storagePath);
  }

  /*
   * TransactionStoragePathConsolidatedOperation
   */

  @Transactional
  public List<TransactionStoragePathConsolidatedOperation> registerConsolidatedStoragePathOperations(
    List<TransactionStoragePathConsolidatedOperation> operations) {
    return transactionStoragePathConsolidatedOperationsRepository.saveAll(operations);
  }

  @Transactional
  public void updateConsolidatedStoragePathOperationState(UUID operationId, OperationState state)
    throws RODATransactionException {
    if (transactionStoragePathConsolidatedOperationsRepository.updateOperationState(operationId, state,
      LocalDateTime.now()) == 0) {
      throw new RODATransactionException("Operation not found for ID: " + operationId);
    }
  }

  @Transactional
  public void updateConsolidatedStoragePathOperationState(UUID operationId, OperationState state,
    String previousVersionID) throws RODATransactionException {
    if (transactionStoragePathConsolidatedOperationsRepository.updateOperationState(operationId, state,
      previousVersionID, LocalDateTime.now()) == 0) {
      throw new RODATransactionException("Operation not found for ID: " + operationId);
    }
  }

  public List<TransactionStoragePathConsolidatedOperation> getConsolidatedStoragePathOperations(
    TransactionLog transactionLog) {
    return transactionStoragePathConsolidatedOperationsRepository.getOperationsByTransactionLog(transactionLog);
  }

  public List<TransactionStoragePathConsolidatedOperation> getSuccessfulConsolidatedStoragePathOperations(
    TransactionLog transactionLog) {
    return transactionStoragePathConsolidatedOperationsRepository
      .getSuccessfulOperationsByTransactionLog(transactionLog);
  }
}
