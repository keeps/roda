package org.roda.core.common.monitor;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolderMonitorNIO extends FolderObservable {

  private static final Logger LOGGER = LoggerFactory.getLogger(FolderMonitor.class);
  Path basePath;
  int timeout;
  Thread th;

  public FolderMonitorNIO(Path p, int timeout) throws Exception {
    this.basePath = p;
    this.timeout = timeout;
    startWatch();
  }

  private void startWatch() throws Exception {
    LOGGER.debug("STARTING WATCH (NIO) ON FOLDER: " + basePath.toString());
    WatchDir watchDir = new WatchDir(basePath, true);
    th = new Thread(watchDir, "FolderWatcher");
    th.start();
    LOGGER.debug("WATCH (NIO) ON FOLDER " + basePath.toString() + " STARTED");
  }

  public class WatchDir implements Runnable {
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final boolean recursive;

    @SuppressWarnings("unchecked")
    <T> WatchEvent<T> cast(WatchEvent<?> event) {
      return (WatchEvent<T>) event;
    }

    @Override
    public void run() {
      processEvents();
    }

    private void register(Path dir) throws IOException {
      WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
      keys.put(key, dir);
    }

    private void registerAll(final Path start) throws IOException {
      Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          register(dir);
          return FileVisitResult.CONTINUE;
        }
      });
    }

    WatchDir(Path dir, boolean recursive) throws IOException {
      this.watcher = FileSystems.getDefault().newWatchService();
      this.keys = new HashMap<WatchKey, Path>();
      this.recursive = recursive;
      if (recursive) {
        registerAll(dir);
      } else {
        register(dir);
      }
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
                registerAll(child);
              }
            } catch (IOException x) {
            }
          }
          if (kind == ENTRY_CREATE) {
            notifyPathCreated(basePath, child);
          } else if (kind == ENTRY_MODIFY) {
            notifyPathModified(basePath, child);
          } else if (kind == ENTRY_DELETE) {
            notifyPathDeleted(basePath, child);
            removeKey(child.toAbsolutePath());
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

    private void removeKey(Path p) {
      List<WatchKey> keysToRemove = new ArrayList<WatchKey>();
      for (Map.Entry<WatchKey, Path> entry : keys.entrySet()) {
        if (entry.getValue().toString().equalsIgnoreCase(p.toString())) {
          keysToRemove.add(entry.getKey());
        }
        if (entry.getValue().startsWith(p)) {
          keysToRemove.add(entry.getKey());
        }
      }
      if (keysToRemove.size() > 0) {
        for (WatchKey key : keysToRemove) {
          keys.remove(key);
        }
      }
    }
  }
}
