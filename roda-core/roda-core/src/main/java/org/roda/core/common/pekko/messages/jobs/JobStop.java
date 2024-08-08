package org.roda.core.common.pekko.messages.jobs;

import org.roda.core.common.pekko.messages.AbstractMessage;

import java.io.Serial;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class JobStop extends AbstractMessage {
  @Serial
  private static final long serialVersionUID = -8806029242967727412L;

  public JobStop() {
    super();
  }

  @Override
  public String toString() {
    return "JobStop []";
  }
}
