/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.pekko.messages.jobs;

import org.roda.core.common.pekko.messages.AbstractMessage;
import org.roda.core.plugins.orchestrate.JobPluginInfo;

import java.io.Serial;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class JobInitEnded extends AbstractMessage {
  @Serial
  private static final long serialVersionUID = 5040958276243865900L;

  private final boolean noObjectsOrchestrated;
  private final JobPluginInfo jobPluginInfo;

  public JobInitEnded(JobPluginInfo jobPluginInfo, boolean noObjectsOrchestrated) {
    super();
    this.jobPluginInfo = jobPluginInfo;
    this.noObjectsOrchestrated = noObjectsOrchestrated;
  }

  public JobPluginInfo getJobPluginInfo() {
    return jobPluginInfo;
  }

  public boolean isNoObjectsOrchestrated() {
    return noObjectsOrchestrated;
  }

  @Override
  public String toString() {
    return "JobInitEnded [noObjectsOrchestrated=" + noObjectsOrchestrated + ", jobPluginInfo=" + jobPluginInfo + "]";
  }
}
