/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.monitor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolderMonitor extends FolderObservable {

  private static final Logger LOGGER = LoggerFactory.getLogger(FolderMonitor.class);
  Path basePath;
  FileAlterationMonitor monitor;
  int timeout;

  public FolderMonitor(Path p, int timeout) throws Exception {
    this.basePath = p;
    this.timeout = timeout;
    startWatch();
  }

  public void createFolder(Path parent, String folderName) throws IOException {
    Files.createDirectory(basePath.resolve(parent).resolve(folderName));
  }

  public void removeFolder(Path path) throws IOException {
    Files.delete(basePath.resolve(path));
  }

  public void createFile(String path, String fileName, InputStream inputStream)
    throws IOException, FileAlreadyExistsException {
    LOGGER.error("BASEPATH: " + basePath);
    LOGGER.error("PATH: " + path);
    Path parent = basePath.resolve(path);
    Files.createDirectories(parent);
    Path file = parent.resolve(fileName);
    Files.copy(inputStream, file);
  }

  private void startWatch() throws Exception {
    LOGGER.debug("STARTING WATCH ON FOLDER: " + basePath.toString());
    FileAlterationObserver observer = new FileAlterationObserver(basePath.toFile());
    monitor = new FileAlterationMonitor(timeout);
    FileAlterationListener listener = new FileAlterationListenerAdaptor() {
      public void onDirectoryCreate(File directory) {
        notifyPathCreated(basePath, directory.toPath());
      }

      public void onDirectoryChange(File directory) {
        notifyPathModified(basePath, directory.toPath());
      }

      public void onDirectoryDelete(File directory) {
        notifyPathDeleted(basePath, directory.toPath());
      }

      public void onFileCreate(File file) {
        notifyPathCreated(basePath, file.toPath());
      }

      public void onFileChange(File file) {
        notifyPathModified(basePath, file.toPath());
      }

      public void onFileDelete(File file) {
        notifyPathDeleted(basePath, file.toPath());
      }
    };
    observer.addListener(listener);
    monitor.addObserver(observer);
    monitor.start();
    LOGGER.debug("WATCH ON FOLDER " + basePath.toString() + " STARTED");
  }

}
