/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.common.monitor.FolderObserver;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.index.utils.SolrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexFolderObserver implements FolderObserver {

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexFolderObserver.class);

  private final SolrClient index;

  private ReindexSipRunnable reindexThread;

  private Path basePath;

  public IndexFolderObserver(SolrClient index, Path basePath) throws SolrServerException, IOException {
    super();
    this.index = index;
    this.basePath = basePath;
  }

  public void reindex(Path base, Date from) {
    reindexThread = new ReindexSipRunnable(base, from);
    reindexThread.run();
  }

  public void pathAdded(Path basePath, Path createdPath) {
    pathAdded(basePath, createdPath, true);
  }

  public void pathAdded(Path basePath, Path createdPath, boolean commit) {
    LOGGER.debug("ADD " + createdPath.toString());
    try {
      Path relativePath = basePath.relativize(createdPath);
      if (relativePath.getNameCount() > 1) {
        SolrInputDocument pathDocument = SolrUtils.transferredResourceToSolrDocument(createdPath, relativePath);

        List<SolrInputDocument> parents = new ArrayList<SolrInputDocument>();
        Path parentPath = createdPath.getParent();
        // add parents to parents arrayList
        while (!Files.isSameFile(basePath, parentPath)) {
          Path relativeParentPath = basePath.relativize(parentPath);
          if (relativeParentPath.getNameCount() > 1) {
            SolrInputDocument sidParent = SolrUtils.transferredResourceToSolrDocument(parentPath,
              basePath.relativize(parentPath));
            sidParent.setField(RodaConstants.TRANSFERRED_RESOURCE_SIZE,
              pathDocument.getFieldValue(RodaConstants.TRANSFERRED_RESOURCE_SIZE));
            parents.add(sidParent);
          }
          parentPath = parentPath.getParent();
        }
        index.add(RodaConstants.INDEX_SIP, pathDocument);

        if (parents.size() > 0) {
          for (SolrInputDocument parent : parents) {
            String parentID = (String) parent.getFieldValue(RodaConstants.TRANSFERRED_RESOURCE_ID);
            SolrDocument current = index.getById(RodaConstants.INDEX_SIP, parentID);
            if (current != null) { // if parent already exists, update...
              long currentSize = SolrUtils.objectToLong(current.getFieldValue(RodaConstants.TRANSFERRED_RESOURCE_SIZE));
              currentSize += SolrUtils.objectToLong(parent.getFieldValue(RodaConstants.TRANSFERRED_RESOURCE_SIZE));
              current.setField(RodaConstants.TRANSFERRED_RESOURCE_SIZE, currentSize);
              index.add(RodaConstants.INDEX_SIP, ClientUtils.toSolrInputDocument(current));
            } else { // parent doesn't exist.. add
              index.add(RodaConstants.INDEX_SIP, parent);
            }
          }
        }
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
  public void pathModified(Path basePath, Path modifiedPath) {
    LOGGER.debug("MODIFY: " + modifiedPath.toString());
    Path relativePath = basePath.relativize(modifiedPath);
    if (relativePath.getNameCount() > 1) {
      try {
        SolrDocument current = index.getById(RodaConstants.INDEX_SIP, relativePath.toString().replaceAll("\\s+", ""));
        if(current==null){
          pathAdded(basePath, modifiedPath);
        }else{
          long sizeToRemove = SolrUtils.objectToLong(current.getFieldValue(RodaConstants.TRANSFERRED_RESOURCE_SIZE));
  
          Path parentPath = modifiedPath.getParent();
          while (!Files.isSameFile(basePath, parentPath)) {
            Path relativeParentPath = basePath.relativize(parentPath);
            if (relativeParentPath.getNameCount() > 1) {
              SolrDocument p = index.getById(RodaConstants.INDEX_SIP,
                relativeParentPath.toString().replaceAll("\\s+", ""));
              p.setField(RodaConstants.TRANSFERRED_RESOURCE_SIZE,
                SolrUtils.objectToLong(p.getFieldValue(RodaConstants.TRANSFERRED_RESOURCE_SIZE)) - sizeToRemove);
              index.add(RodaConstants.INDEX_SIP, ClientUtils.toSolrInputDocument(p));
            }
            parentPath = parentPath.getParent();
          }
        }
        index.commit(RodaConstants.INDEX_SIP);
      } catch (Exception e) {
        LOGGER.error(e.getMessage(), e);
      }
    }

  }

  @Override
  public void pathDeleted(Path basePath, Path deletedPath) {
    LOGGER.debug("DELETE: " + deletedPath.toString());
    Path relativePath = basePath.relativize(deletedPath);
    try {
      SolrDocument current = index.getById(RodaConstants.INDEX_SIP, relativePath.toString().replaceAll("\\s+", ""));
      long sizeToRemove = SolrUtils.objectToLong(current.getFieldValue(RodaConstants.TRANSFERRED_RESOURCE_SIZE));

      Path parentPath = deletedPath.getParent();

      try {
        while (!Files.isSameFile(basePath, parentPath)) {
          Path relativeParentPath = basePath.relativize(parentPath);
          if (relativeParentPath.getNameCount() > 1) {
            SolrDocument p = index.getById(RodaConstants.INDEX_SIP,
              relativeParentPath.toString().replaceAll("\\s+", ""));
            p.setField(RodaConstants.TRANSFERRED_RESOURCE_SIZE,
              SolrUtils.objectToLong(p.getFieldValue(RodaConstants.TRANSFERRED_RESOURCE_SIZE)) - sizeToRemove);
            index.add(RodaConstants.INDEX_SIP, ClientUtils.toSolrInputDocument(p));
          }
          parentPath = parentPath.getParent();
        }
      } catch (NoSuchFileException nsfe) {
        // Exception occurs when modifying a resource... but no problem...
      }
      if (relativePath.getNameCount() > 1) {
        index.deleteById(RodaConstants.INDEX_SIP, relativePath.toString().replaceAll("\\s+", ""));
        index.deleteByQuery(RodaConstants.INDEX_SIP, "id:" + relativePath.toString().replaceAll("\\s+", "") + "*");
      }
      index.commit(RodaConstants.INDEX_SIP);
    } catch (IOException | SolrServerException e) {
      LOGGER.error("Error deleting path to SIPMonitorIndex: " + e.getMessage(), e);
    } catch (Throwable t) {
      LOGGER.error("ERROR: " + t.getMessage(), t);
    }
  }

  public class ReindexSipRunnable implements Runnable {
    private Path basePath;
    private long counter;
    private Date from;

    public ReindexSipRunnable(Path basePath, Date from) {
      this.basePath = basePath;
      this.counter = 0;
      this.from = from;
    }

    public void run() {
      long start = System.currentTimeMillis();
      LOGGER.debug("Start indexing SIPs: " + basePath.toString());
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
              pathAdded(basePath, file, false);
              counter++;
              if (counter >= 1000) {
                try {
                  LOGGER.debug("Commiting...");
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
        LOGGER.debug("End indexing SIPs");
        LOGGER.debug("TIME: " + ((System.currentTimeMillis() - start) / 1000) + " segundos");
      } catch (IOException | SolrServerException e) {
        LOGGER.error("ERROR REINDEXING SIPS");
      }

    }

  }
}
