package org.roda.index;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.roda.common.monitor.FolderObserver;
import org.roda.core.common.RodaConstants;
import org.roda.index.utils.SolrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexFolderObserver implements FolderObserver {

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexFolderObserver.class);

  private final SolrClient index;

  public IndexFolderObserver(SolrClient index) {
    super();
    this.index = index;
  }

  @Override
  public void pathAdded(Path basePath, Path createdPath) {
    Path relativePath = basePath.relativize(createdPath);
    try {
      SolrInputDocument pathDocument = SolrUtils.transferredResourceToSolrDocument(basePath, createdPath);
      if (!createdPath.toFile().isDirectory()) {
        if (createdPath.getParent().compareTo(basePath) != 0) {
          SolrUtils.updateSizeRecursive(index, relativePath, Files.size(createdPath));
        }
      }
      index.add(RodaConstants.INDEX_SIP, pathDocument);
    } catch (IOException | SolrServerException e) {
      LOGGER.error("Error adding path to SIPMonitorIndex");
    }
    try {
      index.commit(RodaConstants.INDEX_SIP);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Could not commitbasePath, pathCreated indexed path to SIPMonitor index", e);
    }
  }

  @Override
  public void pathModified(Path basePath, Path createdPath) {
    try {
      Path relativePath = basePath.relativize(createdPath);
      if (createdPath.getParent().compareTo(basePath) != 0) {
        SolrUtils.updateSizeRecursive(index, relativePath.getParent(), -Files.size(createdPath));
      }
      pathAdded(basePath, createdPath);
    } catch (IOException | SolrServerException e) {
      LOGGER.error("Error modifying path to SIPMonitorIndex");
    }
  }

  @Override
  public void pathDeleted(Path basePath, Path createdPath) {
    LOGGER.debug("PATH DELETED: " + createdPath);
    try {
      Path relativePath = basePath.relativize(createdPath);
      if (createdPath.getParent().compareTo(basePath) != 0) {
        SolrUtils.updateSizeRecursive(index, relativePath.getParent(), -Files.size(createdPath));
      }
      index.deleteById(relativePath.toString());
    } catch (IOException | SolrServerException e) {
      LOGGER.error("Error deleting path to SIPMonitorIndex");
    }
  }
}
