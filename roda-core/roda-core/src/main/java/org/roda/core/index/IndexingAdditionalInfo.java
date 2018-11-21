/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class IndexingAdditionalInfo {
  public enum Flags {
    SAFE_MODE_ON, SAFE_MODE_OFF
  }

  private final Map<String, Object> accumulators = new HashMap<>();

  public abstract Map<String, Object> getPreCalculatedFields();

  public Map<String, Object> getAccumulators() {
    return accumulators;
  }

  public List<Flags> getFlags() {
    return Collections.emptyList();
  }

  public static class NoAdditionalInfo extends IndexingAdditionalInfo {

    @Override
    public Map<String, Object> getPreCalculatedFields() {
      return Collections.emptyMap();
    }

  }

  public static IndexingAdditionalInfo empty() {
    return new NoAdditionalInfo();
  }

}
