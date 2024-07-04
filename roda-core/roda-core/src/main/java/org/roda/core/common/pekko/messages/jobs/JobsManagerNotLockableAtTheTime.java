package org.roda.core.common.pekko.messages.jobs;

import org.roda.core.common.pekko.messages.AbstractMessage;

import java.io.Serial;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class JobsManagerNotLockableAtTheTime extends AbstractMessage {
  @Serial
  private static final long serialVersionUID = -2313831907910175641L;

  private final String msg;

  public JobsManagerNotLockableAtTheTime() {
    super();
    this.msg = "";
  }

  public JobsManagerNotLockableAtTheTime(String msg) {
    super();
    this.msg = msg;
  }

  @Override
  public String toString() {
    return "JobsManagerUnlockableAtTheTime [msg=" + msg + "]";
  }
}
