/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.monitor;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
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
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.storage.StorageServiceException;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolderMonitorNIO {
  private static final Logger LOGGER = LoggerFactory.getLogger(FolderMonitorNIO.class);

  private FolderObserver observer;
  private final List<FolderObserver> observers;
  private Path basePath;
  private int timeout;
  private Thread th;
  private boolean index;
  private Date from;
  private SolrClient solr;

  public FolderMonitorNIO(Path p, int timeout, SolrClient solr, boolean index, Date from, FolderObserver observer)
    throws Exception {
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
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    LOGGER.debug("STARTING WATCH (NIO) ON FOLDER: " + basePath.toString() + "( FROM: "
      + (from == null ? "NULL" : df.format(from)));
    WatchDir watchDir = new WatchDir(basePath, true, solr, index, from, observer);
    th = new Thread(watchDir, "FolderWatcher");
    th.start();
    LOGGER.debug("WATCH (NIO) ON FOLDER " + basePath.toString() + " STARTED");
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

  public class WatchDir implements Runnable {
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final boolean recursive;
    private final SolrClient solr;
    private long counter;
    private final Path watched;
    private final FolderObserver folderObserver;

    private void clearIndex() throws SolrServerException, IOException {
      LOGGER.debug("clearIndex()");
      solr.deleteByQuery(RodaConstants.INDEX_SIP, "*:*");
      solr.commit(RodaConstants.INDEX_SIP);
    }

    @SuppressWarnings("unchecked")
    <T> WatchEvent<T> cast(WatchEvent<?> event) {
      return (WatchEvent<T>) event;
    }

    @Override
    public void run() {
      long startTime = System.currentTimeMillis();
      try {
        if (index && from == null) {
          clearIndex();
        }
        if (recursive) {
          registerAll(watched, index, from);
        } else {
          register(watched, index, from);
        }

        if (index) {
          RodaCoreFactory.setFolderMonitorDate(watched, new Date());
        }
      } catch (SolrServerException | IOException e) {
        LOGGER.error("Error initialing watch thread: " + e.getMessage(), e);
      }
      LOGGER.debug("STARTUP ENDED...");
      LOGGER.debug("TIME ELAPSED: " + ((System.currentTimeMillis() - startTime) / 1000) + " segundos");
      processEvents();
    }

    private void register(Path directoryPath, boolean index, Date from) throws IOException {
      LOGGER.debug("Register: " + directoryPath);
      WatchKey key = directoryPath.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
      keys.put(key, directoryPath);
      if (index) {
        if (from == null || from.before(new Date(Files.getLastModifiedTime(directoryPath).toMillis()))) {
          LOGGER.debug("Adding " + basePath + " TO INDEX");
          observer.pathAdded(basePath, directoryPath, false);
          counter++;
          if (counter >= 1000) {
            try {
              LOGGER.debug("Commiting SIPS");
              solr.commit(RodaConstants.INDEX_SIP);
            } catch (SolrServerException | IOException e) {
              LOGGER.error("Error commiting SIPS: " + e.getMessage(), e);
            }
            counter = 0;
          }
        }
        Files.walkFileTree(directoryPath, new FileVisitor<Path>() {
          @Override
          public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (!Files.isDirectory(file)) {
              if (from == null || from.before(new Date(Files.getLastModifiedTime(file).toMillis()))) {
                LOGGER.debug("Adding " + basePath + " TO INDEX");
                observer.pathAdded(basePath, file, false);
                counter++;
                if (counter >= 1000) {
                  try {
                    LOGGER.debug("Commiting SIPS");
                    solr.commit(RodaConstants.INDEX_SIP);
                  } catch (SolrServerException | IOException e) {
                    LOGGER.error("Error commiting SIPS: " + e.getMessage(), e);
                  }
                  counter = 0;
                }
              }
            }
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
          }
        });

      }
    }

    private void registerAll(final Path start, boolean index, Date from) throws IOException {
      LOGGER.debug("RegisterAll(" + start + "," + index + "," + from + ")");
      EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
      Files.walkFileTree(start, opts, 100, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          register(dir, index, from);
          return FileVisitResult.CONTINUE;
        }
      });
    }

    WatchDir(Path dir, boolean recursive, SolrClient solr, boolean index, Date from, FolderObserver observer)
      throws IOException {
      this.watcher = FileSystems.getDefault().newWatchService();
      this.keys = new HashMap<WatchKey, Path>();
      this.recursive = recursive;
      this.solr = solr;
      this.counter = 0;
      this.watched = dir;
      this.folderObserver = observer;
    }

    void processEvents() {
      for (;;) {
        WatchKey key;
        try {
          key = watcher.poll(timeout,TimeUnit.MILLISECONDS);
         //key = watcher.take();
        } catch (InterruptedException x) {
          return;
        }
        Path dir = keys.get(key);
        if (dir == null) {
          continue;
        }
        for (WatchEvent<?> event : key.pollEvents()) {
          WatchEvent.Kind kind = event.kind();
          if (kind == OVERFLOW) {
            continue;
          }
          WatchEvent<Path> ev = cast(event);
          Path name = ev.context();
          Path child = dir.resolve(name);
          if (recursive && (kind == ENTRY_CREATE)) {
            try {
              if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                registerAll(child, false, null);
              }
            } catch (IOException x) {
            }
          }
          if (kind == ENTRY_CREATE) {
            LOGGER.error("CREATED: "+child.toString());
            notifyPathCreated(basePath, child);
            if(Files.isDirectory(child)){
              try{
                Files.walkFileTree(child, new FileVisitor<Path>() {
                  @Override
                  public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                  }
  
                  @Override
                  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                  }
  
                  @Override
                  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    notifyPathCreated(basePath, file);
                    return FileVisitResult.CONTINUE;
                  }
  
                  @Override
                  public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                  }
                });
              }catch(IOException e){
                LOGGER.error(e.getMessage(),e);
              }
            }
          } else if (kind == ENTRY_MODIFY) {
            notifyPathModified(basePath, child);
          } else if (kind == ENTRY_DELETE) {
            notifyPathDeleted(basePath, child);
            // removeKey(child.toAbsolutePath());
          }
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
    /*
     * private void removeKey(Path p) { List<WatchKey> keysToRemove = new
     * ArrayList<WatchKey>(); for (Map.Entry<WatchKey, Path> entry :
     * keys.entrySet()) { if
     * (entry.getValue().toString().equalsIgnoreCase(p.toString())) {
     * keysToRemove.add(entry.getKey()); } if (entry.getValue().startsWith(p)) {
     * keysToRemove.add(entry.getKey()); } } if (keysToRemove.size() > 0) { for
     * (WatchKey key : keysToRemove) { keys.remove(key); } } }
     */
  }
}
