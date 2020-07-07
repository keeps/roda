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
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.EnumSet;
import java.util.Optional;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.SolrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexTransferredResourcesRunnable implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReindexTransferredResourcesRunnable.class);

  private Path basePath;
  private Optional<String> folderRelativePath;
  private IndexService index;

  public ReindexTransferredResourcesRunnable(IndexService index, Path basePath, Optional<String> folderRelativePath) {
    this.basePath = basePath;
    this.index = index;
    this.folderRelativePath = folderRelativePath;
  }

  @Override
  public void run() {

    long start = System.currentTimeMillis();
    Date lastScanDate = new Date();
    RodaCoreFactory.setTransferredResourcesScannerUpdateStatus(folderRelativePath, true);

    try {
      EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
      Path path;
      if (folderRelativePath.isPresent()) {
        Path resolvedBasePath = basePath.resolve(Paths.get(folderRelativePath.get()));
        boolean isWithin = RodaCoreFactory.checkPathIsWithin(resolvedBasePath,basePath);
        if (isWithin) {
          path = resolvedBasePath.normalize();
        }else{
          LOGGER.warn("Request trying to access folders outside the transfer resources folder ({})", folderRelativePath.get());
          throw new AuthorizationDeniedException("Request trying to access folders outside the transfer resources folder (" + folderRelativePath.get() + ")");
        }
      } else {
        path = basePath;
      }

      LOGGER.info("Start indexing transferred resources {}", path);
      Files.walkFileTree(path, opts, Integer.MAX_VALUE, new FileVisitor<Path>() {

        ArrayDeque<BasicFileAttributes> actualDirectoryAttributesStack = new ArrayDeque<>();
        ArrayDeque<Long> fileSizeStack = new ArrayDeque<>();

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
          actualDirectoryAttributesStack.push(attrs);
          fileSizeStack.push(0L);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          try {
            long size = Files.size(file);
            long actualSize = fileSizeStack.pop();
            fileSizeStack.push(actualSize + size);
            TransferredResource resource = TransferredResourcesScanner.createTransferredResource(file, attrs, size,
              basePath, lastScanDate);
            index.create(TransferredResource.class, resource);
          } catch (NoSuchFileException | AuthorizationDeniedException e) {
            // can be a broken symlink (do nothing)
            // can be unauthorized action (do nothing)
          }

          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
          if (!dir.equals(basePath)) {
            BasicFileAttributes actualDirectoryAttributes = actualDirectoryAttributesStack.pop();
            long fileSize = fileSizeStack.pop();
            TransferredResource resource = TransferredResourcesScanner.createTransferredResource(dir,
              actualDirectoryAttributes, fileSize, basePath, lastScanDate);

            if (!fileSizeStack.isEmpty()) {
              long actualSize = fileSizeStack.pop();
              fileSizeStack.push(actualSize + fileSize);
            }

            try {
              index.create(TransferredResource.class, resource);
            } catch (AuthorizationDeniedException e) {
              // do nothing & carry on
            }
          }

          return FileVisitResult.CONTINUE;
        }
      });

      index.commit(TransferredResource.class);

      Filter filter;
      if (!folderRelativePath.isPresent()) {
        filter = new Filter(new NotSimpleFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_LAST_SCAN_DATE,
          SolrUtils.formatDateWithMillis(lastScanDate)));
      } else {
        filter = new Filter(
          new SimpleFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_ANCESTORS, folderRelativePath.get()),
          new NotSimpleFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_LAST_SCAN_DATE,
            SolrUtils.formatDateWithMillis(lastScanDate)));
      }

      index.delete(TransferredResource.class, filter);
      index.commit(TransferredResource.class);
      LOGGER.info("End indexing Transferred Resources. Time elapsed: {} seconds",
        (System.currentTimeMillis() - start) / 1000);
      RodaCoreFactory.setTransferredResourcesScannerUpdateStatus(folderRelativePath, false);
    } catch (IOException | GenericException | RuntimeException | AuthorizationDeniedException e) {
      LOGGER.error("Error reindexing Transferred Resources", e);
    }
  }
}