/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.common.monitor.FolderObserver;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.index.utils.SolrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexFolderObserver implements FolderObserver {

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexFolderObserver.class);

  private final SolrClient index;

  private ReindexSipRunnable t;

  public IndexFolderObserver(SolrClient index, Path basePath) throws SolrServerException, IOException {
    super();
    this.index = index;
    clearIndex();
    t = new ReindexSipRunnable(basePath);
    t.run();
  }

  private void clearIndex() throws SolrServerException, IOException {
    index.deleteByQuery(RodaConstants.INDEX_SIP, "*:*");
    index.commit(RodaConstants.INDEX_SIP);

  }

  public void pathAdded(Path basePath, Path createdPath, boolean addChildren, boolean addParent, boolean commit) {
    LOGGER.debug("ADD: " + createdPath.toString());
    LOGGER.debug("ADDCHILDREN: " + addChildren);
    LOGGER.debug("ADDPARENT: " + addParent);
    try {
      Path relativePath = basePath.relativize(createdPath);
      if (relativePath.getNameCount() > 1) {
        SolrInputDocument pathDocument = SolrUtils.transferredResourceToSolrDocument(createdPath, relativePath);
        if (addParent) {
          Path parentPath = createdPath.getParent();
          while (!Files.isSameFile(basePath, parentPath)) {
            pathAdded(basePath, parentPath, false, false, false);
            /*
             * if(!Files.isDirectory(createdPath)){
             * index.commit(RodaConstants.INDEX_SIP); Path relativeParentPath =
             * basePath.relativize(parentPath);
             * 
             * SolrDocument sd = index.getById(RodaConstants.INDEX_SIP,
             * relativeParentPath.toString().replaceAll("\\s+", ""));
             * if(sd==null){ LOGGER.error("SD NULL"); }else{ LOGGER.error(
             * "SD NOT NULL"); SolrInputDocument sid =
             * SolrUtils.addSize(sd,Files.size(createdPath));
             * index.add(RodaConstants.INDEX_SIP,sid);
             * index.commit(RodaConstants.INDEX_SIP); } }
             */
            parentPath = parentPath.getParent();
          }
        }

        if (createdPath.toFile().isDirectory() && addChildren) {
          Files.walkFileTree(createdPath, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
              pathAdded(basePath, file, false, false, false);
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
        }
        index.add(RodaConstants.INDEX_SIP, pathDocument);
        if (commit) {
          index.commit(RodaConstants.INDEX_SIP);
        }
      }
    } catch (IOException | SolrServerException e) {
      LOGGER.error("Error adding path to SIPMonitorIndex: " + e.getMessage(), e);
    } catch (Throwable t) {
      LOGGER.error("ERROR: " + t.getMessage(), t);
    }
  }

  @Override
  public void pathAdded(Path basePath, Path createdPath, boolean addChildren, boolean addParent) {
    LOGGER.debug("!!!!!!!!!!!!! " + createdPath.toString());
    pathAdded(basePath, createdPath, addChildren, addParent, true);
  }

  @Override
  public void pathModified(Path basePath, Path modifiedPath) {
    LOGGER.debug("MODIFY: " + modifiedPath.toString());
    pathAdded(basePath, modifiedPath, true, false, true);
  }

  @Override
  public void pathDeleted(Path basePath, Path deletedPath) {
    LOGGER.debug("DELETE: " + deletedPath.toString());
    try {
      Path relativePath = basePath.relativize(deletedPath);
      if (relativePath.getNameCount() > 1) {
        index.deleteById(RodaConstants.INDEX_SIP, relativePath.toString().replaceAll("\\s+", ""));
        index.deleteByQuery(RodaConstants.INDEX_SIP, "id:" + relativePath.toString().replaceAll("\\s+", "") + "*");
        index.commit(RodaConstants.INDEX_SIP);
      }
    } catch (IOException | SolrServerException e) {
      LOGGER.error("Error deleting path to SIPMonitorIndex: " + e.getMessage(), e);
    } catch (Throwable t) {
      LOGGER.error("ERROR: " + t.getMessage(), t);
    }
  }

  public class ReindexSipRunnable implements Runnable {
    private Path basePath;

    public ReindexSipRunnable(Path basePath) {
      this.basePath = basePath;
    }

    public void run() {
      LOGGER.debug("Start indexing SIPs");
      try {
        Files.walkFileTree(basePath, new FileVisitor<Path>() {
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            pathAdded(basePath, file, true, true);
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
        LOGGER.debug("End indexing SIPs");
      } catch (IOException | SolrServerException e) {
        LOGGER.error("ERROR REINDEXING SIPS");
      }

    }

  }
}
