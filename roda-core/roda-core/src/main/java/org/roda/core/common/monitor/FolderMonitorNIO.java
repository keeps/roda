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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.roda.core.data.common.NotFoundException;
import org.roda.core.data.v2.TransferredResource;
import org.roda.core.storage.StorageServiceException;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolderMonitorNIO {
  private static final Logger LOGGER = LoggerFactory.getLogger(FolderMonitorNIO.class);

  private final List<FolderObserver> observers;
  private Path basePath;
  private Thread threadWatch;
  private Date indexDate;
  private SolrClient index;
  private WatchDir watchDir;

  public FolderMonitorNIO(Path basePath, Date indexDate, SolrClient index) throws Exception {
    this.observers = new ArrayList<FolderObserver>();
    this.basePath = basePath;
    this.indexDate = indexDate;
    this.index = index;

    startWatch();
  }

  private void startWatch() throws Exception {
    LOGGER.debug("STARTING WATCH (NIO) ON FOLDER: " + basePath.toString());
    watchDir = new WatchDir(basePath, true, indexDate, index, observers);
    threadWatch = new Thread(watchDir, "FolderWatcher");
    threadWatch.start();
    LOGGER.debug("WATCH (NIO) ON FOLDER " + basePath.toString() + " STARTED");
  }

  public void addFolderObserver(FolderObserver observer) {
    observers.add(observer);
    if (watchDir != null) {
      watchDir.setObservers(observers);
    }

  }

  public void removeFolderObserver(FolderObserver observer) {
    observers.remove(observer);
  }

  public String createFolder(Path parent, String folderName) throws IOException {
    Path createdPath = Files.createDirectories(basePath.resolve(parent).resolve(folderName));
    TransferredResource tr = createTransferredResource(createdPath, basePath);
    for (FolderObserver observer : observers) {
      observer.transferredResourceAdded(tr, true);
    }
    return tr.getId();
  }

  public void remove(Path path) throws IOException, NotFoundException {
    try {
      Path fullpath = basePath.resolve(path);
      if (Files.exists(fullpath)) {
        FSUtils.deletePath(fullpath);
      } else {
        throw new NotFoundException("Path does not exist: " + fullpath);
      }
    } catch (StorageServiceException sse) {
      throw new IOException(sse.getMessage(), sse);
    }
  }

  public void removeSync(List<String> ids) throws NotFoundException, IOException {
    for (String s : ids) {
      try {
        Path relative = Paths.get(s);
        Path fullPath = basePath.resolve(relative);
        if (Files.exists(fullPath)) {
          for (FolderObserver observer : observers) {
            observer.transferredResourceDeleted(createTransferredResource(fullPath, basePath));
          }
          FSUtils.deletePath(fullPath);
        } else {
          throw new NotFoundException("Path does not exist: " + fullPath);
        }
      } catch (StorageServiceException sse) {
        throw new IOException(sse.getMessage(), sse);
      }
    }

  }

  public void createFile(String path, String fileName, InputStream inputStream)
    throws IOException, FileAlreadyExistsException {
    Path parent = basePath.resolve(path);
    Files.createDirectories(parent);
    Path file = parent.resolve(fileName);
    Files.copy(inputStream, file);
  }

  public static TransferredResource createTransferredResource(Path resourcePath, Path basePath) {
    Path relativeToBase = basePath.relativize(resourcePath);
    TransferredResource tr = new TransferredResource();
    tr.setBasePath(basePath.toString());
    tr.setCreationDate(new Date());
    tr.setFile(!Files.isDirectory(resourcePath));
    tr.setFullPath(resourcePath.toString());
    tr.setId(relativeToBase.toString());
    tr.setName(resourcePath.getFileName().toString());
    tr.setOwner(relativeToBase.getName(0).toString());
    if (relativeToBase.getNameCount() > 1) {
      tr.setToIndex(true);
      tr.setRelativePath(relativeToBase.subpath(1, relativeToBase.getNameCount()).toString());
      if (relativeToBase.getParent() != null && relativeToBase.getParent().getNameCount() > 1) {
        tr.setParentId(relativeToBase.subpath(1, relativeToBase.getNameCount()).getParent().toString());
      }
    } else {
      tr.setToIndex(false);
    }
    try {
      tr.setSize(Files.isDirectory(resourcePath) ? 0L : Files.size(resourcePath));
    } catch (IOException e) {
      tr.setSize(0L);
    }
    List<String> ancestors = new ArrayList<String>();
    String[] tokens = relativeToBase.toString().split("/");
    String temp = "";
    for (String s : tokens) {
      temp += s;
      ancestors.add(temp);
      temp += "/";
    }
    ancestors.remove(ancestors.size() - 1);
    tr.setAncestorsPaths(ancestors);
    return tr;
  }

  public boolean isFullyInitialized() {
    if(watchDir==null){
      return false;
    }else{
      return watchDir.isFullyInitialized();
    }
  }

}
