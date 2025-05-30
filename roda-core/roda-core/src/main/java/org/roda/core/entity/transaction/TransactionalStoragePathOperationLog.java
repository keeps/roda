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

  @Column(name = "version")
  private String version;

  @Enumerated(EnumType.STRING)
  @Column(name = "operation_type", nullable = false)
  private OperationType operationType;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  public TransactionalStoragePathOperationLog() {
  }

  public TransactionalStoragePathOperationLog(String storagePath, OperationType operationType, String version) {
    this.storagePath = storagePath;
    this.operationType = operationType;
    this.version = version;
    this.createdAt = LocalDateTime.now();
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

  public String getVersion() {
    return version;
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
