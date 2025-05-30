package org.roda.core.entity.transaction;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.roda.core.data.v2.ip.StoragePath;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Entity
@Table(name = "transaction_log")
public class TransactionLog implements Serializable {

  @Serial
  private static final long serialVersionUID = -1711448061359115698L;

  public enum TransactionStatus {
    PENDING, COMMITTING, COMMITTED, ROLLED_BACK
  }

  public enum TransactionRequestType {
    JOB, API, NON_DEFINED
  }

  @Id
  @GeneratedValue
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TransactionStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "request_type", nullable = false)
  private TransactionRequestType requestType;

  @Column(name = "request_id")
  private UUID requestId;
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "transactionLog", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<TransactionalStoragePathOperationLog> storagePathsOperations = new ArrayList<>();

  @OneToMany(mappedBy = "transactionLog", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<TransactionalModelOperationLog> modelOperations = new ArrayList<>();

  public TransactionLog() {

  }

  public TransactionLog(TransactionRequestType requestType, UUID requestId) {
    this.createdAt = LocalDateTime.now();
    this.status = TransactionStatus.PENDING;
    this.requestType = requestType;
    this.requestId = requestId;
  }

  public UUID getId() {
    return id;
  }

  public UUID getRequestId() {
    return requestId;
  }

  public void setStatus(TransactionStatus transactionStatus) {
    this.updatedAt = LocalDateTime.now();
    this.status = transactionStatus;
  }

  public void setStoragePathsOperations(List<TransactionalStoragePathOperationLog> storagePathsOperations) {
    this.storagePathsOperations = storagePathsOperations;
  }

  public void setModelOperations(List<TransactionalModelOperationLog> modelOperations) {
    this.modelOperations = modelOperations;
  }

  public List<TransactionalStoragePathOperationLog> getStoragePathsOperations() {
    return storagePathsOperations;
  }

  public void addStoragePath(StoragePath storagePath, OperationType operation, String version) {
    if (storagePath != null) {
      TransactionalStoragePathOperationLog transactionalStoragePathOperationLog = new TransactionalStoragePathOperationLog(
        storagePath, operation, version);
      transactionalStoragePathOperationLog.setTransactionLog(this);
      storagePathsOperations.add(transactionalStoragePathOperationLog);
    }
  }

  public List<TransactionalModelOperationLog> getModelOperations() {
    return modelOperations;
  }

  public TransactionalModelOperationLog addModelOperation(String liteObject, OperationType operationType) {
    if (liteObject != null) {
      TransactionalModelOperationLog transactionalModelOperationLog = new TransactionalModelOperationLog(liteObject,
        operationType);
      transactionalModelOperationLog.setTransactionLog(this);
      modelOperations.add(transactionalModelOperationLog);
      return transactionalModelOperationLog;
    }
    return null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    TransactionLog that = (TransactionLog) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
