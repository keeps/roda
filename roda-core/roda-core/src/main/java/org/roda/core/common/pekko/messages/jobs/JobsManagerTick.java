package org.roda.core.common.pekko.messages.jobs;

import org.roda.core.common.pekko.messages.AbstractMessage;

import java.io.Serial;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class JobsManagerTick extends AbstractMessage {
  @Serial
  private static final long serialVersionUID = -2514581679498648676L;

  public JobsManagerTick() {
    super();
  }

  @Override
  public String toString() {
    return "JobManagerTick []";
  }
}
