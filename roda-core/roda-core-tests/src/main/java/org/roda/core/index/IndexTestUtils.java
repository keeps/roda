/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.index.schema.SolrCollectionRegistry;
import org.roda.core.index.utils.SolrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class IndexTestUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexTestUtils.class);

  public static void resetIndex() {
    SolrCollectionRegistry.registry().forEach(r -> {
      try {
        // between test classes nothing else is indexing, and every document must be gone at once
        RodaCoreFactory.getSolr().deleteByQuery(r.getIndexName(), "*:*");
        SolrUtils.commit(RodaCoreFactory.getSolr(), r.getIndexClass());
      } catch (GenericException | SolrServerException | IOException e) {
        LOGGER.error("Failed to reset index", e);
      }
    });
  }

  private IndexTestUtils() {
  }
}
