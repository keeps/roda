package org.roda.core.common.pekko.messages.jobs;

import java.io.Serial;

import org.roda.core.common.pekko.messages.AbstractMessage;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.orchestrate.JobPluginInfo;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class JobInfoUpdated extends AbstractMessage {
  @Serial
  private static final long serialVersionUID = -6918015956027259760L;

  private final Plugin<?> plugin;
  private final JobPluginInfo jobPluginInfo;

  public JobInfoUpdated(Plugin<?> plugin, JobPluginInfo jobPluginInfo) {
    super();
    this.plugin = plugin;
    this.jobPluginInfo = jobPluginInfo;
  }

  public Plugin<?> getPlugin() {
    return plugin;
  }

  public JobPluginInfo getJobPluginInfo() {
    return jobPluginInfo;
  }

  @Override
  public String toString() {
    return "JobInfoUpdated [plugin=" + plugin + ", jobPluginInfo=" + jobPluginInfo + "]";
  }
}
