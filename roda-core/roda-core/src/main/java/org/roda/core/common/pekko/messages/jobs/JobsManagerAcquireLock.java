/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.pekko.messages.jobs;

import java.io.Serial;
import java.util.Date;
import java.util.List;

import org.apache.pekko.actor.ActorRef;
import org.roda.core.common.pekko.messages.AbstractMessage;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class JobsManagerAcquireLock extends AbstractMessage {
  @Serial
  private static final long serialVersionUID = 4924002662559968741L;

  private final List<String> lites;
  private final boolean waitForLockIfLocked;
  private final Date expireDate;
  private final String requestUuid;
  private ActorRef sender;

  public JobsManagerAcquireLock(List<String> lites, boolean waitForLockIfLocked, int secondsToExpire,
    String requestUuid) {
    super();
    this.lites = lites;
    this.waitForLockIfLocked = waitForLockIfLocked;
    this.expireDate = new Date(new Date().getTime() + (secondsToExpire * 1000L));
    this.requestUuid = requestUuid;
  }

  public List<String> getLites() {
    return lites;
  }

  public ActorRef getSender() {
    return sender;
  }

  public JobsManagerAcquireLock setSender(ActorRef sender) {
    this.sender = sender;
    return this;
  }

  public boolean isWaitForLockIfLocked() {
    return waitForLockIfLocked;
  }

  public Date getExpireDate() {
    return expireDate;
  }

  public String getRequestUuid() {
    return requestUuid;
  }

  @Override
  public String toString() {
    return "JobsManagerAcquireLock [lites=" + lites + ", waitForLockIfLocked=" + waitForLockIfLocked + ", expireDate="
      + expireDate + ", requestUuid=" + requestUuid + "]";
  }
}