/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.catalog;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;

/**
 * Structural information of an AIP, taken from its aip.json.
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public record CatalogAip(String id, String parentId, AIPState state, boolean ghost, String ingestJobId,
  List<String> ingestUpdateJobIds, List<String> ingestSIPIds, String instanceId, Instant updatedOn) {

  public static CatalogAip fromAIP(AIP aip) {
    Instant updatedOn = aip.getUpdatedOn() != null ? aip.getUpdatedOn().toInstant()
      : aip.getCreatedOn() != null ? aip.getCreatedOn().toInstant() : Instant.now();
    return new CatalogAip(aip.getId(), aip.getParentId(), aip.getState() != null ? aip.getState() : AIPState.getDefault(),
      Boolean.TRUE.equals(aip.getGhost()), aip.getIngestJobId(), copy(aip.getIngestUpdateJobIds()),
      copy(aip.getIngestSIPIds()), aip.getInstanceId(), updatedOn);
  }

  private static List<String> copy(List<String> list) {
    return list == null ? List.of() : List.copyOf(new ArrayList<>(list));
  }

  public boolean hasIngestSIPId(String sipId) {
    return ingestSIPIds.contains(sipId);
  }
}
