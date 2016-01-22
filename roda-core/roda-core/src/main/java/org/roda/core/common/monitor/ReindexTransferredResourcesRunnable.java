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

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.index.utils.SolrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexTransferredResourcesRunnable implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReindexTransferredResourcesRunnable.class);

  private Path basePath;
  private long counter;
  private Date from;
  private SolrClient index;

  public ReindexTransferredResourcesRunnable(Path basePath, Date from, SolrClient index) {
    this.basePath = basePath;
    this.counter = 0;
    this.from = from;
    this.index = index;
  }

  public void run() {
    long start = System.currentTimeMillis();
    LOGGER.info("Start indexing transferred resources {}", basePath.toString());
    try {
      EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
      Files.walkFileTree(basePath, opts, Integer.MAX_VALUE, new FileVisitor<Path>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          if (!dir.equals(basePath)) {
            indexPath(dir, attrs);
          }
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          indexPath(file, attrs);
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
      index.commit(RodaConstants.INDEX_TRANSFERRED_RESOURCE);
      LOGGER.info("End indexing Transferred Resources");
      LOGGER.info("Time elapsed: {} seconds", ((System.currentTimeMillis() - start) / 1000));
      RodaCoreFactory.setFolderMonitorDate(new Date());
    } catch (IOException | SolrServerException e) {
      LOGGER.error("Error reindexing Transferred Resources", e);
    }
  }

  private void indexPath(Path file, BasicFileAttributes attrs) {
    Date modifiedDate = new Date(attrs.lastModifiedTime().toMillis());
    if (from == null || from.before(modifiedDate)) {
      transferredResourceAdded(FolderMonitorNIO.createTransferredResource(file, basePath), false);
      counter++;
      if (counter >= 1000) {
        try {
          LOGGER.debug("Commiting...");
          index.commit(RodaConstants.INDEX_TRANSFERRED_RESOURCE);
          counter = 0;
        } catch (IOException | SolrServerException e) {
          LOGGER.error(e.getMessage(), e);
        }
      }
    }
  }

  public void transferredResourceAdded(TransferredResource resource, boolean commit) {

    try {
      // TODO check if indexing ancestors is really needed
      // if (resource.getAncestorsPaths() != null &&
      // resource.getAncestorsPaths().size() > 0) {
      // for (String ancestor : resource.getAncestorsPaths()) {
      // TransferredResource resourceAncestor = FolderMonitorNIO
      // .createTransferredResource(basePath.resolve(Paths.get(ancestor)),
      // Paths.get(resource.getBasePath()));
      // index.add(RodaConstants.INDEX_TRANSFERRED_RESOURCE,
      // SolrUtils.transferredResourceToSolrDocument(resourceAncestor));
      //
      // }
      // }

      index.add(RodaConstants.INDEX_TRANSFERRED_RESOURCE, SolrUtils.transferredResourceToSolrDocument(resource));

      if (commit) {
        index.commit(RodaConstants.INDEX_TRANSFERRED_RESOURCE);
      }
    } catch (IOException | SolrServerException e) {
      LOGGER.error("Error adding path to Transferred Resources index", e);
    }
  }
}