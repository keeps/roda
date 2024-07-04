package org.roda.core.common.pekko.messages;

import java.io.Serial;
import java.io.Serializable;

import org.roda.core.data.v2.jobs.JobParallelism;
import org.roda.core.data.v2.jobs.JobPriority;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public abstract class AbstractMessage implements Serializable {
  @Serial
  private static final long serialVersionUID = 1898368418865765060L;
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMessage.class);

  private String uuid;
  private final long creationTime;
  private JobPriority jobPriority;
  private JobParallelism jobParallelism;

  protected AbstractMessage() {
    if (LOGGER.isTraceEnabled()) {
      uuid = IdUtils.createUUID();
      LOGGER.trace("{} Created message {}", uuid, getClass().getSimpleName());
    }

    creationTime = System.currentTimeMillis();
  }

  public void logProcessingStarted() {
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("{} Started processing message {} [{}]", uuid, getClass().getSimpleName(), toString());
    }
  }

  public void logProcessingEnded() {
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("{} Ended processing message {}", uuid, getClass().getSimpleName());
    }
  }

  public long getTimeSinceCreation() {
    return System.currentTimeMillis() - creationTime;
  }

  public AbstractMessage withJobPriority(JobPriority priority) {
    this.jobPriority = priority;
    return this;
  }

  public AbstractMessage withParallelism(JobParallelism parallelism) {
    this.jobParallelism = parallelism;
    return this;
  }

  public JobPriority getJobPriority() {
    return jobPriority;
  }

  public JobParallelism getParallelism() {
    return jobParallelism;
  }
}
