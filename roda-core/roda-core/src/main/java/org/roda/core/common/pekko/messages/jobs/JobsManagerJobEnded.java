package org.roda.core.common.pekko.messages.jobs;

import java.io.Serial;

import org.roda.core.common.pekko.messages.AbstractMessage;
import org.roda.core.data.v2.jobs.JobParallelism;
import org.roda.core.data.v2.jobs.JobStats;
import org.roda.core.data.v2.jobs.PluginType;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class JobsManagerJobEnded extends AbstractMessage {
  @Serial
  private static final long serialVersionUID = -2514581679498648676L;

  private final String jobId;
  private final String plugin;
  private final PluginType pluginType;
  private final long duration;
  private final JobStats jobStats;
  private final JobParallelism parallelism;

  public JobsManagerJobEnded(String jobId, String plugin, PluginType pluginType, long duration, JobStats jobStats,
                             JobParallelism parallelism) {
    super();
    this.jobId = jobId;
    this.plugin = plugin;
    this.pluginType = pluginType;
    this.duration = duration;
    this.jobStats = jobStats;
    this.parallelism = parallelism;
  }

  public String getJobId() {
    return jobId;
  }

  public String getPlugin() {
    return plugin;
  }

  public PluginType getPluginType() {
    return pluginType;
  }

  public long getDuration() {
    return duration;
  }

  public JobStats getJobStats() {
    return jobStats;
  }

  public JobParallelism getJobParallelism() {
    return parallelism;
  }

  @Override
  public String toString() {
    return "JobsManagerJobEnded [jobId=" + jobId + ", plugin=" + plugin + ", pluginType=" + pluginType
        + ", jobParallelism=" + parallelism + "]";

  }
}
