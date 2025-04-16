package org.roda.core.model.transaction;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.roda.core.data.v2.ip.StoragePath;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */

@Entity
@Table(name = "TX_STORAGE_PATH")
public class TransactionalStoragePath implements Serializable {

  @Serial
  private static final long serialVersionUID = -359012958079838014L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "transaction_id", nullable = false)
  private TransactionLog transactionLog;

  @Column(name = "storage_path", length = 255)
  private String storagePath;

//  @ManyToMany(mappedBy = "storagePaths")
//  private Set<TransactionLog> transactionLogs = new HashSet<>();

  public TransactionalStoragePath() {
  }

  public TransactionalStoragePath(StoragePath storagePath) {
    this.storagePath = storagePath.toString();
  }

  public String getStoragePath() {
    return storagePath;
  }

  public void setTransactionLog(TransactionLog transactionLog) {
    this.transactionLog = transactionLog;
  }
}
