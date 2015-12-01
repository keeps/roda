/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.monitor;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.List;

import org.roda.core.data.v2.TransferredResource;

public class NotifierThread implements Runnable {
  List<FolderObserver> observers;
  Path basePath;
  Path updatedPath;
  WatchEvent.Kind<?> kind;

  public NotifierThread(List<FolderObserver> observers, Path basePath, Path updatedPath, WatchEvent.Kind<?> kind) {
    this.observers = observers;
    this.basePath = basePath;
    this.updatedPath = updatedPath;
    this.kind = kind;
  }

  @Override
  public void run() {
    TransferredResource resource = FolderMonitorNIO.createTransferredResource(updatedPath, basePath);
    if (kind == ENTRY_CREATE) {
      notifyTransferredResourceCreated(resource);
    } else if (kind == ENTRY_MODIFY) {
      notifyTransferredResourceModified(resource);
    } else if (kind == ENTRY_DELETE) {
      notifyTransferredResourceDeleted(resource);
    }
  }

  protected void notifyTransferredResourceCreated(TransferredResource resource) {
    for (FolderObserver observer : observers) {
      observer.transferredResourceAdded(resource, true);
    }
  }

  protected void notifyTransferredResourceDeleted(TransferredResource resource) {
    for (FolderObserver observer : observers) {
      observer.transferredResourceDeleted(resource);
    }
  }

  protected void notifyTransferredResourceModified(TransferredResource resource) {
    for (FolderObserver observer : observers) {
      observer.transferredResourceAdded(resource);
    }
  }

}