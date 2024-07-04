package org.roda.core.common.pekko.messages.jobs;

import java.io.Serial;
import java.util.Map;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class JobSourceObjectsUpdated extends JobPartialUpdate {
  @Serial
  private static final long serialVersionUID = -8395563279621159731L;

  private final Map<String, String> oldToNewIds;

  public JobSourceObjectsUpdated(Map<String, String> oldToNewIds) {
    super();
    this.oldToNewIds = oldToNewIds;
  }

  public Map<String, String> getOldToNewIds() {
    return oldToNewIds;
  }

  @Override
  public String toString() {
    return "JobSourceObjectsUpdated [oldToNewIds=" + oldToNewIds + "]";
  }
}
