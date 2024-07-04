package org.roda.core.common.pekko.messages.jobs;

import org.roda.core.data.v2.jobs.Job;
import org.roda.core.plugins.Plugin;

import java.io.Serial;
import java.util.Optional;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class JobStateUpdated extends JobStateDetailsUpdated {
  @Serial
  private static final long serialVersionUID = 1946036502369851214L;

  private final Job.JOB_STATE state;

  public JobStateUpdated(Plugin<?> plugin, Job.JOB_STATE state) {
    this(plugin, state, Optional.empty());
  }

  public JobStateUpdated(Plugin<?> plugin, Job.JOB_STATE state, Optional<String> stateDetails) {
    super(plugin, stateDetails);
    this.state = state;
  }

  public JobStateUpdated(Plugin<?> plugin, Job.JOB_STATE state, Throwable throwable) {
    this(plugin, state, Optional.of(throwable.getClass().getName() + ": " + throwable.getMessage()));
  }

  public Job.JOB_STATE getState() {
    return state;
  }

  @Override
  public String toString() {
    return "JobStateUpdated [plugin=" + getPlugin() + ", state=" + state + ", stateDetails=" + getStateDetails()
        + "]";
  }
}
