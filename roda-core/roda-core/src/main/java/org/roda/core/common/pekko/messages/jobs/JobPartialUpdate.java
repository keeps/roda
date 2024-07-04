package org.roda.core.common.pekko.messages.jobs;

import org.roda.core.common.pekko.messages.AbstractMessage;

import java.io.Serial;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public abstract class JobPartialUpdate extends AbstractMessage {
  @Serial
  private static final long serialVersionUID = 4722216970884172260L;

  @Override
  public String toString() {
    return "JobPartialUpdate []";
  }
}
