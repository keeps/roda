/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.monitor;

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
      observer.pathAdded(basePath, pathCreated, true);
    }
  }

  protected void notifyPathDeleted(Path basePath, Path pathCreated) {
    for (FolderObserver observer : observers) {
      observer.pathDeleted(basePath, pathCreated);
    }
  }

  protected void notifyPathModified(Path basePath, Path pathCreated) {
    for (FolderObserver observer : observers) {
      observer.pathModified(basePath, pathCreated, true);
    }
  }

}