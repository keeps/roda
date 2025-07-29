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
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class JobsManagerReleaseLock extends AbstractMessage {
  @Serial
  private static final long serialVersionUID = 4924002662559968741L;

  private final List<String> lites;
  private final String requestUuid;

  public JobsManagerReleaseLock(List<String> lites, String requestUuid) {
    super();
    this.lites = lites;
    this.requestUuid = requestUuid;
  }

  public List<String> getLites() {
    return lites;
  }

  public String getRequestUuid() {
    return requestUuid;
  }

  @Override
  public String toString() {
    return "JobsManagerReleaseLock [lites=" + lites + ", requestUuid=" + requestUuid + "]";
  }

}
