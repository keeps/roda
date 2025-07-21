package org.roda.core.entity.transaction;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */

@Entity
@Table(name = "transactional_storage_path_operation_log")
public class TransactionalStoragePathOperationLog implements Serializable {

  @Serial
  private static final long serialVersionUID = -359012958079838014L;

  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "transaction_id", nullable = false)
  private TransactionLog transactionLog;

  @Column(name = "storage_path", nullable = false, columnDefinition = "text")
  private String storagePath;

  @Column(name = "previous_version")
  private String previousVersion;

  @Column(name = "version")
  private String version;

  @Enumerated(EnumType.STRING)
  @Column(name = "operation_type", nullable = false)
  private OperationType operationType;

  @Enumerated(EnumType.STRING)
  @Column(name = "operation_state", nullable = false)
  private OperationState operationState;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public TransactionalStoragePathOperationLog() {
  }

  public TransactionalStoragePathOperationLog(String storagePath, OperationType operationType, String previousVersion,
    String version) {
    this.storagePath = storagePath;
    this.operationType = operationType;
    this.operationState = OperationState.RUNNING;
    this.previousVersion = previousVersion;
    this.version = version;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  public UUID getId() {
    return id;
  }

  public String getStoragePath() {
    return storagePath;
  }

  public void setTransactionLog(TransactionLog transactionLog) {
    this.transactionLog = transactionLog;
  }

  public OperationType getOperationType() {
    return operationType;
  }

  public String getPreviousVersion() {
    return previousVersion;
  }

  public String getVersion() {
    return version;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public OperationState getOperationState() {
    return operationState;
  }

  public void setOperationState(OperationState operationState) {
    this.updatedAt = LocalDateTime.now();
    this.operationState = operationState;
  }

  public void setPreviousVersion(String previousVersion) {
    this.updatedAt = LocalDateTime.now();
    this.previousVersion = previousVersion;
  }

  public void setVersion(String version) {
    this.updatedAt = LocalDateTime.now();
    this.version = version;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    TransactionalStoragePathOperationLog that = (TransactionalStoragePathOperationLog) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
