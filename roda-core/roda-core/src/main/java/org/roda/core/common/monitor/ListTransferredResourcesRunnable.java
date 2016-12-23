/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.monitor;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.EnumSet;
import java.util.Stack;

import org.roda.core.data.v2.ip.TransferredResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListTransferredResourcesRunnable implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(ListTransferredResourcesRunnable.class);

  private Path basePath;
  private TransferredResource nextResource = null;

  public ListTransferredResourcesRunnable(Path basePath) {
    this.basePath = basePath;
  }

  @Override
  public void run() {
    try {
      Date lastScanDate = new Date();
      EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);

      LOGGER.info("Start listing transferred resources {}", basePath);
      Files.walkFileTree(basePath, opts, Integer.MAX_VALUE, new FileVisitor<Path>() {

        Stack<BasicFileAttributes> actualDirectoryAttributesStack = new Stack<BasicFileAttributes>();
        Stack<Long> fileSizeStack = new Stack<Long>();

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          System.err.print("preVisitDirectory");
          actualDirectoryAttributesStack.push(attrs);
          fileSizeStack.push(0L);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          System.err.print("visitFile");
          long size = Files.size(file);
          long actualSize = fileSizeStack.pop();
          fileSizeStack.push(actualSize + size);

          nextResource = TransferredResourcesScanner.createTransferredResource(file, attrs, size, basePath,
            lastScanDate);

          putThreadToWait();
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
          System.err.print("visitFileFailed");
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
          System.err.print("postVisitDirectory");
          if (!dir.equals(basePath)) {
            BasicFileAttributes actualDirectoryAttributes = actualDirectoryAttributesStack.pop();
            long fileSize = fileSizeStack.pop();

            nextResource = TransferredResourcesScanner.createTransferredResource(dir, actualDirectoryAttributes,
              fileSize, basePath, lastScanDate);

            if (!fileSizeStack.isEmpty()) {
              long actualSize = fileSizeStack.pop();
              fileSizeStack.push(actualSize + fileSize);
            }

            putThreadToWait();
          }
          return FileVisitResult.CONTINUE;
        }
      });

      LOGGER.info("End listing Transferred Resources");
    } catch (IOException | RuntimeException e) {
      LOGGER.error("Error listing Transferred Resources", e);
    }

  }

  private void putThreadToWait() {
    synchronized (this) {
      try {
        wait();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public void notifyRunnable() {
    synchronized (this) {
      notify();
    }
  }

  public TransferredResource getNextResource() {
    return nextResource;
  }

}