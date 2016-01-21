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
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.TransferredResource;
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
    LOGGER.debug("Starting watch (NIO) on folder: {}", basePath);
    watchDir = new WatchDir(basePath, true, indexDate, index, observers);
    threadWatch = new Thread(watchDir, "FolderWatcher");
    threadWatch.start();
    LOGGER.debug("Watch (NIO) on folder {} started", basePath);
  }

  public void stopWatch() {
    threadWatch.interrupt();
    watchDir.stop();
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

  public String createFolder(String parent, String folderName) throws IOException {
    Path parentPath = parent != null ? basePath.resolve(parent) : basePath;
    Path createdPath = Files.createDirectories(parentPath.resolve(folderName));
    TransferredResource tr = createTransferredResource(createdPath, basePath);
    for (FolderObserver observer : observers) {
      observer.transferredResourceAdded(tr);
    }
    return tr.getId();
  }

  public void remove(Path path) throws NotFoundException, GenericException {

    Path fullpath = basePath.resolve(path);
    if (Files.exists(fullpath)) {
      FSUtils.deletePath(fullpath);
    } else {
      throw new NotFoundException("Path does not exist: " + fullpath);
    }

  }

  public void removeSync(List<String> ids) throws NotFoundException, GenericException {
    for (String s : ids) {
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
    }

  }

  public void createFile(String path, String fileName, InputStream inputStream)
    throws IOException, FileAlreadyExistsException {
    Path parent = path != null ? basePath.resolve(path) : basePath;
    Files.createDirectories(parent);
    Path file = parent.resolve(fileName);
    Files.copy(inputStream, file);
  }

  public InputStream retrieveFile(String path) throws NotFoundException, RequestNotValidException, GenericException {
    InputStream ret;
    Path p = basePath.resolve(path);
    if (!Files.exists(p)) {
      throw new NotFoundException("File not found: " + path);
    } else if (!Files.isRegularFile(p)) {
      throw new RequestNotValidException("Requested file is not a regular file: " + path);
    } else {
      try {
        ret = Files.newInputStream(p);
      } catch (IOException e) {
        throw new GenericException("Could not create input stream: " + e.getMessage());
      }
    }
    return ret;
  }

  public static TransferredResource createTransferredResource(Path resourcePath, Path basePath) {
    Path relativeToBase = basePath.relativize(resourcePath);
    TransferredResource tr = new TransferredResource();
    try {
      BasicFileAttributes attr = Files.readAttributes(basePath, BasicFileAttributes.class);
      Date d = new Date(attr.creationTime().toMillis());
      tr.setCreationDate(d);
    } catch (IOException e) {
      LOGGER.warn("Error getting file creation time. Setting to current time.");
      tr.setCreationDate(new Date());
    }
    tr.setFile(!Files.isDirectory(resourcePath));
    tr.setFullPath(resourcePath.toString());
    tr.setId(relativeToBase.toString());
    tr.setName(resourcePath.getFileName().toString());

    tr.setRelativePath(relativeToBase.toString());
    if (relativeToBase.getParent() != null) {
      tr.setParentId(relativeToBase.getParent().toString());
    }

    try {
      tr.setSize(Files.isDirectory(resourcePath) ? 0L : Files.size(resourcePath));
    } catch (IOException e) {
      tr.setSize(0L);
    }

    List<String> ancestors = new ArrayList<String>();

    StringBuilder temp = new StringBuilder();
    Iterator<Path> pathIterator = relativeToBase.iterator();
    while (pathIterator.hasNext()) {
      temp.append(pathIterator.next().toString());
      ancestors.add(temp.toString());
      temp.append("/");
    }
    ancestors.remove(ancestors.size() - 1);
    tr.setAncestorsPaths(ancestors);

    return tr;
  }

  public boolean isFullyInitialized() {
    return watchDir != null && watchDir.isFullyInitialized();
  }

}
