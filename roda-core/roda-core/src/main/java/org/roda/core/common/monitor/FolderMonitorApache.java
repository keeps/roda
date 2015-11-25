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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.solr.client.solrj.SolrClient;
import org.roda.core.storage.StorageServiceException;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolderMonitorApache {

  private static final Logger LOGGER = LoggerFactory.getLogger(FolderMonitorNIO.class);
  
  private FolderObserver observer;
  private final List<FolderObserver> observers;
  private Path basePath;
  private int timeout;
  private Thread th;
  private boolean index;
  private Date from;
  private SolrClient solr;
  FileAlterationMonitor monitor;
  
  public FolderMonitorApache(Path p, int timeout, SolrClient solr, boolean index, Date from, FolderObserver observer) throws Exception {
    this.observers = new ArrayList<FolderObserver>();
    this.basePath = p;
    this.timeout = timeout;
    this.index = index;
    this.from = from;
    this.solr = solr;
    this.observer = observer;
    startWatch();
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

  public void remove(Path path) throws IOException {
    try{
      FSUtils.deletePath(path);
    }catch(StorageServiceException sse){
      throw new IOException(sse.getMessage(),sse);
    }
  }

  public void createFile(String path, String fileName, InputStream inputStream)
    throws IOException, FileAlreadyExistsException {
    Path parent = basePath.resolve(path);
    Files.createDirectories(parent);
    Path file = parent.resolve(fileName);
    Files.copy(inputStream, file);
  }
}
