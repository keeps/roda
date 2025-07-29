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
public class JobsManagerReleaseAllLocks extends AbstractMessage {
  @Serial
  private static final long serialVersionUID = -2842420964416530808L;

  public JobsManagerReleaseAllLocks() {
    super();
  }

  @Override
  public String toString() {
    return "JobsManagerReleaseAllLocks []";
  }
}
