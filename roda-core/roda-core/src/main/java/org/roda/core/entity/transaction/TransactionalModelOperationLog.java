package org.roda.core.entity.transaction;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "transactional_model_operation_log")
public class TransactionalModelOperationLog implements Serializable {
  @Serial
  private static final long serialVersionUID = -3676056836840227015L;

  public enum OperationType {
    CREATE, UPDATE, DELETE, READ
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "transaction_id", nullable = false)
  private TransactionLog transactionLog;

  @Column(name = "lite_object", length = 255)
  private String liteObject;

  @Enumerated(EnumType.STRING)
  @Column(name = "operation_type", nullable = false, length = 20)
  private OperationType operationType;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  public TransactionalModelOperationLog() {
  }

  public TransactionalModelOperationLog(String liteObject, OperationType operationType) {
    this.liteObject = liteObject;
    this.operationType = operationType;
    this.createdAt = LocalDateTime.now();
  }

  public String getLiteObject() {
    return liteObject;
  }

  public void setTransactionLog(TransactionLog transactionLog) {
    this.transactionLog = transactionLog;
  }

  public OperationType getOperationType() {
    return operationType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TransactionalModelOperationLog that = (TransactionalModelOperationLog) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
