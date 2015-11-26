/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.roda.core.common.monitor.FolderMonitorNIO;
import org.roda.core.common.monitor.FolderObserver;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.TransferredResource;
import org.roda.core.index.utils.SolrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexFolderObserver implements FolderObserver {

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexFolderObserver.class);

  private final SolrClient index;

  private Path basePath;

  public IndexFolderObserver(SolrClient index, Path basePath) throws SolrServerException, IOException {
    super();
    this.index = index;
    this.basePath = basePath;
  }

  public void transferredResourceAdded(TransferredResource resource) {
    transferredResourceAdded(resource, true);
  }

  public void transferredResourceAdded(TransferredResource resource, boolean commit) {
    try {
      if (resource.getAncestorsPaths() != null && resource.getAncestorsPaths().size() > 0) {
        for (String ancestor : resource.getAncestorsPaths()) {
          TransferredResource resourceAncestor = FolderMonitorNIO
            .createTransferredResource(basePath.resolve(Paths.get(ancestor)), Paths.get(resource.getBasePath()));
          if (resourceAncestor.isToIndex()) {
            LOGGER.debug("---------------- ADDED ----------------------");
            LOGGER.debug("FULLPATH " + resourceAncestor.getFullPath());
            LOGGER.debug("RELATIVE " + resourceAncestor.getRelativePath());
            LOGGER.debug("PARENT " + resourceAncestor.getParentPath());
            LOGGER.debug("------------------------------------------------");
            index.add(RodaConstants.INDEX_SIP, SolrUtils.transferredResourceToSolrDocument(resourceAncestor));
          } else {
            LOGGER.debug("---------------- NOT ADDED ----------------------");
            LOGGER.debug("FULLPATH " + resourceAncestor.getFullPath());
            LOGGER.debug("RELATIVE " + resourceAncestor.getRelativePath());
            LOGGER.debug("PARENT " + resourceAncestor.getParentPath());
            LOGGER.debug("------------------------------------------------");
          }
        }
      }
      if (resource.isToIndex()) {
        LOGGER.debug("---------------- ADDED ----------------------");
        LOGGER.debug("FULLPATH " + resource.getFullPath());
        LOGGER.debug("RELATIVE " + resource.getRelativePath());
        LOGGER.debug("PARENT " + resource.getParentPath());
        LOGGER.debug("------------------------------------------------");
        index.add(RodaConstants.INDEX_SIP, SolrUtils.transferredResourceToSolrDocument(resource));
      } else {
        LOGGER.debug("---------------- NOT ADDED ----------------------");
        LOGGER.debug("FULLPATH " + resource.getFullPath());
        LOGGER.debug("RELATIVE " + resource.getRelativePath());
        LOGGER.debug("PARENT " + resource.getParentPath());
        LOGGER.debug("------------------------------------------------");
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

  @Override
  public void transferredResourceModified(TransferredResource resource) {
    LOGGER.debug("MODIFY: " + resource.toString());
    transferredResourceAdded(resource);

  }

  @Override
  public void transferredResourceDeleted(TransferredResource resource) {
    LOGGER.debug("DELETE: " + resource.toString());
    try {
      index.deleteById(RodaConstants.INDEX_SIP, resource.getId());
      index.deleteByQuery(RodaConstants.INDEX_SIP, "ancestors:\"" + resource.getId() + "\"");
      index.commit(RodaConstants.INDEX_SIP);
    } catch (SolrServerException | IOException e) {
      LOGGER.error("ERROR DELETING RESOURCE " + resource.getId() + " : " + e.getMessage(), e);
    }
  }

}
