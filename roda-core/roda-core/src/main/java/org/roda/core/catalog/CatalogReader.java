/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.catalog;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Read access to the AIP catalog. Inside a RODA transaction, the reader also
 * sees the AIPs written by that transaction; outside, only committed AIPs.
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface CatalogReader {

  Optional<CatalogAip> get(String aipId);

  /** AIPs that have {@code sipId} among their ingest SIP ids. */
  List<CatalogAip> findByIngestSIPId(String sipId);

  List<CatalogAip> findChildren(String parentId);

  /** Ghost AIPs created by the job, or every ghost when {@code jobId} is null. */
  List<CatalogAip> findGhosts(String jobId);

  /**
   * @return true if {@code ancestorId} is an ancestor of {@code aipId} (walking
   *         up the parent chain)
   */
  default boolean isAncestor(String ancestorId, String aipId) {
    Set<String> visited = new HashSet<>();
    Optional<CatalogAip> current = get(aipId);
    while (current.isPresent() && current.get().parentId() != null && visited.add(current.get().id())) {
      String parentId = current.get().parentId();
      if (parentId.equals(ancestorId)) {
        return true;
      }
      current = get(parentId);
    }
    return false;
  }
}
