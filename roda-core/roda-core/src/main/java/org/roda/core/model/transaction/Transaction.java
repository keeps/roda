package org.roda.core.model.transaction;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.ip.StoragePath;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class Transaction {
  private String transactionId;
  private List<StoragePath> storagePathList;

  public Transaction(String transactionId) {
    this.transactionId = transactionId;
    this.storagePathList = new ArrayList<>();
  }

  public String getTransactionId() {
    return transactionId;
  }

  public List<StoragePath> getStoragePathList() {
    return storagePathList;
  }

  public Boolean exist(StoragePath storagePath) {
    return storagePathList.stream().anyMatch(path -> path.equals(storagePath));
  }

  public void addStoragePath(StoragePath storagePath) {
    storagePathList.add(storagePath);
  }
}
