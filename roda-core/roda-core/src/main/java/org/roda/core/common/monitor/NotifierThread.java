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

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.List;

import org.roda.core.data.v2.ip.TransferredResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotifierThread implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(NotifierThread.class);
  private List<FolderObserver> observers;
  private Path basePath;
  private Path updatedPath;
  private WatchEvent.Kind<?> kind;
  private boolean recursive;

  public NotifierThread(List<FolderObserver> observers, Path basePath, Path updatedPath, WatchEvent.Kind<?> kind,
    boolean recursive) {
    this.observers = observers;
    this.basePath = basePath;
    this.updatedPath = updatedPath;
    this.kind = kind;
    this.recursive = recursive;
  }

  @Override
  public void run() {
    MonitorVariables.getInstance().getTaskBlocker().acquire();
    LOGGER.debug("RESOURCE: " + updatedPath);
    LOGGER.debug("BASE: " + basePath);
    TransferredResource resource = FolderMonitorNIO.createTransferredResource(updatedPath, basePath);
    if (kind == ENTRY_CREATE) {
      notifyTransferredResourceCreated(resource);
    } else if (kind == ENTRY_MODIFY) {
      notifyTransferredResourceModified(resource);
    } else if (kind == ENTRY_DELETE) {
      notifyTransferredResourceDeleted(resource);
      notifyPathDeleted(basePath.relativize(updatedPath));

    }

    // handle children
    if (kind == ENTRY_CREATE) {
      // add all children if recursive
      try {
        if (Files.isDirectory(updatedPath)) {
          if (recursive) {
            MonitorVariables.getInstance().registerAll(updatedPath);
          }
        }
      } catch (IOException x) {
        LOGGER.error("Error while registering all: " + x.getMessage(), x);
      }
      updateChildren(updatedPath, true);
    } else if (kind == ENTRY_DELETE) {
      updateChildren(updatedPath, false);
    }
    MonitorVariables.getInstance().getTaskBlocker().release();
  }

  private void updateChildren(Path updatedPath, boolean add) {
    try {
      EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
      Files.walkFileTree(updatedPath, opts, 100, new FileVisitor<Path>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          TransferredResource resource = FolderMonitorNIO.createTransferredResource(file, basePath);
          if (add) {
            notifyTransferredResourceCreated(resource);
          } else {
            notifyPathDeleted(basePath.relativize(file));
            notifyTransferredResourceDeleted(resource);
          }
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
          return FileVisitResult.CONTINUE;
        }
      });
    } catch (IOException e) {
      LOGGER.error("Error adding children: " + e.getMessage(), e);
    }

  }

  protected void notifyTransferredResourceCreated(TransferredResource resource) {
    LOGGER.debug("CREATED: " + resource.getFullPath());
    for (FolderObserver observer : observers) {
      observer.transferredResourceAdded(resource);
    }
  }

  protected void notifyTransferredResourceDeleted(TransferredResource resource) {
    LOGGER.debug("DELETED: " + resource.getFullPath());
    for (FolderObserver observer : observers) {
      observer.transferredResourceDeleted(resource);
    }
  }

  protected void notifyTransferredResourceModified(TransferredResource resource) {
    LOGGER.debug("MODIFIED: " + resource.getFullPath());
    for (FolderObserver observer : observers) {
      observer.transferredResourceAdded(resource);
    }
  }

  protected void notifyPathDeleted(Path deleted) {
    LOGGER.debug("DELETED: " + deleted);
    for (FolderObserver observer : observers) {
      observer.pathDeleted(deleted);
    }
  }

}