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

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.TransferredResource;
import org.roda.core.index.utils.SolrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexSipRunnable implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReindexSipRunnable.class);

  private Path basePath;
  private long counter;
  private Date from;
  private SolrClient index;

  public ReindexSipRunnable(Path basePath, Date from, SolrClient index) {
    this.basePath = basePath;
    this.counter = 0;
    this.from = from;
    this.index = index;
  }

  public void run() {
    long start = System.currentTimeMillis();
    LOGGER.error("Start indexing SIPs: " + basePath.toString());
    try {
      EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
      Files.walkFileTree(basePath, opts, 100, new FileVisitor<Path>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          Date modifiedDate = new Date(attrs.lastModifiedTime().toMillis());
          if (from == null || from.before(modifiedDate)) {
            transferredResourceAdded(FolderMonitorNIO.createTransferredResource(file, basePath), false);
            counter++;
            if (counter >= 1000) {
              try {
                LOGGER.error("Commiting...");
                index.commit(RodaConstants.INDEX_SIP);
                counter = 0;
              } catch (IOException | SolrServerException e) {
                LOGGER.error(e.getMessage(), e);
              }
            }
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
      index.commit(RodaConstants.INDEX_SIP);
      LOGGER.error("End indexing SIPs");
      LOGGER.error("TIME: " + ((System.currentTimeMillis() - start) / 1000) + " segundos");
      RodaCoreFactory.setFolderMonitorDate(basePath, new Date());
    } catch (IOException | SolrServerException e) {
      LOGGER.error("ERROR REINDEXING SIPS");
    }
  }

  public void transferredResourceAdded(TransferredResource resource, boolean commit) {

    try {
      if (resource.getAncestorsPaths() != null && resource.getAncestorsPaths().size() > 0) {
        for (String ancestor : resource.getAncestorsPaths()) {
          TransferredResource resourceAncestor = FolderMonitorNIO
            .createTransferredResource(basePath.resolve(Paths.get(ancestor)), Paths.get(resource.getBasePath()));
          if (resourceAncestor.isToIndex()) {
            LOGGER.error("---------------- ADDED ----------------------");
            LOGGER.error("FULLPATH " + resourceAncestor.getFullPath());
            LOGGER.error("RELATIVE " + resourceAncestor.getRelativePath());
            LOGGER.error("PARENT " + resourceAncestor.getParentPath());
            LOGGER.error("------------------------------------------------");
            index.add(RodaConstants.INDEX_SIP, SolrUtils.transferredResourceToSolrDocument(resourceAncestor));
          } else {
            LOGGER.error("---------------- NOT ADDED ----------------------");
            LOGGER.error("FULLPATH " + resourceAncestor.getFullPath());
            LOGGER.error("RELATIVE " + resourceAncestor.getRelativePath());
            LOGGER.error("PARENT " + resourceAncestor.getParentPath());
            LOGGER.error("------------------------------------------------");
          }
        }
      }
      if (resource.isToIndex()) {
        LOGGER.error("---------------- ADDED ----------------------");
        LOGGER.error("FULLPATH " + resource.getFullPath());
        LOGGER.error("RELATIVE " + resource.getRelativePath());
        LOGGER.error("PARENT " + resource.getParentPath());
        LOGGER.error("------------------------------------------------");
        index.add(RodaConstants.INDEX_SIP, SolrUtils.transferredResourceToSolrDocument(resource));
      } else {
        LOGGER.error("---------------- NOT ADDED ----------------------");
        LOGGER.error("FULLPATH " + resource.getFullPath());
        LOGGER.error("RELATIVE " + resource.getRelativePath());
        LOGGER.error("PARENT " + resource.getParentPath());
        LOGGER.error("------------------------------------------------");
      }
      if (commit) {
        index.commit(RodaConstants.INDEX_SIP);
      }
    } catch (IOException | SolrServerException e) {
      LOGGER.error("Error adding path to SIPMonitorIndex: " + e.getMessage(), e);
    } catch (Throwable t) {
      LOGGER.error("ERROR: " + t.getMessage(), t);
    }
  }
}