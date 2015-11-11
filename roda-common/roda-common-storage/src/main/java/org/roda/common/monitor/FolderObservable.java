package org.roda.common.monitor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class FolderObservable {
  private final List<FolderObserver> observers;

  public FolderObservable() {
    super();
    this.observers = new ArrayList<FolderObserver>();
  }

  public void addFolderObserver(FolderObserver observer) {
    observers.add(observer);
  }

  public void removeFolderObserver(FolderObserver observer) {
    observers.remove(observer);
  }

  protected void notifyPathCreated(Path basePath, Path pathCreated) {
    for (FolderObserver observer : observers) {
      observer.pathAdded(basePath, pathCreated);
    }
  }

  protected void notifyPathDeleted(Path basePath, Path pathCreated) {
    for (FolderObserver observer : observers) {
      observer.pathDeleted(basePath, pathCreated);
    }
  }

  protected void notifyPathModified(Path basePath, Path pathCreated) {
    for (FolderObserver observer : observers) {
      observer.pathModified(basePath, pathCreated);
    }
  }

}