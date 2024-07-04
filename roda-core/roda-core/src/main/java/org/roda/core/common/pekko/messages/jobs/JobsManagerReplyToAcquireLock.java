package org.roda.core.common.pekko.messages.jobs;

import org.roda.core.common.pekko.messages.AbstractMessage;

import java.io.Serial;
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class JobsManagerReplyToAcquireLock extends AbstractMessage {
  @Serial
  private static final long serialVersionUID = 4924002662559968741L;

  private final List<String> lites;

  public JobsManagerReplyToAcquireLock(List<String> lites) {
    super();
    this.lites = lites;
  }

  public List<String> getLites() {
    return lites;
  }

  @Override
  public String toString() {
    return "JobsManagerReplyToAcquireLock [lites=" + lites + "]";
  }
}
