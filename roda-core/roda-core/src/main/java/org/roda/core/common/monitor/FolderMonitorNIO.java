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
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.solr.client.solrj.SolrClient;
import org.roda.core.data.common.NotFoundException;
import org.roda.core.data.v2.TransferredResource;
import org.roda.core.storage.StorageServiceException;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolderMonitorNIO {
  private static final Logger LOGGER = LoggerFactory.getLogger(FolderMonitorNIO.class);
  ExecutorService executor;
  private final List<FolderObserver> observers;
  private Path basePath;
  private Thread threadWatch, threadReindex;
  private Date indexDate;
  private SolrClient index;

  public FolderMonitorNIO(Path p, Date indexDate, SolrClient index) throws Exception {
    executor = Executors.newSingleThreadExecutor();
    this.observers = new ArrayList<FolderObserver>();
    this.basePath = p;
    this.indexDate = indexDate;
    this.index = index;
    startWatch();
  }

  private void startWatch() throws Exception {
    LOGGER.debug("STARTING WATCH (NIO) ON FOLDER: " + basePath.toString());
    WatchDir watchDir = new WatchDir(basePath, true);
    threadWatch = new Thread(watchDir, "FolderWatcher");
    threadWatch.start();
    ReindexSipRunnable reindex = new ReindexSipRunnable(basePath, indexDate, index);
    threadReindex = new Thread(reindex, "ReindexThread");
    threadReindex.start();
    LOGGER.debug("WATCH (NIO) ON FOLDER " + basePath.toString() + " STARTED");
  }

  public void addFolderObserver(FolderObserver observer) {
    observers.add(observer);
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

  class WatchDir implements Runnable {
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final boolean recursive;
    private final Path watched;

    @SuppressWarnings("unchecked")
    <T> WatchEvent<T> cast(WatchEvent<?> event) {
      return (WatchEvent<T>) event;
    }

    @Override
    public void run() {
      long startTime = System.currentTimeMillis();
      try {
        if (recursive) {
          registerAll(watched);
        } else {
          register(watched);
        }
      } catch (IOException e) {
        LOGGER.error("Error initialing watch thread: " + e.getMessage(), e);
      }
      LOGGER
        .debug("TIME ELAPSED (INITIALIZE WATCH): " + ((System.currentTimeMillis() - startTime) / 1000) + " segundos");
      processEvents();
    }

    private void register(Path directoryPath) throws IOException {
      WatchKey key = directoryPath.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY, OVERFLOW);
      keys.put(key, directoryPath);
    }

    private void registerAll(final Path start) throws IOException {
      EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
      Files.walkFileTree(start, opts, 100, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          register(dir);
          return FileVisitResult.CONTINUE;
        }
      });
    }

    public WatchDir(Path dir, boolean recursive) throws IOException {
      this.watcher = FileSystems.getDefault().newWatchService();
      this.keys = new HashMap<WatchKey, Path>();
      this.recursive = recursive;
      this.watched = dir;
    }

    void processEvents() {
      for (;;) {
        WatchKey key;
        try {
          key = watcher.take();
        } catch (InterruptedException x) {
          return;
        }
        Path dir = keys.get(key);
        if (dir == null) {
          continue;
        }
        for (WatchEvent<?> event : key.pollEvents()) {
          WatchEvent.Kind<?> kind = event.kind();
          if (kind == OVERFLOW) {
            continue;
          }
          WatchEvent<Path> ev = cast(event);
          Path name = ev.context();
          Path child = dir.resolve(name);
          if (recursive && (kind == ENTRY_CREATE)) {
            try {
              if (Files.isDirectory(child)) {
                registerAll(child);
              }
            } catch (IOException x) {
            }
          }
          NotifierThread nt = new NotifierThread(observers, basePath, child, kind);
          executor.execute(nt);

        }
        boolean valid = key.reset();
        if (!valid) {
          keys.remove(key);
          if (keys.isEmpty()) {
            break;
          }
        }
      }
    }
  }
}
