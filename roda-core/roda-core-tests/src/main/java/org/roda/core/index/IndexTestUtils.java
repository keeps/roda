package org.roda.core.index;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.filter.Filter;
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
        SolrUtils.deleteByQuery(RodaCoreFactory.getSolr(), r.getIndexName(), Filter.ALL);
        SolrUtils.commit(RodaCoreFactory.getSolr(), r.getIndexClass());
      } catch (GenericException | RequestNotValidException e) {
        LOGGER.error("Failed to reset index" , e);
      }
    });
  }

  private IndexTestUtils() {
  }
}
