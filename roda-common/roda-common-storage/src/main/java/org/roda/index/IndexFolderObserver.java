package org.roda.index;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.roda.common.monitor.FolderObserver;
import org.roda.core.common.RodaConstants;
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
    LOGGER.debug("PATH CREATED: " + createdPath);
    try {
      Path relativePath = basePath.relativize(createdPath);
      
      
      SolrInputDocument pathDocument = new SolrInputDocument();
      pathDocument.addField(RodaConstants.SIPMONITOR_ID, relativePath.toString());
      pathDocument.addField(RodaConstants.SIPMONITOR_FULLPATH, createdPath.toString());
      if (createdPath.getParent().compareTo(basePath) != 0) {
        pathDocument.addField(RodaConstants.SIPMONITOR_PARENTPATH, relativePath.getParent().toString());
      }
      pathDocument.addField(RodaConstants.SIPMONITOR_RELATIVEPATH, relativePath.toString());
      pathDocument.addField(RodaConstants.SIPMONITOR_DATE, new Date());
      if (createdPath.toFile().isDirectory()) {
        pathDocument.addField(RodaConstants.SIPMONITOR_ISFILE, false);
        pathDocument.addField(RodaConstants.SIPMONITOR_SIZE, 0L);
      } else {
        pathDocument.addField(RodaConstants.SIPMONITOR_ISFILE, true);
        long fileSize = Files.size(createdPath);
        pathDocument.addField(RodaConstants.SIPMONITOR_SIZE, fileSize);
        updateSize(fileSize,relativePath.getParent());
      }
      
      pathDocument.addField(RodaConstants.SIPMONITOR_NAME, relativePath.getFileName().toString());
      
      LOGGER.debug("DIC");
      for(String s : pathDocument.getFieldNames()){
        LOGGER.debug(s+" - "+pathDocument.getFieldValue(s));
      }
      index.add(RodaConstants.INDEX_SIP, pathDocument);
    } catch (IOException | SolrServerException e) {
      LOGGER.error("Error adding path to SIPMonitorIndex");
    }
    try {
      index.commit(RodaConstants.INDEX_SIP);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("Could not commit indexed path to SIPMonitor index", e);
    }
  }

  private void updateSize(long fileSize, Path p) {
    // TODO UPDATE SIZE of path and call recursive...
    
  }

  @Override
  public void pathModified(Path basePath, Path createdPath) {
    LOGGER.debug("PATH MODIFIED: " + createdPath);

  }

  @Override
  public void pathDeleted(Path basePath, Path createdPath) {
    LOGGER.debug("PATH DELETED: " + createdPath);

  }

}
