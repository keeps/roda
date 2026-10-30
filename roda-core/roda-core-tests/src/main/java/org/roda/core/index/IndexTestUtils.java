/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index;

import java.io.IOException;
import java.util.Collections;

import org.apache.solr.client.solrj.SolrServerException;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.index.schema.SolrCollectionRegistry;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.ModelService;
import org.roda.core.storage.StringContentPayload;
import org.roda.core.util.IdUtils;
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

  /**
   * Creates, without indexing them, an AIP with one representation of the given
   * number of files, at its root and named by {@link #fileId(int)}, and the given
   * number of preservation events.
   */
  public static AIP createAIP(ModelService model, int files, int events) throws RODAException {
    AIP aip = model.createAIP(AIPState.ACTIVE, null, RodaConstants.AIP_TYPE_MIXED, new Permissions(), false,
      RodaConstants.ADMIN, IdUtils.createUUID());
    String representationId = IdUtils.createUUID();
    model.createRepresentation(aip.getId(), representationId, true, RodaConstants.REPRESENTATION_TYPE_MIXED, false,
      RodaConstants.ADMIN);
    for (int i = 0; i < files; i++) {
      model.createFile(aip.getId(), representationId, Collections.emptyList(), fileId(i),
        new StringContentPayload("file " + i), RodaConstants.ADMIN, false);
    }
    for (int i = 0; i < events; i++) {
      model.createEvent(aip.getId(), null, null, null, RodaConstants.PreservationEventType.FIXITY_CHECK, "event " + i,
        null, null, PluginState.SUCCESS, "", "", RodaConstants.ADMIN, false, null);
    }
    return model.retrieveAIP(aip.getId());
  }

  /** The id of the i-th file of an AIP created by {@link #createAIP}. */
  public static String fileId(int i) {
    return String.format("file%05d.txt", i);
  }

  private IndexTestUtils() {
  }
}
