/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.monitor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class FolderObservable {
  private final List<FolderObserver> observers;
  Path basePath;
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
  
  public void createFolder(Path parent, String folderName) throws IOException {
    Files.createDirectory(basePath.resolve(parent).resolve(folderName));
  }

  public void removeFolder(Path path) throws IOException {
    Files.delete(basePath.resolve(path));
  }

  public void createFile(String path, String fileName, InputStream inputStream)
    throws IOException, FileAlreadyExistsException {
    Path parent = basePath.resolve(path);
    Files.createDirectories(parent);
    Path file = parent.resolve(fileName);
    Files.copy(inputStream, file);
  }

}