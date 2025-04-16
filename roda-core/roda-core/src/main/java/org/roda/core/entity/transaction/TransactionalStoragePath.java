package org.roda.core.entity.transaction;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.roda.core.data.v2.ip.StoragePath;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */

@Entity
@Table(name = "TX_STORAGE_PATH")
public class TransactionalStoragePath implements Serializable {

  @Serial
  private static final long serialVersionUID = -359012958079838014L;

  public enum OperationType {
    CREATE, UPDATE, DELETE, READ
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "transaction_id", nullable = false)
  private TransactionLog transactionLog;

  @Column(name = "storage_path", length = 255)
  private String storagePath;

  @Enumerated(EnumType.STRING)
  @Column(name = "operation_type", nullable = false, length = 20)
  private OperationType operationType;

  public TransactionalStoragePath() {
  }

  public TransactionalStoragePath(StoragePath storagePath) {
    this(storagePath, OperationType.READ);
  }

  public TransactionalStoragePath(StoragePath storagePath, OperationType operationType) {
    this.storagePath = storagePath.toString();
    this.operationType = operationType;
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
}
