/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SIPUpdateInformation implements Serializable {
  private static final long serialVersionUID = -5028697752208138720L;
  private Map<String, Map<String, List<String>>> updatedData;

  public SIPUpdateInformation() {
    updatedData = new HashMap<>();
  }

  public SIPUpdateInformation(SIPUpdateInformation other) {
    updatedData = other.getUpdatedData();
  }

  public Map<String, Map<String, List<String>>> getUpdatedData() {
    return updatedData;
  }

  @JsonIgnore
  public boolean hasUpdatedData() {
    return !updatedData.isEmpty();
  }

  @JsonIgnore
  public boolean isValid() {
    // 20170901 nvieira it can be valid with different content in the future
    return hasUpdatedData();
  }

  @JsonIgnore
  public void addRepresentationData(String aipId, String representationUUID) {
    if (!updatedData.containsKey(aipId)) {
      updatedData.put(aipId, new HashMap<String, List<String>>());
    }

    Map<String, List<String>> map = updatedData.get(aipId);

    if (!updatedData.get(aipId).containsKey(RodaConstants.RODA_OBJECT_REPRESENTATION)) {
      map.put(RodaConstants.RODA_OBJECT_REPRESENTATION, new ArrayList<String>());
    }

    map.get(RodaConstants.RODA_OBJECT_REPRESENTATION).add(representationUUID);
    return;
  }

  @JsonIgnore
  public void addFileData(String aipId, String representationUUID, File file) {
    if (!updatedData.containsKey(aipId)) {
      updatedData.put(aipId, new HashMap<String, List<String>>());
    } else if (updatedData.get(aipId).containsKey(RodaConstants.RODA_OBJECT_REPRESENTATION)
      && updatedData.get(aipId).get(RodaConstants.RODA_OBJECT_REPRESENTATION).contains(representationUUID)) {
      return;
    }

    Map<String, List<String>> map = updatedData.get(aipId);

    if (!updatedData.get(aipId).containsKey(RodaConstants.RODA_OBJECT_FILE)) {
      map.put(RodaConstants.RODA_OBJECT_FILE, new ArrayList<String>());
    }

    StringBuilder builder = new StringBuilder();
    builder.append(file.getRepresentationId());
    for (String path : file.getPath()) {
      builder.append("/").append(path);
    }
    builder.append("/").append(file.getId());

    map.get(RodaConstants.RODA_OBJECT_FILE).add(builder.toString());
    return;
  }
}
