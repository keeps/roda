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
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.EnumSet;
import java.util.Stack;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.index.utils.SolrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexTransferredResourcesRunnable implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReindexTransferredResourcesRunnable.class);

  private Path basePath;
  private TransferredResource folder;
  private SolrClient index;

  public ReindexTransferredResourcesRunnable(Path basePath, SolrClient index) {
    this.basePath = basePath;
    this.folder = null;
    this.index = index;
  }

  public ReindexTransferredResourcesRunnable(Path basePath, String folderUUID, SolrClient index) {
    this.basePath = basePath;
    this.index = index;

    try {
      this.folder = RodaCoreFactory.getIndexService().retrieve(TransferredResource.class, folderUUID);
    } catch (NotFoundException | GenericException e) {
      LOGGER.error("Specific folder is not indexed or does not exist");
    }
  }

  public void run() {
    if (!RodaCoreFactory.getTransferredResourcesScannerUpdateStatus()) {
      long start = System.currentTimeMillis();
      Date lastScanDate = new Date();
      RodaCoreFactory.setTransferredResourcesScannerUpdateStatus(true);
      LOGGER.info("Start indexing transferred resources {}", basePath.toString());
      try {
        EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        Path path;
        if (folder != null) {
          path = basePath.resolve(Paths.get(folder.getRelativePath()));
        } else {
          path = basePath;
        }
        Files.walkFileTree(path, opts, Integer.MAX_VALUE, new FileVisitor<Path>() {

          Stack<BasicFileAttributes> actualDirectoryAttributesStack = new Stack<BasicFileAttributes>();
          Stack<Long> fileSizeStack = new Stack<Long>();

          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
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
              index.add(RodaConstants.INDEX_TRANSFERRED_RESOURCE, SolrUtils.transferredResourceToSolrDocument(resource));
            } catch (IOException | SolrServerException e) {
              LOGGER.error("Error adding path to Transferred Resources index", e);
            }

            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (!dir.equals(basePath)) {
              try {
                BasicFileAttributes actualDirectoryAttributes = actualDirectoryAttributesStack.pop();
                long fileSize = fileSizeStack.pop();
                TransferredResource resource = TransferredResourcesScanner.createTransferredResource(dir,
                  actualDirectoryAttributes, fileSize, basePath, lastScanDate);
                index.add(RodaConstants.INDEX_TRANSFERRED_RESOURCE,
                  SolrUtils.transferredResourceToSolrDocument(resource));
              } catch (IOException | SolrServerException e) {
                LOGGER.error("Error adding path to Transferred Resources index", e);
              }
            }
            return FileVisitResult.CONTINUE;
          }
        });

        RodaCoreFactory.getIndexService().commit(TransferredResource.class);

        String query = SolrUtils.getQueryToDeleteTransferredResourceIndexes(folder, lastScanDate);
        index.deleteByQuery(RodaConstants.INDEX_TRANSFERRED_RESOURCE, query);

        RodaCoreFactory.getIndexService().commit(TransferredResource.class);
        LOGGER.info("End indexing Transferred Resources");
        LOGGER.info("Time elapsed: {} seconds", ((System.currentTimeMillis() - start) / 1000));
        RodaCoreFactory.setTransferredResourcesScannerUpdateStatus(false);
      } catch (IOException | SolrServerException | GenericException e) {
        LOGGER.error("Error reindexing Transferred Resources", e);
      }
    }
  }
}