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

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
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

    } catch (IOException | SolrServerException e) {
      LOGGER.error("Error adding path to Transferred Resources index", e);
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
      index.deleteById(RodaConstants.INDEX_TRANSFERRED_RESOURCE, resource.getId());
      index.deleteByQuery(RodaConstants.INDEX_TRANSFERRED_RESOURCE, "ancestors:\"" + resource.getId() + "\"");
    } catch (SolrServerException | IOException e) {
      LOGGER.error("ERROR DELETING RESOURCE " + resource.getId() + " : " + e.getMessage(), e);
    }
  }

  @Override
  public void pathDeleted(Path deleted) {
    LOGGER.debug("PATH DELETED: " + deleted);
    try {
      index.deleteById(RodaConstants.INDEX_TRANSFERRED_RESOURCE, deleted.toString());
      index.deleteByQuery(RodaConstants.INDEX_TRANSFERRED_RESOURCE, "ancestors:\"" + deleted.toString() + "\"");
    } catch (SolrServerException | IOException e) {
      LOGGER.error("ERROR DELETING PATH " + deleted.toString() + " : " + e.getMessage(), e);
    }

  }

}
