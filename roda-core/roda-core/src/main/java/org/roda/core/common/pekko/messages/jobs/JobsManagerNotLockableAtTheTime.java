/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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
