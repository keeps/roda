package org.roda.core.entity.transaction;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.ip.StoragePath;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Entity
@Table(name = "TX_LOG")
public class TransactionLog implements Serializable {

  @Serial
  private static final long serialVersionUID = -1711448061359115698L;

  public void setStatus(TransactionStatus transactionStatus) {
    this.updatedAt = LocalDateTime.now();
    this.status = transactionStatus;
  }

  public List<TransactionalStoragePath> getStoragePaths() {
      return storagePaths;
  }

  public void removeStoragePath(TransactionalStoragePath storagePath) {
    if (storagePath != null) {
        storagePaths.removeIf(path -> path.getStoragePath().equals(storagePath.getStoragePath()));
    }
  }

  public void removeLiteObject(String lite) {
    if (lite != null) {
      liteObjects.removeIf(path -> path.getLiteObject().equals(lite));
    }
  }

  public List<String> getLiteObjects() {
    return liteObjects.stream().map(TransactionalLiteObject::getLiteObject).toList();
  }

  public enum TransactionStatus {
    PENDING, COMMITTING, COMMITTED, ROLLED_BACK
  }

  public enum OperationType {
    CREATE, UPDATE, DELETE, READ
  }

  @Id
  private String id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TransactionStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "operation_type", nullable = false, length = 20)
  private OperationType operationType;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // @ManyToMany
  // @JoinTable(name = "TX_LOB_STORAGE_PATH", joinColumns = @JoinColumn(name =
  // "transaction_log_id"), inverseJoinColumns = @JoinColumn(name =
  // "storage_path"))
  // private Set<TransactionalStoragePath> storagePaths = new HashSet<>();

  @OneToMany(mappedBy = "transactionLog", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<TransactionalStoragePath> storagePaths = new ArrayList<>();

  @OneToMany(mappedBy = "transactionLog", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<TransactionalLiteObject> liteObjects = new ArrayList<>();;

  public TransactionLog() {
  }

  public TransactionLog(String id) {
    this.id = id;
    this.createdAt = LocalDateTime.now();
    this.status = TransactionStatus.PENDING;
    this.operationType = OperationType.CREATE;
  }

  public String getId() {
    return id;
  }

  public void addStoragePath(StoragePath storagePath, TransactionalStoragePath.OperationType operation) {
    if (storagePath != null) {
      TransactionalStoragePath transactionalStoragePath = new TransactionalStoragePath(storagePath, operation);
      transactionalStoragePath.setTransactionLog(this);
      storagePaths.add(transactionalStoragePath);
    }
  }

  public TransactionalStoragePath getStoragePath(String storagePath) {
    return storagePaths.stream().filter(path -> path.getStoragePath().equals(storagePath)).findFirst().orElse(null);
  }

  public boolean hasStoragePath(String storagePath) {
    return storagePaths.stream().anyMatch(path -> path.getStoragePath().equals(storagePath));
  }

  public void setLiteObjects(List<TransactionalLiteObject> liteObjects) {
    this.liteObjects = liteObjects;
  }

  public void addLiteObject(String liteObject) {
    if (liteObject != null) {
      TransactionalLiteObject transactionalLiteObject = new TransactionalLiteObject(liteObject);
      transactionalLiteObject.setTransactionLog(this);
      liteObjects.add(transactionalLiteObject);
    }
  }

  public TransactionalLiteObject getLiteObject(String liteObject) {
    return liteObjects.stream().filter(path -> path.getLiteObject().equals(liteObject)).findFirst().orElse(null);
  }

  public boolean hasLiteObject(String liteObject) {
    return liteObjects.stream().anyMatch(path -> path.getLiteObject().equals(liteObject));
  }

}
